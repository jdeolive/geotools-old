/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2005, Institut de Recherche pour le Développement
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

import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.AlphaComposite;

import java.awt.Shape;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;

import java.awt.Image;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import java.awt.Component;

import org.geotools.display.canvas.AbstractGraphic;
import org.geotools.display.canvas.BufferedCanvas2D;
import org.geotools.display.canvas.GraphicPrimitive2D;
import org.geotools.display.canvas.ReferencedCanvas;
import org.opengis.display.canvas.Canvas;
import org.opengis.referencing.operation.TransformException;

import org.geotools.resources.i18n.Loggings;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.util.RangeSet;
import org.opengis.display.primitive.Graphic;


/**
 * Default canvas implementation using {@link Graphics2D}. Rendering can optionnaly make use
 * of {@link Image} buffers.
 *
 * @since 2.3
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
public class BufferedRenderer2D extends ReferencedRenderer2D {
    
    protected BufferedCanvas2D canvas = null;
        
    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void add(Graphic graphic) {
        super.add(graphic);
//        flushOffscreenBuffer(graphic.getZOrderHint());    ------------------------------------------ WILL BE CALLED BY A RENDERER EVENT
        getCanvas().repaint(); // Must be invoked last
    }

    /**
     * Removes the given {@code Graphic} from this canvas.
     */
    @Override
    public synchronized void remove(final Graphic graphic) {
        getCanvas().repaint(); // Must be invoked first
//        flushOffscreenBuffer(graphic.getZOrderHint());    ------------------------------------------ WILL BE CALLED BY A RENDERER EVENT
        super.remove(graphic);
    }

    /**
     * Remove all graphics from this canvas.
     */
    @Override
    public synchronized void removeAll() {
        getCanvas().repaint(); // Must be invoked first
//        flushOffscreenBuffers();  ------------------------------------------------------------------- WILL BE CALLED BY A RENDERER EVENT
        super.removeAll();
    }

    /**
     * 
     * @param output
     * @param zoom
     * @param displayBounds
     * @param isPrinting
     * @param offscreenCount
     * @param offscreenBuffers
     * @param offscreenZRanges
     * @param owner 
     * @param config
     * @param offscreenNeedRepaint 
     * @param offscreenIsVolatile
     * @param clipBounds 
     * @return
     */
    @Override
    public boolean paint(Graphics2D output, AffineTransform zoom, 
                                        Rectangle displayBounds, 
                                        final boolean isPrinting,
                                        int offscreenCount, 
                                        Image[] offscreenBuffers, 
                                        RangeSet<Double> offscreenZRanges,
                                        Component owner,
                                        boolean[] offscreenIsVolatile,
                                        boolean[] offscreenNeedRepaint,
                                        GraphicsConfiguration config,
                                        Rectangle clipBounds) {
                
                
        /*
         * Draw all graphics, starting with the one with the lowest <var>z</var> value. Before
         * to start the actual drawing,  we will notify all graphics that they are about to be
         * drawn. Some graphics may spend one or two threads for pre-computing data.
         */
        final List/*<Graphic>*/ graphics = getGraphics();
        final int graphicCount = graphics.size();
        boolean success = false;
        output.addRenderingHints(hints);
        final RenderingContext context = new RenderingContext(getCanvas(), displayBounds, isPrinting);
        try {
            context.setGraphics(output, zoom);
//            if (allowPrefetch()) {    -------------------------------------------------------------------RENDERER OPTIMISATION HINT
//                // Prepare data in background threads. While we are painting
//                // one graphic, next graphics will be pre-computed.
//                for (int i=0; i<graphicCount; i++) {
//                    final Graphic candidate = (Graphic) graphics.get(i);
//                    if (candidate instanceof GraphicPrimitive2D) {
//                        ((GraphicPrimitive2D) candidate).prefetch(context);
//                    }
//                }
//            }
            int    offscreenIndex = -1;                  // Index of current offscreen buffer.
            double minZOrder = Double.NEGATIVE_INFINITY; // The minimum z-value for current buffer.
            double maxZOrder = Double.NaN;               // The maximum z-value for current buffer.
            for (int graphicIndex=0; graphicIndex<graphicCount; graphicIndex++) {
                int graphicIndexUp = graphicIndex;
                Graphic candidate = (Graphic) graphics.get(graphicIndex);
                if (!(candidate instanceof GraphicPrimitive2D)) {
                    continue;
                }
                final GraphicPrimitive2D graphic = (GraphicPrimitive2D) candidate;
                final double zOrder = graphic.getZOrderHint();
                while (zOrder >= minZOrder) {
                    if (!(zOrder <= maxZOrder)) {
                        if (++offscreenIndex < offscreenCount) {
                            minZOrder = offscreenZRanges.getMinValueAsDouble(offscreenIndex);
                            maxZOrder = offscreenZRanges.getMaxValueAsDouble(offscreenIndex);
                            continue;
                        } else {
                            minZOrder = Double.NaN;
                            maxZOrder = Double.NaN;
                            break;
                        }
                    }
                    assert offscreenZRanges.indexOfRange(Double.valueOf(zOrder)) == offscreenIndex;
                    /*
                     * We have found the begining of a range to be rendered using offscreen
                     * buffer. Search the index of the last graphic in this range, exclusive.
                     */
                    while (++graphicIndexUp < graphicCount) {
                        candidate = (Graphic) graphics.get(graphicIndexUp);
                        if (!(((AbstractGraphic)candidate).getZOrderHint() <= maxZOrder)) {
                            break;
                        }
                    }
                    break;
                }
                /*
                 * If the current graphic is not part of any offscreen buffer,
                 * render it directly in the Swing's output handler.
                 */
                if (graphicIndex == graphicIndexUp) {
                    try {
                        paint(graphic, context, clipBounds);
                    } catch (TransformException exception) {
                        handleException(GraphicPrimitive2D.class, "paint", exception);
                    } catch (RuntimeException exception) {
                        handleException(GraphicPrimitive2D.class, "paint", exception);
                    }
                    continue;
                }
                /*
                 * The range of graphic index to render offscreen goes from 'graphicIndex'
                 * inclusive to 'graphicIndexUp' exclusive. If the image is still valid,
                 * paint it immediately. Otherwise, performs the rendering offscreen.
                 */
                boolean createFromComponent = (owner != null);
                Rectangle bufferClip = clipBounds;
                Image buffer = offscreenBuffers[offscreenIndex];
renderOffscreen:while (true) {
                    switch (validate(buffer, config)) {
                        //
                        // Image incompatible or inexistant: recreates an empty
                        // one and restarts the loop from the 'validate' check.
                        //
                        case VolatileImage.IMAGE_INCOMPATIBLE: {
                            if (buffer != null) {
                                buffer.flush();
                                buffer = null;
                            }
                            if (createFromComponent) {
                                createFromComponent = false;
                                if (offscreenIsVolatile[offscreenIndex]) {
                                    buffer = owner.createVolatileImage(
                                                   displayBounds.width, displayBounds.height);
                                }
                            }
                            if (buffer == null) {
                                if (offscreenIsVolatile[offscreenIndex]) {
                                    buffer = config.createCompatibleVolatileImage(
                                                    displayBounds.width, displayBounds.height);
                                } else {
                                    buffer = config.createCompatibleImage(
                                                    displayBounds.width, displayBounds.height,
                                                    Transparency.TRANSLUCENT);
                                }
                            }
                            offscreenBuffers[offscreenIndex] = buffer;
                            bufferClip = displayBounds;
                            continue;
                        }
                        //
                        // Image preserved: paint it immediately if no graphic changed
                        // since last rendering, or repaint only the damaged area.
                        //
                        case VolatileImage.IMAGE_OK: {
                            if (!offscreenNeedRepaint[offscreenIndex]) {
                                // REVISIT: Which AlphaComposite to use here?
                                output.drawImage(buffer, displayBounds.x,
                                                         displayBounds.y, owner);
                                if (contentsLost(buffer)) {
                                    // Upcomming 'validate' will falls in IMAGE_RESTORED case.
                                    continue;
                                }
                                // Rendering finished for this offscreen buffer.
                                break renderOffscreen;
                            }
                            // Repaint only the damaged area.
                            break;
                        }
                        //
                        // Contents has been lost: we need to repaint the whole image.
                        //
                        case VolatileImage.IMAGE_RESTORED: {
                            bufferClip = displayBounds;
                            break;
                        }
                    }
                    /*
                     * At this point, we know that some area need to be repainted.
                     * Reset a transparent background on the area to be repainted,
                     * and invokes GraphicPrimitive2D.update(...) for each graphics
                     * to be included in this offscreen buffer.
                     */
                    final Graphics2D graphicsOff = (Graphics2D) buffer.getGraphics();
                    final Composite oldComposite = graphicsOff.getComposite();
                    graphicsOff.addRenderingHints(hints);
                    graphicsOff.translate(-displayBounds.x, -displayBounds.y);
                    if (offscreenIsVolatile[offscreenIndex]) {
                        // HACK: We should use the transparent color in all cases. However,
                        //       as of J2SE 1.4, VolatileImage doesn't supports transparency.
                        //       We have to use some opaque color in the main time.
                        // TODO: Delete this hack when J2SE 1.5 will be available.
                        //       Avoid filling if the image has just been created.
                        graphicsOff.setComposite(AlphaComposite.Src);
                    } else {
                        graphicsOff.setComposite(AlphaComposite.Clear);
                    }
                    graphicsOff.setColor(owner!=null ? owner.getBackground() : Color.WHITE);
                    graphicsOff.fill(bufferClip);
                    graphicsOff.setComposite(oldComposite); // REVISIT: is it the best composite?
                    graphicsOff.setColor(owner!=null ? owner.getForeground() : Color.BLACK);
                    graphicsOff.clip(bufferClip); // Information needed by some graphics.
                    context.setGraphics(graphicsOff, zoom);
                    offscreenNeedRepaint[offscreenIndex] = false;
                    for (int i=graphicIndex; i<graphicIndexUp; i++) {
                        candidate = (Graphic) graphics.get(i);
                        if (candidate instanceof GraphicPrimitive2D) try {
                            paint((GraphicPrimitive2D) candidate, context, bufferClip);
                        } catch (Exception exception) {
                            /*
                             * An exception occured in user code. Do not try anymore to use
                             * offscreen buffer for this graphic; render it in the "ordinary"
                             * loop instead. If this exception still occurs, it will be the
                             * "ordinary" loop's job to handle it.
                             */
                            context.disposeGraphics();
                            buffer.flush();
                            offscreenBuffers[offscreenIndex] = buffer = null;
                            graphicIndexUp = graphicIndex; // Force re-rendering of this graphic.
                            minZOrder    = Double.NaN;  // Disable offscreen for all graphics.
                            handleOffscreenException((AbstractGraphic) candidate,exception);
                            break renderOffscreen;
                        }
                        if (contentsLost(buffer)) {
                            break;
                        }
                    }
                    context.disposeGraphics();
                }
                /*
                 * The offscreen buffer has been successfully rendered (or we failed because
                 * of an exception in user's code). If the ordinary (clipped) context was
                 * modified, it will be restored in the "ordinary" loop when first needed.
                 */
                graphicIndex = graphicIndexUp-1;
            }
            success = true;
        } finally {
            context.setGraphics(null, null);            
        }
        
        return success;
    }
    
    /**
     * {@linkplain GraphicPrimitive2D#paint Paints} the specified graphic and
     * {@linkplain GraphicPrimitive2D#setDisplayBounds update its display bounds}.
     * If the specified graphic is not {@linkplain GraphicPrimitive2D#getVisible visible}
     * or if {@code clipBounds} doesn't intersect the
     * {@linkplain GraphicPrimitive2D#getDisplayBounds graphic display bounds},
     * then this method do nothing.
     *
     * @param graphic    The graphics to paint.
     * @param context    Information relative to the rendering context.
     * @param clipBounds The area to paint in terms of display CRS, or {@code null}.
     * @throws TransformException If a coordinate transformation failed during the rendering
     *         process.
     */
    private void paint(final GraphicPrimitive2D graphic,
                       final RenderingContext   context,
                       final Rectangle          clipBounds)
            throws TransformException
    {
        assert Thread.holdsLock(this);
        if (graphic.getVisible()) {
            final Shape paintedArea = graphic.getDisplayBounds();
            if (paintedArea==null || clipBounds==null || paintedArea.intersects(clipBounds)) {
                context.paintedArea = null;
                graphic.paint(context);
                graphic.setDisplayBounds(context.paintedArea);
            }
        }
    }
    
    
    
    /**
     * Logs a warning message when the offscreen rendering failed for some graphics.
     * The message is logged with a low level (FINE rather than WARNING) because an
     * other attempt will be done using the default rendering loop (without offscreen buffer).
     *
     * @param graphic The graphic for which the offscreen rendering failed.
     * @param exception The exception.
     */
    private void handleOffscreenException(final AbstractGraphic graphic, final Exception exception) {
        final Locale locale = getLocale();
        final LogRecord record = Loggings.getResources(locale).getLogRecord(Level.FINE,
                LoggingKeys.OFFSCREEN_RENDERING_FAILED_$1, graphic.getName());
        record.setSourceClassName(BufferedRenderer2D.class.getName());
        record.setSourceMethodName("paint");
        record.setThrown(exception);
        getLogger().log(record);
    }
    
    /**
     * Returns {@code true} if rendering data was lost since last validate call.
     */
    private static boolean contentsLost(final Image image) {
        if (image instanceof VolatileImage) {
            return ((VolatileImage) image).contentsLost();
        }
        return false;
    }
    
    
    /**
     * Checks the state of the given image. If {@code image} is null, then this method returns
     * {@link VolatileImage#IMAGE_INCOMPATIBLE}. Otherwise, if {@code image} is an instance of
     * {@link VolatileImage}, then this method invokes {@link VolatileImage#validate}. Otherwise,
     * (usually the {@link BufferedImage} case) this method returns {@link VolatileImage#IMAGE_OK}.
     *
     * @param  image The image.
     * @param  config The graphics configuration.
     * @return The state, as one of {@link VolatileImage} constants.
     */
    private static int validate(final Image image, final GraphicsConfiguration config) {
        if (image == null) {
            return VolatileImage.IMAGE_INCOMPATIBLE;
        }
        if (image instanceof VolatileImage) {
            return ((VolatileImage) image).validate(config);
        }
        return VolatileImage.IMAGE_OK;
    }

    @Override
    public BufferedCanvas2D getCanvas() {
        return canvas;
    }

    @Override
    public void setCanvas(Canvas canvas) {
        this.canvas = (BufferedCanvas2D) canvas;
    }
    
    
}
