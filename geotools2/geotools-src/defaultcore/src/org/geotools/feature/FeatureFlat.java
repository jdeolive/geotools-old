/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.feature;

import java.util.*;
import org.apache.log4j.Category;
import com.vividsolutions.jts.geom.*;

/**
 * Provides a more efficient feature representation for the flat feature type.
 * This implementation actually not only enforces feature type synchronization,
 * it also enforces the use of its accessor methods to change the state of
 * internal object representations.  In this case, the implementation is 
 * trivial, since all allowed attribute objects (from the feature type) are
 * immutable.
 *
 * @version $Id: FeatureFlat.java,v 1.10 2002/07/11 16:58:17 loxnard Exp $
 * @author Rob Hranac, VFNY
 */
public class FeatureFlat implements Feature {

    private static Category _log = Category.getInstance(FeatureFlat.class.getName());


    /** Flat feature type schema for this feature. */
    private final FeatureTypeFlat schema;
    
    /** Attributes for the feature. */
    private Object[] attributes;

    /** Geometry for this feature. */
    //private int geometryPosition;
    

    /**
     * Creates a new instance of flat feature, which must take a flat feature 
     * type schema and all attributes as arguments. 
     *
     *
     * @param schema Feature type schema for this flat feature.
     * @param attributes Initial attributes for this feature.
     * @throws IllegalFeatureException Attribtues do not conform to feature type
     * schema.
     */
    protected FeatureFlat (FeatureTypeFlat schema, Object[] attributes) 
        throws IllegalFeatureException {

        // Set the feature type reference
        this.schema = schema;
        _log.debug("creating feature");

        // Gets the number of attributes from feature and uses to set valid flag
        int n = schema.attributeTotal();
        boolean isValid = (n == attributes.length);
        //_log.info("schema attributes: " + n);
        _log.debug("passed attributes: " + attributes.length);
        _log.debug("is valid: " + isValid);

        // Check to ensure that all attributes are valid
        for (int i = 0; i < n ; i++) {
            isValid =  schema.getAttributeType(i).getType().
                isAssignableFrom(attributes[i].getClass()) &&
                isValid;
            //String existingType = schema.getAttributeType(i).getType();
            //String targetType = attributes[i].getClass().getName();
            //_log.info("target type:" + attributes[i].toString());
            //_log.info("validity check:" + schema.getAttributeType(i).getName());
            //_log.info("existing type:" + existingType );
            //_log.info("target type:" + targetType );
            //if( !(existingType.equals( targetType ) ) ) {
            //  isValid = false;
            //}
            //_log.info("is valid: " + isValid);
        }

        // Add if it is valid, otherwise throw an exception.
        if (isValid) {
            //_log.info("about to copy");
            this.attributes = new Object[n];
            System.arraycopy(attributes, 0, this.attributes, 0, n);
            //_log.info("just copied");
        }
        else {
            throw new IllegalFeatureException("You have attempted to create an invalid feature " +
                                              "instance.");
        }
    }


    /** 
     * Gets a reference to the feature type schema for this feature.
     *
     * @return A copy of this feature's metadata in the form of a feature type
     *         schema.
     */
     public FeatureType getSchema() {
        return (FeatureType) schema;
    }

    /* ***********************************************************************
     * Attribute extraction methods.
     * ***********************************************************************/

    // TODO 2:
    // Several of these methods must be synchronized to ensure that the state
    //  transitions are smooth.
    /** 
     * Gets all attributes from this feature, returned as a complex object
     * array.  This array comes with no metadata, so to interpret this 
     * collection the caller class should ask for the feature type as well.
     *
     * @return All feature attributes.
     */
    public Object[] getAttributes() {
        Object[] attributesCopy = new Object[attributes.length];
        System.arraycopy(attributes, 0, attributesCopy, 0, attributes.length);
        return attributesCopy;
    }

    /** 
     * Gets an attribute for this feature at the location specified by xPath.
     *
     * @param xPath XPath representation of attribute location.
     * @return Attribute.
     * @throws IllegalFeatureException Requested attribute doesn't
     * match feature type.
     */
    public Object getAttribute(String xPath)
        throws IllegalFeatureException {

        //_log.info("looking for attribute: " + xPath);
        AttributeType definition = null;
        //_log.info("has attribute: " + schema.hasAttributeType(xPath));
        //_log.info("attribute is: " + schema.getAttributeType(xPath).toString());
        if (schema.hasAttributeType(xPath)) {
            definition = schema.getAttributeType(xPath);
        }
        else {
            String message = "Could not find requested attribute: " + xPath;
            throw new IllegalFeatureException(message);
        }
        //_log.info("position is: " + definition.getPosition());
        //_log.info("attribute is: " + attributes[definition.getPosition()].toString());
        return attributes[definition.getPosition()];
    }

