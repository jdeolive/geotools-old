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

import java.util.*;


/**
 * A basic implementation of FeatureType.
 *
 * @author Ian Schneider
 * @version $Id: DefaultFeatureType.java,v 1.3 2003/07/17 16:14:02 ianschneider Exp $
 */
public class DefaultFeatureType implements FeatureType {
    private final String typeName;
    private final String namespace;
    private final AttributeType[] types;
    private final FeatureType[] ancestors = new FeatureType[] {
      FeatureType.GML_FEATURE 
    };
    private final AttributeType defaultGeom;
    private final int defaultGeomIdx;

    public DefaultFeatureType(String typeName, String namespace, List types,
        AttributeType defaultGeom) {
        if (typeName == null)
          throw new NullPointerException(typeName);
        this.typeName = typeName;
        this.namespace = (namespace == null) ? "" : namespace;
        this.types = (DefaultAttributeType[]) types.toArray(new DefaultAttributeType[types.size()]);
        
        // do this first...
        this.defaultGeomIdx = find(defaultGeom);
        // before doing this
        this.defaultGeom = defaultGeom;
    }

    
    public Feature create(Object[] attributes) throws IllegalAttributeException {
        return new DefaultFeature(this, attributes);
    }

    public Feature create(Object[] attributes, String featureID)
        throws IllegalAttributeException {
        return new DefaultFeature(this, attributes, featureID);
    }

    public AttributeType getDefaultGeometry() {
        return defaultGeom;
    }

    public AttributeType getAttributeType(String xPath) {
        for (int i = 0, ii = types.length; i < ii; i++) {
            if (types[i].getName().equals(xPath)) {
                return types[i];
            }
        }

        return null;
    }

    public int find(AttributeType type) {
        if (type == defaultGeom) {
            return defaultGeomIdx;
        }

        for (int i = 0, ii = types.length; i < ii; i++) {
            if (types[i].equals(type)) {
                return i;
            }
        }

        return -1;
    }

    public AttributeType getAttributeType(int position) {
        return types[position];
    }

    public AttributeType[] getAttributeTypes() {
        return (AttributeType[]) types.clone();
    }

    public AttributeType getGeometry() {
        return defaultGeom;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getTypeName() {
        return typeName;
    }

    public boolean hasAttributeType(String xPath) {
        return getAttributeType(xPath) != null;
    }

    public int getAttributeCount() {
        return types.length;
    }

    public boolean equals(FeatureType other) {
        if (other == null) {
            return false;
        }

        if ((typeName == null) && (other.getTypeName() != null)) {
            return false;
        } else if (!typeName.equals(other.getTypeName())) {
            return false;
        }

        if ((namespace == null) && (other.getNamespace() != null)) {
            return false;
        } else if (!namespace.equals(other.getNamespace())) {
            return false;
        }

        if (types.length != other.getAttributeCount()) {
            return false;
        }

        for (int i = 0, ii = types.length; i < ii; i++) {
            if (!types[i].equals(other.getAttributeType(i))) {
                return false;
            }
        }

        return true;
    }

    public int hashCode() {
        int hash = typeName.hashCode();
        hash *= namespace.hashCode();

        for (int i = 0, ii = types.length; i < ii; i++) {
            hash *= types[i].hashCode();
        }

        return hash;
    }

    public String toString() {
        String types = "";

        for (int i = 0, ii = this.types.length; i < ii; i++) {
            types += this.types[i].toString();

            if (i < ii) {
                types += ",";
            }
        }

        return "DefaultFeatureType ( " + types + ")";
    }

    public boolean equals(Object other) {
        return equals((FeatureType) other);
    }

    /**
     * Obtain an array of this FeatureTypes ancestors. Implementors should
     * return a non-null array (may be of length 0).
     *
     * @return An array of ancestors.
     */
    public FeatureType[] getAncestors() {
        return ancestors;
    }

    /**
     * Is this FeatureType an abstract type?
     *
     * @return true if abstract, false otherwise.
     */
    public boolean isAbstract() {
        return false;
    }

    /**
     * A convenience method for calling<br>
     * <code> FeatureType f1; FeatureType f2;
     * f1.isDescendedFrom(f2.getNamespace(),f2.getName()); </code>
     *
     * @param type The type to compare to.
     *
     * @return true if descendant, false otherwise.
     */
    public boolean isDescendedFrom(FeatureType type) {
        return isDescendedFrom(type.getNamespace(), type.getTypeName());
    }

    /**
     * Test to determine whether this FeatureType is descended from the given
     * FeatureType. Think of this relationship likes the "extends"
     * relationship in java.
     *
     * @param nsURI The namespace URI to use.
     * @param typeName The typeName.
     *
     * @return true if descendant, false otherwise.
     */
    public boolean isDescendedFrom(String nsURI, String typeName) {
      for (int i = 0, ii = ancestors.length; i < ii; i++) {
         if (ancestors[i].getNamespace().equalsIgnoreCase(nsURI) &&
             ancestors[i].getTypeName().equalsIgnoreCase(typeName)) {
            return true;
        }
      }  
      return false;
    }
    
    static final class Abstract extends DefaultFeatureType {
      public Abstract(String typeName, String namespace, List types,
        AttributeType defaultGeom) {
        super(typeName, namespace, types, defaultGeom);
      }
      public final boolean isAbstract() {
        return true; 
      }
      public Feature create(Object[] atts) throws IllegalAttributeException {
        throw new UnsupportedOperationException("Abstract Type");
      }
      
      public Feature create(Object[] atts,String id) throws IllegalAttributeException {
        throw new UnsupportedOperationException("Abstract Type");
      }
    }
}
