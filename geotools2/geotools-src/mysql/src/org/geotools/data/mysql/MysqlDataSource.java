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

import java.util.Set;
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
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.AttributeType;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceMetaData;
import org.geotools.data.AbstractDataSource;
import org.geotools.data.Query;
import java.util.logging.Logger;

/**
 * Connects to a Mysql database and returns properly formatted GML.
 *
 * <p>This class is in a rather sorry state, as it has been neglected
 * for far too long.  It was written before Mysql added spatial extensions
 * for 4.1, and was an attempt to show how a non-spatially enabled db could
 * be used - following the schemas of the Simple Features for SQL specification.
 * <p>Since MySQL is now attempting OGC compliant spatial types, this class
 * should be rewritten with those in mind.  This will make this class 
 * extremely similar to the Postgis datasource, so if anyone is interested
 * in rewriting this class, that module should be examined closely.  Most
 * of the problems will likely come from Mysql itself, as it is not yet
 * stable.  But it is good news that it will be spatially enabled, and even
 * if it is a bit buggy, the sooner we are able to get it as a datasource
 * the better.
 *
 * <p>I'm keeping the datasource up to date with api changes, but am doing
 * very little in terms of testing to make sure it is all still working, 
 * I'm more concerned about it not breaking the build, and hopefully a 
 * complete rewrite will come within a few months.  A good rewrite would
 * consider writing some abstract JDBCDataSource classes, as there is 
 * extreme overlap between this and postgis, and to a lesser, though still
 * major, extent oraclespatial.  But it'd probably be easier to just start
 * with a rewrite, and then figure out what common code can factor up.  I 
 * suspect the sql encoding code, as well as the handling of connections,
 * could easily be in an abstract class.
 *
 * @version $Id: MysqlDataSource.java,v 1.5 2003/07/17 07:09:55 ianschneider Exp $
 * @author Chris Holmes, Vision for New York
 * @author Rob Hranac, Vision for New York
 *
 */
