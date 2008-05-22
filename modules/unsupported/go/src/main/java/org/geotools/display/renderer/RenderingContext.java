/*
 *    GeoTools - An Open Source Java GIS Tookit
 *    http://geotools.org
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.display.renderer;

import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.AffineTransform;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.TransformException;
import org.opengis.referencing.operation.CoordinateOperationFactory; // For javadoc

import org.geotools.factory.Hints;  // For javadoc
import org.geotools.display.canvas.ReferencedCanvas;
import org.geotools.referencing.CRS;
import org.geotools.resources.CRSUtilities;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;


/**
 * Informations relative to a rendering in progress. A {@code RenderingContext} instance is
 * created by {@link AWTDirectRenderer2D#paint} at rendering time, which iterates over all graphic
 * objects and invokes {@link GraphicPrimitive2D#paint} for each of them. The rendering context
 * is disposed once the rendering is completed. {@code RenderingContext} instances contain the
 * following informations:
 * <p>
 * <ul>
 *   <li>The {@link Graphics2D} handler to use for rendering.</li>
 *   <li>The coordinate reference systems in use and the transformations between them.</li>
 *   <li>The area rendered up to date. This information shall be updated by each
 *       {@link GraphicPrimitive2D} while they are painting.</li>
 *   <li>The map scale.</li>
 * </ul>
 * <p>
 * A rendering usually implies the following transformations (names are
 * {@linkplain CoordinateReferenceSystem coordinate reference systems} and arrows
 * are {@linkplain MathTransform transforms}):
 * 
 * <p align="center">
 * &nbsp; {@code graphicCRS}    &nbsp; <img src="doc-files/right.png">
 * &nbsp; {@link #objectiveCRS} &nbsp; <img src="doc-files/right.png">
 * &nbsp; {@link #displayCRS}   &nbsp; <img src="doc-files/right.png">
 * &nbsp; {@code deviceCRS}
 * </p>
 * 
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 *
 * @see BufferedCanvas2D#paint
 * @see GraphicPrimitive2D#paint
 */
public final class RenderingContext {
    /**
     * The originating canvas.
     */
    private final ReferencedCanvas canvas;

    /**
     * The graphics handle to use for painting. This graphics is set by {@link BufferedCanvas2D}
     * when a new painting in underway. It is reset to {@code null} once the rendering is finished.
     *
     * @see #getGraphics
     * @see BufferedCanvas2D#paint
     */
    private Graphics2D graphics;

    /**
     * A snapshot of {@link ReferencedCanvas#getObjectiveCRS} at the time of painting. This is the
     * "real world" coordinate reference system that the user will see on the screen. Data from all
     * {@link GraphicPrimitive2D} must be transformed to this CRS before to be painted. Units are
     * usually "real world" metres.
     * <p>
     * This coordinate system is usually set once for a given {@link BufferedCanvas2D} and do not
     * change anymore, except if the user wants to change the projection see on screen.
     *
     * @see #displayCRS
     * @see #setGraphicsCRS
     * @see ReferencedCanvas#getObjectiveCRS
     */
    public final CoordinateReferenceSystem objectiveCRS;

    /**
     * A snapshot of {@link ReferencedCanvas#getDisplayCRS} at the time of painting. This CRS maps
     * the {@linkplain Graphics2D user space} in terms of <cite>Java2D</cite>: each "unit" is a dot
     * (about 1/72 of inch). <var>x</var> values increase toward the right of the screen and
     * <var>y</var> values increase toward the bottom of the screen. This CRS is appropriate
     * for rendering text and labels.
     * <p>
     * This coordinate system may be different between two different renderings,
     * especially if the zoom (or map scale) has changed since the last rendering.
     *
     * @see #objectiveCRS
     * @see #setGraphicsCRS
     * @see ReferencedCanvas#getDisplayCRS
     */
    public final CoordinateReferenceSystem displayCRS;

    /**
     * The affine transform from {@link #objectiveCRS} to {@code deviceCRS}. Used by
     * {@link #setGraphicsCRS} when the CRS is {@link #objectiveCRS}. This is a pretty common case,
     * and unfortunatly one that is badly optimized by {@link ReferencedCanvas#getMathTransform}.
     */
    private AffineTransform objectiveToDevice;

    /**
     * The affine transform from {@link #displayCRS} to {@code deviceCRS}.
     * Used by {@link #setGraphicsCRS} when the CRS is {@link #displayCRS}.
     */
    private AffineTransform displayToDevice;

    /**
     * The painted area in the {@linkplain #displayCRS Java2D user space}, or {@code null}
     * if unknow. This field is built by {@link #addPaintedArea} at rendering time, and
     * read by {@link BufferedCanvas2D} only.
     */
    public Shape paintedArea;

