/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.renderer.j2d;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.geotools.gc.GridCoverage;
import org.geotools.renderer.geom.Geometry;
import org.geotools.renderer.geom.GeometryProxy;
import org.geotools.renderer.geom.JTSGeometries;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.renderer.style.Style;
import org.geotools.resources.XArray;
import org.geotools.styling.ColorMap;
import org.geotools.styling.ColorMapEntry;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.geotools.styling.TextSymbolizer;
import org.geotools.util.NumberRange;
import org.geotools.util.RangeSet;

// JTS dependencies
import java.awt.Color;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Logger;


/**
 * A factory creating {@link RenderedLayer}s from {@link Feature}s and {@link Style}s.
 *
 * @author Andrea Aime
 * @author Martin Desruisseaux
 * @version $Id: RenderedLayerFactory.java,v 1.24 2004/03/27 23:40:00 jmacgill Exp $
 */
public class RenderedLayerFactory {
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.renderer.j2d");

    /** The full range of map scale. */
    private static final NumberRange FULL_SCALE_RANGE = new NumberRange(0.0, Double.MAX_VALUE);

    /** Prepare the style factory that will convert SLD styles into resolved styles. */
    private final SLDStyleFactory styleFactory = new SLDStyleFactory();

    /**
     * Maps the feature sources into a map, which in turn maps the JTS geometries into the rendered
     * geometries that wraps them. Used to optimize memory usage and get the same optmized and
     * rendered geometry when a layer is going to be rebuilt (that is, we avoid to rebuild the
     * decimated version of the rendered geometry on style change)
     */
    private final WeakHashMap geometryMaps = new WeakHashMap();

    /**
     * The default coordinate system for geometry to be created. If a geometry defines explicitly a
     * coordinate system, then the geometry CS will have precedence over this default CS.
     *
     * @see #getCoordinateSystem
     * @see #setCoordinateSystem
     */
    private CoordinateSystem coordinateSystem = Geometry.DEFAULT_COORDINATE_SYSTEM;

    /**
     * Construct a default factory.
     */
    public RenderedLayerFactory() {
    }

    /**
     * Returns the default coordinate system for geometry to be created. If a geometry defines
     * explicitly a coordinate system, then the geometry CS will have precedence over this default
     * CS.
     *
     * @return The default coordinate system.
     */
    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    /**
     * Set the default coordinate system for geometry to be created. This CS is used only if a
     * geometry doesn't specifies explicitly its own CS. If this method is never invoked, then the
     * default CS is {@link Geometry#DEFAULT_COORDINATE_SYSTEM}.
     *
     * @param coordinateSystem The default coordinate system.
     */
    public void setCoordinateSystem(final CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
    }

    /**
     * Create an array of rendered layers from the specified feature and style.
     *
     * @param featureSource the source used to read features
     * @param style the style for these features
     *
     * @return The rendered layer array for the specified feature and style.
     *
     * @throws TransformException if a transformation was required and failed.
     * @throws IOException if an error occurs while reading the features
     * @throws IllegalAttributeException if an attribute is read from the data that is incompatible
     *         with the feature type
     */
    public RenderedLayer[] createOld(final FeatureSource featureSource,
        final org.geotools.styling.Style style)
        throws TransformException, IOException, IllegalAttributeException {
        FeatureReader fr = featureSource.getFeatures().reader();
        Feature[] features = new Feature[100];
        int currFeature = 0;

        while (fr.hasNext()) {
            if (currFeature >= features.length) {
                features = (Feature[]) XArray.resize(features, (int) ((features.length * 3) / 2)
                        + 1);
            }

            features[currFeature] = fr.next();
            currFeature++;
        }

        features = (Feature[]) XArray.resize(features, currFeature);

        return create(features, style);
    }

