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
import org.geotools.filter.*;
import org.geotools.map.*;
import org.geotools.data.*;
import org.geotools.shapefile.*;
import org.geotools.datasource.extents.EnvelopeExtent;
import java.awt.event.*;
import java.awt.*;



import org.geotools.styling.*;
/**
 *
 * @author  iant
 */
public class Example1 extends java.awt.Panel{
    Envelope r;
    //AWTRenderer renderer = new AWTRenderer();
    Java2DRenderer renderer = new Java2DRenderer();
    Map map = new DefaultMap();
    /** Creates a new instance of Example1 */

 
    public Example1(String uri){
        FeatureCollectionDefault ft = new FeatureCollectionDefault();

        

        String dataFolder = System.getProperty("user.dir");//"d:\\ian\\development\\geotools2/geotools-src/spike/ian";
       // String uri = "statepop.shp";

        try{
            java.net.URL url = new java.net.URL("file:///" + dataFolder + "/" + uri);
            System.out.println("Testing ability to load "+url);
            org.geotools.shapefile.Shapefile shapefile = new org.geotools.shapefile.Shapefile(url);
            org.geotools.shapefile.ShapefileDataSource datasource = new org.geotools.shapefile.ShapefileDataSource(shapefile);
            
            ft.setDataSource(datasource);
            
            r=(Envelope)datasource.getBbox();
            //Feature[] features = table.getFeatures(r);
            //System.out.println("No features loaded = "+features.length);
        }catch(IOException ioe){
            System.out.println(ioe);
            ioe.printStackTrace();
            return;
        }
        
        
        //The following is complex, and should be built from
        //an SLD document and not by hand 
        DefaultFill myFill = new DefaultFill();
        myFill.setColor("#ffaaaa");
        myFill.setOpacity(new ExpressionLiteral(0.5));
        DefaultStroke myStroke = new DefaultStroke();
        myStroke.setColor("#880000");
        //Using a BasicPolgyonStyle avoids setting up rules and symbolizers
        BasicPolygonStyle style = new BasicPolygonStyle(myFill,myStroke);
        try{
            ft.getFeatures(new EnvelopeExtent(r));
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
        Image img = this.createImage(this.getWidth(),this.getHeight());
        renderer.setOutput(img.getGraphics(),this.getBounds());
        map.render(renderer,r);//and finaly  try and draw it!
        g.drawImage(img,0,0,this);
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
        Example1 ex1 = new Example1(args[0]);
        f.add(ex1);
        f.setVisible(true);
        
        
    }
    
}
