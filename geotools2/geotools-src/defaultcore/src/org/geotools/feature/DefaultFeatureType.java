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

import java.util.Collection;
import java.util.Iterator;

/**
 * A basic implementation of FeatureType.
 *
 * @author Ian Schneider
 * @version $Id: DefaultFeatureType.java,v 1.9 2003/08/20 19:56:18 cholmesny Exp $
 */
public class DefaultFeatureType implements FeatureType {
    /** The name of this FeatureType. */
    private final String typeName;

    /** The namespace to uniquely identify this FeatureType. */
    private final String namespace;

    /** The array of types that this FeatureType can have as attributes. */
    private final AttributeType[] types;

    /** The FeatureTypes this is descended from. */
    private final FeatureType[] ancestors;

    /** The default geometry AttributeType. */
    private final AttributeType defaultGeom;

    /** The position of the default Geometry */
    private final int defaultGeomIdx;

    /**
     * Constructs a new DefaultFeatureType.
     *
     * @param typeName The name to give this FeatureType.
     * @param namespace The namespace of the new FeatureType.
     * @param types The attributeTypes to use for validation.
     * @param superTypes The ancestors of this FeatureType.
     * @param defaultGeom The attributeType to set as the defaultGeometry.
     *
     * @throws SchemaException For problems making the FeatureType.
     * @throws NullPointerException If typeName is null.
     */
    public DefaultFeatureType(String typeName, String namespace,
        Collection types, Collection superTypes, AttributeType defaultGeom)
        throws SchemaException, NullPointerException {
        if (typeName == null) {
            throw new NullPointerException(typeName);
        }

        this.typeName = typeName;
        this.namespace = (namespace == null) ? "" : namespace;
        this.types = (AttributeType[]) types.toArray(new AttributeType[types
                .size()]);
        this.ancestors = (FeatureType[]) superTypes.toArray(new FeatureType[superTypes
                .size()]);

        // do this first...
        this.defaultGeomIdx = find(defaultGeom);

        // before doing this
        this.defaultGeom = defaultGeom;
    }

    /**
     * Creates a new feature, with a generated unique featureID.  This is less
     * than ideal, as a FeatureID should be persistant over time, generally
     * created by a datasource.  This method is more for testing that doesn't
     * need featureID.
     *
     * @param attributes the array of attribute values
     *
     * @return The created feature with this as its feature type.
     *
     * @throws IllegalAttributeException if this FeatureType does not validate
     *         the attributes.
     */
    public Feature create(Object[] attributes) throws IllegalAttributeException {
        return create(attributes, null);
    }

    /**
     * Creates a new feature, with the proper featureID, using this
     * FeatureType.
     *
     * @param attributes the array of attribute values.
     * @param featureID the feature ID.
     *
     * @return the created feature.
     *
     * @throws IllegalAttributeException if this FeatureType does not validate
     *         the attributes.
     */
    public Feature create(Object[] attributes, String featureID)
        throws IllegalAttributeException {
        return new DefaultFeature(this, attributes, featureID);
    }

    /**
     * Gets the default geometry AttributeType.  If the FeatureType has more
     * one geometry it is up to the implementor to determine which geometry is
     * the default.  If working with multiple geometries it is best to get the
     * attributeTypes and iterate through them, checking isGeometry on each.
     * This should just be used a convenience method when it is known that the
     * features are flat.
     *
     * @return The attribute type of the default geometry, which will contain
     *         the position.
     */
    public AttributeType getDefaultGeometry() {
        return defaultGeom;
    }

    /**
     * Gets the attributeType at this xPath, if the specified attributeType
     * does not exist then null is returned.
     *
     * @param xPath XPath pointer to attribute type.
     *
     * @return True if attribute exists.
     */
    public AttributeType getAttributeType(String xPath) {
        for (int i = 0, ii = types.length; i < ii; i++) {
            if (types[i].getName().equals(xPath)) {
                return types[i];
            }
        }

        return null;
    }

    /**
     * Find the position of a given AttributeType.
     *
     * @param type The type to search for.
     *
     * @return -1 if not found, a zero-based index if found.
     */
    public int find(AttributeType type) {
        for (int i = 0, ii = types.length; i < ii; i++) {
            if (types[i].equals(type)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Gets the attributeType at the specified index.
     *
     * @param position the position of the attribute to check.
     *
     * @return The attribute type at the specified position.
     */
    public AttributeType getAttributeType(int position) {
        return types[position];
    }

    public AttributeType[] getAttributeTypes() {
        return (AttributeType[]) types.clone();
    }

    //Is this used?  Delete if everything compiles fine.
    //public AttributeType getGeometry() {
    //  return defaultGeom;
    //}

    /**
     * Gets the global schema namespace.
     *
     * @return Namespace of schema.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Gets the type name for this schema.
     *
     * @return Namespace of schema.
     */
    public String getTypeName() {
        return typeName;
    }

    /**
     * This is only used twice in the whole geotools code base, and  one of
     * those is for a test, so we're removing it from the interface. If
     * getAttributeType does not have the AttributeType it will just return
     * null.  Gets the number of occurrences of this attribute.
     *
     * @param xPath XPath pointer to attribute type.
     *
     * @return Number of occurrences.
     */
    public boolean hasAttributeType(String xPath) {
        return getAttributeType(xPath) != null;
    }

    /**
     * Returns the number of attributes at the first 'level' of the schema.
     *
     * @return the total number of first level attributes.
     */
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
        String info = "name=" + typeName;
        info += (" , namespace=" + namespace);
        info += (" , abstract=" + isAbstract());

        String types = "types=(";

        for (int i = 0, ii = this.types.length; i < ii; i++) {
            types += this.types[i].toString();

            if (i < ii) {
                types += ",";
            }
        }

        types += ")";
        info += (" , " + types);

        return "DefaultFeatureType [" + info + "]";
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
     *
     * @task HACK: if nsURI is null only typeName is tested.
     */
    public boolean isDescendedFrom(String nsURI, String typeName) {
        for (int i = 0, ii = ancestors.length; i < ii; i++) {
            if (((nsURI == null)
                    || ancestors[i].getNamespace().equalsIgnoreCase(nsURI))
                    && ancestors[i].getTypeName().equalsIgnoreCase(typeName)) {
                return true;
            }
        }

        return false;
    }

    static final class Abstract extends DefaultFeatureType {
        public Abstract(String typeName, String namespace, Collection types,
            Collection superTypes, AttributeType defaultGeom)
            throws SchemaException {
            super(typeName, namespace, types, superTypes, defaultGeom);

            Iterator st = superTypes.iterator();

            while (st.hasNext()) {
                FeatureType ft = (FeatureType) st.next();

                if (!ft.isAbstract()) {
                    throw new SchemaException(
                        "Abstract type cannot descend from no abstract type : "
                        + ft);
                }
            }
        }

        public final boolean isAbstract() {
            return true;
        }

        public Feature create(Object[] atts) throws IllegalAttributeException {
            throw new UnsupportedOperationException("Abstract Type");
        }

        public Feature create(Object[] atts, String id)
            throws IllegalAttributeException {
            throw new UnsupportedOperationException("Abstract Type");
        }
    }
}
