package org.geotools;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.net.URL;

import org.geotools.data.DataUtilities;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;

/**
 * Check GMLEncoder abilities
 */
public class GMLEncoderTest {
    /**
     * Check if we can encode a SimpleFeatureType using GML2
     */
    @Test
    public void testEncodeGML2XSD() throws Exception {
        SimpleFeatureType TYPE = DataUtilities.createType("location","geom:Point,name:String");
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GMLEncoder encode = new GMLEncoder( out );
        encode.setBaseURL( new URL("http://localhost/"));
        encode.setGML("gml2");
        
        encode.encode( TYPE );
        
        out.close();
        
        String xsd = out.toString();
        
        System.out.println( xsd );
        assertNotNull( xsd );

    }
    
    @Test
    public void testEncodeGML3XSD() throws Exception {
        SimpleFeatureType TYPE = DataUtilities.createType("location","geom:Point,name:String");
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GMLEncoder encode = new GMLEncoder( out );
        encode.setBaseURL( new URL("http://localhost/"));
        encode.setGML("gml3");
        
        encode.encode( TYPE );
        
        out.close();
        
        String xsd = out.toString();
        
        System.out.println( xsd );
        assertNotNull( xsd );

    }
}
