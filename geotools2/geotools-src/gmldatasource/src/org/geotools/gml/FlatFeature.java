/*
 * DefaultFeature.java
 *
 * Created on March 15, 2002, 3:46 PM
 */

package org.geotools.gml;

import java.util.*;

import org.geotools.datasource.*;

import com.vividsolutions.jts.geom.*;

/**
 * <p>Describes a flat feature, which is a simplification of the allowed GML
 * feature types.  We define a <code>FlatFeature</code> as feature with the
 * following properties:<ul>
 * <li>Contains a single geometry element.
 * <li>Contains an arbitrary number of non-geometric properties, which are
 * all simple.  Simple means that the properties are in the set: {String,
 * Int, Double, Boolean}.</ul>
 *
 * @author Rob Hranac, Vision for New York
 *
 * $Id: FlatFeature.java,v 1.5 2002/05/03 11:46:44 ianturton Exp $
 */
public class FlatFeature implements Feature {
    
    // A COUPLE OF IMPORTANT POINTS TO DEVELOPERS...
    // GIVEN THE CURRENT STRUCTURE OF THE FEATURE INTERFACE,
    // THIS FEATURE OBJECT DOES NOT DO ANY INTERNAL CHECKS TO MAKE
    // SURE THAT IT COMPLIES WITH OUR DEFINITION OF A FLAT FEATURE.
    // INSTEAD, THESE CHECKS ARE PERFORMED IN FLATFEATURETABLE,
    // THEY SHOULD REALLY BE HERE, BUT THAT WOULD NECESSITATE A DESIGN
    // CHANGE TO THE FEATURE INTERFACE, WHICH IS UP TO JAMES AND IAN.
    
    /** Geometry element for the feature */
    private Geometry geometry;
    
    /** Attributes for the feature */
    private Object[] attributes;
    
    /** Attributes for the feature */
    private String[] attributeNames;
    
    private String typeName = "FeatureType";
    /**
     * Creates a new instance of DefaultFeature
     */
    public FlatFeature() {
    }
    
    /**
     * Creates a new instance of DefaultFeature
     */
    public FlatFeature(int numAttributes) {
        attributes = new Object[numAttributes];
    }
    
    
    public void setAttributes(Object[] attributes, String[] colNames){
        this.attributes = attributes;
        this.attributeNames = colNames;
    }
    
    
    public Object[] getAttributes() {
        return attributes;
    }
    
    
    public String[] getAttributeNames() {
        return attributeNames;
    }
    
    
    public Geometry getGeometry() {
        return this.geometry;
    }
    
    
    public void setGeometry(Geometry geom) {
        this.geometry = geom;
        
    }
    
    
    public void setAttributes(Object[] a) {
        setAttributes(a,new String[]{});
    }
    
    public String getTypeName(){
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
    
    public String toString() {
        StringBuffer featureString = new StringBuffer();
        Vector currentAttributes = new Vector( java.util.Arrays.asList(attributes) );
        
        featureString.append("\n");
        if(currentAttributes != null){
            featureString.append(" attributes:  " + currentAttributes.toString() + "\n");
        }else{
            featureString.append(" No attributes set \n");
        }
        if(geometry != null){
            featureString.append(" geometry:    " + geometry.toString() + "\n");
        }else{
            featureString.append(" No Geometry set \n");
        }
        
        return featureString.toString();
        
        
    }
    
    
    
}
