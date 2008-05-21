/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotools.gui.swing.go;

import com.vividsolutions.jts.geom.MultiLineString;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.display.geom.MultiLineGraphic;
import org.geotools.display.primitive.FeatureGraphic;
import org.geotools.display.renderer.AWTDirectRenderer2D;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author sorel
 */
public class J2DRenderer extends AWTDirectRenderer2D {

    protected MapContext context = null;
    protected Map<MapLayer, List<FeatureGraphic>> featureGraphics = new HashMap<MapLayer, List<FeatureGraphic>>();

    public void setContext(MapContext context) {

        add(new LineGraphic());
        
        if (this.context != context) {
            removeContextGraphics();
            this.context = context;
            if (this.context != null) {
                parseContext(context);
            }
        }
        
    }

    public MapContext getContext() {
        return context;
    }

    private void removeContextGraphics() {
        Collection<List<FeatureGraphic>> lists = featureGraphics.values();

//        for (Collection<FeatureGraphic> list : lists) {
//            remove(list);
//        }
    }

    private void parseContext(MapContext context) {
        MapLayer[] layers = context.getLayers();

        for (int z = 0, n = layers.length; z < n; z++) {
            MapLayer layer = layers[z];
            try {
                parseLayer(layer, z);
            } catch (IOException ex) {
                System.out.println(ex);
                Logger.getLogger(J2DRenderer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    /**
     * Only works for multiline
     * 
     * @param layer
     * @param z
     * @throws java.io.IOException
     */
    private void parseLayer(MapLayer layer, int z) throws IOException {

        CoordinateReferenceSystem crs = layer.getFeatureSource().getSchema().getCRS();

        FeatureCollection<? extends FeatureType, ? extends Feature> features = layer.getFeatureSource().getFeatures();

        Iterator<? extends Feature> ite = features.iterator();

//        for(int i=0;i<2;i++){
        while (ite.hasNext()) {
            SimpleFeature feature = (SimpleFeature) ite.next();
            
            MultiLineGraphic gra = new MultiLineGraphic(crs, feature, z) {};
            add(gra);
            

        }

    }

    public RenderedImage getSnapShot() {
        return null;
    }

}
