package org.geotools.gui.tools;


/**
 * A tool which provides methods for zooming.
 * @version $Id: ZoomTool.java,v 1.3 2003/04/14 21:37:14 jmacgill Exp $
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
