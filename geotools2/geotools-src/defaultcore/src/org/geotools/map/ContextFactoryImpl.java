package org.geotools.map;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.DataSource;
import org.geotools.gui.tools.AbstractTool;
import org.geotools.styling.Style;
import org.opengis.cs.CS_CoordinateSystem;

/**
 * An implementation of ContextFactory to be used to construct context
 * classes.  It should not be called directly.  Instead it should be
 * created from ContextFactory, and ContextFactory methods should be
 * called instead.
 */
public class ContextFactoryImpl extends ContextFactory {

    /**
     * Create an instance of ContextFactoryImpl.  Note that this constructor
     * should only be called from ContextFactory.
     */
    protected void ContextFactoryImpl() {
    }

    /**
     * Create a BoundingBox.
     * @param bbox The extent associated with this class.
     * @param coordinateSystem The coordinate system associated with this
     * class.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public BoundingBox createBoundingBox(
        Envelope bbox,
        CS_CoordinateSystem coordinateSystem)
        throws IllegalArgumentException
    {
        return new BoundingBoxImpl(bbox,coordinateSystem);
    }

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
    public Context createContext(
        BoundingBox bbox,
        LayerList layerList,
        SelectedTool selectedTool,
        String title,
        String _abstract,
        String[] keywords,
        String contactInformation) throws IllegalArgumentException
    {
        return new ContextImpl(
           bbox, 
           layerList,
           selectedTool,
           title,
           _abstract,
           keywords,
           contactInformation);
    }
    
    /** Creates a Layer.
     * @param dataSource The dataSource to query in order to get features for
     * this layer.
     * @param style The style to use when rendering features associated with
     * this layer.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public Layer createLayer(
        DataSource dataSource,
        Style style) throws IllegalArgumentException
    {
        return new LayerImpl(
            dataSource,
            style);
    }

    /**
     * Create a LayerList.
     */
    public LayerList createLayerList() {
        return new LayerListImpl();
    }

    /**
     * Creates a SelectedTool.
     * @param tool The selected tool.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public SelectedTool createSelectedContext(AbstractTool tool)
        throws IllegalArgumentException
    {
        return new SelectedToolImpl(tool);
    }
}
