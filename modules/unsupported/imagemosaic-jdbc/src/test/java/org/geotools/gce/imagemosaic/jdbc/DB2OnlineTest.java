/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gce.imagemosaic.jdbc;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import java.net.URL;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.sql.Types;


public class DB2OnlineTest extends AbstractTest {
    static DBDialect dialect = null;

    public DB2OnlineTest(String test) {
        super(test);
    }

    @Override
    protected String getSrsId() {
        return "WGS84_SRS_1003";
    }

    public static Test suite() {
        TestSuite suite = new TestSuite();

        DB2OnlineTest test = new DB2OnlineTest("");

        if (test.checkPreConditions() == false) {
            return suite;
        }
        suite.addTest(new DB2OnlineTest("testScripts"));
        suite.addTest(new DB2OnlineTest("testGetConnection"));
        suite.addTest(new DB2OnlineTest("testDrop"));
        suite.addTest(new DB2OnlineTest("testCreate"));
        suite.addTest(new DB2OnlineTest("testImage1"));
        suite.addTest(new DB2OnlineTest("testFullExtent"));
        suite.addTest(new DB2OnlineTest("testNoData"));
        suite.addTest(new DB2OnlineTest("testPartial"));
        suite.addTest(new DB2OnlineTest("testVienna"));
        suite.addTest(new DB2OnlineTest("testViennaEnv"));
        suite.addTest(new DB2OnlineTest("testDrop"));
        suite.addTest(new DB2OnlineTest("testCreateJoined"));
        suite.addTest(new DB2OnlineTest("testImage1Joined"));
        suite.addTest(new DB2OnlineTest("testFullExtentJoined"));
        suite.addTest(new DB2OnlineTest("testNoDataJoined"));
        suite.addTest(new DB2OnlineTest("testPartialJoined"));
        suite.addTest(new DB2OnlineTest("testViennaJoined"));
        suite.addTest(new DB2OnlineTest("testViennaEnvJoined"));
        suite.addTest(new DB2OnlineTest("testDrop"));
        suite.addTest(new DB2OnlineTest("testCloseConnection"));
        

        return suite;
    }

    @Override
    protected String getSubDir() {
        return "db2";
    }

    @Override
    public String getConfigUrl() {
        return "file:target/resources/oek.db2.xml";
    }

    @Override
    protected DBDialect getDBDialect() {
        if (dialect != null) {
            return dialect;
        }

        Config config = null;

        try {
            config = Config.readFrom(new URL(getConfigUrl()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        dialect = DBDialect.getDBDialect(config);

        return dialect;
    }

    public void testGetConnection() {
        super.testGetConnection();

        try {
            CallableStatement s = Connection.prepareCall(
                    " {call db2gse.ST_enable_db(?,?,?) }");
            s.registerOutParameter(2, Types.INTEGER);
            s.registerOutParameter(3, Types.CHAR);
            s.setNull(1, Types.CHAR);
            s.executeUpdate();
            System.out.println(s.getInt(2) + "|" + s.getString(3));
        } catch (SQLException e) {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    void executeRegister(String stmt) throws SQLException {
        String s = "{" + stmt + "}";
        CallableStatement ps = Connection.prepareCall(s);
        ps.registerOutParameter(1, Types.INTEGER);
        ps.registerOutParameter(2, Types.CHAR);
        ps.executeUpdate();
    }

    void executeUnRegister(String stmt) throws SQLException {
        String s = "{" + stmt + "}";
        CallableStatement ps = Connection.prepareCall(s);
        ps.registerOutParameter(1, Types.INTEGER);
        ps.registerOutParameter(2, Types.CHAR);
        ps.executeUpdate();
    }
}
