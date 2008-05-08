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
package org.geotools.resources.jaxb.metadata;

import javax.xml.bind.JAXBException;
import org.geotools.resources.jaxb.FreeText;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.geotools.util.GrowableInternationalString;
import org.geotools.resources.jaxb.LocalisedCharacterString;
import org.geotools.util.SimpleInternationalString;
import org.geotools.resources.jaxb.TextGroup;
import org.opengis.util.InternationalString;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
@XmlSeeAlso(FreeText.class)
public class InternationalStringAdapter
        extends XmlAdapter<InternationalStringAdapter, InternationalString> {
    /**
     * The text value.
     */
    protected String text;

    /**
     * Empty constructor for JAXB only.
     */
    protected InternationalStringAdapter() {
    }

    /**
     * Builds an adapter for {@link InternationalString}.
     *
     * @param text The string to marshall.
     */
    public InternationalStringAdapter(final String text) {
        this.text = text;
    }

    /**
     * Returns the string generated from the metadata value. This method is
     * systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "CharacterString", namespace = "http://www.isotc211.org/2005/gco")
    public String getText() {
        return text;
    }

    /**
     * Sets the value for the string. This method is systematically called at
     * unmarshalling-time by JAXB.
     */
    public void setText(final String text) {
        this.text = text;
    }

    /**
     * Does the link between an {@link InternationalString} red from an XML stream and the
     * object which will contains this value. JAXB calls automatically this method at
     * unmarshalling-time.
     *
     * @param value The adapter for this metadata value.
     * @return An {@link InternationalString} which represents the metadata value.
     */
    public InternationalString unmarshal(final InternationalStringAdapter value) {
        if (value == null) {
            return null;
        }
        if (value instanceof FreeText) {
            final FreeText freeText = (FreeText) value;
            final TextGroup textGroup = freeText.getTextGroup();
            if (textGroup == null) {
                return null;
            }
            final GrowableInternationalString growable = new GrowableInternationalString();
            for (LocalisedCharacterString i : textGroup.getLocalised()) {
                growable.add(i.getLocale(), i.getText());
            }
            return growable;
        }
        return new SimpleInternationalString(value.text);
    }

    /**
     * Does the link between {@link InternationalString} and the way they will be marshalled
     * into an XML file or stream. JAXB calls automatically this method at marshalling-time.
     *
     * @param value The string value.
     * @return The adapter for this string.
     */
    public InternationalStringAdapter marshal(final InternationalString value) throws JAXBException {
        if (value == null) {
            return null;
        }
        if (value instanceof SimpleInternationalString) {
            return new InternationalStringAdapter(value.toString(null));
        } else {
            if (value instanceof GrowableInternationalString) {
                final GrowableInternationalString growable = (GrowableInternationalString) value;
                if (this.text == null) {
                    this.text = growable.toString();
                }
                return new FreeText(growable);
            } else {
                throw new JAXBException("The international string given is not an instance of " +
                        "GrowableInternationalString nor SimpleInternationalString. " +
                        "\nType not know in this context.");
            }
        }
    }

    @Override
    public String toString() {
        return (text == null) ? "text : null" : text.toString();
    }
}
