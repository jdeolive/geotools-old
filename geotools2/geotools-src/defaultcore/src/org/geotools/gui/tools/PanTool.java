package org.geotools.gui.tools;

import javax.swing.JComponent;
import org.geotools.gui.tools.AbstractTool;

/**
 * A tool which provides methods for panning.
 */
public interface PanTool extends AbstractTool {

    /**
     * Set the Widget which sends MouseEvents and contains widget size
     * information.
     * @param widget The widget to get size information from.
     * @throws IllegalStateException if the widget has already been set to
     * another widget.
     */
    public void setWidget(JComponent widget) throws IllegalStateException;
}
