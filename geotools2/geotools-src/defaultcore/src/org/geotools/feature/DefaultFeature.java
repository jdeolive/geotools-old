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


// Java Topology Suite dependencies
import com.vividsolutions.jts.geom.*;

// J2SE dependencies
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Provides a more efficient feature representation for the flat and complex
 * features. This implementation actually not only enforces feature type
 * synchronization, it also enforces the use of its accessor methods to change
 * the state of internal object representations.  In this case, the
 * implementation is  trivial, since all allowed attribute objects (from the
 * feature type) are immutable.
 *
 * @todo look at synchronization (or locks as IanS thinks)
 *
 * @author Chris Holmes, TOPP <br>
 * @author Rob Hranac, TOPP
 * @version $Id: DefaultFeature.java,v 1.2 2003/07/17 07:09:53 ianschneider Exp $
 */
public class DefaultFeature implements Feature {
    /*
     * Redesign notes, from FeatureFlat:
     *
     * getId -> getID
     *  standards naming -ch
     *
     * getSchema -> getFeatureType
     *  API clarity -ch
     *
     * Object[] getAttributes() -> Object[] getAttributes(Object[])
     *  Performance enhancements.  Ian, check this code -ch
     *
     * Added getAttribute(int index) -ch
     *
     * Got rid of SchemaException on getAttributes -ch
     *
     * Implemented getBounds, with lazy computation.  If someone
     * could check this that'd be great - needs test cases.  I
     * made it so whenever a geometry is set the bounds are set to null.
     * Bounds are computed whenever getBounds is called and its not null,
     * then is cached. -ch
     *
     *
     */
  
  /** The logger for the default core module. */
  private static final Logger LOGGER = Logger.getLogger(
  "org.geotools.defaultcore");
  
  /** The unique id of this feature */
  private final String featureId;
  
  /** Flat feature type schema for this feature. */
  private final DefaultFeatureType schema;
  
  /** Attributes for the feature. */
  private Object[] attributes;
  
  /** The bounds of this feature. */
  private Envelope bounds;
  
  private FeatureCollection parent;
  
  /**
   * Creates a new instance of flat feature, which must take a flat feature
   * type schema and all attributes as arguments.
   *
   * @param schema Feature type schema for this flat feature.
   * @param attributes Initial attributes for this feature.
   * @param featureID The unique ID for this feature.
   *
   * @throws IllegalAttributeException Attribtues do not conform to feature
   *         type schema.
   */
  protected DefaultFeature(DefaultFeatureType schema, Object[] attributes,
  String featureID) throws IllegalAttributeException {
    this.schema = schema;
    this.featureId = featureID == null ? defaultID() : featureID;
    this.attributes = new Object[schema.getAttributeCount()];
    createNew(attributes);
  }
  
  String defaultID() {
    return "feature-" + System.identityHashCode(this);
  }
  
  /**
   * Creates a new instance of flat feature, which must take a flat feature
   * type schema and all attributes as arguments.
   *
   * @param schema Feature type schema for this flat feature.
   * @param attributes Initial attributes for this feature.
   *
   * @throws IllegalAttributeException Attribtues do not conform to feature
   *         type schema.
   * @task REVISIT: should we allow this?  Force users to explicitly set
   * featureID to null?
   */
  protected DefaultFeature(DefaultFeatureType schema, Object[] attributes)
  throws IllegalAttributeException {
    this(schema,attributes,null);
  }
  
  protected int findAttributeByName(String name) {
    FeatureType schema = getFeatureType();
    for (int i = 0, ii = schema.getAttributeCount(); i < ii; i++) {
      AttributeType type = schema.getAttributeType(i);
      if (type.getName().equals(name)) {
        return i;
      }
    }
    return -1;
  }
  
  private void createNew(Object[] vals)
  throws IllegalAttributeException {
    
    final int cnt = schema.getAttributeCount();
    
    if (vals.length != cnt) {
      throw new IllegalAttributeException(
      "Wrong number of attributes expected " + cnt + " got " +
      vals.length);
    }
    
    // Check to ensure that all attributes are valid
    for (int i = 0; i < cnt; i++) {
      AttributeType type = schema.getAttributeType(i);
      try {
        Object val = type.parse(vals[i]);
        type.validate(val);
        attributes[i] = val;
      } catch (IllegalArgumentException iae) {
        throw new IllegalAttributeException(type,vals[i],iae);
      }
    }
    
  }
  
