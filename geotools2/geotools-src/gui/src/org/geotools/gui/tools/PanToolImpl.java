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
import java.awt.geom.AffineTransform;
import java.util.logging.Logger;

import org.geotools.ct.Adapters;
import org.geotools.ct.TransformException;
import org.geotools.gui.swing.event.GeoMouseEvent;
import org.geotools.map.MapContext;
import org.geotools.pt.CoordinatePoint;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Provides both Click/Pan and Drap/Pan functionality.
 * Processes MouseEvents on behalf of MapPanel and constructs a
 * CordinateTransform for the map's Context.
 *
 * @author $author$
 * @version $Revision: 1.20 $
 */
public class PanToolImpl extends AbstractTool implements PanTool {
    private static final Logger LOGGER =
        Logger.getLogger("org.geotools.gui.tools.PanToolImpl");
    private Adapters adapters = Adapters.getDefault();
    private AffineTransform at = new AffineTransform();
    /** The Mouse button press down point in a click/drag operation */
    protected CoordinatePoint pressPoint;
    /** The Mouse button release point in a click/drag operation */
    protected CoordinatePoint releasePoint;

    /**
     * Construct a PanTool.
     */
    public PanToolImpl() {
        setName("Pan");
        setCursor(new Cursor(Cursor.MOVE_CURSOR));
    }

    /**
     * Process Click and Drag Pan.
     *
     * @param e The mouse clicked event.
     */
    public void mouseClicked(MouseEvent e) {
        try {
            // The real world coordinates of the mouse click
            CoordinatePoint mousePoint =
                ((GeoMouseEvent) e).getMapCoordinate(null);
            applyZoomTransform(mousePoint, 1);
        } catch (TransformException t) {
            LOGGER.warning(
                "Transform exception prevented mouseClicks from being processed"
            );
        }
    }

    /**
     * Calculate a transform based on the new midpoint and zoomFactor.
     * ZoomFactor will be 1 for Pan, anything for Zoom.
     *
     * @param midPoint The midPoint of the new AreaOfInterest
     * @param inverseZoomFactor The inverse of the zoomFactor to zoom by,
     *        inverseZoomFactor=0.5 means zoom in.
     */
    protected void applyZoomTransform(
        CoordinatePoint midPoint,
        double inverseZoomFactor
    ) {
        Envelope aoi = context.getAreaOfInterest();

        at.setToIdentity();
        at.translate(
            midPoint.getOrdinate(0),
            midPoint.getOrdinate(1)
        );
        at.scale(inverseZoomFactor, inverseZoomFactor);
        at.translate(
            -(aoi.getMinX() + aoi.getMaxX()) / 2,
            -(aoi.getMinY() + aoi.getMaxY()) / 2
        );

        context.transform(at);
    }

    /**
     * Set the press point in a click-drag operation.
     *
     * @param e contains the mouse click.
     */
    public void mousePressed(MouseEvent e) {
        try {
            pressPoint = ((GeoMouseEvent) e).getMapCoordinate(pressPoint);
        } catch (TransformException t) {
            LOGGER.warning(
                "Transform exception prevented mouseClicks from being processed"
            );
        }
    }

    /**
     * Set the release point in a click-drag operation and process the drag
     * operation.
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

            at.setToIdentity();
            at.translate(
                pressPoint.getOrdinate(0) - releasePoint.getOrdinate(0),
                pressPoint.getOrdinate(1) - releasePoint.getOrdinate(1)
            );

            context.transform(at);
        } catch (TransformException t) {
            LOGGER.warning(
                "Transform exception prevented mouseClicks from being processed"
            );
        } 
    }

    /**
     * Register this tool to receive MouseEvents from <code>component</code>.
     *
     * @param component The tool will process mouseEvents from this component.
     * @param context The Context that will be changed by this Tool.
     */
    public void addMouseListener(
        Component component,
        MapContext context
    ) {
        super.addMouseListener(component, context, this);
    }
}
