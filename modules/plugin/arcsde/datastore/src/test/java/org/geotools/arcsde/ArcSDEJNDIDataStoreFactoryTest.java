/**
 * 
 */
package org.geotools.arcsde;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.naming.NamingException;

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

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        factory = new ArcSDEJNDIDataStoreFactory();
        setupJNDIEnvironment();
    }

    /**
     * Test method for {@link ArcSDEJNDIDataStoreFactory#createDataStore(java.util.Map)}.
     */
    @Test
    public void testCreateDataStore() {
        fail("Not yet implemented");
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

        try {
            // bind any object to the lookup name, canProcess only checks there's something there
            GeoTools.getInitialContext(GeoTools.getDefaultHints()).bind(jndiRef,
                    "AnythingNonNullIsEnoughForCanProcess");
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        assertTrue(factory.canProcess(params));
    }

    /**
     * Test method for {@link ArcSDEJNDIDataStoreFactory#getParametersInfo()}.
     */
    @Test
    public void testGetParametersInfo() {
        Param[] parametersInfo = factory.getParametersInfo();
        assertNotNull(parametersInfo);
        assertEquals(1, parametersInfo.length);
        assertEquals(ArcSDEJNDIDataStoreFactory.JNDI_REFNAME, parametersInfo[0]);
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
