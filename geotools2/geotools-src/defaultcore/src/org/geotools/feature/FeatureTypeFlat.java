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
import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.*;

/** 
 * <p>A GeoTools representation of a simple feature.
 * A flat feature type enforces the following properties:<ul>
 * <li>Attribute types are restricted to Java primitives and Strings. 
 * <li>Attributes may only have one occurrence.
 * <li>Each feature has a single, non-changing geometry attribute.</ul></p>
 *
 * <p>Flat feature types define features types that may be thought of as
 * 'layers' in traditional GIS parlance.  They are called flat because they
 * do not allow any nested elements, but they also restrict the attribute
 * objects to be very simple data types.</p>
 *
 * @version $Id: FeatureTypeFlat.java,v 1.7 2002/06/20 23:18:28 jmacgill Exp $
 * @author Rob Hranac, VFNY
 */
public class FeatureTypeFlat implements FeatureType {


    private static Category _log = Category.getInstance(FeatureTypeFlat.class.getName());

    /** List of allowed attribute classes: primitive wrappers and String. */
    protected static final List ALLOWED_TYPES = 
        new ArrayList( java.util.Arrays.asList(new Class[] 
            { Boolean.class, 
              Character.class, 
              Byte.class, 
              Short.class, 
              Integer.class, 
              Long.class, 
              Float.class, 
              Double.class, 
              String.class }) );
    
    /** Attribute types of this feature type. */
    protected AttributeType[] attributeTypes;

    /** Mapping between feature type attribute and names. */
    protected HashMap nameMap = new HashMap();

    /** */
    protected int geometryPosition = -1;

    /* These final fields are for the attribute implementation. */
    /** The namespace for this feature type. */
    private String namespace = null; 

    /** The name of this type, within the namespace. */
    private String name = "feature";//hack:? prevents null from being returned 

    /** Number of instances of this feature type allowed. */
    private int occurrences = 1;
    
    /** Position of this feature type. */
    private int position = -1;
    
    
    /**
     * Constructor with geometry only; minimum valid constructor.
     *
     * @param geometry The geometry for this feature type.
     * @return Fully initialized flat feature type.
     */
    public FeatureTypeFlat (AttributeType geometry) {
        geometryPosition = 0;
        attributeTypes = new AttributeType[1];
        geometry = geometry.setPosition(geometryPosition);
        attributeTypes[geometryPosition] = geometry;
        nameMap.put(geometry.getName(), geometry);
    }

    /**
     * Constructor with several attributes.  This constructor will fail if the
     * attribute array does not contain exactly one geometry, the attributes
     * do not match the allowed types, or any of the attributes have more than
     * one occurrence.  As one might expect, this constructor ignores
     * (reassigns) positional information in attributes based on the array
     * order.
     * 
     * @param attributeTypes The attribute types for this feature type.
     * @return Fully initialized flat feature type.
     * @throws SchemaException If missing geometry, more than one geometry, or
     * attribute types do not conform to flat feature type definition.
     */
    public FeatureTypeFlat (AttributeType[] attributeTypes) 
        throws SchemaException {

        // set length variable and conformance test flag
        int n = attributeTypes.length;
        boolean isValid = true;

        // run through each attribute type
        for( int i = 0; i < n; i++) {

            _log.info("starting attribute: " + attributeTypes[i].getName() + " [" + i + "]");
            _log.info("type: " + attributeTypes[i].getType());
            _log.info("is a geometry: " + Geometry.class.isAssignableFrom( attributeTypes[i].getType()));

            // if it is a conforming non-geometry, initialize feature type
            if( isAllowed( attributeTypes[i])) {
            _log.info("is ok attribute");
                nameMap.put( attributeTypes[i].getName(), attributeTypes[i]);
                attributeTypes[i] = attributeTypes[i].setPosition(i);
            }

            // if it is a conforming geometry, initialize feature type
            // note that the validity check sets validity flag to false if
            // a geometry has already been assigned
            else if( ( Geometry.class.
                       isAssignableFrom( attributeTypes[i].getType())) &&
                     ( attributeTypes[i].getOccurrences() == 1)) {
            _log.info("is ok geometry");
                isValid = isValid && (geometryPosition == -1);
                geometryPosition = i;
                _log.debug("GeomPosition = "+geometryPosition);
                nameMap.put( attributeTypes[i].getName(), attributeTypes[i]);
                attributeTypes[i] = attributeTypes[i].setPosition(i);
            }

            // if it is neither, set validity flag to false
            else {
            _log.info("is bad");
                isValid = false; 
            }
        }

        // check validity...note that a geometry must have been assigned
        // copy valid array (for immutability), otherwise throw exception
        if( isValid && ( geometryPosition != -1)) {
            this.attributeTypes = new AttributeType[n];
            System.arraycopy(attributeTypes, 0, this.attributeTypes, 0, n);
        }
        else {
            throw new SchemaException("Attempted to create bad flat feature type.");
        }
        
    }