    /** 
     * Throws an exception, since flat features have only one occurrence.
     *
     * @param xPath XPath representation of attribute location.
     * @return Attribute.
     * @throws IllegalFeatureException Requested attribute is not complex or
     * does not exist.
     */
    public Object[] getAttributes(String xPath)
        throws IllegalFeatureException {

        String message = "Flat features do not have nested attributes.";
        throw new IllegalFeatureException(message);
    }


    /* ***********************************************************************
     * Attribute setting methods.
     * ***********************************************************************/
    /** 
     * Sets all attributes for this feature, passed as an array.  All attributes
     * are checked for validity before adding.
     *
     * @param attributes All feature attributes.
     * @throws IllegalFeatureException Passed attributes do not match
     * feature type.
     */
    public void setAttributes(Object[] attributes)
        throws IllegalFeatureException {

        // Gets the number of attributes from feature and uses to set valid flag
        int n = schema.attributeTotal();
        boolean isValid = (n == attributes.length);

        // Checks each attribute for validity
        for (int i = 0; i < n; i++) {
            isValid =  schema.getAttributeType(i).getType().
                isAssignableFrom(attributes[i].getClass()) &&
                isValid;
        }

        // If all attributes are valid, add; otherwise throw exception
        if (isValid) {
            System.arraycopy(attributes, 0, this.attributes, 0, n);
        }
        else {
            throw new IllegalFeatureException("Attempted to set illegal flat "
                                              + "feature attributes.");
        }

    }

    /** 
     * Sets a single attribute for this feature, passed as a complex object.
     * If the attribute does not exist or the object does not conform to the
     * internal feature type, an exception is thrown.
     *
     * @param xPath XPath representation of attribute location.
     * @param attribute Feature attribute to set.
     * @throws IllegalFeatureException Passed attribute does not match
     * feature type
     */
    public void setAttribute(String xPath, Object attribute)
        throws IllegalFeatureException {
        
        _log.debug("about to set attribute");

        AttributeType definition = null;

        _log.debug("has attribute: " + schema.hasAttributeType(xPath));

        if (schema.hasAttributeType(xPath)) {
            //_log.debug("attribute: " + definition.toString());

            definition = schema.getAttributeType(xPath);
            if (definition.getType().isAssignableFrom(attribute.getClass())) {
                //_log.info("position: " + definition.getPosition());
                attributes[definition.getPosition()] = attribute;
            }
            else {
                String message = "Attempted to add attribute that does not match "
                    + "expected type -> attribute: " + xPath + ", type (passed): "
                    + attribute.getClass().toString() + ", type (expected): "
                    + definition.getType().toString();
                throw new IllegalFeatureException(message);
            }            
        }
        else {
            String message = "Could not find requested attribute: " + xPath;
            throw new IllegalFeatureException(message);
        }
    }


    /* ***********************************************************************
     * Geometry handling methods.
     * ***********************************************************************/
    /** 
     * Gets the geometry for this feature.
     *
     * @return Geometry for this feature.
     */
    public Geometry getDefaultGeometry() {
        AttributeType gType = schema.getDefaultGeometry();
        _log.debug("schema " + schema + " \n gType = " + gType);
        _log.debug("fetching geometry from " + gType.getPosition() + " -> " + attributes[gType.getPosition()]);
        return (Geometry) ((Geometry) attributes[gType.getPosition()]).clone();
    }

    /** 
     * Modifies the geometry.
     *
     * @param geometry All feature attributes.
     */
    public void setDefaultGeometry(Geometry geometry)
        throws IllegalFeatureException {

        AttributeType geometryAttribute = schema.getDefaultGeometry();
        if (geometryAttribute.getType().equals(geometry.getClass().getName())) {
            attributes[geometryAttribute.getPosition()] = (Geometry) geometry.clone();
        }
        else {
            String message = "Cannot add geometry that does not match type.";
            throw new IllegalFeatureException(message);
        }
    }


    /* ***********************************************************************
     * Necessary overrides.
     * ***********************************************************************/
    /** 
     * Creates an exact copy of this feature.
     *
     * @return A flat feature.
     */
    public Object clone() {
        FeatureFlat exactCopy = null;
        try {
            exactCopy = new FeatureFlat(this.schema, this.attributes);
        }
        catch (IllegalFeatureException e) {
            // Can never happen
        }
        return exactCopy;
    }

    /** 
     * Returns a copy of all this feature's attributes and its WKT geometry.
     *
     * @return A representation of this feature as a string.
     */
    public String toString() {
        StringBuffer returnString = new StringBuffer("\n" + schema.getName() + " -> \n");
        
        for (int i = 0, n = attributes.length; i < n; i++) {
            returnString.append(attributes[i].toString() + "\n");
        }
        return returnString.toString();        
    }

    /** 
     * Returns a copy of all this feature's attributes and its WKT geometry.
     *
     * @return A representation of this feature as a string.
     */
    public String toString(boolean includeSchema) {
        if (includeSchema) {
            return schema.toString() + "\n" + toString();
        }
        else {
            return toString();
        }
    }
    
}
