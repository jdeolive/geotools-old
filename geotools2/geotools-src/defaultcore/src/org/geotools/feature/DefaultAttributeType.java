/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.feature;

import com.vividsolutions.jts.geom.Geometry;
import java.util.*;


/**
 * Simple, immutable class to store attributes.  This class should be
 * sufficient for all simple (ie. non-schema) attribute implementations of
 * this interface.
 *
 * @author Rob Hranac, VFNY
 * @author Chris Holmes, TOPP
 * @version $Id: DefaultAttributeType.java,v 1.3 2003/07/18 19:41:57 ianschneider Exp $
 */
public class DefaultAttributeType implements AttributeType {
    /** Name of this attribute. */
    protected final String name;

    /** Class type of this attribute. */
    protected final Class type;

    /** Indicates if nulls are allowed for this attribute */
    protected final boolean nillable;

    /**
     * Constructor with name and type.
     *
     * @param name Name of this attribute.
     * @param type Class type of this attribute.
     *
     * @task REVISIT: consider making protected and moving to factory pattern,
     *       though these may be too small to make that worth it.
     */
    public DefaultAttributeType(String name, Class type,boolean nillable) {
        this.name = name == null ? "" : name;
        this.type = type == null ? Object.class : type;
        this.nillable = nillable;
    }


    /**
     * Occurances no longer used, use MultiAttributeType for this
     * functionality. cholmes Constructor with geometry.
     *
     * @param position Name of this attribute.
     */
    /*public DefaultAttributeType (String name, Class type, int occurrences) {
       this.name = name;
       this.type = type;
       this.occurrences = occurrences;
       }*/


    /**
     * False, since it is not a schema.
     *
     * @return False.
     */
    public boolean isNested() {
        return false;
    }

    /**
     * Gets the name of this attribute.
     *
     * @return The name of this attribute.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the type of this attribute.  All attributes that are assigned to
     * this AttributeType must be an instance of this class.  Subclasses are
     * allowed as well.
     *
     * @return The class that the attribute must match to be valid for this
     *         AttributeType.
     */
    public Class getType() {
        return type;
    }

    /**
     * Removed from interface, and thus here too -ch. 
     * Gets the occurrences of this attribute.
     *
     * @return Occurrences.
     */
    //public int getOccurrences() {
    //    return occurrences;
    //}

    /**
     * Gets the position of this attribute.
     * Removed - Ian
     * @return Position.
     */
//    public int getPosition() {
//        return position;
//    }

    /**
     * Returns whether nulls are allowed for this attribute.
     *
     * @return true if nulls are permitted, false otherwise.
     */
    public boolean isNillable() {
        return nillable;
    }
    
    public int hashCode() {
      return name.hashCode() * type.hashCode();
    }
    
    public boolean equals(Object other) {
      if (other == null) return false;
      AttributeType att = (AttributeType) other;
      if (name == null) {
        if (att.getName() != null)
          return false;
      }
      if (!name.equals(att.getName()))
        return false;
      if (!type.equals(att.getType()))
        return false;
      return true;
    }


    /**
     * Returns whether the attribute is a geometry.
     *
     * @return true if the attribute's type is a geometry.
     */
    public boolean isGeometry() {
        return Geometry.class.isAssignableFrom(this.type);
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Gets a representation of this object as a string.
     *
     * @return A representation of this object as a string
     */
    public String toString() {
        String details = "name=" + name;
        details += " , type=" + type;
        details += " , nillable=" + nillable;
        
        return "DefaultAttributeType [" + details + "]";
    }

    /**
     * Whether the tested object passes the validity constraints of  this
     * AttributeType.  At a minimum it should be of the correct class
     * specified by {@link #getType()}, non-null if isNillable is
     * <tt>false</tt>, and a geometry if isGeometry is <tt>true</tt>
     *
     * @param attribute The object to be tested for validity.
     *
     * @return <tt>true</tt> if the object is allowed to be an attribute for
     *         this type, <tt>false</tt> otherwise.
     * @task REVISIT: throw exception if not valid?  The current way loses
     *       reporting on why the object is not valid.  Would be nice if
     *       there was a way to get that back, not null, wrong class, ect.
     */
    public boolean isValid(Object attribute) {
        boolean isValid = false;

        if (attribute != null) { //check if attribute is null
            //if not check to make sure it's the right class.
            isValid = getType().isAssignableFrom(attribute.getClass());
        } else {
            if (!isNillable()) {
                isValid = false; //attribute is null but nils not allowed.
            } else {
                isValid = true;
            }
        }

        return isValid;
    }
    
    public Object parse(Object value) throws IllegalArgumentException {
      return value;
    }
    
    public void validate(Object attribute) throws IllegalArgumentException {
      if (attribute == null && ! isNillable())
        throw new IllegalArgumentException(getName() + " is not nillable");
      if (!type.isAssignableFrom(attribute.getClass()))
        throw new IllegalArgumentException(attribute.getClass().getName() + 
        " is not an acceptable class for " + getName());
    }
    
    
    static class Numeric extends DefaultAttributeType {
      public Numeric(String name,Class type,boolean nillable) {
        super(name, type,nillable);
        if (!Number.class.isAssignableFrom(type))
          throw new IllegalArgumentException("Numeric requires Number class, " +
          "not " + type);
      }
      
      public Object parse(Object value) throws IllegalArgumentException {
        if (value == null) return value;
        if (type == Byte.class) return Byte.decode(value.toString());
        if (type == Short.class) return Short.decode(value.toString());
        if (type == Integer.class) return Integer.decode(value.toString());
        if (type == Float.class) return Float.valueOf(value.toString());
        if (type == Double.class) return Double.valueOf(value.toString());
        if (type == Long.class) return Long.decode(value.toString());
        throw new RuntimeException("DefaultAttributeType.Numeric is coded wrong");
      }
    }
    
    static class Feature extends DefaultAttributeType {
      private final FeatureType featureType;
      public Feature(String name,FeatureType type,boolean nillable) {
        super(name,org.geotools.feature.Feature.class,nillable); 
        this.featureType = type;
      }
      
      public void validate(Object attribute) throws IllegalArgumentException {
        super.validate(attribute);
        org.geotools.feature.Feature att = (org.geotools.feature.Feature) attribute;
        if (! att.getFeatureType().isDescendedFrom(featureType) || 
            ! att.getFeatureType().equals(featureType))
          throw new IllegalArgumentException("Not correct FeatureType");
      }
      
    }
}
