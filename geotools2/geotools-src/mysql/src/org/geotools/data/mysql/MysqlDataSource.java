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


import java.util.List;
import java.util.ArrayList;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
//import org.apache.log4j.Category;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.feature.SchemaException;
import org.geotools.feature.FeatureFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionDefault;
import org.geotools.feature.AttributeType;
import org.geotools.feature.IllegalFeatureException;
import org.geotools.filter.Filter;
import org.geotools.data.DataSourceException;
import java.util.logging.Logger;

/**
 * Connects to a Mysql database and returns properly formatted GML.
 *
 * <p>This standard class must exist for every supported datastore.</p>
 *
 * @version $Id: MysqlDataSource.java,v 1.1 2002/09/13 15:12:55 cholmesny Exp $
 * @author Chris Holmes, Vision for New York
 * @author Rob Hranac, Vision for New York
 *
 */
public class MysqlDataSource implements org.geotools.data.DataSource {

    /** For use when reading in attributes.  One off due to sql columns
     *  starting at 1 instead of 0, another one for Feature ID in first column.*/
    private static final int COLUMN_OFFSET = 2;
 
    /** The logger for the default core module.  */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.mysql");

    /** Standard logging instance. */
    //    private static Category _log = Category.getInstance(MysqlDataSource.class.getName());
    
    /** Factory for producing geometries (from JTS). */
    private static GeometryFactory geometryFactory = new GeometryFactory();
    
    /** Well Known Text reader (from JTS). */
    private static WKTReader geometryReader = new WKTReader(geometryFactory);
    
    /** Well Known Text writer (from JTS). */
    private static WKTWriter geometryWriter = new WKTWriter();

    /** The maximum features allowed by the server for any given response. */
    private int maxFeatures = 500;

    /** A Mysql connection. */
    private javax.sql.DataSource db;

    /** The meta info needed for a db without native Geometry types */
    private MysqlGeomColumn geomDataCol;

    /** The feature table name. */
    private String tableName;

    /** schema of the features*/
    private FeatureType schema = null;

    /**
     * Initializes the database and request handler.
     *
     * @param db The MysqlConnection to be used from the request object.
     * @param feaTableName The query from the request object.
     */
    public MysqlDataSource (javax.sql.DataSource db, String feaTableName) {
        // create the return response type
        this.db = db;
        this.tableName = feaTableName;
       try {
           Connection dbConnection = db.getConnection();
          geomDataCol = new MysqlGeomColumn(dbConnection, feaTableName);
          //schema = geomDataCol.getSchema();
          dbConnection.close();
       } catch (SQLException e) {
            LOGGER.warning("Some sort of database connection error: " + e.getMessage());
       } catch (SchemaException e) {
            LOGGER.warning("problems creating the schema" + e);
        }
    }


    /**
     * Loads features from the datasource into the passed collection, based on
     * the passed filter.  
     *
     * @param collection The collection to put the features into.
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @throws DataSourceException For all data source errors.
     */ 
    public void getFeatures(FeatureCollection collection, Filter filter) 
        throws DataSourceException {
        List features = new ArrayList(maxFeatures);


        try {
            LOGGER.finer("about to make connection");
            Connection dbConnection = db.getConnection();
            Statement statement = dbConnection.createStatement();
            ResultSet result = statement.executeQuery( makeImportSql(filter));
            // if no schema has been passed to the datasource, roll our own
            // TODO: move this to constructor, as modify and delete also need
            // access, works now because those call a get for filtering purposes
            // as we can't yet unpack the filters.
            if ( schema == null) {
                try {
                    //schema = makeSchema(result.getMetaData());      
                    schema = MysqlGeomColumn.makeSchema(result.getMetaData(), 
                                               geomDataCol.getGeomColName());
                } catch (Exception e) {
                    LOGGER.warning("Had problems creating the Schema..." + e.getMessage());
                }
            }
            LOGGER.finer("about to prepare feature reading");
            // set up a factory, attributes, and a counter for feature creation
            FeatureFactory factory = new FeatureFactory(schema);
            Object[] attributes = new Object[schema.attributeTotal()];
            int geometryPosition = schema.getDefaultGeometry().getPosition();
            int resultCounter = 0;
            int numAttr = schema.attributeTotal(); //counter for getting attributes;
            Geometry geom;
            String featureID;
            // loop through entire result set or until maxFeatures are reached
            while (result.next() && ( resultCounter < maxFeatures)) {
                LOGGER.finer("reading feature: " + resultCounter);
                // create an individual attribute by looping through columns
                featureID = result.getString(1); //the first column is always the feature ID;
                for (int col = 0; col < numAttr; col++) {
                    LOGGER.finer("reading attribute: " + col);
                    if (col == geometryPosition) {
                        geom = geomDataCol.getGeometry( result.getInt(col + COLUMN_OFFSET));
                        if (geom == null) {
                            throw new DataSourceException("inconsistent database, " + 
                                    "the gid did not find valid geometry data");
                        }
                        attributes[col] = geom;
                    } else {
                        attributes[col] = result.getObject( col + COLUMN_OFFSET);
                    }
                }
                //The following line used to read: But featureID support is not yet in CVS
                features.add( factory.create(attributes, featureID));
                //features.add( factory.create(attributes));
                resultCounter++;
            }
            filterFeatures(features, filter);
            collection.addFeatures((Feature[]) features.
                                   toArray(new Feature[features.size()]));
            result.close();
            statement.close();
            dbConnection.close();
        } catch (SQLException e) {
            LOGGER.warning("Some sort of database connection error: " + e.getMessage());

         } catch (IllegalFeatureException e){
             LOGGER.warning("Had problems creating the feature: " + e.getMessage());
         }

    }

