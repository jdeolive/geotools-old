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
import java.awt.Color;
import java.awt.Graphics;
import java.lang.IllegalArgumentException;
import javax.swing.JPanel;
import java.util.logging.Logger;
import java.awt.Dimension;
import java.awt.Rectangle;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionDefault;
import org.geotools.gui.tools.Tool;
import java.util.EventObject;
import org.geotools.map.events.AreaOfInterestChangedListener;
import org.geotools.map.BoundingBox;
import org.geotools.map.events.LayerListChangedListener;
import org.geotools.map.Context;
import org.geotools.map.Layer;
import org.geotools.map.LayerList;
import org.geotools.renderer.Java2DRenderer;
import org.geotools.styling.Style;
import org.geotools.datasource.extents.EnvelopeExtent;
import org.geotools.data.DataSourceException;

/**
 * This class provides core functionality for drawing a map.
 * At the moment, this package is still experimental.  I expect that it will
 * be removed, and the functionality will be moved into other classes like
 * MapPane.
 * @version $Id: MapPane2.java,v 1.18 2003/01/28 10:18:11 camerons Exp $
 * @author Cameron Shorter
 * @task REVISIT: We probably should have a StyleModel which sends
 * StyleModelEvents when the Style changes.  Note that the Style should not
 * be stored with the MapModel/LayerList because a user may want to display
 * 2 maps which use the same data, but a different style.
 */

public class MapPane2 extends JPanel implements
    AreaOfInterestChangedListener, LayerListChangedListener
{
    /**
     * The current tool for this MapPane.
     */
    private Tool tool;

    /**
     * The class to use to render this MapPane.
     */
    Java2DRenderer renderer;
   
    /**
     * The model which stores a list of layers and BoundingBox.
     */
    private Context context;

    /**
     * The class used for identifying for logging.
     */
    private static final Logger LOGGER = Logger.getLogger(
        "org.geotools.gui.swing.MapPane2");

    /**
     * Create a MapPane.
     * A MapPane marshals the drawing of maps.
     *
     * @param tool The tool to use with the MapPane, can be set to null if no
     * tool is required.
     * @param context The context where layerList and boundingBox are kept.  If
     * context is null, an IllegalArguementException is thrown.
     */
    public MapPane2(
            Tool tool,
            Context context) throws IllegalArgumentException
    {
        if ((tool==null)||(context==null)){
            throw new IllegalArgumentException();
        }else{
            this.context=context;
            this.context.getBbox().addAreaOfInterestChangedListener(this);
            this.renderer=new Java2DRenderer();
            setTool(tool);
            
            // A zero sized mapPane cannot be resized later and doesn't behave
            // very nicely
            this.setMinimumSize(new Dimension(2,2));
            
            // use absolute positioning
            this.setLayout(null);
        }
    }
    
    /**
     * Set the tool for this mapPane.  The tool handles all the mouse and key
     * actions on behalf of this mapPane.  Different tools can be assigned in
     * order to get the mapPane to behave differently.
     * @param tool The tool to use for this mapPane.
     * @throws IllegalArgumentException if tool is null.
     */
    public void setTool(Tool tool) throws IllegalArgumentException
    {
        if (tool==null){
            throw new IllegalArgumentException();
        }else{
            this.tool=tool;
            this.tool.setWidget(this);
            this.tool.setContext(context);
        }
    }

    /**
     * Get the tool assigned to this mapPane.  If none is assigned, then null
     * is returned.
     * @return The tool assigned to this mapPane.
     */
    public Tool getTool()
    {
        return this.tool;
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
        //renderer.setOutput(graphics,this.getVisibleRect());
        renderer.setOutput(
            graphics,
            new Rectangle(
                getInsets().left,
                getInsets().top,
                getWidth()-getInsets().left-getInsets().right,
                getHeight()-getInsets().top-getInsets().bottom));

        for (int i=0;i<context.getLayerList().getLayers().length;i++) {
            if (context.getLayerList().getLayers()[i].getVisability())
            {
                try {
                    FeatureCollection fc=new FeatureCollectionDefault(
                    context.getLayerList().getLayers()[i].getDataSource());
                    renderer.render(
                        fc.getFeatures(new EnvelopeExtent(
                            context.getBbox().getAreaOfInterest())),
                        context.getBbox().getAreaOfInterest(),
                        context.getLayerList().getLayers()[i].getStyle());
                } catch (Exception exception) {
                    LOGGER.warning(
                        "Exception "
                        + exception
                        + " rendering layer "
                        + context.getLayerList().getLayers()[i]);
                }
            }
        }
    }
    
    /**
     * Process an AreaOfInterestChangedEvent, involves a redraw.
     * @param areaOfInterestChangedEvent The new extent.
     */
    public void areaOfInterestChanged(
            EventObject areaOfInterestChangedEvent) {
        repaint(getVisibleRect());
    }
    
    /**
     * Process an LayerListChangedEvent, involves a redraw.
     * @param LayerListChangedEvent The new extent.
     */
    public void LayerListChanged(
            EventObject layerListChangedEvent) {
        repaint(getVisibleRect());
    }
}
