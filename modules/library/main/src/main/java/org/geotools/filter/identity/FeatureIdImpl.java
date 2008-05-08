package org.geotools.filter.identity;

import org.opengis.feature.Feature;
import org.opengis.filter.identity.FeatureId;
import org.opengis.filter.identity.Identifier;

/**
 * Implementation of {@link org.opengis.filter.identity.FeatureId}
 * 
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FeatureIdImpl implements FeatureId {

	/** underlying fid */
	String fid;
	
	public FeatureIdImpl( String fid ) {
		this.fid = fid;
		if ( fid == null ) {
			throw new NullPointerException( "fid must not be null" );
		}
	}
	
	public String getID() {
		return fid;
	}

	public boolean matches(Feature feature) {
		return feature != null && fid.equals( feature.getID() );
	}

//	public boolean matches(Object object) {
//		if ( object instanceof Feature ) {
//			return matches( (Feature) object );
//		}
//		
//		if ( object instanceof org.geotools.feature.Feature ) {
//			return fid.equals( ( (org.geotools.feature.Feature) object ).getID() );
//		}
//		
//		return false;
//	}

	public String toString() {
		return fid;
	}
	
	public boolean equals(Object obj) {
		if ( obj instanceof FeatureId) {
			return fid.equals( ((FeatureId)obj).getID() );
		}
		
		return false;
	}
	
	public int hashCode() {
		return fid.hashCode();
	}

}
