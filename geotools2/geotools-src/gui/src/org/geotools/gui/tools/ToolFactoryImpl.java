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
package org.geotools.gui.tools;

import org.geotools.gui.tools.PanTool;
import org.geotools.gui.tools.ToolList;
import org.geotools.gui.tools.ZoomTool;


/**
 * An implementation of ToolFactory to be used to construct tools.  This class
 * should not be called directly.  Instead it should be created from
 * ToolFactory, and ToolFactory methods should be called instead.
 */
public class ToolFactoryImpl extends ToolFactory {
    /**
     * Create an instance of ToolFactoryImpl.  Note that this constructor
     * should only be called from ToolFactory.
     */
    protected ToolFactoryImpl() {
    }

    public PanTool createPanTool() {
        return new PanToolImpl();
    }

    public ZoomTool createZoomTool() {
        return new ZoomToolImpl();
    }

    /**
     * Create an instance of ZoomTool.
     *
     * @param zoomFactor he factor to zoom in/out by, zoomFactor=0.5 means zoom
     *        in, zoomFactor=2 means zoom out.
     *
     * @return The ZoomTool.
     */
    public ZoomTool createZoomTool(double zoomFactor) {
        return new ZoomToolImpl(zoomFactor);
    }

    /**
     * Creates an empty ToolList with selectedTool=null.
     *
     * @return An empty ToolList.
     */
    public ToolList createToolList() {
        return new ToolListImpl();
    }

    /**
     * Creates a ToolList with Pan, ZoomIn, ZoomOut and selectedTool=ZoomIn.
     *
     * @return An ToolList with Pan, ZoomIn, ZoomOut and selectedTool=ZoomIn.
     */
    public ToolList createDefaultToolList() {
        ToolList toolList = createToolList();
        ToolFactory toolFactory = ToolFactory.createFactory();
        Tool tool = toolFactory.createZoomTool(2.0);
        toolList.add(tool);

        toolList.setSelectedTool(tool);

        tool = toolFactory.createZoomTool(0.5);
        toolList.add(tool);

        tool = toolFactory.createPanTool();
        toolList.add(tool);

        return toolList;
    }
}
