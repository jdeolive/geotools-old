/*
 * Dimension.java
 *
 * Created on 26 November 2003, 12:45
 */

package org.geotools.wms.gtserver;

/**
 *
 * @author  iant
 */
public class Dimension {
    String name;
    String column;
    /** Creates a new instance of Dimension */
    public Dimension() {
    }
    public Dimension(String name, String column){
        this.name=name;
        this.column = column;
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
    
    /** Getter for property column.
     * @return Value of property column.
     *
     */
    public java.lang.String getColumn() {
        return column;
    }
    
    /** Setter for property column.
     * @param column New value of property column.
     *
     */
    public void setColumn(java.lang.String column) {
        this.column = column;
    }
    
}
