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

import javax.xml.bind.annotation.XmlElement;
import org.opengis.metadata.maintenance.ScopeCode;


/**
 * JAXB adapter for {@link Scope}, in order to integrate the value in a tags
 * respecting the ISO-19139 standard. See package documentation to have more information
 * about the handling of CodeList in ISO-19139.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class ScopeAdapter extends CodeListAdapter<ScopeAdapter, ScopeCode> {
    /**
     * Ensures that the adapted code list class is loaded.
     */
    static {
        ensureClassLoaded(ScopeCode.class);
    }

    /**
     * Empty constructor for JAXB only.
     */
    private ScopeAdapter() {
    }

    public ScopeAdapter(final CodeListProxy proxy) {
        super(proxy);
    }

    protected ScopeAdapter wrap(CodeListProxy proxy) {
        return new ScopeAdapter(proxy);
    }

    protected Class<ScopeCode> getCodeListClass() {
        return ScopeCode.class;
    }

    @XmlElement(name = "MD_ScopeCode")
    public CodeListProxy getCodeListProxy() {
        return proxy;
    }

    public void setCodeListProxy(final CodeListProxy proxy) {
        this.proxy = proxy;
    }
}
