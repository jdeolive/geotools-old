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

import java.util.logging.Logger;


/**
 * Factory for constructing Tool classes.
 *
 * @author Cameron Shorter
 * @version $Id: ToolFactory.java,v 1.7 2004/04/19 17:38:18 jmacgill Exp $
 */
public abstract class ToolFactory {
    /** The logger */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.tools.ToolFactory");
    /** Factory to make tools. */
    private static ToolFactory factory = null;

    /**
     * Create an instance of the factory.
     *
     * @return An instance of ToolFactory, or null if ToolFactory could not be
     *         created.
     */
    public static ToolFactory createFactory() {
        if (factory == null) {
            String factoryClass = null;
            try{
                factoryClass = System.getProperty("ToolFactoryImpl");
                LOGGER.fine("loaded property = " + factoryClass);
            }
            catch(SecurityException se){
                LOGGER.warning("unable to obtain ToolFactory property " + se);
            }
            if ((factoryClass != null) && (factoryClass != "")) {
                factory = createFactory(factoryClass);
            }

            if (factory == null) {
                factory = createFactory(
                        "org.geotools.gui.tools.ToolFactoryImpl");
            }
        }

        return factory;
    }

    /**
     * Create an instance of the factory.
     *
     * @param factoryClass The name of the factory, eg:
     *        "org.geotools.gui.tools.ToolFactoryImpl".
     *
     * @return An instance of the Factory, or null if the Factory could not be
     *         created.
     */
    public static ToolFactory createFactory(String factoryClass) {
        try {
            return factory = (ToolFactory) Class.forName(factoryClass)
                                                .newInstance();
        } catch (ClassNotFoundException e) {
            LOGGER.warning("createFactory failed to find implementation "
                + factoryClass + " , " + e);
        } catch (InstantiationException e) {
            LOGGER.warning("createFactory failed to insantiate implementation "
                + factoryClass + " , " + e);
        } catch (IllegalAccessException e) {
            LOGGER.warning(
                "createStyleFactory failed to access implementation "
                + factoryClass + " , " + e);
        }

        return null;
    }

    /**
     * Create an instance of PanTool.
     *
     * @return the PanTool.
     */
    public abstract PanTool createPanTool();

    /**
     * Create an instance of ZoomTool.
     *
     * @return the ZoomTool.
     */
    public abstract ZoomTool createZoomTool();

    /**
     * Create an instance of ZoomTool. /
     *
     * @param zoomFactor he factor to zoom in/out by, zoomFactor=0.5 means zoom
     *        in, zoomFactor=2 means zoom out.
     *
     * @return the ZoomTool.
     */
    public abstract ZoomTool createZoomTool(double zoomFactor);

    /**
     * Creates an empty ToolList with selectedTool=null.
     *
     * @return An empty ToolList.
     */
    public abstract ToolList createToolList();

    /**
     * Creates an empty ToolList with selectedTool=null.
     *
     * @return An empty ToolList.
     */
    public abstract ToolList createDefaultToolList();
}
