/*
 * FeatureTableModelTest.java
 * JUnit based test
 *
 * Created on March 18, 2002, 4:24 PM
 */                

package org.geotools.swinggui.tables;

import junit.framework.*;
import org.geotools.datasource.extents.*;
import org.geotools.datasource.*;
import org.geotools.featuretable.*;
import org.geotools.shapefile.*;
import com.vividsolutions.jts.geom.*;

import java.io.*;
import java.net.*;

import javax.swing.*;

/**
 *
 * @author jamesm
 */                                
public class FeatureTableModelTest extends TestCase {
    
    public FeatureTableModelTest(java.lang.String testName) {
        super(testName);
    }        
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(FeatureTableModelTest.class);
        return suite;
    }

    public void testDisplay() {
        String dataFolder = System.getProperty("dataFolder");
        try{
            URL url = new URL("file:///"+dataFolder+"/statepop.shp");
            System.out.println("Testing ability to load "+url);
            Shapefile shapefile = new Shapefile(url);
            ShapefileDataSource datasource = new ShapefileDataSource(shapefile);
            DefaultFeatureTable table = new DefaultFeatureTable();
            table.setDataSource(datasource);
            EnvelopeExtent r = new EnvelopeExtent();
            r.setBounds(new Envelope(-180, 180, -90, 90));
            table.getFeatures(r);
            FeatureTableModel ftm = new FeatureTableModel();
            ftm.setFeatureTable(table);
            
            JFrame frame = new JFrame();
            JTable jtable = new JTable();
            jtable.setModel(ftm);
            JScrollPane scroll = new JScrollPane(jtable);
            frame.getContentPane().add(scroll,"Center");
            frame.setVisible(true);
            
        }
        catch(Exception ioe){
            System.out.println(ioe);
            ioe.printStackTrace();
            fail("Load failed because of exception "+ioe.toString());
        }
    }


}
