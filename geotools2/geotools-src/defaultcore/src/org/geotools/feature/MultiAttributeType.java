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
import com.vividsolutions.jts.geom.Geometry;

/**
 * Class to handle more than one occurance of an attribute.  There may 
 * be better ways to do this, but this seems to work.  
 *
 * @version $Id: MultiAttributeType.java,v 1.2 2003/07/17 07:09:53 ianschneider Exp $
 * @authr Chris Holmes
 */
public class MultiAttributeType extends DefaultAttributeType {
    
    /** Name of this attribute. */
    //private final String name;
        
    /** Class type of this attribute. */
    //private final Class type;
        
    /** Number of instances of this attribute in the schema. */
    private int maxOccur = 1;
    
    private AttributeType validator;
    
    private int minOccur = 1;

    /**
     * Constructor with name and type.
     *
     * @param name Name of this attribute.
     * @param type Class type of this attribute.
     */
    public MultiAttributeType(AttributeType validator) {
	super(validator.getName(), validator.getType(),false);
	this.validator = validator;
    }
    
    /**
     * Constructor with geometry.
     *
     * @param name Name of this attribute.
     * @param type Class type of this attribute.
     * @param maxOccur Number of instances of this attribute in the schema.
     */
    public MultiAttributeType(AttributeType validator, int maxOccur) {
	this(validator);
        this.maxOccur = maxOccur;
    }
    
    
     /**
     * Constructor with geometry.
     *
     * @param name Name of this attribute.
     * @param type Class type of this attribute.
     * @param maxOccur Number of instances of this attribute in the schema.
     */
    public MultiAttributeType(AttributeType validator, int maxOccur,
			      int minOccur) {
	this(validator, maxOccur);
	this.minOccur = minOccur;
    }



    /**
     * Gets the name of this attribute.
     * 
     * @return Name.
     */
    /*public String getName() {
        return validator.getName();
	}*/
    
    /**
     * Gets the type of this attribute.
     * 
     * @return Type.
     */
    //public Class getType() {
    //  return validator.getType();
    //}
    
    /**
     * Gets the maxOccur of this attribute.
     * 
     * @return MaxOccur.
     */
    public int getMaxOccurs() {
        return maxOccur;
    }
    
     /**
     * Gets the minimum number of elements that pass the validator
     * that must be in the list to validate.
     * 
     * @return MaxOccur.
     */
    public int getMinOccurs() {
        return minOccur;
    }





    /**
     * Returns whether the attribute is a geometry.
     * Should this be false?  Even if the attributes are geometries?
     * Because this itself isn't actually a geometry, so it can't be used
     * as a geometry.
     * @return true if the attribute's type is a geometry.
     */
    public boolean isGeometry(){
	return false;
	//return Geometry.class.isAssignableFrom(getType());
    }
    
    //revisit:
    public Object clone()
        throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean isValid(Object obj){
	if ((obj == null) && nillable) {
	    return true;
	} else {
	    if (obj instanceof List) {
		int i = 0;
		for (Iterator iter = ((List)obj).iterator(); 
		     iter.hasNext(); i++){
           try {
		    validator.validate(iter.next()); 
        } catch (IllegalArgumentException iae) {
          return false;
        }
			
		    }
		}
		//TODO: checking occurancs here i < minOccurs, i > maxOccurs.
		//need to work out default values for them.
		return true;
	    } 
	
    }

    /**
     * Gets a representation of this object as a string.
     *
     * @return A representation of this object as a string
     */
    public String toString() {
        StringBuffer returnString = new StringBuffer("MultiAttributeType [ ");
						     
        returnString.append("name=").append(name).append(',');
        returnString.append("type=").append(type.getName()).append(',');
        returnString.append("maxOccurs=").append(maxOccur).append(',');
        returnString.append("minOccur=").append(minOccur).append(" ]");
        return returnString.toString();
    }
}