    /**
     * Create an array of rendered layers from the specified feature and style.
     *
     * @param featureSource The feature.
     * @param sldStyle The style to apply.
     *
     * @return The rendered layer array for the specified feature and style.
     *
     * @throws TransformException if a transformation was required and failed.
     * @throws IOException DOCUMENT ME!
     * @throws IllegalAttributeException DOCUMENT ME!
     */
    public RenderedLayer[] create(final FeatureSource featureSource,
        final org.geotools.styling.Style sldStyle)
        throws TransformException, IOException, IllegalAttributeException {
        // get the map from JTSGeometries to rendered geometries
        JTSGeometryMap renderedGeometriesMap = (JTSGeometryMap) geometryMaps.get(featureSource);

        if (renderedGeometriesMap == null) {
            renderedGeometriesMap = new JTSGeometryMap(featureSource);
            geometryMaps.put(featureSource, renderedGeometriesMap);
        }

        //  process the styles in order
        FeatureTypeStyle[] featureStylers = sldStyle.getFeatureTypeStyles();

        // prepare the set of layers that will be generated be each feature type style
        List featureTypeRenderedLayers = new ArrayList();
        List featureTypeCurrentLayer = new ArrayList();

        for (int i = 0; i < featureStylers.length; i++) {
            //  ... the list that will contain all generated rendered layers
            featureTypeRenderedLayers.add(new ArrayList());

            // ... and the first geometric layer
            featureTypeCurrentLayer.add(new JTSGeometries(coordinateSystem));
        }

        // see what attributes we really need 
        //        StyleAttributeExtractor sae = new StyleAttributeExtractor();
        //        sae.visit(SLDStyle);
        //        String[] ftsAttributes = sae.getAttributeNames();
        //        String[] attributes = new String[ftsAttributes.length + 1];
        //        attributes[0] = featureSource.getSchema().getDefaultGeometry().getName();
        //        System.arraycopy(ftsAttributes, 0, attributes, 1, ftsAttributes.length);
        //        Query q = new DefaultQuery(featureSource.getSchema().getTypeName(), Filter.NONE,
        //                Integer.MAX_VALUE, attributes, "");
        // get the features and scan them
        FeatureReader reader = featureSource.getFeatures().reader();

        while (reader.hasNext()) {
            Feature feature = reader.next();

            // generate rendered geometries for each feature type style and store them
            // in separate arrays so that we can put them into the right order later
            for (int i = 0; i < featureStylers.length; i++) {
                FeatureTypeStyle fts = featureStylers[i];
                JTSGeometries geometries = (JTSGeometries) featureTypeCurrentLayer.get(i);
                ArrayList renderedLayers = (ArrayList) featureTypeRenderedLayers.get(i);

                RangeSet rs = new RangeSet(Double.class);

                // Prepare the else features map. Since we are preprocessing, we have to consider
                // what the elseFilters may catch at each scale, so we build a set of scale ranges
                // in which the elseFilter should apply by subtraction: we start with the full
                // floating point range and subtract the ranges covered by filters, if the
                // range set becomes empty, the feature is removed from the elseFeature map,
                // otherwise a Geometry is created from each remaining scale range
                rs.add(FULL_SCALE_RANGE);

                // process the rules for the current feature type style
                Rule[] rules = fts.getRules();

                for (int j = 0; j < rules.length; j++) {
                    // if this rule is not an else filter
                    if (!rules[j].hasElseFilter()) {
                        Filter filter = rules[j].getFilter();
                        Symbolizer[] symbolizers = rules[j].getSymbolizers();
                        List ruleFeatures = new ArrayList();
                        NumberRange ruleRange = buildRuleRange(rules[j]);
                        String ftsTypeName = fts.getFeatureTypeName();

                        // if this rule matches, remove its range of scales from the 
                        // one for the else features, then create the rendered geometries
                        if (featureMatching(feature, ftsTypeName, filter)) {
                            rs.remove(ruleRange);
                            geometries = processSymbolizers(feature, symbolizers, ruleRange,
                                    renderedLayers, geometries, renderedGeometriesMap);
                        }
                    }
                }

                // is some scale range has not been covered by the rules, try with the else rules
                if (!rs.isEmpty()) {
                    for (int j = 0; j < rules.length; j++) {
                        if (rules[j].hasElseFilter()) { // if this rule is an else filter

                            NumberRange ruleRange = buildRuleRange(rules[j]);
                            Symbolizer[] symbolizers = rules[j].getSymbolizers();

                            for (Iterator rangeIt = rs.iterator(); rangeIt.hasNext();) {
                                NumberRange featureRange = (NumberRange) rangeIt.next();
                                NumberRange finalRange = (NumberRange) featureRange.intersect(ruleRange);

                                if ((finalRange != null) && !finalRange.isEmpty()) {
                                    geometries = processSymbolizers(feature, symbolizers,
                                            finalRange, renderedLayers, geometries,
                                            renderedGeometriesMap);
                                }
                            }
                        }
                    }
                }

                // the current layer may have changed 
                featureTypeCurrentLayer.set(i, geometries);
            }
        }

        reader.close();

        // add the current layer if not empty, for each feature type style
        for (int i = 0; i < featureStylers.length; i++) {
            JTSGeometries geometries = (JTSGeometries) featureTypeCurrentLayer.get(i);
            List renderedLayers = (List) featureTypeRenderedLayers.get(i);

            if ((geometries != null) && (geometries.getGeometries().size() > 0)) {
                renderedLayers.add(new SLDRenderedGeometries(geometries));
            }
        }

        // consolidate all layers of each feature type style into a single array
        List fullLayerList = new ArrayList();

        for (int i = 0; i < featureTypeRenderedLayers.size(); i++) {
            List layerList = (List) featureTypeRenderedLayers.get(i);
            fullLayerList.addAll(layerList);
        }

        RenderedLayer[] layers = new RenderedLayer[fullLayerList.size()];

        return (RenderedLayer[]) fullLayerList.toArray(layers);
    }

