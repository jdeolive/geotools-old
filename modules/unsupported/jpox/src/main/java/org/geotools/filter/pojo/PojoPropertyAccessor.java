package org.geotools.filter.pojo;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.expression.PropertyAccessor;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Accessor for working with pojo properties.
 * <ul>
 * <li><b>:</b></li>
 * <li><b>:</b></li>
 * s
 * </ul>
 * Examples:
 * 
 * <pre><code>
 * </code></pre>
 * 
 * @author Jody Garnett, Refractions Research Inc.
 */
class PojoPropertyAccessor implements PropertyAccessor {
    private BeanInfo info;
    private PropertyDescriptor property;

    PojoPropertyAccessor(BeanInfo info, PropertyDescriptor property) {
        this.info = info;
        this.property = property;
    }
    
    public boolean canHandle(Object object, String xpath, Class target) {
        // We can handle everything! ...yeah, right.. ;)
        return true;
    }
    
    PropertyDescriptor access( String name ){
        if( property != null ) return property;
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; i++) {
            if (descriptors[i].getName().equalsIgnoreCase(name)) {
                if( descriptors[i].getReadMethod() != null){
                    return descriptors[i];
                }
                else {
                    return null;
                }
            }
        }
        return null;
    }
    PropertyDescriptor accessDefaultGeometry(){
        PropertyDescriptor[] descriptors = info.getPropertyDescriptors();
        for (int i = 0; i < descriptors.length; i++) {
            if (Geometry.class.isAssignableFrom( descriptors[i].getPropertyType())) {
                if( descriptors[i].getReadMethod() != null){
                    return descriptors[i];
                }
            }
        }
        return null;
    }
    
    public Object get(Object object, String xpath, Class target) {
        if (object == null || xpath == null)
            return null;

        xpath = xpath.trim();
        try {
            if (xpath.equals("")) {
                if (target == Geometry.class) {
                    PropertyDescriptor property = accessDefaultGeometry();
                    if( property != null ){
                        Method getter = property.getReadMethod();
                        getter.setAccessible(true);
                        Object value = getter.invoke(object, null);
                        return value;                    
                    }
                }
                return null;
            }
            PropertyDescriptor property = access( xpath );
            if( property != null ){
                Method getter = property.getReadMethod();
                getter.setAccessible(true);
                Object value = getter.invoke(object, null);
                return value;
            }
            Field field = object.getClass().getDeclaredField(xpath);
            if (field != null) {
                field.setAccessible(true);
                return field.get(object);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void set(Object object, String xpath, Object value, Class target)
            throws IllegalAttributeException {
        // TODO Auto-generated method stub

    }

}
