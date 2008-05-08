package org.geotools.feature.type;

import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class GeometryDescriptorImpl extends AttributeDescriptorImpl 
    implements GeometryDescriptor {

    public GeometryDescriptorImpl(GeometryType type, Name name, int min,
            int max, boolean isNillable, Object defaultValue) {
        super(type, name, min, max, isNillable, defaultValue);
        
    }

    public GeometryType getType() {
        return (GeometryType) super.getType();
    }

	public CoordinateReferenceSystem getCRS() {
		return getType().getCRS();
	}

	public String getLocalName() {
		return getName().getLocalPart();
	}
}
