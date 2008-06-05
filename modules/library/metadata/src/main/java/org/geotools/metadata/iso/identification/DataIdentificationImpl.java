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
package org.geotools.metadata.iso.identification;

import java.util.Collection;
import java.util.Locale;
import java.nio.charset.Charset;

import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.identification.DataIdentification;
import org.opengis.metadata.extent.Extent;
import org.opengis.metadata.extent.GeographicBoundingBox;
import org.opengis.metadata.extent.GeographicDescription;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.Resolution;
import org.opengis.metadata.identification.TopicCategory;
import org.opengis.metadata.spatial.SpatialRepresentationType;
import org.opengis.util.InternationalString;


/**
 * Information required to identify a dataset.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlRootElement(name = "MD_DataIdentification")
@XmlType(name = "MD_DataIdentification", propOrder={"spatialRepresentationTypes", "spatialResolutions", "language", "characterSets", 
                    "topicCategories", "environmentDescription", "extent", "supplementalInformation"})
public class DataIdentificationImpl extends IdentificationImpl implements DataIdentification {
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = -4418520352804939785L;

    /**
     * Method used to spatially represent geographic information.
     */
    private Collection<SpatialRepresentationType> spatialRepresentationTypes;

    /**
     * Factor which provides a general understanding of the density of spatial data
     * in the dataset.
     */
    private Collection<Resolution> spatialResolutions;

    /**
     * Language(s) used within the dataset.
     */
    private Collection<Locale> language;

    /**
     * Full name of the character coding standard used for the dataset.
     */
    private Collection<CharacterSet> characterSets;

    /**
     * Main theme(s) of the datset.
     */
    private Collection<TopicCategory> topicCategories;

    /**
     * Minimum bounding rectangle within which data is available.
     * Only one of {@code getGeographicBox()} and {@link #getGeographicDescription()}
     * should be provided.
     *
     * @deprecated not in ISO 19115:2003
     */
    private Collection<GeographicBoundingBox> geographicBox;

    /**
     * Description of the geographic area within which data is available.
     * Only one of {@link #getGeographicBox()} and {@code getGeographicDescription()}
     * should be provided.
     *
     * @deprecated not in ISO 19115:2003
     */
    private Collection<GeographicDescription> geographicDescription;

    /**
     * Description of the dataset in the producers processing environment, including items
     * such as the software, the computer operating system, file name, and the dataset size
     */
    private InternationalString environmentDescription;

    /**
     * Additional extent information including the bounding polygon, vertical, and temporal
     * extent of the dataset.
     */
    private Collection<Extent> extent;

    /**
     * Any other descriptive information about the dataset.
     */
    private InternationalString supplementalInformation;

    /**
     * Constructs an initially empty data identification.
     */
    public DataIdentificationImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public DataIdentificationImpl(final DataIdentification source) {
        super(source);
    }

    /**
     * Creates a data identification initialized to the specified values.
     */
    public DataIdentificationImpl(final Citation citation,
                                  final InternationalString abstracts,
                                  final Collection<? extends Locale> language,
                                  final Collection<? extends TopicCategory> topicCategories)
    {
        super(citation, abstracts);
        setLanguage       (language       );
        setTopicCategories(topicCategories);
    }

    /**
     * Method used to spatially represent geographic information.
     */
    @XmlElement(name = "spatialRepresentationType", required = false)
    public synchronized Collection<SpatialRepresentationType> getSpatialRepresentationTypes() {
        return xmlOptional(spatialRepresentationTypes = nonNullCollection(spatialRepresentationTypes,
                                                              SpatialRepresentationType.class));
    }

    /**
     * Set the method used to spatially represent geographic information.
     */
    public synchronized void setSpatialRepresentationTypes(
            final Collection<? extends SpatialRepresentationType> newValues)
    {
        spatialRepresentationTypes = copyCollection(newValues, spatialRepresentationTypes,
                                                    SpatialRepresentationType.class);
    }

    /**
     * Factor which provides a general understanding of the density of spatial data
     * in the dataset.
     */
    @XmlElement(name = "spatialResolution", required = false)
    public synchronized Collection<Resolution> getSpatialResolutions() {
        return xmlOptional(spatialResolutions = nonNullCollection(spatialResolutions, Resolution.class));
    }

    /**
     * Set the factor which provides a general understanding of the density of spatial data
     * in the dataset.
     */
    public synchronized void setSpatialResolutions(
            final Collection<? extends Resolution> newValues)
    {
        spatialResolutions = copyCollection(newValues, spatialResolutions, Resolution.class);
    }

    /**
     * Language(s) used within the dataset.
     */
    @XmlElement(name = "language", required = true)
    public synchronized Collection<Locale> getLanguage() {
        return language = nonNullCollection(language, Locale.class);
    }

    /**
     * Set the language(s) used within the dataset.
     */
    public synchronized void setLanguage(final Collection<? extends Locale> newValues)  {
        language = copyCollection(newValues, language, Locale.class);
    }

    /**
     * Full name of the character coding standard used for the dataset.
     *
     * @deprecated Use {@link #getCharacterSets} instead.
     */
    public Charset getCharacterSet() {
        final Collection<CharacterSet> characterSet = getCharacterSets();
        return characterSet.isEmpty() ? null : characterSet.iterator().next().toCharset();
    }

    /**
     * Full name of the character coding standard used for the dataset.
     */
    @XmlElement(name = "characterSet", required = false)
    public synchronized Collection<CharacterSet> getCharacterSets() {
        return xmlOptional(characterSets = nonNullCollection(characterSets, CharacterSet.class));
    }

    /**
     * Set the full name of the character coding standard used for the dataset.
     *
     * @deprecated Use {@link #setCharacterSets} instead.
     */
    public synchronized void setCharacterSet(final Charset newValue) {
        throw new UnsupportedOperationException();
    }

    /**
     * Set the full name of the character coding standard used for the dataset.
     */
    public synchronized void setCharacterSets(final Collection<? extends CharacterSet> newValues) {
        characterSets = copyCollection(newValues, characterSets, CharacterSet.class);
    }

    /**
     * Main theme(s) of the datset.
     */
    @XmlElement(name = "topicCategory", required = false)
    public synchronized Collection<TopicCategory> getTopicCategories()  {
        return xmlOptional(topicCategories = nonNullCollection(topicCategories, TopicCategory.class));
    }

    /**
     * Set the main theme(s) of the datset.
     */
    public synchronized void setTopicCategories(
            final Collection<? extends TopicCategory> newValues)
    {
        topicCategories = copyCollection(newValues, topicCategories, TopicCategory.class);
    }

    /**
     * Minimum bounding rectangle within which data is available.
     * Only one of {@code getGeographicBox()} and {@link #getGeographicDescription()}
     * should be provided.
     *
     * @deprecated not in ISO 19115:2003
     */
    public synchronized Collection<GeographicBoundingBox> getGeographicBox() {
        return geographicBox = nonNullCollection(geographicBox, GeographicBoundingBox.class);
    }

    /**
     * Set the minimum bounding rectangle within which data is available.
     *
     * @deprecated not in ISO 19115:2003
     */
    public synchronized void setGeographicBox(
            final Collection<? extends GeographicBoundingBox> newValues)
    {
        geographicBox = copyCollection(newValues, geographicBox, GeographicBoundingBox.class);
    }

    /**
     * Description of the geographic area within which data is available.
     * Only one of {@link #getGeographicBox()} and {@code getGeographicDescription()}
     * should be provided.
     *
     * @deprecated not in ISO 19115:2003
     */
    public synchronized Collection<GeographicDescription> getGeographicDescription() {
        return geographicDescription = nonNullCollection(geographicDescription,
                                                         GeographicDescription.class);
    }

    /**
     * Set the description of the geographic area within which data is available.
     *
     * @deprecated not in ISO 19115:2003
     */
    public synchronized void setGeographicDescription(
            final Collection<? extends GeographicDescription> newValues)
    {
        geographicDescription = copyCollection(newValues, geographicDescription,
                                               GeographicDescription.class);
    }

    /**
     * Description of the dataset in the producers processing environment, including items
     * such as the software, the computer operating system, file name, and the dataset size.
     */
    @XmlElement(name = "environmentDescription", required = false)
    public InternationalString getEnvironmentDescription() {
        return environmentDescription;
    }

    /**
     * Set the description of the dataset in the producers processing environment.
     */
    public synchronized void setEnvironmentDescription(final InternationalString newValue)  {
        checkWritePermission();
        environmentDescription = newValue;
    }

    /**
     * Additional extent information including the bounding polygon, vertical, and temporal
     * extent of the dataset.
     */
    @XmlElement(name = "extent", required = false)
    public synchronized Collection<Extent> getExtent() {
        return xmlOptional(extent = nonNullCollection(extent, Extent.class));
    }

    /**
     * Set additional extent information.
     */
    public synchronized void setExtent(final Collection<? extends Extent> newValues) {
        extent = copyCollection(newValues, extent, Extent.class);
    }

    /**
     * Any other descriptive information about the dataset.
     */
    @XmlElement(name = "supplementalInformation", required = false)
    public InternationalString getSupplementalInformation() {
        return supplementalInformation;
    }

    /**
     * Set any other descriptive information about the dataset.
     */
    public synchronized void setSupplementalInformation(final InternationalString newValue) {
        checkWritePermission();
        supplementalInformation = newValue;
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
///   }
}
