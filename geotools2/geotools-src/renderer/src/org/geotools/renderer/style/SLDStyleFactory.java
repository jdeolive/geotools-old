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
/*
 * SLDStyleConverter.java
 *
 * Created on 2 giugno 2003, 18.37
 */
package org.geotools.renderer.style;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.feature.*;
import org.geotools.filter.Expression;
import org.geotools.renderer.style.*;
import org.geotools.styling.*;
import org.geotools.util.*;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.media.jai.util.*;


/**
 * Factory object that converts SLD style into rendered styles
 *
 * @author aaime
 */
public class SLDStyleFactory {
    /** The logger for the rendering module. */
    private static final Logger LOGGER = Logger.getLogger("org.geotools.rendering");

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final java.util.Map joinLookup = new java.util.HashMap();

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final java.util.Map capLookup = new java.util.HashMap();

    /** Holds a lookup bewteen SLD names and java constants. */
    private static final java.util.Map fontStyleLookup = new java.util.HashMap();

    /** Holds the set of well-known marks. */
    static Set wellKnownMarks = new java.util.HashSet();

    /** Holds the of graphic formats supported by the current jdk */
    static Set supportedGraphicFormats = new java.util.HashSet();

    /** Current way to load images */
    static ImageLoader imageLoader = new ImageLoader();

    /** This one is used as the observer object in image tracks */
    private static final Canvas obs = new Canvas();

    /** where the centre of an untransormed mark is */
    private static com.vividsolutions.jts.geom.Point markCentrePoint;

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
         * A list of wellknownshapes that we know about: square, circle, triangle, star, cross, x.
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

