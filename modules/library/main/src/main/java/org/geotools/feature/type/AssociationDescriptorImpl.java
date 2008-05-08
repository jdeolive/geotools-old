package org.geotools.feature.type;

import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.Name;

public class AssociationDescriptorImpl extends PropertyDescriptorImpl 
		implements AssociationDescriptor {

	public AssociationDescriptorImpl(AssociationType type, Name name, int min, int max, boolean isNillable) {
		super(type,name,min,max,isNillable);
	}
	
	public AssociationType getType() {
		return (AssociationType) super.getType();
	}
}