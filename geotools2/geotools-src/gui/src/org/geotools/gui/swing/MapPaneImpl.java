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
package org.geotools.gui.swing;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.ct.Adapters;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransformFactory;
import org.geotools.data.DataSourceException;
import org.geotools.gui.swing.event.GeoMouseEvent;
import org.geotools.gui.tools.Tool;
import org.geotools.gui.tools.ToolFactory;
import org.geotools.gui.tools.ToolList;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.map.Layer;
import org.geotools.map.LayerList;
import org.geotools.map.events.BoundingBoxEvent;
import org.geotools.map.events.BoundingBoxListener;
import org.geotools.map.events.LayerListListener;
import org.geotools.gui.tools.event.SelectedToolListener;
import org.geotools.renderer.lite.LiteRenderer;
import org.geotools.styling.Style;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.lang.IllegalArgumentException;
import java.util.EventObject;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.geotools.renderer.Renderer2D;


/**
 * This widget is responsible for marshalling the rendering of a map. It
 * processes Listener events and calls the Renderer as required. It maintains
 * the correct aspect ratio by resizing the Context's BoundingBox when this
 * component changes size.
 *
 * @author Cameron Shorter
 * @version $Id: MapPaneImpl.java,v 1.30 2003/07/22 06:25:50 aaime Exp $
 *
 * @task REVISIT: We need to add a PixcelAspectRatio varible which defaults to
 *       1, ie width/heigh=1.  Currently, this is assumed to be 1.
 */
