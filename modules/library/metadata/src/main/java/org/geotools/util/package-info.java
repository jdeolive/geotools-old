/**
 * Contains utilities and addition to the collection
 * framework. Some classes implements the {@link java.util.Set} interface
 * and provides facilities for mutually exclusive set, caching or
 * handling ranges of values.
 */
@XmlSchema(elementFormDefault= XmlNsForm.QUALIFIED,
namespace="http://www.isotc211.org/2005/gmd",
xmlns = {
    @XmlNs(prefix = "gmd", namespaceURI = "http://www.isotc211.org/2005/gmd")
})
@XmlAccessorType(XmlAccessType.NONE)
package org.geotools.util;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;
