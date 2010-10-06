package org.geotools.data.excel;

/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2010, Open Source Geospatial Foundation (OSGeo)
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
 */
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.AbstractDataStoreFactory;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.util.logging.Logging;

public class ExcelDataStoreFactory extends AbstractDataStoreFactory implements DataStoreFactorySpi {
    private static final Logger logger = Logging
            .getLogger("org.geotools.excel.datastore.ExcelDataStoreFactory");

    static HashSet<Param> params = new HashSet<DataAccessFactory.Param>();

    public static final Param TYPE = new Param("type", String.class, "Type", true, "excel");

    public static final Param FILENAME = new Param("filename", String.class,
            "The name of the file containing the data", true);

    public static final Param SHEETNAME = new Param("sheet", String.class, "name of the sheet",
            true);

    public static final Param LATCOL = new Param("latcol", Integer.class,
            "Column index of Latitude or X value", true);

    public static final Param LONGCOL = new Param("longcol", Integer.class,
            "Column index of Longitude or Y value", true);

    public static final Param PROJECTION = new Param("projection", String.class,
            "EPSG code of projection", true);

    public static final Param HEADERROW = new Param("headerrow", Integer.class,
            "Row index for header row (default 0)", false);

    public String getDisplayName() {
        // TODO Auto-generated method stub
        return "Excel DataStore";
    }

    public String getDescription() {
        // TODO Auto-generated method stub
        return "A Datastore backed by an Excel Workbook";
    }

    public boolean canProcess(Map params) {
        if (!super.canProcess(params)) {

            return false; // was not in agreement with getParametersInfo
        }

        File file = new File(params.get(FILENAME.key).toString());
        return file.exists();
    }

    public final Param[] getParametersInfo() {
        LinkedHashMap map = new LinkedHashMap();
        setupParameters(map);

        return (Param[]) map.values().toArray(new Param[map.size()]);
    }

    void setupParameters(LinkedHashMap map) {
        map.put(FILENAME.key, FILENAME);
        map.put(HEADERROW.key, HEADERROW);
        map.put(LATCOL.key, LATCOL);
        map.put(LONGCOL.key, LONGCOL);
        map.put(SHEETNAME.key, SHEETNAME);
        map.put(PROJECTION.key, PROJECTION);
    }

    public boolean isAvailable() {

        return true;
    }

    public DataStore createDataStore(Map<String, Serializable> params) throws IOException {
        String file = (String) FILENAME.lookUp(params);
        String sheet = (String) SHEETNAME.lookUp(params);
        int headerRow = 0;
        if (params.containsKey(HEADERROW.key)) {
            headerRow = ((Integer) HEADERROW.lookUp(params)).intValue();
        }
        int latCol = ((Integer) LATCOL.lookUp(params)).intValue();
        int longCol = ((Integer) LONGCOL.lookUp(params)).intValue();
        String projectionString = (String) PROJECTION.lookUp(params);
        ExcelDataStore excel = new ExcelDataStore(file, sheet, headerRow, latCol, longCol,
                projectionString);
        return excel;
    }

    public DataStore createNewDataStore(Map<String, Serializable> params) throws IOException {
        throw new UnsupportedOperationException("Read only datastore");

    }

}
