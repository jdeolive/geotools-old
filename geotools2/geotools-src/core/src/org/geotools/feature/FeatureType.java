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

/**
 * <p>
 * A metadata template for a feature of arbitrary complexity.  Note that this
 * documentation should be read in conjunction with the {@link Feature} API.
 * </p>
 * 
 * <p>
 * This interface answers the question: How do we represent features within
 * GeoTools?  Of course, the most general answer would be: features can be any
 * Java object. However, this is also the least useful solution because it
 * means that users of features have essentially no way to find out about the
 * meaning of features other than using Java introspection/reflection. This is
 * too cumbersome and is insufficient for the goal of creating a simple
 * framework for manipulating and accessing generic geographic data.  The
 * opposite approach might be to define a very constrained set of possible
 * attributes (that, for example, mirrored Java primitives and OGC simple
 * geometries) and only allow features of this type.
 * </p>
 * 
 * <p>
 * This interface takes a different approach: it defines a minimal ontology for
 * representing a feature and serves as a consistent framework for defining
 * more constrained (and, therefore, often more meaningful) feature types.  A
 * <code>FeatureType</code> represents features as an object that contains
 * zero or more attribute objects, one of which will generally be a geometry,
 * but no geometry and multiple geometries are allowed, according to
 * implementation. Note that instances of implementations of this class are
 * henceforth referred to as schemas.
 * </p>
 * 
 * <p>
 * With one exception, the type of an attribute is considered to be its
 * cannonical definition by the FeatureType.  For example, an attribute type
 * might be a <code>javax.sound.midi.Sequence</code> object, which contains a
 * <code>float</code> public field called PPQ.  The fact that this attribute
 * exists is not known by the <code>FeatureType</code> itself.  If a caller
 * asks this <code>FeatureType</code> for all of its attributes, the <code>
 * FeatureType</code> will tell the caller that it has an attribute of type
 * <code>javax.sound.midi.Sequence</code>, but not that this attribute has a
 * sub-attribute (field) called PPQ.  It is the responsibility of the callers
 * to understand the objects it is asking for and manipulate them
 * appropriately. The sole exception is if the type stored in the
 * <code>FeatureType</code> is a <code>org.geotools.datasource.Feature</code>
 * type.  In this case, all information about sub-attributes are stored and
 * passed to calling classes upon request.  The style of reference (XPath) is
 * defined in and mediated by <code>FeatureType</code> implementations.
 * </p>
 * 
 * <p>
 * It is the responsibility of the implementing class to ensure that the
 * <code>FeatureType</code> is always in a valid state.  This means that each
 * attribute tuple must be fully initialized and valid.  The minimum valid
 * <code>FeatureType</code> is one with nulls for namespace, type, and
 * attributes; this is clearly a trivial case, since it is so constrained that
 * it would not allow for any feature construction.  There are a few
 * conventions of which implementers of this interface must be aware in order
 * to successfully manage a <code>FeatureType</code>:
 * </p>
 * 
 * <ol>
 * <li>
 * <b>Immutability</b><br><i>FeatureTypes must be implemented as immutable
 * objects!</i>  All setting methods have been removed from this interface,
 * that functionality is now available in the mutable {@link
 * FeatureTypeFactory}
 * </li>
 * <li>
 * <b>Default Geometries</b><br>
 * Note that the FeatureType contains a special methods for handling
 * geometries. The primary geometry retrieval methods are in
 * <code>Feature</code> because they may change over the life of the feature,
 * while the schema may not.  In cases where there are more than one
 * geometries it is up to the implementor to determine which is the default
 * geometry.  <code>getDefaultGeometry</code> may return <code>null</code> if
 * there are no geometries in the FeatureType, but if there is one or more
 * geometry then the method must return one of them, <code>null</code>  is
 * never an acceptable return value.
 * </li>
 * <li>
 * <b>XPath</b><br> XPath is the standard used to access all attributes (flat,
 * nested, and multiple), via a single, unified string.  Using XPath to access
 * attributes has the convenient side-benefit of making them appear to be
 * non-nested and non-multiple to callers with no awareness of XPath.  This
 * greatly simplifies accessing and manipulating data.  However, it does put
 * extra burden on the implementers of <code>FeatureType</code> to understand
 * and correctly  implement XPath pointers.  Note that the
 * <code>Feature</code> object does not understand XPath at all and relies on
 * implementors of this interface to interpret XPath references.  Fortunately,
 * XPath is quite simple and has a  clearly written <a
 * href="http://www.w3.org/TR/xpath">specification</a>.
 * </li>
 * <li>
 * <b>Feature Creation</b><br>
 * FeatureType also must provide methods for the  creation of Features, as
 * specified in FeatureFactory.  The creating FeatureType should check to see
 * if the passed in objects validate against its AttributeTypes, and if it
 * does should return a new Feature.
 * </li>
 * </ol>
 *
 * 
 *
 * @author Rob Hranac, VFNY
 * @author Chris Holmes, TOPP
 * @version $Id: FeatureType.java,v 1.11 2003/11/26 19:06:12 cholmesny Exp $
 *
 *
 * @see org.geotools.feature.Feature
 * @see org.geotools.feature.FeatureTypeFactory
 * @see org.geotools.feature.DefaultFeatureType
 */
