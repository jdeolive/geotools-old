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

import java.awt.Graphics;

import org.geotools.feature.FeatureCollection;
import org.geotools.styling.Style;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * Base interface for renderer. This is very much work in progress.
 * <strong>Note: this interface will changes in future versions.</strong>
 *
 * @version $Id: Renderer.java,v 1.22 2003/12/23 17:20:06 aaime Exp $
 * @author James Macgill
 */
public interface Renderer {

    /**
     * Flag which determines if the renderer is interactive or not.
     * An interactive renderer will return rather than waiting for time
     * consuming operations to complete (e.g. Image Loading).
     * A non-interactive renderer (e.g. a SVG or PDF renderer) will block
     * for these operations.
     *
     * @deprecated This flag should not be there.
     */
     boolean interactive = true;
    
    /**
     * Renders the provided features using the specified style.
     * The features should fill the viewport but may well extend beyond it.
     * Features should be cropped (if appropriate) to the specified viewport.
     *
     * @param fc The feature collection to render
     * @param viewport The visible extent to be rendered
     * @param style The style definition to apply to each feature
     */
    void render(FeatureCollection fc, Envelope viewport, Style style);

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
