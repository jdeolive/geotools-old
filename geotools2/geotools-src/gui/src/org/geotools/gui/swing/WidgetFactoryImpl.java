package org.geotools.gui.swing;
import org.geotools.gui.swing.MapPaneImpl;
import org.geotools.gui.tools.AbstractTool;
import org.geotools.gui.widget.FrameWidget;
import org.geotools.gui.widget.WidgetFactory;
import org.geotools.map.Context;

/**
 * An implementation of WidgetFactory to be used to construct widgets.  This
 * class should not be called directly.  Instead it should be created from
 * WidgetFactory, and WidgetFactory methods should be called instead.
 */
public class WidgetFactoryImpl extends WidgetFactory {

    /**
     * Create an instance of WidgetFactoryImpl.  Note that this constructor
     * should only be called from WidgetFactory.
     */
//    protected WidgetFactoryImpl() {
    public WidgetFactoryImpl() {
    }

    public org.geotools.gui.widget.MapPane createMapPane(
            AbstractTool tool,
            Context context) throws IllegalArgumentException
    {
        return new MapPaneImpl(
            tool,
            context);
    }
    
    /** Create an instance of FrameWidget.
     *
     */
    public FrameWidget createFrameWidget() {
        return new FrameWidgetImpl();
    }
    
}
