/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2003, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
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
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 */
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.awt.Paint;

// JTS dependencies
import com.vividsolutions.jts.geom.sfs.SFSGeometry;

// Geotools dependencies
import org.geotools.styling.Style;
import org.geotools.feature.Feature;
import org.geotools.ct.TransformException;
import org.geotools.renderer.geom.JTSIsoline;


/**
 * A factory creating {@link RenderedLayer}s from {@link Feature}s and {@link Style}s.
 *
 * @version $Id: RenderedLayerFactory.java,v 1.2 2003/05/13 11:00:47 desruisseaux Exp $
 * @author Martin Desruisseaux
 * @author <--- add your name --->
 */
public class RenderedLayerFactory {
    /**
     * Create a rendered layer from the specified feature and style.
     *
     * @param  feature The feature.
     * @param  style   The style to apply.
     * @return The rendered layer for the specified feature and style.
     * @throws TransformException if a transformation was required and failed.
     */
    public RenderedLayer create(final Feature feature, final Style style) throws TransformException
    {
        //
        // Case 1: Draw a geometry collection. This block create an 'isoline' object, which can
        //         contains an arbitrary number of geometric shapes.   All geometries must have
        //         the same 'z' value. For example, an 'isoline' could be the 50 meters isobath.
        //
        final JTSIsoline isoline = new JTSIsoline(0 /* put the z value here */
                                                    /* put the CS here (default to geographic) */);
        for (int i=0; i<5 /* put the number of geometry here */; i++) {
            final SFSGeometry geometry = null; // Get a geometry here. It can be a GeometryCollection.
            isoline.add(geometry);
        }
        final RenderedIsoline layer = new RenderedIsoline(isoline);
        layer.setContour   (null); // Set here a 'Paint' for the contour line.
        layer.setForeground(null); // Set here a 'Paint' for filling polygon.
        layer.setBackground(null); // Set here a 'Paint' for filling polygon holes.
        return layer;
    }
}
