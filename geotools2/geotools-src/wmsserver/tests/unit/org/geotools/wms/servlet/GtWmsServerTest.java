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
import org.geotools.feature.FeatureCollection;

public class GtWmsServerTest extends TestCase {
    static WMSServer server;
  
    private static Logger LOGGER = Logger.getLogger("org.geotools.wmsserver");
    
    public GtWmsServerTest(String name) {
        super(name);
        Geotools.init();
       
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(GtWmsServerTest.class);
    }
    private static boolean setup=false;
    public void setUp() throws Exception {
        if(setup) return;
        LOGGER.info("Running setup");
        setup=true;
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
            //WMSServlet temp = new WMSServlet();
            assertNotNull("GetCapabilites failed, reutrned null object",cap);
            Object usa = cap.getLayer("USA");
            assertNotNull("Layer USA missing from capabilites list",usa);
            Vector styles = cap.getAvailableStyles("USA");
            
            assertTrue("style 'normal' not found for layer USA", styles.contains("normal"));
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
    
    public void testBlueLakeData() {
        try {
            BufferedImage map = server.getMap(new String[] {"ShpBuildings"}, new String[] {"green"}, "EPSG:4326", new double[] {-0.004,-0.003,0.004,0.003}, 620, 400, false, null);
            ImageView view = new ImageView(map, "the map");
            view.setSize(500,500);
            view.createFrame();
            try{
                Thread.sleep(5000);
            }catch (InterruptedException e){}
            view.close();
            
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    }
    
    public void testGetMap() {
        try {
            BufferedImage map = server.getMap(new String[] {"USA"}, new String[] {"population"}, "EPSG:4326", new double[] {-130, 16, -60, 52}, 620, 400, false, null);
            ImageView view = new ImageView(map, "the map");
            view.createFrame();
            try{
                Thread.sleep(5000);
            }catch (InterruptedException e){}

            view.close();
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    }
    
    public void testGetMapTwice() {
        try {
            BufferedImage map = server.getMap(new String[] {"USA"}, new String[] {"population"}, "EPSG:4326", new double[] {-130, 16, -60, 52}, 620, 400, false, null);
            map = server.getMap(new String[] {"USA"}, new String[] {"population"}, "EPSG:4326", new double[] {-130, 18, -60, 50}, 620, 400, false, null);
            ImageView view = new ImageView(map, "testGetMapTwice");
            view.createFrame();
            try{
                Thread.sleep(5000);
            }catch (InterruptedException e){}

            view.close();
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    }
    public void testGetMapNullStyle() {
        try {
            BufferedImage map = server.getMap(new String[] {"USA"}, null, "EPSG:4326", new double[] {-130, 16, -60, 52}, 620, 400, false, null);
            ImageView view = new ImageView(map, "testGetMapNullStyle");
            view.createFrame();
            try{
                Thread.sleep(5000);
            }catch (InterruptedException e){}

            view.close();
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    }
    
     public void testGetMapNullStyleTwice() {
        try {
            BufferedImage map = server.getMap(new String[] {"USA"}, null, "EPSG:4326", new double[] {-130, 16, -60, 52}, 620, 400, false, null);
            map = server.getMap(new String[] {"USA"}, null, "EPSG:4326", new double[] {-130, 18, -60, 50}, 620, 400, false, null);
            ImageView view = new ImageView(map, "testGetMapNullStyleTwice");
            view.createFrame();
            try{
                Thread.sleep(5000);
            }catch (InterruptedException e){}

            view.close();
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    }
    
     public void testGetFeatureInfo() {
        try {
            FeatureCollection f = server.getFeatureInfo(new String[] {"USA"}, "EPSG:4326", new double[] {-130, 17, -60, 52}, 600, 300,1 , 300, 150);
            Feature[] features = (Feature[]) f.toArray(new Feature[f.size()]);
            assertNotNull("No features returned", features);
            assertEquals("Wrong number of features found",1,features.length);
            Object atrib[] = features[0].getAttributes(new Object[features[0].getNumberOfAttributes()]);
            assertEquals("Oklahoma",atrib[1].toString());
            f = server.getFeatureInfo(new String[] {"USA"}, "EPSG:4326", new double[] {-130, 17, -60, 52}, 600, 300,1 , 210, 150);
            features = (Feature[]) f.toArray(new Feature[f.size()]);
            assertNotNull("No features returned", features);
            assertEquals("Wrong number of features found",1,features.length);
            atrib = features[0].getAttributes(new Object[features[0].getNumberOfAttributes()]);
            assertEquals("New Mexico",atrib[1].toString());
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

