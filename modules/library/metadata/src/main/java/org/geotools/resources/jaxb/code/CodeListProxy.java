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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import org.opengis.util.CodeList;


/**
 * Stores information about {@link CodeList}, in order to handle format defined in ISO-19139
 * about the {@code CodeList} tags.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
@XmlType(name = "CodeList", propOrder = { "codeListValue", "codeList" })
public class CodeListProxy {
    /**
     * Default common URL path for the {@code codeList} attribute value.
     */
    private static final String URL = "http://www.tc211.org/ISO19139/resources/codeList.xml#";

    /**
     * The {@code codeList} attribute in the XML tags.
     */
    private String codeList;

    /**
     * The {@code codeListValue} attribute in the XML tags.
     */
    private String codeListValue;

    /**
     * Default empty constructor for JAXB used.
     */
    public CodeListProxy() {
    }

    /**
     * Builds a {@link CodeList} as defined in ISO-19139 standard.
     *
     * @param languageCode The language code to define for this {@link CodeList}.
     */
    public CodeListProxy(final String codeList) {
        this.codeList = URL.concat(codeList);
    }

    /**
     * Builds a proxy instance of {@link CodeList}. It stores the values that will be
     * used for marshalling.
     *
     * @param codeList The CodeList to use.
     */
    public CodeListProxy(final CodeList codeList) {
        final String umlName = codeList.identifier();
        this.codeList = URL.concat(umlName);
    }

    /**
     * Returns the {@code codeList} attribute, as defined in ISO-19139.
     */
    @XmlAttribute(name = "codeList", required = true)
    public String getCodeList() {
        return codeList;
    }

    /**
     * Returns the {@code codeListValue} attribute, as defined in ISO-19139.
     */
    @XmlAttribute(name = "codeListValue", required = true)
    public String getCodeListValue() {
        return codeListValue;
    }

    /**
     * Sets the {@code codeList}. It will be automatically called at
     * unmarshalling-time.
     */
    public void setCodeList(final String codeList) {
        this.codeList = codeList;
    }

    /**
     * Sets the {@code codeListValue}. It will be automatically called at
     * unmarshalling-time.
     */
    public void setCodeListValue(final String codeListValue) {
        this.codeListValue = codeListValue;
    }
}
