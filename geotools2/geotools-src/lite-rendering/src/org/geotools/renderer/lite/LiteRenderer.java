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
package org.geotools.renderer.lite;


//Java Topology Suite
import com.vividsolutions.jts.geom.*;

//geotools imports
import org.geotools.data.*;
import org.geotools.datasource.extents.*;
import org.geotools.feature.*;
import org.geotools.filter.*;
import org.geotools.gc.GridCoverage;
import org.geotools.map.*;
import org.geotools.renderer.*;
import org.geotools.styling.*;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;

//standard java awt imports
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.*;

// file handling
import java.io.*;
import java.net.*;

//util imports
import java.util.*;
import java.util.logging.Level;

//Logging system
import java.util.logging.Logger;

// image handling
import javax.imageio.ImageIO;


/**
 * A lite implementation of the Renderer and Renderer2D interfaces. Lite means
 * that:
 * 
 * <ul>
 * <li>
 * The code is relatively simple to understand, so it can be used as a simple
 * example of an SLD compliant rendering code
 * </li>
 * <li>
 * Uses as few memory as possible
 * </li>
 * </ul>
 * 
 * Use this class if you need a stateless renderer that provides low memory
 * footprint and  decent rendering performance on the first call but don't
 * need good optimal performance on subsequent calls on the same data. Notice:
 * for the time being, this class doesn't support GridCoverage stylers, that
 * will be rendered using the non geophisics version of the GridCoverage, if
 * available, with the geophisics one, otherwise.
 *
 * @author James Macgill
 * @author Andrea Aime
 * @version $Id: LiteRenderer.java,v 1.6 2003/07/12 10:56:42 aaime Exp $
 */
public class LiteRenderer implements Renderer, Renderer2D {
    /** The logger for the rendering module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.rendering");

    /** where the centre of an untransormed mark is */
    private static com.vividsolutions.jts.geom.Point markCentrePoint;
    static Set fontFamilies = null;
    static java.util.Map loadedFonts = new HashMap();
    static java.awt.Canvas obs = new java.awt.Canvas();
    private static double tolerance = 1e-6;

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final java.util.Map joinLookup = new java.util.HashMap();

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final java.util.Map capLookup = new java.util.HashMap();

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final java.util.Map fontStyleLookup = new java.util.HashMap();

    /** Holds a list of well-known marks. */
    static Set wellKnownMarks = new java.util.HashSet();
    static Set supportedGraphicFormats = new java.util.HashSet();
    static ImageLoader imageLoader = new ImageLoader();

    static { //static block to populate the lookups
        joinLookup.put("miter", new Integer(BasicStroke.JOIN_MITER));
        joinLookup.put("bevel", new Integer(BasicStroke.JOIN_BEVEL));
        joinLookup.put("round", new Integer(BasicStroke.JOIN_ROUND));

        capLookup.put("butt", new Integer(BasicStroke.CAP_BUTT));
        capLookup.put("round", new Integer(BasicStroke.CAP_ROUND));
        capLookup.put("square", new Integer(BasicStroke.CAP_SQUARE));

        fontStyleLookup.put("normal", new Integer(java.awt.Font.PLAIN));
        fontStyleLookup.put("italic", new Integer(java.awt.Font.ITALIC));
        fontStyleLookup.put("oblique", new Integer(java.awt.Font.ITALIC));
        fontStyleLookup.put("bold", new Integer(java.awt.Font.BOLD));

        /**
         * A list of wellknownshapes that we know about: square, circle,
         * triangle, star, cross, x. Note arrow is an implementation specific
         * mark.
         */
        wellKnownMarks.add("Square");
        wellKnownMarks.add("Triangle");
        wellKnownMarks.add("Cross");
        wellKnownMarks.add("Circle");
        wellKnownMarks.add("Star");
        wellKnownMarks.add("X");
        wellKnownMarks.add("Arrow");
        wellKnownMarks.add("square");
        wellKnownMarks.add("triangle");
        wellKnownMarks.add("cross");
        wellKnownMarks.add("circle");
        wellKnownMarks.add("star");
        wellKnownMarks.add("x");
        wellKnownMarks.add("arrow");

        String[] types = ImageIO.getReaderMIMETypes();

        for (int i = 0; i < types.length; i++) {
            supportedGraphicFormats.add(types[i]);
        }

        //        supportedGraphicFormats.add("image/gif");
        //        supportedGraphicFormats.add("image/jpg");
        //        supportedGraphicFormats.add("image/png");
        Coordinate c = new Coordinate(100, 100);
        GeometryFactory fac = new GeometryFactory();
        markCentrePoint = fac.createPoint(c);
    }

    /**
     * Context which contains LayerList, BoundingBox which needs to be
     * rendered.
     */
    private Context context;

    /**
     * Flag which determines if the renderer is interactive or not. An
     * interactive renderer will return rather than waiting for time consuming
     * operations to complete (e.g. Image Loading). A non-interactive renderer
     * (e.g. a SVG or PDF renderer) will block for these operations.
     */
    private boolean interactive = true;

    /**
     * Flag which controls behaviour for applying affine transformation to the
     * graphics object.  If true then the transform will be concatenated to
     * the existing transform.  If false it will be replaced.
     */
    private boolean concatTransforms = false;
    private Envelope mapExtent = null;

    /** Graphics object to be rendered to. Controlled by set output. */
    private Graphics2D graphics;

    /** The size of the output area in output units. */
    private Rectangle screenSize;

    /**
     * The ratio required to scale the features to be rendered so that they fit
     * into the output space.
     */
    private double scaleDenominator;

    /** Maximun displacement for generalization during rendering */
    private double maxDistance = 1.0;

    /**
     * Creates a new instance of LiteRenderer.
     *
     * @deprecated Renderer is to be created with a Context.
     */
    public LiteRenderer() {
        LOGGER.fine("creating new lite renderer");
    }

    /**
     * Creates a new instance of Java2DRenderer.
     *
     * @param context Contains pointers to layers, bounding box, and style
     *        required for rendering.
     */
    public LiteRenderer(Context context) {
        this.context = context;
    }

    /**
     * Sets the flag which controls behaviour for applying affine
     * transformation to the graphics object.
     *
     * @param flag If true then the transform will be concatenated to the
     *        existing transform.  If false it will be replaced.
     */
    public void setConcatTransforms(boolean flag) {
        concatTransforms = flag;
    }

