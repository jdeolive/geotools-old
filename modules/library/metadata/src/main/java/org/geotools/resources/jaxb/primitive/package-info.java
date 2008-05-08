/**
 * <p>JAXB designed package for adapters. </p>
 * <p>JAXB is able to write directly primitive type java variables at marshalling-time
 * "as it", nevertheless ISO-19139 specifies this kind of values has to be surrounded by
 * a tags representing the type of this data. The role of these adapters is to add these
 * tags around the java value.</p>
 * <p>For example, a {@link String} value has to be marshalled this way :
 * {@code <gco:CharacterString>my text</gco:CharacterString>}<br/>
 * In this example, gco is the prefix for the namespace url 
 * {@link http://www.isotc211.org/2005/gco}.
 * </p>
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
package org.geotools.resources.jaxb.primitive;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
