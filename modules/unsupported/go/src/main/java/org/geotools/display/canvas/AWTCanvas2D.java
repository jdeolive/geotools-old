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
package org.geotools.display.canvas;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.geotools.util.logging.Logging;
import org.geotools.display.primitive.AbstractGraphic;
import org.geotools.display.renderer.AWTDirectRenderer2D;
import org.geotools.display.renderer.AbstractRenderer;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralDirectPosition;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.operation.matrix.AffineTransform2D;
import org.geotools.referencing.operation.matrix.XAffineTransform;
import org.geotools.resources.GraphicsUtilities;
import org.geotools.resources.geometry.XDimension2D;
import org.geotools.resources.geometry.XRectangle2D;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.LoggingKeys;
import org.geotools.resources.i18n.Loggings;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;

import org.opengis.display.canvas.CanvasController;
import org.opengis.display.primitive.Graphic;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.operation.TransformException;

/**
 * Default implementation of AWT canvas 2D.
 * 
 * @author Martin Desruisseaux (IRD)
 * @author Johann Sorel (Geomatys)
 */
public class AWTCanvas2D extends ReferencedCanvas2D implements CanvasController{
    /**
     * The name of the {@linkplain PropertyChangeEvent property change event} fired when the
     * {@linkplain AWTCanvas2D#getHandler canvas handler} changed.
     */
    public static final String HANDLER_PROPERTY = "handler";

    /**
     * Small number for floating point comparaisons.
     */
    private static final double EPS = 1E-12;

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

    private CanvasHandler handler;
    
    private final DirectPosition objectiveCenter = new DirectPosition2D();

    /**
     * Rectangle in which to place the coordinates returned by {@link #getDisplayBounds}. This
     * object is defined in order to avoid allocating objects too often {@link Rectangle}.
     */
    private transient Rectangle cachedBounds;

    /**
     * Updates the enclosing canvas according various AWT events.
     */
    private final class ComponentListener extends ComponentAdapter {
        /** Invoked when the component's size changes. */
        @Override public void componentResized(final ComponentEvent event) {
            synchronized (AWTCanvas2D.this) {
                //cache bounds
                cachedBounds = event.getComponent().getBounds(cachedBounds);
                setDisplayBounds(cachedBounds);
                checkDisplayBounds();
                
                zoomChanged(null);
            }
        }

        /** Invoked when the component's position changes. */
        @Override public void componentMoved(final ComponentEvent event) {
            synchronized (AWTCanvas2D.this) {
                //cache bounds
                cachedBounds = event.getComponent().getBounds(cachedBounds);
                setDisplayBounds(cachedBounds);
                checkDisplayBounds();
                
                zoomChanged(null); // Translation term has changed.
            }
        }

