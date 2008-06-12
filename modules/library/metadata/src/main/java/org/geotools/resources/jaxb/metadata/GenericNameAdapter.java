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
package org.geotools.resources.jaxb.metadata;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.geotools.util.LocalName;


/**
 * JAXB adapter in order to map implementing class with the GeoAPI interface. See
 * package documentation for more information about JAXB and interface.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class GenericNameAdapter
        extends XmlAdapter<org.geotools.util.GenericName,org.opengis.util.GenericName>
{
    /**
     * Does the link between {@linkplain org.geotools.util.GenericName generic names} and the
     * way they will be unmarshalled. JAXB calls automatically this method at unmarshalling-time.
     *
     * @param value The {@linkplain org.geotools.util.GenericName generic name} value.
     * @return The implementing class for this string.
     */
    public org.opengis.util.GenericName unmarshal(final org.geotools.util.GenericName value) {
        return (value == null) ? null : value;
    }

    /**
     * Does the link between a {@link org.opengis.util.GenericName} and the string associated. JAXB calls
     * automatically this method at marshalling-time.
     *
     * @param value The implementing class for this metadata value.
     * @return A {@linkplain org.geotools.util.GenericName generic name} which represents the metadata value.
     */
    public org.geotools.util.GenericName marshal(final org.opengis.util.GenericName value) {
        return (value == null) ? null :
                                 new LocalName(value.toInternationalString());
    }
}