    /**
     * Runs through a list of features and returns a list of the
     * ones that are contained by the filter.  Hopefully when
     * unpacking filters has more support this will do relatively
     * little, as the SQL statement should do the majority of the filtering
     *
     * @param features The list to be tested.
     * @param filter The filter to test with.
     */
    private void filterFeatures(List features, Filter filter){
        List filteredFeatures = new ArrayList(maxFeatures);
        for (int i = 0; i < features.size(); i++){
            if (!filter.contains((Feature) features.get(i))){
                features.remove(i);
                i--; //remove shifts index, so we must compensate
            }
        }
    }
    
    
    /**
     * Loads features from the datasource into the returned collection, based on
     * the passed filter.
     *
     * @param filter An OpenGIS filter; specifies which features to retrieve.
     * @return Collection The collection to put the features into.
     * @throws DataSourceException For all data source errors.
     */
    public FeatureCollection getFeatures(Filter filter) 
        throws DataSourceException {
        FeatureCollection fc = new FeatureCollectionDefault();

        getFeatures(fc, filter);
        return fc;
    }

    /**
     * Adds all features from the passed feature collection to the datasource.
     *
     * @param features The collection from which to add the features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     */
    public void addFeatures(FeatureCollection features)
        throws DataSourceException {
        Feature[] featureArr;
        AttributeType[] attributeTypes;
        int geomPos;
        int curFeature;
        Object[] curAttributes;
        String sql = "";
        String featureID;
        String geomSql = "";
        int numAttributes;
        String attrValue = "";
        int gid;
        Geometry curGeom = null;
       
        featureArr = features.getFeatures();
        schema = featureArr[0].getSchema();
        attributeTypes = schema.getAttributeTypes(); 
        numAttributes = attributeTypes.length;     
        geomPos = schema.getDefaultGeometry().getPosition();
        try { 
            Connection dbConnection = db.getConnection();
            Statement statement = dbConnection.createStatement();
            for (int i = 0; i < featureArr.length; i++){
                curAttributes = featureArr[i].getAttributes();
                sql = "INSERT INTO " + geomDataCol.getFeaTableName() + 
                    " VALUES(";
                featureID = featureArr[i].getId(); 
                sql += addQuotes(featureID) + ", ";
                for (int j = 0; j < curAttributes.length; j++){
                if (j == geomPos) {
                    gid = createGID(featureID);
                    curGeom = (Geometry) curAttributes[j];
                    geomSql = makeGeomSql(curGeom, gid);
                    geomDataCol.populateData(gid, geometryWriter.write(curGeom));
                    attrValue = new Integer(gid).toString();
                } else {
                    attrValue = addQuotes(curAttributes[j]);
                }
                sql += attrValue;
                if (j < curAttributes.length - 1){
                    sql += ", ";
                }
            }
            sql += ")";
            LOGGER.finer("this sql statement = " + sql);
            statement.executeUpdate(geomSql);
            statement.executeUpdate(sql);
        }
        statement.close();
        dbConnection.close();
        } catch (SQLException e) {
            LOGGER.warning("Some sort of database connection error: " + e.getMessage());
        }
    }

