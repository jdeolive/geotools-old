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

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.ct.Adapters;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransformFactory;
import org.geotools.ct.TransformException;
import org.geotools.gui.swing.event.GeoMouseEvent;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.pt.CoordinatePoint;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;


/**
 * DOCUMENT ME!
 *
 * @author $author$
 * @version $Revision: 1.16 $
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
        Envelope aoi = context.getBbox().getAreaOfInterest();

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

        MathTransform transform =
            MathTransformFactory.getDefault().createAffineTransform(at);

        context.getBbox().transform(adapters.export(transform));
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

            MathTransform transform =
                MathTransformFactory.getDefault().createAffineTransform(at);

            context.getBbox().transform(adapters.export(transform));
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
        Context context
    ) {
        super.addMouseListener(component, context, this);
    }
}
