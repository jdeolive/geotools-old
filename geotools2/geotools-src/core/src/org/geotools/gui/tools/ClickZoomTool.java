/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.gui.tools;

/**
 * Pan the map so that the new extent has the click point in the middle of the
 * map and then zoom in/out by the zoomFactor.
 *
 * @author Cameron Shorter
 * @version $Id: ClickZoomTool.java,v 1.2 2003/08/20 21:32:13 cholmesny Exp $
 */
public interface ClickZoomTool extends Tool {
    /**
     * The factor to zoom in out by, zoomFactor=0.5 means zoom in, zoomFactor=2
     * means zoom out. Defaults to 2.
     *
     * @param zoomFactor the factor to zoom by.
     */
    void setZoomFactor(double zoomFactor);

    /**
     * The factor to zoom in out by, zoomFactor=0.5 means zoom in, zoomFactor=2
     * means zoom out. Defaults to 2.
     *
     * @return the factor to zoom by.
     */
    double getZoomFactor();
}
