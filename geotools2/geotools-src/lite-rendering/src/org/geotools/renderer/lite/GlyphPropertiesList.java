/*
 * GlyphPropertiesList.java
 *
 * Created on April 6, 2004, 4:11 PM
 */

package org.geotools.renderer.lite;

import java.util.Vector;
import org.geotools.filter.Expression;
import org.geotools.filter.FilterFactory;

/**
 *
 * @author  jfc173
 */
public class GlyphPropertiesList {
    
    private Vector list = new Vector();
    private Vector names = new Vector();
    private int propertyCount = 0;
    private FilterFactory factory = FilterFactory.createFilterFactory();
        
    /** Creates a new instance of GlyphPropertiesList */
    public GlyphPropertiesList() {
    }
    
    public void addProperty(String name, Class type, Object value){
//        if (value.getClass().equals(type)){  
            list.add(name);
            names.add(name);
            list.add(type);
            list.add(value);
            propertyCount++;   
//        } else {
//            throw new RuntimeException("Wrong class for setting variable " + name + ".  Expected a " + type + " but received a " + value.getClass() + ".");
//        }
    }
    
    /**
     * the index i starts counting at 0, not 1.  A list with two properties has property 0 and property 1.
     */
    public String getPropertyName(int i){
        return (String) list.elementAt(i * 3);
    }
    
    public int getPropertyIndex(String name){
        return (list.indexOf(name) / 3);
    }
    
    /**
     * the index i starts counting at 0, not 1.  A list with two properties has property 0 and property 1.
     */
    public Class getPropertyType(int i){
        return (Class) list.elementAt((i * 3) + 1);
    }
    
    public Class getPropertyType(String name){
        int index = list.indexOf(name); 
        if (index != -1){
            return (Class) list.elementAt(index + 1);
        } else {
            throw new RuntimeException("Tried to get the class of a non-existent property: " + name);
        }
    }
    
    public boolean hasProperty(String name){
        return names.contains(name);
    }
    
    /**
     * the index i starts counting at 0, not 1.  A list with two properties has property 0 and property 1.
     */
    public Object getPropertyValue(int i){
        return list.elementAt((i * 3) + 2);
    }
 
    public Object getPropertyValue(String name){
        int index = list.indexOf(name); 
        if (index != -1){
            return list.elementAt(index + 2);
        } else {
            throw new RuntimeException("Tried to get the class of a non-existent property: " + name);
        }
    }    
    
    private Expression stringToLiteral(String s){
        return factory.createLiteralExpression(s);
    }
    
    
    private Expression numberToLiteral(Double d){
        return factory.createLiteralExpression(d.doubleValue());
    }
    
    private Expression numberToLiteral(Integer i){
        return factory.createLiteralExpression(i.intValue());
    }
    
    public void setPropertyValue(String name, int value){
        setPropertyValue(name, new Integer(value));        
    }
    
    public void setPropertyValue(String name, double value){
        setPropertyValue(name, new Double(value));
    }
    
    public void setPropertyValue(String name, Object value){
        int index = list.indexOf(name); 
        if (index != -1){
            Class t = (Class) list.elementAt(index + 1);
            if (value instanceof String){
                value = stringToLiteral((String) value);
            }
            if (value instanceof Integer){                
                value = numberToLiteral((Integer) value);
            }
            if (value instanceof Double){
                value = numberToLiteral((Double) value);
            }
            if (t.isAssignableFrom(value.getClass())){
//            if (value.getClass().equals(t)){  
                list.setElementAt(value, index + 2);
            } else {
                throw new RuntimeException("Wrong class for setting variable " + name + ".  Expected a " + list.elementAt(index + 1) + " but received a " + value.getClass() + ".");
            }
        } else {
            throw new RuntimeException("Tried to set the value of a non-existent property: " + name);
        }
    }
}
