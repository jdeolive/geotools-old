package org.geotools.jdbc;

import org.geotools.filter.visitor.DuplicatingFilterVisitor;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.expression.PropertyName;

public class JoinPrefixingVisitor2 extends DuplicatingFilterVisitor {

    SimpleFeatureType ft1, ft2;
    String alias1, alias2;
    
    public JoinPrefixingVisitor2(SimpleFeatureType ft, String alias) {
        this(ft, alias, null, null);
    }
    
    public JoinPrefixingVisitor2(SimpleFeatureType ft1, String alias1, SimpleFeatureType ft2, String alias2) {
        this.ft1 = ft1;
        this.ft2 = ft2;
        this.alias1 = alias1;
        this.alias2 = alias2;
    }

    @Override
    public Object visit(PropertyName expression, Object extraData) {
        String name = expression.getPropertyName();
        String[] split = name.split("\\.");

        //if split.length > 2 then join up remaining parts, means the column name itself had a 
        // period in it
        if (split.length > 2) {
            String prefix = split[0];
            StringBuffer sb = new StringBuffer();
            for (int i = 1; i < split.length; i++) {
                sb.append(split[i]);
            }
            split = new String[]{prefix, sb.toString()};
        }

        JoinPropertyName propertyName = null;

        //if we only have one feature type its easy, use the first feature type
        if (ft2 == null) {
            propertyName = new JoinPropertyName(ft1, alias1, split.length > 1 ? split[1] : split[0]);
        }
        else {
            if (split.length == 1) {
                //name was unprefixed, figure out what feature type the meant
                SimpleFeatureType ft = ft1.getDescriptor(split[0]) != null ? ft1 : 
                    ft2.getDescriptor(split[0]) != null ? ft2 : null; 
                if (ft == null) {
                    throw new IllegalArgumentException(String.format("Attribute '%s' not present in"
                     +   " either type '%s' or '%s'", split[0], ft1.getTypeName(), ft2.getTypeName()));
                }

                propertyName = new JoinPropertyName(ft, ft == ft1 ? alias1 : alias2, split[0]); 
            }
            else {
                //name was prefixed, look up the type based on prefix
                SimpleFeatureType ft = split[0].equals(alias1) ? ft1 : 
                    split[0].equals(alias2) ? ft2 : null;
                if (ft == null) {
                    throw new IllegalArgumentException(String.format("Prefix '%s' does not match " +
                        "either alias '%s' or '%s'", split[0], alias1, alias2));
                }
                
                propertyName = new JoinPropertyName(ft, ft == ft1 ? alias1 : alias2, split[1]); 
            }
        }
        
        return propertyName;
    }
}
