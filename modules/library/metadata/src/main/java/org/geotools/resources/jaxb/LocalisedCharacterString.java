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

import java.util.Locale;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;


/**
 * Represents the {@code <LocalisedCharacterString>} tags nested in a {@code <textGroup>}
 * one.
 * It contains a string and its matching {@linkplain Locale locale}.
 *
 * @see TextGroup
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public class LocalisedCharacterString {
    /**
     * A prefix to concatenate with the {@linkplain Locale#getLanguage() language code}
     * in order to have the attribute value specified in ISO-19139 for this tags.
     */
    private static final String LOC = "#locale-";

    /**
     * The locale value for this string.
     */
    private Locale locale;

    /**
     * The string written in this locale.
     */
    private String text;

    /**
     * Empty constructor only used by JAXB.
     */
    private LocalisedCharacterString() {
    }

    /**
     * Constructs a localised string for the locale and the string specified.
     *
     * @param locale The {@link Locale} in which the string is written.
     * @param text The string.
     */
    public LocalisedCharacterString(final Locale locale, final String text) {
        this.locale = locale;
        this.text = text;
    }

    /**
     * Returns the locale language, as ISO-19139 expects to have in attribute of the
     * {@code <LocalisedCharacterString>} tags.
     */
    @XmlAttribute(name = "locale", required = true)
    public String getLocaleLanguage() {
        return (locale != null) ? LOC.concat(locale.getLanguage()) : null;
    }

    /**
     * Returns the {@linkplain Locale locale} as it is stored in this class.
     * JAXB will not use it during {@code Unmarshalling} process.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Returns the text in the locale specified in this class.
     * JAXB uses it to put this string as a value of the {@code <LocalisedCharacterString>}
     * tags in the XML tree at marshalling-time.
     */
    @XmlValue
    public String getText() {
        return text;
    }

    /**
     * Sets the locale language, using a string of this format : {@code #locale-xx},
     * where xx are the two letters representing the language.
     */
    public void setLocaleLanguage(final String locale) {
        this.locale = (locale.contains("-")) ?
            new Locale(locale.substring(locale.indexOf("-") + 1)) : new Locale(locale);
    }

    /**
     * Sets the {@linkplain Locale locale}.
     * JAXB will not use it during {@code Unmarshalling} process.
     */
    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    /**
     * Sets the text in the locale specified in this class.
     * JAXB uses it to get this string from the {@code <LocalisedCharacterString>}
     * tags in the XML tree at unmarshalling-time.
     */
    public void setText(final String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        if (text == null) {
            return "text | null";
        }
        if (locale == null) {
            return "null | " + text;
        }
        return locale.getLanguage() + " | " + text;
    }

    @Override
    public boolean equals(final Object object) {
        if (!(object instanceof LocalisedCharacterString)) {
            return false;
        }
        final LocalisedCharacterString localised = (LocalisedCharacterString) object;
        if (!localised.locale.equals(locale)) {
            return false;
        }
        if (!localised.text.equals(text)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 31 * hash + (locale != null ? locale.hashCode() : 0);
        hash = 31 * hash + (text != null ? text.hashCode() : 0);
        return hash;
    }
}
