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
import java.util.Arrays;

// JAI dependencies
import javax.media.jai.ParameterList;
import javax.media.jai.operator.BinarizeDescriptor; // For javadoc

// Geotools dependencies
import org.geotools.cv.Category;
import org.geotools.cv.SampleDimension;
import org.geotools.ct.MathTransform1D;
import org.geotools.cs.CoordinateSystem;


/**
 * Wraps any JAI operation producing a bilevel image. An example of such operation is
 * {@link BinarizeDescriptor Binarize}.
 *
 * @version $Id: BilevelOperation.java,v 1.2 2003/04/30 21:58:00 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class BilevelOperation extends OperationJAI {
    /**
     * The sample dimension for the resulting image.
     */
    private static final SampleDimension SAMPLE_DIMENSION = new SampleDimension(new Category[] {
        Category.FALSE,
        Category.TRUE
    }, null);

    /**
     * Construct a bilevel operation.
     *
     * @param name The OpenGIS and JAI name.
     */
    public BilevelOperation(final String name) {
        this(name, name);
    }

    /**
     * Construct a bilevel operation.
     *
     * @param name The OpenGIS name.
     * @param nameJAI The JAI name.
     */
    public BilevelOperation(final String name, final String nameJAI) {
        super(name, getOperationDescriptor(nameJAI), null);
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
        final SampleDimension[] bands = new SampleDimension[bandLists[0].length];
        Arrays.fill(bands, SAMPLE_DIMENSION);
        return bands;
    }
}
