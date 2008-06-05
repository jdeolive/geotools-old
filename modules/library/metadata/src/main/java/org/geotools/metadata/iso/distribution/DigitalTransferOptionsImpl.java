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
package org.geotools.metadata.iso.distribution;

import java.util.Collection;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.citation.OnLineResource;
import org.opengis.metadata.distribution.DigitalTransferOptions;
import org.opengis.metadata.distribution.Medium;
import org.opengis.util.InternationalString;
import org.geotools.metadata.iso.MetadataEntity;


/**
 * Technical means and media by which a resource is obtained from the distributor.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlType(propOrder={
    "unitsOfDistribution", "transferSize", "onLines", "offLine"
})
@XmlRootElement(name = "MD_DigitalTransferOptions")
public class DigitalTransferOptionsImpl extends MetadataEntity implements DigitalTransferOptions {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1533064478468754337L;

    /**
     * Tiles, layers, geographic areas, etc., in which data is available.
     */
    private InternationalString unitsOfDistribution;

    /**
     * Estimated size of a unit in the specified transfer format, expressed in megabytes.
     * The transfer size is &gt; 0.0.
     * Returns {@code null} if the transfer size is unknown.
     */
    private Double transferSize;

    /**
     * Information about online sources from which the resource can be obtained.
     */
    private Collection<OnLineResource> onLines;

    /**
     * Information about offline media on which the resource can be obtained.
     */
    private Medium offLines;

    /**
     * Constructs an initially empty digital transfer options.
     */
    public DigitalTransferOptionsImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public DigitalTransferOptionsImpl(final DigitalTransferOptions source) {
        super(source);
    }

    /**
     * Returne tiles, layers, geographic areas, etc., in which data is available.
     */
    @XmlElement(name = "unitsOfDistribution", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public InternationalString getUnitsOfDistribution() {
        return unitsOfDistribution;
    }

    /**
     * Set tiles, layers, geographic areas, etc., in which data is available.
     */
    public synchronized void setUnitsOfDistribution(final InternationalString newValue) {
        checkWritePermission();
        unitsOfDistribution = newValue;
    }

    /**
     * Returns an estimated size of a unit in the specified transfer format, expressed in megabytes.
     * The transfer size is &gt; 0.0.
     * Returns {@code null} if the transfer size is unknown.
     */
    @XmlElement(name = "transferSize", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public Double getTransferSize() {
        return transferSize;
    }

    /**
     * Set an estimated size of a unit in the specified transfer format, expressed in megabytes.
     * The transfer size is &gt; 0.0.
     */
    public synchronized void setTransferSize(final Double newValue) {
        checkWritePermission();
        transferSize = newValue;
    }

    /**
     * Returns information about online sources from which the resource can be obtained.
     */
    @XmlElement(name = "onLine", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<OnLineResource> getOnLines() {
        return xmlOptional(onLines = nonNullCollection(onLines, OnLineResource.class));
    }

    /**
     * Set information about online sources from which the resource can be obtained.
     */
    public synchronized void setOnLines(final Collection<? extends OnLineResource> newValues) {
        onLines = copyCollection(newValues, onLines, OnLineResource.class);
    }

    /**
     * Returns information about offline media on which the resource can be obtained.
     */
    @XmlElement(name = "offLine", required = false, namespace = "http://www.isotc211.org/2005/gmd")
    public Medium getOffLine() {
        return offLines;
    }

    /**
     * Set information about offline media on which the resource can be obtained.
     */
    public synchronized void setOffLine(final Medium newValue) {
        checkWritePermission();
        offLines = newValue;
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