    /**
     * Create an array of rendered layers from the specified feature and style.
     *
     * @param features The feature.
     * @param sldStyle The style to apply.
     *
     * @return The rendered layer array for the specified feature and style.
     *
     * @throws TransformException if a transformation was required and failed.
     */
    public RenderedLayer[] create(final Feature[] features,
        final org.geotools.styling.Style sldStyle) throws TransformException {
        // the list that will contain all generated rendered layers
        List renderedLayers = new ArrayList();

        // and the first geometric layer
        JTSGeometries geometries = new JTSGeometries(coordinateSystem);

        // process the styles in order
        FeatureTypeStyle[] featureStylers = sldStyle.getFeatureTypeStyles();

        for (int i = 0; i < featureStylers.length; i++) {
            FeatureTypeStyle fts = featureStylers[i];

            // Prepare the else features map. Since we are preprocessing, we have to consider
            // what the elseFilters may catch at each scale, so we build a set of scale ranges
            // in which the elseFilter should apply by subtraction: we start with the full
            // floating point range and subtract the ranges covered by filters, if the
            // range set becomes empty, the feature is removed from the elseFeature map,
            // otherwise a Geometry is created from each remaining scale range
            LinkedHashMap elseFeatureRanges = new LinkedHashMap(features.length);

            for (int j = 0; j < features.length; j++) {
                RangeSet rs = new RangeSet(Double.class);
                rs.add(FULL_SCALE_RANGE);
                elseFeatureRanges.put(features[j], rs);
            }

            // process the rules for the current feature type style
            Rule[] rules = fts.getRules();

            for (int j = 0; j < rules.length; j++) {
                // if this rule is not an else filter
                if (!rules[j].hasElseFilter()) {
                    Filter filter = rules[j].getFilter();
                    Symbolizer[] symbolizers = rules[j].getSymbolizers();
                    List ruleFeatures = new ArrayList();
                    NumberRange ruleRange = buildRuleRange(rules[j]);
                    String ftsTypeName = fts.getFeatureTypeName();

                    // get all the features that must be rendered according to
                    // this rule and update the elseFeatures ranges (remove the scale range
                    // covered by this rule, if the else feature range is empty, remove it from
                    // the map
                    for (int k = 0; k < features.length; k++) {
                        Feature feature = features[k];

                        if (featureMatching(feature, ftsTypeName, filter)) {
                            ruleFeatures.add(feature);

                            RangeSet rs = (RangeSet) elseFeatureRanges.get(feature);

                            if (rs != null) {
                                rs.remove(ruleRange);

                                if (rs.isEmpty()) {
                                    elseFeatureRanges.remove(feature);
                                }
                            }
                        }
                    }

                    // now process the symbolizers on the catched features
                    for (Iterator it = ruleFeatures.iterator(); it.hasNext();) {
                        Feature feature = (Feature) it.next();
                        geometries = processSymbolizers(feature, symbolizers, ruleRange,
                                renderedLayers, geometries, null);
                    }
                }
            }

            // if some feature is not catched by the rules, see if some else rule applies
            if (elseFeatureRanges.size() > 0) {
                Set elseFeatures = elseFeatureRanges.keySet();

                for (int j = 0; j < rules.length; j++) {
                    if (rules[j].hasElseFilter()) { // if this rule is an else filter

                        NumberRange ruleRange = buildRuleRange(rules[j]);
                        Symbolizer[] symbolizers = rules[j].getSymbolizers();

                        // for each feature get the list of the ranges that are not catched by rules
                        // and compare with the elseRule range
                        for (Iterator featureIt = elseFeatures.iterator(); featureIt.hasNext();) {
                            Feature feature = (Feature) featureIt.next();
                            RangeSet rs = (RangeSet) elseFeatureRanges.get(feature);

                            for (Iterator rangeIt = rs.iterator(); rangeIt.hasNext();) {
                                NumberRange featureRange = (NumberRange) rangeIt.next();
                                NumberRange finalRange = (NumberRange) featureRange.intersect(ruleRange);

                                if ((finalRange != null) && !finalRange.isEmpty()) {
                                    geometries = processSymbolizers(feature, symbolizers,
                                            finalRange, renderedLayers, geometries, null);
                                }
                            }
                        }
                    }
                }
            }
        }

        // add the current layer if not empty
        if ((geometries != null) && (geometries.getGeometries().size() > 0)) {
            renderedLayers.add(new SLDRenderedGeometries(geometries));
        }

        RenderedLayer[] layers = new RenderedLayer[renderedLayers.size()];

        return (RenderedLayer[]) renderedLayers.toArray(layers);
    }

