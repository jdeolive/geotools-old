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
package org.geotools.resources.jaxb;

import org.geotools.util.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.geotools.resources.jaxb.metadata.InternationalStringAdapter;


/**
 * JAXB adapter for {@linkplain org.opengis.util.InternationalString international strings}.
 * It will be used in order to marshall and unmarshall international strings that
 * embedded one or several translations (using the implementing class
 * {@link GrowableInternationalString}).
 *
 * This class represents the {@code <PT_FreeText>} tags specified in ISO-19139.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
@XmlType(name = "PT_FreeText_PropertyType", namespace = "http://www.w3.org/2001/XMLSchema-instance")
public class FreeText extends InternationalStringAdapter {
    /**
     * A set of {@link LocalisedCharacterString}, representing the <textGroup> tags.
     */
    private TextGroup textGroup;

    /**
     * Empty constructor only used by JAXB.
     */
    private FreeText() {
    }

    /**
     * Constructs a {@linkplain TextGroup text group} from a {@link GrowableInternationalString}
     * which could contains several localised strings.
     *
     * @param text An international string which could have several translations embedded for the
     *             same text.
     */
    public FreeText(final GrowableInternationalString text) {
        this.textGroup = new TextGroup(text);
        this.text = (text == null) ? null : text.toString();
    }

    /**
     * Returns the {@linkplain TextGroup text group}.
     */
    @XmlElement(name = "PT_FreeText", required = true, namespace = "http://www.isotc211.org/2005/gmd")
    public TextGroup getTextGroup() {
        return textGroup;
    }

    /**
     * Sets the {@linkplain TextGroup text group} value.
     */
    public void setTextGroup(final TextGroup textGroup) {
        this.textGroup = textGroup;
    }

    @Override
    public String toString() {
        return (textGroup == null) ? "textGroup : null" : textGroup.toString();
    }
}
