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
package org.geotools.resources.jaxb.code;

import javax.xml.bind.annotation.XmlElement;
import org.opengis.metadata.content.CoverageContentType;


/**
 * JAXB adapter for {@link CoverageContentType}, in order to integrate the value in a tags
 * respecting the ISO-19139 standard. See package documentation to have more information
 * about the handling of CodeList in ISO-19139.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class CoverageContentTypeAdapter
        extends CodeListAdapter<CoverageContentTypeAdapter, CoverageContentType>
{
    /**
     * Ensures that the adapted code list class is loaded.
     */
    static {
        ensureClassLoaded(CoverageContentType.class);
    }

    /**
     * Empty constructor for JAXB only.
     */
    private CoverageContentTypeAdapter() {
    }

    public CoverageContentTypeAdapter(final CodeListProxy proxy) {
        super(proxy);
    }

    protected CoverageContentTypeAdapter wrap(CodeListProxy proxy) {
        return new CoverageContentTypeAdapter(proxy);
    }

    protected Class<CoverageContentType> getCodeListClass() {
        return CoverageContentType.class;
    }

    @XmlElement(name = "MD_CoverageContentTypeCode")
    public CodeListProxy getCodeListProxy() {
        return proxy;
    }

    public void setCodeListProxy(final CodeListProxy proxy) {
        this.proxy = proxy;
    }
}
