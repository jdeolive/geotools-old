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


// JTS dependencies
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;

// Geotools dependencies
import org.geotools.feature.Feature;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.geotools.gc.GridCoverage;
import org.geotools.renderer.geom.Geometry;
import org.geotools.renderer.geom.JTSGeometries;
import org.geotools.renderer.style.SLDStyleFactory;
import org.geotools.renderer.style.Style;
import org.geotools.resources.XArray;
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
import java.io.IOException;

// J2SE dependencies
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;


/**
 * A factory creating {@link RenderedLayer}s from {@link Feature}s and {@link Style}s.
 *
 * @author Andrea Aime
 * @author Martin Desruisseaux
 * @version $Id: RenderedLayerFactory.java,v 1.17 2003/12/04 23:19:47 aaime Exp $
 */
public class RenderedLayerFactory {
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.renderer.j2d");

    /** The full range of map scale. */
    private static final NumberRange FULL_SCALE_RANGE = new NumberRange(0.0, Double.MAX_VALUE);

    /** Prepare the style factory that will convert SLD styles into resolved styles. */
    private final SLDStyleFactory styleFactory = new SLDStyleFactory();

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
    public RenderedLayer[] create(final FeatureSource featureSource,
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
     * @param features The feature.
     * @param SLDStyle The style to apply.
     *
     * @return The rendered layer array for the specified feature and style.
     *
     * @throws TransformException if a transformation was required and failed.
     */
    public RenderedLayer[] create(final Feature[] features,
        final org.geotools.styling.Style SLDStyle) throws TransformException {
        // ... the list that will contain all generated rendered layers
        List renderedLayers = new ArrayList();

        // ... and the first geometric layer
        JTSGeometries geometries = new JTSGeometries(coordinateSystem);

        // process the styles in order
        FeatureTypeStyle[] featureStylers = SLDStyle.getFeatureTypeStyles();

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
                                renderedLayers, geometries);
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
                                            finalRange, renderedLayers, geometries);
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
     *
     * @return the new vector layer under construction (may be equal to <code>geometries</code>)
     *
     * @throws TransformException if a transformation was required and failed.
     */
    private JTSGeometries processSymbolizers(Feature feature, Symbolizer[] symbolizers,
        NumberRange scaleRange, List renderedLayers, JTSGeometries geometries)
        throws TransformException {
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

                GridCoverage grid = (GridCoverage) feature.getAttribute("grid");
                renderedLayers.add(new RenderedGridCoverage(grid));
            } else {
                com.vividsolutions.jts.geom.Geometry geometry = findGeometry(feature, symb);

                if (geometry != null) {
                    geometries.add(geometry).setStyle(style);
                }
            }
        }

        return geometries;
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
    public com.vividsolutions.jts.geom.Geometry findGeometry(Feature f, Symbolizer s) {
        String geomName = null;

        // TODO: fix the styles, should be the same method name and probably should be moved 
        // into an interface...
        if (s instanceof PolygonSymbolizer) {
            geomName = ((PolygonSymbolizer) s).getGeometryPropertyName();
        } else if (s instanceof PointSymbolizer) {
            geomName = ((PointSymbolizer) s).getGeometryPropertyName();
        } else if (s instanceof LineSymbolizer) {
            geomName = ((LineSymbolizer) s).getGeometryPropertyName();
        } else if (s instanceof TextSymbolizer) {
            geomName = ((TextSymbolizer) s).getGeometryPropertyName();
        }

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
            com.vividsolutions.jts.geom.Geometry jtsGeom = (com.vividsolutions.jts.geom.Geometry) geom;

            if (geom instanceof LineString && !(geom instanceof LinearRing)) {
                // use the mid point to represent the point/text symbolizer anchor
                Coordinate[] coordinates = jtsGeom.getCoordinates();
                Coordinate start = coordinates[0];
                Coordinate end = coordinates[1];
                Coordinate mid = new Coordinate((start.x + end.x) / 2, (start.y + end.y) / 2);
                geom = new Point(mid, jtsGeom.getPrecisionModel(), 0);
            } else {
                // otherwise use the centroid of the polygon
                geom = jtsGeom.getCentroid();
            }
        }

        return geom;
    }
}