    /**
     * Process the symbolizers on the current feature. Will update the renderedLayer list and
     * return the new current JSTGeometries object (the vector layer under construction
     *
     * @param feature The feature to be styled
     * @param symbolizers The symbolizers that will apply styles to the feature
     * @param scaleRange The scale range in which the styled feature should be rendered
     * @param renderedLayers The list of renderedLayers
     * @param geometries The current vector layer under construction
     * @param geometryMap DOCUMENT ME!
     *
     * @return the new vector layer under construction (may be equal to <code>geometries</code>)
     *
     * @throws TransformException if a transformation was required and failed.
     */
    private JTSGeometries processSymbolizers(Feature feature, Symbolizer[] symbolizers,
        NumberRange scaleRange, List renderedLayers, JTSGeometries geometries,
        JTSGeometryMap geometryMap) throws TransformException {
        for (int i = 0; i < symbolizers.length; i++) {
            Symbolizer symb = symbolizers[i];
            Style style = styleFactory.createStyle(feature, symb, scaleRange);

            // if the symbolizer is a raster one we have to create
            // another rendered layer with the grid alone. 
            if (symb instanceof RasterSymbolizer) {
                if (geometries.getGeometries().size() > 0) {
                    renderedLayers.add(new SLDRenderedGeometries(geometries));
                    geometries = new JTSGeometries(coordinateSystem);
                }

                GridCoverage recoloredGrid = createRecoloredGrid(feature, (RasterSymbolizer) symb);
                renderedLayers.add(new RenderedGridCoverage(recoloredGrid.geophysics(false)));
            } else {
                String id = feature.getID();
                Geometry g = geometryMap.get(id, symb);

                if (g != null) {
                    // add may clone the geometry
                    g = geometries.add(new GeometryProxy(g));
                } else {
                    com.vividsolutions.jts.geom.Geometry geometry = findGeometry(feature, symb);

                    if (geometry != null) {
                        g = geometries.add(geometry);

                        if (!(geometry instanceof Point)) {
                            geometryMap.put(id, symb, g);
                        }
                    }
                }

                if (g != null) {
                    g.setStyle(style);
                    g.setID(feature.getID());
                }
            }
        }

        return geometries;
    }