    /**
     * The widget bounding box, in coordinates of {@link #displayCRS}.
     */
    private final Rectangle displayBounds;

    /**
     * The widget bounding box, in coordinates of {@link #objectiveCRS}.
     * Will be computed only when first needed.
     */
    private transient Shape objectiveBounds;

    /**
     * {@code true} if the map is printed instead of painted on screen. When printing, graphic
     * primitives should block until all data are available instead of painting only available
     * data and invoke {@link GraphicPrimitive2D#refresh()} later.
     */
    private final boolean isPrinting;

    /**
     * Constructs a new {@code RenderingContext} for the specified canvas.
     *
     * @param canvas        The canvas which creates this rendering context.
     * @param displayBounds The drawing area in display coordinates.
     * @param isPrinting    {@code true} if this context is used for printing.
     */
    public RenderingContext(final ReferencedCanvas canvas,
                     final Rectangle displayBounds,
                     final boolean      isPrinting)
    {
        this.canvas        = canvas;
        this.objectiveCRS  = canvas.getObjectiveCRS();
        this.displayCRS    = canvas.getDisplayCRS();
        this.displayBounds = displayBounds;
        this.isPrinting    = isPrinting;
    }

    /**
     * Sets the destination {@link Graphics2D}. This method is invoked
     * for switching rendering between different offscreen buffers.
     *
     * @param graphics           The graphic handle.
     * @param objectiveToDisplay The {@linkplain #objectiveCRS objective} to {@linkplain #display
     *                           display} transform.
     */
    public final void setGraphics(final Graphics2D graphics, final AffineTransform objectiveToDisplay) {
        this.graphics = graphics;
        if (graphics != null) {
            displayToDevice   = graphics.getTransform();
            objectiveToDevice = new AffineTransform(displayToDevice);
            objectiveToDevice.concatenate(objectiveToDisplay);
        } else {
            objectiveToDevice = null;
            displayToDevice   = null;
        }
        paintedArea = null;
    }

    /**
     * Disposes the {@link Graphics2D}. This method is invoked only when the graphics
     * was used for offscreen rendering.
     */
    public final void disposeGraphics() {
        graphics.dispose();
        graphics = null;
    }

    /**
     * Returns the graphics where painting occurs. The initial coordinate reference system is
     * {@link #displayCRS}, which maps the <cite>Java2D</cite> {@linkplain Graphics2D user space}.
     * For drawing shapes directly in terms of "real world" coordinates, users should invoke
     * <code>{@linkplain #setGraphicsCRS setGraphicsCRS}({@linkplain #objectiveCRS})</code>.
     */
    public final Graphics2D getGraphics() {
        return graphics;
    }

    /**
     * Returns the painting area in the specified coordinate reference system. If the CRS
     * is {@link #displayCRS}, then this method will usually returns the widget's bounds
     * ({@link java.awt.Component#getBounds}).
     *
     * @param  crs The coordinate reference system (usually one of {@link #displayCRS} or
     *         {@link #objectiveCRS}).
     * @return The painting area in terms of the specified CRS.
     * @throws TransformException if the painting area can be expressed in the specified CRS.
     */
    public Shape getPaintingArea(CoordinateReferenceSystem crs) throws TransformException {
        crs = CRSUtilities.getCRS2D(crs);
        if (CRS.equalsIgnoreMetadata(displayCRS, crs)) {
            return displayBounds;
        }
        final boolean isObjectiveCRS = CRS.equalsIgnoreMetadata(objectiveCRS, crs);
        if (isObjectiveCRS && objectiveBounds!=null) {
            return objectiveBounds;
        }
        final MathTransform2D mt;
        try {
            mt = (MathTransform2D) getMathTransform(displayCRS, crs);
        } catch (FactoryException e) {
            throw new TransformException(Errors.format(ErrorKeys.CANT_TRANSFORM_ENVELOPE), e);
        }
        final Shape userBounds = mt.createTransformedShape(displayBounds);
        if (isObjectiveCRS) {
            // Cache for later reuse.
            objectiveBounds = userBounds;
        }
        return userBounds;
    }

    /**
     * Returns the painting area in display CRS. Invoking this method is equivalent to invoking
     * {@code getPaintingArea(displayCRS).getBounds()}, except that this method returns a direct
     * reference to its internal rectangle. <strong>Do not modify!</strong> This method is provided
     * for {@link GraphicLegend#translate} implementation only.
     *
     * @deprecated Need to find a replacement for that.
     */
    final Rectangle getPaintingArea() {
        return displayBounds;
    }

