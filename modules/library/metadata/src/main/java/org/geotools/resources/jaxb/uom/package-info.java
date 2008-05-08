/**
 * <p>JAXB designed package for adapters. </p>
 * <p>Regroups all adapters designed to handle <i>unit of measure</i> as the
 * ISO-19103 specifications. For example, a measure marshalled with JAXB will be
 * written this way : {@code <gco:Measure uom="pixel">220.0</gco:Measure>}</p>
 *
 * @see javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter
 * @author Cédric Briançon
 */
@XmlSchema(elementFormDefault= XmlNsForm.QUALIFIED,
namespace="http://www.isotc211.org/2005/gmd",
xmlns = {
    @XmlNs(prefix = "gmd", namespaceURI = "http://www.isotc211.org/2005/gmd"),
    @XmlNs(prefix = "gco", namespaceURI = "http://www.isotc211.org/2005/gco"),
    @XmlNs(prefix = "xsi", namespaceURI = "http://www.w3.org/2001/XMLSchema-instance")
})
@XmlAccessorType(XmlAccessType.NONE)
package org.geotools.resources.jaxb.uom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
