package org.geotools.feature;

import java.util.List;

import org.geotools.feature.type.FeatureTypeFactoryImpl;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.feature.type.GeometryType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.util.InternationalString;

/**
 * Extension of FeatureTypeFactoryImpl which creates instances of DefaultFeatureType.
 * <p>
 * The point of this class is to maintain backwards compatability internally and
 * it should not be used by any client code. 
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 * @deprecated
 * @since 2.5
 */
public class DefaultFeatureTypeFactory2 extends FeatureTypeFactoryImpl {

    public SimpleFeatureType createSimpleFeatureType(Name name,
            List<AttributeDescriptor> schema,
            GeometryDescriptor defaultGeometry, boolean isAbstract,
            List<Filter> restrictions, AttributeType superType,
            InternationalString description) {
        return new DefaultFeatureType(name, schema, defaultGeometry, isAbstract,
                restrictions, superType, description);
    }
}
