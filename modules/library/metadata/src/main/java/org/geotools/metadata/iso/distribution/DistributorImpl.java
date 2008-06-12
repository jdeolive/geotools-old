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
package org.geotools.metadata.iso.distribution;

import java.util.Collection;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.distribution.DigitalTransferOptions;
import org.opengis.metadata.distribution.Distributor;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.distribution.StandardOrderProcess;
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Information about the distributor.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlType(propOrder={
    "distributorContact", "distributionOrderProcesses", "distributorFormats", "distributorTransferOptions"
})
@XmlRootElement(name = "MD_Distributor")
public class DistributorImpl extends MetadataEntity implements Distributor {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 7142984376823483766L;

    /**
     * Party from whom the resource may be obtained. This list need not be exhaustive.
     */
    private ResponsibleParty distributorContact;

    /**
     * Provides information about how the resource may be obtained, and related
     * instructions and fee information.
     */
    private Collection<StandardOrderProcess> distributionOrderProcesses;

    /**
     * Provides information about the format used by the distributor.
     */
    private Collection<Format> distributorFormats;

    /**
     * Provides information about the technical means and media used by the distributor.
     */
    private Collection<DigitalTransferOptions> distributorTransferOptions;

    /**
     * Constructs an initially empty distributor.
     */
    public DistributorImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public DistributorImpl(final Distributor source) {
        super(source);
    }

    /**
     * Creates a distributor with the specified contact.
     */
    public DistributorImpl(final ResponsibleParty distributorContact) {
        setDistributorContact(distributorContact);
    }

    /**
     * Party from whom the resource may be obtained. This list need not be exhaustive.
     */
    @XmlElement(name = "distributorContact", required = true, namespace = "http://www.isotc211.org/2005/gmd")
    public ResponsibleParty getDistributorContact() {
        return distributorContact;
    }

    /**
     * Set the party from whom the resource may be obtained. This list need not be exhaustive.
     */
    public synchronized void setDistributorContact(final ResponsibleParty newValue) {
        checkWritePermission();
        distributorContact = newValue;
    }

    /**
     * Provides information about how the resource may be obtained, and related
     * instructions and fee information.
     */
    @XmlElement(name = "distributionOrderProcess", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<StandardOrderProcess> getDistributionOrderProcesses() {
        return xmlOptional(distributionOrderProcesses = nonNullCollection(distributionOrderProcesses,
                                                              StandardOrderProcess.class));
    }

    /**
     * Set information about how the resource may be obtained, and related
     * instructions and fee information.
     */
    public synchronized void setDistributionOrderProcesses(
            final Collection<? extends StandardOrderProcess> newValues)
    {
        distributionOrderProcesses = copyCollection(newValues, distributionOrderProcesses,
                                                    StandardOrderProcess.class);
    }

    /**
     * Provides information about the format used by the distributor.
     */
    @XmlElement(name = "distributorFormat", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<Format> getDistributorFormats() {
        return xmlOptional(distributorFormats = nonNullCollection(distributorFormats, Format.class));
    }

    /**
     * Set information about the format used by the distributor.
     */
    public synchronized void setDistributorFormats(final Collection<? extends Format> newValues) {
        distributorFormats = copyCollection(newValues, distributorFormats, Format.class);
    }

    /**
     * Provides information about the technical means and media used by the distributor.
     */
    @XmlElement(name = "distributorTransferOptions", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<DigitalTransferOptions> getDistributorTransferOptions() {
        return xmlOptional(distributorTransferOptions = nonNullCollection(distributorTransferOptions,
                                                              DigitalTransferOptions.class));
    }

    /**
     * Provides information about the technical means and media used by the distributor.
     */
    public synchronized void setDistributorTransferOptions(
            final Collection<? extends DigitalTransferOptions> newValues)
    {
        distributorTransferOptions = copyCollection(newValues, distributorTransferOptions,
                                                    DigitalTransferOptions.class);
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