    /**
     * Returns a unique Geometry ID given a feature ID object
     *
     * @param featureID the feature ID that this Geometry ID corresponds to.
     * @return a unique integer representation based on the featureID.
     */ 
    private int createGID(Object featureID){
        /*The feature ID can be any type of object, its hashcode
         * will consistently return the same integer, and if two
         * objects are equal according to the equals(Object) method
         * they return the same hashcode.*/
        return featureID.hashCode();
    }

    /**
     * Adds quotes to an object for storage in mysql.  The object should
     * be a string or a number.  To perform an insert strings need quotes
     * around them, and numbers work fine with quotes, so this method can
     * be called on unknown objects.
     *
     * @param value The object to add quotes to.
     * @return a string representation of the object with quotes.
     */
    private String addQuotes(Object value){
        String retString;
        retString = "'" + value.toString() + "'";
        return retString;
    }

    /**
     * Creates a sql select statement from the query, currently just 
     * selects all from the tablename passed in at construction.  When
     * unpacking filter support is available this will be more complex.
     *
     * @param filter passed in to importFeatures.
     * @return the sql statement.
     */ 
    private String makeImportSql(Filter filter) {        
        return "SELECT * FROM " + tableName + ";";
    }
    
    /**
     * Creates a sql delete statement from a GID.
     *
     * @param gid The id for this feature's geometry row.
     * @return the sql statement.
     */ 
    private String makeDelGeomSql(int gid){
        return  "DELETE FROM " + geomDataCol.getGeomTableName() + " WHERE " +
                "GID = " + gid;
    }

 /**
     * Creates a sql insert statement to make a row in the geometry
     * column table.
     *
     * @param curGeom the geometry to be stored.
     * @param geomID  the unique GID for this row.
     * @return the sql statement.
     *  tasks TODO use wkb instead of wkt.
     */ 
    private String makeGeomSql(Geometry curGeom, int geomID){
        Envelope env = curGeom.getEnvelopeInternal();
         double xMin = env.getMinX();
         double yMin = env.getMinY();
         double xMax = env.getMaxX();
         double yMax = env.getMaxY();
         String returnSql = "INSERT INTO " + geomDataCol.getGeomTableName()
            + " VALUES(" + geomID + ", " + xMin + ", " + yMin + ", " + xMax + ", " + yMax + ", '" 
            +  geometryWriter.write(curGeom)  + "')";//; "; //eventually this will be wkb, not
        //wktext as it is now.  FeatureID will be same as Geometry ID, x and y maxs
        //are currently just nulls, TODO implement proper maxs and mins.
        LOGGER.finer("to put our location in we say: " + returnSql);
        return returnSql;
    
    }


    /**
     * Creates a sql update statement.
     *
     * @param type the attribute to be changed.
     * @param value the value to change it to.
     * @param feature the feature to update.
     * @return an update sql statement.
     */ 
    private String makeModifySql(AttributeType type, Object value, Feature feature){
        String sql;
        String id = addQuotes(feature.getId());
        //TODO error checking to make sure type matches schema
        sql = "UPDATE " + geomDataCol.getFeaTableName() + " SET " + 
            type.getName() + " = " + addQuotes(value) + " WHERE ID = " + id;
        return sql;
    }


    /**
     *  Performs the filter operation on the database.
     *
     * @param filter the filter for the data
     * @return an list of the feature ID strings
     * tasks TODO: Use unpacking of filters to eliminate the need to call 
     * an import before doing any remove or modify operation.
     * @throws DataSourceException if there was trouble with the datasource.
     */
    private Feature[] getFilteredFeatures(Filter filter)
        throws DataSourceException{
       /* This implementation is awful, we need to think about how
        * to better use SQL clauses to do the filtering instead of pulling
        * all the data out and then examining it
        */
        FeatureCollection collection = getFeatures(filter);
        return collection.getFeatures();
    }

