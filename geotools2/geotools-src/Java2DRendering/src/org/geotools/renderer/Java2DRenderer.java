/**
 * Geotools - OpenSource mapping toolkit (C) 2002, Centre for Computational
 * Geography This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; version 2.1 of the License. This
 * library is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details. You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.geotools.renderer;


//Java Topology Suite
import com.vividsolutions.jts.geom.*;
import org.apache.commons.collections.LRUMap;

//geotools imports
import org.geotools.data.*;
import org.geotools.datasource.extents.EnvelopeExtent;
import org.geotools.feature.*;
import org.geotools.filter.*;
import org.geotools.gc.GridCoverage;
import org.geotools.map.Context;
import org.geotools.pt.CoordinatePoint;
import org.geotools.styling.*;
import org.opengis.ct.CT_MathTransform;
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
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.*;

// file handling
import java.io.*;
import java.net.*;

//util imports
import java.util.*;
import java.util.Collections;
import java.util.logging.Level;

//Logging system
import java.util.logging.Logger;

// image handling
import javax.imageio.ImageIO;


/**
 * Render Features onto a map, features, style information and area is derived
 * from the Context associated with this map.<br>
 * This current version supports 2 implementations during the transformation
 * from one design pattern to another.  The deprecated methods shall
 * eventually be removed.
 *
 * @author James Macgill
 * @author Cameron Shorter
 * @version $Id: Java2DRenderer.java,v 1.74 2003/05/04 09:49:44 camerons Exp $
 *
 * @task TODO Remove deprecated methods.
 */
