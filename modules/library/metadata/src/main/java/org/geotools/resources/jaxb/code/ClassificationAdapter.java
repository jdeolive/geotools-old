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
package org.geotools.resources.jaxb.code;

import javax.xml.bind.annotation.XmlElement;
import org.opengis.metadata.constraint.Classification;


/**
 * JAXB adapter for {@link Classification}, in order to integrate the value in a tags
 * respecting the ISO-19139 standard. See package documentation to have more information
 * about the handling of CodeList in ISO-19139.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class ClassificationAdapter
        extends CodeListAdapter<ClassificationAdapter, Classification>
{
    /**
     * Ensures that the adapted code list class is loaded.
     */
    static {
        ensureClassLoaded(Classification.class);
    }

    /**
     * Empty constructor for JAXB only.
     */
    private ClassificationAdapter() {
    }

    public ClassificationAdapter(final CodeListProxy proxy) {
        super(proxy);
    }

    protected ClassificationAdapter wrap(CodeListProxy proxy) {
        return new ClassificationAdapter(proxy);
    }

    protected Class<Classification> getCodeListClass() {
        return Classification.class;
    }

    @XmlElement(name = "MD_ClassificationCode")
    public CodeListProxy getCodeListProxy() {
        return proxy;
    }

    public void setCodeListProxy(final CodeListProxy proxy) {
        this.proxy = proxy;
    }
}
