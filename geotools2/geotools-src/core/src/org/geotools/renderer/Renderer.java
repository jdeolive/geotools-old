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
 *     UNITED KINDOM: James Macgill.  j.macgill@geog.leeds.ac.uk
 */
package org.geotools.renderer;

import org.geotools.styling.Style;
import org.geotools.feature.Feature;
import com.vividsolutions.jts.geom.Envelope;

/**
 * this is very much work in progress
 * @author  jamesm
 */
public interface Renderer {

    /**
     * Render the provided features using the specified style.
     * the features should fill the viewport but may well extend beyond it.
     * Features should be cropped (if appropriate) to the specified viewport
     *
     * TODO: Think more in whether Renderer should be called by Map of
     * TODO: if Map should be called by Renderer
     *
     * @param f The features to render
     * @param viewport The visible extent to be rendered
     * @param style The style defenition to apply to each feature
     */
    public void render(Feature f[], Envelope viewport, Style style);

}

