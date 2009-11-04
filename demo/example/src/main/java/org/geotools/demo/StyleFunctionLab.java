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

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.visitor.UniqueVisitor;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;
import org.geotools.styling.SLD;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;


public class StyleFunctionLab {

    public static void main(String[] args) throws Exception {
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        StyleFunctionLab me = new StyleFunctionLab();
        me.displayShapefile(file);
    }

    /**
     * Display
     * @param file
     * @throws Exception
     */
    private void displayShapefile(File file) throws Exception {
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        FeatureSource featureSource = store.getFeatureSource();

        // Create a map context and add our shapefile to it
        MapContext map = new DefaultMapContext();
        map.setTitle("Filter Function Lab");

        FeatureType type = featureSource.getSchema();
        PropertyDescriptor geomDesc = type.getGeometryDescriptor();
        List<String> attributeNames = new ArrayList<String>();
        for (PropertyDescriptor desc : type.getDescriptors()) {
            if (!desc.equals(geomDesc)) {
                attributeNames.add(desc.getName().getLocalPart());
            }
        }

        Object selection = JOptionPane.showInputDialog(null,
                "Unique values of the attribute will be used to create a colour lookup table",
                "Select feature attribute",
                JOptionPane.PLAIN_MESSAGE, null,
                attributeNames.toArray(), null);

        if (selection != null) {
            Style style = createStyle(featureSource, (String)selection);
            map.addLayer(featureSource, style);

            // Now display the map
            JMapFrame.showMap(map);
        }
    }

    /**
     * Create a rendering style to display features from the given feature source
     * by matching unique values of the specified feature attribute to colours
     *
     * @param featureSource the feature source
     * @return a new Style instance
     */
    private Style createStyle(FeatureSource featureSource, String attributeName) 
            throws Exception {

        FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);
        StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

        UniqueColourFunction colourFn = new UniqueColourFunction(
                featureSource.getFeatures(), ff.property(attributeName));
        Fill fill = sf.createFill(colourFn);
        Stroke stroke = sf.createStroke(colourFn, ff.literal(1.0f));

        Class<?> geomClass = featureSource.getSchema().getGeometryDescriptor().getType().getBinding();
        Symbolizer sym = null;
        if (Polygon.class.isAssignableFrom(geomClass) ||
            MultiPolygon.class.isAssignableFrom(geomClass)) {

            sym = sf.createPolygonSymbolizer(stroke, fill, null);

        } else if (LineString.class.isAssignableFrom(geomClass) ||
                   MultiLineString.class.isAssignableFrom(geomClass)) {
            sym = sf.createLineSymbolizer(stroke, null);

        } else {
            Graphic gr = sf.createDefaultGraphic();
            gr.graphicalSymbols().clear();
            Mark mark = sf.getCircleMark();
            mark.setFill(fill);
            mark.setStroke(stroke);
            gr.graphicalSymbols().add(mark);
            gr.setSize(ff.literal(10.0f));
            sym = sf.createPointSymbolizer(gr, null);
        }

        Style style = SLD.wrapSymbolizers(sym);
        return style;
    }


    /**
     * A function to dynamically allocate colours to features. It works with a lookup table
     * where the key is a user-specified feature attribute. Colours are generated using
     * a simple colour ramp algorithm.
     */
    static class UniqueColourFunction extends FunctionExpressionImpl {

        private static final float INITIAL_HUE = 0.1f;
        private final FeatureCollection collection;

        Map<Object, Color> lookup;
        private int numColours;

        private float hue;
        private float hueIncr;
        private float saturation = 0.7f;
        private float brightness = 0.7f;


        /**
         * Creates an instance of the function for the given feature collection. Features will
         * be assigned fill colours by matching the value of the specified feature attribute
         * in a lookup table of unique attribute values with associated colours.
         *
         * @param collection the feature collection
         *
         * @param colourAttribute a literal expression that specifies the feature attribute
         *        to use for colour lookup
         */
        public UniqueColourFunction(FeatureCollection collection, Expression colourAttribute) {
            super("UniqueColour");
            this.collection = collection;

            this.params.add(colourAttribute);
            this.fallback = CommonFactoryFinder.getFilterFactory2(null).literal(Color.WHITE);
        }

        @Override
        public int getArgCount() {
            return 1;
        }

        /**
         * Evalute this function for a given feature and return a
         * Color.
         *
         * @param object the feature for which a colour is being requested
         *
         * @return the colour for this feature
         */
        @Override
        public Object evaluate(Object feature) {
            if (lookup == null) {
                createLookup();
            }

            Object key = ((Expression)params.get(0)).evaluate(feature);
            Color color = lookup.get(key);
            if (color == null) {
                color = addColor(key);
            }

            return color;
        }

        /**
         * Creates the lookup table and initializes variables used in
         * colour generation
         */
        private void createLookup() {
            lookup = new HashMap<Object, Color>();
            try {
                UniqueVisitor visitor = new UniqueVisitor((Expression)params.get(0));
                collection.accepts(visitor, null);
                numColours = visitor.getUnique().size();
                hue = INITIAL_HUE;
                hueIncr = (1.0f - hue) / numColours;

            } catch (Exception ex) {
                throw new IllegalStateException("Problem creating colour lookup", ex);
            }
        }

        /*
         * Generates a new colour for the colour ramp and adds it to
         * the lookup table
         */
        private Color addColor(Object key) {
            Color c = new Color(Color.HSBtoRGB(hue, saturation, brightness));
            hue += hueIncr;
            lookup.put(key, c);
            return c;
        }
    }
}

