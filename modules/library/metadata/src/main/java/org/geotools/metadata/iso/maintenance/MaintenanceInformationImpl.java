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
package org.geotools.metadata.iso.maintenance;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.maintenance.MaintenanceInformation;
import org.opengis.metadata.maintenance.MaintenanceFrequency;
import org.opengis.metadata.maintenance.ScopeCode;
import org.opengis.metadata.maintenance.ScopeDescription;
import org.opengis.temporal.PeriodDuration;
import org.opengis.util.InternationalString;
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Information about the scope and frequency of updating.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlType(propOrder={
    "maintenanceAndUpdateFrequency", "dateOfNextUpdate",
    "updateScopes", "updateScopeDescriptions", "maintenanceNotes", "contacts"
})
@XmlRootElement(name = "MD_MaintenanceInformation")
public class MaintenanceInformationImpl extends MetadataEntity implements MaintenanceInformation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8523463344581266776L;

    /**
     * Frequency with which changes and additions are made to the resource after the
     * initial resource is completed.
     */
    private MaintenanceFrequency maintenanceAndUpdateFrequency;

    /**
     * Scheduled revision date for resource, in milliseconds ellapsed
     * since January 1st, 1970. If there is no such date, then this field
     * is set to the special value {@link Long#MIN_VALUE}.
     */
    private long dateOfNextUpdate = Long.MIN_VALUE;

    /**
     * Maintenance period other than those defined, in milliseconds.
     */
    private PeriodDuration userDefinedMaintenanceFrequency;

    /**
     * Scope of data to which maintenance is applied.
     */
    private Collection<ScopeCode> updateScopes;

    /**
     * Additional information about the range or extent of the resource.
     */
    private Collection<ScopeDescription> updateScopeDescriptions;

    /**
     * Information regarding specific requirements for maintaining the resource.
     */
    private Collection<InternationalString> maintenanceNotes;

    /**
     * Identification of, and means of communicating with,
     * person(s) and organization(s) with responsibility for maintaining the metadata
     */
    private Collection<ResponsibleParty> contacts;

    /**
     * Creates a an initially empty maintenance information.
     */
    public MaintenanceInformationImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public MaintenanceInformationImpl(final MaintenanceInformation source) {
        super(source);
    }

    /**
     * Creates a maintenance information.
     */
    public MaintenanceInformationImpl(final MaintenanceFrequency maintenanceAndUpdateFrequency) {
        setMaintenanceAndUpdateFrequency(maintenanceAndUpdateFrequency);
    }

    /**
     * Returns the frequency with which changes and additions are made to the resource
     * after the initial resource is completed.
     */
    @XmlElement(name = "maintenanceAndUpdateFrequency", required = true)
    public MaintenanceFrequency getMaintenanceAndUpdateFrequency() {
        return maintenanceAndUpdateFrequency;
    }

    /**
     * Set the frequency with which changes and additions are made to the resource
     * after the initial resource is completed.
     */
    public synchronized void setMaintenanceAndUpdateFrequency(final MaintenanceFrequency newValue) {
        checkWritePermission();
        maintenanceAndUpdateFrequency = newValue;
    }

    /**
     * Returns the scheduled revision date for resource.
     */
    @XmlElement(name = "dateOfNextUpdate", required = false)
    public synchronized Date getDateOfNextUpdate() {
        return (dateOfNextUpdate!=Long.MIN_VALUE) ? new Date(dateOfNextUpdate) : null;
    }

    /**
     * Set the scheduled revision date for resource.
     */
    public synchronized void setDateOfNextUpdate(final Date newValue) {
        checkWritePermission();
        dateOfNextUpdate = (newValue!=null) ? newValue.getTime() : Long.MIN_VALUE;
    }

    /**
     * Returns the maintenance period other than those defined.
     *
     * @return The period, in milliseconds.
     * 
     * @TODO: needs an implementation of org.opengis.temporal modules to anntote
     *        this parameter.
     */
    public PeriodDuration getUserDefinedMaintenanceFrequency() {
        return userDefinedMaintenanceFrequency;
    }

    /**
     * Set the maintenance period other than those defined.
     */
    public synchronized void setUserDefinedMaintenanceFrequency(final PeriodDuration newValue) {
        checkWritePermission();
        userDefinedMaintenanceFrequency = newValue;
    }

    /**
     * Scope of data to which maintenance is applied.
     *
     * @deprecated Replaced by {@link #getUpdateScopes}.
     */
    public ScopeCode getUpdateScope() {
        final Collection updateScopes = getUpdateScopes();
        return updateScopes.isEmpty() ? null : (ScopeCode) updateScopes.iterator().next();
    }

    /**
     * Scope of data to which maintenance is applied.
     *
     * @deprecated Replaced by {@link #setUpdateScopes}.
     */
    public void setUpdateScope(final ScopeCode newValue) {
        setUpdateScopes(Collections.singleton(newValue));
    }

    /**
     * Returns the scope of data to which maintenance is applied.
     *
     * @since 2.4
     */
    @XmlElement(name = "updateScope", required = false)
    public synchronized Collection<ScopeCode> getUpdateScopes() {
        return xmlOptional(updateScopes = nonNullCollection(updateScopes, ScopeCode.class));
    }

    /**
     * Set the scope of data to which maintenance is applied.
     *
     * @since 2.4
     */
    public synchronized void setUpdateScopes(final Collection<? extends ScopeCode> newValues) {
        updateScopes = copyCollection(newValues, updateScopes, ScopeCode.class);
    }

    /**
     * Additional information about the range or extent of the resource.
     *
     * @deprecated Replaced by {@link #getUpdateScopeDescriptions}.
     */
    public ScopeDescription getUpdateScopeDescription() {
        final Collection<ScopeDescription> updateScopeDescriptions = getUpdateScopeDescriptions();
        return updateScopeDescriptions.isEmpty() ? null : updateScopeDescriptions.iterator().next();
    }

    /**
     * Additional information about the range or extent of the resource.
     *
     * @deprecated Replaced by {@link #setUpdateScopeDescriptions}.
     */
    public void setUpdateScopeDescription(final ScopeDescription newValue) {
        setUpdateScopeDescriptions(Collections.singleton(newValue));
    }

    /**
     * Returns additional information about the range or extent of the resource.
     *
     * @since 2.4
     */
    @XmlElement(name = "updateScopeDescription", required = false)
    public synchronized Collection<ScopeDescription> getUpdateScopeDescriptions() {
        return xmlOptional(updateScopeDescriptions = nonNullCollection(updateScopeDescriptions, ScopeDescription.class));
    }

    /**
     * Set additional information about the range or extent of the resource.
     *
     * @since 2.4
     */
    public synchronized void setUpdateScopeDescriptions(
            final Collection<? extends ScopeDescription> newValues)
    {
        updateScopeDescriptions = copyCollection(newValues, updateScopeDescriptions, ScopeDescription.class);
    }

    /**
     * Information regarding specific requirements for maintaining the resource.
     *
     * @deprecated Replaced by {@link #getMaintenanceNotes}.
     */
    public InternationalString getMaintenanceNote() {
        final Collection<InternationalString> maintenanceNotes = getMaintenanceNotes();
        return maintenanceNotes.isEmpty() ? null : maintenanceNotes.iterator().next();
    }

    /**
     * Information regarding specific requirements for maintaining the resource.
     *
     * @deprecated Replaced by {@link #setMaintenanceNotes}.
     */
    public void setMaintenanceNote(final InternationalString newValue) {
        setMaintenanceNotes(Collections.singleton(newValue));
    }

    /**
     * Returns information regarding specific requirements for maintaining the resource.
     *
     * @since 2.4
     */
    @XmlElement(name = "maintenanceNote", required = false)
    public synchronized Collection<InternationalString> getMaintenanceNotes() {
        return xmlOptional(maintenanceNotes = nonNullCollection(maintenanceNotes, InternationalString.class));
    }

    /**
     * Set information regarding specific requirements for maintaining the resource.
     *
     * @since 2.4
     */
    public synchronized void setMaintenanceNotes(
            final Collection<? extends InternationalString> newValues)
    {
        maintenanceNotes = copyCollection(newValues, maintenanceNotes, InternationalString.class);
    }

    /**
     * Returns identification of, and means of communicating with,
     * person(s) and organization(s) with responsibility for maintaining the metadata.
     *
     * @since 2.4
     */
    @XmlElement(name = "contact", required = false)
    public synchronized Collection<ResponsibleParty> getContacts() {
        return xmlOptional(contacts = nonNullCollection(contacts, ResponsibleParty.class));
    }

    /**
     * Set identification of, and means of communicating with,
     * person(s) and organization(s) with responsibility for maintaining the metadata.
     *
     * @since 2.4
     */
    public synchronized void setContacts(final Collection<? extends ResponsibleParty> newValues) {
        contacts = copyCollection(newValues, contacts, ResponsibleParty.class);
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