    /**
     * Flag which controls behaviour for applying affine transformation to the
     * graphics object.
     *
     * @return a boolean flag. If true then the transform will be concatenated
     *         to the existing transform.  If false it will be replaced.
     */
    public boolean getConcatTransforms() {
        return concatTransforms;
    }

    /**
     * Called before {@link render}, this sets where any output will be sent.
     *
     * @param g A graphics object for future rendering to be sent to.  Note:
     *        must be an instance of lite renderer.
     * @param bounds The size of the output area, required so that scale can be
     *        calculated.
     *
     * @deprecated Graphics and bounds is to be set in renderer().
     */
    public void setOutput(Graphics g, Rectangle bounds) {
        graphics = (Graphics2D) g;
        screenSize = bounds;
    }

    /**
     * Setter for property scaleDenominator.
     *
     * @param scaleDenominator New value of property scaleDenominator.
     */
    protected void setScaleDenominator(double scaleDenominator) {
        this.scaleDenominator = scaleDenominator;
    }

    /**
     * Render features based on the LayerList, BoundBox and Style specified in
     * this.context. Don't mix calls to paint and setOutput, when calling this
     * method the graphics set in the setOutput method is discarded.
     *
     * @param graphics The graphics object to draw to.
     * @param paintArea The size of the output area in output units (eg:
     *        pixels).
     * @param transform A transform which converts World coordinates to Screen
     *        coordinates.
     *
     * @task TODO Need to check if the Layer CoordinateSystem is different to
     *       the BoundingBox rendering CoordinateSystem and if so, then
     *       transform the coordinates.
     */
    public void paint(Graphics2D graphics, Rectangle paintArea,
        AffineTransform transform) {
        Date start = new Date();

        if ((graphics == null) || (paintArea == null)) {
            LOGGER.info("renderer passed null arguements");

            return;
        }

        try {
            // set the passed graphic as the current graphic but be sure to release it before
            this.graphics = graphics;

            Layer[] layers = context.getLayerList().getLayers();

            for (int l = 0; l < layers.length; l++) {
                Layer layer = layers[l];

                if (!layer.getVisability()) {
                    // Only render layer when layer is visible
                    continue;
                }

                FeatureCollection fc = new FeatureCollectionDefault(layer.getDataSource());

                try {
                    Feature[] features = fc.getFeatures(new EnvelopeExtent(
                                this.context.getBbox().getAreaOfInterest()));
                    mapExtent = this.context.getBbox().getAreaOfInterest();

                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("renderering " + features.length +
                            " features");
                    }

                    // TODO: Need to check if the Layer CoordinateSystem is
                    // different to the BoundingBox rendering CoordinateSystem and
                    // if so, then transform the coordinates.
                    AffineTransform at = transform;

                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("Affine Transform is " + at);
                    }

                    /* If we are rendering to a component which has already set up some
                     * form of transformation then we can concatenate our
                     * transformation to it. An example of this is the ZoomPane
                     * component of the swinggui module.*/
                    if (concatTransforms) {
                        graphics.getTransform().concatenate(at);
                    } else {
                        graphics.setTransform(at);
                    }

                    setScaleDenominator(1 / graphics.getTransform().getScaleX());

                    //extract the feature type stylers from the style object and
                    //process them
                    FeatureTypeStyle[] featureStylers = layer.getStyle()
                                                             .getFeatureTypeStyles();
                    processStylers(features, featureStylers);

                    Date end = new Date();

                    if (LOGGER.getLevel() == Level.INFO) { //change to fine when finished
                        LOGGER.info("Time to render " + features.length +
                            " is " + (end.getTime() - start.getTime()) +
                            " milliSecs");
                    }
                } catch (Exception exception) {
                    LOGGER.warning("Exception " + exception +
                        " rendering layer " + layer);
                }
            }
        } finally {
            this.graphics = null;
        }
    }

    /**
     * Performs the actual rendering process to the graphics context set in
     * setOutput.
     * 
     * <p>
     * The style parameter controls the appearance features.  Rules within the
     * style object may cause some features to be rendered multiple times or
     * not at all.
     * </p>
     *
     * @param features An array of features to be rendered.
     * @param map Controls the full extent of the input space.  Used in the
     *        calculation of scale.
     * @param s A style object.  Contains a set of FeatureTypeStylers that are
     *        to be applied in order to control the rendering process.
     */
    public void render(Feature[] features, Envelope map, Style s) {
        if (graphics == null) {
            LOGGER.info("renderer passed null graphics");

            return;
        }

        long startTime = 0;

        if (LOGGER.isLoggable(Level.FINE)) {
            startTime = System.currentTimeMillis();
        }

        mapExtent = map;

        //set up the affine transform and calculate scale values
        AffineTransform at = setUpTransform(mapExtent, screenSize);

        /* If we are rendering to a component which has already set up some form
         * of transformation then we can concatenate our transformation to it.
         * An example of this is the ZoomPane component of the swinggui module.*/
        if (concatTransforms) {
            graphics.getTransform().concatenate(at);
        } else {
            graphics.setTransform(at);
        }

        scaleDenominator = 1 / graphics.getTransform().getScaleX();

        //extract the feature type stylers from the style object and process them
        FeatureTypeStyle[] featureStylers = s.getFeatureTypeStyles();
        processStylers(features, featureStylers);

        if (LOGGER.isLoggable(Level.FINE)) {
            long endTime = System.currentTimeMillis();
            double elapsed = (endTime - startTime) / 1000.0;
            LOGGER.fine("Rendered " + features.length + " features in " +
                elapsed + " sec.");
        }
    }

    private AffineTransform setUpTransform(Envelope mapExtent,
        Rectangle screenSize) {
        //double scale = Math.min(screenSize.getHeight()/ mapExtent.getHeight(), 
        //                        screenSize.getWidth()/ mapExtent.getWidth());
        double scaleX = screenSize.getWidth() / mapExtent.getWidth();
        double scaleY = screenSize.getHeight() / mapExtent.getHeight();

        //TODO: angle is almost certainly not needed and should be dropped
        //double angle = 0; //-Math.PI/8d;// rotation angle
        double tx = -mapExtent.getMinX() * scaleX; // x translation - mod by ian
        double ty = (mapExtent.getMinY() * scaleY) + screenSize.getHeight(); // y translation

        AffineTransform at = new AffineTransform(scaleX, 0.0d, 0.0d, -scaleY,
                tx, ty);

        return at;
    }

    /**
     *
     */
    public Coordinate pixelToWorld(int x, int y, Envelope map) {
        if (graphics == null) {
            LOGGER.info("no graphics yet deffined");

            return null;
        }

        //set up the affine transform and calculate scale values
        AffineTransform at = setUpTransform(map, screenSize);

        /* If we are rendering to a component which has already set up some form
         * of transformation then we can concatenate our transformation to it.
         * An example of this is the ZoomPane component of the swinggui module.*/
        if (concatTransforms) {
            graphics.getTransform().concatenate(at);
        } else {
            graphics.setTransform(at);
        }

        try {
            Point2D result = at.inverseTransform(new Point2D.Double(x, y),
                    new Point2D.Double());
            Coordinate c = new Coordinate(result.getX(), result.getY());

            return c;
        } catch (Exception e) {
            LOGGER.warning(e.toString());
        }

        return null;
    }

    private boolean isWithInScale(Rule r) {
        return ((r.getMinScaleDenominator() - tolerance) <= scaleDenominator) &&
        ((r.getMaxScaleDenominator() + tolerance) > scaleDenominator);
    }

    /**
     * Applies each feature type styler in turn to all of the features. This
     * perhaps needs some explanation to make it absolutely clear.
     * featureStylers[0] is applied to all features before featureStylers[1]
     * is applied.  This can have important consequences as regards the
     * painting order.
     * 
     * <p>
     * In most cases, this is the desired effect.  For example, all line
     * features may be rendered with a fat line and then a thin line.  This
     * produces a 'cased' effect without any strange overlaps.
     * </p>
     * 
     * <p>
     * This method is internal and should only be called by render.
     * </p>
     * 
     * <p></p>
     *
     * @param features An array of features to be rendered
     * @param featureStylers An array of feature stylers to be applied
     */
    private void processStylers(final Feature[] features,
        final FeatureTypeStyle[] featureStylers) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("processing " + featureStylers.length + " stylers");
        }

        // create the arrayList of features that sits whithin the map envelop
        List insideFeatures = new ArrayList();

        for (int i = 0; i < features.length; i++) {
            Feature feature = features[i];
            Envelope internal = feature.getDefaultGeometry()
                                       .getEnvelopeInternal();

            if (mapExtent.overlaps(internal)) {
                insideFeatures.add(feature);
            }
        }

        // if nothing to do exit immediatly
        if (insideFeatures.size() == 0) {
            return;
        }

        // process stylers in order
        for (int i = 0; i < featureStylers.length; i++) {
            FeatureTypeStyle fts = featureStylers[i];

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("about to draw " + features.length + " feature");
            }

            // get rules
            Rule[] rules = fts.getRules();

            for (Iterator it = insideFeatures.iterator(); it.hasNext();) {
                Feature feature = (Feature) it.next();

                for (int k = 0; k < rules.length; k++) {
                    // if this rule applies
                    if (isWithInScale(rules[k]) && !rules[k].hasElseFilter()) {
                        Filter filter = rules[k].getFilter();
                        Symbolizer[] symbolizers = rules[k].getSymbolizers();

                        String typeName = feature.getSchema().getTypeName();

                        if (((typeName != null) &&
                                typeName.equalsIgnoreCase(
                                    fts.getFeatureTypeName())) &&
                                ((filter == null) || filter.contains(feature))) {
                            processSymbolizers(feature, symbolizers);
                        }
                    }
                }

                for (int k = 0; k < rules.length; k++) {
                    // if this rule applies
                    if (isWithInScale(rules[k]) && rules[k].hasElseFilter()) {
                        Symbolizer[] symbolizers = rules[k].getSymbolizers();
                        processSymbolizers(feature, symbolizers);
                    }
                }
            }
        }
    }

    /**
     * Applies each of a set of symbolizers in turn to a given feature.
     * 
     * <p>
     * This is an internal method and should only be called by processStylers.
     * </p>
     *
     * @param feature The feature to be rendered
     * @param symbolizers An array of symbolizers which actually perform the
     *        rendering.
     */
    private void processSymbolizers(final Feature feature,
        final Symbolizer[] symbolizers) {
        for (int m = 0; m < symbolizers.length; m++) {
            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("applying symbolizer " + symbolizers[m]);
            }

            if (symbolizers[m] instanceof PolygonSymbolizer) {
                renderPolygon(feature, (PolygonSymbolizer) symbolizers[m]);
            } else if (symbolizers[m] instanceof LineSymbolizer) {
                renderLine(feature, (LineSymbolizer) symbolizers[m]);
            } else if (symbolizers[m] instanceof PointSymbolizer) {
                renderPoint(feature, (PointSymbolizer) symbolizers[m]);
            } else if (symbolizers[m] instanceof TextSymbolizer) {
                renderText(feature, (TextSymbolizer) symbolizers[m]);
            } else if (symbolizers[m] instanceof RasterSymbolizer) {
                renderRaster(feature, (RasterSymbolizer) symbolizers[m]);
            }
        }
    }

    /**
     * Renders the given feature as a polygon using the specified symbolizer.
     * Geometry types other than inherently area types can be used. If a line
     * is used then the line string is closed for filling (only) by connecting
     * its end point to its start point. This is an internal method that
     * should only be called by processSymbolizers. TODO: the properties of a
     * symbolizer may, in part, be dependent on TODO: attributes of the
     * feature.  This is not yet supported.
     *
     * @param feature The feature to render
     * @param symbolizer The polygon symbolizer to apply
     */
    private void renderPolygon(Feature feature, PolygonSymbolizer symbolizer) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("rendering polygon with a scale of " +
                this.scaleDenominator);
        }

        Fill fill = symbolizer.getFill();
        String geomName = symbolizer.geometryPropertyName();
        Geometry geom = findGeometry(feature, geomName);

        if (geom.isEmpty()) {
            return;
        }

        Shape path = createPath(geom);

        if (fill != null) {
            applyFill(graphics, fill, feature);

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("paint in renderPoly: " + graphics.getPaint());
            }

            graphics.fill(path);

            // shouldn't we reset the graphics when we return finished?
            resetFill();
        }

        if (symbolizer.getStroke() != null) {
            Stroke stroke = symbolizer.getStroke();
            applyStroke(graphics, stroke, feature);

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("path is " +
                    graphics.getTransform().createTransformedShape(path)
                            .getBounds2D().toString());
            }

            if (stroke.getGraphicStroke() == null) {
                graphics.draw(path);
            } else {
                // set up the graphic stroke
                drawWithGraphicStroke(graphics, path,
                    stroke.getGraphicStroke(), feature);
            }
        }
    }

    /**
     * Renders the given feature as a line using the specified symbolizer. This
     * is an internal method that should only be called by processSymbolizers
     * Geometry types other than inherently linear types can be used. If a
     * point geometry is used, it should be interpreted as a line of zero
     * length and two end caps.  If a polygon is used (or other "area" type)
     * then its closed outline will be used as the line string (with no end
     * caps). TODO: the properties of a symbolizer may, in part, be dependent
     * on TODO: attributes of the feature.  This is not yet supported.
     *
     * @param feature The feature to render
     * @param symbolizer The polygon symbolizer to apply
     */
    private void renderLine(Feature feature, LineSymbolizer symbolizer) {
        if (symbolizer.getStroke() == null) {
            return;
        }

        Stroke stroke = symbolizer.getStroke();
        applyStroke(graphics, stroke, feature);

        String geomName = symbolizer.geometryPropertyName();
        Geometry geom = findGeometry(feature, geomName);

        if (geom.isEmpty()) {
            return;
        }

        Shape path = createPath(geom);

        if (stroke.getGraphicStroke() == null) {
            graphics.draw(path);
        } else {
            // set up the graphic stroke
            drawWithGraphicStroke(graphics, path, stroke.getGraphicStroke(),
                feature);
        }
    }

    private void renderPoint(Feature feature, PointSymbolizer symbolizer) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("rendering a point from " + feature);
        }

        org.geotools.styling.Graphic sldgraphic = symbolizer.getGraphic();

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("sldgraphic = " + sldgraphic);
        }

        String geomName = symbolizer.geometryPropertyName();
        Geometry geom = findGeometry(feature, geomName);

        if (geom.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("empty geometry");
            }

            return;
        }

        // TODO: consider if mark and externalgraphic should share an ancestor?

        /*
           if (null != (Object)sldgraphic.getExternalGraphics()){
               LOGGER.finer("rendering External graphic");
               renderExternalGraphic(geom, sldgraphic, feature);
           } else{
               LOGGER.finer("rendering mark");
               renderMark(geom, sldgraphic, feature);
           }
         */
        Symbol[] symbols = sldgraphic.getSymbols();
        boolean flag = false;

        for (int i = 0; i < symbols.length; i++) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("trying to render symbol " + i);
            }

            if (symbols[i] instanceof ExternalGraphic) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering External graphic");
                }

                flag = renderExternalGraphic(geom, sldgraphic, feature,
                        (ExternalGraphic) symbols[i]);

                if (flag) {
                    return;
                }
            }

            if (symbols[i] instanceof Mark) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering mark @ PointRenderer " +
                        symbols[i].toString());
                }

                flag = renderMark(geom, sldgraphic, feature, (Mark) symbols[i]);

                if (flag) {
                    return;
                }
            }

            if (symbols[i] instanceof TextMark) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering text symbol");
                }

                flag = renderTextSymbol(geom, sldgraphic, feature,
                        (TextMark) symbols[i]);

                if (flag) {
                    return;
                }
            }
        }
    }

    private void renderText(Feature feature, TextSymbolizer symbolizer) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("rendering text");
        }

        String geomName = symbolizer.getGeometryPropertyName();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("geomName = " + geomName);
        }

        Geometry geom = findGeometry(feature, geomName);

        if (geom.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("empty geometry");
            }

            return;
        }

        Object obj = symbolizer.getLabel().getValue(feature);

        if (obj == null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Null label in render text");
            }

            return;
        }

        String label = obj.toString();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("label is " + label);
        }

        if (label == null) {
            return;
        }

        org.geotools.styling.Font[] fonts = symbolizer.getFonts();
        java.awt.Font javaFont = getFont(feature, fonts);

        LabelPlacement placement = symbolizer.getLabelPlacement();

        if (javaFont != null) {
            graphics.setFont(javaFont);
        }

        TextLayout tl = new TextLayout(label, graphics.getFont(),
                graphics.getFontRenderContext());
        Rectangle2D textBounds = tl.getBounds();
        double x = 0;
        double y = 0;
        double rotation = 0;
        double tx = 0;
        double ty = 0;

        if (placement instanceof PointPlacement) {
            //HACK: this will fail if the geometry of the feature isn't a point
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("setting pointPlacement");
            }

            tx = ((Point) geom).getX();
            ty = ((Point) geom).getY();

            PointPlacement p = (PointPlacement) placement;
            x = ((Number) p.getAnchorPoint().getAnchorPointX().getValue(feature)).doubleValue() * -textBounds.getWidth();
            y = ((Number) p.getAnchorPoint().getAnchorPointY().getValue(feature)).doubleValue() * textBounds.getHeight();

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("anchor point (" + x + "," + y + ")");
            }

            x += ((Number) p.getDisplacement().getDisplacementX().getValue(feature)).doubleValue();
            y += ((Number) p.getDisplacement().getDisplacementY().getValue(feature)).doubleValue();

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("total displacement (" + x + "," + y + ")");
            }

            rotation = ((Number) p.getRotation().getValue(feature)).doubleValue();
            rotation *= (Math.PI / 180.0);
        } else if (placement instanceof LinePlacement) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("setting line placement");
            }

            //HACK: this will fail if the geometry of the feature is not a linestring
            double offset = ((Number) ((LinePlacement) placement).getPerpendicularOffset()
                                       .getValue(feature)).doubleValue();
            LineString line = (LineString) geom;
            Point start = line.getStartPoint();
            Point end = line.getEndPoint();
            double dx = end.getX() - start.getX();
            double dy = end.getY() - start.getY();
            rotation = Math.atan2(dx, dy) - (Math.PI / 2.0);
            tx = (dx / 2.0) + start.getX();
            ty = (dy / 2.0) + start.getY();
            x = -textBounds.getWidth() / 2.0;

            y = 0;

            if (offset >= 0.0) { // to the left of the line
                y = -offset;
            } else {
                y = offset + textBounds.getHeight();
            }

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("offset = " + offset + " x = " + x + " y " + y);
            }
        }

        Halo halo = symbolizer.getHalo();

        if (halo != null) {
            drawHalo(halo, tx, ty, x, y, tl, feature, rotation);
        }

        renderString(graphics, tx, ty, x, y, tl, feature, symbolizer.getFill(),
            rotation);
    }

    private java.awt.Font getFont(Feature feature,
        org.geotools.styling.Font[] fonts) {
        if (fontFamilies == null) {
            java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
            fontFamilies = new HashSet();

            List f = Arrays.asList(ge.getAvailableFontFamilyNames());
            fontFamilies.addAll(f);

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("there are " + fontFamilies.size() +
                    " fonts available");
            }
        }

        java.awt.Font javaFont = null;

        int styleCode = 0;
        int size = 6;
        String requestedFont = "";

        for (int k = 0; k < fonts.length; k++) {
            requestedFont = fonts[k].getFontFamily().getValue(feature).toString();

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("trying to load " + requestedFont);
            }

            if (loadedFonts.containsKey(requestedFont)) {
                javaFont = (Font) loadedFonts.get(requestedFont);

                String reqStyle = (String) fonts[k].getFontStyle().getValue(feature);

                if (fontStyleLookup.containsKey(reqStyle)) {
                    styleCode = ((Integer) fontStyleLookup.get(reqStyle)).intValue();
                } else {
                    styleCode = java.awt.Font.PLAIN;
                }

                String reqWeight = (String) fonts[k].getFontWeight().getValue(feature);

                if (reqWeight.equalsIgnoreCase("Bold")) {
                    styleCode = styleCode | java.awt.Font.BOLD;
                }

                size = ((Number) fonts[k].getFontSize().getValue(feature)).intValue();

                return javaFont.deriveFont(styleCode, size);
            }

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("not already loaded");
            }

            if (fontFamilies.contains(requestedFont)) {
                String reqStyle = (String) fonts[k].getFontStyle().getValue(feature);

                if (fontStyleLookup.containsKey(reqStyle)) {
                    styleCode = ((Integer) fontStyleLookup.get(reqStyle)).intValue();
                } else {
                    styleCode = java.awt.Font.PLAIN;
                }

                String reqWeight = (String) fonts[k].getFontWeight().getValue(feature);

                if (reqWeight.equalsIgnoreCase("Bold")) {
                    styleCode = styleCode | java.awt.Font.BOLD;
                }

                size = ((Number) fonts[k].getFontSize().getValue(feature)).intValue();

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("requesting " + requestedFont + " " +
                        styleCode + " " + size);
                }

                javaFont = new java.awt.Font(requestedFont, styleCode, size);
                loadedFonts.put(requestedFont, javaFont);

                return javaFont;
            }

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("not a system font");
            }

            // may be its a file or url
            InputStream is = null;

            if (requestedFont.startsWith("http") ||
                    requestedFont.startsWith("file:")) {
                try {
                    URL url = new URL(requestedFont);
                    is = url.openStream();
                } catch (MalformedURLException mue) {
                    // this may be ok - but we should mention it
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("Bad url in java2drenderer" +
                            requestedFont + "\n" + mue);
                    }
                } catch (IOException ioe) {
                    // we'll ignore this for the moment
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("IO error in java2drenderer " +
                            requestedFont + "\n" + ioe);
                    }
                }
            } else {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("not a URL");
                }

                File file = new File(requestedFont);

                //if(file.canRead()){
                try {
                    is = new FileInputStream(file);
                } catch (FileNotFoundException fne) {
                    // this may be ok - but we should mention it
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("Bad file name in java2drenderer" +
                            requestedFont + "\n" + fne);
                    }
                }

                /*} else {
                   LOGGER.info("not a readable file");
                   continue; // check for next font
                   }*/
            }

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("about to load");
            }

            if (is == null) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("null input stream");
                }

                continue;
            }

            try {
                javaFont = Font.createFont(Font.TRUETYPE_FONT, is);
            } catch (FontFormatException ffe) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("Font format error in java2drender " +
                        requestedFont + "\n" + ffe);
                }

                continue;
            } catch (IOException ioe) {
                // we'll ignore this for the moment
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("IO error in java2drenderer " + requestedFont +
                        "\n" + ioe);
                }

                continue;
            }

            loadedFonts.put(requestedFont, javaFont);

            return javaFont;
        }

        return null;
    }

    private void renderString(Graphics2D graphic, double x, double y,
        double dx, double dy, TextLayout tl, Feature feature, Fill fill,
        double rotation) {
        AffineTransform temp = graphics.getTransform();
        AffineTransform labelAT = new AffineTransform();

        Point2D mapCentre = new Point2D.Double(x, y);
        Point2D graphicCentre = new Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        labelAT.translate(graphicCentre.getX(), graphicCentre.getY());

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("rotation " + rotation);
        }

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();

        double originalRotation = Math.atan(shearY / scaleY);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("originalRotation " + originalRotation);
        }

        labelAT.rotate(rotation - originalRotation);

        graphics.setTransform(labelAT);

        applyFill(graphics, fill, feature);

        // we move this to the centre of the image.
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("about to draw at " + x + "," + y +
                " with the start of the string at " + (x + dx) + "," +
                (y + dy));
        }

        tl.draw(graphic, (float) dx, (float) dy);

        //graphics.drawString(label,(float)x,(float)y);
        resetFill();
        graphics.setTransform(temp);

        return;
    }

    private void drawHalo(Halo halo, double x, double y, double dx, double dy,
        TextLayout tl, Feature feature, double rotation) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("doing halo");
        }

        /*
         * Creates an outline shape from the TextLayout.
         */
        AffineTransform temp = graphics.getTransform();
        AffineTransform labelAT = new AffineTransform();

        Point2D mapCentre = new Point2D.Double(x, y);
        Point2D graphicCentre = new Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        labelAT.translate(graphicCentre.getX(), graphicCentre.getY());

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("rotation " + rotation);
        }

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();

        double originalRotation = Math.atan(shearY / scaleY);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("originalRotation " + originalRotation);
        }

        labelAT.rotate(rotation - originalRotation);

        graphics.setTransform(labelAT);

        AffineTransform at = new AffineTransform();
        at.translate(dx, dy);

        Shape sha = tl.getOutline(at);
        applyFill(graphics, halo.getFill(), feature);

        float radius = ((Number) halo.getRadius().getValue(feature)).floatValue();
        Shape haloShape = new BasicStroke(2f * radius).createStrokedShape(sha);
        graphics.fill(haloShape);
        resetFill();
        graphics.setTransform(temp);
    }

    private boolean renderExternalGraphic(Geometry geom, Graphic graphic,
        Feature feature, ExternalGraphic symb) {
        BufferedImage img = getImage(symb);

        return renderExternalGraphic(geom, graphic, feature, img);
    }

    private boolean renderExternalGraphic(Geometry geom, Graphic graphic,
        Feature feature, BufferedImage img) {
        if (img != null) {
            int size = ((Number) graphic.getSize().getValue(feature)).intValue();
            double rotation = ((Number) graphic.getRotation().getValue(feature)).doubleValue();
            renderImage((Point) geom, img, size, rotation);

            return true;
        } else {
            return false;
        }
    }

    private BufferedImage getExternalGraphic(Graphic graphic) {
        ExternalGraphic[] extgraphics = graphic.getExternalGraphics();

        if (extgraphics != null) {
            for (int i = 0; i < extgraphics.length; i++) {
                ExternalGraphic eg = extgraphics[i];
                BufferedImage img = getImage(eg);

                if (img != null) {
                    return img;
                }
            }
        }

        return null;
    }

    private BufferedImage getImage(ExternalGraphic eg) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("got a " + eg.getFormat());
        }

        if (supportedGraphicFormats.contains(eg.getFormat().toLowerCase())) {
            if (eg.getFormat().equalsIgnoreCase("image/gif") ||
                    eg.getFormat().equalsIgnoreCase("image/jpg") ||
                    eg.getFormat().equalsIgnoreCase("image/png")) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("a java supported format");
                }

                BufferedImage img = imageLoader.get(eg.getLocation(),
                        isInteractive());

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("Image return = " + img);
                }

                if (img != null) {
                    return img;
                } else {
                    return null;
                }
            }
        }

        return null;
    }

    private boolean renderMark(Geometry geom, Graphic graphic, Feature feature,
        Mark mark) {
        if (mark == null) {
            return false;
        }

        String name = mark.getWellKnownName().getValue(feature).toString();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("rendering mark " + name);
        }

        if (!wellKnownMarks.contains(name)) {
            return false;
        }

        int size = 6; // size in pixels
        double rotation = 0.0; // rotation in degrees
        size = ((Number) graphic.getSize().getValue(feature)).intValue();
        rotation = (((Number) graphic.getRotation().getValue(feature)).doubleValue() * Math.PI) / 180d;
        fillDrawMark(graphics, (Point) geom, mark, size, rotation, feature);

        return true;
    }

    private Mark getMark(Graphic graphic, Feature feature) {
        Mark[] marks = graphic.getMarks();
        Mark mark;

        for (int i = 0; i < marks.length; i++) {
            String name = marks[i].getWellKnownName().getValue(feature)
                                  .toString();

            if (wellKnownMarks.contains(name)) {
                mark = marks[i];

                return mark;
            }
        }

        mark = null;

        return mark;
    }

    private void renderImage(com.vividsolutions.jts.geom.Point point,
        BufferedImage img, int size, double rotation) {
        renderImage(point.getX(), point.getY(), img, size, rotation);
    }

    private void renderImage(double tx, double ty, BufferedImage img, int size,
        double rotation) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("drawing Image @" + tx + "," + ty);
        }

        AffineTransform temp = graphics.getTransform();
        AffineTransform markAT = new AffineTransform();
        Point2D mapCentre = new Point2D.Double(tx, ty);
        Point2D graphicCentre = new Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        markAT.translate(graphicCentre.getX(), graphicCentre.getY());

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();

        double originalRotation = Math.atan(shearY / scaleY);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("originalRotation " + originalRotation);
        }

        markAT.rotate(rotation - originalRotation);

        double unitSize = Math.max(img.getWidth(), img.getHeight());

        double drawSize = (double) size / unitSize;

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("unitsize " + unitSize + " size = " + size +
                " -> scale " + drawSize);
        }

        markAT.scale(drawSize, drawSize);
        graphics.setTransform(markAT);

        // we moved the origin to the centre of the image.
        graphics.drawImage(img, -img.getWidth() / 2, -img.getHeight() / 2, obs);

        graphics.setTransform(temp);

        return;
    }

    private void fillDrawMark(Graphics2D graphic,
        com.vividsolutions.jts.geom.Point point, Mark mark, int size,
        double rotation, Feature feature) {
        fillDrawMark(graphic, point.getX(), point.getY(), mark, size, rotation,
            feature);
    }

    private void fillDrawMark(Graphics2D graphic, double tx, double ty,
        Mark mark, int size, double rotation, Feature feature) {
        AffineTransform temp = graphic.getTransform();
        AffineTransform markAT = new AffineTransform();
        Shape shape = Java2DMark.getWellKnownMark(mark.getWellKnownName()
                                                      .getValue(feature)
                                                      .toString());

        Point2D mapCentre = new Point2D.Double(tx, ty);
        Point2D graphicCentre = new Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        markAT.translate(graphicCentre.getX(), graphicCentre.getY());

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();

        double originalRotation = Math.atan(shearY / scaleY);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("originalRotation " + originalRotation);
        }

        markAT.rotate(rotation - originalRotation);

        double unitSize = 1.0; // getbounds is broken !!!
        double drawSize = (double) size / unitSize;
        markAT.scale(drawSize, -drawSize);

        graphic.setTransform(markAT);

        if (mark.getFill() != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying fill to mark");
            }

            applyFill(graphic, mark.getFill(), null);
            graphic.fill(shape);
        }

        if (mark.getStroke() != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying stroke to mark");
            }

            applyStroke(graphic, mark.getStroke(), null);
            graphic.draw(shape);
        }

        graphic.setTransform(temp);

        if (mark.getFill() != null) {
            resetFill();
        }

        return;
    }

    private boolean renderTextSymbol(Geometry geom, Graphic graphic,
        Feature feature, TextMark mark) {
        int size = 6; // size in pixels
        double rotation = 0.0; // rotation in degrees
        size = ((Number) graphic.getSize().getValue(feature)).intValue();
        rotation = (((Number) graphic.getRotation().getValue(feature)).doubleValue() * Math.PI) / 180d;

        return fillDrawTextMark(graphics, (Point) geom, mark, size, rotation,
            feature);
    }

    private boolean fillDrawTextMark(Graphics2D graphic,
        com.vividsolutions.jts.geom.Point point, TextMark mark, int size,
        double rotation, Feature feature) {
        return fillDrawTextMark(graphic, point.getX(), point.getY(), mark,
            size, rotation, feature);
    }

    private boolean fillDrawTextMark(Graphics2D graphic, double tx, double ty,
        TextMark mark, int size, double rotation, Feature feature) {
        java.awt.Font javaFont = getFont(feature, mark.getFonts());

        if (javaFont != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("found font " + javaFont.getFamily());
            }

            graphic.setFont(javaFont);
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("failed to find font ");
            }

            return false;
        }

        String symbol = mark.getSymbol().getValue(feature).toString();
        TextLayout tl = new TextLayout(symbol, javaFont,
                graphic.getFontRenderContext());
        Rectangle2D textBounds = tl.getBounds();

        // TODO: consider if symbols should carry an offset
        double dx = textBounds.getWidth() / 2.0;
        double dy = textBounds.getHeight() / 2.0;
        renderString(graphic, tx, ty, dx, dy, tl, feature, mark.getFill(),
            rotation);

        return true;
    }

    private void applyFill(Graphics2D graphic, Fill fill, Feature feature) {
        if (fill == null) {
            return;
        }

        graphic.setColor(Color.decode(
                (String) fill.getColor().getValue(feature)));

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Setting fill: " + graphic.getColor().toString());
        }

        Number value = (Number) fill.getOpacity().getValue(feature);
        float opacity = value.floatValue();
        graphic.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacity));

        org.geotools.styling.Graphic gr = fill.getGraphicFill();

        if (gr != null) {
            setTexture(graphic, gr, feature);
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("no graphic fill set");
            }
        }
    }

    private void setTexture(Graphics2D graphic, Graphic gr, Feature feature) {
        BufferedImage image = getExternalGraphic(gr);

        if (image != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("got an image in graphic fill");
            }
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("going for the mark from graphic fill");
            }

            Mark mark = getMark(gr, feature);
            int size = 200;

            image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g1 = image.createGraphics();
            double rotation = 0.0;

            rotation = ((Number) gr.getRotation().getValue(feature)).doubleValue();

            fillDrawMark(g1, markCentrePoint, mark, (int) (size * .9),
                rotation, feature);

            java.awt.MediaTracker track = new java.awt.MediaTracker(obs);
            track.addImage(image, 1);

            try {
                track.waitForID(1);
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
        }

        double width = image.getWidth();
        double height = image.getHeight();
        double unitSize = Math.max(width, height);
        int size = 6;

        size = ((Number) gr.getSize().getValue(feature)).intValue();

        double drawSize = (double) size / unitSize;

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("size = " + size + " unitsize " + unitSize +
                " drawSize " + drawSize);
        }

        AffineTransform at = graphics.getTransform();
        double scaleX = drawSize / at.getScaleX();
        double scaleY = drawSize / -at.getScaleY();

        /* This is needed because the image must be a fixed size in pixels
         * but when the image is used as the fill it is transformed by the
         * current transform.
         * However this causes problems as the image size can become very
         * small e.g. 1 or 2 pixels when the drawScale is large, this makes
         * the image fill look very poor - I have no idea how to fix this.
         */
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("scale " + scaleX + " " + scaleY);
        }

        width *= scaleX;
        height *= scaleY;

        Rectangle2D.Double rect = new Rectangle2D.Double(0.0, 0.0, width, height);
        java.awt.TexturePaint imagePaint = new java.awt.TexturePaint(image, rect);
        graphic.setPaint(imagePaint);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("applied TexturePaint " + imagePaint);
        }
    }

    private void resetFill() {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("reseting the graphics");
        }

        graphics.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 1.0f));
    }

    /**
     * Convenience method for applying a geotools Stroke object as a Graphics2D
     * Stroke object.
     *
     * @param graphic
     * @param stroke the Stroke to apply.
     * @param feature The feature to be stroked
     */
    private void applyStroke(Graphics2D graphic,
        org.geotools.styling.Stroke stroke, Feature feature) {
        if (stroke == null) {
            return;
        }

        double scale = graphics.getTransform().getScaleX();

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("line join = " + stroke.getLineJoin());
        }

        String joinType;

        if (stroke.getLineJoin() == null) {
            joinType = "miter";
        } else {
            joinType = (String) stroke.getLineJoin().getValue(feature);
        }

        if (joinType == null) {
            joinType = "miter";
        }

        int joinCode;

        if (joinLookup.containsKey(joinType)) {
            joinCode = ((Integer) joinLookup.get(joinType)).intValue();
        } else {
            joinCode = java.awt.BasicStroke.JOIN_MITER;
        }

        String capType;

        if (stroke.getLineCap() != null) {
            capType = (String) stroke.getLineCap().getValue(feature);
        } else {
            capType = "square";
        }

        if (capType == null) {
            capType = "square";
        }

        int capCode;

        if (capLookup.containsKey(capType)) {
            capCode = ((Integer) capLookup.get(capType)).intValue();
        } else {
            capCode = java.awt.BasicStroke.CAP_SQUARE;
        }

        float[] dashes = stroke.getDashArray();

        if (dashes != null) {
            for (int i = 0; i < dashes.length; i++) {
                dashes[i] = (float) Math.max(1, dashes[i] / (float) scale);
            }
        }

        Number value = (Number) stroke.getWidth().getValue(feature);
        float width = value.floatValue();
        value = (Number) stroke.getDashOffset().getValue(feature);

        float dashOffset = value.floatValue();
        value = (Number) stroke.getOpacity().getValue(feature);

        float opacity = value.floatValue();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("width, dashoffset, opacity " + width + " " +
                dashOffset + " " + opacity);
        }

        BasicStroke stroke2d;

        //TODO: It should not be necessary to divide each value by scale.
        if ((dashes != null) && (dashes.length > 0)) {
            if (width <= 1.0) {
                width = 0;
            }

            stroke2d = new BasicStroke(width / (float) scale, capCode,
                    joinCode, (float) (Math.max(1, 10 / scale)), dashes,
                    dashOffset / (float) scale);
        } else {
            if (width <= 1.0) {
                width = 0;
            }

            stroke2d = new BasicStroke(width / (float) scale, capCode,
                    joinCode, (float) (Math.max(1, 10 / scale)));
        }

        graphic.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, opacity));

        if (!graphic.getStroke().equals(stroke2d)) {
            graphic.setStroke(stroke2d);
        }

        Color color = Color.decode((String) stroke.getColor().getValue(feature));

        if (!graphic.getColor().equals(color)) {
            graphic.setColor(color);
        }

        org.geotools.styling.Graphic gr = stroke.getGraphicFill();

        if (gr != null) {
            setTexture(graphic, gr, feature);
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("no graphic fill set");
            }
        }

        //System.out.println("stroke color "+graphics.getColor());
    }

    /**
     * a method to draw the path with a graphic stroke.
     *
     * @param graphic the Graphics2D to draw on
     * @param path the general path to be drawn
     * @param gFill the graphic fill to be used to draw the stroke
     * @param feature The feature to be drawn with the graphic stroke
     */
    private void drawWithGraphicStroke(Graphics2D graphic, Shape path,
        org.geotools.styling.Graphic gFill, Feature feature) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("drawing a graphicalStroke");
        }

        int trueImageHeight = 0;
        int trueImageWidth = 0;

        // get the image to draw
        BufferedImage image = getExternalGraphic(gFill);

        if (image != null) {
            trueImageWidth = image.getWidth();
            trueImageHeight = image.getHeight();

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("got an image in graphic fill");
            }
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("going for the mark from graphic fill");
            }

            Mark mark = getMark(gFill, feature);
            int size = 200;
            trueImageWidth = size;
            trueImageHeight = size;

            image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g1 = image.createGraphics();
            double rotation = 0.0;
            rotation = ((Number) gFill.getRotation().getValue(feature)).doubleValue();
            fillDrawMark(g1, markCentrePoint, mark, (int) (size * .9),
                rotation, feature);

            java.awt.MediaTracker track = new java.awt.MediaTracker(obs);
            track.addImage(image, 1);

            try {
                track.waitForID(1);
            } catch (InterruptedException e) {
                LOGGER.warning(e.toString());
            }
        }

        int size = 6;

        size = ((Number) gFill.getSize().getValue(feature)).intValue();

        int imageWidth = size; //image.getWidth();
        int imageHeight = size; //image.getHeight();

        double scaleX = graphic.getTransform().getScaleX();
        double scaleY = -graphic.getTransform().getScaleY();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("scale X " + scaleX + " Y " + scaleY);
        }

        PathIterator pi = path.getPathIterator(null, 10.0);
        double[] coords = new double[6];
        int type;

        double[] first = new double[2];
        double[] previous = new double[2];
        type = pi.currentSegment(coords);
        first[0] = coords[0];
        first[1] = coords[1];
        previous[0] = coords[0];
        previous[1] = coords[1];

        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest("starting at " + first[0] + "," + first[1]);
        }

        pi.next();

        while (!pi.isDone()) {
            type = pi.currentSegment(coords);

            switch (type) {
            case PathIterator.SEG_MOVETO:

                // nothing to do?
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("moving to " + coords[0] + "," + coords[1]);
                }

                break;

            case PathIterator.SEG_CLOSE:

                // draw back to first from previous
                coords[0] = first[0];
                coords[1] = first[1];

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("closing from " + previous[0] + "," +
                        previous[1] + " to " + coords[0] + "," + coords[1]);
                }

            // no break here - fall through to next section
            case PathIterator.SEG_LINETO:

                // draw from previous to coords
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("drawing from " + previous[0] + "," +
                        previous[1] + " to " + coords[0] + "," + coords[1]);
                }

                double dx = coords[0] - previous[0];
                double dy = coords[1] - previous[1];
                double len = Math.sqrt((dx * dx) + (dy * dy)) * scaleX; // - imageWidth;

                double theta = Math.atan2(dx, dy);
                dx = (Math.sin(theta) * imageWidth) / scaleX;
                dy = (Math.cos(theta) * imageHeight) / scaleY;

                //int dx2 = (int)Math.round(dy/2d);
                //int dy2 = (int)Math.round(dx/2d);
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("dx = " + dx + " dy " + dy + " step = " +
                        Math.sqrt((dx * dx) + (dy * dy)));
                }

                double rotation = theta - (Math.PI / 2d);
                double x = previous[0] + (dx / 2.0);
                double y = previous[1] + (dy / 2.0);

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("len =" + len + " imageWidth " + imageWidth);
                }

                double dist = 0;

                for (dist = 0; dist < (len - imageWidth); dist += imageWidth) {
                    /*graphic.drawImage(image2,(int)x-midx,(int)y-midy,null); */
                    renderImage(x, y, image, size, rotation);

                    x += dx;
                    y += dy;
                }

                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("loop end dist " + dist + " len " + len +
                        " " + (len - dist));
                }

                if ((len - dist) > 0.0) {
                    double remainder = len - dist;

                    //clip and render image
                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("about to use clipped image " +
                            remainder);
                    }

                    BufferedImage img = new BufferedImage(trueImageHeight,
                            trueImageWidth, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D ig = img.createGraphics();
                    ig.setClip(0, 0,
                        (int) (((double) trueImageWidth * remainder) / (double) size),
                        trueImageHeight);

                    ig.drawImage(image, 0, 0, trueImageWidth, trueImageHeight,
                        obs);

                    renderImage(x, y, img, size, rotation);
                }

                break;

            default:
                LOGGER.warning(
                    "default branch reached in drawWithGraphicStroke");
            }

            previous[0] = coords[0];
            previous[1] = coords[1];
            pi.next();
        }
    }

    private void renderRaster(Feature feature, RasterSymbolizer symbolizer) {
        try {
            GridCoverage grid = (GridCoverage) feature.getAttribute("grid");
            GridCoverageRenderer gcr = new GridCoverageRenderer(grid);
            gcr.paint(graphics);
            LOGGER.finest("Raster rendered");
        } catch (IllegalFeatureException ife) {
            LOGGER.severe("No grid in feature " + ife.getMessage());
        }
    }

    /**
     * Convenience method.  Converts a Geometry object into a GeneralPath.
     *
     * @param geom The Geometry object to convert
     *
     * @return A GeneralPath that is equivalent to geom
     */
    private Shape createPath(final Geometry geom) {
        return new LiteShape(geom, true, maxDistance);
    }

    /**
     * Extracts the named geometry from feature. If geomName is null then the
     * feature's default geometry is used. If geomName cannot be found in
     * feature then null is returned.
     *
     * @param feature The feature to find the geometry in
     * @param geomName The name of the geometry to find: null if the default
     *        geometry should be used.
     *
     * @return The geometry extracted from feature or null if this proved
     *         impossible.
     */
    private Geometry findGeometry(final Feature feature, final String geomName) {
        Geometry geom = null;

        if (geomName == null) {
            geom = feature.getDefaultGeometry();
        } else {
            try {
                geom = (Geometry) feature.getAttribute(geomName);
            } catch (IllegalFeatureException ife) {
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("Geometry " + geomName + " not found " + ife);
                }

                //hack: not sure if null is the right thing to return at this point
                geom = null;
            }
        }

        return geom;
    }

    /**
     * Getter for property interactive.
     *
     * @return Value of property interactive.
     */
    public boolean isInteractive() {
        return interactive;
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }
}