        /** Invoked when the component has been made invisible. */
        @Override public void componentHidden(final ComponentEvent event) {
            synchronized (AWTCanvas2D.this) {
                cachedBounds.x = 0;
                cachedBounds.y = 0;
                cachedBounds.width = 0;
                cachedBounds.height = 0;
                setDisplayBounds(cachedBounds);
                
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

    public CanvasHandler getHandler(){
        return handler;
    }

    public void setHandler(CanvasHandler handler){

        if(this.handler != handler) {
            //TODO : check for possible vetos

            final CanvasHandler old = this.handler;

            if (this.handler != null){
                this.handler.uninstall(owner);
                this.handler.setCanvas(null);
            }

            this.handler = handler;

            if (this.handler != null) {
                this.handler.setCanvas(this);
                this.handler.install(owner);
            }

            propertyListeners.firePropertyChange(HANDLER_PROPERTY, old, handler);
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

    /**
     * Returns the preferred pixel size for a close zoom. For image rendering, the preferred pixel
     * size is the image's pixel size in logical units. For other kinds of rendering, this "pixel"
     * size should be some reasonable resolution. The default implementation computes a default
     * value from {@link #getGraphicsEnvelope2D}.
     */
    protected Dimension2D getPreferredPixelSize() {
        final Rectangle2D area = getGraphicsEnvelope2D();
        if (isValid(area)) {
            return new XDimension2D.Double(area.getWidth () / (10 * owner.getWidth ()),
                                           area.getHeight() / (10 * owner.getHeight()));
        }
        else {
            return new Dimension(1, 1);
        }
    }

    /**
     * Invoked when an affine transform that should be invertible is not.
     * Default implementation logs the stack trace and resets the zoom.
     *
     * @param methodName The caller's method name.
     * @param exception  The exception.
     */
    private void unexpectedException(final String methodName,
                                     final NoninvertibleTransformException exception) {
        objectiveToDisplay.setToIdentity();
        Logging.unexpectedException(AWTCanvas2D.class, methodName, exception);
    }


    //----------------------AWT Paint methods ----------------------------------
    public void paint(Graphics2D output){
        //correct the displayToDevice transform
        final AffineTransform normalize = output.getDeviceConfiguration().getNormalizingTransform();
        displayToDevice = new AffineTransform2D(normalize);

        
        Rectangle clipBounds = output.getClipBounds();

        AffineTransform2D objToDisp = null;

        //retrieve an affineTransform that will not be modify
        // while rendering
        try{
            objToDisp = setObjectiveToDisplayTransform(clipBounds);
        }catch(TransformException exception){
            exception.printStackTrace();
            GraphicsUtilities.paintStackTrace(output, owner.getBounds(), exception);
        }

        ((AWTDirectRenderer2D)renderer).paint( output, objToDisp );
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
//                if (bounds != null) {
//                    owner.repaint(bounds.x, bounds.y, bounds.width, bounds.height);
//                } else {
                    owner.repaint();
//                }
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

    public AWTCanvas2D getController() {
        return this;
    }

    /**
     * Reinitializes the affine transform {@link #zoom} in order to cancel any zoom, rotation or
     * translation.  The default implementation initializes the affine transform {@link #zoom} in
     * order to make the <var>y</var> axis point upwards and make the whole of the region covered
     * by the {@link #getPreferredArea} logical coordinates appear in the panel.
     * <p>
     * Note: for the derived classes: {@code reset()} is <u>the only</u> method of {@code ZoomPane}
     * which doesn't have to pass through {@link #transform(AffineTransform)} to modify the zoom.
     * This exception is necessary to avoid falling into an infinite loop.
     */
    public void reset() {
        reset(getGraphicsEnvelope2D(), getDisplayBounds().getBounds(), true);
    }

    /**
     * Reinitializes the affine transform {@link #zoom} in order to cancel any zoom, rotation or
     * translation. The argument {@code yAxisUpward} indicates whether the <var>y</var> axis should
     * point upwards.  The value {@code false} lets it point downwards. This method is offered
     * for convenience sake for derived classes which want to redefine {@link #reset()}.
     *
     * @param zoomableBounds Coordinates, in pixels, of the screen space in which to draw.
     *        This argument will usually be
     *        <code>{@link #getZoomableBounds(Rectangle) getZoomableBounds}(null)</code>.
     * @param yAxisUpward {@code true} if the <var>y</var> axis should point upwards rather than
     *        downwards.
     */
    protected final void reset(final Rectangle2D preferredArea, final Rectangle zoomableBounds, final boolean yAxisUpward) {
        if (!zoomableBounds.isEmpty()) {
            zoomableBounds.x = 0;
            zoomableBounds.y = 0;

            if (isValid(preferredArea)) {
                final AffineTransform change;
                try {
                    change = objectiveToDisplay.createInverse();
                } catch (NoninvertibleTransformException exception) {
                    unexpectedException("reset", exception);
                    return;
                }
                
                if (yAxisUpward) {
                    objectiveToDisplay.setToScale(+1, -1);
                }else {
                    objectiveToDisplay.setToIdentity();
                }
                
                final AffineTransform transform = setVisibleArea(preferredArea, zoomableBounds);
                change.concatenate(objectiveToDisplay);
                objectiveToDisplay.concatenate(transform);
                change.concatenate(transform);
//                getVisibleArea(zoomableBounds); // Force update of 'visibleArea'

                /*
                 * The three private versions 'fireZoomPane0', 'getVisibleArea'
                 * and 'setVisibleArea' avoid calling other methods of ZoomPane
                 * so as not to end up in an infinite loop.
                 */
                if (!change.isIdentity()) {
                    repaint();
                }

            }
        }
    }


    public Point2D getDisplayCenter(){
        Rectangle bounds = owner.getBounds();
        bounds.x = 0;
        bounds.y = 0;
        Point2D center = new Point2D.Double(bounds.getCenterX(), bounds.getCenterY());
        return center;
    }

    /**
     * Returns the center of the canvas in objective CRS.
     *
     * @return DirectPosition : center of the canvas
     */
    public DirectPosition getCenter(){
        Point2D center = getDisplayCenter();
//        System.out.println("center (pixels) => " + center);
//        System.out.println("objectiveToDisplay => " + objectiveToDisplay);
        try {
            center = objectiveToDisplay.inverseTransform(center, center);
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
            //TODO : propager l'exception
        }
//        System.out.println("center (objective) => " + center);
        return new GeneralDirectPosition(center);
    }

    public void setCenter(DirectPosition center) {
        DirectPosition oldCenter = getCenter();
        double diffX = center.getOrdinate(0) - oldCenter.getOrdinate(0);
        double diffY = center.getOrdinate(1) - oldCenter.getOrdinate(1);
        objectiveTranslate(diffX, diffY);
    }


    /**
     * Translate of x and y amount in display units.
     *
     * @param x : translation against the X axy
     * @param y : translation against the Y axy
     */
    public void displayTranslate(double x, double y){
        final AffineTransform change;
        try {
            change = objectiveToDisplay.createInverse();
        } catch (NoninvertibleTransformException exception) {
            unexpectedException("transform", exception);
            return;
        }

        change.translate(x,y);

        change.concatenate(objectiveToDisplay);
        XAffineTransform.round(change, EPS);
        transform(change);
    }

    public void objectiveTranslate(double x, double y){
        Point2D dispCenter = getDisplayCenter();
        DirectPosition center = getCenter();
        Point2D objCenter = new Point2D.Double(center.getOrdinate(0) + x, center.getOrdinate(1) + y);
        objCenter = objectiveToDisplay.transform(objCenter,objCenter);
        
        displayTranslate(dispCenter.getX() - objCenter.getX(), dispCenter.getY() - objCenter.getY());
    }




    public void setScale(double newScale){
        double oldScale = XAffineTransform.getScale(objectiveToDisplay);
        double diff = newScale/oldScale;
        scale(diff);
    }

    /**
     * Returns the current {@linkplain #zoom} scale factor. A value of 1/100 means that 100 metres
     * are displayed as 1 pixel (provided that the logical coordinates of {@link #getArea} are
     * expressed in metres). Scale factors for X and Y axes can be computed separately using the
     * following equations:
     *
     * <table cellspacing=3><tr>
     * <td width=50%><IMG src="doc-files/scaleX.png"></td>
     * <td width=50%><IMG src="doc-files/scaleY.png"></td>
     * </tr></table>
     *
     * This method combines scale along both axes, which is correct if this {@code ZoomPane} has
     * been constructed with the {@link #UNIFORM_SCALE} type.
     */
    public double getScale() {
        return XAffineTransform.getScale(objectiveToDisplay);
        
        //TODO : which one to keep
//        final double m00 = objectiveToDisplay.getScaleX();
//        final double m11 = objectiveToDisplay.getScaleY();
//        final double m01 = objectiveToDisplay.getShearX();
//        final double m10 = objectiveToDisplay.getShearY();
//        return Math.sqrt(m00 * m00 + m11 * m11 + m01 * m01 + m10 * m10);
    }

    /**
     * Change scale by a precise amount.
     *
     * @param s : multiplication scale factor
     */
    public void scale(double s){
        scale(s, getDisplayCenter());
    }

    public void scale(double s, Point2D center){
        final AffineTransform change;
        try {
            change = objectiveToDisplay.createInverse();
        } catch (NoninvertibleTransformException exception) {
            unexpectedException("transform", exception);
            return;
        }

        if (center != null) {
            final double centerX = center.getX();
            final double centerY = center.getY();

            change.translate(+centerX, +centerY);
            change.scale(s,s);
            change.translate(-centerX, -centerY);
        }

        change.concatenate(objectiveToDisplay);
        XAffineTransform.round(change, EPS);
        transform(change);
    }

    public void setRotation(double r){
        double rotation = getRotation();
        rotate(rotation-r);
    }

    public double getRotation(){
        return XAffineTransform.getRotation(objectiveToDisplay);
    }

    public void rotate(double r){
        rotate(r, getDisplayCenter());
    }

    public void rotate(double r, Point2D center){
        final AffineTransform change;
        try {
            change = objectiveToDisplay.createInverse();
        } catch (NoninvertibleTransformException exception) {
            unexpectedException("transform", exception);
            return;
        }

        if (center != null) {
            final double centerX = center.getX();
            final double centerY = center.getY();

            change.translate(+centerX, +centerY);
            change.rotate(r, centerX, centerY);
            change.translate(-centerX, -centerY);
        }

        change.concatenate(objectiveToDisplay);
        XAffineTransform.round(change, EPS);
        transform(change);
    }




    /**
     * Changes the {@linkplain #zoom} by applying an affine transform. The {@code change} transform
     * must express a change in logical units, for example, a translation in metres. This method is
     * conceptually similar to the following code:
     *
     * <pre>
     * {@link #zoom}.{@link AffineTransform#concatenate(AffineTransform) concatenate}(change);
     * {@link #fireZoomChanged(AffineTransform) fireZoomChanged}(change);
     * {@link #repaint() repaint}({@link #getDisplayBounds getZoomableBounds}(null));
     * </pre>
     *
     * @param  change The zoom change, as an affine transform in logical coordinates. If
     *         {@code change} is the identity transform, then this method does nothing and
     *         listeners are not notified.
     */
    public void transform(AffineTransform change){
        if (!change.isIdentity()) {
            objectiveToDisplay.concatenate(change);
            XAffineTransform.round(objectiveToDisplay, EPS);
            repaint();
        }
//        System.out.println("NEW AFFINE \n" + objectiveToDisplay);
    }

    /**
     * Changes the {@linkplain #zoom} by applying an affine transform. The {@code change} transform
     * must express a change in pixel units, for example, a scrolling of 6 pixels toward right. This
     * method is conceptually similar to the following code:
     *
     * <pre>
     * {@link #zoom}.{@link AffineTransform#preConcatenate(AffineTransform) preConcatenate}(change);
     * {@link #fireZoomChanged(AffineTransform) fireZoomChanged}(<cite>change translated in logical units</cite>);
     * {@link #repaint() repaint}({@link #getDisplayBounds getZoomableBounds}(null));
     * </pre>
     *
     * @param  change The zoom change, as an affine transform in pixel coordinates. If
     *         {@code change} is the identity transform, then this method does nothing
     *         and listeners are not notified.
     *
     * @since 2.1
     */
    public void transformPixels(final AffineTransform change) {
        if (!change.isIdentity()) {
            final AffineTransform logical;
            try {
                logical = objectiveToDisplay.createInverse();
            } catch (NoninvertibleTransformException exception) {
                throw new IllegalStateException(exception);
            }
            logical.concatenate(change);
            logical.concatenate(objectiveToDisplay);
            XAffineTransform.round(logical, EPS);
            transform(logical);
        }
    }

    public void setVisibleArea(ReferencedEnvelope env){        
        Rectangle2D rect2D = new Rectangle2D.Double(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());
        reset(rect2D, getDisplayBounds().getBounds(), true);
    }

    /**
     * Defines the limits of the visible part, in logical coordinates.  This method will modify the
     * zoom and the translation in order to display the specified region. If {@link #zoom} contains
     * a rotation, this rotation will not be modified.
     *
     * @param  logicalBounds Logical coordinates of the region to be displayed.
     * @throws IllegalArgumentException if {@code source} is empty.
     */
    public void setVisibleArea(final Rectangle2D logicalBounds) throws IllegalArgumentException {
//        log("setVisibleArea", logicalBounds);
        transform(setVisibleArea(logicalBounds, getDisplayBounds().getBounds()));
    }

    /**
     * Defines the limits of the visible part, in logical coordinates.  This method will modify the
     * zoom and the translation in order to display the specified region. If {@link #zoom} contains
     * a rotation, this rotation will not be modified.
     *
     * @param  source Logical coordinates of the region to be displayed.
     * @param  dest Pixel coordinates of the region of the window in which to
     *         draw (normally {@link #getDisplayBounds()}).
     * @param  mask A mask to {@code OR} with the {@link #type} for determining which
     *         kind of transformation are allowed. The {@link #type} is not modified.
     * @return Change to apply to the affine transform {@link #zoom}.
     * @throws IllegalArgumentException if {@code source} is empty.
     */
    private AffineTransform setVisibleArea(Rectangle2D source, Rectangle2D dest)
                                           throws IllegalArgumentException
    {
        /*
         * Verifies the validity of the source rectangle. An invalid rectangle will be rejected.
         * However, we will be more flexible for dest since the window could have been reduced by
         * the user.
         */
        if (!isValid(source)) {
            throw new IllegalArgumentException(Errors.format(ErrorKeys.BAD_RECTANGLE_$1, source));
        }
        if (!isValid(dest)) {
            return new AffineTransform();
        }
        /*
         * Converts the destination into logical coordinates.  We can then perform
         * a zoom and a translation which would put {@code source} in {@code dest}.
         */
        try {
            dest = XAffineTransform.inverseTransform(objectiveToDisplay, dest, null);
        } catch (NoninvertibleTransformException exception) {
            unexpectedException("setVisibleArea", exception);
            return new AffineTransform();
        }
        final double sourceWidth  = source.getWidth ();
        final double sourceHeight = source.getHeight();
        final double   destWidth  =   dest.getWidth ();
        final double   destHeight =   dest.getHeight();
              double           sx = destWidth / sourceWidth;
              double           sy = destHeight / sourceHeight;

              /*
         * Standardizes the horizontal and vertical scales,
         * if such a standardization has been requested.
         */
        if (sy * sourceWidth < destWidth) {
            sx = sy;
        } else if (sx * sourceHeight < destHeight) {
            sy = sx;
        }

        final AffineTransform change = AffineTransform.getTranslateInstance(dest.getCenterX(),dest.getCenterY());
        change.scale(sx,sy);
        change.translate(-source.getCenterX(), -source.getCenterY());
        XAffineTransform.round(change, EPS);
        return change;
    }

    /**
     *
     * @return the live interim objective To display transform.
     */
    public AffineTransform2D getTransform(){
        return objectiveToDisplay;
    }

}
