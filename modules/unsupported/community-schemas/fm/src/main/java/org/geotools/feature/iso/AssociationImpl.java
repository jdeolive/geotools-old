package org.geotools.feature.iso;

import org.geotools.resources.Utilities;
import org.opengis.feature.Association;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;

public class AssociationImpl implements Association {

    /** 
     * The related attribute.
     */
    Attribute related;
    
    /**
     * The descriptor, this can never be null for association.
     */
    AssociationDescriptor descriptor;
    
    public AssociationImpl(Attribute related, AssociationDescriptor descriptor) {
        this.related = related;
        this.descriptor = descriptor;
    }
    
    public AssociationDescriptor getDescriptor() {
        return descriptor;
    }

    public PropertyDescriptor descriptor() {
        return getDescriptor();
    }
    
    public AssociationType getType() {
        return descriptor.getType();
    }

    public AttributeType getRelatedType() {
        return getType().getReferenceType();
    }

    public Attribute getRelated() {
        return related;
    }

    public void setRelated(Attribute related) {
        this.related = related;
    }

    public Name name() {
        return getDescriptor().getName();   
    }

    public boolean equals(Object other) {
        if (!(other instanceof AssociationImpl)) {
            return false;
        }

        AssociationImpl assoc = (AssociationImpl) other;

        if (!Utilities.equals(descriptor,assoc.descriptor))
            return false;

        if (!Utilities.equals(related,assoc.related))
            return false;
        
        return true;
    }
    
    public int hashCode() {
        return 37 * (descriptor.hashCode())  
        + (37 * (related == null ? 0 : related.hashCode()));
        
    }
}
