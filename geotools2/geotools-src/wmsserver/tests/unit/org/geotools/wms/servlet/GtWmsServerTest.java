package org.geotools.wms.servlet;

import java.awt.Graphics;
import junit.framework.*;

import org.geotools.wms.*;
import org.geotools.wms.gtserver.*;
import org.geotools.resources.*;

import java.awt.image.*;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.geotools.feature.Feature;

public class GtWmsServerTest extends TestCase {
    WMSServer server;
  
    private static Logger LOGGER = Logger.getLogger("org.geotools.wmsserver");
    
    public GtWmsServerTest(String name) {
        super(name);
        //Geotools.init();
        Logger.getLogger("org.geotools.wmsserver").setLevel(Level.ALL);
        LOGGER.setLevel(Level.FINE);
        LOGGER.info("test constructed");
       
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(GtWmsServerTest.class);
    }
    
    public void setUp() throws Exception {
        
        //String dataFolder = System.getProperty("dataFolder");
        /*if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        System.out.println("looking for "+dataFolder+"/layers.xml");*/
        String path;
        URL tmp = this.getClass().getClassLoader().getResource("../src/webapp/layers.xml");
        if(tmp==null){
            //then we are being run by maven
            path = System.getProperty("basedir");
            path+="/src/webapp/layers.xml";
        }else{
            path = tmp.getFile();
        }
        
        File f = new File(path);
        System.setProperty("user.dir",f.getParent());
        server = new GtWmsServer();
        Properties props = new Properties();
        props.setProperty("layersxml",path);
        server.init(props);
    }
    
    public void testGetCapabilities() {
        try {
            Capabilities cap = server.getCapabilities();
            System.out.println(cap);
            WMSServlet temp = new WMSServlet();
            //System.out.println(temp.capabilitiesToXML(cap));
            Vector styles = cap.getAvailableStyles("USA");
            assertTrue("style 'normal' not found", styles.contains("normal"));
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    }
    
    /*public void testPostGIS() {
        try {
            BufferedImage map = server.getMap(new String[] {"postgistest"}, null, "EPSG:4326", new double[] {425000, 420000,430000, 440000}, 620, 400, false, null);
            ImageView view = new ImageView(map, "a map from postgis");
            view.createFrame();
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    }*/
    
    public void testGetMap() {
        try {
            BufferedImage map = server.getMap(new String[] {"USA"}, new String[] {"population"}, "EPSG:4326", new double[] {-130, 16, -60, 52}, 620, 400, false, null);
            ImageView view = new ImageView(map, "the map");
            view.createFrame();
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    }
    
     public void testGetFeatureInfo() {
        try {
            Feature[] features = server.getFeatureInfo(new String[] {"USA"}, "EPSG:4326", new double[] {-130, 16, -60, 52}, 620, 400,1 , 210, 250);
            assertNotNull("No features returned", features);
            assertEquals(1,features.length);
            Object atrib[] = features[0].getAttributes();
            assertEquals("New Mexico",atrib[1].toString());
            features = server.getFeatureInfo(new String[] {"USA"}, "EPSG:4326", new double[] {-130, 16, -60, 52}, 620, 400,1 , 210, 150);
            assertNotNull("No features returned", features);
            assertEquals(1,features.length);
            atrib = features[0].getAttributes();
            assertEquals("Wyoming",atrib[1].toString());
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    }
    
    /*public void testGetMapWithOSProjection() {
        try {
            BufferedImage map = server.getMap(new String[] {"1881"}, new String[] {"1881"}, "EPSG:4326", new double[] {54000, 7400, 650000, 1200000}, 620, 400, false, null);
            ImageView view = new ImageView(map, "the gb map");
            view.createFrame();
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    } */
    
   
}

