/*
 * DefaultFeature.java
 *
 * Created on March 15, 2002, 3:46 PM
 */

package org.geotools.datasource;

import com.vividsolutions.jts.geom.*;
import java.util.Vector;

/**
 *
 * @author  jamesm
 */
public class DefaultFeature implements org.geotools.datasource.Feature {
    private String typeName = "feature";
    protected Object[] attributes;
    protected String[] colNames;
    protected int geomColumn = 0;//HACK: unsafe assumption?
    
    /** Creates a new instance of DefaultFeature */
    public DefaultFeature() {
        attributes = new Object[1];
        colNames = new String[]{"Geom"};
    }
    
    public void setAttributes(Object[] attributes,String[] colNames){
        this.attributes = attributes;
        this.colNames = colNames;
    }
    
    public Object[] getAttributes() {
        return attributes;
    }
    
    public String[] getAttributeNames() {
        return colNames;
    }
    
    public Geometry getGeometry() {
        return (Geometry)attributes[geomColumn];
    }
    
    public void setGeometry(Geometry geom) {
        attributes[geomColumn]=geom;
        colNames[geomColumn]="Geom";
        
    }
    
    public void setAttributes(Object[] a) {
        setAttributes(a,new String[]{});
    }
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    public String getTypeName() {
        return typeName;
    }
    
    public String toString() {
        StringBuffer featureString = new StringBuffer();
        Vector cols = new Vector( java.util.Arrays.asList(getAttributeNames()));
        Vector currentAttributes = new Vector( java.util.Arrays.asList(attributes) );
        currentAttributes.remove(getGeometry());
        cols.remove("Geom");
        
        featureString.append(typeName+":\n");
        if(cols != null && cols.size() > 0){
            featureString.append(" Names:       " + cols.toString() + "\n");
        }
        if(currentAttributes != null && currentAttributes.size() > 0){
            featureString.append(" attributes:  " + currentAttributes.toString() + "\n");
        }else{
            featureString.append(" No attributes set \n");
        }
        Geometry geometry = getGeometry();
        if(geometry != null){
            featureString.append(" geometry:    " + geometry.toString() + "\n");
        }else{
            featureString.append(" No Geometry set \n");
        }
        
        return featureString.toString();
        
        
    }
    
    
    
}
