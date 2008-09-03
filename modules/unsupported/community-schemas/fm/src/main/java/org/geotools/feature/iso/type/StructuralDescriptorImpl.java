package org.geotools.feature.iso.type;

import org.geotools.resources.Utilities;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.StructuralDescriptor;

public abstract class StructuralDescriptorImpl extends PropertyDescriptorImpl implements
		StructuralDescriptor {

	final protected Name name;
	final protected int minOccurs;
	final protected int maxOccurs;
	
	
	public StructuralDescriptorImpl(Name name, int minOccurs, int maxOccurs) {
		this.name = name;
		this.minOccurs = minOccurs;
		this.maxOccurs = maxOccurs;
	}
	
	public int getMinOccurs() {
		return minOccurs;
	}

	public int getMaxOccurs() {
		return maxOccurs;
	}

	public Name getName() {
		return name;
	}

	public Name name() {
		return getName();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof StructuralDescriptorImpl)) {
			return false;
		}
		
		StructuralDescriptorImpl other = (StructuralDescriptorImpl) obj;
		return Utilities.equals(name,other.name) && 
			(minOccurs == other.minOccurs) && (maxOccurs == other.maxOccurs);
	}
	
	public int hashCode() {
		return (37 * minOccurs + 37 * maxOccurs ) ^ ( name.hashCode() );
	}
	

}
