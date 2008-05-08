/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.vpf.readers;
import java.io.IOException;
import java.sql.SQLException;

import org.geotools.data.vpf.VPFFeatureType;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * DOCUMENT ME!
 *
 * @author <a href="mailto:jeff@ionicenterprise.com">Jeff Yutzler</a>
 * @source $URL$
 */
public abstract class VPFGeometryFactory {
	private static final GeometryFactory m_GeometryFactory = new GeometryFactory();
    /**
     * Constructs a geometry for the appropriate feature type based on values currently on the object,
     * retrieving values as needed from the various VPFFile objects
     * @param featureType the VPFFeatureType to use
     * @param values the current feature
     * @throws SQLException
     * @throws IOException
     * @throws IllegalAttributeException
     */
	public abstract void createGeometry(VPFFeatureType featureType, SimpleFeature values) throws SQLException, IOException, IllegalAttributeException;
}