public interface FeatureType extends FeatureFactory {
  
    
    /*
     * Redesign notes:
     *
     * All set functions have been removed from this interface, so as to
     * not even give the appearance of mutability, as users generally
     * expect setNamespace to actually set the namespace, instead of
     * thoroughly reading the docs to find that they must use the copy.
     * The mutability code has been moved to the factory, FeatureTypeFactory,
     * so look for setNamespace,
     *
     * getAttributeCount added.
     *
     * added find()
     *
     */

    /**
     * Put this functionality in the factory, produce only  immutable
     * FeatureTypes, don't give appearance of mutability, it's damn confusing
     * to realize that you need to get the return type of these set calls in
     * FeatureType. Sets the global schema namespace.  Note that namespaces
     * are not required and should return null if it is not set.
     *
     * @return A modified copy of this schema.
     */

    //FeatureType setNamespace(String namespace);

    /**
     * Gets the global schema namespace.
     *
     * @return Namespace of schema.
     */
    String getNamespace();

    /**
     * Put this functionality in the factory, produce only  immutable
     * FeatureTypes, don't give appearance of mutability, it's damn confusing
     * to realize that you need to get the return type of these set calls in
     * FeatureType. Sets the global schema type name.  Note that type names
     * are not required and should return null if it is not set.
     *
     * @return A modified copy of this schema.
     */

    //FeatureType setTypeName(String name);

    /**
     * Gets the type name for this schema.
     *
     * @return Namespace of schema.
     */
    String getTypeName();

    /**
     * Gets all of the names for the first 'level' of attributes.  This means
     * that nested attributes must be read seperately, via the getNames()
     * method of their schemas or the getAllNames() method.
     *
     * @return Non-nested attribute names.
     */
    AttributeType[] getAttributeTypes();

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
    boolean hasAttributeType(String xPath);

    /**
     * Gets the attributeType at this xPath, if the specified attributeType
     * does not exist then null is returned.
     *
     * @param xPath XPath pointer to attribute type.
     *
     * @return True if attribute exists.
     */
    AttributeType getAttributeType(String xPath);

    //throws SchemaException;

    /** Find the position of a given AttributeType.
     * @return -1 if not found, a zero-based index if found.
     * @param type The type to search for.
     */
    int find(AttributeType type);

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
    // Jody - can we change to the following?
    //GeometryAttributeType getDefaultGeometry();
    AttributeType getDefaultGeometry();

    /**
     * Returns the number of attributes at the first 'level' of the schema.
     *
     * @return the total number of first level attributes.
     */
    int getAttributeCount();

    /** Gets the attributeType at the specified index.
     *
     * @return The attribute type at the specified position.
     * @param position the position of the attribute to check.
     */
    AttributeType getAttributeType(int position);
    
    /** Test to determine whether this FeatureType is descended from the given
     * FeatureType. Think of this relationship likes the "extends" relationship in
     * java.
     * @param nsURI The namespace URI to use.
     * @param typeName The typeName.
     * @return true if descendant, false otherwise.
     */    
    boolean isDescendedFrom(String nsURI, String typeName);
    
    /** A convenience method for calling<br>
     * <code><pre>
     * FeatureType f1;
     * FeatureType f2;
     * f1.isDescendedFrom(f2.getNamespace(),f2.getName());
     * </pre></code>
     * @param type The type to compare to.
     * @return true if descendant, false otherwise.
     */    
    boolean isDescendedFrom(FeatureType type);
    
    /** Is this FeatureType an abstract type?
     * @return true if abstract, false otherwise.
     */    
    boolean isAbstract();
    
    /** Obtain an array of this FeatureTypes ancestors. Implementors should return a
     * non-null array (may be of length 0).
     * @return An array of ancestors.
     */    
    FeatureType[] getAncestors();

    Feature duplicate(Feature feature) throws IllegalAttributeException;
}
