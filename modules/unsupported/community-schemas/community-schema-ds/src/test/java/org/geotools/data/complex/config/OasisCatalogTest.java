package org.geotools.data.complex.config;

import java.net.URL;

import junit.framework.TestCase;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.tools.ResolvingXMLReader;
import org.geotools.test.TestData;

public class OasisCatalogTest extends TestCase {
    org.apache.xml.resolver.Catalog catalog;
    
    protected void setUp() throws Exception {
        super.setUp();
        catalog = new Catalog();
        catalog.setupReaders();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testParseCatalog() throws Exception{
        URL file = TestData.url(this, "commonSchemas_new.oasis.xml");

        ResolvingXMLReader reader = new ResolvingXMLReader();
        Catalog catalog = reader.getCatalog();
        catalog.getCatalogManager().setVerbosity(9);

        catalog.parseCatalog(file);
        
        final URL baseUri = new URL("http://schemas.opengis.net/gml/");
        //the system override defined in the catalog
        final URL override = new URL("file:///schemas/gml/trunk/gml/");
        
        final String extraPath = "3.1.1/basicTypes.xsd";
        final String uri = new URL(baseUri, extraPath).toExternalForm();
        final String expected = new URL(override, extraPath).toExternalForm();

        String resolved = catalog.resolveSystem(uri);
        assertNotNull(resolved);

        final String actual = new URL(resolved).toExternalForm();        
        assertEquals(expected, actual);
    }
}
