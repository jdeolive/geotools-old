/**
 * 
 */
package org.geotools.arcsde;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.geotools.arcsde.data.ArcSDEDataStore;
import org.geotools.arcsde.data.TestData;
import org.geotools.arcsde.session.ArcSDEConnectionConfig;
import org.geotools.arcsde.session.ISession;
import org.geotools.data.DataAccessFactory;
import org.geotools.data.DataAccessFinder;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.DataAccessFactory.Param;
import org.geotools.factory.GeoTools;
import org.geotools.util.logging.Logging;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author groldan
 * 
 */
public class ArcSDEJNDIDataStoreFactoryTest {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde");

    private static ArcSDEJNDIDataStoreFactory factory;

    private static TestData testData;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        factory = new ArcSDEJNDIDataStoreFactory();
        setupJNDIEnvironment();
        testData = new TestData();
        testData.setUp();
        testData.getConProps();
    }

    @Test
    public void testDataStoreFinderFindsIt() throws IOException {
        Iterator<DataStoreFactorySpi> allFactories = DataStoreFinder.getAllDataStores();
        ArcSDEJNDIDataStoreFactory sdeFac = null;
        while (allFactories.hasNext()) {
            DataAccessFactory next = allFactories.next();
            if (next instanceof ArcSDEJNDIDataStoreFactory) {
                sdeFac = (ArcSDEJNDIDataStoreFactory) next;
                break;
            }
        }
        assertNotNull(sdeFac);
    }

    @Test
    public void testDataAccessFinderFindsIt() throws IOException {
        Iterator<DataAccessFactory> allFactories = DataAccessFinder.getAllDataStores();
        ArcSDEJNDIDataStoreFactory sdeFac = null;
        while (allFactories.hasNext()) {
            DataAccessFactory next = allFactories.next();
            if (next instanceof ArcSDEJNDIDataStoreFactory) {
                sdeFac = (ArcSDEJNDIDataStoreFactory) next;
                break;
            }
        }
        assertNotNull(sdeFac);
    }

    /**
     * Test method for {@link ArcSDEJNDIDataStoreFactory#createDataStore(java.util.Map)}.
     * 
     * @throws IOException
     */
    @Test
    public void testCreateDataStore() throws IOException {
        String jndiRef = "java:comp/env/MyArcSdeResource";
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        params.put(ArcSDEJNDIDataStoreFactory.JNDI_REFNAME.key, jndiRef);

        ArcSDEConnectionConfig config = testData.getConnectionPool().getConfig();
        try {
            InitialContext initialContext = GeoTools.getInitialContext(GeoTools.getDefaultHints());
            initialContext.createSubcontext("java:comp/env").bind(jndiRef, config);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        ArcSDEDataStore dataStore = (ArcSDEDataStore) factory.createDataStore(params);
        assertNotNull(dataStore);
        ISession session = dataStore.getSession(Transaction.AUTO_COMMIT);
        assertNotNull(session);
        try {
            assertEquals(config.getUserName().toUpperCase(), session.getUser().toUpperCase());
        } finally {
            session.dispose();
        }
    }

    /**
     * Test method for {@link ArcSDEJNDIDataStoreFactory#canProcess(java.util.Map)}.
     */
    @Test
    public void testCanProcess() {
        assertFalse(factory.canProcess(null));
        Map<String, Serializable> params = new HashMap<String, Serializable>();
        assertFalse(factory.canProcess(params));
        String jndiRef = "java:comp/env/MyArcSdeResource";
        params.put(ArcSDEJNDIDataStoreFactory.JNDI_REFNAME.key, jndiRef);
        assertTrue(factory.canProcess(params));
    }

    /**
     * Test method for {@link ArcSDEJNDIDataStoreFactory#getParametersInfo()}.
     */
    @Test
    public void testGetParametersInfo() {
        Param[] parametersInfo = factory.getParametersInfo();
        assertNotNull(parametersInfo);
        assertEquals(4, parametersInfo.length);
        assertSame(ArcSDEJNDIDataStoreFactory.JNDI_REFNAME, parametersInfo[0]);
        assertSame(ArcSDEDataStoreFactory.NAMESPACE_PARAM, parametersInfo[1]);
        assertSame(ArcSDEDataStoreFactory.VERSION_PARAM, parametersInfo[2]);
        assertSame(ArcSDEDataStoreFactory.ALLOW_NON_SPATIAL_PARAM, parametersInfo[3]);
    }

    /**
     * Test method for {@link ArcSDEJNDIDataStoreFactory#createNewDataStore(java.util.Map)}.
     * 
     * @throws IOException
     */
    @Test
    public void testCreateNewDataStore() throws IOException {
        try {
            factory.createNewDataStore(new HashMap<String, Serializable>());
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            assertTrue(true);
        }
    }

    private static void setupJNDIEnvironment() throws IOException {

        File jndi = new File("target/jndi");
        jndi.mkdirs();

        final String IC_FACTORY_PROPERTY = "java.naming.factory.initial";
        final String JNDI_ROOT = "org.osjava.sj.root";
        final String JNDI_DELIM = "org.osjava.jndi.delimiter";

        if (System.getProperty(IC_FACTORY_PROPERTY) == null) {
            System.setProperty(IC_FACTORY_PROPERTY, "org.osjava.sj.SimpleContextFactory");
        }

        if (System.getProperty(JNDI_ROOT) == null) {
            System.setProperty(JNDI_ROOT, jndi.getAbsolutePath());
        }

        if (System.getProperty(JNDI_DELIM) == null) {
            System.setProperty(JNDI_DELIM, "/");
        }

        LOGGER.fine(IC_FACTORY_PROPERTY + " = " + System.getProperty(IC_FACTORY_PROPERTY));
        LOGGER.fine(JNDI_ROOT + " = " + System.getProperty(JNDI_ROOT));
        LOGGER.fine(JNDI_DELIM + " = " + System.getProperty(JNDI_DELIM));
    }

}
