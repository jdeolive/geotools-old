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
package org.geotools.metadata.iso.lineage;

import java.util.Collection;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.identification.RepresentativeFraction;
import org.opengis.metadata.lineage.Source;
import org.opengis.metadata.lineage.ProcessStep;
import org.opengis.referencing.ReferenceSystem;
import org.opengis.util.InternationalString;
import org.geotools.metadata.iso.MetadataEntity;
import org.geotools.metadata.iso.identification.RepresentativeFractionImpl;


/**
 * Information about the source data used in creating the data specified by the scope.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlType(propOrder={
    "description", "scaleDenominator", "sourceCitation", "sourceExtents", "sourceSteps"
})
@XmlRootElement(name = "LI_Source")
public class SourceImpl extends MetadataEntity implements Source {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 2660914446466438044L;

    /**
     * Detailed description of the level of the source data.
     */
    private InternationalString description;

    /**
     * Denominator of the representative fraction on a source map.
     */
    private RepresentativeFraction scaleDenominator;

    /**
     * Spatial reference system used by the source data.
     */
    private ReferenceSystem sourceReferenceSystem;

    /**
     * Recommended reference to be used for the source data.
     */
    private Citation sourceCitation;

    /**
     * Information about the spatial, vertical and temporal extent of the source data.
     */
    private Collection<Extent> sourceExtents;

    /**
     * Information about an event in the creation process for the source data.
     */
    private Collection<ProcessStep> sourceSteps;

    /**
     * Creates an initially empty source.
     */
    public SourceImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public SourceImpl(final Source source) {
        super(source);
    }

    /**
     * Creates a source initialized with the given description.
     */
    public SourceImpl(final InternationalString description) {
        setDescription(description);
    }

    /**
     * Returns a detailed description of the level of the source data.
     */
    @XmlElement(name = "description", required = false)
    public InternationalString getDescription() {
        return description;
    }

    /**
     * Set a detailed description of the level of the source data.
     */
    public synchronized void setDescription(final InternationalString newValue) {
        checkWritePermission();
        description = newValue;
    }

    /**
     * Returns the denominator of the representative fraction on a source map.
     */
    @XmlElement(name = "scaleDenominator", required = false)
    public synchronized RepresentativeFraction getScaleDenominator()  {
        return scaleDenominator;
    }

    /**
     * Set the denominator of the representative fraction on a source map.
     *
     * @deprecated Use {@link #setScaleDenominator(RepresentativeFraction)}.
     */
    public void setScaleDenominator(final long newValue)  {
        setScaleDenominator(new RepresentativeFractionImpl(newValue));
    }

    /**
     * Set the denominator of the representative fraction on a source map.
     *
     * @since 2.4
     */
    public synchronized void setScaleDenominator(final RepresentativeFraction newValue)  {
        checkWritePermission();
        scaleDenominator = newValue;
    }

    /**
     * Returns the spatial reference system used by the source data.
     * 
     * @TODO: needs to annotate the referencing module before.
     */
    public ReferenceSystem getSourceReferenceSystem()  {
        return sourceReferenceSystem;
    }

    /**
     * Set the spatial reference system used by the source data.
     */
    public synchronized void setSourceReferenceSystem(final ReferenceSystem newValue) {
        checkWritePermission();
        sourceReferenceSystem = newValue;
    }

    /**
     * Returns the recommended reference to be used for the source data.
     */
    @XmlElement(name = "sourceCitation", required = false)
    public Citation getSourceCitation() {
        return sourceCitation;
    }

    /**
     * Set the recommended reference to be used for the source data.
     */
    public synchronized void setSourceCitation(final Citation newValue) {
        checkWritePermission();
        sourceCitation = newValue;
    }

    /**
     * Returns tiInformation about the spatial, vertical and temporal extent
     * of the source data.
     */
    @XmlElement(name = "sourceExtent", required = false)
    public synchronized Collection<Extent> getSourceExtents()  {
        return xmlOptional(sourceExtents = nonNullCollection(sourceExtents, Extent.class));
    }

    /**
     * Information about the spatial, vertical and temporal extent of the source data.
     */
    public synchronized void setSourceExtents(final Collection<? extends Extent> newValues) {
        sourceExtents = copyCollection(newValues, sourceExtents, Extent.class);
    }

    /**
     * Returns information about an event in the creation process for the source data.
     */
    @XmlElement(name = "sourceStep", required = false)
    public synchronized Collection<ProcessStep> getSourceSteps() {
        return xmlOptional(sourceSteps = nonNullCollection(sourceSteps, ProcessStep.class));
    }

    /**
     * Set information about an event in the creation process for the source data.
     */
    public synchronized void setSourceSteps(final Collection<? extends ProcessStep> newValues) {
        sourceSteps = copyCollection(newValues, sourceSteps, ProcessStep.class);
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