    /**
     * Sets the coordinate reference system in use for rendering in {@link Graphics2D}. Invoking
     * this method do not alter the current state of any canvas or GO-1 graphic objects. It is
     * only a convenient way to {@linkplain Graphics2D#setTransform set the affine transform} in
     * the current <cite>Java2D</cite> {@link Graphics2D} handle, for example in order to alternate
     * rendering mode between geographic features and labels. The specified coordinate reference
     * system (the {@code crs} argument) is usually (but not limited to) one of
     * {@link #objectiveCRS} or {@link #displayCRS} values.
     *
     * @param  crs The CRS for the {@link #getGraphics() Java2D graphics handle}.
     * @throws TransformException if this method failed to find an affine transform from the
     *         specified CRS to the device CRS.
     *
     * @see #getGraphics
     * @see #getAffineTransform
     * @see Graphics2D#setTransform
     */
    public void setGraphicsCRS(CoordinateReferenceSystem crs) throws TransformException {
        final AffineTransform at;
        if (crs == objectiveCRS) {
            // Optimization for a pretty common case.
            at = objectiveToDevice;
        } else if (crs == displayCRS) {
            // Optimization for a pretty common case.
            at = displayToDevice;
        } else try {
            crs = CRSUtilities.getCRS2D(crs);
            at = getAffineTransform(crs, displayCRS);
            at.preConcatenate(displayToDevice);
        } catch (FactoryException e) {
            throw new TransformException(Errors.format(
                        ErrorKeys.ILLEGAL_COORDINATE_REFERENCE_SYSTEM), e);
        }
        graphics.setTransform(at);
    }

    /**
     * Returns an affine transform between two coordinate reference systems. This method is
     * equivalents to the following pseudo-code, except for the exception to be thrown if the
     * transform is not an instance of {@link AffineTransform}.
     * 
     * <blockquote><pre>
     * return (AffineTransform) {@link #getMathTransform getMathTransform}(sourceCRS, targetCRS);
     * </pre></blockquote>
     * 
     * @param sourceCRS The source coordinate reference system.
     * @param targetCRS The target coordinate reference system.
     * @return An affine transform from {@code sourceCRS} to {@code targetCRS}.
     * @throws FactoryException if the transform can't be created or is not affine.
     *
     * @see #getMathTransform
     * @see BufferedCanvas2D#getImplHint
     * @see Hints#COORDINATE_OPERATION_FACTORY
     */
    public AffineTransform getAffineTransform(final CoordinateReferenceSystem sourceCRS,
                                              final CoordinateReferenceSystem targetCRS)
            throws FactoryException
    {
        final MathTransform mt =
                canvas.getMathTransform(sourceCRS, targetCRS,
                        RenderingContext.class, "getAffineTransform");
        try {
            return (AffineTransform) mt;
        } catch (ClassCastException cause) {
            throw new FactoryException(Errors.format(ErrorKeys.NOT_AN_AFFINE_TRANSFORM), cause);
        }
    }

    /**
     * Returns a transform between two coordinate systems. If a {@link
     * Hints#COORDINATE_OPERATION_FACTORY} has been provided to the {@link BufferedCanvas2D},
     * then the specified {@linkplain CoordinateOperationFactory coordinate operation factory}
     * will be used. The arguments are usually (but not necessarily) one of the following pairs:
     *
     * <ul>
     *   <li><p><b>({@code graphicCRS}, {@linkplain #objectiveCRS}):</b><br>
     *       Arbitrary transform from the data CRS (used internally in a {@link GraphicPrimitive2D})
     *       to the objective CRS (set in {@link BufferedCanvas2D}).</p></li>
     * 
     *   <li><p><b>({@link #objectiveCRS}, {@link #displayCRS}):</b><br>
     *       {@linkplain AffineTransform Affine transform} from the objective CRS in "real world"
     *       units (usually metres or degrees) to the display CRS in dots (usually 1/72 of inch).
     *       This transform changes every time the zoom (or map scale) changes.</p></li>
     * </ul>
     *
     * @param sourceCRS The source coordinate reference system.
     * @param targetCRS The target coordinate reference system.
     * @return A transform from {@code sourceCRS} to {@code targetCRS}.
     * @throws FactoryException if the transformation can't be created.
     *
     * @see #getAffineTransform
     * @see BufferedCanvas2D#getImplHint
     * @see Hints#COORDINATE_OPERATION_FACTORY
     */
    public MathTransform getMathTransform(final CoordinateReferenceSystem sourceCRS,
                                          final CoordinateReferenceSystem targetCRS)
            throws FactoryException
    {
        return canvas.getMathTransform(sourceCRS, targetCRS,
                RenderingContext.class, "getMathTransform");
    }

