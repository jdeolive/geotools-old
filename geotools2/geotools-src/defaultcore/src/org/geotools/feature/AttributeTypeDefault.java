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

/**
 * Simple, immutable class to store attributes.  This class should be sufficient 
 * for all simple (ie. non-schema) attribute implementations of this interface.
 *
 * @version $Id: AttributeTypeDefault.java,v 1.4 2002/07/04 10:50:08 ianturton Exp $
 * @author Rob Hranac, VFNY
 */
public class AttributeTypeDefault implements AttributeType {
    
    /** Name of this attribute. */
    private final String name;
        
    /** Class type of this attribute. */
    private final Class type;
        
    /** Number of instances of this attribute in the schema. */
    private int occurrences = 1;
    
    /** Storage position of this attribute in the array. */
    private int position = -1;
    
    
    /**
     * Constructor with name and type.
     *
     * @param name Name of this attribute.
     * @param type Class type of this attribute.
     */
    public AttributeTypeDefault (String name, Class type) {
        this.name = name;
        this.type = type;
    }
    
    /**
     * Constructor with geometry.
     *
     * @param name Name of this attribute.
     * @param type Class type of this attribute.
     * @param occurrences Number of instances of this attribute in the schema.
     */
    public AttributeTypeDefault (String name, Class type, int occurrences) {
        this.name = name;
        this.type = type;
        this.occurrences = occurrences;
    }
    
    
    /**
     * Sets the position of this attribute in the schema.
     * 
     * @param position Position of attribute.
     * @return Copy of attribute with modified position.
     */
    public AttributeType setPosition(int position) {
        AttributeTypeDefault tempCopy = 
            new AttributeTypeDefault(this.name, this.type, this.occurrences);
        tempCopy.position = position;
        return tempCopy;
    } 

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
     * @return Name.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Gets the type of this attribute.
     * 
     * @return Type.
     */
    public Class getType() {
        return type;
    }
    
    /**
     * Gets the occurrences of this attribute.
     * 
     * @return Occurrences.
     */
    public int getOccurrences() {
        return occurrences;
    }
    
    /**
     * Gets the position of this attribute.
     * 
     * @return Position.
     */
    public int getPosition() {
        return position;
    }

    
    public Object clone()
        throws CloneNotSupportedException {
        return super.clone();
    }

    /**
     * Gets the number of occurrences of this attribute.
     *
     * @return Number of occurrences.
     * @throws SchemaException If the attribute does not exist.
     */
    public String toString() {
        StringBuffer returnString = new StringBuffer("position:"+this.position + ". ");
        returnString.append(this.name);
        returnString.append(" [type:" + this.type + "]");
        returnString.append(" - occurences:" + this.occurrences);
        return returnString.toString();
    }
}
