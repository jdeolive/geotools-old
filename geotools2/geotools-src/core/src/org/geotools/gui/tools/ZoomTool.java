package org.geotools.gui.tools;

import org.geotools.gui.tools.AbstractTool;

/**
 * A tool which provides methods for zooming.
 * @version $Id: ZoomTool.java,v 1.1 2003/03/19 10:37:36 camerons Exp $
 * @author Cameron Shorter
 */
public interface ZoomTool extends AbstractTool {

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
