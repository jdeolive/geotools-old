/*
 * StyledLayer.java
 *
 * Created on November 3, 2003, 10:02 AM
 */

package org.geotools.styling;

/**
 *
 * @author  jamesm
 */
public abstract class StyledLayer { 
    protected String name;
    
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name = name;
    }
    
}
