/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill.  j.macgill@geog.leeds.ac.uk
 */

package org.geotools.renderer;

import com.vividsolutions.jts.geom.Envelope;
import java.awt.geom.AffineTransform;
import java.awt.Graphics;
import java.awt.Rectangle;
import org.geotools.feature.Feature;
import org.geotools.styling.Style;

/**
 * Renderer draws a map on behalf on MapPane.  It determines what
 * features to draw, BoundingBox, Size, and Style from this.context.
 *
 * @version $Id: J2Renderer.java,v 1.1 2003/03/08 02:25:13 camerons Exp $
 * @author James Macgill
 * @author Cameron Shorter
 */

public interface J2Renderer {

//    /**
//     * Flag which determines if the renderer is interactive or not.
//     * An interactive renderer will return rather than waiting for time
//     * consuming operations to complete (e.g. Image Loading).
//     * A non-interactive renderer (e.g. a SVG or PDF renderer) will block
//     * for these operations.
//     */
//     boolean interactive = true;
    
    /**
     * Render features based on the LayerList, BoundBox and Style specified
     * in this.context.
     * @param graphics The graphics object to draw to.
     * @param screenSize The size of the output area in output units
     * (eg: pixels).
     */
    void render(Graphics graphics, Rectangle screenSize);

//    /**
//     * Getter for property interactive.
//     * @return Value of property interactive.
//     */
//    boolean isInteractive();
//
//    /**
//     * Setter for property interactive.
//     * @param interactive New value of property interactive.
//     */
//    void setInteractive(boolean interactive);
    
    /**
     * Return a transform from pixel to Geographic Coordinate Systems.
     * @return The transform.
     * @task REVISIT It might be better to return MathTransform instead of
     * AffineTransform, however this will create a dependance on the ct
     * modules which would be good to avoid.
     */
    public AffineTransform getDotToCoordinateSystem();
}

