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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JToolBar;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Rule;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.Symbolizer;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.swing.event.MapMouseAdapter;
import org.geotools.swing.event.MapMouseEvent;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.identity.FeatureId;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * In this example we create a map tool to select a feature clicked
 * with the mouse. The selected feature will be painted yellow.
 */
public class SelectionDemo extends MapMouseAdapter {

    StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
    FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);

    JMapFrame mapFrame;
    FeatureSource<SimpleFeatureType, SimpleFeature> featureSource;
    String geometryAttributeName;

    boolean listening = false;

    public static void main(String[] args) throws Exception {
        SelectionDemo me = new SelectionDemo();
        me.doDemo();
    }

    private void doDemo() throws Exception {
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        ShapefileDataStore shapefile = new ShapefileDataStore(file.toURI().toURL());
        String typeName = shapefile.getTypeNames()[0];
        featureSource = shapefile.getFeatureSource();

        SimpleFeatureType schema = featureSource.getSchema();
        geometryAttributeName = schema.getGeometryDescriptor().getLocalName();

        CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();
        MapContext map = new DefaultMapContext(crs);
        map.setTitle("Feature selection tool example");

        Style style = createDefaultStyle();
        map.addLayer(featureSource, style);

        mapFrame = new JMapFrame(map);
        mapFrame.enableTool(JMapFrame.Tool.NONE);

        JToolBar toolBar = mapFrame.getToolBar();

        final JButton btn = new JButton("Select");
        toolBar.add(btn);

        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                listening = !listening;
                if (listening) {
                    mapFrame.getMapPane().addMouseListener(SelectionDemo.this);

                } else {
                    mapFrame.getMapPane().removeMouseListener(SelectionDemo.this);
                }
            }
        });

        mapFrame.setSize(600, 600);
        mapFrame.setVisible(true);
    }

    @Override
    public void onMouseClicked(MapMouseEvent ev) {
        if (listening) {
            DirectPosition2D pos = ev.getMapPosition();

            String filterString =
                    String.format("CONTAINS(%s, POINT(%f %f))",
                    geometryAttributeName, pos.x, pos.y);

            System.out.println("Filter: " + filterString);

            Filter filter = null;
            try {
                filter = CQL.toFilter(filterString);
                FeatureCollection<SimpleFeatureType, SimpleFeature> selectedFeatures = featureSource.getFeatures(filter);

                FeatureIterator<SimpleFeature> iter = selectedFeatures.features();
                Set<FeatureId> IDs = new HashSet<FeatureId>();
                try {
                    while (iter.hasNext()) {
                        SimpleFeature feature = iter.next();
                        IDs.add(feature.getIdentifier());
                    }

                } finally {
                    iter.close();
                }

                Style style = createSelectedStyle(IDs);
                mapFrame.getMapContext().getLayer(0).setStyle(style);
                mapFrame.getMapPane().repaint();

            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
        }
    }

    /**
     * Create a default Style to display polygon features
     */
    private Style createDefaultStyle() {
        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(getRule(Color.BLUE, Color.CYAN));

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }

    /**
     * Create a Style where features with given IDs are painted
     * yellow, while others are painted with the default colors.
     */
    private Style createSelectedStyle(Set<FeatureId> IDs) {
        Rule selectedRule = getRule(Color.YELLOW, Color.YELLOW);
        selectedRule.setFilter(ff.id(IDs));

        Rule otherRule = getRule(Color.BLUE, Color.CYAN);
        otherRule.setElseFilter(true);

        FeatureTypeStyle fts = sf.createFeatureTypeStyle();
        fts.rules().add(selectedRule);
        fts.rules().add(otherRule);

        Style style = sf.createStyle();
        style.featureTypeStyles().add(fts);
        return style;
    }

    /**
     * Create a rule with no filter and a symbolizer that draws polygons
     * with blue outlines and cyan fill
     */
    private Rule getRule(Color outlineColor, Color fillColor) {
        Stroke stroke = sf.createStroke(ff.literal(outlineColor), ff.literal(1.0));
        Fill fill = sf.createFill(ff.literal(fillColor), ff.literal(1.0));
        Symbolizer symbolizer = sf.createPolygonSymbolizer(stroke, fill, null);

        Rule rule = sf.createRule();
        rule.symbolizers().add(symbolizer);
        return rule;
    }
}
