package org.geotools.gui.tools;

/**
 * Pan the map so that the new extent has the click point in the middle
 * of the map and then zoom in/out by the zoomFactor.
 * @version $Id: ClickZoomTool.java,v 1.1 2003/03/30 10:00:22 camerons Exp $
 * @author Cameron Shorter
 */
public interface ClickZoomTool extends Tool {
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
