/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.shapefile;

import org.geotools.data.*;
import java.util.logging.Logger;


/**
 * Provides information about the ShapefileDataSource's capabilities.
 *
 * @author Chris Holmes, TOPP
 * @version $Id: ShapefileMetaData.java,v 1.1 2003/05/05 22:08:33 cholmesny Exp $
 */
public class ShapefileMetaData implements DataSourceMetaData {
    /** Standard logging instance */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.defaultcore");

    public ShapefileMetaData() {
    }

    /**
     * Retrieves whether this datasource supports basic transactions:
     * updateFeatures, modifyFeatures, and removeFeatures.
     *
     * @return true if so, false otherwise.
     */
    public boolean supportsTransactions() {
        return supportsInsert() && supportsUpdate() && supportsDelete();
    }

    public boolean supportsInsert() {
        return false;
    }

    public boolean supportsUpdate() {
        return false;
    }

    public boolean supportsDelete() {
        return false;
    }

    /**
     * Retrieves whether this datasource supports multi transaction operations:
     * startMultiTransaction and endMultiTransaction
     *
     * @return false, not currently supported.
     */
    public boolean supportsMultiTransactions() {
        return false;
    }

    /**
     * Retrieves whether this datasource supports multi the setFeatures
     * operation.
     *
     * @return false, not currently supported.
     */
    public boolean supportsSetFeatures() {
        return true;
    }

    /**
     * Retrives whether this datasource supports the setSchema operation.
     *
     * @return true, the schema can be set.
     */
    public boolean supportsSetSchema() {
        return true;
    }

    /**
     * Retrives whether this datasource supports the abortLoading operation.
     *
     * @return false, not currently supported.
     */
    public boolean supportsAbort() {
        return false;
    }

    /**
     * Retrives whether this datasource returns meaningful results when getBBox
     * is called.
     *
     * @return true, shapefile can get the bbox.
     */
    public boolean supportsGetBbox() {
        return true;
    }

    /**
     * Returns true is the computation for the bounding box of this datasource
     * is relatively fast.  
     * @return true, shapefile's bbox computation is fast.
     */
    public boolean fastBbox(){
	return true;
    }
}
