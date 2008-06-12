/*
 *    GeoTools - The Open Source Java GIS Tookit
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
package org.geotools.metadata.iso.constraint;

import java.util.Collection;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.opengis.util.InternationalString;
import org.opengis.metadata.constraint.Constraints;
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Restrictions on the access and use of a resource or metadata.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlType(name = "MD_Constraints", propOrder = {"useLimitation"})
@XmlSeeAlso({LegalConstraintsImpl.class, SecurityConstraintsImpl.class})
@XmlRootElement(name = "MD_Constraints")
public class ConstraintsImpl extends MetadataEntity implements Constraints {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7197823876215294777L;

    /**
     * Limitation affecting the fitness for use of the resource. Example, "not to be used for
     * navigation".
     */
    private Collection<InternationalString> useLimitation;

    /**
     * Constructs an initially empty constraints.
     */
    public ConstraintsImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public ConstraintsImpl(final Constraints source) {
        super(source);
    }

    /**
     * Returns the limitation affecting the fitness for use of the resource. Example, "not to be used for
     * navigation".
     */
    @XmlElement(name = "useLimitation", required = false,
                namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<InternationalString> getUseLimitation() {
        return xmlOptional(useLimitation = nonNullCollection(useLimitation, InternationalString.class));
    }

    /**
     * Set the limitation affecting the fitness for use of the resource. Example, "not to be used for
     * navigation".
     */
    public synchronized void setUseLimitation(
            final Collection<? extends InternationalString> newValues)
    {
        useLimitation = copyCollection(newValues, useLimitation, InternationalString.class);
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
