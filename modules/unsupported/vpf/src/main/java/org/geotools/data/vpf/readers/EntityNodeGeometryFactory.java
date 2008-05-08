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
 *
 *    Created on Jul 19, 2004
 */
package org.geotools.data.vpf.readers;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import org.geotools.data.vpf.VPFFeatureType;
import org.geotools.data.vpf.file.VPFFile;
import org.geotools.data.vpf.file.VPFFileFactory;
import org.geotools.data.vpf.ifc.FileConstants;
import org.geotools.feature.IllegalAttributeException;
import org.opengis.feature.simple.SimpleFeature;

import com.vividsolutions.jts.geom.Geometry;

/**
 * DOCUMENT ME!
 *
 * @author <a href="mailto:jeff@ionicenterprise.com">Jeff Yutzler</a>
 * @source $URL$
 */
public class EntityNodeGeometryFactory extends VPFGeometryFactory implements FileConstants {
    /* (non-Javadoc)
     * @see com.ionicsoft.wfs.jdbc.geojdbc.module.vpf.VPFGeometryFactory#createGeometry(com.ionicsoft.wfs.jdbc.geojdbc.module.vpf.VPFIterator)
     */
    public void createGeometry(VPFFeatureType featureType, SimpleFeature values) throws SQLException, IOException, IllegalAttributeException{
        Geometry result = null;
        int nodeId = ((Number)values.getAttribute("end_id")).intValue();
//        VPFFeatureType featureType = (VPFFeatureType)values.getFeatureType();
        
        // Get the right edge table
    	String baseDirectory = featureType.getFeatureClass().getDirectoryName();
    	String tileDirectory = baseDirectory;

    	// If the primitive table is there, this coverage is not tiled
        if(!new File(tileDirectory.concat(File.separator).concat(ENTITY_NODE_PRIMITIVE)).exists()){
    		Short tileId = new Short(Short.parseShort(values.getAttribute("tile_id").toString()));
            tileDirectory = tileDirectory.concat(File.separator).concat(featureType.getFeatureClass().getCoverage().getLibrary().getTileMap().get(tileId).toString()).trim();
        }

        String nodeTableName = tileDirectory.concat(File.separator).concat(ENTITY_NODE_PRIMITIVE);
        VPFFile nodeFile = VPFFileFactory.getInstance().getFile(nodeTableName);
        SimpleFeature row = nodeFile.getRowFromId("id", nodeId);
        result = (Geometry)row.getAttribute("coordinate");
        values.setDefaultGeometry(result);
    }
}
