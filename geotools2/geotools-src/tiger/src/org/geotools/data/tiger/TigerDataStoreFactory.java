/*
 *    GeoLBS - OpenSource Location Based Servces toolkit
 *    Copyright (C) 2003-2004, Julian J. Ray, All Rights Reserved
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

package org.geotools.data.tiger;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import java.io.File;
import java.io.IOException;
import java.util.Map;


/**
 * <p>
 * Title: GeoTools2 Development
 * </p>
 * 
 * <p>
 * Description:
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 *
 * @author Julian J. Ray
 * @version 1.0
 */
public class TigerDataStoreFactory implements DataStoreFactorySpi {
    /**
     * Creates a new TigerDataStoreFactory object.
     */
    public TigerDataStoreFactory() {
    }

    /**
     * createDataStore
     *
     * @param params Map
     *
     * @return DataStore
     *
     * @throws IOException
     */
    public DataStore createDataStore(Map params) throws IOException {
        String dirName = (String) params.get("directory");

        return new TigerDataStore(dirName);
    }

    /**
     * createNewDataStore
     *
     * @param params Map
     *
     * @return DataStore
     *
     * @throws IOException
     */
    public DataStore createNewDataStore(Map params) throws IOException {
        String dirName = (String) params.get("directory");

        File dir = new File(dirName);

        if (dir.exists()) {
            throw new IOException("Can't create new data store: " + dir + " already exists!");
        }

        boolean created;
        created = dir.mkdir();

        if (! created) {
            throw new IOException("Can't create directory: " + dir);
        }

        return new TigerDataStore(dirName);
    }

    /**
     * getDescription
     *
     * @return String
     */
    public String getDescription() {
        return "Data Store for TIGER/Line 2002 Line files.";
    }

    /**
     * getParametersInfo
     *
     * @return Param[]
     */
    public Param[] getParametersInfo() {
        Param dir = new Param("directory", String.class, "Directory containing TIGER/Line 2002 files.");

        return new Param[] { dir };
    }

    /**
     * canProcess
     *
     * @param params Map
     *
     * @return boolean
     */
    public boolean canProcess(Map params) {
        return (params != null) && params.containsKey("directory") && params.get("directory") instanceof String;
    }
}
