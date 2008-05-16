/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotools.gui.swing.go;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import java.io.IOException;
import java.lang.ref.Reference;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.geotools.coverage.grid.GridRange2D;
import org.geotools.display.geom.MultiLineGraphic;
import org.geotools.display.primitive.FeatureGraphic;
import org.geotools.display.renderer.BufferedRenderer2D;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.opengis.display.primitive.Graphic;
import org.opengis.feature.Feature;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.FeatureType;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author sorel
 */
public class J2DRenderer extends BufferedRenderer2D {

    protected MapContext context = null;
    protected Map<MapLayer, List<FeatureGraphic>> featureGraphics = new HashMap<MapLayer, List<FeatureGraphic>>();

    public void setContext(MapContext context) {


        if (this.context != context) {
            removeContextGraphics();
            this.context = context;
            if (this.context != null) {
                parseContext(context);
            }
        }
        
        /**
         * we set the maparea to see the complete mapcontext
         */
        ReferencedEnvelope env = null;
        
        try {
            env = context.getLayerBounds();
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(J2DRenderer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        DirectPosition center = new DirectPosition2D(context.getCoordinateReferenceSystem(), env.centre().x, env.centre().y);        
        
        if (env != null) {
            getCanvas().getController().setCenter(center);
        }

    }

    public MapContext getContext() {
        return context;
    }

    private void removeContextGraphics() {
        Collection<List<FeatureGraphic>> lists = featureGraphics.values();

        for (Collection<FeatureGraphic> list : lists) {
            remove(list);
        }
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

        while (ite.hasNext()) {
            SimpleFeature feature = (SimpleFeature) ite.next();
            Object geom = feature.getDefaultGeometry();

            if (geom instanceof MultiLineString) {
                MultiLineGraphic gra = new MultiLineGraphic(crs, (MultiLineString) geom, z);
                add(gra);
            }

        }

    }
}