    /**
     * Removes all of the features specificed by the passed filter from the
     * collection.
     *
     * @param filter An OpenGIS filter; specifies which features to remove.
     * @throws DataSourceException If anything goes wrong or if deleting is
     * not supported.
     * @tasks TODO: make SFS for SQL compliant, currently this only works if
     * the FID name is 'ID', should be able to support any name and type.  It 
     * should also be able to delete any GID 
     */
    public void removeFeatures(Filter filter)
        throws DataSourceException {
        Feature[] featureArr;
        Object[] curAttributes;
        String sql = "";
        Object featureID;
        String geomSql = "";
        String attrValue = "";
        Object fidValue;
        String fid = null;
        AttributeType fidType;
        String gidName;
        int gid = 0;
        int gidPos;
        String feaTabName = geomDataCol.getFeaTableName();

        featureArr = getFilteredFeatures(filter);//this does a nasty import, make it nicer.
        schema = featureArr[0].getSchema();
        gidName = schema.getDefaultGeometry().getName();  //this will not always exist
        gidPos = schema.getDefaultGeometry().getPosition();
        //if we make our filter db interaction more elegant, ie not importing everything.
        //possible solution is to have the geomDataCol store the schema and feature ID type and name
        try { 
            Connection dbConnection = db.getConnection();
            Statement statement = dbConnection.createStatement();
            Statement stmtR = dbConnection.createStatement();
            ResultSet rs;
            //sql = "SELECT * FROM " + feaTabName;// +" WHERE ID = " + fid;
            //rs = stmtR.executeQuery(sql);
            for (int i = 0; i < featureArr.length; i++){
                curAttributes = featureArr[i].getAttributes();
                fidValue = featureArr[i].getId();
                fid = addQuotes(fidValue);
                //rs.next();
                //    gid = rs.getInt(gidPos + 2);
                gid = createGID(fidValue); //TODO: make SFS SQL 92 compliant
                    sql = "DELETE FROM "  + feaTabName + " WHERE ID = " + fid;
                    //TODO: the field that contains ID will not always be named ID.
                    LOGGER.finer("our delete says : " + sql);
                    statement.executeUpdate(sql);
                statement.executeUpdate(sql);
                statement.executeUpdate(makeDelGeomSql(gid));
                geomDataCol.removeData(gid); //shouldn't be necessary, but keeps things consistent.
            }
            //rs.close();
                statement.close();
                dbConnection.close();    
        } catch (SQLException e) {
              LOGGER.warning("Error with sql " + e.getMessage());
        }
    }


  /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.  A convenience
     * method for single attribute modifications.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the object type do not match the attribute type.
     */
    public void modifyFeatures(AttributeType type, Object value, Filter filter)
        throws DataSourceException {
            Feature[] featureArr;
            FeatureCollection collection = new FeatureCollectionDefault();
            int geomPos;
            int gid;

            featureArr = getFilteredFeatures(filter);
            geomPos = schema.getDefaultGeometry().getPosition();
            //be sure to load the schema in the constructor when we no longer 
            //do the nasty getFeatures.

            try {
                Connection dbConnection = db.getConnection();
                Statement stmt = dbConnection.createStatement();
                for (int i = 0; i < featureArr.length; i++){
                    LOGGER.finer("type pos = " + type.getPosition() + ", geomPos = " + geomPos);
                    if (Geometry.class.isAssignableFrom(type.getType())) {
                        if ( featureArr[i].getId().equals("6")){
                            gid = createGID(featureArr[i].getId()); //HACK
                            stmt.executeUpdate(makeDelGeomSql(gid));  //delete old row
                            stmt.executeUpdate(makeGeomSql((Geometry) value, gid)); //create new
                        }
                    } else {
                        stmt.executeUpdate(makeModifySql(type, value, featureArr[i]));
                    }
                }
            } catch (SQLException e) {
                LOGGER.warning("sql exception: " + e);
            }    
    }
       
       
    /**
     * Modifies the passed attribute types with the passed objects in all
     * features that correspond to the passed OGS filter.
     *
     * @param type The attributes to modify.
     * @param value The values to put in the attribute types.
     * @param filter An OGC filter to note which attributes to modify.
     * @throws DataSourceException If modificaton is not supported, if
     * the attribute and object arrays are not eqaul length, or if the object
     * types do not match the attribute types.
     */
    public void modifyFeatures(AttributeType[] type, Object[] value, Filter filter)
            throws DataSourceException{
        for (int i = 0; i < type.length; i++){
            modifyFeatures(type[i], value[i], filter);
        }
    }

    /**
     * Stops this DataSource from loading.
     */
    public void abortLoading() {
    }
    

    /**
     * Gets the bounding box of this datasource using the default speed of 
     * this datasource as set by the implementer. 
     *
     * @return The bounding box of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task REVISIT: Consider changing return of getBbox to Filter once Filters can be unpacked
     */
    public Envelope getBbox() {
        return new Envelope();
    }
    

     /**
     * Gets the bounding box of this datasource using the speed of 
     * this datasource as set by the parameter.
     *
     * @param speed If true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but accurate extent
     * will be returned
     * @return The extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     * @task REVISIT:Consider changing return of getBbox to Filter once Filters can be unpacked
     */
    public Envelope getBbox(boolean speed) {
        return new Envelope();
    }
}

