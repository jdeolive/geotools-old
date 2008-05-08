package org.geotools.feature.type;

import java.util.HashMap;
import java.util.Map;

import org.geotools.resources.Utilities;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.feature.type.PropertyType;

public class PropertyDescriptorImpl implements PropertyDescriptor {

    final protected PropertyType type;
    final protected Name name;
    final protected int minOccurs;
    final protected int maxOccurs;
    final protected boolean isNillable;
    final Map<Object, Object> userData;
    
    protected PropertyDescriptorImpl(PropertyType type, Name name, int min, int max, boolean isNillable) {
        this.type = type;
        this.name = name;
        this.minOccurs = min;
        this.maxOccurs = max;
        this.isNillable = isNillable;
        userData = new HashMap();
        
        if ( type == null ) {
            throw new NullPointerException("type");
        }
        
        if ( name == null ) {
            throw new NullPointerException("name");
        }
        
        if (type == null) {
            throw new NullPointerException();
        }
        
        if (max > 0 && (max < min) ) {
            throw new IllegalArgumentException("max must be -1, or < min");
        }
    }
    
    public PropertyType getType() {
        return type;
    }
    
    public Name getName() {
        return name;
    }

    public int getMinOccurs() {
        return minOccurs;
    }

    public int getMaxOccurs() {
        return maxOccurs;
    }
    
    public boolean isNillable() {
        return isNillable;
    }
    
    public Map<Object, Object> getUserData() {
        return userData;
    }
    
    public boolean equals(Object obj) {
        if (!(obj instanceof PropertyDescriptorImpl)) {
            return false;
        }
        
        PropertyDescriptorImpl other = (PropertyDescriptorImpl) obj;
        return Utilities.equals(type,other.type) && 
            Utilities.equals(name,other.name) && 
            (minOccurs == other.minOccurs) && (maxOccurs == other.maxOccurs) &&
            (isNillable == other.isNillable);
    }
    
    public int hashCode() {
        return (37 * minOccurs + 37 * maxOccurs ) ^ type.hashCode() ^ name.hashCode();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append( getClass().getSimpleName()).append(":");
        sb.append( "type=").append(type).append(";name=").append(name);
        sb.append( ";minOccurs=").append(minOccurs).append(";maxOccurs=").append(maxOccurs);
        sb.append( ";isNillable=").append(isNillable);
        
        return sb.toString();
    }
    
}