    /**
     * Applies the symbolizer to the grid coverage, effectively creating a new coverage with a new
     * set of mappings from geophysics to non geophysics categories
     *
     * @param feature
     * @param symbolizer
     *
     * @return
     */
    private GridCoverage createRecoloredGrid(Feature feature, RasterSymbolizer symbolizer) {
        ColorMap colorMap = symbolizer.getColorMap();
        GridCoverage grid = (GridCoverage) feature.getAttribute("grid");

        if (colorMap == null || colorMap.getColorMapEntries() == null || colorMap.getColorMapEntries().length == 0) {
            return grid;
        }

        SampleDimension sampleDimension = null;

        if (colorMap.getType() == ColorMap.TYPE_RAMP || colorMap.getType() == ColorMap.TYPE_INTERVALS) {
            ColorMapEntry[] entries = colorMap.getColorMapEntries();
            int rangeStart = 1;
            double rangeStep = 254 / (entries.length - 1);
            Category[] categories = new Category[entries.length];

            for (int i = 0; i < (entries.length - 1); i++) {
                // get the entries
                ColorMapEntry curr = entries[i];
                ColorMapEntry next = entries[i + 1];
                // compute the colors
                Color colorCurr = decodeColor(curr, feature);
                Color colorNext = decodeColor(next, feature);
                // compute the sample range
                int rangeEnd = (int) Math.round(1 + ((i + 1) * rangeStep));
                NumberRange sampleRange = new NumberRange(rangeStart, rangeEnd);
                // compute the geophisics range
                float gprStart = ((Number) curr.getQuantity().getValue(feature)).floatValue();
                float gprEnd = ((Number) next.getQuantity().getValue(feature)).floatValue();
                NumberRange geophisicsRange = new NumberRange(gprStart, true, gprEnd, false);
                // create the category according to the color map style
                if(colorMap.getType() == ColorMap.TYPE_RAMP) {
                    categories[i] = new Category(curr.getLabel(), new Color[] { colorCurr, colorNext },
                        sampleRange, geophisicsRange);
                } else {
                    categories[i] = new Category(curr.getLabel(), new Color[] { colorCurr, colorCurr },
                                            sampleRange, geophisicsRange);
                }

                // new range start, avoid overlap
                rangeStart = rangeEnd + 1;
            }

            // TODO: stupid hack, find a way to avoid drawing pixels above the max value...
            float maxValue = ((Number) entries[entries.length - 1].getQuantity().getValue(feature))
                .floatValue();
            
            categories[entries.length - 1] = new Category("trailing nulls",
                    new Color[] { new Color(0, 0, 0, 0) }, new NumberRange(rangeStart, 255),
                    new NumberRange(maxValue, true, Integer.MAX_VALUE, false));
            sampleDimension = new SampleDimension(categories,
                    grid.getSampleDimensions()[0].getUnits());
        } else if (colorMap.getType() == ColorMap.TYPE_VALUES) {
            // TODO: damn, this does not work!
            ColorMapEntry[] entries = colorMap.getColorMapEntries();
            Category[] categories = new Category[entries.length];

            for (int i = 0; i < entries.length; i++) {
                ColorMapEntry curr = entries[i];
                Color colorCurr = Color.decode((String) curr.getColor().getValue(feature));
                float value = ((Number) curr.getQuantity().getValue(feature)).floatValue();
                categories[i] = new Category(curr.getLabel(), colorCurr, value);
            }
            sampleDimension = new SampleDimension(categories, grid.getSampleDimensions()[0].getUnits());
        }

        // create the new sample dimension and finally the converted grid coverage
        SampleDimension geoSd = sampleDimension.geophysics(true);
        SampleDimension[] bands = new SampleDimension[] { geoSd };
        String gridName = "Rendered " + grid.getName(Locale.getDefault());
        RenderedImage image = grid.geophysics(true).getRenderedImage();
        GridCoverage newGrid = new GridCoverage(gridName, image, grid.getCoordinateSystem(),
                grid.getGridGeometry().getGridToCoordinateSystem(), bands,
                new GridCoverage[] { grid }, grid.getProperties());

        return newGrid;
    }
    
