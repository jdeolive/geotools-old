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
import org.geotools.datasource.extents.EnvelopeExtent;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionDefault;
import org.geotools.gui.swing.event.GeoMouseEvent;
import org.geotools.gui.tools.Tool;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.map.Layer;
import org.geotools.map.LayerList;
import org.geotools.map.events.BoundingBoxEvent;
import org.geotools.map.events.BoundingBoxListener;
import org.geotools.map.events.LayerListListener;
import org.geotools.map.events.SelectedToolListener;
import org.geotools.renderer.Java2DRenderer;
import org.geotools.styling.Style;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

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
import java.awt.geom.AffineTransform;
import java.lang.IllegalArgumentException;
import java.util.EventObject;
import java.util.logging.Logger;
import javax.swing.JPanel;


/**
 * This widget is responsible for marshalling the rendering of a map.
 * It processes Listener events and calls the Renderer as required.
 * It maintains the correct aspect ratio by resizing the Context's
 * BoundingBox when this component changes size.
 *
 * @author Cameron Shorter
 * @version $Id: MapPaneImpl.java,v 1.19 2003/04/24 09:36:50 camerons Exp $
 *
 * @task REVISIT: We need to add a PixcelAspectRatio varible which defaults
 * to 1, ie width/heigh=1.  Currently, this is assumed to be 1.
 */
public class MapPaneImpl extends JPanel implements BoundingBoxListener,
    LayerListListener, ComponentListener, SelectedToolListener {
    /** The class used for identifying for logging. */
    private static final Logger LOGGER =
        Logger.getLogger("org.geotools.gui.swing.MapPaneImpl");

    /** The class to use to render this MapPane. */
    Java2DRenderer renderer;

    /** The model which stores a list of layers and BoundingBox. */
    private Context context;
    private Adapters adapters = Adapters.getDefault();

    /** A transform from screen coordinates to real world coordinates. */
    //private DotToCoordinateTransformImpl dotToCoordinateTransform;
    private MathTransform dotToCoordinateTransform;

    /**
     * Create a MapPane. A MapPane marshals the drawing of maps.
     *
     * @param context The context where layerList and boundingBox are kept.  If
     *        context is null, an IllegalArguementException is thrown.
     *
     * @throws IllegalArgumentException when parameters are null.
     *
     * @task TODO Move the "extra stuff" out of this method.
     */
    public MapPaneImpl(Context context) throws IllegalArgumentException {
        if (context == null) {
            throw new IllegalArgumentException();
        } else {
            this.renderer = new Java2DRenderer(context);
            this.context = context;
            
            // Request to be notified when map parameters change
            context.getBbox().addAreaOfInterestChangedListener(this);
            context.getLayerList().addLayerListChangedListener(this);
            context.getToolList().addSelectedToolListener(this);
            context.getToolList().getTool().addMouseListener(this, context);
            addComponentListener(this);

            // A zero sized mapPane cannot be resized later and doesn't behave
            // very nicely
            this.setMinimumSize(new Dimension(2, 2));

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
     *
     * @param graphics The graphics object to paint to.
     *
     * @task TODO fill in exception.  This should implement logging.
     * @task REVISIT Need to create an interface
     *       features=dataSource.getFeatures(extent)
     * @task REVISIT We should set the AreaOfInterest somewhere other than
     *       here.
     * @task TODO Need to change getBbox(false) to getBbox(true) to speed
     *       things up.
     * @task TODO create a layerList.getCoordinateSystem method
     */
    public void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        if (context.getBbox().getAreaOfInterest() == null) {
            Envelope bBox = context.getLayerList().getBbox(false);

            if (bBox != null) {
                LOGGER.info("AreaOfInterest calculated during rendering");
                context.getBbox().setAreaOfInterest(
                    context.getLayerList().getBbox(false),
                    null
                );
            }
        }
        int w=getWidth() - getInsets().left - getInsets().right;
        int h=getHeight() - getInsets().top - getInsets().bottom;
        
        // prevent divide by zero errors
        if (h==0){
            h=2;
        }
        
        renderer.render(
            graphics,
            new Rectangle(getInsets().left, getInsets().top,w,h)
        );
    }

    /**
     * Process an AreaOfInterestChangedEvent, involves a redraw.
     *
     * @param areaOfInterestChangedEvent The new extent.
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
        super.processMouseEvent(
            new GeoMouseEvent(
                event,
                dotToCoordinateTransform
            )
        );
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
        super.processMouseMotionEvent(
            new GeoMouseEvent(
                event,
                dotToCoordinateTransform
            )
        );
    }

    /**
     * Invoked when the component has been made invisible.
     *
     * @param e ComponentEvent.
     */
    public void componentHidden(ComponentEvent e) {}

    /**
     * Invoked when the component's position changes.
     *
     * @param e ComponentEvent.
     */
    public void componentMoved(ComponentEvent e) {}

    /**
     * Invoked when the component's size changes, change the AreaOfInterest so
     * that the aspect ratio remains the same.  One axis will remain the same
     * width/height while the other axis will expand to fit the new aspect
     * ratio.<br>
     * The method will trigger an AreaOfInterestEvent which in turn will
     * cause a repaint.
     *
     * @param e ComponentEvent.
     */
    public void componentResized(ComponentEvent e) {
        int w=getWidth() - getInsets().left - getInsets().right;
        int h=getHeight() - getInsets().top - getInsets().bottom;
        double newAspectRatio = (double)w/(double)h;
        
        AffineTransform at = new AffineTransform();
        Envelope aoi = context.getBbox().getAreaOfInterest();
        
        double contextAspectRatio=
            (double)(aoi.getMaxX() - aoi.getMinX())
            /(double)(aoi.getMaxY() - aoi.getMinY());
        
        // No need to resize if the aspect ratio is correct.
        if (contextAspectRatio==newAspectRatio){
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

        MathTransform transform =
            MathTransformFactory.getDefault().createAffineTransform(at);
        context.getBbox().transform(adapters.export(transform));
    }

    /**
     * Invoked when the component has been made visible.
     *
     * @param e ComponentEvent.
     */
    public void componentShown(ComponentEvent e) {}

    /**
     * Called when the selectedTool on a MapPane changes.
     *
     * @param event ComponenetEvent.
     */
    public void selectedToolChanged(EventObject event) {
        if (context.getToolList().getTool() != null) {
            context.getToolList().getTool().addMouseListener(this, context);
        }
    }
    
    /**
     * Re-evaluate the screen to CoordinateSystem transform, this method
     * should be called whenever the MapPane resizes, boundingBox resizes,
     * or coordinateSystem changes.
     */
    public void updateTransform() {
        //Real World Coordinates
        Envelope aoi=context.getBbox().getAreaOfInterest();
        
        // Scaling
        double scaleX=(aoi.getMaxX()-aoi.getMinX())
            /(getWidth()-getInsets().left
              -getInsets().right);
        double scaleY=(aoi.getMaxY()-aoi.getMinY())
            /(getHeight()-getInsets().top
              -getInsets().bottom);

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

        AffineTransform at=new AffineTransform(
            // m00: ScaleX
            scaleX,
            
            // m10
            0.0,
            
            // m01:
            0.0,
            
            // m11: -ScaleY
            -scaleY,
            
            // m02: TransformX
            aoi.getMinX()-scaleX*getInsets().left,
            
            // m12: TransformY
            (getHeight()-getInsets().bottom)*scaleY
            +aoi.getMinY());
        
        dotToCoordinateTransform=MathTransformFactory.getDefault(
            ).createAffineTransform(at);
     }
}
