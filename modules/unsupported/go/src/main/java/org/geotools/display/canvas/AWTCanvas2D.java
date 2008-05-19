/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.display.canvas;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.geotools.display.primitive.AbstractGraphic;
import org.geotools.display.renderer.AbstractRenderer;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.i18n.Loggings;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.opengis.display.canvas.CanvasController;
import org.opengis.display.primitive.Graphic;
import org.opengis.geometry.DirectPosition;

/**
 *
 * @author sorel
 */
public class AWTCanvas2D extends ReferencedCanvas2D implements CanvasController{
    
    /**
     * The component owner, or {@code null} if none. This is used for managing
     * repaint request (see {@link GraphicPrimitive2D#refresh}) or mouse events.
     */
    private final Component owner;

    /**
     * Listener for events of interest to this canvas. Events may come
     * from any {@link GraphicPrimitive2D} or from the {@link Component}.
     */
    private final ComponentListener listener = new ComponentListener();

    /**
     * Updates the enclosing canvas according various AWT events.
     */
    private final class ComponentListener extends ComponentAdapter {
        /** Invoked when the component's size changes. */
        @Override public void componentResized(final ComponentEvent event) {
            synchronized (AWTCanvas2D.this) {
                checkDisplayBounds();
                zoomChanged(null);
            }
        }

        /** Invoked when the component's position changes. */
        @Override public void componentMoved(final ComponentEvent event) {
            synchronized (AWTCanvas2D.this) {
                checkDisplayBounds();
                zoomChanged(null); // Translation term has changed.
            }
        }

        /** Invoked when the component has been made invisible. */
        @Override public void componentHidden(final ComponentEvent event) {
            synchronized (AWTCanvas2D.this) {
                clearCache();
            }
            // As a symetrical approach,  it would be nice to invoke 'prefetch(...)' inside
            // 'componentShown(...)' too. But we don't know for sure what the widget bounds
            // and the zoom will be. We are better to wait until 'paint(...)' is invoked.
        }
    }
    
    
    public AWTCanvas2D(final AbstractRenderer renderer, final Component owner){
        super(renderer);
                                
        this.owner = owner;
        if (owner != null) {
            owner.addComponentListener(listener);
        } 
    }
        
    /**
     * Returns the display bounds in terms of {@linkplain #getDisplayCRS display CRS}.
     * If no bounds were {@linkplain #setDisplayBounds explicitly set}, then this method
     * returns the {@linkplain Component#getBounds() widget bounds}.
     */
    @Override
    public synchronized Shape getDisplayBounds() {
        Shape bounds = super.getDisplayBounds();
        if (bounds.equals(XRectangle2D.INFINITY) && owner!=null) {
            bounds = owner.getBounds();
        }
        return bounds;
    }
        
    
    
    //----------------------AWT Paint methods ----------------------------------    
    public void paint(Graphics2D output, final AffineTransform zoom){
        renderer.paint(output,zoom);
    }
            
    /**
     * Declares that the {@link Component} need to be repainted. This method can be invoked
     * from any thread (it doesn't need to be the <cite>Swing</cite> thread). Note that this
     * method doesn't invoke any {@link #flushOffscreenBuffer} method; this is up to the caller
     * to invokes the appropriate method.
     */
    public void repaint() {
        if (owner != null) {
            owner.repaint();            
        }
    }
    
    /**
     * {@inheritDoc}
     *
     * @todo Check for dirty area should take zOrder in account.
     */
    public void repaint(final Graphic     graphic,
                        final Rectangle2D objectiveArea,
                        final Rectangle   displayArea)
    {
        // Do NOT synchronize before next block.
        /*
         * If the current thread is not the Swing thread, schedule for
         * execution in the Swing thread and returns immediatement.
         */
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                    repaint(graphic, objectiveArea, displayArea);
                }
            });
            return;
        }
        /*
         * At this point, we know that we are running in the Swing thread. Computes the union of
         * (transformed) objective and display rectangle.  Note: we performs this computation in
         * the Swing thread in order to make sure that we use the affine transform from the last
         * painting.
         */
        final Rectangle bounds;
        synchronized (this) {
            if (objectiveArea != null) {
                if (objectiveArea.equals(XRectangle2D.INFINITY)) {
                    bounds = null;
                } else {
                    bounds = objectiveToDisplay(objectiveArea);
                    if (displayArea != null) {
                        bounds.add(displayArea);
                    }
                }
            } else if (displayArea != null) {
                bounds = displayArea;
            } else {
                return; // Both display and objective area are null: nothing to repaint.
            }
            /*
             * Now we know the display bounding box to repaint. If this area is already in process
             * of being painted (i.e. 'paintStarted' has been invoked but not yet 'paintFinished'),
             * do not send an other 'repaint' event in order to avoid to paint the exact same thing
             * twice. This optimization occurs when a graphic changed its state after the renderer
             * started to paint but before the paint process reached the graphic. The graphic may
             * have changed its state as a result of a "scale" property change event.
             */
            if (isDirtyArea(bounds)) {
                return;
            }
            
            //--------------------------------------------------------------------------------------must be in the renderer
//            /*
//             * Flush the offscreen buffers and send the repaint event. The paint method
//             * will be invoked by Swing at some later, widget-dependent, time.
//             */
//            if (graphic != null && graphic instanceof AbstractGraphic) {
//                flushOffscreenBuffer( ((AbstractGraphic)graphic).getZOrderHint());
//            } else {
//                flushOffscreenBuffers();
//            }
            if (owner != null) {
                if (bounds != null) {
                    owner.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
                } else {
                    owner.repaint();
                }
            }
        }
        /*
         * We are done. Log a debug string if the logging level is finest. The code below
         * is almost never run, except when investigating for performance bootleneck.
         */
        final Logger logger = getLogger();
        if (logger.isLoggable(Level.FINEST)) {
            final Loggings resources = Loggings.getResources(getLocale());
            final String name = (graphic!=null && graphic instanceof AbstractGraphic) ? ((AbstractGraphic)graphic).getName()
                                                : Vocabulary.format(VocabularyKeys.UNKNOW);
            final LogRecord record;
            if (bounds != null) {
                record = resources.getLogRecord(Level.FINEST,
                                LoggingKeys.SEND_REPAINT_EVENT_$5, new Object[] {name,
                                new Integer(bounds.x), new Integer(bounds.x+bounds.width-1),
                                new Integer(bounds.y), new Integer(bounds.y+bounds.height-1)});
            } else {
                record = resources.getLogRecord(Level.FINEST,
                                LoggingKeys.SEND_REPAINT_EVENT_$1, name);
            }
            record.setSourceClassName(AWTCanvas2D.class.getName());
            record.setSourceMethodName("repaint");
            logger.log(record);
        }
    }

    
    
    //--------------------Canvas Controller methods ----------------------------
    public CanvasController getController() {
        return this;
    }
    
    public void setCenter(DirectPosition center) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
