/*
 * RendererUtilities.java
 *
 * Created on 08 January 2003, 13:02
 */
package org.geotools.renderer;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.feature.Feature;
import org.geotools.feature.IllegalFeatureException;

import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.Mark;


/**
 *
 * @author  iant
 */
public class RendererUtilities {
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.rendering");
    private static final Canvas obs = new Canvas();
    static HashSet fontFamilies = null;
    static HashMap loadedFonts = new HashMap();

    /**
     * Holds a lookup bewteen SLD names and java constants.
     */
    private static final java.util.HashMap joinLookup = new java.util.HashMap();

    /**
     * where the centre of an untransormed mark is
     */
    private static com.vividsolutions.jts.geom.Point markCentrePoint;

    /**
     * Holds a lookup bewteen SLD names and java constants.
     */
    private static final java.util.HashMap capLookup = new java.util.HashMap();

    /**
     * Holds a list of well-known marks.
     */
    static HashSet wellKnownMarks = new java.util.HashSet();

    /**
     * Holds a lookup bewteen SLD names and java constants.
     */
    private static final java.util.HashMap fontStyleLookup = 
            new java.util.HashMap();
    private static final HashSet supportedGraphicFormats = 
            new java.util.HashSet();
    private static final ImageLoader imageLoader = new ImageLoader();

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
         * A list of wellknownshapes that we know about:
         * square, circle, triangle, star, cross, x.
         * Note arrow is an implementation specific mark.
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

