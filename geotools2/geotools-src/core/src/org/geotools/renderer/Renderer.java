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

import java.awt.Graphics;import org.geotools.feature.Feature;import org.geotools.styling.Style;import com.vividsolutions.jts.geom.Coordinate;import com.vividsolutions.jts.geom.Envelope;

/**
 * This is very much work in progress.
 *
 * @version $Id: Renderer.java,v 1.19 2003/08/03 03:28:15 seangeo Exp $
 * @author James Macgill
 */

public interface Renderer {

    /**
     * Flag which determines if the renderer is interactive or not.
     * An interactive renderer will return rather than waiting for time
     * consuming operations to complete (e.g. Image Loading).
     * A non-interactive renderer (e.g. a SVG or PDF renderer) will block
     * for these operations.
     */
     boolean interactive = true;
    
    /**
     * Renders the provided features using the specified style.
     * The features should fill the viewport but may well extend beyond it.
     * Features should be cropped (if appropriate) to the specified viewport.
     *
     * @task REVSIT: Think more in whether Renderer should be called by Map or
     *               if Map should be called by Renderer
     *
     * @param f The features to render
     * @param viewport The visible extent to be rendered
     * @param style The style definition to apply to each feature
     */
    void render(Feature f[], Envelope viewport, Style style);

    /**
     * Getter for property interactive.
     * @return Value of property interactive.
     */
    boolean isInteractive();
    
    /**
     * Setter for property interactive.
     * @param interactive New value of property interactive.
     */
    void setInteractive(boolean interactive);
    
    /** sets the output graphics for the renderer and the size of the graphic.
     */
    void setOutput(Graphics g, java.awt.Rectangle r);
    
    public Coordinate pixelToWorld(int x, int y, Envelope map);
}

