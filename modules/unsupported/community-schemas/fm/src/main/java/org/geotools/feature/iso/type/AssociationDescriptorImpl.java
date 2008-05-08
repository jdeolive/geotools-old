package org.geotools.feature.iso.type;

import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyType;

public class AssociationDescriptorImpl extends StructuralDescriptorImpl
		implements AssociationDescriptor {

	AssociationType type;
	
	public AssociationDescriptorImpl(AssociationType type, Name name, int min, int max) {
		super(name,min,max);
		this.type = type;
	}
	
	public AssociationType getType() {
		return type;
	}
	
    public PropertyType type() {
        return getType();
    }
    
	public int hashCode(){
		return (37 * minOccurs + 37 * maxOccurs ) ^ 
            (type != null ? type.hashCode() : 0) ^ 
            (name != null ? name.hashCode() : 0);
	}
	
	public boolean equals(Object o){
		if(!(o instanceof AttributeDescriptorImpl))
			return false;
		
		AttributeDescriptorImpl d = (AttributeDescriptorImpl)o;
		return minOccurs == d.minOccurs && 
			maxOccurs == d.maxOccurs && 
			name.equals(d.name) && 
			type.equals(d.type);
			
	}
	
	public String toString(){
		return "AssociationDescriptor "+getName().getLocalPart()+" to "+getType();
	}

}