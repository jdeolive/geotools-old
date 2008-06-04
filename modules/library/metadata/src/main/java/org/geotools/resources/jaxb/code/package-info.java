/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
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
/**
 * <p>JAXB designed package for adapters. </p>
 * <p>This package regroups all adapters used to generate
 * {@linkplain org.opengis.util.CodeList code lists}.<br/>
 * Each time JAXB is trying to deal with a code list, the adapter will get in action
 * in order to replace the code list value, which will be normally written directly,
 * by a tags like in the following example of a 
 * {@linkplain org.opengis.metadata.identification.CharacterSet character set} : 
 * {@code <gmd:MD_CharacterSetCode codeList="http://www.tc211.org/ISO19139/resources/codeList.xml#utf8" codeListValue="utf8"/>}
 * </p>
 *
 * @see javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter
 * @see org.opengis.util.CodeList
 * @author Cédric Briançon
 */
@XmlSchema(elementFormDefault= XmlNsForm.QUALIFIED,
namespace="http://www.isotc211.org/2005/gmd",
xmlns = {
    @XmlNs(prefix = "gmd", namespaceURI = "http://www.isotc211.org/2005/gmd"),
    @XmlNs(prefix = "gco", namespaceURI = "http://www.isotc211.org/2005/gco")
})
package org.geotools.resources.jaxb.code;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
