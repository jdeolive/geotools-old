/*
 * TextTest.java
 *
 * Created on 04 July 2002, 10:02
 */

package org.geotools.styling;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import org.geotools.feature.AttributeTypeFactory;
import javax.media.jai.JAI;

/**
 *
 * @author  iant
 */
public class TextTest extends junit.framework.TestCase {
    
    /** Creates a new instance of TextTest */
    public TextTest(java.lang.String testName) {
        super(testName);
        
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(TextTest.class);
        return suite;
    }
    
    public void testTextRender()throws Exception {
        System.out.println("\n\nText Test\n\n");
        java.awt.Frame frame = new java.awt.Frame("text test");
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {e.getWindow().dispose(); }
        });
        com.vividsolutions.jts.geom.Envelope ex = new com.vividsolutions.jts.geom.Envelope(0, 50, 0, 100);
        frame.setSize(300,600);
        GeometryFactory geomFac = new GeometryFactory();
        java.util.ArrayList features = new java.util.ArrayList();
        int points = 4;
        int rows = 3;
        org.geotools.feature.AttributeType[] pointAttribute = new org.geotools.feature.AttributeType[3];
        pointAttribute[0] = AttributeTypeFactory.newAttributeType("centre", com.vividsolutions.jts.geom.Point.class);
        pointAttribute[1] = AttributeTypeFactory.newAttributeType("size",Double.class);
        pointAttribute[2] = AttributeTypeFactory.newAttributeType("rotation",Double.class);
        org.geotools.feature.FeatureType pointType = org.geotools.feature.FeatureTypeFactory.newFeatureType(pointAttribute,"testPoint");
        for(int j=0;j<rows;j++){
            double angle =0.0;
            for(int i=0; i<points; i++){
                
                com.vividsolutions.jts.geom.Point point = makeSamplePoint(geomFac,
                2.0+(double)i*((ex.getWidth()-4)/points),
                50.0+(double)j*((50)/rows));
                
                Double size = new Double(5.0+j*5);
                Double rotation = new Double(angle);
                angle+=90.0;
                org.geotools.feature.Feature pointFeature = pointType.create(new Object[]{point,size,rotation});
                //                System.out.println(""+pointFeature);
                features.add(pointFeature);
            }
        }
        
        org.geotools.feature.AttributeType[] lineAttribute = new org.geotools.feature.AttributeType[3];
        lineAttribute[0] = AttributeTypeFactory.newAttributeType("edge", LineString.class);
        lineAttribute[1] = AttributeTypeFactory.newAttributeType("size",Double.class);
        lineAttribute[2] = AttributeTypeFactory.newAttributeType("perpendicularoffset",Double.class);
        org.geotools.feature.FeatureType lineType = org.geotools.feature.FeatureTypeFactory.newFeatureType(lineAttribute,"testLine");
        rows = 2;
        points = 3;
        
        double off = 1;
        for(int j=0;j<rows;j++){
            double angle =0.0;
            int sign = 1;
            for(int i=0; i<points; i++){
                LineString line = makeSimpleLineString(geomFac,i*ex.getWidth()/points,j*20,sign*20,20);
                Double size = new Double(12);
                Double poffset = new Double(off);
                sign--;
                org.geotools.feature.Feature lineFeature = lineType.create(new Object[]{line,size,poffset});
                features.add(lineFeature);
            }
            off-=2;
        }
        
        System.out.println("got "+features.size()+" features");
        org.geotools.feature.FeatureCollection ft = org.geotools.feature.FeatureCollections.newCollection();
        ft.addAll(features);
        
        org.geotools.map.Map map = new org.geotools.map.DefaultMap();
        //        String dataFolder = System.getProperty("dataFolder");
        //        if(dataFolder==null){
        //            //then we are being run by maven
        //            dataFolder = System.getProperty("basedir");
        //            if(dataFolder == null) dataFolder = ".";
        //            dataFolder+="/tests/unit/testData";
        //        }
        java.net.URL url = getClass().getResource("testData/");
        java.io.File f = new java.io.File(url.getPath(),"textTest.sld");
        System.out.println("testing reader using "+f.toString());
        StyleFactory factory = StyleFactory.createStyleFactory();
        SLDStyle stylereader = new SLDStyle(factory,f);
        Style[] style = stylereader.readXML();
        map.addFeatureTable(ft,style[0]);
        org.geotools.renderer.lite.LiteRenderer renderer = new org.geotools.renderer.lite.LiteRenderer();
