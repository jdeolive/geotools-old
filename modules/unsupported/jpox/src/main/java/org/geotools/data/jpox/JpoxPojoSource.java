package org.geotools.data.jpox;

import java.awt.geom.Rectangle2D;
import java.net.URI;
import java.util.Collection;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.swing.Icon;

import org.geotools.catalog.GeoResourceInfo;
import org.geotools.catalog.defaults.DefaultGeoResourceInfo;
import org.geotools.data.Source;
import org.geotools.filter.pojo.JDOQLEncoder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.jpox.PersistenceManagerFactoryImpl;
import org.jpox.store.rdbms.spatial.JtsSpatialHelper;
import org.opengis.feature.type.TypeName;
import org.opengis.filter.Filter;
import org.opengis.filter.capability.FilterCapabilities;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Envelope;

public class JpoxPojoSource implements Source {

	public static final String JPOX_STATE_KEY = "JPOX";
	private PersistenceManagerFactoryImpl pmf;
	private PersistenceManager pm;
	private Class pc;
	private org.geotools.data.Transaction t;
	private GeoResourceInfo info;
	private JtsSpatialHelper helper;
	private String[] geometryFieldNames;

	public JpoxPojoSource( PersistenceManagerFactoryImpl pmf, Class pc ) {
		this.pmf = pmf;
		this.pc = pc;
		helper = new JtsSpatialHelper( pmf );
		geometryFieldNames = helper.getGeometryColumnBackedFields( pc );
		if ( geometryFieldNames.length == 0 ) throw new IllegalArgumentException( "No geometry column backed fields in class " + pc.getName() );
	}

	public GeoResourceInfo getInfo() {
		if ( info == null ) {
			info = new JpoxGeoResourceInfo( pc.getName(), pc.getName(), "Data source for " + pc.getName(), null, null, new String[] {}, null );
		}
		return info;
	}
	
	protected String getDefaultGeometryField() {
		return geometryFieldNames[0];
	}

	public Collection content() {
		return (Collection)new Executor() {
			Query getQuery() {
				return getPm().newQuery( pc );
			}
		}.execute();
	}
	
	public Collection content( String query, String queryLanguage ) {		
		final Query q;
		if ( query == null || query.equals( "" ) ) {
			q = getPm().newQuery();			
		} else {
			q = getPm().newQuery( queryLanguage, query  );
		}
		q.setClass( pc );
		
		return (Collection)new Executor() {
			Query getQuery() {
				return q;
			}
		}.execute();
	}

	public Collection content( Filter filter ) {
		String fieldName = getDefaultGeometryField();
		Integer srid = helper.getSridFromJdoMetadata( pc, fieldName );
		if ( srid == null ) {
			srid = helper.getSridFromDatastoreMetadata( pc, fieldName, getPm() );
			if ( srid == null ) {
				srid = helper.getSridFromDatastoreEntry( pc, fieldName, getPm() );
				if ( srid == null ) {
					// Just choose something
					srid = Integer.valueOf( -1 );
				}
			}
		}
		JDOQLEncoder encoder = new JDOQLEncoder( srid.intValue() );
		final StringBuffer query = new StringBuffer();
		filter.accept( encoder, query );
		
		return (Collection)new Executor() {
			Query getQuery() {
				return getPm().newQuery( pc, query.toString() );
			}
		}.execute();
	}

	public Object describe() {
		return pc;
	}

	public FilterCapabilities getFilterCapabilities() {
		return null;
	}

	public TypeName getName() {
		return new org.geotools.feature.type.TypeName( pc.getName() );
	}
	
	public void setTransaction( org.geotools.data.Transaction t ) {
		this.t = t;
	}
    public void dispose() {
        if( t != null ){
            JpoxTransactionState state = (JpoxTransactionState)t.getState( JPOX_STATE_KEY );
            if( state != null ){
                state.setTransaction( null ); // cleanup!                
                state = null;
                t.putState( JPOX_STATE_KEY, null );
            }
            t = null;
        }
        pm.close();
        pm = null;
        pc = null;
    }

    protected Transaction tx() {
    	return getPm().currentTransaction();
    }
    
    protected PersistenceManager getPm() {
    	if ( t == null || t == org.geotools.data.Transaction.AUTO_COMMIT ) {
    		if ( pm == null ) {
    			pm = pmf.getPersistenceManager();
    		}
    		return pm;
    	} 
    	
    	JpoxTransactionState state = (JpoxTransactionState)t.getState( JPOX_STATE_KEY );
    	
    	if ( state == null ) {
    		state = new JpoxTransactionState( pmf.getPersistenceManager() );
    		t.putState( JPOX_STATE_KEY, state );
    	}
    	
    	return state.getPm();
    }

    private final class JpoxGeoResourceInfo extends DefaultGeoResourceInfo {

		private CoordinateReferenceSystem crs = null;

		private JpoxGeoResourceInfo( String title, String name, String description, URI schema, ReferencedEnvelope bounds, String[] keywords, Icon icon ) {
			super( title, name, description, schema, bounds, keywords, icon );
		}

		public Envelope getBounds() {
			// Lazily compute the bounds, using Spatial-Helper.
			// Bounds are newly calculated for every call, because
			// content of the datastore could have changed in the meantime
			String field = getDefaultGeometryField();
			Rectangle2D bbox = helper.estimateBoundsFromDatastoreMetadata( pc, field, getPm() );
			if ( bbox == null ) {
				bbox = helper.calculateBoundsInDatastore( pc, field, getPm() );
				if ( bbox == null ) {
					// Bring out the big guns!
					bbox = helper.calculateBoundsFromALLDatastoreEntries( pc, field, getPm() );
					if ( bbox == null ) return null;
				}
				
			}
			
			return  new ReferencedEnvelope( bbox.getMinX(), bbox.getMaxX(), bbox.getMinY(), bbox.getMaxY(), getCRS() );
		}

		public CoordinateReferenceSystem getCRS() {
			// Lazily get the CRS, using Spatial-Helper
			// and cache it for later reuse
			if ( crs == null ) {
				String field = getDefaultGeometryField();
				Integer srid = helper.getSridFromJdoMetadata( pc, field );
				if ( srid == null ) {
					srid = helper.getSridFromDatastoreMetadata( pc, field, getPm() );
					if ( srid == null ) {
						srid = helper.getSridFromDatastoreEntry( pc, field, getPm() );
					}
				}
				if ( srid == null ) return null;
				
				String wkt = helper.getCrsWktForSrid( pc, srid.intValue(), getPm() );
				
				if ( wkt != null ) {
					try {
						crs = CRS.parseWKT( wkt );
					} catch ( FactoryException e ) {
						//TODO: log!
						e.printStackTrace();
					}
				}
				
				if ( crs == null ) {
					String crsName = helper.getCrsNameForSrid( pc, srid.intValue(), getPm() );
					if ( crsName == null ) { 
						// A last desperate attempt at finding the CRS
						crsName = "EPSG:" + srid;
					}
					try {
						crs = CRS.decode( crsName );
					} catch ( Exception e ) {
						//TODO: log!
						e.printStackTrace();
					}
				}
				
			}
			return crs;
		}
	}

	private abstract class Executor {
    	Object execute() {
    		boolean isActive = tx().isActive();
    		
    		if ( !isActive ) tx().begin();
    		Query query = getQuery();
    		//TODO: Check if we can close transaction??
    		return query.execute();
    	}
    	
    	abstract Query getQuery();
    }
    
}