        Coordinate c = new Coordinate(100, 100);
        GeometryFactory fac = new GeometryFactory();
        markCentrePoint = fac.createPoint(c);
    }

    /** Creates a new instance of RendererUtilities */
    public RendererUtilities() {
    }

    /**
     * Extracts the named geometry from feature.
     * If geomName is null then the feature's default geometry is used.
     * If geomName cannot be found in feature then null is returned.
     *
     * @param feature The feature to find the geometry in
     * @param geomName The name of the geometry to find: null if the default
     *        geometry should be used.
     * @return The geometry extracted from feature or null if this proved
     *         impossible.
     */
    static Geometry findGeometry(final Feature feature, final String geomName) {
        Geometry geom = null;

        if (geomName == null) {
            geom = feature.getDefaultGeometry();
        } else {
            try {
                geom = (Geometry) feature.getAttribute(geomName);
            } catch (IllegalFeatureException ife) {
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Geometry " + geomName + " not found " + ife);
                }


                //hack: not sure if null is the right thing to return at this point
                geom = null;
            }
        }

        return geom;
    }

    /**
     * Convenience method.  Converts a Geometry object into a GeneralPath.
     * @param geom The Geometry object to convert
     * @return A GeneralPath that is equivalent to geom
     */
    protected GeneralPath createGeneralPath(final Geometry geom) {
        //String geomKey = geom.toString();
        //        if (pathCache.containsKey(geomKey)) {
        //            return (GeneralPath) pathCache.get(geomKey);
        //        }
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        addToPath(geom, path);

        // we could cache the path here using geom to key it
        //        pathCache.put(geomKey, path);
        return path;
    }

    /**
     * Used by createGeneralPath during the conversion of a geometry into
     * a general path.
     *
     * If the Geometry is an instance of Polygon then all of its interior holes
     * are processed and the resulting path is closed.
     *
     * @param geom the geomerty to be converted
     * @param path the GeneralPath to add to
     * @return path with geom added to it
     */
    private GeneralPath addToPath(final Geometry geom, final GeneralPath path) {
        if (geom instanceof GeometryCollection) {
            GeometryCollection gc = (GeometryCollection) geom;

            for (int i = 0; i < gc.getNumGeometries(); i++) {
                addToPath(gc.getGeometryN(i), path);
            }

            return path;
        }

        if (geom instanceof com.vividsolutions.jts.geom.Polygon) {
            com.vividsolutions.jts.geom.Polygon poly;
            poly = (com.vividsolutions.jts.geom.Polygon) geom;
            addToPath(path, poly.getExteriorRing().getCoordinates());
            path.closePath();

            for (int i = 1; i < poly.getNumInteriorRing(); i++) {
                addToPath(path, poly.getInteriorRingN(i).getCoordinates());
                path.closePath();
            }
        } else {
            Coordinate[] coords = geom.getCoordinates();
            addToPath(path, coords);
        }

        return path;
    }

    /**
     * Used by addToPath in the conversion of geometries into general paths.
     * A moveTo is executed for the first coordinate then lineTo for all that
     * remain. The path is not closed.
     *
     * @param path The path to add to.  It is modifed by the method.
     * @param coords An array of coordinates to add to the path.
     */
    private void addToPath(final GeneralPath path, final Coordinate[] coords) {
        path.moveTo((float) coords[0].x, (float) coords[0].y);

        double d2;
        double dx;
        double dy;

        for (int i = 1; i < coords.length; i++) {
            path.lineTo((float) coords[i].x, (float) coords[i].y);
        }
    }

    protected void applyFill(Graphics2D graphic, Fill fill, Feature feature) {
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
        graphic.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
                                                        opacity));

        org.geotools.styling.Graphic gr = fill.getGraphicFill();

        if (gr != null) {
            setTexture(graphic, gr, feature);
        }
    }

    void setTexture(Graphics2D graphic, Graphic gr, Feature feature) {
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

            fillDrawMark(g1, markCentrePoint, mark, (int) (size * .9), rotation, 
                         feature);

            java.awt.MediaTracker track = new java.awt.MediaTracker(obs);
            track.addImage(image, 1);

            try {
                track.waitForID(1);
            } catch (InterruptedException e) {
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

        AffineTransform at = graphic.getTransform();
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

        Rectangle2D.Double rect = new Rectangle2D.Double(0.0, 0.0, width, 
                                                         height);
        java.awt.TexturePaint imagePaint = new java.awt.TexturePaint(image, 
                                                                     rect);
        graphic.setPaint(imagePaint);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("applied TexturePaint " + imagePaint);
        }
    }

    void resetFill(Graphics2D graphics) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("reseting the graphics");
        }

        graphics.setComposite(AlphaComposite.getInstance(
                                      AlphaComposite.SRC_OVER, 1.0f));

        //graphics.setPaint(null);
    }

    /**
     * Convenience method for applying a geotools Stroke object
     * as a Graphics2D Stroke object.
     *
     * @param stroke the Stroke to apply.
     */
    void applyStroke(Graphics2D graphic, org.geotools.styling.Stroke stroke, 
                     Feature feature) {
        if (stroke == null) {
            return;
        }

        double scaleX = graphic.getTransform().getScaleX();
        double scaleY = graphic.getTransform().getScaleY();

        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("line join = " + stroke.getLineJoin());
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
                /** @HACK This shouldn't just use the X scale */
                dashes[i] = (float) Math.max(1, dashes[i] / (float) scaleX);
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
        /** @HACK This shouldn't just use the X scale */
        if ((dashes != null) && (dashes.length > 0)) {
            stroke2d = new BasicStroke(width / (float) scaleX, capCode, joinCode, 
                                       (float) (Math.max(1, 10 / scaleX)), 
                                       dashes, dashOffset / (float) scaleX);
        } else {
            stroke2d = new BasicStroke(width / (float) scaleX, capCode, joinCode, 
                                       (float) (Math.max(1, 10 / scaleX)));
        }

        graphic.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 
                                                        opacity));
        graphic.setStroke(stroke2d);
        graphic.setColor(Color.decode(
                                 (String) stroke.getColor().getValue(feature)));

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
     * @param gFill the graphic fill to be used to draw the stroke
     * @param graphic the Graphics2D to draw on
     * @param path the general path to be drawn
     */
    void drawWithGraphicStroke(Graphics2D graphic, GeneralPath path, 
                               org.geotools.styling.Graphic gFill, 
                               Feature feature) {
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
            fillDrawMark(g1, markCentrePoint, mark, (int) (size * .9), rotation, 
                         feature);

            java.awt.MediaTracker track = new java.awt.MediaTracker(obs);
            track.addImage(image, 1);

            try {
                track.waitForID(1);
            } catch (InterruptedException e) {
            }
        }

        int size = 6;

        size = ((Number) gFill.getSize().getValue(feature)).intValue();

        int imageWidth = size; //image.getWidth();
        int imageHeight = size; //image.getHeight();
        int midx = imageWidth / 2;
        int midy = imageHeight / 2;

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
        double[] tprevious = new double[2];
        double[] tcoords = new double[2];
        double[] in = new double[2];
        double[] out = new double[2];
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
                                  previous[1] + " to " + coords[0] + "," + 
                                  coords[1]);
                }

            // no break here - fall through to next section
            case PathIterator.SEG_LINETO:

                // draw from previous to coords
                if (LOGGER.isLoggable(Level.FINEST)) {
                    LOGGER.finest("drawing from " + previous[0] + "," + 
                                  previous[1] + " to " + coords[0] + "," + 
                                  coords[1]);
                }

                double dx = coords[0] - previous[0];
                double dy = coords[1] - previous[1];
                /** @HACK This shouldn't just use the X scale */
                double len = Math.sqrt((dx * dx) + (dy * dy)) * scaleX; // - imageWidth;

                //if(len<=0){
                //len=imageWidth-1;
                //}
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
                    LOGGER.finest("len =" + len + " imageWidth " + 
                                  imageWidth);
                }

                double dist = 0;

                for (dist = 0; dist < (len - imageWidth); dist += imageWidth) {
                    /*graphic.drawImage(image2,(int)x-midx,(int)y-midy,null); */
                    renderImage(graphic, x, y, image, size, rotation);

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
                                                          trueImageWidth, 
                                                          BufferedImage.TYPE_INT_ARGB);
                    Graphics2D ig = img.createGraphics();
                    ig.setClip(0, 0, 
                               (int) ((double) trueImageWidth * remainder / (double) size), 
                               trueImageHeight);

                    ig.drawImage(image, 0, 0, trueImageWidth, trueImageHeight, 
                                 obs);

                    renderImage(graphic, x, y, img, size, rotation);
                }

                break;
            }

            previous[0] = coords[0];
            previous[1] = coords[1];
            pi.next();
        }
    }

    BufferedImage getExternalGraphic(Graphic graphic) {
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

    BufferedImage getImage(ExternalGraphic eg) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("got a " + eg.getFormat());
        }

        if (supportedGraphicFormats.contains(eg.getFormat().toLowerCase())) {
            if (eg.getFormat().equalsIgnoreCase("image/gif") || 
                    eg.getFormat().equalsIgnoreCase("image/jpg") || 
                    eg.getFormat().equalsIgnoreCase("image/png")) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("a java supported format");
                }

                BufferedImage img = imageLoader.get(eg.getLocation(), false); //isInteractive());

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Image return = " + img);
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

    void renderImage(Graphics2D graphics, 
                     com.vividsolutions.jts.geom.Point point, BufferedImage img, 
                     int size, double rotation) {
        renderImage(graphics, point.getX(), point.getY(), img, size, rotation);
    }

    private void renderImage(Graphics2D graphics, double tx, double ty, 
                             BufferedImage img, int size, double rotation) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("drawing Image @" + tx + "," + ty);
        }

        AffineTransform temp = graphics.getTransform();
        AffineTransform markAT = new AffineTransform();
        Point2D mapCentre = new Point2D.Double(tx, ty);
        Point2D graphicCentre = new Point2D.Double();
        temp.transform(mapCentre, graphicCentre);
        markAT.translate(graphicCentre.getX(), graphicCentre.getY());

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();
        double scaleX = temp.getScaleX();
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
        double xToyRatio = Math.abs(scaleX/scaleY);
        markAT.scale(drawSize*xToyRatio, drawSize/xToyRatio);
        graphics.setTransform(markAT);


        // we moved the origin to the centre of the image.
        graphics.drawImage(img, -img.getWidth() / 2, -img.getHeight() / 2, obs);


        //graphics.setColor(Color.red);
        //graphics.drawRect(-img.getWidth()/2,-img.getHeight()/2,img.getWidth(),img.getHeight());
        graphics.setTransform(temp);

        return;
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


        //LOGGER.finer("going for a defaultMark");
        //mark = StyleFactory.createMark();
        mark = null;

        return mark;
    }

    void fillDrawMark(Graphics2D graphic, 
                      com.vividsolutions.jts.geom.Point point, Mark mark, 
                      int size, double rotation, Feature feature) {
        fillDrawMark(graphic, point.getX(), point.getY(), mark, size, rotation, 
                     feature);
    }

    void fillDrawMark(Graphics2D graphic, double tx, double ty, Mark mark, 
                      int size, double rotation, Feature feature) {
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("fill draw mark " + mark + " " + 
                        mark.getWellKnownName());
        }

        Shape shape = Java2DMark.getWellKnownMark(mark.getWellKnownName()
                                                      .getValue(feature)
                                                      .toString());

        renderMark(graphic, tx, ty, mark.getFill(), mark.getStroke(), size, 
                   rotation, shape);

        return;
    }

    /** Renders the shape of a mark centred on tx,ty with the specified fill and stroke on the the provided graphic
     * @param graphic - Graphics2D to draw on
     * @param tx - X coordinate of centre
     * @param ty - Y coordinate of centre
     * @param fill - the fill to apply to the mark (null if none required)
     * @param stroke - the stroke to apply to the mark (null if none required
     * @param size - the size to draw the mark in pixels
     * @param rotation - the rotation of the mark in degrees from north
      * @param shape - the shape of the mark to be drawn
     */
    void renderMark(final Graphics2D graphic, final double tx, final double ty, 
                    final Fill fill, final org.geotools.styling.Stroke stroke, 
                    final int size, final double rotation, final Shape shape) {
        Point2D mapCentre = new Point2D.Double(tx, ty);
        Point2D graphicCentre = new Point2D.Double();
        AffineTransform temp = graphic.getTransform();
        AffineTransform markAT = new AffineTransform();
        temp.transform(mapCentre, graphicCentre);
        markAT.translate(graphicCentre.getX(), graphicCentre.getY());

        double shearY = temp.getShearY();
        double scaleY = temp.getScaleY();
        double scaleX = temp.getScaleX();

        double originalRotation = Math.atan(shearY / scaleY);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("originalRotation " + originalRotation);
        }

        markAT.rotate(rotation - originalRotation);

        double unitSize = 1.0; // getbounds is broken !!!
        double drawSize = (double) size / unitSize;
        double xToyRatio = Math.abs(scaleX/scaleY);
        markAT.scale(drawSize*xToyRatio, -drawSize/xToyRatio);

        graphic.setTransform(markAT);

        if (fill != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying fill to mark");
            }

            applyFill(graphic, fill, null);
            graphic.fill(shape);
        }

        if (stroke != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying stroke to mark");
            }

            applyStroke(graphic, stroke, null);
            graphic.draw(shape);
        }

        graphic.setTransform(temp);

        if (fill != null) {
            resetFill(graphic);
        }
    }

    java.awt.Font getFont(Feature feature, org.geotools.styling.Font[] fonts) {
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
            requestedFont = fonts[k].getFontFamily().getValue(feature)
                                    .toString();

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("trying to load " + requestedFont);
            }

            if (loadedFonts.containsKey(requestedFont)) {
                javaFont = (Font) loadedFonts.get(requestedFont);

                String reqStyle = (String) fonts[k].getFontStyle()
                                                   .getValue(feature);

                if (fontStyleLookup.containsKey(reqStyle)) {
                    styleCode = ((Integer) fontStyleLookup.get(reqStyle)).intValue();
                } else {
                    styleCode = java.awt.Font.PLAIN;
                }

                String reqWeight = (String) fonts[k].getFontWeight()
                                                    .getValue(feature);

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
                String reqStyle = (String) fonts[k].getFontStyle()
                                                   .getValue(feature);

                if (fontStyleLookup.containsKey(reqStyle)) {
                    styleCode = ((Integer) fontStyleLookup.get(reqStyle)).intValue();
                } else {
                    styleCode = java.awt.Font.PLAIN;
                }

                String reqWeight = (String) fonts[k].getFontWeight()
                                                    .getValue(feature);

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
                    LOGGER.info("IO error in java2drenderer " + 
                                requestedFont + "\n" + ioe);
                }

                continue;
            }

            loadedFonts.put(requestedFont, javaFont);

            return javaFont;
        }

        return null;
    }

    void renderString(Graphics2D graphic, double x, double y, double dx, 
                      double dy, TextLayout tl, Feature feature, Fill fill, 
                      double rotation) {
        AffineTransform temp = graphic.getTransform();
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

        graphic.setTransform(labelAT);

        applyFill(graphic, fill, feature);

        // we move this to the centre of the image.
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("about to draw at " + x + "," + y + 
                         " with the start of the string at " + (x + dx) + 
                         "," + (y + dy));
        }

        tl.draw(graphic, (float) dx, (float) dy);


        //graphics.drawString(label,(float)x,(float)y);
        resetFill(graphic);
        graphic.setTransform(temp);

        return;
    }
}