    private Color decodeColor(ColorMapEntry entry, Feature feature) {
        Color color = Color.decode((String) entry.getColor().getValue(feature));
        double opacity = ((Number) entry.getOpacity().getValue(feature)).doubleValue();
        int alpha = (int) Math.round(opacity * 255.0);
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    /**
     * Checks wheter the feature matches the filter and the type name
     *
     * @param feature
     * @param ftsTypeName The type name that the feature should match
     * @param filter The filter that the feature should be contained in
     *
     * @return true if the feature matches both
     */
    private boolean featureMatching(Feature feature, String ftsTypeName, Filter filter) {
        String typeName = feature.getFeatureType().getTypeName();

        if (typeName == null) {
            return false;
        }

        if (feature.getFeatureType().isDescendedFrom(null, ftsTypeName)
                || typeName.equalsIgnoreCase(ftsTypeName)) {
            return (filter == null) || filter.contains(feature);
        }

        return false;
    }

    /**
     * Builds a range from the rule scale specification
     *
     * @param r The rule
     *
     * @return The range with minimum and maximun scale (will use minimum and maximum double values
     *         if unbounded).
     */
    private NumberRange buildRuleRange(final Rule r) {
        double min = r.getMinScaleDenominator();
        double max = r.getMaxScaleDenominator();

        if (Double.isInfinite(min)) {
            min = Double.MIN_VALUE;
        }

        if (Double.isInfinite(max)) {
            max = Double.MAX_VALUE;
        }

        return new NumberRange(min, max);
    }

    /**
     * Finds the geometric attribute requested by the symbolizer
     *
     * @param f The feature
     * @param s The symbolizer
     *
     * @return The geometry requested in the symbolizer, or the default geometry if none is
     *         specified
     */
    private com.vividsolutions.jts.geom.Geometry findGeometry(Feature f, Symbolizer s) {
        String geomName = getGeometryPropertyName(s);

        // get the geometry
        com.vividsolutions.jts.geom.Geometry geom;

        if (geomName == null) {
            geom = f.getDefaultGeometry();
        } else {
            geom = (com.vividsolutions.jts.geom.Geometry) f.getAttribute(geomName);
        }

        // if the symbolizer is a point or text symbolizer generate a suitable location to place the
        // point in order to avoid recomputing that location at each rendering step
        if ((s instanceof PointSymbolizer || s instanceof TextSymbolizer)
                && !(geom instanceof Point)) {
            if (geom instanceof LineString && !(geom instanceof LinearRing)) {
                // use the mid point to represent the point/text symbolizer anchor
                Coordinate[] coordinates = geom.getCoordinates();
                Coordinate start = coordinates[0];
                Coordinate end = coordinates[1];
                Coordinate mid = new Coordinate((start.x + end.x) / 2, (start.y + end.y) / 2);
                geom = geom.getFactory().createPoint(mid);
            } else {
                // otherwise use the centroid of the polygon
                geom = geom.getCentroid();
            }
        }

        return geom;
    }

    private static String getGeometryPropertyName(Symbolizer s) {
        String geomName = null;

        // TODO: fix the styles, the getGeometryPropertyName should probably be moved into an interface...
        if (s instanceof PolygonSymbolizer) {
            geomName = ((PolygonSymbolizer) s).getGeometryPropertyName();
        } else if (s instanceof PointSymbolizer) {
            geomName = ((PointSymbolizer) s).getGeometryPropertyName();
        } else if (s instanceof LineSymbolizer) {
            geomName = ((LineSymbolizer) s).getGeometryPropertyName();
        } else if (s instanceof TextSymbolizer) {
            geomName = ((TextSymbolizer) s).getGeometryPropertyName();
        }

        return geomName;
    }

    /**
     * A map from feature ids to rendered geometries that is used to avoid wrapping the same JTS
     * geometry twice, thus saving memory and process time need to decimate again the geometry. It
     * is based on ID since there is no guarantee that the feature source will generate exactly
     * the same geometry object loading twice from the same data store (it won't unless it is
     * memory based or there is a cache), but having the hash map use the equal comparison on
     * geometries would be a performance problem (normal form generation and coordinate to
     * coordinate comparison it way too expensive)
     */
    private static class JTSGeometryMap implements FeatureListener {
        HashMap geometryMap;
        RenderedFeatureKey rfk = new RenderedFeatureKey("", "");

        public JTSGeometryMap(FeatureSource featureSource) {
            featureSource.addFeatureListener(this);
            geometryMap = new HashMap();
        }

        public void put(String ID, Symbolizer symb, Geometry renderedGeometry) {
            if (symb instanceof PointSymbolizer || symb instanceof TextSymbolizer) {
                return; // do not cache with these symbolizers, they are problematic
            } else {
                geometryMap.put(new RenderedFeatureKey(ID, getGeometryPropertyName(symb)),
                    renderedGeometry);
            }
        }

        public Geometry get(String ID, Symbolizer symb) {
            if (symb instanceof PointSymbolizer || symb instanceof TextSymbolizer) {
                return null;
            } else {
                rfk.init(ID, getGeometryPropertyName(symb));

                return (Geometry) geometryMap.get(rfk);
            }
        }

        /**
         * If data in a feature source changes we need to drop the or we would generate a memory
         * leak. The specific case is a feature source that dinamically changes its features, the
         * reference to the feature source would not change, but we would collect geometries that
         * may no longer exist
         *
         * @see org.geotools.data.FeatureListener#changed(org.geotools.data.FeatureEvent)
         */
        public void changed(FeatureEvent featureEvent) {
            int eventType = featureEvent.getEventType();

            if ((eventType == FeatureEvent.FEATURES_REMOVED)
                    || (eventType == FeatureEvent.FEATURES_CHANGED)) {
                geometryMap = new HashMap();
            }
        }
    }

    private static class RenderedFeatureKey {
        public String ID;
        public String geomPropName;

        public RenderedFeatureKey(String ID, String geomPropName) {
            this.ID = ID;
            this.geomPropName = geomPropName;
        }

        public void init(String ID, String geomPropName) {
            this.ID = ID;
            this.geomPropName = geomPropName;
        }

        /**
         * @see java.lang.Object#equals(java.lang.Object)
         */
        public boolean equals(Object obj) {
            if (!(obj instanceof RenderedFeatureKey)) {
                return false;
            }

            RenderedFeatureKey other = (RenderedFeatureKey) obj;

            if (geomPropName != null) {
                return ID.equals(other.ID) && geomPropName.equals(other.geomPropName);
            } else {
                return ID.equals(other.ID) && (other.geomPropName == null);
            }
        }

        /**
         * @see java.lang.Object#hashCode()
         */
        public int hashCode() {
            return (37 * (17 * ID.hashCode()))
            + ((geomPropName == null) ? 0 : geomPropName.hashCode());
        }
    }
}
