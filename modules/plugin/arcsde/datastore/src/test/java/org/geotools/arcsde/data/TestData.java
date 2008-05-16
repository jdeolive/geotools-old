/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.arcsde.data;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.pool.ArcSDEConnectionConfig;
import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEConnectionPoolFactory;
import org.geotools.arcsde.pool.Command;
import org.geotools.arcsde.pool.Session;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.data.DataSourceException;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeExtent;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeTable;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Provides access to the ArcSDEDataStore test data configuration.
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java/org/geotools/arcsde/data/TestData.java $
 * @version $Id$
 */
public class TestData {
    /** DOCUMENT ME! */
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger(TestData.class
            .getPackage().getName());

    public static final String[] TEST_TABLE_COLS = { "INT32_COL", "INT16_COL", "FLOAT32_COL",
            "FLOAT64_COL", "STRING_COL", "DATE_COL", "SHAPE" };

    private SeColumnDefinition[] tempTableColumns;

    private SeLayer tempTableLayer;

    private SeTable tempTable;

    /**
     * the set of test parameters loaded from {@code test-data/testparams.properties}
     */
    private Properties conProps = null;

    /**
     * the name of a table that can be manipulated without risk of loosing important data
     */
    private String temp_table;

    /** the configuration keyword to use when creating layers and tables */
    private String configKeyword;

    private ArcSDEConnectionPool _pool;

    /**
     * Creates a new TestData object.
     * 
     * @throws IOException DOCUMENT ME!
     */
    public TestData() {
        // intentionally blank
    }

    /**
     * Must be called from inside the test's setUp() method. Loads the test fixture from
     * <code>testparams.properties</code>, besides that, does not creates any connection nor any
     * other costly resource.
     * 
     * @throws IOException if the test fixture can't be loaded
     * @throws IllegalArgumentException if some required parameter is not found on the test fixture
     */
    public void setUp() throws IOException {
        this.conProps = new Properties();

        String propsFile = "testparams.properties";
        InputStream in = org.geotools.test.TestData.openStream(null, propsFile);

        // The line above should never returns null. It should thow a
        // FileNotFoundException instead if the resource is not available.

        this.conProps.load(in);
        in.close();

        this.temp_table = this.conProps.getProperty("temp_table");
        this.configKeyword = this.conProps.getProperty("configKeyword");
        if (this.configKeyword == null) {
            this.configKeyword = "DEFAULTS";
        }

        if (this.temp_table == null) {
            throw new IllegalArgumentException("temp_table not defined in " + propsFile);
        }
    }

    /**
     * Must be called from inside the test's tearDown() method.
     */
    public void tearDown(boolean cleanTestTable, boolean cleanPool) {
        if (cleanTestTable) {
            deleteTempTable();
        }
        if (cleanPool) {
            ArcSDEConnectionPoolFactory pfac = ArcSDEConnectionPoolFactory.getInstance();
            pfac.clear();
        }
    }

    public SeTable getTempTable() {
        if (tempTable == null) {
            throw new IllegalStateException("createTempTable() not called first");
        }
        return tempTable;
    }

    public SeLayer getTempLayer() {
        if (tempTableLayer == null) {
            throw new IllegalStateException("createTempTable() not called first");
        }
        return tempTableLayer;
    }

    /**
     * creates an ArcSDEDataStore using {@code test-data/testparams.properties} as holder of
     * datastore parameters
     * 
     * @return DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public ArcSDEDataStore getDataStore() throws IOException {
        ArcSDEConnectionPool pool = getConnectionPool();
        ArcSDEDataStore dataStore = new ArcSDEDataStore(pool);

        return dataStore;
    }

    public ArcSDEConnectionPool getConnectionPool() throws DataSourceException {
        if (this._pool == null) {
            ArcSDEConnectionPoolFactory pfac = ArcSDEConnectionPoolFactory.getInstance();
            ArcSDEConnectionConfig config = new ArcSDEConnectionConfig(this.conProps);
            this._pool = pfac.createPool(config);
        }
        return this._pool;
    }

    /**
     * DOCUMENT ME!
     * 
     * @return Returns the conProps.
     */
    public Properties getConProps() {
        return this.conProps;
    }

