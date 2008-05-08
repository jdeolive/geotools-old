package org.geotools.filter.identity;

import org.opengis.filter.identity.GmlObjectId;

/**
 * Implementation of {@link org.opengis.filter.identity.GmlObjectId}.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class GmlObjectIdImpl implements GmlObjectId {

	/** the object id */
	String gmlId;
	
	public GmlObjectIdImpl( String gmlId ) {
		this.gmlId = gmlId;
		if ( gmlId == null ) {
			throw new NullPointerException( "id can not be null" );
		}
	}
	
	public String getID() {
		return gmlId;
	}

	public boolean matches( Object object ) {
		if ( object instanceof org.opengis.feature.Feature ) {
			return new FeatureIdImpl( gmlId ).matches( (org.opengis.feature.Feature) object );
		}
		
		//TODO: geometries
		return false;
	}
	
	public String toString() {
		return gmlId;
	}
	
	public boolean equals(Object obj) {
		if ( obj instanceof GmlObjectIdImpl ) {
			GmlObjectIdImpl other = (GmlObjectIdImpl) obj;
			return gmlId.equals( other.gmlId );
		}
		
		return false;
	}
	
	public int hashCode() {
		return gmlId.hashCode();
	}
	

}
