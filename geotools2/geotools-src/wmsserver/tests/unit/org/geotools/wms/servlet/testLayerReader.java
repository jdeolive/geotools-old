package org.geotools.wms.servlet;

import junit.framework.*;

import org.geotools.wms.*;
import org.geotools.wms.gtserver.*;
import java.util.HashMap;
import java.io.*;

public class testLayerReader extends TestCase {
    LayerReader reader;
    
    public testLayerReader(String name) {
        super(name);
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(testLayerReader.class);
    }
    
    public void setUp() {
        reader = new LayerReader();
    }
    
    public void testParse() throws Exception {
        String dataFolder = System.getProperty("dataFolder");
        if(dataFolder==null){
            //then we are being run by maven
            dataFolder = System.getProperty("basedir");
            dataFolder+="/tests/unit/testData";
        }
        System.out.println("looking for "+dataFolder+"/layers.xml");
        File f = new File(dataFolder,"layers.xml");
        HashMap layers = reader.read(new FileInputStream(f));
        LayerEntry first = (LayerEntry) layers.get("first");
        assertEquals("first",first.id);
        assertEquals("maps/statepop.shp",first.properties.getProperty("filename"));
        assertEquals(3,layers.size());
        Object[] layerArr = layers.values().toArray();
        LayerEntry layer = null;
        for (int i=0;i<layerArr.length;i++){
            layer = (LayerEntry) layerArr[i];	    
          System.out.println("Layer : "+layer.id+" = "+ layer.description);
         }
        
    }
}

/*       LayerEntry [] layers = (reader.read(new FileInputStream(f)));
        assertEquals("first",layers[0].id);
        assertEquals("maps/statepop.shp",layers[0].properties.getProperty("filename"));
        assertEquals(1,layers.length);
        for (int i=0;i<layers.length;i++){
            System.out.println("Layer : "+layers[i].id+" = "+layers[i].description);
        }
        
    }
    }*/

