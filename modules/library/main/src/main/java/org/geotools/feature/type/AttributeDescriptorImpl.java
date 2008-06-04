/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.feature.type;

import org.geotools.resources.Utilities;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;

public class AttributeDescriptorImpl extends PropertyDescriptorImpl 
	implements AttributeDescriptor {
	
	protected final Object defaultValue;
	
	public AttributeDescriptorImpl(
		AttributeType type, Name name, int min, int max, boolean isNillable, Object defaultValue
	) {
	    super(type,name,min,max,isNillable);
		
		this.defaultValue = defaultValue;
	}
	
	public AttributeType getType() {
		return (AttributeType) super.getType();
	}
    
	public Object getDefaultValue() {
		return defaultValue;
	}
	
    public int hashCode(){
		return super.hashCode() ^ 
		    (defaultValue != null ? defaultValue.hashCode() : 0 ); 
	}
	
	public boolean equals(Object o){
		if(!(o instanceof AttributeDescriptorImpl))
			return false;
		
		AttributeDescriptorImpl d = (AttributeDescriptorImpl)o;
	
		return super.equals(o) && Utilities.deepEquals( defaultValue, d.defaultValue );
	}	
	
	public String toString() {
	    return new StringBuffer(super.toString()).append(";defaultValue=")
	        .append(defaultValue).toString();
	}

	public String getLocalName() {
		return getName().getLocalPart();
	}
	
}
