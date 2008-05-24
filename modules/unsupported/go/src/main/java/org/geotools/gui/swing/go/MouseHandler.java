/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.gui.swing.go;

import java.awt.Component;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import org.geotools.display.canvas.AWTCanvas2D;
import org.geotools.gui.swing.go.handler.CanvasHandler;

/**
 *
 * @author sorel
 */
public class MouseHandler implements CanvasHandler{
    
    /**
     * Zoom factor.  This factor must be greater than 1.
     */
    private static final double AMOUNT_SCALE = 1.03125;
    
    /**
     * Object in charge of drawing a box representing the user's selection.  We
     * retain a reference to this object in order to be able to register it and
     * extract it at will from the list of objects interested in being notified
     * of the mouse movements.
     */
    private final MouseListener mouseSelectionTracker = new MouseSelectionTracker() {
        /**
         * Returns the selection shape. This is usually a rectangle, but could
         * very well be an ellipse or any other kind of geometric shape. This
         * method asks {@link ZoomPane#getMouseSelectionShape} for the shape.
         */
        @Override
        protected Shape getModel(final MouseEvent event) {
            final Point2D point = new Point2D.Double(event.getX(), event.getY());
//            if (getZoomableBounds().contains(point)) try {
//                return getMouseSelectionShape(zoom.inverseTransform(point, point));
//            } catch (NoninvertibleTransformException exception) {
//                unexpectedException("getModel", exception);
//            }
            return null;
        }

        /**
         * Invoked when the user finishes the selection. This method will
         * delegate the action to {@link ZoomPane#mouseSelectionPerformed}.
         * Default implementation will perform a zoom.
         */
        protected void selectionPerformed(int ox, int oy, int px, int py) {
            try {
                final Shape selection = getSelectedArea(canvas.getTransform());
                if (selection != null) {
                    mouseSelectionPerformed(selection);
                }
            } catch (NoninvertibleTransformException exception) {
                exception.printStackTrace();
            }
        }
    };

    /**
     * Class responsible for listening out for the different events necessary for the smooth
     * working of {@link ZoomPane}. This class will listen out for mouse clicks (in order to
     * eventually claim the focus or make a contextual menu appear).  It will listen out for
     * changes in the size of the component (to adjust the zoom), etc.
     *
     * @version $Id: ZoomPane.java 28522 2007-12-27 21:51:30Z desruisseaux $
     * @author Martin Desruisseaux
     */
    private final class Listeners extends MouseAdapter
            implements MouseWheelListener, ComponentListener, Serializable
    {
        public void mouseWheelMoved (final MouseWheelEvent event) {MouseHandler.this.mouseWheelMoved (event);}
        public void mousePressed    (final MouseEvent      event) {}
        public void mouseReleased   (final MouseEvent      event) {}
        public void componentResized(final ComponentEvent  event) {}
        public void componentMoved  (final ComponentEvent  event) {}
        public void componentShown  (final ComponentEvent  event) {}
        public void componentHidden (final ComponentEvent  event) {}
    }
       
    private final Listeners listeners = new Listeners();
    
    private AWTCanvas2D canvas;
    
    public void setCanvas(AWTCanvas2D canvas) {        
        this.canvas = canvas;        
    }

    public AWTCanvas2D getCanvas() {
        return canvas;
    }

    public void install(Component component) {        
        component.addComponentListener  (listeners);
        component.addMouseListener(listeners);
        component.addMouseWheelListener(listeners);
        component.addMouseListener(mouseSelectionTracker);
    }

    public void uninstall(Component component) {
        component.removeComponentListener  (listeners);
        component.removeMouseListener(listeners);
        component.removeMouseWheelListener(listeners);
        component.removeMouseListener(mouseSelectionTracker);
    }
    
    
    //---------mouse events ----------------------------------------------------
    
    /**
     * Method called automatically when user moves the mouse wheel. This method
     * performs a zoom centred on the mouse position.
     */
    private final void mouseWheelMoved(final MouseWheelEvent event) {
        if (event.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            int rotation  = event.getUnitsToScroll();
            double scale  = 1 + (AMOUNT_SCALE - 1) * Math.abs(rotation);
            Point2D point = new Point2D.Double(event.getX(), event.getY());
            if (rotation > 0) {
                scale = 1 / scale;
            }
                        
            canvas.scale(scale, point);
            
            event.consume();
        }
    }
    
    /**
     * Method called automatically after the user selects an area with the mouse. The default
     * implementation zooms to the selected {@code area}. Derived classes can redefine this method
     * in order to carry out another action.
     *
     * @param area Area selected by the user, in logical coordinates.
     */
    protected void mouseSelectionPerformed(final Shape area) {
        final Rectangle2D rect = (area instanceof Rectangle2D) ? (Rectangle2D) area : area.getBounds2D();
        if (isValid(rect)) {
            canvas.setVisibleArea(rect);
        }
    }
    

    
    /**
     * Checks whether the rectangle {@code rect} is valid.  The rectangle
     * is considered invalid if its length or width is less than or equal to 0,
     * or if one of its coordinates is infinite or NaN.
     */
    private static boolean isValid(final Rectangle2D rect) {
        if (rect == null) {
            return false;
        }
        final double x = rect.getX();
        final double y = rect.getY();
        final double w = rect.getWidth();
        final double h = rect.getHeight();
        return (x > Double.NEGATIVE_INFINITY && x < Double.POSITIVE_INFINITY &&
                y > Double.NEGATIVE_INFINITY && y < Double.POSITIVE_INFINITY &&
                w > 0                        && w < Double.POSITIVE_INFINITY &&
                h > 0                        && h < Double.POSITIVE_INFINITY);
    }
    
}
