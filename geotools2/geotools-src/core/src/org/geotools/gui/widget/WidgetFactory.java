package org.geotools.gui.widget;

import java.util.logging.Logger;
import java.util.logging.Level;
import org.geotools.gui.tools.AbstractTool;
import org.geotools.map.Context;
/**
 * Factory for constructing Tool classes.
 * @version $Id: WidgetFactory.java,v 1.1 2003/02/06 09:54:42 camerons Exp $
 * @author Cameron Shorter
 */
public abstract class WidgetFactory {
    /**
     * The logger 
     */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.widget.WidgetFactory");

    private static WidgetFactory factory=null;

    /**
     * Create an instance of the factory.
     * @return An instance of WidgetFactory, or null if WidgetFactory could not
     * be created.
     */
    public static WidgetFactory createFactory(){
        if (factory==null){
            String factoryClass = System.getProperty("WidgetFactoryImpl");
            LOGGER.fine("loaded property = " + factoryClass);
            if(factoryClass != null && factoryClass != ""){
                factory = createFactory(factoryClass); 
            }
            if (factory==null) {
                factory = createFactory(
                    "org.geotools.gui.widget.WidgetFactoryImpl");
            }
        }
        return factory;
    }

    /**
     * Create an instance of the factory.
     * @return An instance of the Factory, or null if the Factory could not
     * be created.
     * @param factoryClass The name of the factory, eg:
     * "org.geotools.gui.widget.WidgetFactoryImpl".
     */
    public static WidgetFactory createFactory(String factoryClass) {
        try{
            return factory = (WidgetFactory)Class.forName(
                    factoryClass).newInstance();
        } catch (ClassNotFoundException e){
            LOGGER.warning("createFactory failed to find implementation "
                    + factoryClass+ " , "+e);
        } catch (InstantiationException e){
            LOGGER.warning("createFactory failed to insantiate implementation "
                    + factoryClass+" , "+e);
        } catch (IllegalAccessException e){
            LOGGER.warning("createFactory failed to access implementation "
                    + factoryClass+" , "+e);
        }
        return null;
    }

    /**
     * Create an instance of MapPane.
     */
    public abstract org.geotools.gui.widget.MapPane createMapPane(
            AbstractTool tool,
            Context context) throws IllegalArgumentException;
}
