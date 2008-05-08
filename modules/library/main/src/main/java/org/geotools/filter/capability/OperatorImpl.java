package org.geotools.filter.capability;

import org.opengis.filter.capability.Operator;

/**
 * Implementation of the Operator interface.
 * 
 * @author Justin Deoliveira, The Open Planning Project
 */
public class OperatorImpl implements Operator {
    private String name;
    
    public OperatorImpl( String name ) {
        this.name = name;
    }
    public OperatorImpl( Operator copy ) {
        this.name = copy.getName();
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }
    
    @Override
    public boolean equals( Object obj ) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final OperatorImpl other = (OperatorImpl) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
    @Override
    public String toString() {
        return getName();
    }
}
