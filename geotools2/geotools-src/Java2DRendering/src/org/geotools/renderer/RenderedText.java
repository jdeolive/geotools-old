/*
 * RenderedText.java
 *
 * Created on 08 January 2003, 16:57
 */
package org.geotools.renderer;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.feature.Feature;

import org.geotools.styling.Fill;
import org.geotools.styling.Halo;
import org.geotools.styling.LabelPlacement;
import org.geotools.styling.LinePlacement;
import org.geotools.styling.PointPlacement;
import org.geotools.styling.TextSymbolizer;


/**
 *
 * @author  iant
 */
public class RenderedText implements RenderedObject {
    /**
     * The logger for the rendering module.
     */
    private static final Logger LOGGER = Logger.getLogger(
                                                 "org.geotools.rendering");
    private static final RendererUtilities utils = new RendererUtilities();
    boolean renderable = false;
    private Geometry geom;
    private Feature feature;
    private String label;
    private Font javaFont;
    private LabelPlacement placement;
    private Halo halo;
    private Fill fill;

    /** Creates a new instance of RenderedText */
    public RenderedText(Feature feature, TextSymbolizer symbolizer) {
        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("rendering text");
        }

        String geomName = symbolizer.getGeometryPropertyName();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("geomName = " + geomName);
        }

        geom = RendererUtilities.findGeometry(feature, geomName);

        if (geom.isEmpty()) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("empty geometry");
            }

            renderable = false;

            return;
        }

        Object obj = symbolizer.getLabel().getValue(feature);

        if (obj == null) {
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("Null label in render text");
            }

            renderable = false;

            return;
        }

        label = obj.toString();

        if (LOGGER.isLoggable(Level.FINER)) {
            LOGGER.finer("label is " + label);
        }

        if (label == null) {
            renderable = false;

            return;
        }

        org.geotools.styling.Font[] fonts = symbolizer.getFonts();
        javaFont = utils.getFont(feature, fonts);

        placement = symbolizer.getLabelPlacement();

        halo = symbolizer.getHalo();
        fill = symbolizer.getFill();
    }

    private void drawHalo(Halo halo, Graphics2D graphics, double x, double y, 
                          double dx, double dy, TextLayout tl, Feature feature, 
                          double rotation) {
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
        utils.applyFill(graphics, halo.getFill(), feature);

        float radius = ((Number) halo.getRadius().getValue(feature)).floatValue();
        Shape haloShape = new BasicStroke(2f * radius).createStrokedShape(sha);
        graphics.fill(haloShape);
        utils.resetFill(graphics);
        graphics.setTransform(temp);
    }

    public boolean isRenderable() {
        return renderable;
    }

    public void render(Graphics2D graphics) {
        if (javaFont != null) {
            graphics.setFont(javaFont);
        }

        TextLayout tl = new TextLayout(label, javaFont, 
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
            x = ((Number) p.getAnchorPoint().getAnchorPointX()
                           .getValue(feature)).doubleValue() * -textBounds.getWidth();
            y = ((Number) p.getAnchorPoint().getAnchorPointY()
                           .getValue(feature)).doubleValue() * textBounds.getHeight();

            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.finer("anchor point (" + x + "," + y + ")");
            }

            x += ((Number) p.getDisplacement().getDisplacementX()
                            .getValue(feature)).doubleValue();
            y += ((Number) p.getDisplacement().getDisplacementY()
                            .getValue(feature)).doubleValue();

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

        if (halo != null) {
            drawHalo(halo, graphics, tx, ty, x, y, tl, feature, rotation);
        }

        utils.renderString(graphics, tx, ty, x, y, tl, feature, fill, rotation);
    }
}