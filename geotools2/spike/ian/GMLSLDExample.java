/*
 * Example1.java
 *
 * Created on April 24, 2002, 3:08 PM
 */

package spike.ian;
import java.io.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.renderer.*;
import org.geotools.feature.*;
import org.geotools.map.*;
import org.geotools.data.*;

import org.geotools.datasource.extents.EnvelopeExtent;
import java.awt.event.*;
import java.awt.*;

import org.apache.log4j.Logger;
import org.apache.log4j.BasicConfigurator;


import org.geotools.styling.*;
/**
 *
 * @author  iant
 */
public class GMLSLDExample extends java.awt.Panel{
    EnvelopeExtent r = new EnvelopeExtent();
    //AWTRenderer renderer = new AWTRenderer();
    Java2DRenderer renderer = new Java2DRenderer();
    Map map = new DefaultMap();
    SLDStyle style;
    /** Creates a new instance of Example1 */
    public GMLSLDExample(String gmlFile, String sldFile){
        
        BasicConfigurator.configure();

        FeatureCollection ft = new FeatureCollectionDefault();
        
        
        try{
            
            java.net.URL gmlurl = new java.net.URL("file:///"+ System.getProperty("user.dir")+"/" + gmlFile);
            java.net.URL sldurl = new java.net.URL("file:///"+ System.getProperty("user.dir")+"/" + sldFile);
            System.out.println("Testing ability to load "+gmlurl.toString());
            DataSource datasource = null;
            try{
                 datasource = new org.geotools.gml.GMLDataSource(gmlurl);
            }catch (DataSourceException e){
                System.out.println(e.toString());
                return;
            }
            System.out.println("Testing ability to load "+sldurl.toString());
             style = new SLDStyle(sldurl);
            ft.setDataSource(datasource);
            //testGML7.gml -90.5485,16.2633  32.5485,34.2633
            r.setBounds(new Envelope(-95,37, 10, 54));

            r.setBounds(new Envelope(0,30,0,30));
            //  testGML7_1.gml 10.5485,16.2633  132.5485,34.2633     
            //r.setBounds(new Envelope(5,133, 17, 35));
            // testGML10 1002210.8176,193188.0372 1002218.8175,193298.0361
            // 1002210.8176,193188.0372 1002218.8175,193298.0361
           //r = new EnvelopeExtent(1002200, 1004250, 193150, 193300);
            //276753.650,183754.900 283431.940,191849.110
            //277971.000,185990.000 280016.000,185990.000 280028.000,188025.000 277964.000,188035.000 277971.000,185990.00
            //r.setBounds(new Envelope(277971.000,280016.000,185990.000,188025.000));
            //Feature[] features = table.getFeatures(r);
            //System.out.println("No features loaded = "+features.length);
        }catch(IOException ioe){
            System.out.println(ioe);
            ioe.printStackTrace();
            return;
        }
        
        
        
        
        
        
        try{
            ft.getFeatures(r);
        }catch (DataSourceException e){
            System.err.println("whoops - error reading features " + e);
            return;
        }
        
        
        map.addFeatureTable(ft,style);
        
        this.setSize(300,300);
        
        
    }
    public void paint(Graphics g){
        System.out.println("painting " + this.getBounds().toString());
        super.paint(g);
        renderer.setOutput(this.getGraphics(),this.getBounds());
        map.render(renderer,r.getBounds());//and finaly  try and draw it!
        System.out.println("done paint");
        
    }
    
    public void update(Graphics g){
        super.update(g);
        paint(g);
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length<1){
            System.err.println("Usage: GMLSLDExample gmlfile sldfile");
            return;
        }
        Frame f = new Frame();
        f.setSize(300,300);
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent e) { e.getWindow().dispose(); }
        });
        GMLSLDExample ex1 = new GMLSLDExample(args[0],args[1]);
        f.add(ex1);
        f.setVisible(true);
        
        
    }
    
}
