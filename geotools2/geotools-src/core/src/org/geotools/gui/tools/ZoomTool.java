package org.geotools.gui.tools;

import org.geotools.gui.tools.Tool;

/**
 * A tool which provides methods for zooming.
 * @version $Id: ZoomTool.java,v 1.2 2003/03/29 22:32:55 camerons Exp $
 * @author Cameron Shorter
 */
public interface ZoomTool extends Tool {

    /**
     * The factor to zoom in out by, zoomFactor=0.5 means zoom in,
     * zoomFactor=2 means zoom out. Defaults to 2.
     */
    public void setZoomFactor(double zoomFactor);

    /**
     * The factor to zoom in out by, zoomFactor=0.5 means zoom in,
     * zoomFactor=2 means zoom out. Defaults to 2.
     */
    public double getZoomFactor();
}
