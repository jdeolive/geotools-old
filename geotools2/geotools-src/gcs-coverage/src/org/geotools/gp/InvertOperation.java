/*
 * Geotools - OpenSource mapping toolkit
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
package org.geotools.gp;

// J2SE dependencies
import java.awt.image.DataBuffer;

// JAI dependencies
import javax.media.jai.ParameterList;

// Geotools dependencies
import org.geotools.gc.GridCoverage;
import org.geotools.cv.SampleDimension;
import org.geotools.ct.MathTransform1D;
import org.geotools.cs.CoordinateSystem;


/**
 * The "Invert" operation.
 *
 * @version $Id: InvertOperation.java,v 1.1 2003/04/17 13:57:38 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class InvertOperation extends OperationJAI {
    /**
     * Construct the "Invert" operation.
     */
    public InvertOperation() {
        super("Invert");
    }

    /**
     * Derive the {@link SampleDimension}s for the destination image.
     *
     * @param  bandLists Sample dimensions for each band in each source coverages.
     * @param  cs The coordinate system of the destination grid coverage.
     * @param  parameters The user-supplied parameters.
     * @return The category lists for each band in the destination image.
     */
    protected SampleDimension[] deriveSampleDimension(final SampleDimension[][] bandLists,
                                                      final CoordinateSystem cs,
                                                      final ParameterList parameters)
    {
        final int type = ((GridCoverage) parameters.getObjectParameter("Source"))
                         .getRenderedImage().getSampleModel().getDataType();
        final SampleDimension[] bands = bandLists[0];
        if (type!=DataBuffer.TYPE_BYTE && type!=DataBuffer.TYPE_SHORT) {
            for (int i=0; i<bands.length; i++) {
                bands[i] = bands[i].rescale(-1, 0);
            }
        }
        return bands;
    }
}
