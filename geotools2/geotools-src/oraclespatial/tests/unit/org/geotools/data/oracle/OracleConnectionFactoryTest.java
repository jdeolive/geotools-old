/* $Id: OracleConnectionFactoryTest.java,v 1.2 2003/08/15 00:42:02 seangeo Exp $
 *
 * Created on 4/08/2003
 */
package org.geotools.data.oracle;

import junit.framework.TestCase;
import org.geotools.data.jdbc.ConnectionPool;
import org.geotools.data.jdbc.ConnectionPoolManager;

import java.io.FileInputStream;
import java.util.Properties;


/**
 * Test the Connection poolings
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: OracleConnectionFactoryTest.java,v 1.2 2003/08/15 00:42:02 seangeo Exp $ Last Modified: $Date: 2003/08/15 00:42:02 $
 */
public class OracleConnectionFactoryTest extends TestCase {
    /** The Oracle driver class name */
    private static final String JDBC_DRIVER = "oracle.jdbc.driver.OracleDriver";
    private Properties properties;

    /**
     * Creates a new OracleConnectionFactory Test.
     *
     * @throws ClassNotFoundException If the driver cannot be found
     */
    public OracleConnectionFactoryTest() throws ClassNotFoundException {
        super();
        Class.forName(JDBC_DRIVER);
    }

    /**
     * Creates a new OracleConnectionFactory Test.
     *
     * @param arg0 name of the test
     *
     * @throws ClassNotFoundException If the Oracle Driver cannot be found
     */
    public OracleConnectionFactoryTest(String arg0) throws ClassNotFoundException {
        super(arg0);
        Class.forName(JDBC_DRIVER);
    }

    /**
     * Loads the properties
     *
     * @throws Exception
     *
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        properties = new Properties();
        properties.load(new FileInputStream("test.properties"));
    }

    /**
     * Removes the properties
     *
     * @throws Exception
     *
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
        ConnectionPoolManager manager = ConnectionPoolManager.getInstance();
        manager.closeAll();
        properties = null;
    }

    public void testGetConnection() throws Exception {
        OracleConnectionFactory cFact = new OracleConnectionFactory(properties.getProperty("host"),
                properties.getProperty("port"), properties.getProperty("instance"));
        cFact.setLogin(properties.getProperty("user"), properties.getProperty("passwd"));

        // check that two connection pools from the same fact are the same
        ConnectionPool pool1 = cFact.getConnectionPool();
        ConnectionPool pool2 = cFact.getConnectionPool();
        assertTrue("Connection pool was not equal", pool1 == pool2);

        // check that tow connection pools using the same url,user,pass but
        // from different factories are the same
        OracleConnectionFactory cFact2 = new OracleConnectionFactory(properties.getProperty("host"),
                properties.getProperty("port"), properties.getProperty("instance"));
        cFact2.setLogin(properties.getProperty("user"), properties.getProperty("passwd"));
        ConnectionPool pool3 = cFact2.getConnectionPool();
        assertTrue("New factory returned different pool", pool1 == pool3);
    }
}
