/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.data.mysql;

import java.util.Map;
import java.util.HashMap;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Category;
import com.vividsolutions.jts.io.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.data.*;
import org.geotools.feature.*;
import org.geotools.datasource.extents.EnvelopeExtent;


/**
 * MysqlGeoColumn is used by MysqlDataSource to query its specific
 * geometric information.  There should be one created for each geometry
 * column of each feature table.  It encapsulates information about
 * the column, such as the name of the corresponding geometric table,
 * the storage type used by that table, the type of geometry contained,
 * and various other useful information culled from the GEOMETRY_COLUMNS
 * table.  It also generates the geometries of the column when queried
 * with the ID from the feature table.
 *
 *
 * @version $Id: MysqlGeomColumn.java,v 1.1 2002/08/04 12:57:40 jmacgill Exp $
 * @author Chris Holmes, Vision for New York
 * tasks TODO: put MakeSchema from MysqlDataSource in this class.
 */
public class MysqlGeomColumn {
    
    /**For get and set Storage type, see SFS for SQL spec*/
    public static final int NORMALIZED_STORAGE_TYPE = 0;
    
    /**For get and set Storage type, see SFS for SQL spec, the Well Known Binary*/
    public static final int WKB_STORAGE_TYPE = 1;
    
    /** From the SFS for SQL spec, always has the meta data*/
    public static final String GEOMETRY_META_NAME = "GEOMETRY_COLUMNS";
    
    /** Standard logging instance */
    private static Category _log = Category.getInstance(MysqlGeomColumn.class.getName());
    
    /** Factory for producing geometries (from JTS). */
    private static GeometryFactory geometryFactory = new GeometryFactory();
    
    /** Well Known Text reader (from JTS). */
    private static WKTReader geometryReader = new WKTReader(geometryFactory);
    
    /** A map containing the raw geometric data, accessed by its geom ID*/
    private static Map gidMap = new HashMap();
    
    /** The catalog containing the feature table using this geometry column*/
    private String feaTabCatalog;
    
    /** The schema containing the feature table using this geometry column*/
    private String feaTabSchema;
    
    /** The name of the feature table using this geometry column*/
    private String feaTabName;
    
    /** The name of the geometry column in the feature table. This
     *  class is basically all the information this column points to*/
    private String feaGeomColumn;
    
    /** The catalog of the geometry table where the column is stored*/
    private String geomTabCatalog;
    
    /** The schema of the geometry table where the column is stored*/
    private String geomTabSchema;
    
    /** The name of the geometry table where the column is stored*/
    private String geomTabName;
    
    /** The storage type, 0 for normalized SQL, 1 for WKB */
    private int storageType;
    
    /** The geometry type, see OGC SFS for SQL section 3.1.2.3 */
    private int geomType;
    
    /** The number of ordinates used, corresponds to the number
     *  of dimensions in the spatial reference system. */
    private int coordDimension;
    
    /** The Max Points Per Row, only used in normalized SQL 92 implementation*/
    private int maxPPR;
    
    /** The ID of the spatial reference system.  It is a foreign key reference to
     * the SPATIAL_REF_SYS table.*/
    private int spacRefID;
    
    
    /**
     * Default constructor
     */
    public MysqlGeomColumn() {
    }
    
    /**
     * Convenience constructor with the minimum meta information needed
     * to do anything useful.
     * @param feaTabName The name of the feature table for this geometry.
     * @param feaGeomColumn The name of the column in the feature table
     *                               that refers to the MysqlGeomColumn.
     * @param geomTabName The name of the table holding the geometry data.
     */
    public MysqlGeomColumn(String feaTabName, String feaGeomColumn, String geomTabName){
        this.feaTabName = feaTabName;
        this.feaGeomColumn = feaGeomColumn;
        this.geomTabName = geomTabName;
    }
    
    /**
     * A convenience constructor, when you there is an open connection,
     * and only using  flat features.  This constructor
     * will not work with feature tables that contain multiple geometries
     * as the query on the feature table will return multiple rows,
     * which will be discarded.  For multiple geometries an array of
     * MysqlGeomColumns must be created, each initialized with the
     * default constructor, filling in the values through the accesssor
     * functions.
     * @param dbConnection An open connection to the database.
     * @param feaTableName The feature table that references this Geometry Col.
     * @param geomTableName The geometry columns table containing meta-info.
     * @tasks TODO: Get rid of this constructor, move the functionality outside.
     */
    public MysqlGeomColumn(Connection dbConnection, String feaTableName){
        this.feaTabName  = feaTableName;
        
        try {
            Statement statement = dbConnection.createStatement();
            //MySQL does not pre-compile statements, so making prepared
            //statements leads to no performance increases.
            String sqlQuery = makeGeomSql(feaTableName);
            _log.info("SQL q = " + sqlQuery);
            ResultSet result = statement.executeQuery(sqlQuery);
            while(result.next()){
                //only flat features for now, with multiple geometries
                //all but the last one will be discarded
                feaTabCatalog = result.getString(1);
                feaTabSchema = result.getString(2);
                feaGeomColumn = result.getString(4);
                geomTabCatalog = result.getString(5);
                geomTabSchema = result.getString(6);
                geomTabName = result.getString(7);
                storageType = result.getInt(8);
                geomType = result.getInt(9);
                coordDimension = result.getInt(10);
                maxPPR = result.getInt(11);
                spacRefID = result.getInt(12);
                
            }
            _log.info("creating new geometry column with values: " +
            feaTabName +" " + feaGeomColumn + " " + geomTabName);
            result =  statement.executeQuery("SELECT * FROM " + geomTabName);
            //currently selects all, should be more elegant as we get complex
            //queries.  Ideally move outside and call populate data on results.
            int gid = 0;
            String WKB = null; //now it is actually Well Known Text, waiting for WKB reader
            while (result.next()){
                gid = result.getInt(1);
                WKB = result.getString(6);
                populateData(gid, WKB);
            }
            result.close();
            statement.close();
            
            
        } catch(SQLException e) {
            _log.info("Some sort of database connection error: " + e.getMessage());
        }
        
    }
    
