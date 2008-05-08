package org.geotools.feature.type;

import org.geotools.feature.AttributeTypeBuilder;
import org.geotools.feature.DefaultAttributeType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;

public class DefaultAttributeTypeBuilder extends AttributeTypeBuilder {

	public DefaultAttributeTypeBuilder() {
		super( new DefaultTypeFactory() );
	}

	private static class DefaultTypeFactory extends FeatureTypeFactoryImpl {
		public AttributeDescriptor createAttributeDescriptor(AttributeType type, Name name, int minOccurs, int maxOccurs, boolean isNillable, Object defaultValue) {
			return new DefaultAttributeType( type, name, minOccurs, maxOccurs, isNillable,defaultValue );
		}
		
		public GeometryDescriptor createGeometryDescriptor(GeometryType type,
		        Name name, int minOccurs, int maxOccurs, boolean isNillable,
		        Object defaultValue) {
		    return new GeometricAttributeType(type,name,minOccurs,maxOccurs,isNillable,defaultValue);
		}
	}
}
