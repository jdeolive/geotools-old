package org.geotools.filter.expression;

import org.geotools.Builder;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.PropertyName;

public class PropertyNameBuilder implements Builder<PropertyName> {
    protected FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);    
    String xpath = null; // will result in Expression.NIL
    boolean unset = false;
    
    public PropertyNameBuilder(){
         reset();        
    }
    public PropertyNameBuilder( PropertyName propertyName ){
        reset( propertyName );        
    }
    public PropertyNameBuilder property( String xpath ){
        this.xpath = xpath;
        unset = false;
        return this;
    }
    public PropertyName build() {
        if( unset ){
            return null;
        }
        return ff.property( xpath );
    }

    public PropertyNameBuilder reset() {
        unset = false;
        xpath = null;
        return this;
    }

    public PropertyNameBuilder reset( PropertyName original) {
        unset = false;
        xpath = original.getPropertyName();
        return this;
    }

    public PropertyNameBuilder unset() {
        unset = true;
        xpath = null;
        return this;
    }

}
