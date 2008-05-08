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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opengis.metadata.FeatureTypeList;


/**
 * List of names of feature types with the same spatial representation (same as spatial attributes).
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 * @author Touraïvane
 *
 * @since 2.1
 */
@XmlType(propOrder={
    "spatialObject", "spatialSchemaName"
})
@XmlRootElement(name = "MD_FeatureTypeList")
public class FeatureTypeListImpl extends MetadataEntity implements FeatureTypeList {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5417914796207743856L;

    /**
     * Instance of a type defined in the spatial schema.
     */
    private String spatialObject;

    /**
     * Name of the spatial schema used.
     */
    private String spatialSchemaName;

    /**
     * Construct an initially empty feature type list.
     */
    public FeatureTypeListImpl() {
    }

    /**
     * Constructs a metadata entity initialized with the values from the specified metadata.
     *
     * @since 2.4
     */
    public FeatureTypeListImpl(final FeatureTypeList source) {
        super(source);
    }

    /**
     * Creates a feature type list initialized to the given values.
     */
    public FeatureTypeListImpl(final String spatialObject,
                               final String spatialSchemaName)
    {
        setSpatialObject    (spatialObject    );
        setSpatialSchemaName(spatialSchemaName);
    }

    /**
     * Instance of a type defined in the spatial schema.
     */
    @XmlElement(name = "spatialObject", required = true, namespace = "http://www.isotc211.org/2005/gmd")
    public String getSpatialObject() {
        return spatialObject;
    }

    /**
     * Set the instance of a type defined in the spatial schema.
     */
    public synchronized void setSpatialObject(final String newValue) {
        checkWritePermission();
        spatialObject = newValue;
    }

    /**
     * Name of the spatial schema used.
     */
    @XmlElement(name = "spatialSchemaName", required = true, namespace = "http://www.isotc211.org/2005/gmd")
    public String getSpatialSchemaName() {
        return spatialSchemaName;
    }

    /**
     * Set the name of the spatial schema used.
     */
    public synchronized void setSpatialSchemaName(final String newValue) {
        checkWritePermission();
        spatialSchemaName = newValue;
    }
}
