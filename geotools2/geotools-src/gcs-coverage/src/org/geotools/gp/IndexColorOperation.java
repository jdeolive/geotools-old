/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
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
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.gp;

// J2SE dependencies
import java.util.Arrays;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.IndexColorModel;

// Java Advanced Imaging
import javax.media.jai.OpImage;
import javax.media.jai.NullOpImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterList;

// Geotools implementation
import org.geotools.gc.GridCoverage;
import org.geotools.cv.SampleDimension;


/**
 * Operation applied only on image's colors. This operation work
 * only for source image using an {@link IndexColorModel}.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
abstract class IndexColorOperation extends Operation {
    /**
     * Construct an operation.
     *
     * @param The name of the operation.
     * @param The parameters descriptors.
     */
    public IndexColorOperation(final String name, final ParameterListDescriptor descriptor) {
        super(name, descriptor);
    }
    
    /**
     * Performs the color transformation. This method invokes the
     * {@link #transformColormap transformColormap(...)} method with
     * current RGB colormap, the source {@link SampleDimension} and the
     * supplied parameters.
     *
     * @param parameters The parameters.
     * @param processor The originating {@link GridCoverageProcessor}
     *        (i.e. the instance that invoked this method).
     */
    protected GridCoverage doOperation(final ParameterList         parameters,
                                       final GridCoverageProcessor processor)
    {
        final GridCoverage source = (GridCoverage) parameters.getObjectParameter("Source");
        final RenderedImage image = source.getRenderedImage(false);
        final ColorModel    model = image.getColorModel();
        if (model instanceof IndexColorModel) {
            final int band = 0; // Always 0 in this implementation.
            final SampleDimension[] bands = source.getSampleDimensions();
            final IndexColorModel  colors = (IndexColorModel) model;
            final int             mapSize = colors.getMapSize();
            final byte[] R=new byte[mapSize]; colors.getReds  (R);
            final byte[] G=new byte[mapSize]; colors.getGreens(G);
            final byte[] B=new byte[mapSize]; colors.getBlues(B);
            transformColormap(R,G,B, bands[band], parameters);
            if (!compare(colors, R,G,B)) {
                final int computeType = (image instanceof OpImage) ?
                ((OpImage)image).getOperationComputeType() :
                    OpImage.OP_COMPUTE_BOUND;
                    final IndexColorModel newModel = new IndexColorModel(colors.getComponentSize()[band], mapSize, R,G,B);
                    final ImageLayout       layout = new ImageLayout().setColorModel(newModel);
                    final RenderedImage   newImage = new NullOpImage(image, layout, null, computeType);
                    return new GridCoverage(source.getName(null), newImage,
                    source.getCoordinateSystem(),
                    source.getEnvelope(),
                    new SampleDimension[] {bands[band]},
                    false,
                    new GridCoverage[] {source},
                    null);
            }
        }
        return source;
    }
    
    /**
     * Transform the supplied RGB colors. This method is automatically invoked
     * by {@link #doOperation(ParameterList)}. The source {@link GridCoverage}
     * has usually only one band; consequently <code>transformColormap</code>
     * is invoked with the {@link SampleDimension} for this band only. The
     * <code>R</code>, <code>G</code> and <code>B</code> arrays contains the
     * RGB values from the current source and should be overriden with new RGB
     * values for the destination image.
     *
     * @param R Red   components to transform.
     * @param G Green components to transform.
     * @param B Blue  components to transform.
     * @param band The sample dimension. This parameter is supplied
     *        for information only. It may be usefull for interpretation of
     *        colormap's index. For example, an implementation could use this
     *        information for transforming only colors at index allocated to
     *        geophysics values.
     * @param parameters The user-supplied parameters.
     */
    protected abstract void transformColormap(final byte[] R,
                                              final byte[] G,
                                              final byte[] B,
                                              final SampleDimension band,
                                              final ParameterList parameters);
    
    /**
     * Check if a color model use the specified RGB components.
     *
     * @param colors  Color map to compare.
     * @param R Red   components to compare.
     * @param G Green components to compare.
     * @param B Blue  components to compare.
     */
    private static boolean compare(final IndexColorModel colors,
                                   final byte[] R,
                                   final byte[] G,
                                   final byte[] B)
    {
        final byte[] array=new byte[colors.getMapSize()];
        colors.getReds  (array); if (!Arrays.equals(array, R)) return false;
        colors.getGreens(array); if (!Arrays.equals(array, G)) return false;
        colors.getBlues (array); if (!Arrays.equals(array, B)) return false;
        return true;
    }
}
