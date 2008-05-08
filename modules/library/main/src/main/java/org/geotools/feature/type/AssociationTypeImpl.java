package org.geotools.feature.type;

import java.util.List;

import org.geotools.resources.Utilities;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.filter.Filter;
import org.opengis.util.InternationalString;

public class AssociationTypeImpl extends PropertyTypeImpl implements AssociationType {

    final protected AttributeType relatedType;
	
	public AssociationTypeImpl(
		Name name, AttributeType referenceType, boolean isAbstract, 
		List<Filter> restrictions, AssociationType superType, InternationalString description		
	) {
		super(name, referenceType.getBinding(), isAbstract, restrictions, superType, description);
		this.relatedType = referenceType;
		
		if ( relatedType == null ) {
		    throw new NullPointerException("relatedType");
		}
	}
	
	public AttributeType getRelatedType() {
        return relatedType;
    }

	public AssociationType getSuper() {
		return (AssociationType) super.getSuper();
	}

	public int hashCode() {
        return super.hashCode() ^ relatedType.hashCode();
	}

	public boolean equals(Object other) {
		if (!(other instanceof AssociationTypeImpl)) {
			return false;
		}

		AssociationType ass /*(tee hee)*/ = (AssociationType) other;

		return super.equals(ass) 
		    && Utilities.equals(relatedType, ass.getRelatedType());
	}

	public String toString(){
		return new StringBuffer(super.toString()).append("; relatedType=[")
		    .append(relatedType).append("]").toString();
	}

}
