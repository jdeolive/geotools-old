/*
 * GlyphProperty.java
 *
 * Created on April 15, 2004, 11:50 AM
 */

package org.geotools.renderer.lite;

/**
 *
 * @author  jfc173
 */
public class GlyphProperty {
    
    private String name;
    private Class type;
    private Object value;
    
    /** Creates a new instance of GlyphProperty */
    public GlyphProperty(String s, Class c, Object o) {
        name = s;
        type = c;
        value = o;
    }
    
    public String getName(){
        return name;
    }
    
    public Class getType(){
        return type;
    }
    
    public Object getValue(){
        return value;
    }
    
    public void setValue(Object v){
        value = v;
    }
    
}
