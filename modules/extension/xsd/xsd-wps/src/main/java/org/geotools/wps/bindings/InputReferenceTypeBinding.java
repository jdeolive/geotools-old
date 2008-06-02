/**
    @author lreed@refractions.net
 */

package org.geotools.wps.bindings;

import java.util.List;

import javax.xml.namespace.QName;

import net.opengis.wps.InputReferenceType;
import net.opengis.wps.MethodType;
import net.opengis.wps.WpsFactory;

import org.geotools.wps.WPS;
import org.geotools.xml.ComplexEMFBinding;
import org.geotools.xml.ElementInstance;
import org.geotools.xml.Node;

public class InputReferenceTypeBinding extends ComplexEMFBinding
{
    private WpsFactory factory;

    public InputReferenceTypeBinding(WpsFactory factory)
    {
        super(factory, WPS.InputReferenceType);
        this.factory = factory;
    }

    public QName getTarget()
    {
        return WPS.InputReferenceType;
    }

    public Class getType()
    {
        return InputReferenceType.class;
    }

    public Object parse(ElementInstance instance, Node node, Object value) throws Exception
    {
        InputReferenceType inputReference = factory.createInputReferenceType();

        Node attr = node.getAttribute("method");

        if (null != attr)
        {
            attr.setValue(MethodType.get((String)attr.getValue()));
        }

        return super.parse(instance, node, value);
    }
}
