package org.geotools.jdbc;


/**
 * Represents a column in a primary key.
 * 
 * @author Justin Deoliveira, OpenGEO
 *
 */
public abstract class PrimaryKeyColumn {

    String name;
    
    Class type;
  
    protected PrimaryKeyColumn( String name, Class type ) {
        this.name = name;
        this.type = type;
    }
    
    public String getName() {
        return name;
    }
    
    public Class getType() {
        return type;
    }
    
}
