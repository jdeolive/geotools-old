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
package org.geotools.renderer;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.feature.Feature;
import org.geotools.styling.Style;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;


/**
 * Renderer draws a map on behalf on MapPane.  It determines what features to
 * draw, BoundingBox, Size, and Style from this.context.
 *
 * @author Cameron Shorter
 * @version $Id: Renderer2D.java,v 1.5 2003/05/04 21:52:31 camerons Exp $
 *
 * @task REVISIT Renderer2D should extend Renderer once Renderer has been
 *       cleaned up.
 */
public interface Renderer2D {
    /**
     * Render features based on the LayerList, BoundBox and Style specified in
     * this.context.
     *
     * @param graphics The graphics object to draw to.
     * @param paintArea The size of the output area in output units (eg:
     *        pixels).
     *
     * @deprecated Use render(Graphics2D AffineTransform) instead.
     */
    void render(Graphics2D graphics, Rectangle paintArea);

    /**
     * Render features based on the LayerList, BoundBox and Style specified in
     * this.context.
     *
     * @param graphics The graphics object to draw to.
     * @param transform A transform which converts World coordinates to Screen
     *        coordinates.
     */
    public void paint(Graphics2D graphics, AffineTransform transform);
}