    public String getTemp_table() throws IOException, UnavailableArcSDEConnectionException {
        Session session = getConnectionPool().getConnection();
        String tempTableName;
        try {
            tempTableName = getTemp_table(session);
        } finally {
            session.close();
        }
        return tempTableName;
    }

    /**
     * @return Returns the temp_table.
     * @throws SeException
     */
    public String getTemp_table(Session session) throws IOException {
        return session.getUser() + "." + this.temp_table;
    }

    public String getConfigKeyword() {
        return this.configKeyword;
    }

    /**
     * Gracefully deletes the temp table hiding any exception (no problem if it does not exist)
     */
    public void deleteTempTable() {
        // only if the datastore was used
        if (this._pool != null) {
            try {
                _pool = getConnectionPool();
                deleteTempTable(_pool);
            } catch (Exception e) {
                LOGGER.fine(e.getMessage());
            }
        }
    }

    public void deleteTable(final String typeName) throws IOException,
            UnavailableArcSDEConnectionException {
        ArcSDEConnectionPool connectionPool = getConnectionPool();
        deleteTable(connectionPool, typeName);
    }

    /**
     * Gracefully deletes the temp table hiding any exception (no problem if it does not exist)
     * 
     * @param connPool to get the connection to use in deleting {@link #getTemp_table()}
     */
    public void deleteTempTable(ArcSDEConnectionPool connPool) {
        try {
            deleteTable(connPool, getTemp_table());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void deleteTable(final ArcSDEConnectionPool connPool, final String tableName)
            throws IOException, UnavailableArcSDEConnectionException {

        final Session session = connPool.getConnection();

        final Command<Void> deleteCmd = new Command<Void>() {

            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                try {
                    SeLayer layer = session.getLayer(tableName);
                    layer.delete();
                } catch (NoSuchElementException e) {
                    // nothing to do
                } catch (SeException e) {
                    // LOGGER.log(Level.WARNING, "while deleteing layer " + tableName + " got '" +
                    // e.getSeError().getErrDesc() + "'");
                }
                SeTable table = session.createSeTable(tableName);
                table.delete();
                return null;
            }
        };

        session.issue(deleteCmd);
        session.close();
    }

    /**
     * Creates an ArcSDE feature type names as <code>getTemp_table()</code> on the underlying
     * database and if <code>insertTestData == true</code> also inserts some sample values.
     * 
     * @param insertTestData wether to insert some sample rows or not
     * @throws Exception for any error
     */
    public void createTempTable(final boolean insertTestData) throws Exception {
        ArcSDEConnectionPool connPool = getConnectionPool();

        deleteTempTable(connPool);

        Session session = connPool.getConnection();

        try {
            /*
             * Create a qualified table name with current user's name and the name of the table to
             * be created, "EXAMPLE".
             */
            tempTableLayer = session.createSeLayer();
            String tableName = getTemp_table(session);
            tempTable = session.createSeTable(tableName);
            tempTableLayer.setTableName(tableName);

            tempTableColumns = createBaseTable(session, tempTable, tempTableLayer, configKeyword);

            if (insertTestData) {
                insertData(tempTableLayer, session, tempTableColumns);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            session.close();
        }
    }

    /**
     * Truncates the temp layer and populates it with fresh data. This method cannot be called if
     * {@link #createTempTable(boolean)} has not been called first, no matter if the table already
     * exists, it needs instance state initialized by createTempTable
     * 
     * @throws Exception
     */
    public void insertTestData() throws Exception {
        ArcSDEConnectionPool connPool = getConnectionPool();
        Session session = connPool.getConnection();
        try {
            tempTable.truncate();
            insertData(tempTableLayer, session, tempTableColumns);
        } finally {
            session.close();
        }
    }

    public void truncateTempTable() {
        if (tempTable != null) {
            try {
                tempTable.truncate();
            } catch (SeException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 
     * 
     */
    private static SeColumnDefinition[] createBaseTable(final Session session,
            final SeTable table,
            final SeLayer layer,
            final String configKeyword) throws IOException {

        Command<SeColumnDefinition[]> createTableCmd = new Command<SeColumnDefinition[]>() {

            @Override
            public SeColumnDefinition[] execute(Session session, SeConnection connection)
                    throws SeException, IOException {

                SeColumnDefinition[] colDefs = new SeColumnDefinition[8];

                /*
                 * Define the columns and their attributes for the table to be created. NOTE: The
                 * valid range/values of size and scale parameters vary from one database to
                 * another.
                 */
                boolean isNullable = true;

                // first column to be SDE managed feature id
                colDefs[0] = new SeColumnDefinition("ROW_ID", SeColumnDefinition.TYPE_INTEGER, 10,
                        0, false);

                colDefs[1] = new SeColumnDefinition(TEST_TABLE_COLS[0],
                        SeColumnDefinition.TYPE_INTEGER, 10, 0, isNullable);
                colDefs[2] = new SeColumnDefinition(TEST_TABLE_COLS[1],
                        SeColumnDefinition.TYPE_SMALLINT, 4, 0, isNullable);
                colDefs[3] = new SeColumnDefinition(TEST_TABLE_COLS[2],
                        SeColumnDefinition.TYPE_FLOAT, 5, 2, isNullable);
                colDefs[4] = new SeColumnDefinition(TEST_TABLE_COLS[3],
                        SeColumnDefinition.TYPE_DOUBLE, 25, 4, isNullable);
                colDefs[5] = new SeColumnDefinition(TEST_TABLE_COLS[4],
                        SeColumnDefinition.TYPE_STRING, 25, 0, isNullable);
                colDefs[6] = new SeColumnDefinition(TEST_TABLE_COLS[5],
                        SeColumnDefinition.TYPE_DATE, 1, 0, isNullable);
                // this is a blob one and should be ignored to all effects
                colDefs[7] = new SeColumnDefinition("SE_ANNO_CAD_DATA",
                        SeColumnDefinition.TYPE_BLOB, 1000, 0, isNullable);

                /*
                 * Create the table using the DBMS default configuration keyword. Valid keywords are
                 * defined in the dbtune table.
                 */
                table.create(colDefs, configKeyword);

                /*
                 * Register the column to be used as feature id and managed by sde
                 */
                SeRegistration reg = session.createSeRegistration(table.getName());
                LOGGER.fine("setting rowIdColumnName to ROW_ID in table " + reg.getTableName());
                reg.setRowIdColumnName("ROW_ID");
                final int rowIdColumnType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE;
                reg.setRowIdColumnType(rowIdColumnType);
                reg.alter();

                /*
                 * Define the attributes of the spatial column
                 */
                layer.setSpatialColumnName(TEST_TABLE_COLS[6]);

                /*
                 * Set the type of shapes that can be inserted into the layer. Shape type can be
                 * just one or many. NOTE: Layers that contain more than one shape type can only be
                 * accessed through the C and Java APIs and Arc Explorer Java 3.x. They cannot be
                 * seen from ArcGIS desktop applications.
                 */
                layer.setShapeTypes(SeLayer.SE_NIL_TYPE_MASK | SeLayer.SE_POINT_TYPE_MASK
                        | SeLayer.SE_LINE_TYPE_MASK | SeLayer.SE_SIMPLE_LINE_TYPE_MASK
                        | SeLayer.SE_AREA_TYPE_MASK | SeLayer.SE_MULTIPART_TYPE_MASK);
                layer.setGridSizes(1100.0, 0.0, 0.0);
                layer.setDescription("Layer Example");

                /*
                 * Define the layer's Coordinate Reference
                 */
                SeCoordinateReference coordref = getGenericCoordRef();

                // SeExtent ext = new SeExtent(-1000000.0, -1000000.0, 1000000.0,
                // 1000000.0);
                SeExtent ext = coordref.getXYEnvelope();
                layer.setExtent(ext);
                layer.setCoordRef(coordref);

                layer.setCreationKeyword(configKeyword);

                /*
                 * Spatially enable the new table...
                 */
                layer.create(3, 4);

                return colDefs;
            }
        };
        SeColumnDefinition[] colDefs = session.issue(createTableCmd);
        return colDefs;
    }

    /**
     * Inserts 8 rows of data into the layer Columns Inserted:
     * <ul>
     * <li>1. Integer - values: 1 -> 8
     * <li>2. Short - values: 1 -> 8
     * <li>3. Float - values: 0.1 -> 0.8
     * <li>4. Double - values: 0.1 -> 0.8
     * <li>5. String - values: <code>"FEATURE_" + ["1" -> "8"]</code>
     * <li>6. Date - values: July 1 2004 -> July 8 2004
     * <li>7. Shape - values:
     * <ul>
     * <li> <code>POINT(0 0)</code>
     * <li> <code>MULTIPOINT(0 0, 180 0)</code>
     * <li> <code>LINESTRING(0 0, 180 90)</code>
     * <li> <code>MULTILINESTRING((-180 -90, 180 90), (-180 90, 180 -90))</code>
     * <li> <code>POLYGON((-10 -10, -10 10, 10 10, 10 -10, -10 -10))</code>
     * <li>
     * <code>MULTIPOLYGON( ((-1 -1, -1 1, 1 1, 1 -1, -1 -1)), ((-180 -90, -180 -80, -170 -80, -170 -90, -180 -90)) )</code>
     * <li> <code>GEOMETRYCOLLECTION(POINT(1 1), LINESTRING(0 0, 180 90))</code>
     * <li> <code>null</code>
     * </ul>
     * </li>
     * 
     * @throws ParseException
     */
    private void insertData(SeLayer layer, Session session, SeColumnDefinition[] colDefs)
            throws Exception {
        WKTReader reader = new WKTReader();
        Geometry[] geoms = new Geometry[8];
        geoms[0] = reader.read("POINT(0 0)");
        geoms[1] = reader.read("MULTIPOINT(0 0, 170 0)");
        geoms[2] = reader.read("LINESTRING(0 0, 170 80)");
        geoms[3] = reader.read("MULTILINESTRING((-170 -80, 170 80), (-170 80, 170 -80))");
        geoms[4] = reader.read("POLYGON((-10 -10, -10 10, 10 10, 10 -10, -10 -10))");
        geoms[5] = reader
                .read("MULTIPOLYGON( ((-1 -1, -1 1, 1 1, 1 -1, -1 -1)), ((-170 -80, -170 -70, -160 -70, -160 -80, -170 -80)) )");
        geoms[6] = reader.read("POINT EMPTY");
        geoms[7] = null;

        insertData(geoms, layer, session);
    }

    public void insertData(final Geometry[] g, SeLayer layer, Session session) throws Exception {

        SeColumnDefinition[] colDefs = tempTableColumns;
        Geometry[] geoms = g;
        if (geoms.length < 8) {
            Geometry[] tmp = new Geometry[8];
            System.arraycopy(geoms, 0, tmp, 0, geoms.length);
            geoms = tmp;
        }

        final SeCoordinateReference coordref = layer.getCoordRef();
        SeShape shapes[] = new SeShape[8];
        for (int i = 0; i < shapes.length; i++) {
            Geometry geom = geoms[i];
            SeShape shape;
            if (geom == null) {
                shape = null;
            } else {
                ArcSDEGeometryBuilder builder = ArcSDEGeometryBuilder.builderFor(geom.getClass());
                shape = builder.constructShape(geom, coordref);
            }
            shapes[i] = shape;
        }
        /*
         * Define the names of the columns that data is to be inserted into.
         */
        String[] columns = new String[7];

        columns[0] = colDefs[1].getName(); // INT32 column
        columns[1] = colDefs[2].getName(); // INT16 column
        columns[2] = colDefs[3].getName(); // FLOAT32 column
        columns[3] = colDefs[4].getName(); // FLOAT64 column
        columns[4] = colDefs[5].getName(); // String column
        columns[5] = colDefs[6].getName(); // Date column
        columns[6] = "SHAPE"; // Shape column

        SeInsert insert = session.createSeInsert();
        insert.intoTable(layer.getName(), columns);
        insert.setWriteMode(true);

        Calendar cal = Calendar.getInstance();
        // Year, month, date, hour, minute, second.
        cal.set(2004, 06, 1, 0, 0, 0);

        try {
            for (int i = 1; i <= shapes.length; i++) {
                SeRow row = insert.getRowToSet();
                // col #0 is the sde managed row id
                row.setInteger(0, Integer.valueOf(i));
                row.setShort(1, Short.valueOf((short) i));
                row.setFloat(2, new Float(i / 10.0F));
                row.setDouble(3, new Double(i / 10D));
                row.setString(4, "FEATURE_" + i);
                cal.set(Calendar.DAY_OF_MONTH, i);
                row.setTime(5, cal);
                SeShape seShape = shapes[i - 1];
                row.setShape(6, seShape);

                insert.execute();
            }
        } catch (SeException e) {
            throw e;
        } finally {
            insert.close();
        }

        insert.close();

    } // End method insertData

    /**
     * Creates a FeatureCollection<SimpleFeatureType, SimpleFeature> with features whose schema
     * adheres to the one created in <code>createTestData()</code> and returns it.
     * <p>
     * This schema is something like:
     * 
     * <pre>
     *  colDefs[0] &quot;INT32_COL&quot;, SeColumnDefinition.TYPE_INTEGER, 10, 0, true
     *  colDefs[1] = &quot;INT16_COL&quot;, SeColumnDefinition.TYPE_SMALLINT, 4, 0, true
     *  colDefs[2] = &quot;FLOAT32_COL&quot;, SeColumnDefinition.TYPE_FLOAT, 5, 2, true
     *  colDefs[3] = &quot;FLOAT64_COL&quot;, SeColumnDefinition.TYPE_DOUBLE, 15, 4, true
     *  colDefs[4] = &quot;STRING_COL&quot;, SeColumnDefinition.TYPE_STRING, 25, 0, true
     *  colDefs[5] = &quot;DATE_COL&quot;, SeColumnDefinition.TYPE_DATE, 1, 0, true
     *  colDefs[6] = &quot;SHAPE&quot;, Geometry, 1, 0, true
     * </pre>
     * 
     * </p>
     * 
     * @param jtsGeomType class of JTS geometry to create
     * @param numFeatures number of features to create.
     * @throws IOException if the schema for te test table cannot be fetched from the database.
     * @throws IllegalAttributeException if the feature type created from the test table cannot
     *             build a feature with the given attribute values.
     * @throws SeException
     */
    public FeatureCollection<SimpleFeatureType, SimpleFeature> createTestFeatures(Class jtsGeomType,
            int numFeatures) throws IOException, IllegalAttributeException, SeException {
        FeatureCollection<SimpleFeatureType, SimpleFeature> col = FeatureCollections
                .newCollection();
        SimpleFeatureType type = getDataStore().getSchema(getTemp_table());
        Object[] values = new Object[type.getAttributeCount()];

        for (int i = 0; i < numFeatures; i++) {
            values[0] = Integer.valueOf(i);

            // put some nulls
            values[1] = ((i % 2) == 0) ? null : Integer.valueOf(2 * i);
            values[2] = new Float(0.1 * i);
            values[3] = new Double(1000 * i);
            values[4] = "String value #" + i;

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, i);
            values[5] = cal.getTime();
            values[6] = createTestGeometry(jtsGeomType, i);

            SimpleFeature f = SimpleFeatureBuilder.build(type, values, null);
            col.add(f);
        }

        return col;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param geomType DOCUMENT ME!
     * @param index DOCUMENT ME!
     * @return DOCUMENT ME!
     * @throws UnsupportedOperationException DOCUMENT ME!
     */
    private static Geometry createTestGeometry(Class geomType, int index) {
        Geometry geom = null;
        GeometryFactory gf = new GeometryFactory();

        if (geomType == Geometry.class) {
            geom = createTestGenericGeometry(gf, index);
        } else if (geomType == Point.class) {
            geom = createTestPoint(gf, index);
        } else if (geomType == MultiPoint.class) {
            geom = createTestMultiPoint(gf, index);
        } else if (geomType == LineString.class) {
            geom = createTestLineString(gf, index);
        } else if (geomType == MultiLineString.class) {
            geom = createTestMultiLineString(gf, index);
        } else if (geomType == Polygon.class) {
            geom = createTestPolygon(gf, index);
        } else if (geomType == MultiPolygon.class) {
            geom = createTestMultiPolygon(gf, index);
        } else {
            throw new UnsupportedOperationException("finish implementing this!");
        }

        return geom;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    private static Geometry createTestGenericGeometry(GeometryFactory gf, int index) {
        if ((index % 6) == 0) {
            return createTestPoint(gf, index);
        } else if ((index % 5) == 0) {
            return createTestMultiPoint(gf, index);
        } else if ((index % 4) == 0) {
            return createTestLineString(gf, index);
        } else if ((index % 3) == 0) {
            return createTestMultiLineString(gf, index);
        } else if ((index % 2) == 0) {
            return createTestPolygon(gf, index);
        } else {
            return createTestMultiPolygon(gf, index);
        }
    }

    /**
     * DOCUMENT ME!
     * 
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    private static Point createTestPoint(GeometryFactory gf, int index) {
        return gf.createPoint(new Coordinate(index, index));
    }

    /**
     * DOCUMENT ME!
     * 
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    private static MultiPoint createTestMultiPoint(GeometryFactory gf, int index) {
        Coordinate[] coords = { new Coordinate(index, index), new Coordinate(-index, -index) };

        return gf.createMultiPoint(coords);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    private static LineString createTestLineString(final GeometryFactory gf, final int index) {
        Coordinate[] coords = { new Coordinate(0, 0), new Coordinate(1 + index, -index) };

        return gf.createLineString(coords);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    private static MultiLineString createTestMultiLineString(final GeometryFactory gf,
            final int index) {
        Coordinate[] coords1 = { new Coordinate(0, 0), new Coordinate(1 + index, 1 + index) };
        Coordinate[] coords2 = { new Coordinate(0, index), new Coordinate(index + 1, 0) };
        LineString[] lines = { gf.createLineString(coords1), gf.createLineString(coords2) };

        return gf.createMultiLineString(lines);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    private static Polygon createTestPolygon(GeometryFactory gf, int index) {
        Coordinate[] coords = { new Coordinate(index, index), new Coordinate(index, index + 1),
                new Coordinate(index + 1, index + 1), new Coordinate(index + 1, index),
                new Coordinate(index, index) };
        LinearRing shell = gf.createLinearRing(coords);

        return gf.createPolygon(shell, null);
    }

    /**
     * DOCUMENT ME!
     * 
     * @param gf DOCUMENT ME!
     * @param index DOCUMENT ME!
     * @return DOCUMENT ME!
     */
    private static MultiPolygon createTestMultiPolygon(GeometryFactory gf, int index) {
        Polygon[] polys = { createTestPolygon(gf, index), createTestPolygon(gf, 1 + index) };

        MultiPolygon mp = gf.createMultiPolygon(polys);
        // System.out.println(mp);

        return mp;
    }

    /**
     * Creates and returns a <code>SeCoordinateReference</code> CRS, though based on WGS84, is
     * inclusive enough (in terms of valid coordinate range and presicion) to deal with most
     * coordintates.
     * <p>
     * Actually tested to deal with coordinates with 0.0002 units of separation as well as with
     * large coordinates such as UTM (values greater than 500,000.00)
     * </p>
     * 
     * @return DOCUMENT ME!
     * @throws SeException DOCUMENT ME!
     */
    public static SeCoordinateReference getGenericCoordRef() throws SeException {

        SeCoordinateReference seCRS = new SeCoordinateReference();
        final String wgs84WKT = DefaultGeographicCRS.WGS84.toWKT();
        seCRS.setCoordSysByDescription(wgs84WKT);
        // seCRS.setPrecision(1000);
        seCRS.setXYByEnvelope(new SeExtent(-180, -90, 180, 90));
        return seCRS;
    }

    /**
     * Creates some simple test layers on the sde instance
     * 
     * @param argv
     */
    public static void main(String[] argv) {
        TestData testData = new TestData();
        try {
            testData.setUp();
            testData.createSimpleTestTables();
        } catch (SeException e) {
            LOGGER.log(Level.WARNING, "while creating test tables got '"
                    + e.getSeError().getErrDesc() + "'");
            e.printStackTrace();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "while creating test tables got '" + e.getMessage() + "'");
            e.printStackTrace();
        }
    }

    private void createSimpleTestTables() throws IOException, SeException {
        final ArcSDEConnectionPool connectionPool = getConnectionPool();
        final Session session = connectionPool.getConnection();

        String tableName;
        String rowIdColName;
        int rowIdColumnType;
        int shapeTypeMask;
        try {
            rowIdColName = "ROW_ID";
            shapeTypeMask = SeLayer.SE_POINT_TYPE_MASK;

            tableName = "GT_TEST_POINT_ROWID_USER";
            rowIdColumnType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_USER;
            createSimpleTestTable(session, tableName, rowIdColName, rowIdColumnType, shapeTypeMask);

            tableName = "GT_TEST_POINT_ROWID_SDE";
            rowIdColumnType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE;
            createSimpleTestTable(session, tableName, rowIdColName, rowIdColumnType, shapeTypeMask);

            tableName = "GT_TEST_POINT_ROWID_NONE";
            rowIdColumnType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_NONE;
            createSimpleTestTable(session, tableName, rowIdColName, rowIdColumnType, shapeTypeMask);

            shapeTypeMask = SeLayer.SE_LINE_TYPE_MASK;

            tableName = "GT_TEST_LINE_ROWID_USER";
            rowIdColumnType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_USER;
            createSimpleTestTable(session, tableName, rowIdColName, rowIdColumnType, shapeTypeMask);

            tableName = "GT_TEST_LINE_ROWID_SDE";
            rowIdColumnType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE;
            createSimpleTestTable(session, tableName, rowIdColName, rowIdColumnType, shapeTypeMask);

            tableName = "GT_TEST_LINE_ROWID_NONE";
            rowIdColumnType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_NONE;
            createSimpleTestTable(session, tableName, rowIdColName, rowIdColumnType, shapeTypeMask);

            shapeTypeMask = SeLayer.SE_AREA_TYPE_MASK;

            tableName = "GT_TEST_POLYGON_ROWID_USER";
            rowIdColumnType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_USER;
            createSimpleTestTable(session, tableName, rowIdColName, rowIdColumnType, shapeTypeMask);

            tableName = "GT_TEST_POLYGON_ROWID_SDE";
            rowIdColumnType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE;
            createSimpleTestTable(session, tableName, rowIdColName, rowIdColumnType, shapeTypeMask);

            tableName = "GT_TEST_POLYGON_ROWID_NONE";
            rowIdColumnType = SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_NONE;
            createSimpleTestTable(session, tableName, rowIdColName, rowIdColumnType, shapeTypeMask);
        } finally {
            session.close();
        }
    }

    private void createSimpleTestTable(final Session session,
            final String tableName,
            final String rowIdColName,
            final int rowIdColumnType,
            final int shapeTypeMask) throws IOException {
        System.out.println("Creating layer " + tableName);

        final Command<Void> createCmd = new Command<Void>() {

            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                final SeLayer layer = new SeLayer(connection);
                final SeTable table = new SeTable(connection, tableName);

                try {
                    table.delete();
                } catch (SeException e) {
                    System.out.println("table " + tableName + " didnt already exist");
                }
                layer.setTableName(tableName);

                final boolean isNullable = true;

                // ROW_ID, INT_COL, DATE_COL, STRING_COL, SE_ANNO_CAD_DATA
                final int numCols = 5;
                final SeColumnDefinition[] colDefs = new SeColumnDefinition[numCols];

                // first column to be SDE managed feature id
                colDefs[0] = new SeColumnDefinition("ROW_ID", SeColumnDefinition.TYPE_INT32, 10, 0,
                        false);
                colDefs[1] = new SeColumnDefinition("INT_COL", SeColumnDefinition.TYPE_INT32, 10,
                        0, isNullable);
                colDefs[2] = new SeColumnDefinition("DATE_COL", SeColumnDefinition.TYPE_DATE, 1, 0,
                        isNullable);
                colDefs[3] = new SeColumnDefinition("STRING_COL", SeColumnDefinition.TYPE_STRING,
                        25, 0, isNullable);
                // use a blob type just to make sure they're correctly ignored
                colDefs[4] = new SeColumnDefinition("SE_ANNO_CAD_DATA",
                        SeColumnDefinition.TYPE_BLOB, 4000, 0, isNullable);

                /*
                 * Create the table using the DBMS default configuration keyword. Valid keywords are
                 * defined in the dbtune table.
                 */
                table.create(colDefs, configKeyword);

                /*
                 * Register the column to be used as feature id and managed by sde
                 */
                if (SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_NONE != rowIdColumnType) {
                    SeRegistration reg = new SeRegistration(connection, table.getName());
                    LOGGER.fine("setting rowIdColumnName to ROW_ID in table " + reg.getTableName());
                    reg.setRowIdColumnName("ROW_ID");
                    reg.setRowIdColumnType(rowIdColumnType);
                    reg.alter();
                }

                // Only tables with an sde maintained rowid column can be versioned
                if (SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE == rowIdColumnType) {
                    // make the table multiversioned
                    System.err.println("Making " + tableName + " versioned...");
                    SeRegistration reg = new SeRegistration(connection, tableName);
                    reg.getInfo();
                    reg.setMultiVersion(true);
                    reg.alter();
                    System.err.println(tableName + " successfully made versioned");
                }

                /*
                 * Define the attributes of the spatial column
                 */
                layer.setSpatialColumnName("GEOM");

                /*
                 * Set the type of shapes that can be inserted into the layer.
                 */
                layer.setShapeTypes(SeLayer.SE_NIL_TYPE_MASK | shapeTypeMask);
                layer.setGridSizes(1100.0, 0.0, 0.0);
                layer.setDescription("GeoTools test table");

                /*
                 * Define the layer's Coordinate Reference
                 */
                SeCoordinateReference coordref = getGenericCoordRef();

                // SeExtent ext = new SeExtent(-1000000.0, -1000000.0, 1000000.0,
                // 1000000.0);
                SeExtent ext = coordref.getXYEnvelope();
                layer.setExtent(ext);
                layer.setCoordRef(coordref);

                layer.setCreationKeyword(configKeyword);

                /*
                 * Spatially enable the new table...
                 */
                layer.create(3, 4);
                return null;
            }
        };

        session.equals(createCmd);
    }

    /**
     * Creates a versioned table with a name column and a point SHAPE column
     * 
     * @return the versioned table created
     * @throws Exception any exception thrown by sde
     */
    public SeTable createVersionedTable(final Session session) throws Exception {
        // SeConnection conn = session.unWrap();
        SeLayer layer = session.createSeLayer();
        SeTable table;

        /*
         * Create a qualified table name with current user's name and the name of the table to be
         * created, "EXAMPLE".
         */
        String tableName = (session.getUser() + ".VERSIONED_EXAMPLE");
        table = session.createSeTable(tableName);
        layer.setTableName("VERSIONED_EXAMPLE");

        try {
            table.delete();
        } catch (Exception e) {
            // ignore, the table didn't exist already
        }

        SeColumnDefinition[] colDefs = new SeColumnDefinition[2];
        boolean isNullable = true;
        // first column to be SDE managed feature id
        colDefs[0] = new SeColumnDefinition("ROW_ID", SeColumnDefinition.TYPE_INT32, 10, 0, false);
        colDefs[1] = new SeColumnDefinition("NAME", SeColumnDefinition.TYPE_STRING, 25, 0,
                isNullable);

        table.create(colDefs, getConfigKeyword());
        layer.setSpatialColumnName("SHAPE");

        /*
         * Register the column to be used as feature id and managed by sde
         */
        SeRegistration reg = session.createSeRegistration(table.getName());
        LOGGER.fine("setting rowIdColumnName to ROW_ID in table " + reg.getTableName());
        reg.setRowIdColumnName("ROW_ID");
        reg.setRowIdColumnType(SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_SDE);
        reg.alter();

        layer.setShapeTypes(SeLayer.SE_NIL_TYPE_MASK | SeLayer.SE_POINT_TYPE_MASK);
        layer.setGridSizes(1100.0, 0.0, 0.0);
        layer.setDescription("Layer Example");

        SeExtent ext = new SeExtent(0.0, 0.0, 10000.0, 10000.0);
        layer.setExtent(ext);

        /*
         * Define the layer's Coordinate Reference
         */
        SeCoordinateReference coordref = getGenericCoordRef();
        layer.setCoordRef(coordref);

        /*
         * Spatially enable the new table...
         */
        layer.setCreationKeyword(getConfigKeyword());
        layer.create(3, 4);

        // register the table as versioned
        SeRegistration registration = session.createSeRegistration(tableName);
        registration.setMultiVersion(true);
        registration.alter();

        return table;
    }

    public void insertIntoVersionedTable(final Session session,
            final SeState state,
            final String tableName,
            final String nameField) throws IOException {

        final SeInsert insert = session.createSeInsert();

        session.issue(new Command<Void>() {

            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                SeObjectId differencesId = new SeObjectId(SeState.SE_NULL_STATE_ID);
                insert.setState(state.getId(), differencesId, SeState.SE_STATE_DIFF_NOCHECK);

                insert.intoTable(tableName, new String[] { "NAME" });
                SeRow row = insert.getRowToSet();
                row.setString(0, "NAME 1");
                insert.execute();
                insert.close();
                return null;
            }
        });
    }
}
