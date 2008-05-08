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
package org.geotools.metadata.iso.identification;

import java.util.Collection;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.constraint.Constraints;
import org.opengis.metadata.distribution.Format;
import org.opengis.metadata.identification.AggregateInformation;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.identification.BrowseGraphic;
import org.opengis.metadata.identification.Keywords;
import org.opengis.metadata.identification.Progress;
import org.opengis.metadata.identification.Usage;
import org.opengis.metadata.maintenance.MaintenanceInformation;
import org.opengis.util.InternationalString;
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Basic information required to uniquely identify a resource or resources.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlType(name = "MD_Identification", propOrder={
    "citation", "abstract", "purpose", "credits", "status", "pointOfContacts",
    "resourceMaintenance", "graphicOverviews", "resourceFormat", "descriptiveKeywords",
    "resourceSpecificUsages", "resourceConstraints"
})
@XmlSeeAlso({DataIdentificationImpl.class, ServiceIdentificationImpl.class})
@XmlRootElement(name = "MD_Identification")
public class IdentificationImpl extends MetadataEntity implements Identification {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -3715084806249419137L;

    /**
     * Citation data for the resource(s).
     */
    private Citation citation;

    /**
     * Brief narrative summary of the content of the resource(s).
     */
    private InternationalString abstracts;

    /**
     * Summary of the intentions with which the resource(s) was developed.
     */
    private InternationalString purpose;

    /**
     * Recognition of those who contributed to the resource(s).
     */
    private Collection<String> credits;

    /**
     * Status of the resource(s).
     */
    private Collection<Progress> status;

    /**
     * Identification of, and means of communication with, person(s) and organizations(s)
     * associated with the resource(s).
     */
    private Collection<ResponsibleParty> pointOfContacts;

    /**
     * Provides information about the frequency of resource updates, and the scope of those updates.
     */
    private Collection<MaintenanceInformation> resourceMaintenance;

    /**
     * Provides a graphic that illustrates the resource(s) (should include a legend for the graphic).
     */
    private Collection<BrowseGraphic> graphicOverviews;

    /**
     * Provides a description of the format of the resource(s).
     */
    private Collection<Format> resourceFormat;

    /**
     * Provides category keywords, their type, and reference source.
     */
    private Collection<Keywords> descriptiveKeywords;

    /**
     * Provides basic information about specific application(s) for which the resource(s)
     * has/have been or is being used by different users.
     */
    private Collection<Usage> resourceSpecificUsages;

    /**
     * Provides information about constraints which apply to the resource(s).
     */
    private Collection<Constraints> resourceConstraints;

    /**
     * Provides aggregate dataset information.
     */
    private Collection<AggregateInformation> aggregationInfo;

    /**
     * Constructs an initially empty identification.
     */
    public IdentificationImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public IdentificationImpl(final Identification source) {
        super(source);
    }

    /**
     * Creates an identification initialized to the specified values.
     */
    public IdentificationImpl(final Citation citation, final InternationalString abstracts) {
        setCitation(citation );
        setAbstract(abstracts);
    }

    /**
     * Citation data for the resource(s).
     */
    @XmlElement(name = "citation", required = true,
                namespace = "http://www.isotc211.org/2005/gmd")
    public Citation getCitation() {
        return citation;
    }

    /**
     * Set the citation data for the resource(s).
     */
    public synchronized void setCitation(final Citation newValue) {
        checkWritePermission();
        citation = newValue;
    }

    /**
     * Brief narrative summary of the content of the resource(s).
     */
    @XmlElement(name = "abstract", required = true,
                namespace = "http://www.isotc211.org/2005/gmd")
    public InternationalString getAbstract() {
        return abstracts;
    }

    /**
     * Set a brief narrative summary of the content of the resource(s).
     */
    public synchronized void setAbstract(final InternationalString newValue) {
        checkWritePermission();
        abstracts = newValue;
    }

    /**
     * Summary of the intentions with which the resource(s) was developed.
     */
    @XmlElement(name = "purpose", required = false,
                namespace = "http://www.isotc211.org/2005/gmd")
    public InternationalString getPurpose() {
        return purpose;
    }

    /**
     * Set a summary of the intentions with which the resource(s) was developed.
     */
    public synchronized void setPurpose(final InternationalString newValue) {
        checkWritePermission();
        purpose = newValue;
    }

