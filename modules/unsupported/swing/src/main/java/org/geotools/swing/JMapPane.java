/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.MouseInputAdapter;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.event.MapBoundsEvent;
import org.geotools.swing.event.MapMouseListener;
import org.geotools.swing.event.MapPaneEvent;
import org.geotools.swing.event.MapPaneListener;
import org.geotools.swing.tool.CursorTool;
import org.geotools.swing.tool.MapToolManager;
import org.geotools.map.MapContext;
import org.geotools.map.MapLayer;
import org.geotools.map.event.MapBoundsListener;
import org.geotools.map.event.MapLayerEvent;
import org.geotools.map.event.MapLayerListEvent;
import org.geotools.map.event.MapLayerListListener;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.label.LabelCacheImpl;
import org.geotools.renderer.lite.LabelCache;
import org.geotools.renderer.lite.StreamingRenderer;
import org.opengis.geometry.Envelope;

/**
 * A simple map display container that works with a GTRenderer and
 * MapContext to display features. Supports the use of tool classes
 * to implement, for example, mouse-controlled zooming and panning.
 * 
 * Based on original code by Ian Turton. This version does not yet
 * support selection and highlighting of features.
 * 
 * @author Michael Bedward
 * @author Ian Turton
 * @since 2.6
 * @source $URL$
 * @version $Id$
 */
public class JMapPane extends JPanel implements MapLayerListListener, MapBoundsListener {

    private static final ResourceBundle stringRes = ResourceBundle.getBundle("org/geotools/swing/widget");

    /**
     * Default delay (milliseconds) before the map will be redrawn when resizing
     * the pane. This is to avoid flickering while drag-resizing.
     */
    public static final int DEFAULT_RESIZING_PAINT_DELAY = 500;  // delay in milliseconds

    private Timer resizeTimer;
    private int resizingPaintDelay;
    private boolean acceptRepaintRequests;

    /**
     * If the user sets the display area before the pane is shown on
     * screen we store the requested envelope with this field and refer
     * to it when the pane is shown.
     */
    private ReferencedEnvelope pendingDisplayArea;

    /**
     * This field is used to cache the full extent of the combined map
     * layers.
     */
    private ReferencedEnvelope fullExtent;

    /**
     * Encapsulates XOR box drawing logic used with mouse dragging
     */
    private class DragBox extends MouseInputAdapter {

        private Point startPos;
        private Rectangle rect;
        private boolean dragged;
        private boolean enabled;

        DragBox() {
            rect = new Rectangle();
            dragged = false;
            enabled = false;
        }

        void setEnabled(boolean state) {
            enabled = state;
        }

        @Override
        public void mousePressed(MouseEvent e) {
            startPos = new Point(e.getPoint());
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (enabled) {
                Graphics2D g2D = (Graphics2D) JMapPane.this.getGraphics();
                g2D.setColor(Color.WHITE);
                g2D.setXORMode(Color.RED);
                if (dragged) {
                    g2D.drawRect(rect.x, rect.y, rect.width, rect.height);
                }

                rect.setFrameFromDiagonal(startPos, e.getPoint());
                g2D.drawRect(rect.x, rect.y, rect.width, rect.height);

                dragged = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (dragged) {
                Graphics2D g2D = (Graphics2D) JMapPane.this.getGraphics();
                g2D.setColor(Color.WHITE);
                g2D.setXORMode(Color.RED);
                g2D.drawRect(rect.x, rect.y, rect.width, rect.height);
                dragged = false;
            }
        }
    }
    private DragBox dragBox;
    private MapContext context;
    private GTRenderer renderer;
    private LabelCache labelCache;
    private MapToolManager toolManager;
    private MapLayerTable layerTable;
    private Set<MapPaneListener> listeners = new HashSet<MapPaneListener>();
    private AffineTransform worldToScreen;
    private AffineTransform screenToWorld;
    private Rectangle curPaintArea;
    private BufferedImage baseImage;
    private Point imageOrigin;
    private boolean redrawBaseImage;
    private boolean needNewBaseImage;
    private boolean baseImageMoved;

    /** 
     * Constructor - creates an instance of JMapPane with no map 
     * context or renderer initially
     */
    public JMapPane() {
        this(null, null);
    }

