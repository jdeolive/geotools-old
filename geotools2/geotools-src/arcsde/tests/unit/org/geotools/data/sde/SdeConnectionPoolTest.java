package org.geotools.data.sde;

import com.esri.sde.sdk.client.*;
import junit.framework.*;
import org.geotools.data.*;
import java.util.*;
import java.util.logging.Logger;
import java.net.*;
import java.io.*;

/**
 * Tests de functionality of a pool of ArcSDE connection objects over a
 * live ArcSDE database
 *
 * @author Gabriel Roldán
 * @version $Id: SdeConnectionPoolTest.java,v 1.1 2004/02/02 18:35:31 groldan Exp $
 */
public class SdeConnectionPoolTest extends TestCase {

    private static Logger LOGGER = Logger.getLogger("org.geotools.data.sde");
    private Map connectionParameters;
    private SdeConnectionConfig sdeConnectionConfig = null;
    private SdeConnectionPool sdeConnectionPool = null;

    /**
     * Creates a new SdeConnectionPoolTest object.
     *
     * @param name DOCUMENT ME!
     */
    public SdeConnectionPoolTest(String name) {
        super(name);
    }

    /**
     * loads /testData/testparams.properties to get connection
     * parameters and sets up a SdeConnectionConfig with them for
     * tests to set up SdeConnectionPool's as requiered
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void setUp() throws Exception {
        super.setUp();
        Properties conProps = new Properties();

        String propsFile = "/testData/testparams.properties";

        URL conParamsSource = getClass().getResource(propsFile);
        LOGGER.info("loading connection parameters from " +
                    conParamsSource.toExternalForm());

        InputStream in = conParamsSource.openStream();

        if (in == null)
            throw new IllegalStateException(
              "cannot find test params: " + conParamsSource.toExternalForm());

        conProps.load(in);
        connectionParameters = conProps;
        //test that mandatory connection parameters are set
        try {
          sdeConnectionConfig = new SdeConnectionConfig(conProps);
        }
        catch (Exception ex) {
          throw new IllegalStateException(
          "No valid connection parameters found in " +
          conParamsSource.toExternalForm() + ": " + ex.getMessage());
        }
    }

    /**
     * closes the connection pool if it's still open
     *
     * @throws Exception DOCUMENT ME!
     */
    protected void tearDown() throws Exception {
        sdeConnectionConfig = null;
        if(sdeConnectionPool != null)
          sdeConnectionPool.close();
        sdeConnectionPool = null;
        super.tearDown();
    }

    /**
     * Sets up a new SdeConnectionPool with the connection parameters
     * stored in <code>connParams</code> and throws an exception
     * if something goes wrong
     *
     * @param connParams a set of connection parameters from where the
     * new SdeConnectionPool will connect to the SDE database and create
     * connections
     * @throws IllegalArgumentException if the set of connection parameters
     * are not propperly set
     * @throws NullPointerException if <code>connParams</code> is null
     * @throws DataSourceException if the pool can't create the connections
     * with the passed arguments (i.e. can't connect to SDE database)
     */
    private SdeConnectionPool createPool(Map connParams)
    throws IllegalArgumentException, NullPointerException, DataSourceException
    {
        this.sdeConnectionConfig = new SdeConnectionConfig(connParams);
        LOGGER.info("creating a new SdeConnectionPool with " +
                    sdeConnectionConfig);
        if(this.sdeConnectionPool != null)
        {
          LOGGER.info("pool already created, closing it");
          this.sdeConnectionPool.close();
        }
        this.sdeConnectionPool = new SdeConnectionPool(sdeConnectionConfig);
        LOGGER.info("pool created");
        return this.sdeConnectionPool;
    }
    /**
     * tests that the SdeConnectionPool can create as many connections
     * as specified by the <code>"pool.maxConnections"</code> parameter,
     * and no more than that
     *
     * @throws DataSourceException DOCUMENT ME!
     * @throws UnavailableConnectionException DOCUMENT ME!
     */
    public void testPoolPolicy()
        throws DataSourceException, UnavailableConnectionException {

        int MIN_CONNECTIONS = 2;
        int MAX_CONNECTIONS = 6;
        //override pool.minConnections and pool.maxConnections from
        //the configured parameters to test the connections' pool
        //availability
        Map params = new HashMap(this.connectionParameters);
        params.put(sdeConnectionConfig.MIN_CONNECTIONS_PARAM,
                   new Integer(MIN_CONNECTIONS));
        params.put(sdeConnectionConfig.MAX_CONNECTIONS_PARAM, new Integer(1));

        //this MUST fail, since maxConnections is lower than minConnections
        try {
          LOGGER.info("testing parameters' sanity check at pool creation time");
          createPool(params);
          fail("the connection pool creation should have failed since a wrong set of arguments was passed");
        }catch (IllegalArgumentException ex) {
          //it's ok, it is what's expected
          LOGGER.info("pramams assertion passed");
        }catch(Exception e){
          //any other kind of exception is wrong
          e.printStackTrace();
          super.fail(e.getMessage());
        }

        params.put(sdeConnectionConfig.MAX_CONNECTIONS_PARAM,
                   new Integer(MAX_CONNECTIONS));
        params.put(sdeConnectionConfig.CONNECTIONS_INCREMENT_PARAM, new Integer(1));
        createPool(params);

        //check that after creation, the pool contains the minimun number
        //of connections specified
        assertEquals("after creation, the pool must contain the minimun number of connections specified",
                     MIN_CONNECTIONS, this.sdeConnectionPool.getPoolSize());

        //try to get the maximun number of connections specified
        SeConnection []conns = new SeConnection[MAX_CONNECTIONS];
        for(int i = 0; i < MAX_CONNECTIONS; i++)
        {
          try {
            conns[i] = sdeConnectionPool.getConnection();
          }
          catch (UnavailableConnectionException ex) {
            fail(ex.getMessage());
          }
          catch (DataSourceException ex) {
            fail(ex.getMessage());
          }
        }
        //now that the max number of connections is reached, the pool
        //should throw an UnavailableConnectionException
        try {
          this.sdeConnectionPool.getConnection();
          fail("since the max number of connections was reached, the pool should have throwed an UnavailableConnectionException");
        }
        catch (UnavailableConnectionException ex) {
          LOGGER.info("maximun number of connections reached, got an UnavailableConnectionException, it's OK");
        }
        catch (Exception ex) {
          //any other exception is wrong
          fail(ex.getMessage());
        }

        //now, free one and check the same conection is returned on the
        //next call to getConnection()
        SeConnection expected = conns[0];
        this.sdeConnectionPool.release(expected);
        SeConnection conn = this.sdeConnectionPool.getConnection();
        assertEquals(expected, conn);
    }

    /**
     * DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public void testRefresh() throws DataSourceException {
        //sdeConnectionPool.refresh();

        /**
         * @todo fill in the test code
         */
    }

}
