/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.styling;

import org.opengis.filter.expression.Expression;


/**
 * A class to hold Channel information for use in ChannelSelction objects.
 * <pre>
 * &lt;xs:complexType name="SelectedChannelType"&gt;
 *   &lt;xs:sequence&gt;
 *     &lt;xs:element ref="sld:SourceChannelName"/&gt;
 *     &lt;xs:element ref="sld:ContrastEnhancement" minOccurs="0"/&gt;
 *   &lt;/xs:sequence&gt;
 * &lt;/xs:complexType&gt;
 * &lt;xs:element name="SourceChannelName" type="xs:string"/&gt;
 *  </pre>
 *
 * @author iant
 * @source $URL$
 */
public interface SelectedChannelType {
    public void setChannelName(String name);

    public String getChannelName();

    /**
     * @deprecated Use {@link #setContrastEnhancement(ContrastEnhancement))} instead.
     */
    public void setContrastEnhancement(Expression gammaValue);

    public void setContrastEnhancement(ContrastEnhancement enhancement);

    public ContrastEnhancement getContrastEnhancement();

    public void accept(StyleVisitor visitor);

    // public Expression getContrastEnhancement();
}
