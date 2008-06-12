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
package org.geotools.resources.jaxb.uom;

import javax.xml.bind.annotation.XmlElement;
import org.geotools.resources.jaxb.primitive.*;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * The ISO-19103 standard specifies that {@code Measures} require a {@code unit of measure}
 * defined, using the namespace {@code gco} linked to the following url
 * {@link http://www.isotc211.org/2005/gco}.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class MeasureInPixelAdapter extends XmlAdapter<MeasureInPixelAdapter, Double> {
    /**
     * The unit of measure.
     */
    private String uom = "pixel";

    /**
     * The double value to handle.
     */
    private Double value;

    /**
     * Empty constructor used only by JAXB.
     */
    private MeasureInPixelAdapter() {
    }

    /**
     * Constructs an adapter for this value.
     */
    protected MeasureInPixelAdapter(final Double value) {
        this.value = value;
    }

    /**
     * Returns a proxy representation of the {@code <gco:Measure>} tags.
     */
    @XmlElement(name = "Measure", namespace = "http://www.isotc211.org/2005/gco")
    public UOMProxy getMeasureProxy() {
        return new UOMProxy(uom, value);
    }

    /**
     * Sets the new value for the double.
     */
    public void setMeasureProxy(final UOMProxy proxy) {
        this.value = proxy.value;
        this.uom = proxy.uom;
    }

    /**
     * Allows JAXB to generate a Double object using the value found in the adapter.
     *
     * @param value The value extract from the adapter.
     * @return A double object.
     */
    public Double unmarshal(final MeasureInPixelAdapter value) {
        if (value == null) {
            return null;
        }
        return value.value;
    }

    /**
     * Allows JAXB to change the result of the marshalling process, according to the
     * ISO-19139 standard and its requirements about {@code measures}.
     *
     * @param value The double value we want to integrate into a {@code <gco:Measure> tags}.
     * @return An adaptation of the double value, that is to say a double value surrounded
     *         by {@code <gco:Measure>} tags, with an {@code uom} attribute.
     */
    public MeasureInPixelAdapter marshal(final Double value) {
        if (value == null) {
            return null;
        }
        return new MeasureInPixelAdapter(value);
    }
}
