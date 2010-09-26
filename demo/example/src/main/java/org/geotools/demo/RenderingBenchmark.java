// docs start source
/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.demo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.renderer.shape.ShapefileRenderer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;

/**
 * GeoTools Quickstart demo application. Prompts the user for a shapefile
 * and displays its contents on the screen in a map frame
 *
 * @source $URL$
 */
public class RenderingBenchmark {

    private static final int LOOPS = 10;

    /**
     * GeoTools Quickstart demo application. Prompts the user for a shapefile
     * and displays its contents on the screen in a map frame
     */    
    public static void main(String[] args) throws Exception {
         FileDataStore store = new ShapefileDataStore(new File("/home/aaime/devel/gisData/bc_shapefiles/bc_roads.shp").toURL(), null, true, false, Charset.forName("ISO-8859-1"));
//        FileDataStore store = new ShapefileDataStore(new File("/home/aaime/devel/gisData/gshhs/gshhs_land/gshhs_land.shp").toURL());
        FeatureSource featureSource = store.getFeatureSource();

        // Create a map context and add our shapefile to it
        MapContext map = new DefaultMapContext();
        map.setTitle("Quickstart");
        map.addLayer(featureSource, createStyle(featureSource));
        
        BufferedImage bi = new BufferedImage(1024, 768, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2d = bi.createGraphics();
        g2d.setColor(Color.WHITE);
        g2d.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        
        long start = System.currentTimeMillis();
        for(int i = 0; i < LOOPS; i++) {
            StreamingRenderer sr = new StreamingRenderer();
//            ShapefileRenderer  sr = new ShapefileRenderer();
            
            sr.setContext(map);
            sr.paint(g2d, new Rectangle(bi.getWidth(), bi.getHeight()), featureSource.getBounds());
        }
        g2d.dispose();
        long end = System.currentTimeMillis();
        System.out.println((end - start) / 1000.0);
        
        ImageIO.write(bi, "png", new File("/tmp/test.png"));

    }

    static Style createStyle(FeatureSource featureSource) {
        StyleBuilder sb = new StyleBuilder();
        Symbolizer symbolizer;
        Class<?> binding = featureSource.getSchema().getGeometryDescriptor().getType().getBinding();
        if(Point.class.isAssignableFrom(binding) || MultiPoint.class.isAssignableFrom(binding)) {
            symbolizer = sb.createPointSymbolizer();
        } else if(LineString.class.isAssignableFrom(binding) || MultiLineString.class.isAssignableFrom(binding)) {
            symbolizer = sb.createLineSymbolizer();
        } else {
            symbolizer = sb.createPolygonSymbolizer();
        }
        return sb.createStyle(symbolizer);
    }

}
