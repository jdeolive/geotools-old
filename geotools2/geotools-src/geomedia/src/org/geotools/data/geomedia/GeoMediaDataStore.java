/*
 *    GeoLBS - OpenSource Location Based Servces toolkit
 *    (C) 2004, Julian J. Ray, All Rights Reserved
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

package org.geotools.data.geomedia;

import com.vividsolutions.jts.geom.Geometry;
import org.geotools.data.AttributeReader;
import org.geotools.data.AttributeWriter;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataUtilities;
import org.geotools.data.FeatureReader;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.JDBCDataStore;
import org.geotools.data.jdbc.JDBCDataStoreConfig;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.QueryData.RowData;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.logging.Logger;


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
public class GeoMediaDataStore extends JDBCDataStore {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.geomedia");

    /** Maps SQL Server SQL types to Java classes */
    private static final Map TYPE_MAPPINGS = new HashMap();

    static {
        TYPE_MAPPINGS.put("bigint", Long.class);
        TYPE_MAPPINGS.put("bigint identity", Long.class);
        TYPE_MAPPINGS.put("int", Integer.class);
        TYPE_MAPPINGS.put("int identity", Integer.class);
        TYPE_MAPPINGS.put("smallint", Integer.class);
        TYPE_MAPPINGS.put("smallint identity", Integer.class);
        TYPE_MAPPINGS.put("char", Byte.class);
        TYPE_MAPPINGS.put("decimal", Double.class);
        TYPE_MAPPINGS.put("float", Float.class);
        TYPE_MAPPINGS.put("money", Double.class);
        TYPE_MAPPINGS.put("numeric", Double.class);
        TYPE_MAPPINGS.put("real", Double.class);
        TYPE_MAPPINGS.put("varchar", String.class);
        TYPE_MAPPINGS.put("nvarchar", String.class);
        TYPE_MAPPINGS.put("nchar", String.class);
    }

    // Used to cache GFeature table to remove need for subsequent reads

    /** DOCUMENT ME! */
    private Hashtable mGFeatureCache = null;

    /**
     * DOCUMENT ME!
     *
     * @param connectionPool ConnectionPool
     *
     * @throws IOException
     */
    public GeoMediaDataStore(ConnectionPool connectionPool)
        throws IOException {
        super(connectionPool, new JDBCDataStoreConfig());

        mGFeatureCache = null;
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException
     */

    /*
       public GeoMediaDataStore(ConnectionPool connectionPool, String namespace, String databaseName) throws IOException
             {
                 super(connectionPool, namespace, databaseName);
                 mGFeatureCache = null;
             }
     */

    /**
     * Reads the GeoMedia GFeatures table and caches metadata information. If the metadata has already been read, it is
     * overwritten. Used to update the DataStore if changes have been made by GeoMedia.
     *
     * @throws DataSourceException
     */
    public void readMetadata() throws DataSourceException {
        // See if this is the first time reading metadata
        if (mGFeatureCache == null) {
            mGFeatureCache = new Hashtable();

            // Empty existing cache if populated
        }

        if (mGFeatureCache.size() > 0) {
            mGFeatureCache.clear();
        }

        Connection conn = null;
        ResultSet  rs = null;
        Statement  stmt = null;

        try {
            conn = getConnection(Transaction.AUTO_COMMIT);

            String sql = "SELECT featurename, geometrytype, primarygeometryfieldname, featuredescription FROM gfeatures";
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            while (rs.next()) {
                GFeatureType gFeature = new GFeatureType();
                gFeature.setTypeName(rs.getString(1));
                gFeature.setGeoMediaFeatureType(rs.getInt(2));
                gFeature.setGeomColName(rs.getString(3));
                gFeature.setDescription(rs.getString(4));

                mGFeatureCache.put(gFeature.getTypeName().toUpperCase(), gFeature);
            }
        } catch (IOException e) {
            LOGGER.warning("IOException reading metadata: " + e.getMessage());
            throw new DataSourceException(e.getMessage());
        } catch (SQLException e) {
            LOGGER.warning("SQLException reading metadata: " + e.getMessage());
            throw new DataSourceException(e.getMessage());
        } finally {
            close(rs);
            close(stmt);
            close(conn);
        }
    }

    /**
     * Returns feature tables which have been entered into the GeoMedia metadata table.
     *
     * @param tablename String - the table to test
     *
     * @return boolean - true if the table is a spatial table, false otherwise.
     */
    protected boolean allowTable(String tablename) {
        // If the metadata has not been initialized then we attempt to do it here. Note that
        // we are swallowing any exceptions which are thrown
        if (mGFeatureCache == null) {
            try {
                readMetadata();
            } catch (IOException e) {
                return false;
            }
        }

        // This just allows feature tables which are stored in the GFeature metadata table
        boolean res = mGFeatureCache.containsKey(tablename.toUpperCase());

        return res;
    }

    /**
     * createGeometryReader
     *
     * @param attrType AttributeType
     * @param queryData QueryData
     * @param index int
     *
     * @return AttributeReader
     *
     * @throws IOException
     */
    protected AttributeReader createGeometryReader(AttributeType attrType, QueryData queryData, int index)
        throws IOException {
        return new GeoMediaAttributeReader(attrType, queryData, index);
    }

    /**
     * DOCUMENT ME!
     *
     * @param fReader DOCUMENT ME!
     * @param writer DOCUMENT ME!
     * @param queryData DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IOException DOCUMENT ME!
     */
    protected JDBCFeatureWriter createFeatureWriter(FeatureReader fReader, AttributeWriter writer, QueryData queryData)
        throws IOException {
        LOGGER.fine("returning GeoMedia feature writer");

        return new GeoMediaFeatureWriter(fReader, writer, queryData);
    }

    /* (non-Javadoc)
     * @see org.geolbs.data.jdbc.JDBCDataStore#createGeometryWriter(org.geolbs.feature.AttributeType, org.geolbs.data.jdbc.JDBCDataStore.QueryData, int)
     */
    protected AttributeWriter createGeometryWriter(AttributeType attrType, QueryData queryData, int index)
        throws IOException {
        return new GeoMediaAttributeReader(attrType, queryData, index);
    }

    /**
     * Overrides the buildAttributeType method to check for GDO_GEOMETRY columns. This function fromes any GeoMedia
     * managed columns which do not contain spatial data such as GDO_GEOMETRY_XHI and spatial indexes.
     *
     * @param rs ResultSet
     *
     * @return AttributeType
     *
     * @throws SQLException
     * @throws DataSourceException
     *
     * @see org.geolbs.data.jdbc.JDBCDataStore#buildAttributeType(java.sql.ResultSet)
     */
    protected AttributeType buildAttributeType(ResultSet rs)
        throws SQLException, DataSourceException {
        final int TABLE_NAME = 3;
        final int COLUMN_NAME = 4;

        // Need to check...
        if (mGFeatureCache == null) {
            readMetadata();
        }

        String columnName = rs.getString(COLUMN_NAME);
        String tableName = rs.getString(TABLE_NAME);

        // We have to check to see if this column name is in the GeoMedia GFeatures geometry col names
        GFeatureType gFeature = (GFeatureType) mGFeatureCache.get(tableName.toUpperCase());

        if (gFeature != null) {
            if (columnName.compareToIgnoreCase(gFeature.getGeomColName()) == 0) {
                return AttributeTypeFactory.newAttributeType(columnName, Geometry.class);
            }
        }

        // Filter out the GDO bounding box columns GDO_GEOMETRY_XHI, GDO_GEOMETRY_XLO, GDO_GEOMETRY_YHI and GDO_GEOMETRY_YLO
        if (columnName.startsWith("GDO_GEOMETRY") == true) {
            return null;
        }

        return super.buildAttributeType(rs);
    }

    /**
     * Closes a result set and catches any errors.
     *
     * @param rs The result set to close.
     */
    private void close(ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            LOGGER.warning("Failed to close result set - " + e.getMessage());
        }
    }

    /**
     * Closes a statement and catches any errors.
     *
     * @param s The statement to close.
     */
    private void close(Statement s) {
        try {
            if (s != null) {
                s.close();
            }
        } catch (SQLException e) {
            LOGGER.warning("Failed to close PreparedStatement - " + e.getMessage());
        }
    }

    /**
     * Closes and catches any errors
     *
     * @param conn Connection
     */
    private void close(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            LOGGER.warning("Error closing connection: " + e);
        }
    }

    /**
     * Internal class used to cache metadata from the GeoMedia GFeatures tables. An instance of this class represents a
     * GeoMedia Feature class.
     *
     * @author Julian J. Ray
     *
     * @todo Add CSGUID information to suppot SRIDs
     */
    protected class GFeatureType {
        //~ Instance fields --------------------------------------------------------------------------------------------

        /** DOCUMENT ME! */
        private String mTypeName;

        /** DOCUMENT ME! */
        private String mGeoColName;

        /** DOCUMENT ME! */
        private int mFeatureType;

        /** DOCUMENT ME! */
        private String mDescription;

        //~ Constructors -----------------------------------------------------------------------------------------------

        /**
         * DOCUMENT ME!
         */
        public GFeatureType() {
        }

        //~ Methods ----------------------------------------------------------------------------------------------------

        /**
         * Returns the name of this Feature Class
         *
         * @return String
         */
        public String getTypeName() {
            return mTypeName;
        }

        /**
         * Sets the TypeName for this feature class.
         *
         * @param name String
         */
        public void setTypeName(String name) {
            mTypeName = name;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String getGeomColName() {
            return mGeoColName;
        }

        /**
         * DOCUMENT ME!
         *
         * @param name DOCUMENT ME!
         */
        public void setGeomColName(String name) {
            mGeoColName = name;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public int getGeoMediaFeatureType() {
            return mFeatureType;
        }

        /**
         * DOCUMENT ME!
         *
         * @param val DOCUMENT ME!
         */
        public void setGeoMediaFeatureType(int val) {
            mFeatureType = val;
        }

        /**
         * DOCUMENT ME!
         *
         * @return DOCUMENT ME!
         */
        public String getDescription() {
            return mDescription;
        }

        /**
         * DOCUMENT ME!
         *
         * @param name DOCUMENT ME!
         */
        public void setDescription(String name) {
            mDescription = name;
        }
    }

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
     * <P>
     * Customized class to write GeoMedia records. Overrides the doInsert() function.
     * </p>
     *
     * @author Julian J. Ray
     * @version 1.0
     */
    public class GeoMediaFeatureWriter extends JDBCFeatureWriter {
        //~ Constructors -----------------------------------------------------------------------------------------------

        /**
         * Creates a new GeoMediaFeatureWriter object.
         *
         * @param fReader DOCUMENT ME!
         * @param writer DOCUMENT ME!
         * @param queryData DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        public GeoMediaFeatureWriter(FeatureReader fReader, AttributeWriter writer, QueryData queryData)
            throws IOException {
            super(fReader, writer, queryData);
        }

        //~ Methods ----------------------------------------------------------------------------------------------------

        /**
         * Performs an insert on the SQL Server database. Needs to do this seperately as SQL Server will not allow the
         * user to write a primary key.
         *
         * @param current Feature being inserted
         *
         * @throws IOException DOCUMENT ME!
         * @throws SQLException DOCUMENT ME!
         * @throws DataSourceException DOCUMENT ME!
         *
         * @todo Manage updating the GDO_GEOMETRY_XHI etc. attributes
         */
        public void doInsert(Feature current) throws IOException, SQLException {
            try {
                queryData.startInsert();

                // TODO This is a bit of a hack
                String fid = current.getID();
                fid = fid.substring(fid.lastIndexOf("-") + 1);

                RowData rd = queryData.getRowData(this);

                //rd.write(Integer.valueOf(fid), 1);
                doUpdate(DataUtilities.template(current.getFeatureType()), current);
            } catch (IllegalAttributeException e) {
                throw new DataSourceException("Unable to do insert", e);
            } catch (Exception e) {
                throw new DataSourceException("Unable to do insert", e);
            }

            queryData.doInsert();

            // Now we need to update the GDO_GEOMETRTY_XHI etc. attributes. Note that these are omitted from the
            // schema as they are suposed to be managed by the system
        }

        /**
         * DOCUMENT ME!
         *
         * @param live DOCUMENT ME!
         * @param current DOCUMENT ME!
         *
         * @throws IOException DOCUMENT ME!
         */
        private void doUpdate(Feature live, Feature current)
            throws IOException {
            try {
                //Can we create for array getAttributes more efficiently?
                for (int i = 0; i < current.getNumberOfAttributes(); i++) {
                    Object curAtt = current.getAttribute(i);
                    Object liveAtt = live.getAttribute(i);

                    if ((live == null) || ! DataUtilities.attributesEqual(curAtt, liveAtt)) {
                        LOGGER.info("modifying att# " + i + " to " + curAtt);
                        writer.write(i, curAtt);
                    }
                }
            } catch (IOException ioe) {
                String message = "problem modifying row";

                if (queryData.getTransaction() != Transaction.AUTO_COMMIT) {
                    queryData.getTransaction().rollback();
                    message += "(transaction canceled)";
                }

                throw ioe;
            }
        }
    }
}
