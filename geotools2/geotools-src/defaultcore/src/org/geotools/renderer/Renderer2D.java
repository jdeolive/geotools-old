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
 * @version $Id: Renderer2D.java,v 1.2 2003/04/26 03:17:39 camerons Exp $
 * @author James Macgill
 * @author Cameron Shorter
 */

public interface Renderer2D {    
    /**
     * Render features based on the LayerList, BoundBox and Style specified
     * in this.context.
     * @param graphics The graphics object to draw to.
     * @param screenSize The size of the output area in output units
     * (eg: pixels).
     */
    void render(Graphics graphics, Rectangle screenSize);
}

