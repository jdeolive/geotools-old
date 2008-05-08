/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2008, Geotools Project Managment Committee (PMC)
 *    (C) 2008, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.resources.jaxb.primitive;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * The ISO-19139 standard specifies that primitive types have to be surrounded by a tags
 * which represents the type of the value, using the namespace {@code gco} linked to the
 * following url {@link http://www.isotc211.org/2005/gco}.
 * For the current case, the double value has to be surrounded by {@code <gco:Decimal>}
 * tags.
 *
 * However JAXB is able to marshall primitive java types directly "as it", it would not
 * wrap the value in the required tags.
 * This is the role of this class.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class DoubleAdapter extends XmlAdapter<DoubleAdapter, Double> {
    /**
     * The double value to handle.
     */
    private Double value;

    /**
     * Empty constructor used only by JAXB.
     */
    private DoubleAdapter() {
    }

    /**
     * Constructs an adapter for this value.
     */
    protected DoubleAdapter(final Double value) {
        this.value = value;
    }

    /**
     * Returns the double value.
     */
    @XmlElement(name = "Decimal", namespace = "http://www.isotc211.org/2005/gco")
    public Double getDouble() {
        return value;
    }

    /**
     * Sets the new value for the double.
     */
    public void setDouble(final Double value) {
        this.value = value;
    }

    /**
     * Allows JAXB to generate a Double object using the value found in the adapter.
     *
     * @param value The value extract from the adapter.
     * @return A double object.
     */
    public Double unmarshal(final DoubleAdapter value) {
        if (value == null) {
            return null;
        }
        return value.value;
    }

    /**
     * Allows JAXB to change the result of the marshalling process, according to the
     * ISO-19139 standard and its requirements about primitive types.
     *
     * @param value The double value we want to surround by a tags representing its type.
     * @return An adaptation of the double value, that is to say a double value surrounded
     *         by {@code <gco:Decimal>} tags.
     */
    public DoubleAdapter marshal(final Double value) {
        if (value == null) {
            return null;
        }
        return new DoubleAdapter(value);
    }
}
