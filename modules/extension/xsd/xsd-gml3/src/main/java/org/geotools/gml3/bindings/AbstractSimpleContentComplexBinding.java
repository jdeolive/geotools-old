/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2009, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.gml3.bindings;

import org.geotools.xml.AbstractComplexBinding;
import org.opengis.feature.ComplexAttribute;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * Abstract base class for bindings for XML complexType with simpleContent.
 * 
 * <p>
 * 
 * This class supports the extraction of a simpleContent property as well as encoding XML attributes
 * stored in the UserData map.
 * 
 * @author Ben Caradoc-Davies, CSIRO Exploration and Mining
 */
public abstract class AbstractSimpleContentComplexBinding extends AbstractComplexBinding {

    /**
     * @see org.geotools.xml.AbstractComplexBinding#encode(java.lang.Object, org.w3c.dom.Document,
     *      org.w3c.dom.Element)
     */
    @Override
    public Element encode(Object object, Document document, Element value) throws Exception {
        if (object instanceof ComplexAttribute) {
            ComplexAttribute complex = (ComplexAttribute) object;
            GML3EncodingUtils.encodeClientProperties(complex, value);
            GML3EncodingUtils.encodeSimpleContent(complex, document, value);
        } else if (object instanceof String) {
            Text text = document.createTextNode((String) object);
            value.appendChild(text);
        }
        return value;
    }
}
