/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
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
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.renderer;

// J2SE dependencies
import java.awt.geom.Point2D;


/**
 * An interface for viewers that may be deformed by some artefacts. For example the
 * {@link org.geotools.gui.swing.ZoomPane} viewer  is capable to show a {@linkplain
 * org.geotools.gui.swing.ZoomPane#setMagnifierVisible magnifying glass}  on top of
 * the usual content. The presence of a magnifying glass deforms the viewer in that
 * the apparent position of pixels within the glass are moved. The interface allows
 * for corrections of apparent pixel position in order to get the position we would
 * have if no deformations existed.
 *
 * @version $Id: DeformableViewer.java,v 1.2 2003/05/13 11:00:45 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public interface DeformableViewer {
    /**
     * Corrects a pixel's coordinates for removing the effect of the any kind of deformations.
     * An example of deformation is the zoom pane's {@linkplain
     * org.geotools.gui.swing.ZoomPane#setMagnifierVisible magnifying glass}. Without this
     * method,  transformations from pixels to geographic coordinates would not give exact
     * results for pixels inside the magnifier since the magnifier moves the pixel's apparent
     * position. Invoking this method will remove any deformation effects using the following
     * steps:
     * <ul>
     *   <li>If the pixel's coordinate <code>point</code> is outside deformed areas (for example
     *       outside the magnifier), then this method do nothing.</li>
     *   <li>Otherwise, if the pixel's coordinate is inside some area that has been deformed,
     *       then this method update <code>point</code> in such a way that it contains the
     *       position that the exact same pixel would have in the absence of deformations.</li>
     * </ul>
     *
     * @param point In input, a pixel's coordinate as it appears on the screen.
     *              In output, the coordinate that the same pixel would have if
     *              the deformation wasn't presents.
     */
    public abstract void correctApparentPixelPosition(final Point2D point);
}
