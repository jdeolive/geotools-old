/*
 * AutoStyle.java
 *
 * Created on 08 December 2003, 12:31
 */

package org.geotools.wms.gtserver;

import java.util.ArrayList;

/**
 *
 * @author  iant
 */
public class AutoStyle {
    int nClasses = 0;
    String name = "";
    ArrayList colors = new ArrayList();
    /** Creates a new instance of AutoStyle */
    public AutoStyle() {
    }
    
    public void addColor(String color){
        colors.add(color);
    }
    
    public boolean removeColor(String color){
        return colors.remove(color);
    }
    
    /** Getter for property nClasses.
     * @return Value of property nClasses.
     *
     */
    public int getNClasses() {
        return nClasses;
    }
    
    /** Setter for property nClasses.
     * @param nClasses New value of property nClasses.
     *
     */
    public void setNClasses(int nClasses) {
        this.nClasses = nClasses;
    }
    
    /** Getter for property name.
     * @return Value of property name.
     *
     */
    public java.lang.String getName() {
        return name;
    }
    
    /** Setter for property name.
     * @param name New value of property name.
     *
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    public String[] getColors(){
        return (String[])colors.toArray(new String[0]);
    }
}
