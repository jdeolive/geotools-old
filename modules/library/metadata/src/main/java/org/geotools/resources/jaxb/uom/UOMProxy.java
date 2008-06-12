/*
 *    GeoTools - The Open Source Java GIS Tookit
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
package org.geotools.resources.jaxb.uom;

import org.geotools.resources.jaxb.code.*;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;


/**
 * Stores information about the unit of measure, in order to handle format defined
 * in ISO-19103.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public class UOMProxy {
    /**
     * The value of the measure.
     */
    protected Double value;

    /**
     * The unit of measure.
     */
    protected String uom;

    /**
     * Default empty constructor for JAXB used.
     */
    private UOMProxy() {
    }

    /**
     * Builds a representation of the measure as defined in ISO-19103 standard.
     *
     * @param uom The unit of measure to use.
     * @param value The value of the measure.
     */
    protected UOMProxy(final String uom, final Double value) {
        this.value = value;
        this.uom = uom;
    }

    /**
     * Returns the value of the measure.
     */
    @XmlValue
    public Double getValue() {
        return value;
    }

    /**
     * Sets the value of the measure. It will be automatically called at
     * unmarshalling-time.
     */
    public void setValue(final Double value) {
        this.value = value;
    }

    /**
     * Returns the unit of measure.
     */
    @XmlAttribute(name = "uom", required = true)
    public String getUom() {
        return uom;
    }

    /**
     * Sets the unit of measure. It will be automatically called at
     * unmarshalling-time.
     */
    public void setUom(final String uom) {
        this.uom = uom;
    }
}