    /* ***********************************************************************
     * Static Helper methods to check, add and destroy attributes.           *
     * ***********************************************************************/
    private static boolean isAllowed(AttributeType attribute) {
        if( ALLOWED_TYPES.contains(attribute.getType()) &&
            attribute.getOccurrences() == 1) {
            return true;
        }
        else {
            return false;
        }
    }

    private static FeatureTypeFlat addAttribute(AttributeType attribute, FeatureTypeFlat schema) {
        ArrayList tempAttributes = new ArrayList( Arrays.asList(schema.attributeTypes) );
        AttributeType tempAttribute = attribute.setPosition(tempAttributes.size());

        //_log.info("attributes size (before add): " + tempAttributes.size());
        tempAttributes.add(tempAttribute);            
        tempAttributes.trimToSize();
        //_log.info("attributes size (after add): " + tempAttributes.size());
        schema.attributeTypes = (AttributeType []) tempAttributes.toArray(new AttributeType [] {});
        //_log.info("attributes size (new): " + schema.attributeTypes.length);

        //_log.info("attribute: " + tempAttribute);
        //_log.info("putting attribute: " + attribute.getName());
        schema.nameMap.put(attribute.getName(), tempAttribute);
        //_log.info("getting attribute: " + schema.nameMap.get(attribute.getName()));
        return schema;
    }

    private FeatureTypeFlat removeAttribute(String name, FeatureTypeFlat schema) {
        ArrayList tempAttributes = new ArrayList( Arrays.asList(schema.attributeTypes) );
        AttributeType deadAttribute = (AttributeType) nameMap.get(name);

        tempAttributes.remove(deadAttribute.getPosition());
        tempAttributes.trimToSize();
        schema.attributeTypes = (AttributeType []) tempAttributes.toArray(new AttributeType [] {});

        schema.nameMap.remove(name);
        return schema;
    }
    
    /* ***********************************************************************
     * Handles all attribute interface implementation.                       *
     * ***********************************************************************/
    /**
     * Always true.
     *
     * @return Whether or not this represents a feature type (over a
     * 'flat' attribute).
     */
    public boolean isNested() {
        return true;
    }
    
    /**
     * Gets the (unenforced) unique URI for this feature type.
     *
     * @return Namespace of feature type.
     */
    public String getName() {
        return namespace + "/" + name;
    }
    
    /**
     * Gets the (unenforced) unique type for this feature type.
     *
     * @return Type name of feature type.
     */
    public Class getType() {
        return org.geotools.feature.Feature.class;
    }
    
    /**
     * Gets the (unenforced) unique type for this feature type.
     *
     * @return Number of feature type instances.
     */
    public int getOccurrences() {
        return occurrences;
    }
    
    /**
     * Gets the position of this feature type attribute.
     *
     * @return Position.
     */
    public int getPosition() {
        return position;
    }

    /**
     * Sets the position of this attribute.
     *
     * @param position Position of this attribute.
     * @return New attribute, with altered position.
     */
    public AttributeType setPosition(int position) {
        FeatureTypeFlat schemaCopy = null;
        schemaCopy = (FeatureTypeFlat) this.clone();
        schemaCopy.position = this.position;
        return schemaCopy;
    } 


    /* ***********************************************************************
     * Handles all global feature type modifications.                        *
     * ***********************************************************************/
    /**
     * Sets the global feature type namespace.
     *
     * @param namespace URI namespace associated with this feature type.
     * @return A modified copy of this feature type.
     */
    public FeatureType setNamespace(String namespace) {
        FeatureTypeFlat schemaCopy = null;
        schemaCopy = (FeatureTypeFlat) this.clone();
        schemaCopy.namespace = namespace;
        return schemaCopy;
    }

