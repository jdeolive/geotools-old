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
import java.awt.Graphics;
import javax.swing.JPanel;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionDefault;
import org.geotools.gui.tools.Tool;
import org.geotools.map.events.AreaOfInterestChangedEvent;
import org.geotools.map.events.AreaOfInterestChangedListener;
import org.geotools.map.AreaOfInterestModel;
import org.geotools.map.events.LayerListChangedEvent;
import org.geotools.map.events.LayerListChangedListener;
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
 * @version $Id: MapPane2.java,v 1.5 2002/08/18 03:37:34 camerons Exp $
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
     * The model which stores a list of layers.
     */
    private LayerList layerList;

    /**
     * The areaOfInterest to be drawn by this map.
     */
    private Envelope areaOfInterest;
    private AreaOfInterestModel areaOfInterestModel;

    /**
     * The style used by this MapPane.
     */
    private Style style;
    
    /**
     * Create a DefaultMapPane.  This is the defaultMapPane which is to be
     * extended by other MapPanes.
     * A MapPane provides the core functionality for drawing maps.
     *
     * @param tool The tool to use with the MapPane.
     * parameter should be entered here.
     * @param layerList The layerList where all the layers for this view are
     * kept.
     * @param areaOfInterestModel The model which stores the area of interest.
     * @param style The style used by this MapPane's renderer.
     */
    public MapPane2(
            Tool tool,
            LayerList layerList,
            AreaOfInterestModel areaOfInterestModel,
            Style style) {        
        this.tool=tool;
        this.layerList=layerList;
        this.areaOfInterestModel=areaOfInterestModel;
        this.style=style;
        this.renderer=new Java2DRenderer();
        
        // Initialise the Tool to use this MapPane.
        this.tool.setMapPane(this);
        this.tool.setAreaOfInterestModel(this.areaOfInterestModel);
    }
    
    /**
     * Set the tool for this mapPane.  The tool handles all the mouse and key
     * actions on behalf of this mapPane.  Different tools can be assigned in
     * order to get the mapPane to behave differently.
     * @param tool The tool to use for this mapPane.
     */
    public void setTool(Tool tool)
    {
        this.tool=tool;
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
     * Set the style to be used by this MapPane's renderer.
     * @param style The style to use.
     */
    public void setStyle(Style style) {
        this.style=style;
        this.repaint(this.getVisibleRect());
    }

    /**
     * Get the style used by this MapPane's renderer.
     * @param style The style to use.
     */
    public Style getStyle() {
        return this.style;
    }

    /**
     * Loop through all the layers in this mapPane's layerList and render each
     * Layer which is set to Visable.
     * @param graphics The graphics object to paint to.
     * @task TODO fill in exception.  This should implement logging.
     * @task REVISIT Need to create an interface
     * features=dataSource.getFeatures(extent)
     */
    public void paintComponent(Graphics graphics) {
        renderer.setOutput(graphics, this.getBounds());
        if ((layerList!=null) && (layerList.getLayers()!=null))
        {
            for (int i=0;i<layerList.getLayers().length;i++) {
                if ((layerList.getLayers()[i]!=null)&&
                        layerList.getLayers()[i].getVisability())
                {
                    try {
                        FeatureCollection fc=new FeatureCollectionDefault(
                        layerList.getLayers()[i].getDataSource());
                        renderer.render(
                        fc.getFeatures(new EnvelopeExtent(
                        areaOfInterestModel.getAreaOfInterest())),
                        areaOfInterestModel.getAreaOfInterest(),
                        style);
                    } catch (DataSourceException exception) {
                        // log exception
                    }
                }
            }
        }
    }
    
    /**
     * Process an AreaOfInterestChangedEvent, involves a redraw.
     * @param areaOfInterestChangedEvent The new extent.
     */
    public void areaOfInterestChanged(
            AreaOfInterestChangedEvent areaOfInterestChangedEvent) {
        this.repaint(this.getVisibleRect());
    }
    
    /**
     * Process an LayerListChangedEvent, involves a redraw.
     * @param LayerListChangedEvent The new extent.
     */
    public void LayerListChanged(
            LayerListChangedEvent layerListChangedEvent) {
        this.repaint(this.getVisibleRect());
    }
}
