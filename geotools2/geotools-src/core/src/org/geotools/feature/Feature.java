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

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * <p>
 * Represents a feature of arbitrary complexity. This interface answers the
 * question: How do we access feature attributes? The most generic approach
 * would be to pass all feature attributes as objects and use Java variable
 * and method references to access them.  However, this is also the most
 * useless approach because it establishes no unified methods for getting
 * attribute information (since it is totally Object dependent), without
 * elaborate reflection/introspection, which is inconvenient to use. Unlike
 * its {@link FeatureType} counterpart, this interface does not attempt to
 * serve as a typing framework.  Rather, multiple implementations of this
 * interface should generally be for performance reasons.
 * </p>
 * 
 * <p>
 * This interface serves two important purposes.  Most obviously, it gives
 * users of features a unified, consistent framework for accessing and
 * manipulating feature data.  Perhaps more importantly,  the
 * <code>FeatureType</code> and <code>Feature</code> interfaces also work
 * together to give implementers a framework for constraining and enforcing
 * constraints (respectively) on allowed feature types. As such, this
 * interface is as general as possible in terms of the types of objects to
 * which it provides access. Keep in mind that creating new features is
 * relatively difficult and should only be done to optimize performance for
 * highly constrained schema types. For the vast majority of schemas, the
 * generic feature implementation will  work fine.
 * </p>
 * 
 * <p>
 * <b>Notes for Feature Clients:</b><br>
 * Clients should always use feature accessor methods (getAttribute and
 * setAttribute) to modify the state of internal attribute objects.  It is
 * possible that some feature implementations will allow object state changes
 * by clients outside of the class, but this is strongly discouraged. In
 * general, feature implementations will make defensive copies of objects
 * passed to clients and it is therefore not guaranteed that client state
 * changes that take place outside of the feature will be reflected in the
 * internal state of the feature object!  <i>For this reason, clients should
 * always use the set methods to change feature attribute object states!</i>
 * </p>
 * 
 * <p>
 * <b>Notes for Feature Implementers:</b><br>
 * It is the responsibility of the implementing class to ensure that the
 * <code>Feature</code> attributes stay synchronized with its FeatureType
 * definition. <i>Features should never get out of synch with their declared
 * schemas and should never alter their schemas!</i>  There are four
 * conventions of which implementers of this interface must be aware in order
 * to successfully manage a <code>Feature</code>:
 * </p>
 * 
 * <ol>
 * <li>
 * <b>FeatureType Reference</b><br>
 * Features must always hold a single (immutable: see
 * <code>FeatureType</code>) schema reference and this reference should not be
 * altered after a feature has been created.  To ensure this, is is strongly
 * recommended that features take a valid reference to an existing immutable
 * schema in its constructor and declare that reference final.
 * </li>
 * <li>
 * <b>Default Geometry</b><br>
 * Each feature must have a default geometry, but this primary geometry may be
 * null.  This means that a feature may contain no geometries, but it must
 * always have a method for accessing a geometry object (even if it is null).
 * It also means that a feature with multiple geometries must pick one as its
 * default geometry.  Note that the designation of the default geometry is
 * stored as part of the <code>FeatureType</code> and is therefore immmutable.
 * </li>
 * <li>
 * <b>Attributes</b><br> All features contain zero or more attributes, which
 * can have one or more occurrences inside the feature.  Attributes may be any
 * valid Java object. If attributes are instances of <code>Feature</code>,
 * they are handled specially by the <code>Feature</code> methods, in that
 * their attributes may be accessed directly by their containing feature.  All
 * other object variables and methods must be accessed through the objects
 * themselves. It is up to implementers of <code>Feature</code> to make sure
 * that each attribute value conforms to its internal schema.  A feature
 * should never reach a state where its attributes (or sub-attributes) do not
 * conform to their <code>FeatureType</code> definitions.  There are three
 * ways to implement this.  The first is to simply make features immutable;
 * however, given the ubiquity and required flexibility of features, this is
 * likely not possible. The second (and second easiest), is to make all
 * feature attributes immutable. For most cases, this is probably the best way
 * to handle this issue. The third way, is to never give out a reference that
 * would allow a client to change an attribute object's class (most obviously,
 * an array reference). Generally speaking, features should attempt to
 * minimize external object references by attempting to clone incoming
 * attributes before adding them and outgoing attributes before sending them.
 * For features with non-cloneable attributes, of course, this is not
 * possible, so this is left to the discretion of the implementor.
 * </li>
 * <li>
 * <b>Constructors</b><br> Constructors should take arguments with enough
 * information to create a valid representation of the feature.  They should
 * also always include a  valid schema that can be used to check the proposed
 * attributes.  This is necessary to ensure that the feature is always in a
 * valid state, relative to its schema.
 * </li>
 * <li>
 * <b>hashCode() and equals(Object other)</b><br>
 * Determining equality and equivalence for Feature instances is of utmost
 * importance. This must be done in a constistent manner, as many other areas
 * of geotools will rely on these relations. See java.lang.Object for details.
 * </li>
 * </ol>
 * 
 *
 * @author James Macgill, CCG
 * @author Rob Hranac, TOPP
 * @author Ian Schneider, USDA-ARS
 * @version $Id: Feature.java,v 1.10 2003/08/05 21:33:26 cholmesny Exp $
 *
 * @see org.geotools.feature.FeatureType
 * @see org.geotools.feature.DefaultFeature
 */