    /**
     * Sets the global feature type name.  Note that type names are not
     * required and should return null if it is not set.
     *
     * @param name Type name associated with this feature type.
     * @return A modified copy of this feature type.
     */
    public FeatureType setTypeName(String name) {
        FeatureTypeFlat schemaCopy = null;
        schemaCopy = (FeatureTypeFlat) this.clone();
        schemaCopy.name = name;

        return schemaCopy;
    }

    /* ***********************************************************************
     * Handles all feature type attribute modifications.                     *
     * ***********************************************************************/
    /**
     * Sets the values for any attribute other than a nested Feature attribute.
     *
     * @param attribute AttributeType to add to the feature type.
     * @return A modified copy of this feature type.
     * @throws SchemaException When the type is not cloneable, occurrences 
     * are illegal.
     */
    public FeatureType setAttributeType(AttributeType attribute)
        throws SchemaException {

        //_log.info("got attribute: " + attribute.toString());
        FeatureTypeFlat schemaCopy = (FeatureTypeFlat) this.clone();
        if( isAllowed(attribute) ) {
            if( !hasAttributeType(attribute.getName()) ) {
                //_log.info("attribute already exists");
                int i = getAttributeType(attribute.getName()).getPosition();
                attribute = attribute.setPosition(i);
                schemaCopy = removeAttribute(attribute.getName(), schemaCopy);
            }
            //_log.info("ready to add");
            schemaCopy = addAttribute(attribute, schemaCopy);
            //_log.info("size after added: " + schemaCopy.attributeTypes.length);
            //_log.info("added");
        }
        else if( ( Geometry.class.
                   isAssignableFrom( attribute.getType())) &&
                 ( attribute.getOccurrences() == 1) && 
                 ( hasAttributeType(attribute.getName())) ) {
                //_log.info("attribute already exists");
                int i = getAttributeType(attribute.getName()).getPosition();
                attribute = attribute.setPosition(i);
                schemaCopy = removeAttribute(attribute.getName(), schemaCopy);
                schemaCopy = addAttribute(attribute, schemaCopy);
        }
        else {
            String message = "Attribute type not allowed: " +
                "name: " + attribute.getName() +
                " type: " + attribute.getType() + 
                " occurences: " + attribute.getOccurrences();
            throw new SchemaException(message);
        }

        //_log.info("about to return");
        return schemaCopy;
    }

    /**
     * Removes the attribute, if it exists.
     *
     * @param attribute AttributeType to add to the feature type.
     * @return A modified copy of this feature type.
     * @throws SchemaException When the attribute does not exist.
     */
    public FeatureType removeAttributeType(String xPath)
        throws SchemaException {

        FeatureTypeFlat schemaCopy = null;
        schemaCopy = (FeatureTypeFlat) this.clone();
        if( hasAttributeType(name) && !getDefaultGeometry().getName().equals(xPath) ) {
            schemaCopy = removeAttribute(name, schemaCopy);
        }
        else {
            String message = "Attribute does not exist: " + name + " or "
                + "attempted to remove geometry from flat feature type.";
                throw new SchemaException(message);
        }
    
        return schemaCopy; 
    }

    /**
     * Sets the initial primary geometry.  The current primary geometry is
     * determined by the feature itself, but this serves as a default for 
     * feature types that do not care about changing geometry references
     * with their states.
     *
     * @param name XPath pointer to attribute.
     * @return A modified copy of this feature type.
     * @throws SchemaException If the attribute is not a geometry.
     */
    public FeatureType setDefaultGeometry(String name)
        throws SchemaException {
        throw new SchemaException("Initial geometry cannot be modified in flat" 
                                       + " feature type.");
    }

    /* ***********************************************************************
     * Handles all namespace retrieval                                       *
     * ***********************************************************************/
    /**
     * Gets the global feature type namespace.
     *
     * @return Namespace of feature type.
     */
    public String getNamespace() {
        return namespace;
    }

    /**
     * Gets the type name for this feature type.
     *
     * @return Namespace of feature type.
     */
    public String getTypeName() {
        return name;
    }


