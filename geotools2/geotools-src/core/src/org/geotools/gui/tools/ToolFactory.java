package org.geotools.gui.tools;

import java.util.logging.Logger;

/**
 * Factory for constructing Tool classes.
 * @version $Id: ToolFactory.java,v 1.4 2003/04/14 21:37:14 jmacgill Exp $
 * @author Cameron Shorter
 */
public abstract class ToolFactory {
    /**
     * The logger 
     */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.tools.ToolFactory");

    private static ToolFactory factory=null;

    /**
     * Create an instance of the factory.
     * @return An instance of ToolFactory, or null if ToolFactory could not
     * be created.
     */
    public static ToolFactory createFactory(){
        if (factory==null){
            String factoryClass = System.getProperty("ToolFactoryImpl");
            LOGGER.fine("loaded property = " + factoryClass);
            if(factoryClass != null && factoryClass != ""){
                factory = createFactory(factoryClass); 
            }
            if (factory==null) {
                factory = createFactory(
                    "org.geotools.gui.tools.ToolFactoryImpl");
            }
        }
        return factory;
    }

    /**
     * Create an instance of the factory.
     * @return An instance of the Factory, or null if the Factory could not
     * be created.
     * @param factoryClass The name of the factory, eg:
     * "org.geotools.gui.tools.ToolFactoryImpl".
     */
    public static ToolFactory createFactory(String factoryClass) {
        try{
            return factory = (ToolFactory)Class.forName(
                    factoryClass).newInstance();
        } catch (ClassNotFoundException e){
            LOGGER.warning("createFactory failed to find implementation "
                    + factoryClass+ " , "+e);
        } catch (InstantiationException e){
            LOGGER.warning("createFactory failed to insantiate implementation "
                    + factoryClass+" , "+e);
        } catch (IllegalAccessException e){
            LOGGER.warning("createStyleFactory failed to access implementation "
                    + factoryClass+" , "+e);
        }
        return null;
    }

    /**
     * Create an instance of PanTool.
     */
    public abstract PanTool createPanTool();
    
    /**
     * Create an instance of ZoomTool.
     */
    public abstract ZoomTool createZoomTool();

    /**
     * Create an instance of ZoomTool.
    /* @parma zoomFactor he factor to zoom in/out by, zoomFactor=0.5 means
     * zoom in, zoomFactor=2 means zoom out.
     */
    public abstract ZoomTool createZoomTool(double zoomFactor);
}
