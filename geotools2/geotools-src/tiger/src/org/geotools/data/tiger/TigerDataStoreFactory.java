/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
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
            throw new IOException("Can't create new data store: " + dir
                + " already exists!");
        }

        boolean created;
        created = dir.mkdir();

        if (!created) {
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
        Param dir = new Param("directory", String.class,
                "Directory containing TIGER/Line 2002 files.");

        return new Param[] { dir };
    }

    /**
     * Test to see if this datastore is available, if it has all the
     * appropriate libraries to construct a datastore.  This datastore just
     * returns true for now.  This method is used for gui apps, so as to not
     * advertise data store capabilities they don't actually have.
     *
     * @return <tt>true</tt> if and only if this factory is available to create
     *         DataStores.
     *
     * @task REVISIT: I'm just adding this method to compile, maintainer should
     *       revisit to check for any libraries that may be necessary for
     *       datastore creations. ch.
     */
    public boolean isAvailable() {
        return true;
    }

    /**
     * canProcess
     *
     * @param params Map
     *
     * @return boolean
     */
    public boolean canProcess(Map params) {
        return (params != null) && params.containsKey("directory")
        && params.get("directory") instanceof String;
    }
}
