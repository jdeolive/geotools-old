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
 * SLDRenderedGeometries.java
 *
 * Created on 11 giugno 2003, 7.35
 */
package org.geotools.renderer.j2d;

import java.awt.AlphaComposite;

// Geotools dependencies
import org.geotools.ct.*;
import org.geotools.renderer.geom.GeometryCollection;
import org.geotools.renderer.style.LineStyle2D;
import org.geotools.renderer.style.MarkStyle2D;
import org.geotools.renderer.style.PolygonStyle2D;
import org.geotools.renderer.style.Style2D;
import org.geotools.renderer.style.TextStyle2D;
import org.geotools.resources.XDimension2D;
import org.geotools.resources.XMath;
import java.awt.Color;
import java.awt.Graphics2D;

// J2SE dependencies
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;


/**
 * A RenderedGeometries layer that can process styles (will use device space
 * coordinates for  style rendering)
 *
 * @author aaime
 */
public class SLDRenderedGeometries extends RenderedGeometries {
    private static AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

    /** The current map scale. */
    protected double currentScale;

    /**
     * Construct a layer for the specified geometry.
     *
     * @param geometry The geometry, or <code>null</code> if none.
     *
     * @see #setGeometry
     */
    public SLDRenderedGeometries(final GeometryCollection geometry) {
        super(geometry);

        // this layer works directly with device space coordinates
        setRenderUsingMapCS(false);

        /*
         * Set a default "pixel" size in order to avoid automatic (and costly) resolution
         * computation. We assume that the geometry shape is close to an ellipse, i.e. we
         * estimate the perimeter using PI*sqrt(2*(a² + b²)) where a and b are semi-axis.
         */
        Rectangle2D bounds = geometry.getBounds2D();
        double size = (THICKNESS * 2.2214414690791831235079404950303) * XMath
            .hypot(bounds.getWidth(), bounds.getHeight());
        size /= geometry.getPointCount();
        setPreferredPixelSize(new XDimension2D.Double(size, size));
    }

    /**
     * Draw the geometry.
     *
     * @param context The set of transformations needed for transforming
     *        geographic coordinates
     *        (<var>longitude</var>,<var>latitude</var>) into pixels
     *        coordinates.
     *
     * @throws TransformException If a transformation failed.
     */
    protected void paint(final RenderingContext context)
        throws TransformException {
        // Overridden to get the current scale even if rendering in device space coordinates.
        // Use only the scaleX since this is the behaviour of Java2DRendering 
        // (reference implementation)
        currentScale = context.getAffineTransform(context.mapCS, context.textCS)
                              .getScaleX();

        //        System.out.println("Current scale: " + currentScale);
        super.paint(context);
    }

    /**
     * Invoked automatically when a polyline is about to be draw. This
     * implementation paints the polyline according to the rendered style
     *
     * @param graphics The graphics in which to draw.
     * @param polyline The polyline to draw.
     * @param style The style to apply, or <code>null</code> if none.
     */
    protected void paint(final Graphics2D graphics, final Shape shape,
        final Style2D style) {
        if (style == null) {
            System.out.println("Null style!"); // TODO: what's going on? Should not be reached...

            return;
        }

        // Is the current scale within the style scale range? 
        if (!style.isScaleInRange(currentScale)) {
            System.out.println("Out of scale");

            return;
        }

        if (style instanceof MarkStyle2D) {
            // get the point onto the shape has to be painted
            float[] coords = new float[2];
            PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
            iter.currentSegment(coords);

            MarkStyle2D ms2d = (MarkStyle2D) style;
            Shape transformedShape = ms2d.getTransformedShape(coords[0],
                    coords[1]);

            if (transformedShape != null) {
                if (ms2d.getFill() != null) {
                    graphics.setPaint(ms2d.getFill());
                    graphics.setComposite(ms2d.getFillComposite());
                    graphics.fill(transformedShape);
                }

                if (ms2d.getContour() != null) {
                    graphics.setPaint(ms2d.getContour());
                    graphics.setStroke(ms2d.getStroke());
                    graphics.setComposite(ms2d.getContourComposite());
                    graphics.draw(transformedShape);
                }
            }
        } else if (style instanceof TextStyle2D) {
            // get the point onto the shape has to be painted
            float[] coords = new float[2];
            PathIterator iter = shape.getPathIterator(IDENTITY_TRANSFORM);
            iter.currentSegment(coords);

            AffineTransform old = graphics.getTransform();
            graphics.setTransform(IDENTITY_TRANSFORM);
            AffineTransform temp = new AffineTransform(old);
            TextStyle2D ts2d = (TextStyle2D) style;
            GlyphVector textGv = ts2d.getTextGlyphVector(graphics);
            Rectangle2D bounds = textGv.getLogicalBounds();
            
            temp.translate(coords[0] + (ts2d.getAnchorX() * (-bounds.getWidth())),
                coords[1] + (ts2d.getAnchorY() * bounds.getHeight()));

            if (ts2d.isAbsoluteLineDisplacement()) {
                temp.rotate(ts2d.getRotation());
                temp.translate(0.0, ts2d.getDisplacementY());
            } else {
                temp.translate(ts2d.getDisplacementX(), ts2d.getDisplacementY());
                temp.rotate(ts2d.getRotation());
            }

            graphics.setTransform(temp);
            if(ts2d.getHaloFill() != null) {
                float radious = ts2d.getHaloRadius();
                // graphics.translate(radious, -radious);
                graphics.setPaint(ts2d.getHaloFill());
                graphics.setComposite(ts2d.getHaloComposite());
                graphics.fill(ts2d.getHaloShape(graphics));
                // graphics.translate(radious, radious);
            }
            if(ts2d.getFill() != null) {
                graphics.setPaint(ts2d.getFill());
                graphics.setComposite(ts2d.getComposite());
                graphics.drawGlyphVector(textGv, 0, 0);
            }
            graphics.setTransform(old);
        } else {
            // if the style is a polygon one, process it even if the polyline is not
            // closed (by SLD specification)
            if (style instanceof PolygonStyle2D) {
                PolygonStyle2D ps2d = (PolygonStyle2D) style;

                if (ps2d.getFill() != null) {
                    graphics.setPaint(ps2d.getFill());
                    graphics.setComposite(ps2d.getFillComposite());
                    graphics.fill(shape);
                }
            }

            if (style instanceof LineStyle2D) {
                LineStyle2D ls2d = (LineStyle2D) style;

                if (ls2d.getStroke() != null) {
                    graphics.setPaint(ls2d.getContour());
                    graphics.setStroke(ls2d.getStroke());
                    graphics.setComposite(ls2d.getContourComposite());
                    graphics.draw(shape);
                }
            }
        }
    }
}
