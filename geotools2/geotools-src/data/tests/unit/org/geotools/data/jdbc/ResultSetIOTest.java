/*
 * Created on 28/10/2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.data.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

import org.geotools.data.AttributeReader;
import org.geotools.data.Transaction;
import org.geotools.data.jdbc.JDBCDataStore.FeatureTypeInfo;
import org.geotools.data.jdbc.JDBCDataStore.QueryData;
import org.geotools.feature.AttributeType;
import org.geotools.feature.AttributeTypeFactory;
import org.geotools.feature.FeatureType;
import org.geotools.feature.FeatureTypeFactory;
import org.geotools.feature.IllegalAttributeException;

import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockResultSet;

/** Provides ...
 * 
 *  @author Sean Geoghegan, Defence Science and Technology Organisation.
 */
public class ResultSetIOTest extends TestCase {
    Connection conn = new MockConnection();
    JDBCDataStore.FeatureTypeInfo info;
    MockResultSet rs;

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        AttributeType[] atts =
            new AttributeType[] {
                AttributeTypeFactory.newAttributeType("Name", String.class),
                AttributeTypeFactory.newAttributeType("B", Boolean.class),
                AttributeTypeFactory.newAttributeType("DEC", Double.class),
                };
        FeatureType schema = FeatureTypeFactory.newFeatureType(atts, "TYPE");

        info = new FeatureTypeInfo("TYPE", "ID", schema);
        rs = createDataResultSet();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    private MockResultSet createDataResultSet() {
        MockResultSet data = new MockResultSet("data");
        data.setResultSetConcurrency(ResultSet.CONCUR_UPDATABLE);

        data.addColumn("ID");
        data.addColumn("Name");
        data.addColumn("B");
        data.addColumn("DEC");

        data.addRow(new Object[] { new Integer(1), "Feature 1", Boolean.TRUE, new Double(10.001)});
        data.addRow(new Object[] { new Integer(2), "Feature 2", Boolean.FALSE, new Double(20.002)});
        data.addRow(new Object[] { new Integer(3), "Feature 3", Boolean.TRUE, new Double(30.003)});

        System.out.println(data.getRowCount());
        return data;
    }

    public void testReadData() throws Exception {
        JDBCDataStore.QueryData queryData =
            new QueryData(info, conn, conn.createStatement(), rs, Transaction.AUTO_COMMIT);
        ResultSetAttributeIO reader =
            new ResultSetAttributeIO(info.getSchema().getAttributeTypes(), queryData, 2, 5);
        assertTrue(reader.hasNext());
        assertEquals(3, count(reader));
    }

    public void testUpdateData() throws Exception {
        JDBCDataStore.QueryData queryData =
            new QueryData(info, conn, conn.createStatement(), rs, Transaction.AUTO_COMMIT);
        ResultSetAttributeIO writer =
            new ResultSetAttributeIO(info.getSchema().getAttributeTypes(), queryData, 2, 5);

        assertTrue(writer.hasNext());
        writer.next();

        writer.write(0, "Changed Feature");
        rs.updateRow();

        assertEquals("Changed Feature", writer.read(0));

        while (writer.hasNext())
            writer.next();
        writer.next();

        int i = rs.getRow();
        writer.write(0, "New Feature");
        rs.insertRow();
        rs.moveToCurrentRow();
        System.out.println(rs);

        assertEquals("New Feature", rs.getString(2));
    }

    int count(AttributeReader reader) throws NoSuchElementException, IOException, IllegalAttributeException {
        int count = 0;

        try {
            while (reader.hasNext()) {
                reader.next();
                count++;
            }
        } finally {
            reader.close();
        }

        return count;
    }
}