    /**
     * Constructor - creates an instance of JMapPane with the given
     * renderer and map context.
     * 
     * @param renderer a renderer object
     * @param context an instance of MapContext
     */
    public JMapPane(GTRenderer renderer, MapContext context) {
        imageOrigin = new Point(0, 0);

        acceptRepaintRequests = true;
        needNewBaseImage = true;
        redrawBaseImage = true;
        baseImageMoved = false;

        /*
         * We use a Timer object to avoid rendering delays and
         * flickering when the user is drag-resizing the parent
         * container of this map pane.
         *
         * Using a ComponentListener doesn't work because, unlike
         * a JFrame, the pane receives a stream of events during
         * drag-resizing.
         */
        resizingPaintDelay = DEFAULT_RESIZING_PAINT_DELAY;
        resizeTimer = new Timer(resizingPaintDelay, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                onResizingCompleted();
            }
        });
        resizeTimer.setRepeats(false);

        setRenderer(renderer);
        setMapContext(context);

        toolManager = new MapToolManager(this);

        dragBox = new DragBox();
        this.addMouseListener(dragBox);
        this.addMouseMotionListener(dragBox);

        this.addMouseListener(toolManager);
        this.addMouseMotionListener(toolManager);
        this.addMouseWheelListener(toolManager);