    /**
     * Creates a sql select statement to get the information on the
     * Geometry column of a feature table.
     *
     * @param feaTableName The feature table we want information about.
     * @return The SQL statement to get the geometry data.
     */
    private String makeGeomSql(String feaTableName) {
        return "SELECT * FROM " + GEOMETRY_META_NAME +
        " WHERE F_TABLE_NAME = '" + feaTableName + "';";
    }
    
    /**
     * Stores the geometry information by geometry ID, so that
     * it can be queried later.  Currently only takes Well Known
     * Text.  This should eventually change to Well Known Binary,
     * possible stored as a bit stream?  And in time an overloaded
     * populateData that allows for normalized SQL 92 storage.
     * @param geomID the primary key for a row in the Geometry Table;
     * @param wellKnownText the WKT representation of the geometry;
     * tasks: TODO: Well Known Binary, and normalized SQL 92 (see SFS for
     * for SQL spec 2.2.5)
     */
    public void populateData(int geomID, String wellKnownText){
        
        _log.info("putting " + wellKnownText + " into gidMap");
        gidMap.put(new Integer(geomID), wellKnownText);
        //we should probably change to objects, GID not necessarily an
        //int, and the getString will change to blob when we do WKB
        
    }
    
    /**
     * Takes out a geometry according to its ID.
     * @param geomID the primary key for a rwo in the Geometry Table
     */
    public void removeData(int geomID) {
        gidMap.remove(new Integer(geomID));
    }
    
    /**
     * Returns a jts Geometry when queried with a geometry ID.
     * @param geomID the ID of the feature geometry.
     * @return a jts geometry represention of the stored data, returns
     * null is it is not found.
     */
    public Geometry getGeometry(int geomID) throws DataSourceException {
        String wellKnownText;
        Geometry returnGeometry = null;
        wellKnownText = (String) gidMap.get(new Integer(geomID));
        _log.info("about to create geometry for " + wellKnownText);
        if (wellKnownText == null){
            return null;
        } else {
            try {
                returnGeometry = geometryReader.read(wellKnownText);
            }
            catch (ParseException e) {
                _log.info("Failed to parse the geometry from Mysql: " + e.getMessage());
            }
            return returnGeometry;
        }
    }
    
    /**
     * Setter method for feature catalog.
     * @param catalog the name of the catalog.
     */
    public void setFeaTableCat(String catalog){
        feaTabCatalog = catalog;
    }
    
    /**
     * Getter method for Feature Catalog.
     * @return the name of the catalog.
     */
    public String getFeaTableCat(){
        return feaTabCatalog;
    }
    
    /**
     * Setter method for feature schema.
     * @param schema the name of the schema.
     */
    public void setFeaTableSchema(String schema){
        feaTabSchema = schema;
    }
    
    /**
     * Getter method for feature schema.
     * @return the name of the schema.
     */
    public String getFeaTableSchema(){
        return feaTabSchema;
    }
    
    /**
     * Setter method for feature table name.
     * @param name the name of the feature table.
     */
    public void setFeaTableName(String name){
        feaTabName = name;
    }
    
    /**
     * Getter method for feature table name.
     * @return the name of the feature table.
     */
    public String getFeaTableName(){
        return feaTabName;
    }
    
    /**
     * Setter method for geometry column.
     * @param name the name of the column.
     */
    public void setGeomColName(String name){
        feaGeomColumn = name;
    }
    
    /**
     * Getter method for geometry column.
     * @return the name of the column.
     */
    public String getGeomColName(){
        return feaGeomColumn;
    }
    
    /**
     * Setter method for geometry catalog.
     * @param catalog the name of the catalog.
     */
    public void setGeomTableCat(String catalog){
        geomTabCatalog = catalog;
    }
    
    /**
     * Getter method for Geometry Catalog.
     * @return the name of the catalog.
     */
    public String getGeomTableCat(){
        return geomTabCatalog;
    }
    
    /**
     * Setter method for geometry schema.
     * @param schema the name of the catalog.
     */
    public void setGeomTableSchema(String schema){
        geomTabSchema = schema;
    }
    
    /**
     * Getter method for geometry schema
     * @return the name of the schema.
     */
    public String getGeomTableSchema(){
        return geomTabSchema;
    }
    
    /**
     * Setter method for geometry table name.
     * @param name the name of the geometry table.
     */
    public void setGeomTableName(String name){
        geomTabName = name;
    }
    
    /**
     * Getter method for geometry table name.
     * @return the name of the catalog.
     */
    public String getGeomTableName(){
        return geomTabName;
    }
    
    /**
     * Sets the type used for storage in the geometry column.
     * @param sType 0 for NORMALIZED_STORAGE_TYPE 1, for WKB_STORAGE_TYPE
     */
    public void setStorageType(int sType){
        storageType = sType;
    }
    
    /**
     * Gets the type used for storage in the geometry column.
     * @return 0 for NORMALIZED_STORAGE_TYPE, 1 for WKB_STORAGE_TYPE
     */
    public int getStorageType(){
        return storageType;
    }
    
    //see note on get geom type
    public void setGeomType(int gType){
        geomType = gType;
    }
    
    //I think this should eventually return the jts geometry class
    //TODO implement a hashmap, int of type key to class type.
    public int getGeomType(){
        return geomType;
    }
    
    
}