public class MapPaneImpl extends JPanel implements BoundingBoxListener,
    LayerListListener, ComponentListener, SelectedToolListener {
    /** The class used for identifying for logging. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.swing.MapPaneImpl");

    /** The class to use to render this MapPane. */
    Renderer2D renderer;

    /** The model which stores a list of layers and BoundingBox. */
    private Context context;

    /** List of tools which can be used by this class. */
    private ToolList toolList;

    /* Used to convert between types. */
    private Adapters adapters = Adapters.getDefault();

    /** A transform from screen coordinates to real world coordinates. */
    private AffineTransform dotToCoordinateTransform = new AffineTransform();

    /**
     * Create a MapPane. A MapPane marshals the drawing of maps.
     *
     * @param context The context where layerList and boundingBox are kept.  If
     *        context is null, an IllegalArguementException is thrown.
     * @param toolList The list of tools which can be used by this class.
     *
     * @throws IllegalArgumentException when parameters are null.
     */
    public MapPaneImpl(Context context, ToolList toolList)
        throws IllegalArgumentException {
        if ((context == null) || (toolList == null)) {
            throw new IllegalArgumentException();
        } else {
            this.toolList = toolList;
            this.renderer = new LiteRenderer(context);
            // this.renderer = new org.geotools.renderer.Java2DRenderer(context);
            this.context = context;

            // Request to be notified when map parameters change
            context.getBbox().addAreaOfInterestChangedListener(this);
            context.getLayerList().addLayerListChangedListener(this);
            toolList.addSelectedToolListener(this);
            toolList.getSelectedTool().addMouseListener(this, context);
            addComponentListener(this);

            // A zero sized mapPane cannot be resized later and doesn't behave
            // very nicely
            this.setMinimumSize(new Dimension(2, 2));

            // use absolute positioning
            this.setLayout(null);

            // set up the varialbes associated with this tool.
            initialiseTool();
        }
    }

    /**
     * Create a MapPane. A MapPane marshals the drawing of maps.
     *
     * @param context The context where layerList and boundingBox are kept.  If
     *        context is null, an IllegalArguementException is thrown.
     *
     * @throws IllegalArgumentException when parameters are null.
     */
    public MapPaneImpl(Context context) throws IllegalArgumentException {
        this(context, ToolFactory.createFactory().createDefaultToolList());
    }

    /**
     * Loop through all the layers in this mapPane's layerList and render each
     * Layer which is set to Visable.  If the AreaOfInterest is null, then the
     * AreaOfInterest will be set to the AreaOfInterest of the the LayerList.
     * Rendering will not occur now, but will wait until after the
     * AreaOfInterestChangedEvent has been received.
     *
     * @param graphics The graphics object to paint to.
     *
     * @task TODO fill in exception.  This should implement logging.
     * @task REVISIT Need to create an interface
     *       features=dataSource.getFeatures(extent)
     * @task REVISIT We should set the AreaOfInterest somewhere other than
     *       here.
     * @task REVISIT Need to change getBbox(false) to getBbox(true) to speed
     *       things up.  ch - that method no longer exists, changed to no 
     *       argument.
     * @task TODO create a layerList.getCoordinateSystem method
     */
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        if (context.getBbox().getAreaOfInterest() == null) {
            Envelope bBox = context.getLayerList().getBbox();

            if (bBox != null) {
                LOGGER.info("AreaOfInterest calculated during rendering");
                context.getBbox().setAreaOfInterest(context.getLayerList()
                                                           .getBbox(), null);
            }
        }

        int w = getWidth() - getInsets().left - getInsets().right;
        int h = getHeight() - getInsets().top - getInsets().bottom;

        // prevent divide by zero errors
        if (h == 0) {
            h = 2;
        }

        try {
            System.out.println(graphics.getClipBounds());
            // paint only what's needed
            renderer.paint((Graphics2D) graphics,  
                graphics.getClipBounds(),
                dotToCoordinateTransform.createInverse());
        } catch (java.awt.geom.NoninvertibleTransformException e) {
            LOGGER.warning("Transform error while rendering. Cause is: " +
                e.getCause());
        }
    }

    /**
     * Process an AreaOfInterestChangedEvent, involves a redraw.
     *
     * @param boundingBoxEvent The new extent.
     */
    public void areaOfInterestChanged(BoundingBoxEvent boundingBoxEvent) {
        updateTransform();
        repaint(getVisibleRect());
    }

    /**
     * Process an LayerListChangedEvent, involves a redraw.
     *
     * @param layerListChangedEvent The new extent.
     */
    public void layerListChanged(EventObject layerListChangedEvent) {
        repaint(getVisibleRect());
    }

    /**
     * Processes mouse events occurring on this component. This method
     * overrides the default AWT's implementation in order to wrap the
     * <code>MouseEvent</code> into a {@link GeoMouseEvent}. Then, the default
     * AWT's implementation is invoked in  order to pass this event to any
     * registered {@link MouseListener} objects.
     *
     * @param event The click point.
     */
    protected void processMouseEvent(final MouseEvent event) {
        super.processMouseEvent(new GeoMouseEvent(event,
                MathTransformFactory.getDefault().createAffineTransform(dotToCoordinateTransform)));
    }

    /**
     * Processes mouse motion events occurring on this component. This method
     * overrides the default AWT's implementation in order to wrap the
     * <code>MouseEvent</code> into a {@link GeoMouseEvent}. Then, the default
     * AWT's implementation is invoked in order to pass this event to any
     * registered {@link MouseMotionListener} objects.
     *
     * @param event The mouse point.
     */
    protected void processMouseMotionEvent(final MouseEvent event) {
        super.processMouseMotionEvent(new GeoMouseEvent(event,
                MathTransformFactory.getDefault().createAffineTransform(dotToCoordinateTransform)));
    }

    /**
     * Invoked when the component has been made invisible.
     *
     * @param e ComponentEvent.
     */
    public void componentHidden(ComponentEvent e) {
    }

    /**
     * Invoked when the component's position changes.
     *
     * @param e ComponentEvent.
     */
    public void componentMoved(ComponentEvent e) {
    }

    /**
     * Invoked when the component's size changes, change the AreaOfInterest so
     * that the aspect ratio remains the same.  One axis will remain the same
     * width/height while the other axis will expand to fit the new aspect ratio.<br>
     * The method will trigger an AreaOfInterestEvent which in turn will cause
     * a repaint.
     *
     * @param e ComponentEvent.
     *
     * @throws java.lang.reflect.UndeclaredThrowableException DOCUMENT ME!
     */
    public void componentResized(ComponentEvent e) {
        int w = getWidth() - getInsets().left - getInsets().right;
        int h = getHeight() - getInsets().top - getInsets().bottom;
        double newAspectRatio = (double) w / (double) h;

        AffineTransform at = new AffineTransform();
        Envelope aoi = context.getBbox().getAreaOfInterest();

        double contextAspectRatio = (double) (aoi.getMaxX() - aoi.getMinX()) / (double) (aoi.getMaxY() -
            aoi.getMinY());

        // No need to resize if the aspect ratio is correct.
        if (contextAspectRatio == newAspectRatio) {
            return;
        }

        if (newAspectRatio > contextAspectRatio) {
            // Increase the width to fit new aspect ratio
            at.translate((aoi.getMinX() + aoi.getMaxX()) / 2, 0);
            at.scale(newAspectRatio / contextAspectRatio, 1);
            at.translate(-(aoi.getMinX() + aoi.getMaxX()) / 2, 0);
        } else {
            // Increase the height to fit new aspect ratio
            at.translate(0, (aoi.getMinY() + aoi.getMaxY()) / 2);
            at.scale(1, contextAspectRatio / newAspectRatio);
            at.translate(0, -(aoi.getMinY() + aoi.getMaxY()) / 2);
        }

        MathTransform transform = MathTransformFactory.getDefault()
                                                      .createAffineTransform(at);

        try {
            context.getBbox().transform(adapters.export(transform));
        } catch (java.rmi.RemoteException exception) {
            // TODO: We should not hide a checked exception that way.
            throw new java.lang.reflect.UndeclaredThrowableException(exception,
                "Remote call failed");
        }
    }

    /**
     * Invoked when the component has been made visible.
     *
     * @param e ComponentEvent.
     */
    public void componentShown(ComponentEvent e) {
    }

    /**
     * Called when the selectedTool on a MapPane changes. Register for
     * mouseEvents on behalf of the tool, and set the Cursor.
     *
     * @param event ComponenetEvent.
     */
    public void selectedToolChanged(EventObject event) {
        initialiseTool();
    }

    /**
     * Initialise variables associated with the tool.
     */
    private void initialiseTool() {
        if (toolList.getSelectedTool() != null) {
            toolList.getSelectedTool().addMouseListener(this, context);
            setCursor(toolList.getSelectedTool().getCursor());
        } else {
            setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }

    /**
     * Re-evaluate the screen to CoordinateSystem transform, this method should
     * be called whenever the MapPane resizes, boundingBox resizes, or
     * coordinateSystem changes.
     */
    public void updateTransform() {
        //Real World Coordinates
        Envelope aoi = context.getBbox().getAreaOfInterest();

        // Scaling
        double scaleX = (aoi.getMaxX() - aoi.getMinX()) / (getWidth() -
            getInsets().left - getInsets().right);
        double scaleY = (aoi.getMaxY() - aoi.getMinY()) / (getHeight() -
            getInsets().top - getInsets().bottom);

        // x'=(x-leftBorder)*scaleX+csMinX
        //   = x*scaleX -leftBorder*scaleX+csMinX
        // y'=(maxY-bottomBorder-y)*scaleY+csMinY
        //   =-y*scaleY+(maxY-bottomBorder)*scaleY+csMinY
        //
        // This equates to an AffineTransform:
        //
        // [ x']   [  m00  m01  m02  ] [ x ]   [ m00x + m01y + m02 ]
        // [ y'] = [  m10  m11  m12  ] [ y ] = [ m10x + m11y + m12 ]
        // [ 1 ]   [   0    0    1   ] [ 1 ]   [         1         ]
        //
        // [x'] [scaleX  0       -leftBorder*scaleX+csMinX         ][x]
        // [y']=[0       -scaleY (maxY-bottomBorder)*scaleY+csMinY][y]
        // [1 ] [0       0       1                                ][1]
        dotToCoordinateTransform = new AffineTransform(
            // m00: ScaleX
            scaleX, 
            // m10
            0.0, 
            // m01:
            0.0, -scaleY, 
            // m02: TransformX
            aoi.getMinX() - (scaleX * getInsets().left),
                (
            // m12: TransformY
            (getHeight() - getInsets().bottom) * scaleY) + aoi.getMinY());
    }

    /**
     * Set the ToolList for this class.
     *
     * @param toolList The list of tools than can be used by this class.
     */
    public void setToolList(ToolList toolList) {
        this.toolList = toolList;
    }

    /**
     * Get the ToolList for this class.
     *
     * @return the toolList for this class.
     */
    public ToolList getToolList() {
        return this.toolList;
    }
}