  /**
   * Gets a reference to the feature type schema for this feature.
   *
   * @return A copy of this feature's metadata in the form of a feature type
   *         schema.
   */
  public FeatureType getFeatureType() {
    return schema;
  }
  
  /**
   * Gets the unique indentification string of this Feature.
   *
   * @return The unique id.
   */
  public String getID() {
    return featureId;
  }
  
  
  
  /**
   * Copy all the attributes of this Feature into the given array. If the
   * argument array is null, a new one will be created. Gets all attributes
   * from this feature, returned as a complex object array.  This array
   * comes with no metadata, so to interpret this  collection the caller
   * class should ask for the schema as well.
   *
   * @param array DOCUMENT ME!
   *
   * @return The array passed in, or a new one if null.
   */
  public Object[] getAttributes(Object[] array) {
    if (array == null) {
      array = new Object[attributes.length];
    }
    
    System.arraycopy(attributes, 0, array, 0, attributes.length);
    
    return array;
  }
  
  /**
   * Gets an attribute for this feature at the location specified by xPath.
   *
   * @param xPath XPath representation of attribute location.
   *
   * @return Attribute.
   */
  public Object getAttribute(String xPath) {
    int idx = findAttributeByName(xPath);
    if (idx == -1)
      return null;
    return attributes[idx];
  }
  
  /**
   * Gets an attribute by the given zero-based index.
   *
   * @param index
   *
   * @return The attribute at the given index.
   *
   * @throws ArrayIndexOutOfBoundsException If the index is less than zero or
   *         greater than the number of attributes - 1.
   */
  public Object getAttribute(int index) {
    return attributes[index];
  }
  
  public void setAttribute(int position, Object val) throws IllegalAttributeException {
    AttributeType type = schema.getAttributeType(position);
    try {
      Object parsed = type.parse(val);
      type.validate(parsed);
      attributes[position] = parsed;
    } catch (IllegalArgumentException iae) {
      throw new IllegalAttributeException(type,val,iae);
    }
  }
  
  
  
  
  /**
   * Sets all attributes for this feature, passed as an array.  All
   * attributes are checked for validity before adding.
   *
   * @param attributes All feature attributes.
   *
   * @throws IllegalAttributeException Passed attributes do not match feature
   *         type.
   */
  public void setAttributes(Object[] attributes)
  throws IllegalAttributeException {
    createNew(attributes);
  }
  
  /**
   * Sets a single attribute for this feature, passed as a complex object. If
   * the attribute does not exist or the object does not conform to the
   * internal feature type, an exception is thrown.
   *
   * @param xPath XPath representation of attribute location.
   * @param attribute Feature attribute to set.
   *
   * @throws IllegalAttributeException Passed attribute does not match
   *         feature type
   */
  public void setAttribute(String xPath, Object attribute)
  throws IllegalAttributeException {
    
    int idx = findAttributeByName(xPath);
    if (idx < 0) {
      throw new IllegalAttributeException("No attribute named " + xPath);
    }
    
    AttributeType type = schema.getAttributeType(idx);
    
    try {
      Object val = type.parse(attribute);
      type.validate(val);
      attribute = val;
    } catch (IllegalArgumentException iae) {
      throw new IllegalAttributeException(type,attribute,iae);
    }
    
    if (type.isGeometry())
      bounds = null;
    
    attributes[idx] = attribute;
    
  }
  
  
  
  /**
   * Gets the geometry for this feature.
   *
   * @return Geometry for this feature.
   */
  public Geometry getDefaultGeometry() {
    int idx = schema.find(schema.getDefaultGeometry());
    if (idx == -1)
      return null;
    return (Geometry) attributes[idx];
  }
  