public class MysqlDataSource extends AbstractDataSource implements DataSource {

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
    public void getFeatures(FeatureCollection collection, Query query) 
        throws DataSourceException {
	Filter filter = query.getFilter();
	maxFeatures = query.getMaxFeatures();



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
            //FeatureFactory factory = new FeatureFactory(schema);
            Object[] attributes = new Object[schema.getAttributeCount()];
	    AttributeType[] attributeTypes = schema.getAttributeTypes();
	    //int geometryPosition = schema.getDefaultGeometry().getPosition();

            int resultCounter = 0;
            int numAttr = schema.getAttributeCount(); //counter for getting attributes;
            Geometry geom;
	    
            String featureID;
            // loop through entire result set or until maxFeatures are reached
            while (result.next() && ( resultCounter < maxFeatures)) {
                LOGGER.finer("reading feature: " + resultCounter);
                // create an individual attribute by looping through columns
                featureID = result.getString(1); //the first column is always the feature ID;
                for (int col = 0; col < numAttr; col++) {
                    LOGGER.finer("reading attribute: " + col);
                    if (attributeTypes[col].isGeometry()) {
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
		Feature curFeature = schema.create(attributes, featureID);
		if (filter == null || filter.contains(curFeature)) {
		    collection.add(curFeature);
		    LOGGER.finest("adding feature: " + curFeature);
		    resultCounter++;
		}
            }
            result.close();
            statement.close();
            dbConnection.close();
        } catch (SQLException e) {
            LOGGER.warning("Some sort of database connection error: " + e.getMessage());

         } catch (IllegalAttributeException e){
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
	/*private void filterFeatures(List features, Filter filter){
        List filteredFeatures = new ArrayList(maxFeatures);
        for (int i = 0; i < features.size(); i++){
            if (!filter.contains((Feature) features.get(i))){
                features.remove(i);
                i--; //remove shifts index, so we must compensate
            }
        }
	}*/
    

    /**
     * Adds all features from the passed feature collection to the datasource.
     *
     * @param features The collection from which to add the features.
     * @throws DataSourceException If anything goes wrong or if exporting is
     * not supported.
     */
    public Set addFeatures(FeatureCollection features)
        throws DataSourceException {
        Feature[] featureArr = new Feature[features.size()];
        AttributeType[] attributeTypes;
        int geomPos;
        int curFeature;
        String sql = "";
        String featureID;
        String geomSql = "";
        int numAttributes;
        String attrValue = "";
        int gid;
        Geometry curGeom = null;
       
	//TODO: This should use the feature iterator, this is just a 
	//hack to get things working, but it should work fine.
        featureArr = (Feature [])features.toArray(featureArr);
        schema = featureArr[0].getFeatureType();
        attributeTypes = schema.getAttributeTypes(); 
        numAttributes = attributeTypes.length;     
	Object[] curAttributes = new Object[schema.getAttributeCount()];
        //geomPos = schema.getDefaultGeometry().getPosition();
        try { 
            Connection dbConnection = db.getConnection();
            Statement statement = dbConnection.createStatement();
            for (int i = 0; i < featureArr.length; i++){
                curAttributes = featureArr[i].getAttributes(curAttributes);
                sql = "INSERT INTO " + geomDataCol.getFeaTableName() + 
                    " VALUES(";
                featureID = featureArr[i].getID(); 
                sql += addQuotes(featureID) + ", ";
                for (int j = 0; j < curAttributes.length; j++){
                if (attributeTypes[j].isGeometry()) {
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
	return null;
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
        String id = addQuotes(feature.getID());
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
        return (Feature [])collection.toArray(new Feature[collection.size()]);
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
        schema = featureArr[0].getFeatureType();
        gidName = schema.getDefaultGeometry().getName();  //this will not always exist
        //gidPos = schema.getDefaultGeometry().getPosition();
        //if we make our filter db interaction more elegant, ie not importing everything.
        //possible solution is to have the geomDataCol store the schema and feature ID type and name
        try { 
            Connection dbConnection = db.getConnection();
            Statement statement = dbConnection.createStatement();
            Statement stmtR = dbConnection.createStatement();
            ResultSet rs;
            //sql = "SELECT * FROM " + feaTabName;// +" WHERE ID = " + fid;
            //rs = stmtR.executeQuery(sql);
	    Object[] curAttributes = new Object[schema.getAttributeCount()];
	    for (int i = 0; i < featureArr.length; i++){
                curAttributes = featureArr[i].getAttributes(curAttributes);
                fidValue = featureArr[i].getID();
                fid = addQuotes(fidValue);
                //rs.next();
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
            int gid;

            featureArr = getFilteredFeatures(filter);
            //geomPos = schema.getDefaultGeometry().getPosition();
            //be sure to load the schema in the constructor when we no longer 
            //do the nasty getFeatures.

            try {
                Connection dbConnection = db.getConnection();
                Statement stmt = dbConnection.createStatement();
                for (int i = 0; i < featureArr.length; i++){
		    if (Geometry.class.isAssignableFrom(type.getType())) {
                        if ( featureArr[i].getID().equals("6")){
                            gid = createGID(featureArr[i].getID()); //HACK
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
     * Retrieves the featureType that features extracted from this datasource
     * will be created with.
     */
    public FeatureType getSchema() throws DataSourceException {
	return schema;
    }

    /**
     * Creates the a metaData object.  This method should be overridden in any
     * subclass implementing any functions beyond getFeatures, so that clients
     * recieve the proper information about the datasource's capabilities.  <p>
     * 
     * @return the metadata for this datasource.
     *
     * @see #MetaDataSupport
     */
    protected DataSourceMetaData createMetaData() {
	MetaDataSupport mysqlMeta = new MetaDataSupport();
	mysqlMeta.setSupportsAdd(true);
	mysqlMeta.setSupportsRemove(true);
	mysqlMeta.setSupportsModify(true);
	return mysqlMeta;
    }
}