public class Java2DRenderer implements org.geotools.renderer.Renderer,
    Renderer2D {
    /** The logger for the rendering module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.renderer.Java2DRenderer");
    private static double tolerance = 1e-6;

    /**
     * Context which contains LayerList, BoundingBox which needs to be
     * rendered.
     */
    private Context context;
    private java.util.LinkedHashMap renderedObjects = new LinkedHashMap();

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
    private Feature[] cachedFeatures;
    private FeatureTypeStyle[] cachedFeatureStylers;

    /**
     * Creates a new instance of Java2DRenderer.
     *
     * @deprecated Renderer is to be created with a Context.
     */
    public Java2DRenderer() {
        LOGGER.fine("creating new j2drenderer");
    }

    /**
     * Creates a new instance of Java2DRenderer.
     *
     * @param context Contains pointers to layers, bounding box, and style
     *        required for rendering.
     */
    public Java2DRenderer(Context context) {
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
     *        must be an instance of Graphics2D.
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
     *
     * @deprecated Use render(Graphics, Rectangle) instead.
     */
    public void render(Feature[] features, Envelope map, Style s) {
        Date start = new Date();

        if (graphics == null) {
            LOGGER.info("renderer passed null graphics");

            return;
        }

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("renderering " + features.length + " features");
        }

        mapExtent = map;

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

        scaleDenominator = 1 / graphics.getTransform().getScaleX();

        //extract the feature type stylers from the style object and process them
        FeatureTypeStyle[] featureStylers = s.getFeatureTypeStyles();
        processStylers(features, featureStylers);

        Date end = new Date();

        if (LOGGER.getLevel() == Level.INFO) { // change to fine when finished
            LOGGER.info("Time to render " + features.length + " is " +
                (end.getTime() - start.getTime()) + " milliSecs");
        }
    }

    /**
     * Render features based on the LayerList, BoundBox and Style specified in
     * this.context.
     *
     * @param graphics The graphics object to draw to.
     * @param paintArea The size of the output area in output units (eg:
     *        pixels).
     * @deprecated Use render(Graphics2D, CT_MathTransform) instead.
     * @task TODO Move this functionality into
     * render(Graphics2D, CT_MathTransform).
     */
    public void render(Graphics2D graphics, Rectangle paintArea) {
        Date start = new Date();

        if ((graphics == null) || (paintArea == null)) {
            LOGGER.info("renderer passed null arguements");
            return;
        }

        for (int l = 0; l < this.context.getLayerList().getLayers().length;
                l++) {
            if (!this.context.getLayerList().getLayers()[l].getVisability()) {
                // Only render layer when layer is visable
                break;
            }

            FeatureCollection fc = new FeatureCollectionDefault(context.getLayerList()
                                                                       .getLayers()[l].getDataSource());

            try {
                Feature[] features = fc.getFeatures(new EnvelopeExtent(
                            this.context.getBbox().getAreaOfInterest()));
                mapExtent = this.context.getBbox().getAreaOfInterest();

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("renderering " + features.length + " features");
                }

                //set up the affine transform and calculate scale values
                AffineTransform at = setUpTransform(mapExtent, paintArea);

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

                scaleDenominator = 1 / graphics.getTransform().getScaleX();

                //extract the feature type stylers from the style object and
                //process them
                FeatureTypeStyle[] featureStylers = context.getLayerList()
                                                                                         .getLayers()[l].getStyle()
                                                                                         .getFeatureTypeStyles();
                processStylers(features, featureStylers, graphics);

                Date end = new Date();

                if (LOGGER.getLevel() == Level.INFO) { //change to fine when finished
                    LOGGER.info("Time to render " + features.length + " is " +
                        (end.getTime() - start.getTime()) + " milliSecs");
                }
            } catch (Exception exception) {
                LOGGER.warning("Exception " + exception + " rendering layer " +
                    context.getLayerList().getLayers()[l]);
            }
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

        double sc = scaleX;
        double ss = 0.0d;

        // TODO: if user space is geographic (i.e. in degrees) we need to
        // transform it
        // to Km/m here to calc the size of the pixel and hence the
        // scaleDenominator
        AffineTransform at = new AffineTransform(scaleX, 0.0d, 0.0d, -scaleY,
                tx, ty);

        return at;
    }

    /**
     * DOCUMENT ME!
     *
     * @param x DOCUMENT ME!
     * @param y DOCUMENT ME!
     * @param map DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @deprecated Use getDotToCoordinateSystem() to get an AffineTransform,
     *             then transform coordinates in the calling system instead.
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
     * DOCUMENT ME!
     *
     * @param features DOCUMENT ME!
     * @param featureStylers DOCUMENT ME!
     *
     * @deprecated Use processStylers(Feature[],FeatureTypeStyle[],Graphics2D)
     *             instead.
     */
    private void processStylers(final Feature[] features,
        final FeatureTypeStyle[] featureStylers) {
        processStylers(features, featureStylers, this.graphics);
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
     * @param features An array of features to be rendered.
     * @param featureStylers An array of feature stylers to be applied.
     * @param graphics Object to render into.
     */
    private void processStylers(final Feature[] features,
        final FeatureTypeStyle[] featureStylers, Graphics2D graphics) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("processing " + featureStylers.length + " stylers");
        }

        if (!(features.equals(cachedFeatures) &&
                featureStylers.equals(cachedFeatureStylers))) {
            // the features or style has changed so return process
            cachedFeatures = features;
            cachedFeatureStylers = featureStylers;

            renderedObjects = new LinkedHashMap();

            for (int i = 0; i < featureStylers.length; i++) {
                FeatureTypeStyle fts = featureStylers[i];

                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("about to draw " + features.length +
                        " feature");
                }

                for (int j = 0; j < features.length; j++) {
                    Feature feature = features[j];

                    if (!mapExtent.overlaps(feature.getDefaultGeometry()
                                                       .getEnvelopeInternal())) {
                        // if its off screen don't bother drawing it
                        if (LOGGER.isLoggable(Level.FINER)) {
                            LOGGER.finer("skipping " + feature.toString());
                        }

                        continue;
                    }

                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("feature is " +
                            feature.getSchema().getTypeName() +
                            " type styler is " + fts.getFeatureTypeName());
                    }

                    if (feature.getSchema().getTypeName().equalsIgnoreCase(fts.getFeatureTypeName())) {
                        //this styler is for this type of feature
                        //now find which rule applies
                        Rule[] rules = fts.getRules();
                        boolean featureProcessed = false;

                        for (int k = 0; k < rules.length; k++) {
                            //does this rule apply?
                            if (isWithInScale(rules[k]) &&
                                    !rules[k].hasElseFilter()) {
                                Filter filter = rules[k].getFilter();

                                if (LOGGER.isLoggable(Level.FINEST)) {
                                    LOGGER.finest("Filter " + filter);
                                }

                                if ((filter == null) ||
                                        (filter.contains(feature) == true)) {
                                    if (LOGGER.isLoggable(Level.FINE)) {
                                        LOGGER.fine(
                                            "rule passed, moving on to symobolizers");
                                    }

                                    //yes it does
                                    //this gives us a list of symbolizers
                                    Symbolizer[] symbolizers = rules[k].getSymbolizers();
                                    processSymbolizers(feature, symbolizers);
                                    featureProcessed = true;
                                }
                            }
                        }

                        if (featureProcessed == true) {
                            continue;
                        }

                        //if else present apply elsefilter
                        for (int k = 0; k < rules.length; k++) {
                            //if none of the above rules applied do any of them have elsefilters that do
                            if (isWithInScale(rules[k])) {
                                if (rules[k].hasElseFilter()) {
                                    if (LOGGER.isLoggable(Level.FINE)) {
                                        LOGGER.fine(
                                            "rule passed as else filter, moving on to symobolizers");
                                    }

                                    Symbolizer[] symbolizers = rules[k].getSymbolizers();
                                    processSymbolizers(feature, symbolizers);
                                }
                            }
                        }
                    }
                }
            }
        }

        Iterator it = renderedObjects.values().iterator();

        while (it.hasNext()) {
            ((RenderedObject) it.next()).render(graphics);
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
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("applying symbolizer " + symbolizers[m]);
            }

            Integer key = new Integer((symbolizers[m].hashCode() * 19) +
                    feature.hashCode());

            if (symbolizers[m] instanceof PolygonSymbolizer) {
                if (!renderedObjects.containsKey(key)) {
                    RenderedPolygon rPolygon = new RenderedPolygon(feature,
                            (PolygonSymbolizer) symbolizers[m]);

                    //                    rPolygon.render(graphics);
                    renderedObjects.put(key, rPolygon);
                }
            } else if (symbolizers[m] instanceof LineSymbolizer) {
                if (!renderedObjects.containsKey(key)) {
                    RenderedLine rLine = new RenderedLine(feature,
                            (LineSymbolizer) symbolizers[m]);

                    //                    rLine.render(graphics);
                    renderedObjects.put(key, rLine);
                }
            } else if (symbolizers[m] instanceof PointSymbolizer) {
                if (!renderedObjects.containsKey(key)) {
                    RenderedPoint rPoint = new RenderedPoint(feature,
                            (PointSymbolizer) symbolizers[m]);

                    //                    rPoint.render(graphics);
                    renderedObjects.put(key, rPoint);
                }
            } else if (symbolizers[m] instanceof TextSymbolizer) {
                if (!renderedObjects.containsKey(key)) {
                    RenderedText rText = new RenderedText(feature,
                            (TextSymbolizer) symbolizers[m]);

                    //                   rText.render(graphics);
                    renderedObjects.put(key, rText);
                }
            } else if (symbolizers[m] instanceof RasterSymbolizer) {
                if (!renderedObjects.containsKey(key)) {
                    RenderedRaster rRaster = new RenderedRaster(feature,
                            (RasterSymbolizer) symbolizers[m]);

                    //                    rRaster.render(graphics);
                    renderedObjects.put(key, rRaster);
                }
            }
        }
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
    
    /** Render features based on the LayerList, BoundBox and Style specified in
     * this.context.
     *
     * @param graphics The graphics object to draw to.
     * @param transform A transform which converts World coordinates to
     * Screen coordinates.
     * @task TODO Populate this method.
     */
    public void render(Graphics2D graphics, CT_MathTransform transform) {
    }
    
}
