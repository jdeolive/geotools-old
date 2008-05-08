package org.geotools.feature.iso.type;

import java.util.Set;

import org.geotools.resources.Utilities;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.TypeName;
import org.opengis.util.InternationalString;

public class AssociationTypeImpl extends PropertyTypeImpl implements AssociationType {

	final protected boolean isIdentified;
	
	final protected AssociationType superType;

	final protected AttributeType referenceType;
	
	public AssociationTypeImpl(
		TypeName name, AttributeType referenceType, boolean isIdentified, boolean isAbstract,
		Set/*<Filter>*/ restrictions, AssociationType superType, 
		InternationalString description		
	) {
		super(name, isAbstract, restrictions, description);
		
		this.referenceType = referenceType;
		this.isIdentified = isIdentified;
		this.superType = superType;
	}
	
	public boolean isIdentified() {
		return isAbstract;
	}

	public AssociationType getSuper() {
		return superType;
	}

	public int hashCode() {
        
		return 17 * (getName() == null ? 0 : getName().hashCode()) 
				^ (getReferenceType() == null ? 0 : getReferenceType().hashCode());
	}

	public boolean equals(Object other) {
		if (!(other instanceof AssociationTypeImpl)) {
			return false;
		}

		if (!super.equals(other)) 
			return false;
		
		AssociationType ass /*(tee hee)*/ = (AssociationType) other;

		if (!Utilities.equals(referenceType, ass.getReferenceType())) {
			return false;
		}
		
		if (!Utilities.equals(superType, ass.getSuper())) {
			return false;
		}
	
		return true;
	}

	public AttributeType getReferenceType() {
		return referenceType;
	}
	
	public String toString(){
		return "AssociationType "+getName().getLocalPart() + " reference to " + getReferenceType().getName().getLocalPart();
	}

}
