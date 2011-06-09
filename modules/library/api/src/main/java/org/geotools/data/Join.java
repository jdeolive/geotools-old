package org.geotools.data;

import java.util.Collections;
import java.util.List;

import org.opengis.filter.Filter;
import org.opengis.filter.expression.PropertyName;

public class Join {

    public static enum Type {
        INNER, OUTER, LEFT, RIGHT;
    }

    /** join type */
    Type type;

    /**
     * the feature type name being joined to
     */
    String typeName;

    /**
     * attributes to fetch for this feature type 
     */
    List<PropertyName> properties = Query.ALL_PROPERTIES;

    /** 
     * the join predicate
     */
    Filter join;

    /**
     * additional predicate against the target of the join 
     */
    Filter filter;

    public Join(String typeName, Filter join, Filter filter) {
        this(Type.INNER, typeName, Query.ALL_PROPERTIES, join, filter);
    }

    public Join(Type type, String typeName, List<PropertyName> properties, Filter join, Filter filter) {
        this.type = type;
        this.typeName = typeName;
        this.properties = properties;
        this.join = join;
        this.filter = filter;
    }
    
    public void setType(Type type) {
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getTypeName() {
        return typeName;
    }

    public List<PropertyName> getProperties() {
        if (properties == Query.ALL_PROPERTIES) {
            return properties;
        }
        return Collections.unmodifiableList(properties);
    }
    
    public void setJoin(Filter join) {
        this.join = join;
    }

    public Filter getJoin() {
        return join;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

}
