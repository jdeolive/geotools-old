/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
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
        Node attr = node.getAttribute("method");

        if (null != attr)
        {
            attr.setValue(MethodType.get((String)attr.getValue()));
        }

        return super.parse(instance, node, value);
    }
}
