/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.jdbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Geometry;

/**
 * Describes a virtual table, that is, a feature type created starting from a generic SQL query.
 * This class also carries information about the primary key (to generate stable feature ids) and
 * the geometry type and native srid (as in most databases those informations are not available on
 * 
 * @author Andrea Aime - OpenGeo
 * 
 */
public class VirtualTable {
    String name;

    String sql;

    List<String> primaryKeyColumns;

    Map<String, Class<? extends Geometry>> geometryTypes = new HashMap<String, Class<? extends Geometry>>();

    Map<String, Integer> nativeSrids = new HashMap<String, Integer>();

    /**
     * Builds a new virtual table stating its name and the query to be executed to work on it
     * 
     * @param name
     * @param sql
     */
    public VirtualTable(String name, String sql) {
        this.name = name;
        this.sql = sql;
    }

    /**
     * Returns the virtual table primary key columns. It should refer to fields returned by the query, if
     * that is not true the behavior is undefined
     */
    public List<String> getPrimaryKeyColumns() {
        return primaryKeyColumns;
    }

    
    /**
     * Sets the virtual table primary key
     * @param primaryKeyColumns
     */
    public void setPrimaryKeyColumns(List<String> primaryKeyColumns) {
        this.primaryKeyColumns = primaryKeyColumns;
    }

    /**
     * The virtual table name
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * The virtual table sql
     * @return
     */
    public String getSql() {
        return sql;
    }
    
    /**
     * Adds geometry metadata to the virtual table. This is important to get the datastore working,
     * often that is not the case if the right native srid is not in place
     * @param geometry
     * @param binding
     * @param nativeSrid
     */
    public void addGeometryMetadatata(String geometry, Class<? extends Geometry> binding, int nativeSrid) {
        geometryTypes.put(geometry, binding);
        nativeSrids.put(geometry, nativeSrid);
    }

    /**
     * Returns the geometry's specific type, or null if not known
     * @param geometryName
     * @return
     */
    public Class<? extends Geometry> getGeometryType(String geometryName) {
        return geometryTypes.get(geometryName);
    }

    /**
     * Returns the geometry native srid, or -1 if not known
     * @param geometryName
     * @return
     */
    public int getNativeSrid(String geometryName) {
        Integer srid = nativeSrids.get(geometryName);
        if(srid == null) {
            srid = -1;
        }
        return srid;
    }

}
