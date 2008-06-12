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
package org.geotools.resources.jaxb.metadata;

import java.util.Locale;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * JAXB adapter for {@link Locale}, in order to integrate the value in a tags
 * respecting the ISO-19139 standard.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public class LocaleAdapter extends XmlAdapter<LocaleAdapter, Locale> {
    /**
     * The locale value.
     */
    private Locale locale;

    /**
     * Empty constructor for JAXB only.
     */
    private LocaleAdapter() {
    }

    /**
     * Builds an adapter for {@link Locale}.
     *
     * @param date The date to marshall.
     */
    protected LocaleAdapter(final Locale locale) {
        this.locale = locale;
    }

    /**
     * Returns the date matching with the metadata value. This method is systematically
     * called at marshalling-time by JAXB.
     */
    @XmlElement(name = "CharacterString", namespace = "http://www.isotc211.org/2005/gco")
    public String getLocale() {
        return locale.getLanguage();
    }

    /**
     * Sets the value for the metadata date. This method is systematically called at
     * unmarshalling-time by JAXB.
     */
    public void setLocale(final String locale) {
        for (Locale candidate : Locale.getAvailableLocales()) {
            if (candidate.getISO3Language().equalsIgnoreCase(locale)) {
                this.locale = candidate;
                return;
            }
        }
        this.locale = new Locale(locale);
    }

    /**
     * Does the link between a date red from an XML stream and the object which will
     * contains this value. JAXB calls automatically this method at unmarshalling-time.
     *
     * @param value The adapter for this metadata value.
     * @return A {@linkplain Locale locale} which represents the metadata value.
     */
    public Locale unmarshal(final LocaleAdapter value) {
        if (value == null) {
            return null;
        }
        return value.locale;
    }

    /**
     * Does the link between {@linkplain Locale locale} and the way they will be marshalled into
     * an XML file or stream. JAXB calls automatically this method at marshalling-time.
     *
     * @param value The date value.
     * @return The adapter for this date.
     */
    public LocaleAdapter marshal(final Locale value) {
        if (value == null) {
            return null;
        }
        return new LocaleAdapter(value);
    }
}
