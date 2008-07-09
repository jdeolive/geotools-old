/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.metadata.iso.citation;

import java.util.Collection;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.citation.Telephone;
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Telephone numbers for contacting the responsible individual or organization.
 *
 * @author Jody Garnett
 * @author Martin Desruisseaux
 *
 * @since 2.1
 * @source $URL$
 */
@XmlType(propOrder={
    "voices", "facsimiles"
})
@XmlRootElement(name = "CI_Telephone")
public class TelephoneImpl extends MetadataEntity implements Telephone {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 4920157673337669241L;

    /**
     * Telephone numbers by which individuals can speak to the responsible organization or
     * individual.
     */
    private Collection<String> voices;

    /**
     * Telephone numbers of a facsimile machine for the responsible organization or individual.
     */
    private Collection<String> facsimiles;

    /**
     * Constructs a default telephone.
     */
    public TelephoneImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public TelephoneImpl(final Telephone source) {
        super(source);
    }

    /**
     * Returns the telephone numbers by which individuals can speak to the responsible
     * organization or individual.
     *
     * @since 2.4
     */
    @XmlElement(name = "voice", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<String> getVoices() {
        return xmlOptional(voices = nonNullCollection(voices, String.class));
    }

    /**
     * Set the telephone numbers by which individuals can speak to the responsible
     * organization or individual.
     *
     * @since 2.4
     */
    public synchronized void setVoices(final Collection<? extends String> newValues) {
        voices = copyCollection(newValues, voices, String.class);
    }

    /**
     * Returns the telephone numbers of a facsimile machine for the responsible organization
     * or individual.
     *
     * @since 2.4
     */
    @XmlElement(name = "facsimile", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<String> getFacsimiles() {
        return xmlOptional(facsimiles = nonNullCollection(facsimiles, String.class));
    }

    /**
     * Set the telephone number of a facsimile machine for the responsible organization
     * or individual.
     *
     * @since 2.4
     */
    public synchronized void setFacsimiles(final Collection<? extends String> newValues) {
        facsimiles = copyCollection(newValues, facsimiles, String.class);
    }

    /**
     * Sets the {@code xmlMarshalling} flag to {@code true}, since the marshalling
     * process is going to be done.
     * This method is automatically called by JAXB, when the marshalling begins.
     * 
     * @param marshaller Not used in this implementation.
     */
///    private void beforeMarshal(Marshaller marshaller) {
///        xmlMarshalling(true);
///    }

    /**
     * Sets the {@code xmlMarshalling} flag to {@code false}, since the marshalling
     * process is finished.
     * This method is automatically called by JAXB, when the marshalling ends.
     * 
     * @param marshaller Not used in this implementation
     */
///    private void afterMarshal(Marshaller marshaller) {
///        xmlMarshalling(false);
///    }
}
