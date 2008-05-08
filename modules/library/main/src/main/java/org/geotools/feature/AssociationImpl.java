package org.geotools.feature;

import org.opengis.feature.Association;
import org.opengis.feature.Attribute;
import org.opengis.feature.type.AssociationDescriptor;
import org.opengis.feature.type.AssociationType;
import org.opengis.feature.type.AttributeType;

public class AssociationImpl extends PropertyImpl implements Association {

    protected AssociationImpl(Attribute value, AssociationDescriptor descriptor) {
        super(value, descriptor);
    }

    public AttributeType getRelatedType() {
        return getType().getRelatedType();
    }

    public AssociationDescriptor getDescriptor() {
        return (AssociationDescriptor) super.getDescriptor();
    }
    
    public AssociationType getType() {
        return (AssociationType) super.getType();
    }
    
    public Attribute getValue() {
        return (Attribute) super.getValue();
    }
}
