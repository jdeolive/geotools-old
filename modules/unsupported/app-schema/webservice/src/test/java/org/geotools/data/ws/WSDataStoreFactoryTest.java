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
package org.geotools.data.ws;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.ws.XmlDataStore;
import org.geotools.data.ws.WSDataStoreFactory;
import org.geotools.data.ws.protocol.http.HTTPProtocol;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WSDataStoreFactoryTest {

    private static final String BASE_DIRECTORY = "./src/test/resources/test-data/";

    private WSDataStoreFactory dsf;

    private Map<String, Serializable> params;

    @Before
    public void setUp() throws Exception {
        dsf = new WSDataStoreFactory();
        params = new HashMap<String, Serializable>();
    }

    @After
    public void tearDown() throws Exception {
        dsf = null;
        params = null;
    }

    @Test
    public void testCanProcess() {
        // URL not set
        assertFalse(dsf.canProcess(params));

        params.put(WSDataStoreFactory.GET_CONNECTION_URL.key,
                "http://someserver.example.org/wfs?request=GetCapabilities");
        assertFalse(dsf.canProcess(params));

        params.put(WSDataStoreFactory.TEMPLATE_NAME.key, "request.ftl");
        assertFalse(dsf.canProcess(params));
        
        params.put(WSDataStoreFactory.TEMPLATE_DIRECTORY.key, "./src/test/resources/test-data");
        assertFalse(dsf.canProcess(params));
        
        params.put(WSDataStoreFactory.CAPABILITIES_FILE_LOCATION.key, "./src/test/resources/test-data/ws_capabilities.xml");
        assertTrue(dsf.canProcess(params));
        
        params.put(WSDataStoreFactory.USERNAME.key, "astroboy");
        assertFalse(dsf.canProcess(params));

        params.put(WSDataStoreFactory.PASSWORD.key, "secret");
        assertTrue(dsf.canProcess(params));
        
        params.put(WSDataStoreFactory.TIMEOUT.key, "30000");
        assertTrue(dsf.canProcess(params));       
    }

    @Test
    public void testCreateDataStoreWS() throws IOException {
        String capabilitiesFile;
        capabilitiesFile = "ws_capabilities_equals_removed.xml";
        testCreateDataStore_WS(capabilitiesFile);
    }

    private void testCreateDataStore_WS(final String capabilitiesFile) throws IOException {
        // override caps loading not to set up an http connection at all but to
        // load the test file
        final WSDataStoreFactory dsf = new WSDataStoreFactory() {
            @Override
            byte[] loadCapabilities(final URL capabilitiesUrl, HTTPProtocol htp) throws IOException {
                InputStream in = capabilitiesUrl.openStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                int aByte;
                while ((aByte = in.read()) != -1) {
                    out.write(aByte);
                }
                return out.toByteArray();
            }
        };
        Map<String, Serializable> params = new HashMap<String, Serializable>();
         
            
        File file = new File(BASE_DIRECTORY + capabilitiesFile);
        if (!file.exists()) {
            throw new IllegalArgumentException(capabilitiesFile + " not found");
        }    
        URL url = file.toURL();
       
        params.put(WSDataStoreFactory.GET_CONNECTION_URL.key, url);
        params.put(WSDataStoreFactory.TEMPLATE_DIRECTORY.key, BASE_DIRECTORY);
        params.put(WSDataStoreFactory.TEMPLATE_NAME.key, "request.ftl");
        params.put(WSDataStoreFactory.CAPABILITIES_FILE_LOCATION.key, BASE_DIRECTORY  + capabilitiesFile);

        XmlDataStore dataStore = dsf.createDataStore(params);
        assertTrue(dataStore instanceof WS_DataStore);
    }

    @SuppressWarnings("nls")
    @Test
    public void testCreateCapabilities() throws MalformedURLException, UnsupportedEncodingException {
        final String parametrizedUrl = "https://excise.pyr.ec.gc.ca:8081/cgi-bin/mapserv.exe?map=/LocalApps/Mapsurfer/PYRWQMP.map&service=WS&version=1.0.0&request=GetCapabilities";
        URL url = WSDataStoreFactory.createGetCapabilitiesRequest(new URL(parametrizedUrl));
        assertNotNull(url);
        assertEquals("https", url.getProtocol());
        assertEquals("excise.pyr.ec.gc.ca", url.getHost());
        assertEquals(8081, url.getPort());
        assertEquals("/cgi-bin/mapserv.exe", url.getPath());

        String query = url.getQuery();
        assertNotNull(query);
    }
}
