/* $Id: ServiceTest.java,v 1.1 2003/08/08 07:33:04 seangeo Exp $
 *
 * Created on 4/08/2003
 */
package org.geotools.data.oracle;

import junit.framework.TestCase;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceFinder;
import java.io.FileInputStream;
import java.util.Properties;


/**
 * Tests the DataSourceFinder mechanism.
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: ServiceTest.java,v 1.1 2003/08/08 07:33:04 seangeo Exp $ Last Modified: $Date: 2003/08/08 07:33:04 $
 */
public class ServiceTest extends TestCase {
    private Properties properties;

    /**
     * Constructor for ServiceTest.
     *
     * @param arg0
     */
    public ServiceTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        properties = new Properties();
        properties.load(new FileInputStream("test.properties"));
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testService() throws Exception {
        DataSource ds = DataSourceFinder.getDataSource(properties);
        assertEquals(OracleDataSource.class, ds.getClass());
    }
}
