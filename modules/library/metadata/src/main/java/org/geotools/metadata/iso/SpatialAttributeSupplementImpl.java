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
package org.geotools.metadata.iso;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.metadata.FeatureTypeList;
import org.opengis.metadata.SpatialAttributeSupplement;


/**
 * Spatial attributes in the application schema for the feature types.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlRootElement(name = "MD_SpatialAttributeSupplement")
public class SpatialAttributeSupplementImpl extends MetadataEntity
        implements SpatialAttributeSupplement
{
    /**
     * Serial number for compatibility with different versions.
     */
    private static final long serialVersionUID = 273337004694210422L;

    /**
     * Provides information about the list of feature types with the same spatial representation.
     */
    private Collection<FeatureTypeList> featureTypeList;

    /**
     * Construct an initially empty spatial attribute supplement.
     */
    public SpatialAttributeSupplementImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public SpatialAttributeSupplementImpl(final SpatialAttributeSupplement source) {
        super(source);
    }

    /**
     * Creates a spatial attribute supplement initialized to the given values.
     */
    public SpatialAttributeSupplementImpl(final Collection featureTypeList) {
        setFeatureTypeList(featureTypeList);
    }

    /**
     * Provides information about the list of feature types with the same spatial representation.
     */
    @XmlElement(name = "featureTypeList", required = true, namespace = "http://www.isotc211.org/2005/gmd")
    public synchronized Collection<FeatureTypeList> getFeatureTypeList() {
        return featureTypeList = nonNullCollection(featureTypeList, FeatureTypeList.class);
    }

    /**
     * Set information about the list of feature types with the same spatial representation.
     */
    public synchronized void setFeatureTypeList(
            final Collection<? extends FeatureTypeList> newValues)
    {
        featureTypeList = copyCollection(newValues, featureTypeList, FeatureTypeList.class);
    }
}
