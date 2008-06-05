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
/**
 * {@linkplain org.geotools.metadata.iso.content.ContentInformationImpl Content information} implementation. An explanation
 * for this package is provided in the {@linkplain org.opengis.metadata.content OpenGIS&reg; javadoc}.
 * The remaining discussion on this page is specific to the Geotools implementation.
 */
@XmlSchema(elementFormDefault= XmlNsForm.QUALIFIED,
namespace="http://www.isotc211.org/2005/gmd",
xmlns = {
    @XmlNs(prefix = "gmd", namespaceURI = "http://www.isotc211.org/2005/gmd"),
    @XmlNs(prefix = "gco", namespaceURI = "http://www.isotc211.org/2005/gco"),
    @XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance")
})
@XmlAccessorType(XmlAccessType.NONE)
@XmlJavaTypeAdapters({
    @XmlJavaTypeAdapter(CoverageContentTypeAdapter.class),
    @XmlJavaTypeAdapter(RangeDimensionAdapter.class),
    @XmlJavaTypeAdapter(CitationAdapter.class),
    @XmlJavaTypeAdapter(LocaleAdapter.class),
    @XmlJavaTypeAdapter(ImagingConditionAdapter.class),
    @XmlJavaTypeAdapter(IdentifierAdapter.class),
    @XmlJavaTypeAdapter(DateAdapter.class),
    @XmlJavaTypeAdapter(StringAdapter.class),
    @XmlJavaTypeAdapter(InternationalStringAdapter.class),
    @XmlJavaTypeAdapter(RecordTypeAdapter.class),
    // Primitive type handling
    @XmlJavaTypeAdapter(DoubleAdapter.class),
    @XmlJavaTypeAdapter(type=double.class, value=DoubleAdapter.class),
    @XmlJavaTypeAdapter(FloatAdapter.class),
    @XmlJavaTypeAdapter(type=float.class, value=FloatAdapter.class),
    @XmlJavaTypeAdapter(IntegerAdapter.class),
    @XmlJavaTypeAdapter(type=int.class, value=IntegerAdapter.class),
    @XmlJavaTypeAdapter(LongAdapter.class),
    @XmlJavaTypeAdapter(type=long.class, value=LongAdapter.class),
    @XmlJavaTypeAdapter(BooleanAdapter.class),
    @XmlJavaTypeAdapter(type=boolean.class, value=BooleanAdapter.class)
})
package org.geotools.metadata.iso.content;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapters;
import org.geotools.resources.jaxb.metadata.*;
import org.geotools.resources.jaxb.code.*;
import org.geotools.resources.jaxb.primitive.*;
