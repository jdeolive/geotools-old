/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2004, Institut de Recherche pour le Développement
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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.constraint.Classification;
import org.opengis.metadata.constraint.SecurityConstraints;
import org.opengis.util.InternationalString;


/**
 * Handling restrictions imposed on the resource for national security or similar security concerns.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlType(name = "MD_SecurityConstraints", propOrder={
    "classification", "userNote", "classificationSystem", "handlingDescription"
})
@XmlRootElement(name = "MD_SecurityConstraints")
public class SecurityConstraintsImpl extends ConstraintsImpl implements SecurityConstraints {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6412833018607679734L;;

    /**
     * Name of the handling restrictions on the resource.
     */
    private Classification classification;

    /**
     * Explanation of the application of the legal constraints or other restrictions and legal
     * prerequisites for obtaining and using the resource.
     */
    private InternationalString userNote;

    /**
     * Name of the classification system.
     */
    private InternationalString classificationSystem;

    /**
     * Additional information about the restrictions on handling the resource.
     */
    private InternationalString handlingDescription;

    /**
     * Creates an initially empty security constraints.
     */
    public SecurityConstraintsImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public SecurityConstraintsImpl(final SecurityConstraints source) {
        super(source);
    }

    /**
     * Creates a security constraints initialized with the specified classification.
     */
    public SecurityConstraintsImpl(final Classification classification) {
        setClassification(classification);
    }

    /**
     * Returns the name of the handling restrictions on the resource.
     */
    @XmlElement(name = "classification", required = true, namespace = "http://www.isotc211.org/2005/gmd")
    public Classification getClassification() {
        return classification;
    }

    /**
     * Set the name of the handling restrictions on the resource.
     */
    public synchronized void setClassification(final Classification newValue) {
        checkWritePermission();
        classification = newValue;
    }

    /**
     * Returns the explanation of the application of the legal constraints or other restrictions and legal
     * prerequisites for obtaining and using the resource.
     */
    @XmlElement(name = "userNote", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public InternationalString getUserNote() {
        return userNote;
    }

    /**
     * Set the explanation of the application of the legal constraints or other restrictions and legal
     * prerequisites for obtaining and using the resource.
     */
    public synchronized void setUserNote(final InternationalString newValue) {
        checkWritePermission();
        userNote = newValue;
    }

    /**
     * Returns the name of the classification system.
     */
    @XmlElement(name = "classificationSystem", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public InternationalString getClassificationSystem() {
        return classificationSystem;
    }

    /**
     * Set the name of the classification system.
     */
    public synchronized void setClassificationSystem(final InternationalString newValue) {
        checkWritePermission();
        classificationSystem = newValue;
    }

    /**
     * Returns the additional information about the restrictions on handling the resource.
     */
    @XmlElement(name = "handlingDescription", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public InternationalString getHandlingDescription() {
        return handlingDescription;
    }

    /**
     * Set the additional information about the restrictions on handling the resource.
     */
    public synchronized void setHandlingDescription(final InternationalString newValue) {
        checkWritePermission();
        handlingDescription = newValue;
    }
}
