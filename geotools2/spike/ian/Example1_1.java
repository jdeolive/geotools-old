/*
 * Example1.java
 *
 * Created on April 24, 2002, 3:08 PM
 */

package spike.ian;
import java.io.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.renderer.*;
import org.geotools.featuretable.*;
import org.geotools.map.*;
import org.geotools.datasource.*;

import org.geotools.datasource.extents.EnvelopeExtent;
import java.awt.event.*;
import java.awt.*;



import org.geotools.styling.*;
/**
 *
 * @author  iant
 */
public class Example1_1 extends java.awt.Panel{
    EnvelopeExtent r = new EnvelopeExtent();
    //AWTRenderer renderer = new AWTRenderer();
    Java2DRenderer renderer = new Java2DRenderer();
    Map map = new DefaultMap();
    /** Creates a new instance of Example1 */
    public Example1_1(String uri){
        
        DefaultFeatureTable ft = new DefaultFeatureTable();
        
        String dataFolder = "d:\\ian\\Development\\geotools2\\geotools-src\\gmldatasource\\tests\\unit\\testData";
        
        try{
            System.out.println("datafolder " + dataFolder);
            java.net.URL url = new java.net.URL("file:///" + dataFolder + "/" + uri);
            System.out.println("Testing ability to load "+url.toString());
            DataSource datasource = null;
            try{
                 datasource = new org.geotools.gml.GMLDataSource(url);
            }catch (DataSourceException e){
                System.out.println(e.toString());
                return;
            }
            ft.setDataSource(datasource);
            //testGML7.gml -90.5485,16.2633  32.5485,34.2633
            r.setBounds(new Envelope(-95,33, 17, 54));
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
        
        
        //The following is complex, and should be built from
        //an SLD document and not by hand
        DefaultLineSymbolizer linesym = new DefaultLineSymbolizer();
        DefaultPolygonSymbolizer plsym = new DefaultPolygonSymbolizer();
        DefaultStroke stroke = new DefaultStroke();
        stroke.setDashArray(new float[]{5,3});
        stroke.setWidth(2);
        stroke.setOpacity(.4);
        
        linesym.setStroke(stroke);
        DefaultPolygonSymbolizer polysym = new DefaultPolygonSymbolizer();
        DefaultFill myFill = new DefaultFill();
        myFill.setColor("#ff0000");
        polysym.setFill(myFill);
        DefaultRule rulePolygon = new DefaultRule();
        rulePolygon.setSymbolizers(new Symbolizer[]{polysym});
        DefaultFeatureTypeStyle fts = new DefaultFeatureTypeStyle();
        fts.setFeatureTypeName("Polygon");
        fts.setRules(new Rule[]{rulePolygon});
        
        DefaultRule rulePolyline = new DefaultRule();
        rulePolyline.setSymbolizers(new Symbolizer[]{linesym});
        
        DefaultFeatureTypeStyle fts2 = new DefaultFeatureTypeStyle();
        fts2.setFeatureTypeName("Polyline");
        fts2.setRules(new Rule[]{rulePolyline});
        
        DefaultStyle style = new DefaultStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{fts,fts2});
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
            System.err.println("Usage: Example1 file");
            return;
        }
        Frame f = new Frame();
        f.setSize(300,300);
        f.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(WindowEvent e) { e.getWindow().dispose(); }
        });
        Example1_1 ex1 = new Example1_1(args[0]);
        f.add(ex1);
        f.setVisible(true);
        
        
    }
    
}
