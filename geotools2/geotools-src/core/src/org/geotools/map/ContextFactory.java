package org.geotools.map;

import com.vividsolutions.jts.geom.Envelope;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.geotools.data.DataSource;
import org.geotools.gui.tools.Tool;
import org.geotools.styling.Style;
import org.opengis.cs.CS_CoordinateSystem;

/**
 * An implementation of ContextFactory to be used to construct context
 * classes.  It should not be called directly.  Instead it should be
 * created from ContextFactory, and ContextFactory methods should be
 * called instead.
 */
public abstract class ContextFactory {
    /**
     * The logger 
     */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.tools.ContextFactory");

    private static ContextFactory factory=null;

    /**
     * Create an instance of the factory.
     * @return An instance of ContextFactory, or null if ContextFactory
     * could not be created.
     */
    public static ContextFactory createFactory(){
        if (factory==null){
            String factoryClass = System.getProperty("ContextFactoryImpl");
            LOGGER.fine("loaded property = " + factoryClass);
            if(factoryClass != null && factoryClass != ""){
                factory = createFactory(factoryClass); 
            }
            if (factory==null) {
                factory = createFactory(
                    "org.geotools.map.ContextFactoryImpl");
            }
        }
        return factory;
    }

    /**
     * Create an instance of the factory.
     * @return An instance of the Factory, or null if the Factory could not
     * be created.
     * @param factoryClass The name of the factory, eg:
     * "org.geotools.gui.tools.ContextFactoryImpl".
     */
    public static ContextFactory createFactory(String factoryClass) {
        try{
            return factory = (ContextFactory)Class.forName(
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
     * Create a BoundingBox.
     * @param bbox The extent associated with this class.
     * @param coordinateSystem The coordinate system associated with this
     * class.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public abstract BoundingBox createBoundingBox(
        Envelope bbox,
        CS_CoordinateSystem coordinateSystem)
        throws IllegalArgumentException;

    /**
     * Create a Context.
     * @param bbox The extent associated with this class.
     * @param layerList The list of layers associated with this context.
     * @param title The name of this context.  Must be set.
     * @param _abstract A description of this context.  Optional, set to
     * null if none exists.
     * @param keywords An array of keywords to be used when searching for
     * this context.  Optional, set to null if none exists.
     * @param contactInformation Contact details for the person who created
     * this context.  Optional, set to null if none exists.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public abstract Context createContext(
        BoundingBox bbox,
        LayerList layerList,
        ToolList selectedTool,
        String title,
        String _abstract,
        String[] keywords,
        String contactInformation) throws IllegalArgumentException;
    
    /** Creates a Layer.
     * @param dataSource The dataSource to query in order to get features for
     * this layer.
     * @param style The style to use when rendering features associated with
     * this layer.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public abstract Layer createLayer(
        DataSource dataSource,
        Style style) throws IllegalArgumentException;

    /**
     * Create a LayerList.
     */
    public abstract LayerList createLayerList();

    /**
     * Creates a SelectedTool.
     * @param tool The selected tool.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public abstract ToolList createSelectedTool(Tool tool)
        throws IllegalArgumentException;
}