//        org.geotools.renderer.Java2DRenderer renderer = new org.geotools.renderer.Java2DRenderer();
        java.awt.Panel p = new java.awt.Panel();
        frame.add(p);
        
        frame.setLocation(600,0);
        frame.setVisible(true);
        renderer.setOutput(p.getGraphics(),p.getBounds());
        map.render(renderer,ex);//and finaly try and draw it!
        int w = 300, h = 600;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(w,h,java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics g = image.getGraphics();
        g.setColor(java.awt.Color.white);
        g.fillRect(0,0,w,h);
        renderer.setOutput(g,new java.awt.Rectangle(0,0,w,h));
        map.render(renderer,ex);//and finaly try and draw it!
        java.io.File file = new java.io.File(url.getPath(), "TextTest.png");
        java.io.FileOutputStream out = new java.io.FileOutputStream(file);
        javax.imageio.ImageIO.write(image, "PNG", out);
        
        java.io.File file2 = new java.io.File(url.getPath()+"/exemplars/", "TextTest.png");
        System.out.println("about to load "+file2+" to carry out checks");
        
        RenderedImage image2 = (RenderedImage) JAI.create("fileload", file2.toString());
        
        
        /*
        assertNotNull("Failed to load exemplar image",image2); 
        Raster data = image.getData();
        Raster data2 = image2.getData(); 
        int [] pix=null;
        int [] pix2=null;
        for(int band=0;band<data2.getNumBands();band++){
            pix=data.getSamples(0,0,image.getWidth(),image.getHeight(),band,pix); 
            pix2=data2.getSamples(0,0,image2.getWidth(),image2.getHeight(),band,pix2);
            System.out.println("band "+band+" pix "+pix.length+" pix2 "+pix2.length);
            for(int i=0;i<pix.length;i++){
                assertEquals("mismatch in image comparision at ("+i+")",pix[i],pix2[i]);
            }
        }
        //Thread.sleep(5000);
         */
        frame.dispose();
    }
    private com.vividsolutions.jts.geom.Point makeSamplePoint(final GeometryFactory geomFac, double x, double y) {
        Coordinate c = new Coordinate(x,y);
        com.vividsolutions.jts.geom.Point point = geomFac.createPoint(c);
        return point;
    }
    private LineString makeSampleLineString(final GeometryFactory geomFac, double xoff, double yoff) {
        Coordinate[] linestringCoordinates = new Coordinate[4];
        linestringCoordinates[0] = new Coordinate(0.0d+xoff,0.0d+yoff);
        linestringCoordinates[1] = new Coordinate(10.0d+xoff,10.0d+yoff);
        linestringCoordinates[2] = new Coordinate(15.0d+xoff,20.0d+yoff);
        linestringCoordinates[3] = new Coordinate(20.0d+xoff,30.0d+yoff);
        
        LineString line = geomFac.createLineString(linestringCoordinates);
        
        return line;
    }
    private LineString makeSimpleLineString(final GeometryFactory geomFac, double xstart, double ystart, double width, double height) {
        Coordinate[] linestringCoordinates = new Coordinate[2];
        linestringCoordinates[0] = new Coordinate(xstart,ystart);
        
        linestringCoordinates[1] = new Coordinate(xstart+width,ystart+height);
        
        LineString line = geomFac.createLineString(linestringCoordinates);
        
        return line;
    }
}
