package org.geotools.wms.servlet;

import junit.framework.*;

import org.geotools.wms.*;
import org.geotools.wms.gtserver.*;
import org.geotools.resources.*;

import java.awt.image.*;
import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.logging.Level;

public class testGtWmsServer extends TestCase {
    WMSServer server;
  
    private static Logger LOGGER = Logger.getLogger("org.geotools.wmsserver");
    
    public testGtWmsServer(String name) {
        super(name);
        Geotools.init();
        Logger.getLogger("org.geotools.shapefile").setLevel(Level.ALL);
        LOGGER.setLevel(Level.FINE);
        LOGGER.info("test constructed");
       
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(testGtWmsServer.class);
    }
    
    public void setUp() throws Exception {
        
        //String dataFolder = System.getProperty("dataFolder");
        /*if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        System.out.println("looking for "+dataFolder+"/layers.xml");*/
        
        String path = this.getClass().getClassLoader().getResource("testData/layers.xml").getFile();
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
            System.out.println(temp.capabilitiesToXML(cap));
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    }
    
    public void testPostGIS() {
        try {
            BufferedImage map = server.getMap(new String[] {"postgistest"}, null, "EPSG:4326", new double[] {425000, 420000,430000, 440000}, 620, 400, false, null);
            ImageView view = new ImageView(map, "a map from postgis");
            view.createFrame();
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    }
    
    public void testGetMap() {
        try {
            BufferedImage map = server.getMap(new String[] {"first"}, new String[] {"population"}, "EPSG:4326", new double[] {-130, 16, -60, 52}, 620, 400, false, null);
            ImageView view = new ImageView(map, "the map");
            view.createFrame();
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    }
    
   
}

