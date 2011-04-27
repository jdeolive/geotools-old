package org.geotools.filter.v2_0.bindings;

import javax.xml.namespace.QName;

import org.geotools.filter.v2_0.FES;
import org.geotools.xml.AbstractComplexEMFBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;
import org.opengis.filter.FilterFactory;

public class ResourceIdTypeBinding extends AbstractComplexEMFBinding {

    FilterFactory factory;
    
    public ResourceIdTypeBinding(FilterFactory factory) {
        this.factory = factory;
    }
    
    public QName getTarget() {
        return FES.ResourceIdType;
    }

    @Override
    public Object parse(ElementInstance instance, Node node, Object value) throws Exception {
        return factory.featureId((String)node.getAttributeValue("rid"));
    }
}
