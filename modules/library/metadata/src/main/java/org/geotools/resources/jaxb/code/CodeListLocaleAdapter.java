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
package org.geotools.resources.jaxb.code;

import java.util.Locale;
import javax.xml.bind.annotation.adapters.XmlAdapter;


/**
 * JAXB adapter for {@link Locale}, in order to integrate the value in a tags
 * respecting the ISO-19139 standard. See package documentation to have more information
 * about the handling of CodeList in ISO-19139.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public abstract class CodeListLocaleAdapter<ValueType extends CodeListLocaleAdapter,BoundType extends Locale>
        extends XmlAdapter<ValueType,BoundType>
{
    protected CodeListProxy proxy;

    /**
     * Empty constructor for JAXB only.
     */
    protected CodeListLocaleAdapter() {
    }

    protected CodeListLocaleAdapter(final CodeListProxy proxy) {
        this.proxy = proxy;
    }

    protected abstract ValueType wrap(final CodeListProxy proxy);

    protected abstract Class<BoundType> getCodeListClass();

    public final BoundType unmarshal(final ValueType value) {
        if (value == null) {
            return null;
        }
        return (BoundType) new Locale(value.proxy.getCodeListValue());
    }

    public final ValueType marshal(final BoundType value) throws NoSuchFieldException {
        if (value == null) {
            return null;
        }
        final CodeListProxy codeList = new CodeListProxy("LanguageCode");
        final String identifier = value.getLanguage();
        codeList.setCodeListValue(identifier);
        return wrap(codeList);
    }
}
