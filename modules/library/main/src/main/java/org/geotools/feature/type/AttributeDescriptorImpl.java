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
