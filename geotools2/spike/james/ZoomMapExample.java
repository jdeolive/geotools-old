/*
 * Example1.java
 *
 * Created on April 24, 2002, 3:08 PM
 */

package spike.james;
import java.io.*;
import com.vividsolutions.jts.geom.*;
import org.geotools.renderer.*;
import org.geotools.featuretable.*;
import org.geotools.map.*;
import org.geotools.datasource.*;
import org.geotools.shapefile.*;
import org.geotools.datasource.extents.EnvelopeExtent;
import java.awt.event.*;
import java.awt.*;
import org.geotools.swinggui.*;

import javax.swing.*;



import org.geotools.styling.*;
/**
 *
 * @author  iant
 */
public class ZoomMapExample extends java.awt.Panel{
    EnvelopeExtent r = new EnvelopeExtent();
    //AWTRenderer renderer = new AWTRenderer();
    Java2DRenderer renderer = new Java2DRenderer();
    Map map = new DefaultMap();
    /** Creates a new instance of Example1 */
    public ZoomMapExample(String uri){
   
        DefaultFeatureTable ft = new DefaultFeatureTable();
        
        //String dataFolder = "d:\\work\\java\\geotools2/geotools-src\\shapefile\\tests\\unit\\testData";
        try{
            java.net.URL url = new java.net.URL("file:///" + uri);
            System.out.println("Testing ability to load "+url);
            org.geotools.shapefile.Shapefile shapefile = new org.geotools.shapefile.Shapefile(url);
            org.geotools.shapefile.ShapefileDataSource datasource = new org.geotools.shapefile.ShapefileDataSource(shapefile);
            
            ft.setDataSource(datasource);
            
            r=(EnvelopeExtent)datasource.getExtent();
            //Feature[] features = table.getFeatures(r);
            //System.out.println("No features loaded = "+features.length);
        }catch(IOException ioe){
            System.out.println(ioe);
            ioe.printStackTrace();
            return;
        }
        
        
        //The following is complex, and should be built from
        //an SLD document and not by hand
        LineSymbolizer linesym = new DefaultLineSymbolizer();
        DefaultPolygonSymbolizer polysym = new DefaultPolygonSymbolizer();
        DefaultFill myFill = new DefaultFill();
        myFill.setColor("#ff0000");
        polysym.setFill(null);
        DefaultStroke stroke = new DefaultStroke();
        //stroke.setDashArray(new float[]{5,3});
        stroke.setWidth(2);
        //stroke.setOpacity(.4);
        polysym.setStroke(stroke);
        DefaultRule rule = new DefaultRule();
        rule.setSymbolizers(new Symbolizer[]{polysym});
        DefaultFeatureTypeStyle fts = new DefaultFeatureTypeStyle();
        fts.setRules(new Rule[]{rule});
        
        DefaultStyle style = new DefaultStyle();
        style.setFeatureTypeStyles(new FeatureTypeStyle[]{fts});
        try{
            ft.getFeatures(r);
        }catch (DataSourceException e){
            System.err.println("whoops - error reading features " + e);
            return;
        }
        
            
        map.addFeatureTable(ft,style);
        final MapPane master = new MapPane(map,r);
        //final MapPane slave  = new MapPane();
        
        JFrame frame;
        frame=new JFrame("Master");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(master.createScrollPane());
        frame.setLocation(0,0);
        frame.pack();
        frame.show();
     
        
    }
   
  
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        if(args.length<1){
            System.err.println("Usage: ZoomMapExample shapefilefile.shp");
            return;
        }
        ZoomMapExample ex1 = new ZoomMapExample(args[0]);
    }
    
}
