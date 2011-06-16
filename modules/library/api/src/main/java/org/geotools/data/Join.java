package org.geotools.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.PropertyName;

public class Join {

    /**
     * filter factory
     */
    static final FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    /**
     * type of join
     */
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

    /**
     * The alias to be used for the typeName in this join
     */
    String alias;

    public Join(String typeName, Filter join) {
        this.typeName = typeName;
        this.join = join;
        this.type = Type.INNER;
        this.properties = Query.ALL_PROPERTIES;
        this.filter = Filter.INCLUDE;
        this.alias = null;
    }

    public Join(Join other) {
        this.typeName = other.getTypeName();
        this.join = other.getJoinFilter();
        this.filter = other.getFilter();
        this.type = other.getType();
        this.properties = other.getProperties();
        this.filter = other.getFilter();
        this.alias = other.getAlias();
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

    public String[] getPropertyNames() {
        if (properties == Query.ALL_PROPERTIES) {
            return Query.ALL_NAMES;
        }
        
        String[] names = new String[properties.size()];
        for (int i = 0; i < names.length; i++) {
            names[i] = properties.get(i).getPropertyName();
        }
        return names;
    }

    public void setJoin(Filter join) {
        this.join = join;
    }

    public Filter getJoinFilter() {
        return join;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public Filter getFilter() {
        return filter;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    public Join properties(String... properties) {
        this.properties = new ArrayList();
        for (String p : properties) {
            this.properties.add(ff.property(p));
        }
        return this;
    }

    public Join filter(Filter filter) {
        setFilter(filter);
        return this;
    }

    public Join alias(String alias) {
        setAlias(alias);
        return this;
    }

    public Join type(Type type) {
        setType(type);
        return this;
    }
}