    /* ***********************************************************************
     * Handles all attribute information retreival.                          *
     * ***********************************************************************/
    /**
     * Gets all of the names for the first 'level' of attributes.  This means
     * that nested attributes must be read seperately, via the getNames()
     * method of their feature types or the getAllNames() method.
     *
     * @return Non-nested attribute names.
     */
    public AttributeType[] getAttributeTypes() {
        AttributeType[] attributeTypes = 
            new AttributeType[this.attributeTypes.length];
        System.arraycopy(this.attributeTypes, 0, 
                         attributeTypes, 0, this.attributeTypes.length);
        return attributeTypes;
    }

    /**
     * Gets all of the names for the first 'level' of attributes.  This means
     * that nested attributes must be set seperately, via the getFeatureType()
     * method.
     *
     * @return Nested attribute names.
     */
    public AttributeType[] getAllAttributeTypes() {
        return this.getAttributeTypes();
    }

    /**
     * Checks for attribute existence.
     *
     * @param xPath XPath pointer to attribute.
     * @return True if attribute exists.
     */
    public boolean hasAttributeType(String xPath) {
        return (nameMap.get(name) != null);
    }


    /**
     * Gets the initial geometry.  Features are allowed to change the primary
     * geometry pointer and ignore this method.
     *
     * @return Path to initial geometry as XPath.
     */
    public AttributeType getDefaultGeometry() {
        _log.debug("geometry Position = "+geometryPosition);
        return this.attributeTypes[geometryPosition];
    }

    /**
     * Checks for attribute existence.
     *
     * @param xPath XPath pointer to attribute.
     * @return True if attribute exists.
     */
    public AttributeType getAttributeType(String xPath) {

        //_log.info("has element: " + nameMap.containsValue(attributeTypes[1]));

        /*_log.info("getting attribute: " + nameMap.get(xPath));
          _log.info("getting map size: " + nameMap.size());
        Set mySet = nameMap.keySet(); 
        Iterator myIt = mySet.iterator();
        while( myIt.hasNext() ) {
            _log.info("keys: " + myIt.next());            
        }
        Collection myCol = nameMap.keySet(); 
        myIt = myCol.iterator();
        while( myIt.hasNext() ) {
            _log.info("values: " + myIt.next());            
        }
        */
        return (AttributeType) nameMap.get(xPath);
    }


    /**
     * Checks for attribute existence.
     *
     * @return True if attribute exists.
     */
    public int attributeTotal() {
        return attributeTypes.length;
    }
    
    /**
     * Gets the number of occurrences of this attribute.
     *
     * @param position XPath pointer to attribute.
     * @return Number of occurrences.
     * @throws SchemaException If the attribute does not exist.
     */
    public AttributeType getAttributeType(int position) {
        return attributeTypes[position];
    }


    /**
     * Gets the number of occurrences of this attribute.
     *
     * @return Number of occurrences.
     * @throws SchemaException If the attribute does not exist.
     */
    public Object clone() {
        FeatureTypeFlat copy = new FeatureTypeFlat(attributeTypes[0]);
        _log.debug("about to clone");
        _log.debug("instantiated new copy");

        copy.attributeTypes = new AttributeType[attributeTypes.length];
        System.arraycopy(this.attributeTypes, 0, copy.attributeTypes, 0, attributeTypes.length);
        //_log.info("attribute length (original): " + attributeTypes.length);
        //_log.info("attribute length (copy): " + copy.attributeTypes.length);        
        //_log.info("copied attributes");
        copy.name = this.name;
        copy.namespace = this.namespace;
        copy.occurrences = this.occurrences;
        copy.position = this.position;
        copy.geometryPosition = this.geometryPosition;
        copy.nameMap = (HashMap) nameMap.clone();
        //_log.info("test geometry there: " + nameMap.get("testGeometry"));

        return copy;
    }

    /**
     * Gets the number of occurrences of this attribute.
     *
     * @return Number of occurrences.
     * @throws SchemaException If the attribute does not exist.
     */
    public String toString() {
        StringBuffer returnString = new StringBuffer("\n" + namespace + "/" + name);
        returnString.append("\n occurrences: " + this.occurrences);
        returnString.append("\n position: " + this.position);
        returnString.append("\n attributes:");
        for( int i = 0, n = attributeTypes.length; i < n; i++ ) {
            returnString.append("\n  " + this.attributeTypes[i].toString());
        } 
        return returnString.toString();
    }


}
