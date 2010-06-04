package org.geotools;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;

import net.opengis.wfs.WfsFactory;

import org.geotools.GMLEncoder.Version;
import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.WKTReader2;
import org.geotools.wfs.WFS;
import org.geotools.xml.Encoder;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeatureType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.WKTReader;

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
        GMLEncoder encode = new GMLEncoder( out, Version.GML2 );
        encode.setBaseURL( new URL("http://localhost/"));
        encode.encode( TYPE );
        
        out.close();
        
        String xsd = out.toString();
        assertTrue( xsd.indexOf("gml/2.1.2/feature.xsd") != -1 );
        
    }
    @Test
    public void testEncodeGML2Legacy() throws Exception {
        SimpleFeatureType TYPE = DataUtilities.createType("location","geom:Point,name:String");
        
        SimpleFeatureCollection collection = FeatureCollections.newCollection("internal");
        WKTReader2 wkt = new WKTReader2();
        GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory( null );

        WKTReader reader = new WKTReader( geometryFactory );
        
        collection.add( SimpleFeatureBuilder.build( TYPE, new Object[]{ wkt.read("POINT (1 2)"), "name1"}, null) );
        collection.add( SimpleFeatureBuilder.build( TYPE, new Object[]{ wkt.read("POINT (4 4)"), "name2"}, null) );
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GMLEncoder encode = new GMLEncoder( out, Version.GML2 );
        encode.setNamespace("location", "http://localhost/location.xsd");
        encode.setLegacy(true);
        encode.encode( collection );
        
        out.close();
        
        String gml = out.toString();
        assertTrue( gml.indexOf("<gml:Point>") != -1);
    }
    
    public void testEncodeGML2() throws Exception {
        // step one write out xsd file
        SimpleFeatureType TYPE = DataUtilities.createType("location","geom:Point,name:String");
        File locationFile = new File("location.xsd");
        locationFile = locationFile.getCanonicalFile();
        
        locationFile.deleteOnExit();
        if( locationFile.exists() ){
            locationFile.delete();
        }
        locationFile.createNewFile();
        
        URL locationURL = locationFile.toURI().toURL();
        URL baseURL = locationFile.getParentFile().toURI().toURL();
        
        FileOutputStream out = new FileOutputStream(locationFile);
        
        GMLEncoder encode = new GMLEncoder( out, Version.GML2 );
        encode.setBaseURL( baseURL );
        encode.setNamespace("location", locationURL.toExternalForm() );
        encode.encode( TYPE );
        
        out.close();
        
        SimpleFeatureCollection collection = FeatureCollections.newCollection("internal");
        WKTReader2 wkt = new WKTReader2();
        
        collection.add( SimpleFeatureBuilder.build( TYPE, new Object[]{ wkt.read("POINT (1 2)"), "name1"}, null) );
        collection.add( SimpleFeatureBuilder.build( TYPE, new Object[]{ wkt.read("POINT (4 4)"), "name2"}, null) );
        
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        GMLEncoder encode2 = new GMLEncoder( out2, Version.GML2 );
        encode2.setBaseURL( baseURL );
        encode2.setNamespace("location", "location.xsd" );
        encode2.encode( collection );
        
        out.close();
        
        String gml = out.toString();
        
        assertTrue( gml.indexOf("<gml:Point>") != -1);
    }
    
    @Test
    public void testEncodeWFS1_0FeatureCollection() throws Exception {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName( "feature" );
        tb.setNamespaceURI( "http://geotools.org");
        tb.add( "geometry", Point.class );
        tb.add( "name", String.class );
        
        SimpleFeatureType TYPE = tb.buildFeatureType();
        
        SimpleFeatureCollection collection = FeatureCollections.newCollection("internal");
        WKTReader2 wkt = new WKTReader2();
        collection.add( SimpleFeatureBuilder.build( TYPE, new Object[]{ wkt.read("POINT (1 2)"), "name1"}, null) );
        collection.add( SimpleFeatureBuilder.build( TYPE, new Object[]{ wkt.read("POINT (4 4)"), "name2"}, null) );
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        GMLEncoder encode = new GMLEncoder( out, Version.WFS1_0 );
        encode.setNamespace("geotools","http://geotools.org");
        
        encode.encode( collection );
        
        out.close();
        String gml = out.toString();
        assertTrue( gml.indexOf("<gml:Point>") != -1);
    }
    
    @Test
    public void testEncodeWFS1_1FeatureCollection() throws Exception {
        SimpleFeatureTypeBuilder tb = new SimpleFeatureTypeBuilder();
        tb.setName( "feature" );
        tb.setNamespaceURI( "http://geotools.org");
        tb.add( "geometry", Point.class );
        tb.add( "name", String.class );
        
        SimpleFeatureType TYPE = tb.buildFeatureType();
        
        SimpleFeatureCollection collection = FeatureCollections.newCollection("internal");
        WKTReader2 wkt = new WKTReader2();
        collection.add( SimpleFeatureBuilder.build( TYPE, new Object[]{ wkt.read("POINT (1 2)"), "name1"}, null) );
        collection.add( SimpleFeatureBuilder.build( TYPE, new Object[]{ wkt.read("POINT (4 4)"), "name2"}, null) );
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        GMLEncoder encode = new GMLEncoder( out, Version.WFS1_1 );
        encode.setNamespace("geotools","http://geotools.org");
        
        encode.encode( collection );
        
        out.close();
        String gml = out.toString();
        assertTrue( gml.indexOf("<gml:Point>") != -1);
    }
    
    @Test
    public void testEncodeGML3XSD() throws Exception {
        SimpleFeatureType TYPE = DataUtilities.createType("location","geom:Point,name:String");
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        GMLEncoder encode = new GMLEncoder( out, Version.GML3 );
        encode.setBaseURL( new URL("http://localhost/"));
        encode.setNamespace("location", "http://localhost/location.xsd");
        encode.encode( TYPE );
        
        out.close();
        
        String xsd = out.toString();
        
        assertTrue( xsd.indexOf("gml/3.1.1/base/gml.xsd") != -1 );

    }
}