  /**
   * Modifies the geometry.
   *
   * @param geometry All feature attributes.
   *
   * @throws IllegalAttributeException DOCUMENT ME!
   */
  public void setDefaultGeometry(Geometry geometry)
  throws IllegalAttributeException {
    
    AttributeType geometryAttribute = schema.getDefaultGeometry();
    int idx = schema.find(geometryAttribute);
    
    
    
    if (geometryAttribute.getType().equals(geometry.getClass().getName())) {
      attributes[idx] = (Geometry) geometry.clone();
      
      //reset the bounds so it gets recomputed.
      bounds = null;
    } else {
      String message = "Cannot add geometry that does not match type.";
      throw new IllegalAttributeException(message);
    }
  }
  
  
  /**
   * Get the number of attributes this feature has.
   * This is simply a convenience method for calling
   * getFeatureType().getNumberOfAttributes();
   *
   * @returns The total number of attributes this Feature contains.
   */
  public int getNumberOfAttributes(){
    return attributes.length;
  }
  
  
  /**
   * Get the total bounds of this feature which is calculated by doing a
   * union of the bounds of each geometry this feature is associated with.
   *
   * @return An Envelope containing the total bounds of this Feature.
   *
   * @task REVISIT: what to return if there are no geometries in the
   *       feature?  For now we'll return a null envelope, make this part of
   *       interface? (IanS - by OGC standards, all Feature must have geom)
   */
  public Envelope getBounds() {
    if (bounds == null) {
      bounds = new Envelope();
      
      for (int i = 0, n = schema.getAttributeCount(); i < n; i++) {
        if (schema.getAttributeType(i).isGeometry()) {
          bounds.expandToInclude(((Geometry) attributes[i]).getEnvelopeInternal());
        }
      }
    }
    return bounds;
  }
  
  /**
   * Creates an exact copy of this feature.
   *
   * @return A default feature.
   */
  public Object clone() {
    DefaultFeature exactCopy = null;
    
    try {
      exactCopy = new DefaultFeature(this.schema, this.attributes);
    } catch (IllegalAttributeException e) {
      // Can never happen
    }
    
    return exactCopy;
  }
  
  /**
   * Returns a string representation of this feature.
   *
   * @return A representation of this feature as a string.
   */
  public String toString() {
    StringBuffer returnString = new StringBuffer("\n" + schema.getTypeName() +
    " -> \n");
    
    for (int i = 0, n = attributes.length; i < n; i++) {
      returnString.append(((attributes[i] == null) ? "NULL"
      : attributes[i].toString()) +
      "\n");
    }
    
    return returnString.toString();
  }
  
  /**
   * Returns a string representation, with the schema if requested.
   *
   * @param includeSchema if the schema should be printed with the feature.
   *
   * @return A representation of this feature as a string.
   */
  public String toString(boolean includeSchema) {
    if (includeSchema) {
      return schema.toString() + "\n" + toString();
    } else {
      return toString();
    }
  }
  
  /**
   * returns a unique code for this feature
   *
   * @return A unique int
   */
  public int hashCode() {
    int hash = featureId.hashCode();
    hash *= 13 * schema.hashCode();
    return hash;
//    int key = featureId.hashCode();
//    key = (key * 13) + schema.hashCode();
//    for (int i = 0; i < attributes.length; i++) {
//      key = (key * 13) + attributes[i].hashCode();
//    }
//    
//    return key;
  }
  

  public boolean equals(Object obj) {
    if (obj == null) return false;
    Feature feat = (Feature) obj;
    if (!feat.getFeatureType().equals(schema))
      return false;
    // this check shouldn't exist, by contract, all features should have an ID.
    if (featureId == null) {
      if (feat.getID() != null)
        return false;
    }
    if (!featureId.equals(feat.getID()))
      return false;
    return true;
//    if ((obj != null) && (obj.getClass() == this.getClass())) {
//      DefaultFeature testFeature = (DefaultFeature) obj;
//      boolean isEqual = true;
//      
//      //isEqual = (this.schema.toString().equals
//      //       (testFeature.getSchema().toString())) && isEqual;
//      LOGGER.finest("schemas are equal: " + isEqual);
//      
//      Object[] testAttributes = testFeature.getAttributes(null);
//      
//      if (this.attributes.length == testAttributes.length) {
//        LOGGER.finest("both are of length " + testAttributes.length);
//        
//        for (int i = 0; i < this.attributes.length; i++) {
//          isEqual = isEqual &&
//          this.attributes[i].equals(testAttributes[i]);
//          LOGGER.finest(this.attributes[i] + " is equal to " +
//          testAttributes[i] + ": " + isEqual);
//        }
//      } else {
//        isEqual = false;
//      }
//      
//      return isEqual;
//    } else {
//      return false;
//    }
  }
  
  public FeatureCollection getParent() {
    return parent;
  }  
  
  public void setParent(FeatureCollection collection) {
    if (parent == null)
      parent = collection;
  }
  
}