    /**
     * Returns the scale factor, or {@link Double#NaN NaN} if the scale is unknow. The scale factor
     * is usually smaller than 1. For example for a 1:1000 scale, the scale factor will be 0.001.
     * This scale factor takes in account the physical size of the rendering device (e.g. the
     * screen size) if such information is available. Note that this scale can't be more accurate
     * than the {@linkplain java.awt.GraphicsConfiguration#getNormalizingTransform() information
     * supplied by the underlying system}.
     *
     * @return The rendering scale factor as a number between 0 and 1, or {@link Double#NaN}.
     * @see BufferedCanvas2D#getScale
     */
    public double getScale() {
        return 1d;
//        return canvas.getScale();
    }

    /**
     * Returns {@code true} if the output device is a printer instead of screen. When printing,
     * graphic primitives should block until all data are available instead of painting only
     * available data and invoke {@link GraphicPrimitive2D#refresh()} later.
     */
    public boolean isPrinting() {
        return isPrinting;
    }

    /**
     * Declares that an area has been painted in the {@linkplain #getGraphics() graphics handler}.
     * The coordinate reference system for {@code area} is infered from the
     * {@linkplain Graphics2D#getTransform current affine transform} set in the graphic handler.
     * Invoking this method is equivalents to invoking <code>{@linkplain #addPaintedArea(Shape,
     * CoordinateReferenceSystem) addPaintedArea}(area, null)</code>.
     *
     * @param area A bounding shape of the area just painted. This shape may be approximative,
     *        as long as it completely encloses the painted area. Simple shapes with fast
     *        {@code contains(...)} and {@code intersects(...)} methods are encouraged.
     *
     * @see #addPaintedArea(Shape, CoordinateReferenceSystem)
     * @see Graphics2D#getTransform
     */
    public void addPaintedArea(final Shape area) {
        try {
            addPaintedArea(area, null);
        } catch (TransformException exception) {
            // Should never happen, since the 'crs' argument was null.
            throw new AssertionError(exception);
        }
    }

    /**
     * Declares that an area has been painted. This method should be invoked from
     * {@link GraphicPrimitive2D#paint} at rendering time. {@link BufferedCanvas2D}
     * uses this information in order to determine which graphic primitives need to
     * be repainted when a screen area is damaged. If {@code addPaintedArea(...)}
     * methods are never invoked from a particular {@link GraphicPrimitive2D}, then
     * the canvas will assumes that the painted area is unknow and conservatively
     * repaint all graphic primitives during subsequent rendering.
     *
     * @param  area A bounding shape of the area just painted. This shape may be approximative,
     *         as long as it completely encloses the painted area. Simple shapes with fast
     *         {@code contains(...)} and {@code intersects(...)} methods are encouraged.
     * @param  crs The coordinate reference system for {@code area}, or {@code null} to infer it
     *         from the {@linkplain Graphics2D#getTransform current affine transform} set in the
     *         graphic handler.
     * @throws TransformException if {@code area} coordinates can't be transformed.
     */
    public void addPaintedArea(Shape area, CoordinateReferenceSystem crs) throws TransformException {
        final Shape userArea = area;
        if (crs != null) {
            /*
             * Transforms the shape. NOTE: we invokes 'inverse()' instead of swapping 'sourceCRS'
             * and 'targetCRS' arguments in order to give a chance to 'getMathTransform' to uses
             * the Canvas cache (the cache will never be used if the 'targetCRS' is 'displayCRS').
             */
            crs = CRSUtilities.getCRS2D(crs);
            final MathTransform mt;
            try {
                mt = canvas.getMathTransform(displayCRS, crs,
                        RenderingContext.class, "addPaintedArea");
            } catch (FactoryException e) {
                throw new TransformException(Errors.format(ErrorKeys.CANT_TRANSFORM_ENVELOPE, e));
            }
            final MathTransform2D transform = (MathTransform2D) mt.inverse();
            if (!transform.isIdentity()) {
                area = transform.createTransformedShape(area);
            }
        } else {
            final AffineTransform transform = graphics.getTransform();
            if (!transform.isIdentity()) {
                area = transform.createTransformedShape(area);
            }
        }
        /*
         * Now add the painted area.
         */
        if (paintedArea == null) {
            if (area==userArea && area instanceof Area) {
                // Protect the user's object from changes,
                // since the code below may update the area.
                area = new Area(area);
            }
            paintedArea = area;
        } else {
            if (!(paintedArea instanceof Area)) {
                paintedArea = new Area(area);
            }
            ((Area) paintedArea).add((area instanceof Area) ? (Area) area : new Area(area));
        }
    }
}
