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
package org.geotools.map;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CoordinateSystemFactory;
import org.geotools.cs.HorizontalDatum;
import org.geotools.ct.Adapters;
import org.geotools.data.DataSource;
import org.geotools.gui.tools.Tool;
import org.geotools.gui.tools.ToolFactory;
import org.geotools.styling.Style;
import org.opengis.cs.CS_CoordinateSystem;
import java.util.logging.Logger;


/**
 * An implementation of ContextFactory to be used to construct context classes.
 * It should not be called directly.  Instead it should be created from
 * ContextFactory, and ContextFactory methods should be called instead.
 */
public class ContextFactoryImpl extends ContextFactory {
    /** The class used for identifying for logging. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.map.ContextFactoryImpl");

    /** Translates between coordinate systems */
    private Adapters adapters = Adapters.getDefault();

    /**
     * Create an instance of ContextFactoryImpl.  Note that this constructor
     * should only be called from ContextFactory.
     */
    protected void ContextFactoryImpl() {
    }

    /**
     * Create a BoundingBox.
     *
     * @param bbox The extent associated with this class.
     * @param coordinateSystem The coordinate system associated with this
     *        class.
     *
     * @return The BoundingBox.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public BoundingBox createBoundingBox(Envelope bbox,
        CS_CoordinateSystem coordinateSystem) throws IllegalArgumentException {
        return new BoundingBoxImpl(bbox, coordinateSystem);
    }

    /**
     * Create a Context.
     *
     * @param bbox The extent associated with this class.
     * @param layerList The list of layers associated with this context.
     * @param toolList The ToolList for this context.
     * @param title The name of this context.  Must be set.
     * @param _abstract A description of this context.  Optional, set to null
     *        if none exists.
     * @param keywords An array of keywords to be used when searching for this
     *        context.  Optional, set to null if none exists.
     * @param contactInformation Contact details for the person who created
     *        this context.  Optional, set to null if none exists.
     *
     * @return A new Context
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public Context createContext(BoundingBox bbox, LayerList layerList,
        ToolList toolList, String title, String _abstract, String[] keywords,
        String contactInformation) throws IllegalArgumentException {
        return new ContextImpl(bbox, layerList, toolList, title, _abstract,
            keywords, contactInformation);
    }

    /**
     * Create a Context with default parameters.<br>
     * boundingBox = WGS84 GeographicCoordinateSystem, (-180,-90),(180,90)<br>
     * layerList = empty list <br>
     * toolList = Pan, ZoomIn, ZoomOut, selectedTool=ZoomIn<br>
     * title = "" <br>
     * _abstract = ""<br>
     * keywords = empty array<br>
     * contactInformation = ""<br>
     *
     * @return A default Context class.
     *
     * @throws RuntimeException If there is a FactoryException or
     *         RemoteException.
     */
    public Context createContext() {
        try {
            CoordinateSystem cs = CoordinateSystemFactory.getDefault()
                                                         .createGeographicCoordinateSystem("WGS84",
                    HorizontalDatum.WGS84);
            org.geotools.pt.Envelope envelope = cs.getDefaultEnvelope();
            Envelope envelope2 = new Envelope(envelope.getMinimum(0),
                    envelope.getMaximum(0), envelope.getMinimum(1),
                    envelope.getMaximum(1));

            CS_CoordinateSystem cs1 = adapters.export(cs);

            // Create a toolList with Pan, ZoomIn, ZoomOut tools
            ToolList toolList = createToolList();
            ToolFactory toolFactory = ToolFactory.createFactory();
            Tool tool = toolFactory.createZoomTool(2.0);
            toolList.add(tool);

            toolList.setSelectedTool(tool);

            tool = toolFactory.createZoomTool(0.5);
            toolList.add(tool);

            tool = toolFactory.createPanTool();
            toolList.add(tool);

            return createContext(createBoundingBox(envelope2, cs1),
                createLayerList(), // empty LayerList
                toolList, //
                "", // title
                "", // _abstract
                null, // keywords
                "" // contactInformation
            );
        } catch (org.geotools.cs.FactoryException e) {
            LOGGER.warning(
                "CS Factory Exception, check your CLASSPATH.  Cause is: " +
                e.getCause());
            throw new RuntimeException();
        } catch (java.rmi.RemoteException e) {
            LOGGER.warning("CS RemoteExcepion.  Cause is: " + e.getCause());
            throw new RuntimeException();
        }
    }

    /**
     * Creates a Layer.
     *
     * @param dataSource The dataSource to query in order to get features for
     *        this layer.
     * @param style The style to use when rendering features associated with
     *        this layer.
     *
     * @return A new Layer.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public Layer createLayer(DataSource dataSource, Style style)
        throws IllegalArgumentException {
        return new LayerImpl(dataSource, style);
    }

    /**
     * Create a LayerList.
     *
     * @return A new LayerList.
     */
    public LayerList createLayerList() {
        return new LayerListImpl();
    }

    /**
     * Creates an empty ToolList with selectedTool=null.
     *
     * @return An empty ToolList.
     */
    public ToolList createToolList() {
        return new ToolListImpl();
    }
}
