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

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import org.geotools.ct.TransformException;
import org.geotools.gui.swing.event.GeoMouseEvent;
import org.geotools.map.MapContext;
import org.geotools.pt.CoordinatePoint;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Provides both Click/Zoom and Drag/Zoom functionality. Processes MouseEvents
 * on behalf of MapPane and constructs a CordinateTransform for the map's Context.<br>
 * For Click/Zoom, pan the map so that the new extent has the click point in
 * the middle of the map and then zoom in/out by the zoomFactor.<br>
 * For Drag/Zoom, pan to the center of the dragged area and zoom in to include
 * that area.
 *
 * @author Cameron Shorter
 * @version $Id: ZoomToolImpl.java,v 1.13 2003/12/23 17:21:02 aaime Exp $
 */
public class ZoomToolImpl extends PanToolImpl implements ZoomTool {
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.tools.ZoomToolImpl");

    /**
     * The factor to zoom in/out by when using click/zoom. zoomFactor=0.5 means
     * zoom in, zoomFactor=2 means zoom out. Defaults to 2.
     */
    private double inverseZoomFactor = 0.5;

    /**
     * Construct a ZoomTool.
     */
    public ZoomToolImpl() {
        setName("Zoom");
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    /**
     * Construct a ZoomTool. /
     *
     * @param zoomFactor the factor to zoom in/out by, zoomFactor=2 means
     *        zoom in, zoomFactor=0.5 means zoom out.
     */
    public ZoomToolImpl(double zoomFactor) {
        this.inverseZoomFactor = 1 / zoomFactor;

        if (zoomFactor == 1) {
            setName("Pan");
            setCursor(new Cursor(Cursor.MOVE_CURSOR));
        } else if (zoomFactor > 1) {
            setName("Zoom In");
            setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        } else {
            setName("Zoom Out");
            setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        }
    }

    /**
     * Set up Click/Zoom. Pan the map so that the new extent has the click
     * point in the middle of the map and then zoom in/out by the zoomFactor.
     *
     * @param e The mouse clicked event.
     */
    public void mouseClicked(MouseEvent e) {
        try {
            // The real world coordinates of the mouse click
            releasePoint = ((GeoMouseEvent) e).getMapCoordinate(releasePoint);
            applyZoomTransform(releasePoint, inverseZoomFactor);
        } catch (TransformException t) {
            LOGGER.warning(
                "Transform exception prevented mouseClicks from being processed");
        }
    }

    /**
     * Set the release point in a click-drag operation and process the drag
     * operation.  The new areaOfInterest will be centered round the middle of
     * the dragged area and zoomed in to include the all the dragged box. The
     * aspect ratio of the old area of interest is maintained.  That means,
     * you cannot cause the map to become skinny or fat.
     *
     * @param e contains the mouse click.
     */
    public void mouseReleased(MouseEvent e) {
        try {
            releasePoint = ((GeoMouseEvent) e).getMapCoordinate(releasePoint);

            // Don't process mouse drag if this is a mouse click.
            if (releasePoint.equals(pressPoint)) {
                return;
            }

            // Calculate midpoint of the zoom rectangle.
            CoordinatePoint midpoint = new CoordinatePoint((pressPoint.getOrdinate(
                        0) + releasePoint.getOrdinate(0)) / 2,
                    (pressPoint.getOrdinate(1) + releasePoint.getOrdinate(1)) / 2);

            // Calculate the inverseZoomFactor
            // zoomFactor=min(zoomWidth/displayWidth,zoomHeight/displayHeight)
            Envelope aoi = context.getAreaOfInterest();
            double izf = Math.max(Math.abs(pressPoint.getOrdinate(0) -
                        releasePoint.getOrdinate(0)) / (aoi.getMaxX() -
                    aoi.getMinX()),
                    Math.abs(pressPoint.getOrdinate(1) -
                        releasePoint.getOrdinate(1)) / (aoi.getMaxY() -
                    aoi.getMinY()));

            applyZoomTransform(midpoint, izf);
        } catch (TransformException t) {
            LOGGER.warning(
                "Transform exception prevented mouseClicks from being processed");
        }
    }

    /**
     * Register this tool to receive MouseEvents from <code>component</code>.
     *
     * @param component The tool will process mouseEvents from this component.
     * @param context The Context that will be changed by this Tool.
     */
    public void addMouseListener(Component component, MapContext context) {
        super.addMouseListener(component, context, this);
    }

    /**
     * The factor to zoom in out by when processing mouseClicks. zoomFactor=0.5
     * means zoom in, zoomFactor=2 means zoom out. Defaults to 2.
     *
     * @param zoomFactor The factor to zoom in/out by.
     */
    public void setZoomFactor(double zoomFactor) {
        this.inverseZoomFactor = 1 / zoomFactor;
    }

    /**
     * The factor to zoom in out by, zoomFactor=0.5 means zoom in, zoomFactor=2
     * means zoom out. Defaults to 2.
     *
     * @return The factor to zoom in/out by.
     */
    public double getZoomFactor() {
        return 1 / inverseZoomFactor;
    }
}
