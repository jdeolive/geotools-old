/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.go;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.coverage.grid.GridRange2D;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.display.canvas.AWTCanvas2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.gui.swing.ZoomPane;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.operation.matrix.AffineTransform2D;
import org.geotools.styling.BasicLineStyle;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author sorel
 */
public class ZoomMapPane extends ZoomPane{

    private final AWTCanvas2D canvas;
    private final J2DRenderer renderer;
    private MapContext context;
    
    
    public ZoomMapPane(){
        super(ZoomPane.SCALE_X | ZoomPane.SCALE_Y | ZoomPane.ROTATE | ZoomPane.TRANSLATE_X | ZoomPane.TRANSLATE_Y | ZoomPane.RESET);
        
        renderer = new J2DRenderer();
        canvas = new AWTCanvas2D(renderer,this);
        renderer.setCanvas(canvas);
        
        
        context = buildContext();
//        
        renderer.setContext(context);
//        try {
//            canvas.setObjectiveCRS(context.getCoordinateReferenceSystem());
////        canvas.getController().setObjectiveCRS(context.getCoordinateReferenceSystem());
//        } catch (TransformException ex) {
//            Logger.getLogger(ZoomMapPane.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        
////        canvas.getController().setObjectiveCRS(context.getCoordinateReferenceSystem()); 
//        
//        renderer.add(new LineGraphic());
        
        
        
    }
    
    private MapContext buildContext() {
        MapContext context = null;
        MapLayer layer;

        try {
            context = new DefaultMapContext(DefaultGeographicCRS.WGS84);
            Map<String,Object> params = new HashMap<String,Object>();
            File shape = new File("/home/sorel/GIS_DATA/RESROU_TRONCON_ROUTE.SHP");
            params.put( "url", shape.toURI().toURL() );
           
            DataStore store = DataStoreFinder.getDataStore(params);
            FeatureSource<SimpleFeatureType, SimpleFeature> fs = store.getFeatureSource(store.getTypeNames()[0]);
            Style style = new BasicLineStyle();
            layer = new DefaultMapLayer(fs, style);
            layer.setTitle("Some lines");
            context.addLayer(layer);

            context.setCoordinateReferenceSystem(layer.getFeatureSource().getSchema().getCRS());
            context.setTitle("DemoContext");
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return context;
    }
    
    @Override
    public Rectangle2D getArea() {
        Rectangle2D rect = null;
        try {            
            rect = new Envelope2D(context.getLayerBounds());
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(ZoomMapPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        return rect;
    }

    @Override
    protected void paintComponent(Graphics2D g) {                
        Graphics2D output = (Graphics2D) g;
        canvas.setDisplayBounds(getBounds());
        canvas.paint(output, zoom);
                
//        canvas.setDisplayBounds(getBounds());
//        canvas.paint(output, zoom);
    }

}
