/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;

// JAI dependencies
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.operator.BandCombineDescriptor; // For Javadoc

// Geotools dependencies
import org.geotools.cv.SampleDimension;
import org.geotools.gp.jai.CombineDescriptor;


/**
 * The "Combine" operation. This operation can be simplified to a JAI's
 * "{@linkplain BandCombineDescriptor BandCombine}" if the operation has
 * only one source.
 *
 * @version $Id: CombineOperation.java,v 1.4 2003/11/12 14:13:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class CombineOperation extends PolyadicOperation {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 5007404929716610690L;

    /**
     * Small number for floating-point comparaisons.
     */
    private static final double EPS = 1E-6;
    
    /**
     * Creates a new instance of <code>CombineOperation</code>
     */
    public CombineOperation() {
        super(CombineDescriptor.OPERATION_NAME);
    }

    /**
     * Apply the JAI operation. If the operation has only one source and no transform, then
     * this method maps directly to a JAI "{@linkplain BandCombineDescriptor BandCombine}"
     * operation.
     *
     * @param parameters The parameters to be given to JAI.
     * @param hints The rendering hints to be given to JAI.
     *
     * @task TODO: More optimisations could be done for cases simplifiable
     *             to "MultiplyConst", "Add" or "Subtract" operation.
     */
    protected RenderedImage createRenderedImage(final ParameterBlockJAI parameters,
                                                final RenderingHints    hints)
    {
        // TODO: Remove source image with 0 coefficient.
        final Object transform = parameters.getObjectParameter("transform");
        if (transform == null) {
            switch (parameters.getNumSources()) {
                case 1: {
                    // TODO: Check for application of "MultiplyConst" operation.
                    return getJAI(hints).createNS("BandCombine", parameters, hints);
                }
                case 2: {
                    // TODO: Check for application of "Add" or "Subtract" operations.
                    //       C={+1, -1}: Subtract(S0,S1)
                    //       C={-1, +1}: Subtract(S1,S0)
                    //       C={+1, +1}: Add     (S0,S1)
                }
            }
        }
        return super.createRenderedImage(parameters, hints);
    }

    /**
     * Derives the {@link SampleDimension}s for the destination image. If this
     * <code>"Combine"</code> operation is used for computing a linear interpolation
     * between two images, then the destination image will use the same color model
     * and sample dimensions than sources images. More specifically, if the following
     * conditions are meet:
     *
     * <ul>
     *   <li>For all destination band computed from <code>dest.band[i]</code> =
     *       <var>C</var><sub>0</sub>&times;<code>source[0].band[0]</code> +
     *       <var>C</var><sub>1</sub>&times;<code>source[0].band[1]</code> + ... +
     *       <var>C</var><sub>n</sub>&times;<code>source[0].band[n]</code> +
     *       <var>C</var><sub>n+1</sub>&times;<code>source[1].band[0]</code> + etc...,
     *       the sum <var>C</var><sub>0</sub> + ... + <var>C</var><sub>n</sub> + etc...
     *       is equals to 1.</li>
     *   <li>There is no additive constant (i.e. the last column in the matrix is 0).</li>
     * </ul>
     *
     * Then the source color model is reused. If all sources don't use the same color model,
     * the one with the highest coefficient (is absolute value) will be used.
     *
     * @task TODO: We could make this method more general if we compute destination categories
     *             ranges from source categories ranges and the coefficients. May be done in the
     *             <code>deriveCategories</code> method.
     */
    protected SampleDimension[] deriveSampleDimension(final SampleDimension[][] bandLists,
                                                      final Parameters         parameters)
    {
        final double[][] matrix = (double[][]) parameters.parameters.getObjectParameter("matrix");
        final SampleDimension[] destBands = new SampleDimension[matrix.length];
        for (int destBand=0; destBand<destBands.length; destBand++) {
            final double[] coefficients = matrix[destBand];
            double sum = coefficients[coefficients.length-1];
            double max = sum;
            if (sum != 0) {
                // NOTE: Comment out this line for relaxing the "no additive constant" criterion.
                return super.deriveSampleDimension(bandLists, parameters);
            }
            int sourceBandCumul = 0;
            for (int sourceImage=0; sourceImage<bandLists.length; sourceImage++) {
                final SampleDimension[] sourceBands = bandLists[sourceImage];
                for (int sourceBand=0; sourceBand<sourceBands.length; sourceBand++) {
                    double coeff = coefficients[sourceBandCumul++];
                    sum += coeff;
                    coeff = Math.abs(coeff);
                    if (coeff > max) {
                        max = coeff;
                        destBands[destBand] = sourceBands[sourceBand];
                    }
                }
            }
            assert sourceBandCumul == coefficients.length-1 : sourceBandCumul;
            if (!(Math.abs(1-sum) <= EPS) || destBands[destBand]==null) {
                return super.deriveSampleDimension(bandLists, parameters);
            }
        }
        return destBands;
    }
}
