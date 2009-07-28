/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.data.ws.v1_1_0;
import static org.geotools.data.ws.v1_1_0.DataTestSupport.createTestProtocol;
import static org.geotools.data.ws.v1_1_0.DataTestSupport.wfs;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.SchemaNotFoundException;
import org.geotools.data.Transaction;
import org.geotools.data.ws.XmlDataStore;
import org.geotools.data.ws.WSDataStoreFactory;
import org.geotools.data.ws.WSDataStoreFactory.WSFactoryParam;
import org.geotools.data.ws.v1_1_0.WS_DataStore;
import org.geotools.data.ws.v1_1_0.DataTestSupport.TestHttpProtocol;
import org.geotools.data.ws.v1_1_0.DataTestSupport.TestHttpResponse;
import org.geotools.test.TestData;
import org.jdom.Document;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;

/**
 * Unit test suite for {@link WS_DataStore}
 * 
 * @author Gabriel Roldan
 * @version $Id$
 * @since 2.5.x
 * @source $URL:
 *         http://gtsvn.refractions.net/trunk/modules/plugin/wfs/src/test/java/org/geotools/data
 *         /wfs/v1_1_0/WFSDataStoreTest.java $
 */
@SuppressWarnings("nls")
public class WS_DataStoreTest {
    private static final String GSMLNS = "http://www.cgi-iugs.org/xml/GeoSciML/2";

    /**
     * Test method for {@link WS_DataStore#getTypeNames()}.
     * 
     * @throws IOException
     */
    @Test
    public void testCreate() throws IOException {
        
        Map<Object, Object> properties = new HashMap<Object, Object>();
        WSDataStoreFactory dsf = new WSDataStoreFactory();
        properties.put("WSDataStoreFactory:GET_CONNECTION_URL", "http://d00109:8080/xaware/XADocSoapServlet");     
        properties.put("WSDataStoreFactory:PROTOCOL", true);
        properties.put("WSDataStoreFactory:TIMEOUT", new Integer(30000));
        properties.put("WSDataStoreFactory:TEMPLATE_DIRECTORY", new String(".\\src\\test\\resources\\org\\geotools\\ws\\"));
        properties.put("WSDataStoreFactory:TEMPLATE_NAME", "request.ftl");
        properties.put("WSDataStoreFactory:DEFAULT_SRS", "urn:x-ogc:def:crs:EPSG:4283");
        properties.put("WSDataStoreFactory:CAPABILITIES_FILE_LOCATION", ".\\src\\test\\resources\\org\\geotools\\ws\\ws_capabilities_equals_removed.xml");

        XmlDataStore ds = dsf.createDataStore(properties); 
    //    Document doc = ds.getXmlReader(namedQuery(Filter.INCLUDE,
     //               new Integer(5)), null);
    }
    private DefaultQuery namedQuery(Filter filter, int count) {
        DefaultQuery namedQuery = null;
        try {
            namedQuery = new DefaultQuery("MappedFeature", new URI(GSMLNS), filter, count,
                new String[] {}, "tom");
        } catch (Exception e) {
            System.out.println(e);
        }
        
        return namedQuery;
    }
    /**
     * Test method for {@link WS_DataStore#getTypeNames()}.
     * 
     * @throws IOException
     */
 //   @Test
 /*   public void testGetTypeNames() throws IOException {
        String[] expected = {"gubs:GovernmentalUnitCE", "gubs:GovernmentalUnitMCD",
                "gubs:GovernmentalUnitST", "hyd:HydroElementARHI", "hyd:HydroElementARMD",
                "hyd:HydroElementFLHI", "hyd:HydroElementFLMD", "hyd:HydroElementLIHI",
                "hyd:HydroElementLIMD", "hyd:HydroElementPTHI", "hyd:HydroElementPTMD",
                "hyd:HydroElementWBHI", "hyd:HydroElementWBMD", "trans:RoadSeg"};
        List<String> expectedTypeNames = Arrays.asList(expected);

        createTestProtocol(null);

        WS_DataStore ds = new WS_DataStore(wfs);

        String[] typeNames = ds.getTypeNames();
        assertNotNull(typeNames);
        List<String> names = Arrays.asList(typeNames);
        assertEquals(expectedTypeNames.size(), names.size());
        assertEquals(expectedTypeNames, names);
    }*/

    /**
     * Test method for
     * {@link org.geotools.wfs.WS_DataStore.data.WFS_1_1_0_DataStore#getSchema(java.lang.String)}.
     * 
     * @throws IOException
     */
/*    @Test
    public void testGetSchema() throws IOException {
        final InputStream schemaStream = TestData.openStream(this, null);
        TestHttpResponse httpResponse = new TestHttpResponse("", "UTF-8", schemaStream);
        TestHttpProtocol mockHttp = new TestHttpProtocol(httpResponse);
        createTestProtocol(null, mockHttp);

        // override the describe feature type url so it loads from the test resource
        URL describeUrl = TestData.getResource(this, null);
        wfs.setDescribeFeatureTypeURLOverride(describeUrl);

        WS_DataStore ds = new WS_DataStore(wfs);

        try {
            ds.getSchema("nonExistentTypeName");
            fail("Expected SchemaNotFoundException");
        } catch (SchemaNotFoundException e) {
            assertTrue(true);
        }

        SimpleFeatureType schema = ds.getSchema("");
        assertNotNull(schema);
    }
*/
//    @Test
//       public void tesGetFeatureReader() throws IOException {
//        final InputStream dataStream = TestData.openStream(this, null);
//        TestHttpResponse httpResponse = new TestHttpResponse("text/xml; subtype=gml/3.1.1",
//                "UTF-8", dataStream);
//        TestHttpProtocol mockHttp = new TestHttpProtocol(httpResponse);
//        createTestProtocol(null, mockHttp);
//
//        // override the describe feature type url so it loads from the test resource
//        URL describeUrl = TestData.getResource(this, null);
//        wfs.setDescribeFeatureTypeURLOverride(describeUrl);
//
//        WS_DataStore ds = new WS_DataStore(wfs);
//        DefaultQuery query = new DefaultQuery("");
//        FeatureReader<SimpleFeatureType, SimpleFeature> featureReader;
//        featureReader = ds.getFeatureReader(query, Transaction.AUTO_COMMIT);
//        assertNotNull(featureReader);
//        // test data file contains three features...
//        assertTrue(featureReader.hasNext());
//        assertNotNull(featureReader.next());
//
//        assertTrue(featureReader.hasNext());
//        assertNotNull(featureReader.next());
//
//        assertTrue(featureReader.hasNext());
//        assertNotNull(featureReader.next());
//
//        assertFalse(featureReader.hasNext());
//    }
}
