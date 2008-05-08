package org.geotools.feature;

import java.util.List;

import org.opengis.feature.Attribute;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;

public class LenientFeatureFactory extends FeatureFactoryImpl {

    @Override
    public Attribute createAttribute(Object value,
            AttributeDescriptor descriptor, String id) {
        return new LenientAttribute( value, descriptor, id );
    }
    public SimpleFeature createSimpleFeature(List<Attribute> properties, SimpleFeatureType type, String id) {
        LenientFeature newFeature = new LenientFeature( properties, type, id );
        
//        List<Object> values = new ArrayList<Object>();
//        for( Attribute attribute : properties ){
//            values.add( attribute.getValue() );
//        }
//        newFeature.setAttributes(values);
//        
        return newFeature;
    }
}
