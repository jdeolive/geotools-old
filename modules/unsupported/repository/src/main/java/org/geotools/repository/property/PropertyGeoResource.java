package org.geotools.repository.property;

import java.io.File;
import java.io.IOException;

import org.geotools.repository.FeatureSourceGeoResource;
import org.geotools.util.ProgressListener;

public class PropertyGeoResource extends FeatureSourceGeoResource {

	public PropertyGeoResource( PropertyService service, String name ) {
		super( service, name );
	}

	public boolean canResolve(Class adaptee) {
		if ( adaptee == null) 
			return false;
	
		
		if ( adaptee.isAssignableFrom( File.class ) ) {
			return true;
		}
		
		return super.canResolve( adaptee );
	}
	
	public Object resolve(Class adaptee, ProgressListener monitor) throws IOException {
		if ( adaptee == null ) 
			return null;
		
		if ( adaptee.isAssignableFrom( File.class ) ) {
			PropertyService service = (PropertyService) parent( monitor );
			return new File( service.directory, getName() + ".properties" );
		}
		
		return super.resolve( adaptee, monitor );
	}
	
}