        // Compute the centre of an untransformed mark... 
        // TODO: understand what's the use of this :-)
        Coordinate c = new Coordinate(100, 100);
        GeometryFactory fac = new GeometryFactory();
        markCentrePoint = fac.createPoint(c);
    }

    /**
     * Creates a rendered style
     *
     * @param f The feature
     * @param symbolizer The SLD symbolizer
     * @param scaleRange The scale range in which the feature should be painted according to the
     *        symbolizer
     *
     * @return A rendered style equivalent to the symbolizer
     *
     * @throws UnsupportedOperationException if an unknown symbolizer is passed to this method
     */
    public Style2D createStyle(Feature f, Symbolizer symbolizer, Range scaleRange) {
        Style2D style = null;

        if (symbolizer instanceof PolygonSymbolizer) {
            style = createPolygonStyle(f, (PolygonSymbolizer) symbolizer, scaleRange);
        } else if (symbolizer instanceof LineSymbolizer) {
            style = createLineStyle(f, (LineSymbolizer) symbolizer, scaleRange);
        } else {
            throw new UnsupportedOperationException("This kind of symbolizer is not yet supported");
        }

        return style;
    }

    Style2D createPolygonStyle(Feature feature, PolygonSymbolizer symbolizer, Range scaleRange) {
        PolygonStyle2D style = new PolygonStyle2D();

        setScaleRange(style, scaleRange);
        setStroke(style, symbolizer.getStroke(), feature);
        setFill(style, symbolizer.getFill(), feature);

        return style;
    }

    Style2D createLineStyle(Feature feature, LineSymbolizer symbolizer, Range scaleRange) {
        LineStyle2D style = new LineStyle2D();
        setScaleRange(style, scaleRange);
        setStroke(style, symbolizer.getStroke(), feature);

        return style;
    }

    void setScaleRange(Style style, Range scaleRange) {
        double min = ((Number) scaleRange.getMinValue()).doubleValue();
        double max = ((Number) scaleRange.getMaxValue()).doubleValue();
        style.setMinMaxScale(min, max);
    }

    void setStroke(LineStyle2D style, org.geotools.styling.Stroke stroke, Feature feature) {
        if (stroke == null) {
            return;
        }

        // resolve join type into a join code
        String joinType;
        int joinCode;

        joinType = evaluateExpression(stroke.getLineJoin(), feature, "miter");

        if (joinLookup.containsKey(joinType)) {
            joinCode = ((Integer) joinLookup.get(joinType)).intValue();
        } else {
            joinCode = java.awt.BasicStroke.JOIN_MITER;
        }

        // resolve cap type into a cap code
        String capType;
        int capCode;

        capType = evaluateExpression(stroke.getLineCap(), feature, "square");

        if (capLookup.containsKey(capType)) {
            capCode = ((Integer) capLookup.get(capType)).intValue();
        } else {
            capCode = java.awt.BasicStroke.CAP_SQUARE;
        }

        // get the other properties needed for the stroke
        float[] dashes = stroke.getDashArray();
        float width = ((Number) stroke.getWidth().getValue(feature)).floatValue();
        float dashOffset = ((Number) stroke.getDashOffset().getValue(feature)).floatValue();

        // Simple optimization: let java2d use the fast drawing path if the line width
        // is small enough...
        if (width <= 1) {
            width = 0;
        }

        // now set up the stroke
        BasicStroke stroke2d;

        if ((dashes != null) && (dashes.length > 0)) {
            stroke2d = new BasicStroke(width, capCode, joinCode, 1, dashes, dashOffset);
        } else {
            stroke2d = new BasicStroke(width, capCode, joinCode, 1);
        }

        // get the opacity and prepare the composite
        float opacity = ((Number) stroke.getOpacity().getValue(feature)).floatValue();
        Composite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);

        // the foreground color
        Paint contourPaint = Color.decode((String) stroke.getColor().getValue(feature));

        // if a graphic fill is to be used, prepare the paint accordingly....
        org.geotools.styling.Graphic gr = stroke.getGraphicFill();

        if (gr != null) {
            contourPaint = getTexturePaint(gr, feature);
        }

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("width, dashoffset, opacity " + width + " " + dashOffset + " " + opacity);
        }

        // finally fill in the rendered style
        style.setStroke(stroke2d);
        style.setContour(contourPaint);
        style.setContourComposite(composite);
    }

    /**
     * DOCUMENT ME!
     *
     * @param style
     * @param fill
     * @param feature
     */
    protected void setFill(PolygonStyle2D style, Fill fill, Feature feature) {
        if (fill == null) {
            return;
        }

        // get fill color
        Paint fillPaint = Color.decode((String) fill.getColor().getValue(feature));

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Setting fill: " + fillPaint.toString());
        }

        // get the opacity and prepare the composite
        float opacity = ((Number) fill.getOpacity().getValue(feature)).floatValue();
        Composite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);

        // if a graphic fill is to be used, prepare the paint accordingly....
        org.geotools.styling.Graphic gr = fill.getGraphicFill();

        if (gr != null) {
            fillPaint = getTexturePaint(gr, feature);
        }

        // now fill in the style
        style.setFill(fillPaint);
        style.setFillComposite(composite);
    }

    private TexturePaint getTexturePaint(org.geotools.styling.Graphic gr, Feature feature) {
        BufferedImage image = getExternalGraphic(gr);

        if (image != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("got an image in graphic fill");
            }
        } else {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("going for the mark from graphic fill");
            }

            org.geotools.styling.Mark mark = getMark(gr, feature);
            int size = 200;

            image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = image.createGraphics();
            double rotation = 0.0;

            rotation = ((Number) gr.getRotation().getValue(feature)).doubleValue();

            fillDrawMark(g2d, markCentrePoint, mark, (int) (size * .9), rotation, feature);

            java.awt.MediaTracker track = new java.awt.MediaTracker(obs);
            track.addImage(image, 1);

            try {
                track.waitForID(1);
            } catch (InterruptedException e) {
                // TODO: what should we do with this?
                LOGGER.warning("An unterupptedException occurred while drawing a local image..." +
                    e);
            }
        }

        int size = ((Number) gr.getSize().getValue(feature)).intValue();
        double width = image.getWidth();
        double height = image.getHeight();

        double unitSize = Math.max(width, height);
        double drawSize = (double) size / unitSize;

        width *= drawSize;
        height *= drawSize;

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("size = " + size + " unitsize " + unitSize + " drawSize " + drawSize);
        }

        Rectangle2D.Double rect = new Rectangle2D.Double(0.0, 0.0, width, height);
        TexturePaint imagePaint = new TexturePaint(image, rect);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("applied TexturePaint " + imagePaint);
        }

        return imagePaint;
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

                BufferedImage img = imageLoader.get(eg.getLocation(), false);

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

    private Mark getMark(Graphic graphic, Feature feature) {
        Mark[] marks = graphic.getMarks();
        Mark mark;

        for (int i = 0; i < marks.length; i++) {
            String name = marks[i].getWellKnownName().getValue(feature).toString();

            if (wellKnownMarks.contains(name)) {
                mark = marks[i];

                return mark;
            }
        }

        mark = null;

        return mark;
    }

    private void fillDrawMark(Graphics2D graphic, com.vividsolutions.jts.geom.Point point,
        Mark mark, int size, double rotation, Feature feature) {
        fillDrawMark(graphic, point.getX(), point.getY(), mark, size, rotation, feature);
    }

    private void fillDrawMark(Graphics2D graphic, double tx, double ty, Mark mark, int size,
        double rotation, Feature feature) {
        AffineTransform temp = graphic.getTransform();
        AffineTransform markAT = new AffineTransform();
        Shape shape = Java2DMark.getWellKnownMark(mark.getWellKnownName().getValue(feature)
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

            PolygonStyle2D ps2d = new PolygonStyle2D();
            setFill(ps2d, mark.getFill(), null);
            graphic.setPaint(ps2d.getFill());
            graphic.setComposite(ps2d.getFillComposite());
            graphic.fill(shape);
        }

        if (mark.getStroke() != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying stroke to mark");
            }

            LineStyle2D ls2d = new LineStyle2D();
            setStroke(ls2d, mark.getStroke(), null);
            graphic.setPaint(ls2d.getContour());
            graphic.setComposite(ls2d.getContourComposite());
            graphic.setStroke(ls2d.getStroke());
            graphic.draw(shape);
        }

        graphic.setTransform(temp);

        if (mark.getFill() != null) {
            graphic.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        return;
    }

    /**
     * Evaluates an expression over the passed feature, if the expression or the result is null,
     * the default value will be returned
     *
     * @param e DOCUMENT ME!
     * @param feature DOCUMENT ME!
     * @param defaultValue DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    private String evaluateExpression(Expression e, Feature feature, String defaultValue) {
        String result = defaultValue;

        if (e != null) {
            result = (String) e.getValue(feature);

            if (result == null) {
                result = defaultValue;
            }
        }

        return result;
    }
}