    /**
     * Recognition of those who contributed to the resource(s).
     */
    @XmlElement(name = "credit", required = false,
                namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<String> getCredits() {
        return xmlOptional(credits = nonNullCollection(credits, String.class));
    }

    /**
     * Set a recognition of those who contributed to the resource(s).
     */
    public synchronized void setCredits(final Collection<? extends String> newValues) {
        credits = copyCollection(newValues, credits, String.class);
    }

    /**
     * Status of the resource(s).
     */
    @XmlElement(name = "status", required = false,
                namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<Progress> getStatus() {
        return xmlOptional(status = nonNullCollection(status, Progress.class));
    }

    /**
     * Set the status of the resource(s).
     */
    public synchronized void setStatus(final Collection<? extends Progress> newValues) {
        status = copyCollection(newValues, status, Progress.class);
    }

    /**
     * Identification of, and means of communication with, person(s) and organizations(s)
     * associated with the resource(s).
     */
    @XmlElement(name = "pointOfContact", required = false,
                namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<ResponsibleParty> getPointOfContacts() {
        return xmlOptional(pointOfContacts = nonNullCollection(pointOfContacts, ResponsibleParty.class));
    }

    /**
     * Set the point of contacts.
     */
    public synchronized void setPointOfContacts(
            final Collection<? extends ResponsibleParty> newValues)
    {
        pointOfContacts = copyCollection(newValues, pointOfContacts, ResponsibleParty.class);
    }

    /**
     * Provides information about the frequency of resource updates, and the scope of those updates.
     */
    @XmlElement(name = "resourceMaintenance", required = false,
                namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<MaintenanceInformation> getResourceMaintenance() {
        return xmlOptional(resourceMaintenance = nonNullCollection(resourceMaintenance,
                                                       MaintenanceInformation.class));
    }

    /**
     * Set information about the frequency of resource updates, and the scope of those updates.
     */
    public synchronized void setResourceMaintenance(
            final Collection<? extends MaintenanceInformation> newValues)
    {
        resourceMaintenance = copyCollection(newValues, resourceMaintenance,
                                             MaintenanceInformation.class);
    }

    /**
     * Provides a graphic that illustrates the resource(s) (should include a legend for the graphic).
     */
    @XmlElement(name = "graphicOverview", required = false,
                namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<BrowseGraphic> getGraphicOverviews() {
        return xmlOptional(graphicOverviews = nonNullCollection(graphicOverviews, BrowseGraphic.class));
    }

    /**
     * Set a graphic that illustrates the resource(s).
     */
    public synchronized void setGraphicOverviews(
            final Collection<? extends BrowseGraphic> newValues)
    {
        graphicOverviews = copyCollection(newValues, graphicOverviews, BrowseGraphic.class);
    }

    /**
     * Provides a description of the format of the resource(s).
     */
    @XmlElement(name = "resourceFormat", required = false,
                namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<Format> getResourceFormat() {
        return xmlOptional(resourceFormat = nonNullCollection(resourceFormat, Format.class));
    }

    /**
     * Set a description of the format of the resource(s).
     */
    public synchronized void setResourceFormat(final Collection<? extends Format> newValues) {
        resourceFormat = copyCollection(newValues, resourceFormat, Format.class);
    }

    /**
     * Provides category keywords, their type, and reference source.
     */
    @XmlElement(name = "descriptiveKeywords", required = false,
                namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<Keywords> getDescriptiveKeywords() {
        return xmlOptional(descriptiveKeywords = nonNullCollection(descriptiveKeywords, Keywords.class));
    }

    /**
     * Set category keywords, their type, and reference source.
     */
    public synchronized void setDescriptiveKeywords(
            final Collection<? extends Keywords> newValues)
    {
        descriptiveKeywords = copyCollection(newValues, descriptiveKeywords, Keywords.class);
    }

    /**
     * Provides basic information about specific application(s) for which the resource(s)
     * has/have been or is being used by different users.
     */
    @XmlElement(name = "resourceSpecificUsage", required = false, 
                namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<Usage> getResourceSpecificUsages() {
        return xmlOptional(resourceSpecificUsages = nonNullCollection(resourceSpecificUsages, Usage.class));
    }

    /**
     * Set basic information about specific application(s).
     */
    public synchronized void setResourceSpecificUsages(
            final Collection<? extends Usage> newValues)
    {
        resourceSpecificUsages = copyCollection(newValues, resourceSpecificUsages, Usage.class);
    }

    /**
     * Provides information about constraints which apply to the resource(s).
     */
    @XmlElement(name = "resourceConstraints", required = false,
                namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<Constraints> getResourceConstraints() {
        return xmlOptional(resourceConstraints = nonNullCollection(resourceConstraints, Constraints.class));
    }

    /**
     * Set information about constraints which apply to the resource(s).
     */
    public synchronized void setResourceConstraints(
            final Collection<? extends Constraints> newValues)
    {
        resourceConstraints = copyCollection(newValues, resourceConstraints, Constraints.class);
    }

    /**
     * Provides aggregate dataset information.
     *
     * @since 2.4
     */
    public synchronized Collection<AggregateInformation> getAggregationInfo() {
        return aggregationInfo = nonNullCollection(aggregationInfo, AggregateInformation.class);
    }

    /**
     * Sets aggregate dataset information.
     *
     * @since 2.4
     */
    public synchronized void setAggregationInfo(
            final Collection<? extends AggregateInformation> newValues)
    {
        aggregationInfo = copyCollection(newValues, aggregationInfo, AggregateInformation.class);
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
