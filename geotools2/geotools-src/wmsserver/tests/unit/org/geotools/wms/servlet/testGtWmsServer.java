package org.geotools.wms.servlet;

import junit.framework.*;

import org.geotools.wms.*;
import org.geotools.wms.gtserver.*;

import java.awt.image.*;
import java.io.*;
import java.util.Properties;

public class testGtWmsServer extends TestCase {
    WMSServer server;
    
    public testGtWmsServer(String name) {
        super(name);
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(testGtWmsServer.class);
    }
    
    public void setUp() throws Exception {
        
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        System.out.println("looking for "+dataFolder+"/layers.xml");
        File f = new File(dataFolder,"layers.xml");
        System.setProperty("user.dir",dataFolder);
        server = new GtWmsServer();
        Properties props = new Properties();
        props.setProperty("layersxml",f.toString());
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
    
    public void testGetMap() {
        try {
            BufferedImage map = server.getMap(new String[] {"first"}, null, "EPSG:4326", new double[] {-130, 20, -60, 50}, 320, 200, false, null);
            ImageView view = new ImageView(map, "the map");
            view.createFrame();
        }
        catch(WMSException wmsexp) {
            fail("WMSException : "+wmsexp.getMessage());
        }
    }
}