        /*
         * Listen for mouse entered events to (re-)set the
         * current tool cursor, otherwise the cursor seems to
         * default to the standard cursor sometimes (at least
         * on OSX)
         */
        this.addMouseListener(new MouseInputAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                super.mouseEntered(e);
                CursorTool tool = toolManager.getCursorTool();
                if (tool != null) {
                    JMapPane.this.setCursor(tool.getCursor());
                }
            }
        });

        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent ev) {
                acceptRepaintRequests = false;
                resizeTimer.restart();
            }
        });

    }

    /**
     * Repaint the map when resizing has finished. This method will
     * also be called if the user pauses drag-resizing for a period
     * longer than resizingPaintDelay milliseconds, and when the
     * map pane is first displayed.
     */
    private void onResizingCompleted() {
        acceptRepaintRequests = true;
        needNewBaseImage = true;

        curPaintArea = getVisibleRect();

        // allow a single pixel margin at the right and bottom edges
        curPaintArea.width -= 1;
        curPaintArea.height -= 1;

        if (context != null && context.getLayerCount() > 0) {
            if (fullExtent == null) {
                setFullExtent();
            }

            if (pendingDisplayArea != null) {
                doSetDisplayArea(pendingDisplayArea);
                pendingDisplayArea = null;

            } else {
                doSetDisplayArea(context.getAreaOfInterest());
            }

            repaint();
        
            MapPaneEvent ev = new MapPaneEvent(this, MapPaneEvent.Type.PANE_RESIZED);
            publishEvent(ev);
        }
    }

    /**
     * Set the current cursor tool
     * 
     * @param tool the tool to set; null means no active cursor tool
     */
    public void setCursorTool(CursorTool tool) {
        if (tool == null) {
            toolManager.setNoCursorTool();
            this.setCursor(Cursor.getDefaultCursor());
            dragBox.setEnabled(false);

        } else {
            this.setCursor(tool.getCursor());
            toolManager.setCursorTool(tool);
            dragBox.setEnabled(tool.drawDragBox());
        }
    }

    /**
     * Register an object that wishes to receive MapMouseEvents
     * such as a {@linkplain org.geotools.swing.StatusBar}
     *
     * @throws IllegalArgumentException if listener is null
     */
    public void addMouseListener(MapMouseListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(stringRes.getString("arg_null_error"));
        }

        toolManager.addMouseListener(listener);
    }

    /**
     * Unregister the specified MapMouseListener object.
     *
     * @throws IllegalArgumentException if listener is null
     */
    public void removeMouseListener(MapMouseListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(stringRes.getString("arg_null_error"));
        }

        toolManager.removeMouseListener(listener);
    }

    public void addMapPaneListener(MapPaneListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException(stringRes.getString("arg_null_error"));
        }

        listeners.add(listener);
    }

    /**
     * Register a {@linkplain MapLayerTable} object to be receive
     * layer change events from this map pane and to control layer
     * ordering, visibility and selection.
     *
     * @param layerTable an instance of MapLayerTable
     *
     * @throws IllegalArgumentException if layerTable is null
     */
    public void setMapLayerTable(MapLayerTable layerTable) {
        if (layerTable == null) {
            throw new IllegalArgumentException(stringRes.getString("arg_null_error"));
        }

        this.layerTable = layerTable;
    }

    /**
     * Get the current renderer used by this map pane
     */
    public GTRenderer getRenderer() {
        return renderer;
    }

    /**
     * Set the renderer for this map pane. If faster raster rendering was previously
     * requested with the {@linkplain #setRasterRendering} method, this will be set
     * for the new renderer.
     */
    public void setRenderer(GTRenderer renderer) {
        Map<Object, Object> hints;
        if (renderer instanceof StreamingRenderer) {
            hints = renderer.getRendererHints();
            if (hints == null) {
                hints = new HashMap<Object, Object>();
            }
            if (hints.containsKey(StreamingRenderer.LABEL_CACHE_KEY)) {
                labelCache = (LabelCache) hints.get(StreamingRenderer.LABEL_CACHE_KEY);
            } else {
                labelCache = new LabelCacheImpl();
                hints.put(StreamingRenderer.LABEL_CACHE_KEY, labelCache);
            }
            renderer.setRendererHints(hints);
        }

        this.renderer = renderer;

        if (this.context != null) {
            this.renderer.setContext(this.context);
        }
    }

    /**
     * Get the map context associated with this map pane
     */
    public MapContext getMapContext() {
        return context;
    }

    /**
     * Set the map context for this map pane
     */
    public void setMapContext(MapContext context) {
        if (this.context != context) {
            
            if (this.context != null) {
                this.context.removeMapLayerListListener(this);
                for( MapLayer layer : this.context.getLayers() ){
                    if( layer instanceof ComponentListener){
                        removeComponentListener( (ComponentListener) layer );
                    }
                }
            }

            this.context = context;

            if (context != null) {
                this.context.addMapLayerListListener(this);
                this.context.addMapBoundsListener(this);

                // set all layers as selected by default for the info tool
                for (MapLayer layer : context.getLayers()) {
                    layer.setSelected(true);
                    if( layer instanceof ComponentListener){
                        addComponentListener( (ComponentListener) layer );
                    }                    
                }

                setFullExtent();
            }

            if (renderer != null) {
                renderer.setContext(this.context);
            }

            MapPaneEvent ev = new MapPaneEvent(this, MapPaneEvent.Type.NEW_CONTEXT);
            publishEvent(ev);
        }
    }

    /**
     * Return a (copy of) the currently displayed map area.
     * <p>
     * Note, this will not always be the same as the envelope returned by
     * {@code MapContext.getAreaOfInterest()}. For example, when the
     * map is displayed at the full extent of all layers
     * {@code MapContext.getAreaOfInterest()} will return the union of the
     * layer bounds while this method will return an evnelope that can
     * included extra space beyond the bounds of the layers.
     */
    public ReferencedEnvelope getDisplayArea() {
        ReferencedEnvelope aoi = null;

        if (curPaintArea != null && screenToWorld != null) {
            Point2D p0 = new Point2D.Double(curPaintArea.getMinX(), curPaintArea.getMinY());
            Point2D p1 = new Point2D.Double(curPaintArea.getMaxX(), curPaintArea.getMaxY());
            screenToWorld.transform(p0, p0);
            screenToWorld.transform(p1, p1);

            aoi = new ReferencedEnvelope(
                    Math.min(p0.getX(), p1.getX()),
                    Math.max(p0.getX(), p1.getX()),
                    Math.min(p0.getY(), p1.getY()),
                    Math.max(p0.getY(), p1.getY()),
                    context.getCoordinateReferenceSystem());
        }

        return aoi;
    }

    /**
     * Sets the area to display by calling the {@linkplain MapContext#setAreaOfInterest}
     * method of this pane's map context. Does nothing if the MapContext has not been set.
     * If neither the context or the envelope have coordinate reference systems defined
     * this method does nothing.
     * <p>
     * The map area that ends up being displayed will often be larger than the requested
     * display area. For instance, if the square area is requested, but the map pane's
     * screen area is a rectangle with width greater than height, then the displayed area
     * will be centred on the requested square but include additional area on each side.
     * <p>
     * You can pass any GeoAPI Envelope implementation to this method such as
     * ReferenedEnvelope or Envelope2D.
     * <p>
     * Note: This method does <b>not</b> check that the requested area overlaps
     * the bounds of the current map layers.
     * 
     * @param envelope the bounds of the map to display
     *
     * @throws IllegalStateException if a map context is not set
     */
    public void setDisplayArea(Envelope envelope) {
        if (context != null) {
            /*
             * If the pane has not been displayed yet or is zero size then 
             * just record the requested display area and defer setting transforms
             * etc.
             */
            if (curPaintArea == null || curPaintArea.isEmpty()) {
                pendingDisplayArea = new ReferencedEnvelope(envelope);
            } else {
                doSetDisplayArea(envelope);
                labelCache.clear();
                repaint();
            }
            
        } else {
            throw new IllegalStateException("Map context must be set before setting the display area");
        }
    }

    /**
     * Helper method for {@linkplain #setDisplayArea} which is also called by
     * other methods that want to set the display area without provoking
     * repainting of the display
     *
     * @param envelope requested display area
     */
    private void doSetDisplayArea(Envelope envelope) {
        assert (context != null && curPaintArea != null && !curPaintArea.isEmpty());

        if (equalsFullExtent(envelope)) {
            setTransforms(fullExtent, curPaintArea);
        } else {
            setTransforms(envelope, curPaintArea);
        }
        ReferencedEnvelope adjustedEnvelope = getDisplayArea();
        context.setAreaOfInterest(adjustedEnvelope);

        MapPaneEvent ev = new MapPaneEvent(this, MapPaneEvent.Type.DISPLAY_AREA_CHANGED);
        publishEvent(ev);
    }


    /**
     * Check if the envelope corresponds to full extent. It will probably not
     * equal the full extent envelope because of slack space in the display
     * area, so we check that at least one pair of opposite edges are
     * equal to the full extent envelope, allowing for slack space on the
     * other two sides.
     * <p>
     * Note: this method returns {@code false} if the full extent envelope
     * is wholly within the requested envelope (e.g. user has zoomed out
     * from full extent), only touches one edge, or touches two adjacent edges.
     * In all these cases we assume that the user wants to maintain the slack
     * space in the display.
     * <p>
     * This method is part of the work-around that the map pane needs because
     * of the differences in how raster and vector layers are treated by the
     * renderer classes.
     *
     * @param envelope a pending display envelope to compare to the full extent
     *        envelope
     *
     * @return true if the envelope is coincident with the full extent evenlope
     *         on at least two edges; false otherwise
     *
     * @todo My logic here seems overly complex - I'm sure there must be a simpler
     *       way for the map pane to handle this.
     */
    private boolean equalsFullExtent(final Envelope envelope) {
        assert(fullExtent != null);

        final double TOL = 1.0e-6d * (fullExtent.getWidth() + fullExtent.getHeight());

        boolean touch = false;
        if (Math.abs(envelope.getMinimum(0) - fullExtent.getMinimum(0)) < TOL) {
            touch = true;
        }
        if (Math.abs(envelope.getMaximum(0) - fullExtent.getMaximum(0)) < TOL) {
            if (touch) {
                return true;
            }
        }
        if (Math.abs(envelope.getMinimum(1) - fullExtent.getMinimum(1)) < TOL) {
            touch = true;
        }
        if (Math.abs(envelope.getMaximum(1) - fullExtent.getMaximum(1)) < TOL) {
            if (touch) {
                return true;
            }
        }

        return false;
    }

    /**
     * Reset the map area to include the full extent of all
     * layers and redraw the display
     */
    public void reset() {
        if (fullExtent != null) {
            setDisplayArea(fullExtent);
        }
    }

    /**
     * Specify whether the map pane should defer its normal
     * repainting behaviour. 
     * <p>
     * Typical use:
     * <pre>{@code
     * myMapPane.setRepaint(false);
     *
     * // do various things that would cause time-consuming
     * // re-paints normally
     *
     * myMapPane.setRepaint(true);
     * myMapPane.repaint();
     * }</pre>
     *
     * @param repaint if true, paint requests will be handled normally;
     * if false, paint requests will be deferred.
     */
    void setRepaint(boolean repaint) {
        acceptRepaintRequests = repaint;

        // we also want to accept / ignore system requests for repainting
        setIgnoreRepaint(!repaint);
    }

    /**
     * Get a (copy of) the screen to world coordinate transform
     * being used by this map pane.
     */
    public AffineTransform getScreenToWorldTransform() {
        if (screenToWorld != null) {
            return new AffineTransform(screenToWorld);
        } else {
            return null;
        }
    }

    /**
     * Get a (copy of) the world to screen coordinate transform
     * being used by this map pane. This method can be 
     * used to determine the current drawing scale...
     * <pre>{@code
     * double scale = mapPane.getWorldToScreenTransform().getScaleX();
     * }</pre>
     */
    public AffineTransform getWorldToScreenTransform() {
        if (worldToScreen != null) {
            return new AffineTransform(worldToScreen);
        } else {
            return null;
        }
    }

    /**
     * Move the image currently displayed by the map pane from
     * its current origin (x,y) to (x+dx, y+dy). This method
     * allows dragging the map without the overhead of redrawing
     * the features during the drag. For example, it is used by
     * {@link org.geotools.swing.tool.PanTool}.
     * 
     * @param dx the x offset in pixels
     * @param dy the y offset in pixels.
     */
    public void moveImage(int dx, int dy) {
        imageOrigin.translate(dx, dy);
        redrawBaseImage = false;
        baseImageMoved = true;
        repaint();
    }

    /**
     * Called by the system to draw the layers currently visible layers.
     * Client code should not use this method directly; instead call
     * repaint().
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (acceptRepaintRequests) {

            if (context == null || renderer == null) {
                return;
            }

            if (needNewBaseImage) {
                baseImage = new BufferedImage(curPaintArea.width + 1, curPaintArea.height + 1, BufferedImage.TYPE_INT_ARGB);
                needNewBaseImage = false;
                redrawBaseImage = true;
                labelCache.clear();
            }

            ReferencedEnvelope mapAOI = context.getAreaOfInterest();
            if (mapAOI == null) {
                return;
            }

            if (redrawBaseImage) {
                if (baseImageMoved) {
                    afterImageMove(mapAOI, curPaintArea);
                    baseImageMoved = false;
                    labelCache.clear();
                }
                clearBaseImage();
                Graphics2D baseGr = baseImage.createGraphics();
                renderer.paint(baseGr, curPaintArea, mapAOI, worldToScreen);
            }

            ((Graphics2D) g).drawImage(baseImage, imageOrigin.x, imageOrigin.y, this);
            redrawBaseImage = true;
        }
    }

    /**
     * Called after the base image has been dragged. Sets the new map area and
     * transforms
     * @param mapAOI pre-move map area
     * @param paintArea drawing area
     */
    protected void afterImageMove(ReferencedEnvelope env, Rectangle paintArea) {
        int dx = imageOrigin.x;
        int dy = imageOrigin.y;
        DirectPosition2D newPos = new DirectPosition2D(dx, dy);
        screenToWorld.transform(newPos, newPos);

        env.translate(env.getMinimum(0) - newPos.x, env.getMaximum(1) - newPos.y);
        doSetDisplayArea(env);
        imageOrigin.setLocation(0, 0);
    }

    /**
     * Called when a new map layer has been added. Sets the layer
     * as selected (for queries) and, if the layer table is being
     * used, adds the new layer to the table.
     */
    public void layerAdded(MapLayerListEvent event) {
        if (layerTable != null) {
            layerTable.addLayer(event.getLayer());
        }
        MapLayer layer = event.getLayer();
        layer.setSelected(true);

        if( layer instanceof ComponentListener ){
            addComponentListener( (ComponentListener) layer );
        }
        
        if (context.getLayerCount() == 1) {
            setFullExtent();
            reset();
        }

        repaint();
    }

    /**
     * Called when a map layer has been removed
     */
    public void layerRemoved(MapLayerListEvent event) {
        MapLayer layer = event.getLayer();
        if (layerTable != null) {
            layerTable.removeLayer(layer);
        }

        if( layer instanceof ComponentListener ){
            addComponentListener( (ComponentListener) layer );
        }

        setFullExtent();
        repaint();
    }

    /**
     * Called when a map layer has changed, e.g. features added
     * to a displayed feature collection
     */
    public void layerChanged(MapLayerListEvent event) {
        if (layerTable != null) {
            layerTable.repaint(event.getLayer());
        }

        int reason = event.getMapLayerEvent().getReason();

        if (reason == MapLayerEvent.DATA_CHANGED) {
            setFullExtent();
        }

        if (reason != MapLayerEvent.SELECTION_CHANGED) {
            repaint();
        }
    }

    /**
     * Called when the bounds of a map layer have changed
     */
    public void layerMoved(MapLayerListEvent event) {
        repaint();
    }

    /**
     * Called by the map context when its bounds have changed. Used
     * here to watch for a changed CRS, in which case the map is
     * redisplayed at (new) full extent.
     */
    public void mapBoundsChanged(MapBoundsEvent event) {

        int type = event.getType();
        if ((type & MapBoundsEvent.COORDINATE_SYSTEM_MASK) != 0) {
            /*
             * The coordinate reference system has changed. Set the map
             * to display the full extent of layer bounds to avoid the
             * effect of a shrinking map
             */
            reset();
        }
    }

    /**
     * Gets the full extent of map context's layers. The only reason
     * this method is defined is to avoid having try-catch blocks all
     * through other methods.
     */
    private void setFullExtent() {
        if (context != null && context.getLayerCount() > 0) {
            try {
                fullExtent = context.getLayerBounds();
            } catch (Exception ex) {
                throw new IllegalStateException(ex);
            }
        } else {
            fullExtent = null;
        }
    }

    /**
     * Calculate the affine transforms used to convert between
     * world and pixel coordinates. The calculations here are very
     * basic and assume a cartesian reference system.
     * <p>
     * Tne transform is calculated such that {@code envelope} will
     * be centred in the display
     * 
     * @param mapEnv the current map extent (map units)
     * @param paintArea the current map pane extent (pixels)
     *
     * @return a new ReferencedEnvelope that contains the input
     *         envelope and has the same width/height ratio as
     *         the pane's current paint area
     */
    private void setTransforms(final Envelope envelope, final Rectangle paintArea) {
        ReferencedEnvelope refEnv = new ReferencedEnvelope(envelope);

        double xscale = paintArea.getWidth() / refEnv.getWidth();
        double yscale = paintArea.getHeight() / refEnv.getHeight();

        double scale = Math.min(xscale, yscale);

        double xoff = refEnv.getMedian(0) * scale - paintArea.getCenterX();
        double yoff = refEnv.getMedian(1) * scale + paintArea.getCenterY();

        worldToScreen = new AffineTransform(scale, 0, 0, -scale, -xoff, yoff);
        try {
            screenToWorld = worldToScreen.createInverse();
            
        } catch (NoninvertibleTransformException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Erase the base image. This is much faster than recreating a new BufferedImage
     * object each time we need to redraw the image
     */
    private void clearBaseImage() {
        assert(baseImage != null);
        Graphics2D g2D = baseImage.createGraphics();
        g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
        Rectangle2D.Double rect = new Rectangle2D.Double(
                0, 0, baseImage.getWidth(), baseImage.getHeight());
        g2D.fill(rect);
    }

    /**
     * Publish a MapPaneEvent to listening objects
     */
    private void publishEvent(MapPaneEvent ev) {
        for (MapPaneListener listener : listeners) {
            switch (ev.getType()) {
                case NEW_CONTEXT:
                    listener.onNewContext(ev);
                    break;

                case NEW_RENDERER:
                    listener.onNewRenderer(ev);
                    break;

                case PANE_RESIZED:
                    listener.onResized(ev);
                    break;

                case DISPLAY_AREA_CHANGED:
                    listener.onDisplayAreaChanged(ev);
                    break;
            }
        }
    }
}
