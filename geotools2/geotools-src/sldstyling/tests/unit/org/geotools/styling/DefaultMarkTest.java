/*
 * RenderStyleTest.java
 *
 * Created on 27 May 2002, 15:40
 */

package org.geotools.styling;

import com.vividsolutions.jts.geom.Point;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import javax.imageio.ImageIO;
import org.geotools.feature.AttributeTypeFactory;
import javax.media.jai.JAI;

/**
 *
 * @author jamesm
 */
public class DefaultMarkTest extends junit.framework.TestCase {
    
    public DefaultMarkTest(java.lang.String testName) {
        super(testName);
        
    }
    
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(DefaultMarkTest.class);
        return suite;
    }
    
    public void testSimpleRender()throws Exception {
        //same as the datasource test, load in some features into a table
        System.out.println("\n\nMark Test\n");
        // Request extent
        com.vividsolutions.jts.geom.Envelope ex = new com.vividsolutions.jts.geom.Envelope(0, 45,0,45);
        //EnvelopeExtent ex = new EnvelopeExtent(0, 45, 0, 45);
        
        com.vividsolutions.jts.geom.GeometryFactory geomFac = new com.vividsolutions.jts.geom.GeometryFactory();
        java.util.ArrayList features = new java.util.ArrayList();
        
        org.geotools.feature.AttributeType[] pointAttribute = new org.geotools.feature.AttributeType[4];
        pointAttribute[0] = AttributeTypeFactory.newAttributeType("centre", Point.class);
        pointAttribute[1] = AttributeTypeFactory.newAttributeType("size",Double.class);
        pointAttribute[2] = AttributeTypeFactory.newAttributeType("rotation",Double.class);
        pointAttribute[3] = AttributeTypeFactory.newAttributeType("name",String.class);
        org.geotools.feature.FeatureType pointType = org.geotools.feature.FeatureTypeFactory.newFeatureType(pointAttribute,"testPoint");
        
        org.geotools.feature.AttributeType[] labelAttribute = new org.geotools.feature.AttributeType[4];
        labelAttribute[0] = AttributeTypeFactory.newAttributeType("centre", Point.class);
        labelAttribute[1] = AttributeTypeFactory.newAttributeType("name",String.class);
        labelAttribute[2] = AttributeTypeFactory.newAttributeType("X",Double.class);
        labelAttribute[3] = AttributeTypeFactory.newAttributeType("Y",Double.class);
        org.geotools.feature.FeatureType labelType = org.geotools.feature.FeatureTypeFactory.newFeatureType(labelAttribute,"labelPoint");
        String[] marks = {"Circle","Triangle","Cross","Star","X","Square","Arrow"};
        double size = 6;
        double rotation = 0;
        int rows = 7;
        for(int j=0;j<rows;j++){
            Point point = makeSamplePoint(geomFac,2,5.0+j*5);
            org.geotools.feature.Feature pointFeature = labelType.create(new Object[]{point,""+size+"/"+rotation,new Double(0.3),new Double(.5)});
            features.add(pointFeature);
            for(int i=0; i<marks.length; i++){
                point = makeSamplePoint(geomFac,(double)i*5.0+10.0, 5.0+j*5);
                pointFeature = pointType.create(new Object[]{point,new Double(size),new Double(rotation),marks[i]});
                features.add(pointFeature);
            }
            size+=2;
            rotation+=45;
        }
        for(int i=0; i<marks.length; i++){
            Point point = makeSamplePoint(geomFac,(double)i*5.0+10.0,5.0+rows*5);
            org.geotools.feature.Feature pointFeature = labelType.create(new Object[]{point,marks[i],new Double(.5),new Double(0)});
            features.add(pointFeature);
        }
        //        System.out.println("got "+features.size()+" features");
        org.geotools.feature.FeatureCollection ft = org.geotools.feature.FeatureCollections.newCollection();
        ft.addAll(features);
        
        org.geotools.map.Map map = new org.geotools.map.DefaultMap();
        java.net.URL base = getClass().getResource("testData/");
        //        String dataFolder = System.getProperty("dataFolder");
        //        if(dataFolder==null){
        //            //then we are being run by maven
        //            dataFolder = System.getProperty("basedir");
        //            if(dataFolder == null) dataFolder = ".";
        //            dataFolder+="/tests/unit/testData";
        //        }
        //        File f = new File(dataFolder,"markTest.sld");
        //        System.out.println("testing reader using "+ url);
        
        
        StyleFactory factory = StyleFactory.createStyleFactory();
        java.net.URL surl = new java.net.URL(base + "markTest.sld");
        SLDStyle stylereader = new SLDStyle(factory,surl);
        Style[] style = stylereader.readXML();
        
        map.addFeatureTable(ft,style[0]);
        org.geotools.renderer.lite.LiteRenderer renderer = new org.geotools.renderer.lite.LiteRenderer();
//        org.geotools.renderer.Java2DRenderer renderer = new org.geotools.renderer.Java2DRenderer();
        java.awt.Frame frame = new java.awt.Frame("default mark test");
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {e.getWindow().dispose(); }
        });
        java.awt.Panel p = new java.awt.Panel();
        frame.add(p);
        frame.setSize(300,300);
        frame.setLocation(300,0);
        frame.setVisible(true);
        renderer.setOutput(p.getGraphics(),p.getBounds());
        map.render(renderer,ex);//and finaly try and draw it!
        int w=400, h=400;
        java.awt.image.BufferedImage image = new java.awt.image.BufferedImage(w,h,java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.Graphics g = image.getGraphics();
        g.setColor(java.awt.Color.white);
        g.fillRect(0,0,w,h);
        renderer.setOutput(g,new java.awt.Rectangle(0,0,w,h));
        map.render(renderer,ex);//and finaly try and draw it!
        java.io.File file = new java.io.File(base.getPath(), "DefaultMarkTest.png");
        java.io.FileOutputStream out = new java.io.FileOutputStream(file);
        boolean fred = javax.imageio.ImageIO.write(image, "PNG", out);
        if(!fred){
            System.out.println("Failed to write image to " + file.toString());
        }
        java.io.File file2 = new java.io.File(base.getPath()+"/exemplars/", "DefaultMarkTest.png");
        System.out.println("about to load "+file2+" to carry out checks");
        
        RenderedImage image2 = (RenderedImage) JAI.create("fileload", file2.toString());
        
        
        
        assertNotNull("Failed to load exemplar image",image2); 
        Raster data = image.getData();
        Raster data2 = image2.getData(); 
        int [] pix=null;
        int [] pix2=null;
        for(int band=0;band<data2.getNumBands();band++){
            pix=data.getSamples(0,0,image.getWidth(),image.getHeight(),band,pix); 
            pix2=data2.getSamples(0,0,image2.getWidth(),image2.getHeight(),band,pix2);
            for(int i=0;i<pix.length;i++){
                assertEquals("mismatch in image comparision",pix[i],pix2[i]);
            }
        }
        
    //Thread.sleep(5000); 
    frame.dispose(); 
}

private Point makeSamplePoint(final com.vividsolutions.jts.geom.GeometryFactory geomFac, double x, double y) {
    com.vividsolutions.jts.geom.Coordinate c = new com.vividsolutions.jts.geom.Coordinate(x,y);
    Point point = geomFac.createPoint(c);
    return point;
}
}



