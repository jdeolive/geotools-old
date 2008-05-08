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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.arcsde.pool.Session;
import org.geotools.arcsde.pool.UnavailableArcSDEConnectionException;
import org.geotools.data.DataSourceException;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.esri.sde.sdk.client.SDEPoint;
import com.esri.sde.sdk.client.SeColumnDefinition;
import com.esri.sde.sdk.client.SeCoordinateReference;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeRow;
import com.esri.sde.sdk.client.SeShape;
import com.esri.sde.sdk.client.SeTable;

/**
 * Data setup and utilities for testing the support of in-process views
 * 
 * @author Gabriel Roldan, Axios Engineering
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/test/java/org/geotools/arcsde/data/InProcessViewSupportTestData.java $
 * @version $Id: InProcessViewSupportTestData.java 27989 2007-11-22 14:38:30Z
 *          groldan $
 * @since 2.4.x
 */
public class InProcessViewSupportTestData {

    private static final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(InProcessViewSupportTestData.class.getPackage().getName());

    public static final String MASTER_UNQUALIFIED = "GT_SDE_TEST_MASTER";

    public static final String CHILD_UNQUALIFIED = "GT_SDE_TEST_CHILD";

    private static CoordinateReferenceSystem testCrs;

    public static String MASTER;

    public static String CHILD;

    public static String masterChildSql;

    /**
     * Extra datastore creation parameters to set up {@link #typeName} as a
     * FeatureType defined by {@link #masterChildSql}
     */
    public static Map registerViewParams;

    public static final String typeName = "MasterChildTest";

    public static void setUp(Session session, TestData td) throws SeException, DataSourceException,
            UnavailableArcSDEConnectionException {

        testCrs = DefaultGeographicCRS.WGS84;

        /**
         * Remember, shape field has to be the last one
         */
        masterChildSql = "SELECT " + MASTER_UNQUALIFIED + ".ID, " + MASTER_UNQUALIFIED + ".NAME, "
                + CHILD_UNQUALIFIED + ".DESCRIPTION, " + MASTER_UNQUALIFIED + ".SHAPE " + "FROM "
                + MASTER_UNQUALIFIED + ", " + CHILD_UNQUALIFIED + " WHERE " + CHILD_UNQUALIFIED
                + ".MASTER_ID = " + MASTER_UNQUALIFIED + ".ID ORDER BY " + MASTER_UNQUALIFIED
                + ".ID";

        MASTER = session.getUser() + "." + MASTER_UNQUALIFIED;
        CHILD = session.getUser() + "." + CHILD_UNQUALIFIED;
        createMasterTable(session, td);
        createChildTable(session, td);

        registerViewParams = new HashMap();
        registerViewParams.put("sqlView.1.typeName", typeName);
        registerViewParams.put("sqlView.1.sqlQuery", masterChildSql);
    }

    private static void createMasterTable(Session session, TestData td) throws SeException,
            DataSourceException, UnavailableArcSDEConnectionException {
        SeTable table = session.createSeTable( MASTER);
        SeLayer layer = null;
        try {
            table.delete();
        } catch (SeException e) {
            // no-op, table didn't existed
        }

        SeColumnDefinition[] colDefs = new SeColumnDefinition[2];

        layer = session.createSeLayer();
        layer.setTableName(MASTER);

        colDefs[0] = new SeColumnDefinition("ID", SeColumnDefinition.TYPE_INT32, 10, 0, false);
        colDefs[1] = new SeColumnDefinition("NAME", SeColumnDefinition.TYPE_STRING, 255, 0, false);

        table.create(colDefs, td.getConfigKeyword());

        layer.setSpatialColumnName("SHAPE");
        layer.setShapeTypes(SeLayer.SE_POINT_TYPE_MASK);
        layer.setGridSizes(1100.0, 0.0, 0.0);
        layer.setDescription("Geotools sde pluing join support testing master table");
        SeCoordinateReference coordref = new SeCoordinateReference();
        coordref.setCoordSysByDescription(testCrs.toWKT());
        
        layer.setCreationKeyword(td.getConfigKeyword());
        layer.create(3, 4);

        insertMasterData(session, layer);
        LOGGER.info("successfully created master table " + layer.getQualifiedName());
    }

