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
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import org.geotools.feature.*;
import org.geotools.filter.Expression;
import org.geotools.styling.*;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
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

    /** Set containing the font families known of this machine */
    private static Set fontFamilies = null;

    /** Fonts already loaded */
    private static Map loadedFonts = new HashMap();

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
        } else if (symbolizer instanceof PointSymbolizer) {
            style = createPointStyle(f, (PointSymbolizer) symbolizer, scaleRange);
        } else if (symbolizer instanceof TextSymbolizer) {
            style = createTextStyle(f, (TextSymbolizer) symbolizer, scaleRange);
        }

        return style;
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
    public Style2D createDynamicStyle(Feature f, Symbolizer symbolizer, Range scaleRange) {
        Style2D style = null;

        if (symbolizer instanceof PolygonSymbolizer) {
            style = createDynamicPolygonStyle(f, (PolygonSymbolizer) symbolizer, scaleRange);
        } else if (symbolizer instanceof LineSymbolizer) {
            style = createDynamicLineStyle(f, (LineSymbolizer) symbolizer, scaleRange);
        } else {
            throw new UnsupportedOperationException("This kind of symbolizer is not yet supported");
        }

        return style;
    }

    Style2D createPolygonStyle(Feature feature, PolygonSymbolizer symbolizer, Range scaleRange) {
        PolygonStyle2D style = new PolygonStyle2D();

        setScaleRange(style, scaleRange);
        style.setStroke(getStroke(symbolizer.getStroke(), feature));
        style.setContour(getStrokePaint(symbolizer.getStroke(), feature));
        style.setContourComposite(getStrokeComposite(symbolizer.getStroke(), feature));
        style.setFill(getPaint(symbolizer.getFill(), feature));
        style.setFillComposite(getComposite(symbolizer.getFill(), feature));

        return style;
    }

    Style2D createDynamicPolygonStyle(Feature feature, PolygonSymbolizer symbolizer,
        Range scaleRange) {
        PolygonStyle2D style = new DynamicPolygonStyle2D(feature, symbolizer);

        setScaleRange(style, scaleRange);

        //setStroke(style, symbolizer.getStroke(), feature);
        //setFill(style, symbolizer.getFill(), feature);
        return style;
    }

    Style2D createLineStyle(Feature feature, LineSymbolizer symbolizer, Range scaleRange) {
        LineStyle2D style = new LineStyle2D();
        setScaleRange(style, scaleRange);
        style.setStroke(getStroke(symbolizer.getStroke(), feature));
        style.setContour(getStrokePaint(symbolizer.getStroke(), feature));
        style.setContourComposite(getStrokeComposite(symbolizer.getStroke(), feature));

        return style;
    }

    Style2D createDynamicLineStyle(Feature feature, LineSymbolizer symbolizer, Range scaleRange) {
        LineStyle2D style = new DynamicLineStyle2D(feature, symbolizer);
        setScaleRange(style, scaleRange);

        //setStroke(style, symbolizer.getStroke(), feature);
        return style;
    }

    Style2D createPointStyle(Feature feature, PointSymbolizer symbolizer, Range scaleRange) {
        Style2D retval = null;

        // extract base properties		
        Graphic sldGraphic = symbolizer.getGraphic();
        float opacity = ((Number) sldGraphic.getOpacity().getValue(feature)).floatValue();
        int size = ((Number) sldGraphic.getSize().getValue(feature)).intValue();
        float rotation = (float) ((((Number) sldGraphic.getRotation().getValue(feature)).floatValue() * Math.PI) / 180);

        // Extract the sequence of external graphics and symbols and process them in order
        // to recognize which one will be used for rendering
        Symbol[] symbols = sldGraphic.getSymbols();
        boolean flag = false;

        for (int i = 0; i < symbols.length; i++) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("trying to render symbol " + i);
            }

            // try loading external graphic and creating a GraphicsStyle2D
            if (symbols[i] instanceof ExternalGraphic) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering External graphic");
                }

                BufferedImage img = getImage((ExternalGraphic) symbols[i]);

                if (img != null) {
                    retval = new GraphicStyle2D(img, size, rotation, opacity);

                    break;
                }
            }

            if (symbols[i] instanceof Mark) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("rendering mark @ PointRenderer " + symbols[i].toString());
                }

                Mark mark = (Mark) symbols[i];
                Shape shape = Java2DMark.getWellKnownMark(mark.getWellKnownName().getValue(feature)
                                                              .toString());

                MarkStyle2D ms2d = new MarkStyle2D();
                ms2d.setShape(shape);
                ms2d.setFill(getPaint(mark.getFill(), feature));
                ms2d.setFillComposite(getComposite(mark.getFill(), feature));
                ms2d.setStroke(getStroke(mark.getStroke(), feature));
                ms2d.setContour(getStrokePaint(mark.getStroke(), feature));
                ms2d.setContourComposite(getStrokeComposite(mark.getStroke(), feature));
                ms2d.setSize(size);
                ms2d.setRotation(rotation);
                retval = ms2d;

                break;
            }

            if (symbols[i] instanceof TextMark) {
                // for the moment don't support TextMarks since they are not part
                // of the SLD specification
                continue;

                /**
                 * if (LOGGER.isLoggable(Level.FINER)) {     LOGGER.finer("rendering text symbol");
                 * } flag = renderTextSymbol(geom, sldgraphic, feature, (TextMark) symbols[i]); if
                 * (flag) {     return; }
                 */
            }
        }

        if (retval != null) {
            setScaleRange(retval, scaleRange);
        }

        return retval;
    }

    Style2D createTextStyle(Feature feature, TextSymbolizer symbolizer, Range scaleRange) {
        TextStyle2D ts2d = new TextStyle2D();
        setScaleRange(ts2d, scaleRange);

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("creating text style");
        }

        String geomName = symbolizer.getGeometryPropertyName();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("geomName = " + geomName);
        }

        // extract geometry
        Geometry geom = findGeometry(feature, geomName);

        if (geom.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("empty geometry");
            }

            return null;
        }

        // extract label
        Object obj = symbolizer.getLabel().getValue(feature);

        if (obj == null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Null label in render text");
            }

            return null;
        }

        String label = obj.toString();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("label is " + label);
        }

        if (label == null) {
            return null;
        }

        ts2d.setLabel(label);

        // get the sequence of fonts to be used and set the first one available
        Font[] fonts = symbolizer.getFonts();
        java.awt.Font javaFont = getFont(feature, fonts);
        ts2d.setFont(javaFont);

        // compute label position, anchor, rotation and displacement
        LabelPlacement placement = symbolizer.getLabelPlacement();
        double anchorX = 0;
        double anchorY = 0;
        double rotation = 0;
        double dispX = 0;
        double dispY = 0;

        if (placement instanceof PointPlacement) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("setting pointPlacement");
            }

            // compute anchor point and displacement
            PointPlacement p = (PointPlacement) placement;
            anchorX = ((Number) p.getAnchorPoint().getAnchorPointX().getValue(feature)).doubleValue();
            anchorY = ((Number) p.getAnchorPoint().getAnchorPointY().getValue(feature)).doubleValue();

            dispX = ((Number) p.getDisplacement().getDisplacementX().getValue(feature)).doubleValue();
            dispY = ((Number) p.getDisplacement().getDisplacementY().getValue(feature)).doubleValue();

            // rotation
            rotation = ((Number) p.getRotation().getValue(feature)).doubleValue();
            rotation *= (Math.PI / 180.0);
        } else if (placement instanceof LinePlacement && geom instanceof LineString) {
            // @TODO: if the geometry is a ring or a polygon try to find out
            // some "axis" to follow in the label placement
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("setting line placement");
            }

            LineString ls = (LineString) geom;
            Coordinate s = ls.getStartPoint().getCoordinate();
            Coordinate e = ls.getEndPoint().getCoordinate();
            double dx = e.x - s.x;
            double dy = e.y - s.y;

            double offset = ((Number) ((LinePlacement) placement).getPerpendicularOffset().getValue(feature))
                .doubleValue();
            rotation = Math.atan2(dx, dy) - (Math.PI / 2.0);
            anchorX = -0.5;
            anchorY = -0.5;
            dispX = 0;
            dispY = offset;
            ts2d.setAbsoluteLineDisplacement(true);
        }

        ts2d.setAnchorX(anchorX);
        ts2d.setAnchorY(anchorY);
        ts2d.setRotation((float) rotation);
        ts2d.setDisplacementX(dispX);
        ts2d.setDisplacementY(dispY);

        // setup fill and composite
        ts2d.setFill(getPaint(symbolizer.getFill(), feature));
        ts2d.setComposite(getComposite(symbolizer.getFill(), feature));

        // compute halo parameters
        Halo halo = symbolizer.getHalo();

        if (halo != null) {
            ts2d.setHaloFill(getPaint(halo.getFill(), feature));
            ts2d.setHaloComposite(getComposite(halo.getFill(), feature));
            ts2d.setHaloRadius(((Number) halo.getRadius().getValue(feature)).floatValue());
        }

        return ts2d;
    }

    /**
     * Extracts the named geometry from feature. If geomName is null then the feature's default
     * geometry is used. If geomName cannot be found in feature then null is returned.
     *
     * @param feature The feature to find the geometry in
     * @param geomName The name of the geometry to find: null if the default geometry should be
     *        used.
     *
     * @return The geometry extracted from feature or null if this proved impossible.
     */
    private Geometry findGeometry(final Feature feature, final String geomName) {
        Geometry geom = null;

        if (geomName == null) {
            geom = feature.getDefaultGeometry();
        } else {
            geom = (Geometry) feature.getAttribute(geomName);
        }

        return geom;
    }

    /**
     * Returns the first font associated to the feature that can be found on the current machine
     *
     * @param feature The feature whose font is to be found
     * @param fonts An array of fonts dependent of the feature, the first that is found on the
     *        current machine is returned
     *
     * @return The first of the specified fonts found on this machine or null if none found
     */
    private java.awt.Font getFont(Feature feature, Font[] fonts) {
        if (fontFamilies == null) {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            fontFamilies = new HashSet();

            List f = Arrays.asList(ge.getAvailableFontFamilyNames());
            fontFamilies.addAll(f);

            if (LOGGER.isLoggable(Level.FINEST)) {
                LOGGER.finest("there are " + fontFamilies.size() + " fonts available");
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
                javaFont = (java.awt.Font) loadedFonts.get(requestedFont);

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
                    LOGGER.finest("requesting " + requestedFont + " " + styleCode + " " + size);
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

            if (requestedFont.startsWith("http") || requestedFont.startsWith("file:")) {
                try {
                    URL url = new URL(requestedFont);
                    is = url.openStream();
                } catch (MalformedURLException mue) {
                    // this may be ok - but we should mention it
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("Bad url in SLDStyleFactory " + requestedFont + "\n" + mue);
                    }
                } catch (IOException ioe) {
                    // we'll ignore this for the moment
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.info("IO error in SLDStyleFactory " + requestedFont + "\n" + ioe);
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
                        LOGGER.info("Bad file name in SLDStyleFactory" + requestedFont + "\n" + fne);
                    }
                }
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
                javaFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
            } catch (FontFormatException ffe) {
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("Font format error in SLDStyleFactory " + requestedFont + "\n"
                        + ffe);
                }

                continue;
            } catch (IOException ioe) {
                // we'll ignore this for the moment
                if (LOGGER.isLoggable(Level.INFO)) {
                    LOGGER.info("IO error in SLDStyleFactory " + requestedFont + "\n" + ioe);
                }

                continue;
            }

            loadedFonts.put(requestedFont, javaFont);

            return javaFont;
        }

        return null;
    }

    void setScaleRange(Style style, Range scaleRange) {
        double min = ((Number) scaleRange.getMinValue()).doubleValue();
        double max = ((Number) scaleRange.getMaxValue()).doubleValue();
        style.setMinMaxScale(min, max);
    }

    private Stroke getStroke(org.geotools.styling.Stroke stroke, Feature feature) {
        if (stroke == null) {
            return null;
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
        if (width < 1.5) {
            width = 0;
        }

        // now set up the stroke
        BasicStroke stroke2d;

        if ((dashes != null) && (dashes.length > 0)) {
            stroke2d = new BasicStroke(width, capCode, joinCode, 1, dashes, dashOffset);
        } else {
            stroke2d = new BasicStroke(width, capCode, joinCode, 1);
        }

        return stroke2d;
    }

    private Paint getStrokePaint(org.geotools.styling.Stroke stroke, Feature feature) {
        if (stroke == null) {
            return null;
        }

        // the foreground color
        Paint contourPaint = Color.decode((String) stroke.getColor().getValue(feature));

        // if a graphic fill is to be used, prepare the paint accordingly....
        org.geotools.styling.Graphic gr = stroke.getGraphicFill();

        if (gr != null) {
            contourPaint = getTexturePaint(gr, feature);
        }

        return contourPaint;
    }

    private Composite getStrokeComposite(org.geotools.styling.Stroke stroke, Feature feature) {
        if (stroke == null) {
            return null;
        }

        // get the opacity and prepare the composite
        float opacity = ((Number) stroke.getOpacity().getValue(feature)).floatValue();
        Composite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);

        return composite;
    }

    /**
     * DOCUMENT ME!
     *
     * @param style
     * @param fill
     * @param feature
     */
    protected Paint getPaint(Fill fill, Feature feature) {
        if (fill == null) {
            return null;
        }

        // get fill color
        Paint fillPaint = Color.decode((String) fill.getColor().getValue(feature));

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("Setting fill: " + fillPaint.toString());
        }

        // if a graphic fill is to be used, prepare the paint accordingly....
        org.geotools.styling.Graphic gr = fill.getGraphicFill();

        if (gr != null) {
            fillPaint = getTexturePaint(gr, feature);
        }

        return fillPaint;
    }

    /**
     * Computes the Composite equivalent to the opacity in the SLD Fill
     *
     * @param fill
     * @param feature
     */
    protected Composite getComposite(Fill fill, Feature feature) {
        if (fill == null) {
            return null;
        }

        // get the opacity and prepare the composite
        float opacity = ((Number) fill.getOpacity().getValue(feature)).floatValue();
        Composite composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);

        return composite;
    }

    /**
     * DOCUMENT ME!
     *
     * @param gr DOCUMENT ME!
     * @param feature DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public TexturePaint getTexturePaint(org.geotools.styling.Graphic gr, Feature feature) {
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
            rotation *= (Math.PI / 180.0);

            fillDrawMark(g2d, markCentrePoint, mark, (int) (size * .9), rotation, feature);

            java.awt.MediaTracker track = new java.awt.MediaTracker(obs);
            track.addImage(image, 1);

            try {
                track.waitForID(1);
            } catch (InterruptedException e) {
                // TODO: what should we do with this?
                LOGGER.warning("An unterupptedException occurred while drawing a local image..."
                    + e);
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
            if (eg.getFormat().equalsIgnoreCase("image/gif")
                    || eg.getFormat().equalsIgnoreCase("image/jpg")
                    || eg.getFormat().equalsIgnoreCase("image/png")) {
                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.finer("a java supported format");
                }

                try {
                    BufferedImage img = imageLoader.get(eg.getLocation(), false);

                    if (LOGGER.isLoggable(Level.FINEST)) {
                        LOGGER.finest("Image return = " + img);
                    }

                    return img;
                } catch (java.net.MalformedURLException e) {
                    LOGGER.warning("ExternalGraphic has a malformed url: " + e);
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

            graphic.setPaint(getPaint(mark.getFill(), null));
            graphic.setComposite(getComposite(mark.getFill(), null));
            graphic.fill(shape);
        }

        if (mark.getStroke() != null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("applying stroke to mark");
            }

            graphic.setPaint(getStrokePaint(mark.getStroke(), null));
            graphic.setComposite(getStrokeComposite(mark.getStroke(), null));
            graphic.setStroke(getStroke(mark.getStroke(), null));
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

    /**
     * DOCUMENT ME!
     *
     * @param joinType DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static int lookUpJoin(String joinType) {
        if (SLDStyleFactory.joinLookup.containsKey(joinType)) {
            return ((Integer) joinLookup.get(joinType)).intValue();
        } else {
            return java.awt.BasicStroke.JOIN_MITER;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param capType DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public static int lookUpCap(String capType) {
        if (SLDStyleFactory.capLookup.containsKey(capType)) {
            return ((Integer) capLookup.get(capType)).intValue();
        } else {
            return java.awt.BasicStroke.CAP_SQUARE;
        }
    }
}
