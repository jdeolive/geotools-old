package org.geotools.gui.tools;

//import org.geotools.gui.tools.PanTool;

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
}