public interface Feature {
    /**
     * Gets the feature collection this feature is stored in.
     *
     * @return The collection that is the parent of this feature.
     */
    FeatureCollection getParent();

    /**
     * Sets the parent collection this feature is stored in, if it is not
     * already set.  If it is set then this method does nothing.
     *
     * @param collection the collection to be set as parent.
     */
    void setParent(FeatureCollection collection);

    /**
     * Gets a reference to the schema for this feature.
     *
     * @return A reference to this feature's schema.
     */
    FeatureType getFeatureType();

    /**
     * Gets the unique feature ID for this feature.
     *
     * @return Unique identifier for this feature.
     */
    String getID();

    /**
     * Copy all the attributes of this Feature into the given array. If the
     * argument array is null, a new one will be created. Gets all attributes
     * from this feature, returned as a complex object array.  This array
     * comes with no metadata, so to interpret this collection the caller
     * class should ask for the schema as well.
     *
     * @param attributes An array to copy attributes into. May be null.
     *
     * @return The array passed in, or a new one if null.
     */
    Object[] getAttributes(Object[] attributes);

    /**
     * Gets an attribute for this feature at the location specified by xPath.
     * Due to the complex nature of xpath, the return Object may be a single
     * attribute, a FeatureCollection containing only Features, or a
     * java.util.Collection containing either a mix of attributes, Features,
     * and/or FeatureCollections.
     *
     * @param xPath XPath representation of attribute location.
     *
     * @return A copy of the requested attribute, null if the requested xpath
     *         is not found, or NULL_ATTRIBUTE.
     */
    Object getAttribute(String xPath);

    /**
     * Gets an attribute by the given zero-based index.
     *
     * @param index The requested index. Must be 0 &lt;= idx &lt;
     *        getNumberOfAttributes().
     *
     * @return A copy of the requested attribute, or NULL_ATTRIBUTE.
     */
    Object getAttribute(int index);

    /**
     * Sets an attribute by the given zero-based index.
     *
     * @param position The requested index. Must be 0 &lt;= idx &lt;
     *        getNumberOfAttributes()
     * @param val An object representing the attribute being set
     *
     * @throws IllegalAttributeException if the passed in val does not validate
     *         against the AttributeType at that position.
     * @throws ArrayIndexOutOfBoundsException if an invalid position is given
     */
    void setAttribute(int position, Object val)
        throws IllegalAttributeException, ArrayIndexOutOfBoundsException;

    /**
     * Get the number of attributes this feature has. This is simply a
     * convenience method for calling
     * getFeatureType().getNumberOfAttributes();
     *
     * @return The total number of attributes this Feature contains.
     */
    int getNumberOfAttributes();

    /**
     * Sets all attributes for this feature, passed as a complex object array.
     * Note that this array must conform to the internal schema for this
     * feature, or it will throw an exception.  Checking this is, of course,
     * left to the feature to do internally.  Well behaved features should
     * always fully check the passed attributes against thier schema before
     * adding them.
     *
     * @param attributes All feature attributes.
     *
     * @throws IllegalAttributeException Passed attributes do not match schema.
     */
    void setAttributes(Object[] attributes) throws IllegalAttributeException;

    /**
     * Sets a single attribute for this feature, passed as a complex object. If
     * the attribute does not exist or the object does not conform to the
     * internal schema, an exception is thrown.  Checking this is, of course,
     * left to the feature to do internally.  Well behaved features should
     * always fully check the passed attributes against thier schema before
     * adding them.
     *
     * @param xPath XPath representation of attribute location.
     * @param attribute Feature attribute to set.
     *
     * @throws IllegalAttributeException If the attribute is illegal for the
     *         path specified.
     */
    void setAttribute(String xPath, Object attribute)
        throws IllegalAttributeException;

    /**
     * Gets the default geometry for this feature.
     *
     * @return Default geometry for this feature.
     */
    Geometry getDefaultGeometry();

    /**
     * Sets the default geometry for this feature.
     *
     * @param geometry The geometry to set.
     *
     * @throws IllegalAttributeException If the AttributeType is not a
     *         geometry, or is invalid for some other reason.
     */
    void setDefaultGeometry(Geometry geometry) throws IllegalAttributeException;

    /**
     * Get the total bounds of this feature which is calculated by doing a
     * union of the bounds of each geometry this feature is associated with.
     *
     * @return An Envelope containing the total bounds of this Feature.
     */
    Envelope getBounds();

    /**
     * A "null" Object representing a null value for a given attribute.
     */

    //    static final Object NULL_ATTRIBUTE = new NULL();

    /**
     * Not straight forward, this is a "null" object to represent the value
     * null for a given attribute which is nullable.
     */
    static final class NULL implements Comparable {
        /**
         * Implementation of Comparable.
         *
         * @param o The other thing to compare to.
         *
         * @return 0 if null or this, 1 for all others.
         */
        public int compareTo(Object o) {
            if (o == null) {
                return 0;
            }

            if (o == this) {
                return 0;
            }

            return 1;
        }
    }
}
