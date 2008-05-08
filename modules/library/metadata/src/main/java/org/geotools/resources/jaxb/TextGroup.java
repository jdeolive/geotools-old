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
package org.geotools.resources.jaxb;

import org.geotools.util.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;


/**
 * Regroups the strings translated in different languages in a set. It represents the
 * {@code <textGroup>} tags defined for embedded translations in ISO-19139 standard.
 * 
 * @see LocalisedCharacterString
 * @source $URL$
 * @author Cédric Briançon
 */
public class TextGroup {
    /**
     * A set of {@link LocalisedCharacterString}.
     */
    private Collection<LocalisedCharacterString> localised;

    /**
     * Empty constructor only used by JAXB.
     */
    private TextGroup() {
    }

    /**
     * Constructs a {@linkplain TextGroup text group} from a {@link GrowableInternationalString}.
     */
    public TextGroup(final GrowableInternationalString text) {
        final Set<Locale> locales = text.getLocales();
        final Collection<LocalisedCharacterString> localisedStrings =
                new ArrayList<LocalisedCharacterString>();
        for (Iterator<Locale> it = locales.iterator(); it.hasNext();) {
            final Locale locale = it.next();
            localisedStrings.add(new LocalisedCharacterString(locale, text.toString(locale)));
        }
        this.localised = localisedStrings;
    }

    /**
     * Returns the set of {@linkplain LocalisedCharacterString localised string}.
     * JAXB uses this getter at marshalling-time, in order to generate {@code N} tags
     * {@code <LocalisedCharacterString>} in a single tags {@code <textGroup>}.
     */
    @XmlElementWrapper(name = "textGroup")
    @XmlElement(name = "LocalisedCharacterString")
    public Collection<LocalisedCharacterString> getLocalised() {
        return localised;
    }

    /**
     * Sets localised strings.
     */
    public void setLocalised(final Collection<LocalisedCharacterString> localised) {
        this.localised = localised;
    }

    @Override
    public String toString() {
        if (localised == null) {
            return "localised : null";
        }
        final StringBuffer buffer = new StringBuffer("Collection [");
        for (LocalisedCharacterString i : localised) {
            buffer.append("\n  ").append(i.toString());
        }
        buffer.append("\n]");
        return buffer.toString();
    }
}
