package org.geotools.gui.swing;

/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Center for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

import com.vividsolutions.jts.geom.Envelope;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.lang.IllegalArgumentException;
import java.util.EventObject;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.geotools.data.DataSourceException;
import org.geotools.datasource.extents.EnvelopeExtent;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionDefault;
import org.geotools.gui.swing.event.GeoMouseEvent;
import org.geotools.gui.tools.Tool;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.map.events.BoundingBoxListener;
import org.geotools.map.events.LayerListListener;
import org.geotools.map.events.SelectedToolListener;
import org.geotools.map.Layer;
import org.geotools.map.LayerList;
import org.geotools.renderer.Java2DRenderer;
import org.geotools.styling.Style;

/**
 * This class provides core functionality for drawing a map.
 * At the moment, this package is still experimental.  I expect that it will
 * be removed, and the functionality will be moved into other classes like
 * MapPane.
 * @version $Id: MapPaneImpl.java,v 1.16 2003/03/30 20:07:49 camerons Exp $
 * @author Cameron Shorter
 * @task REVISIT: We probably should have a StyleModel which sends
 * StyleModelEvents when the Style changes.
 */

public class MapPaneImpl extends JPanel implements
    BoundingBoxListener,LayerListListener,ComponentListener,SelectedToolListener
{
    /**
     * The class to use to render this MapPane.
     */
    Java2DRenderer renderer;
   
    /**
     * The model which stores a list of layers and BoundingBox.
     */
    private Context context;
    
    /**
     * A transform from screen coordinates to real world coordinates.
     */
    private DotToCoordinateTransformImpl dotToCoordinateTransform;

    /**
     * The class used for identifying for logging.
     */
    private static final Logger LOGGER = Logger.getLogger(
        "org.geotools.gui.swing.MapPaneImpl");

    /**
     * Create a MapPane.
     * A MapPane marshals the drawing of maps.
     * @param context The context where layerList and boundingBox are kept.  If
     * context is null, an IllegalArguementException is thrown.
     * @task TODO Move the "extra stuff" out of this method.
     */
    public MapPaneImpl(
            Context context) throws IllegalArgumentException
    {
        if (context==null){
            throw new IllegalArgumentException();
        }else{
            this.renderer=new Java2DRenderer(context);
            this.context=context;
            context.getBbox().addAreaOfInterestChangedListener(this);
            context.getToolList().addSelectedToolListener(this);
            context.getToolList().getTool().addMouseListener(this,context);
            
            // Create a transform for this mapPane.
            this.dotToCoordinateTransform=new DotToCoordinateTransformImpl(
                this, context);
            
            // A zero sized mapPane cannot be resized later and doesn't behave
            // very nicely
            this.setMinimumSize(new Dimension(2,2));
            
            // use absolute positioning
            this.setLayout(null);
            
        }
    }

    /**
     * Loop through all the layers in this mapPane's layerList and render each
     * Layer which is set to Visable.  If the AreaOfInterest is null, then the
     * AreaOfInterest will be set to the AreaOfInterest of the the LayerList.
     * Rendering will not occur now, but will wait until after the
     * AreaOfInterestChangedEvent has been received.
     * @param graphics The graphics object to paint to.
     * @task TODO fill in exception.  This should implement logging.
     * @task REVISIT Need to create an interface
     * features=dataSource.getFeatures(extent)
     * @task REVISIT We should set the AreaOfInterest somewhere other than here.
     * @task TODO Need to change getBbox(false) to getBbox(true) to speed
     * things up.
     * @task TODO create a layerList.getCoordinateSystem method
     */
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (context.getBbox().getAreaOfInterest()==null){
            Envelope bBox=context.getLayerList().getBbox(false);
            if (bBox!=null){
                LOGGER.info("AreaOfInterest calculated during rendering");
                context.getBbox().setAreaOfInterest(
                    context.getLayerList().getBbox(false),
                    null);
            }
        }

        renderer.render(
            graphics,
            new Rectangle(
                getInsets().left,
                getInsets().top,
                getWidth()-getInsets().left-getInsets().right,
                getHeight()-getInsets().top-getInsets().bottom));
    }
    
    /**
     * Process an AreaOfInterestChangedEvent, involves a redraw.
     * @param areaOfInterestChangedEvent The new extent.
     */
    public void areaOfInterestChanged(
            EventObject areaOfInterestChangedEvent) {
        dotToCoordinateTransform.updateTransform();
        repaint(getVisibleRect());
    }
    
    /**
     * Process an LayerListChangedEvent, involves a redraw.
     * @param LayerListChangedEvent The new extent.
     */
    public void layerListChanged(EventObject layerListChangedEvent) {
        repaint(getVisibleRect());
    }

    /**
     * Processes mouse events occurring on this component. This method
     * overrides the default AWT's implementation in order to wrap the
     * <code>MouseEvent</code> into a {@link GeoMouseEvent}. Then, the default
     * AWT's implementation is invoked in * order to pass this event to any
     * registered {@link MouseListener} objects.
     */
    protected void processMouseEvent(final MouseEvent event) {
        super.processMouseEvent(
            new GeoMouseEvent(
                event,
                dotToCoordinateTransform.getTransform()));
    }

    /**
     * Processes mouse motion events occurring on this component. This method
     * overrides the default AWT's implementation in order to wrap the
     * <code>MouseEvent</code> into a {@link GeoMouseEvent}. Then, the default
     * AWT's implementation is invoked in order to pass this event to any
     * registered {@link MouseMotionListener} objects.
     */
    protected void processMouseMotionEvent(final MouseEvent event) {
        super.processMouseMotionEvent(
            new GeoMouseEvent(
                event,
                dotToCoordinateTransform.getTransform()));
    }
    
    /** Invoked when the component has been made invisible.
     *
     */
    public void componentHidden(ComponentEvent e) {
    }
    
    /** Invoked when the component's position changes.
     *
     */
    public void componentMoved(ComponentEvent e) {
    }
    
    /** Invoked when the component's size changes, update the
     * screenToCoordinateTransform.
     *
     */
    public void componentResized(ComponentEvent e) {
        dotToCoordinateTransform.updateTransform();
    }
    
    /** Invoked when the component has been made visible.
     *
     */
    public void componentShown(ComponentEvent e) {
    }
    
    /**
     * Called when the selectedTool on a MapPane changes.
     */
    public void selectedToolChanged(EventObject event)
    {
        if (context.getToolList().getTool()!=null){
            context.getToolList().getTool().addMouseListener(this,context);
        }
    }
    
}
