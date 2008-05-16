/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.go;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.display.canvas.BufferedCanvas2D;
import org.geotools.display.renderer.BufferedRenderer2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.DefaultMapLayer;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.BasicLineStyle;
import org.geotools.styling.Style;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author sorel
 */
public class NormalMapPane extends JPanel{

    
    private final BufferedCanvas2D canvas;
    private final J2DRenderer renderer;
    
    public NormalMapPane(){
        super();
        renderer = new J2DRenderer();
        canvas = new BufferedCanvas2D(renderer,this);
        renderer.setCanvas(canvas);
        
        
        MapContext context = buildContext();
        
        renderer.setContext(context);
        
        DirectPosition center = new GeneralDirectPosition(context.getCoordinateReferenceSystem());                
        
        try {
            center.setOrdinate(0, context.getLayerBounds().getCenter(0));
            center.setOrdinate(1, context.getLayerBounds().getCenter(1));
        } catch (IOException ex) {
            Logger.getLogger(NormalMapPane.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {

            canvas.getController().setObjectiveCRS(context.getCoordinateReferenceSystem());
        } catch (TransformException ex) {
            ex.printStackTrace();
            Logger.getLogger(NormalMapPane.class.getName()).log(Level.SEVERE, null, ex);
        }
//        canvas.setS
        
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
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
                
        Graphics2D output = (Graphics2D) g;
        
        canvas.setDisplayBounds(getBounds());
        canvas.paint(output, new AffineTransform());
        
    }
    
    
}
