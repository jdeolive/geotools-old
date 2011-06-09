package org.geotools.jdbc;

import org.geotools.filter.AttributeExpressionImpl;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Property name that knows what feature type it comes from.
 * <p>
 * Used by the sql encoder to determine how to property encode the join query. 
 * </p>
 * 
 * @author Justin Deoliveira, OpenGeo
 */
public class JoinPropertyName extends AttributeExpressionImpl {

    SimpleFeatureType featureType;
    String alias;
    
    public JoinPropertyName(SimpleFeatureType featureType, String alias, String name) {
        super(name);
        this.featureType = featureType;
        this.alias = alias;
    }

    public SimpleFeatureType getFeatureType() {
        return featureType;
    }

    public String getAlias() {
        return alias;
    }
}