    private static void createChildTable(Session session, TestData td) throws DataSourceException,
            UnavailableArcSDEConnectionException, SeException {
        SeTable table = session.createSeTable(CHILD);
        try {
            table.delete();
        } catch (SeException e) {
            // no-op, table didn't existed
        }

        SeColumnDefinition[] colDefs = new SeColumnDefinition[4];

        colDefs[0] = new SeColumnDefinition("ID", SeColumnDefinition.TYPE_INTEGER, 10, 0, false);
        colDefs[1] = new SeColumnDefinition("MASTER_ID", SeColumnDefinition.TYPE_INTEGER, 10, 0,
                false);
        colDefs[2] = new SeColumnDefinition("NAME", SeColumnDefinition.TYPE_STRING, 255, 0, false);
        colDefs[3] = new SeColumnDefinition("DESCRIPTION", SeColumnDefinition.TYPE_STRING, 255, 0,
                false);

        table.create(colDefs, td.getConfigKeyword());

        /*
         * SeRegistration tableRegistration = new SeRegistration(conn, CHILD);
         * tableRegistration.setRowIdColumnType(SeRegistration.SE_REGISTRATION_ROW_ID_COLUMN_TYPE_USER);
         * tableRegistration.setRowIdColumnName("ID");
         * tableRegistration.alter();
         */
        insertChildData(session, table);

        LOGGER.info("successfully created child table " + CHILD);
    }

    /**
     * <pre>
     * <code>
     *  -----------------------------------------------
     *  |            GT_SDE_TEST_MASTER               |
     *  -----------------------------------------------
     *  |  ID(int)  | NAME (string)  | SHAPE (Point)  |
     *  -----------------------------------------------
     *  |     1     |   name1        |  POINT(1, 1)   |
     *  -----------------------------------------------
     *  |     2     |   name2        |  POINT(2, 2)   |
     *  -----------------------------------------------
     *  |     3     |   name3        |  POINT(3, 3)   |
     *  -----------------------------------------------
     * </code>
     * </pre>
     * 
     * @param session
     * @throws SeException
     * @throws Exception
     */
    private static void insertMasterData(Session session, SeLayer layer)
            throws SeException {
        SeInsert insert = null;

        SeCoordinateReference coordref = layer.getCoordRef();
        final String[] columns = { "ID", "NAME", "SHAPE" };

        for (int i = 1; i < 4; i++) {
            insert = session.createSeInsert();
            insert.intoTable(layer.getName(), columns);
            insert.setWriteMode(true);

            SeRow row = insert.getRowToSet();
            SeShape shape = new SeShape(coordref);
            SDEPoint[] points = { new SDEPoint(i, i) };
            shape.generatePoint(1, points);

            row.setInteger(0, Integer.valueOf(i));
            row.setString(1, "name" + i);
            row.setShape(2, shape);
            insert.execute();
        }
        session.commitTransaction();
    }

    /**
     * <pre>
     * <code>
     *  ---------------------------------------------------------------------
     *  |                     GT_SDE_TEST_CHILD                             |
     *  ---------------------------------------------------------------------
     *  | ID(int)   | MASTER_ID      | NAME (string)  | DESCRIPTION(string  |
     *  ---------------------------------------------------------------------
     *  |    1      |      1         |   child1       |    description1     |
     *  ---------------------------------------------------------------------
     *  |    2      |      2         |   child2       |    description2     |
     *  ---------------------------------------------------------------------
     *  |    3      |      2         |   child3       |    description3     |
     *  ---------------------------------------------------------------------
     *  |    4      |      3         |   child4       |    description4     |
     *  ---------------------------------------------------------------------
     *  |    5      |      3         |   child5       |    description5     |
     *  ---------------------------------------------------------------------
     *  |    6      |      3         |   child6       |    description6     |
     *  ---------------------------------------------------------------------
     *  |    7      |      3         |   child6       |    description7     | 
     *  ---------------------------------------------------------------------
     * </code>
     * </pre>
     * 
     * Note last row has the same name than child6, for testing group by.
     * 
     * @param session
     * @param table
     * @throws SeException
     * @throws Exception
     */
    private static void insertChildData(Session session, SeTable table)
            throws SeException {
        final String[] columns = { "ID", "MASTER_ID", "NAME", "DESCRIPTION" };

        int childId = 0;

        for (int master = 1; master < 4; master++) {
            for (int child = 0; child < master; child++) {
                childId++;

                SeInsert insert = session.createSeInsert();
                insert.intoTable(table.getName(), columns);
                insert.setWriteMode(true);

                SeRow row = insert.getRowToSet();

                row.setInteger(0, Integer.valueOf(childId));
                row.setInteger(1, Integer.valueOf(master));
                row.setString(2, "child" + (childId));
                row.setString(3, "description" + (childId));
                insert.execute();
                //insert.close();
            }
        }
        // add the 7th row to test group by
        SeInsert insert = session.createSeInsert();
        insert.intoTable(table.getName(), columns);
        insert.setWriteMode(true);
        SeRow row = insert.getRowToSet();

        row.setInteger(0, new Integer(7));
        row.setInteger(1, new Integer(3));
        row.setString(2, "child6");
        row.setString(3, "description7");
        insert.execute();
        //insert.close();
        session.commitTransaction();
    }
}
