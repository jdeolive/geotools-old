/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
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
import java.awt.RenderingHints;
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
import org.geotools.resources.GCSUtilities;
import org.geotools.resources.ImageUtilities;


/**
 * Operation applied only on image's colors. This operation work
 * only for source image using an {@link IndexColorModel}.
 *
 * @version $Id: IndexColorOperation.java,v 1.10 2003/05/13 10:59:52 desruisseaux Exp $
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
     * @param hints Rendering hints (ignored in this implementation).
     */
    protected GridCoverage doOperation(final ParameterList  parameters,
                                       final RenderingHints hints)
    {
        final GridCoverage     source = (GridCoverage) parameters.getObjectParameter("Source");
        final GridCoverage     visual = source.geophysics(false);
        final RenderedImage     image = visual.getRenderedImage();
        final SampleDimension[] bands = visual.getSampleDimensions();
        final int         visibleBand = GCSUtilities.getVisibleBand(image);
        ColorModel model = image.getColorModel();
        boolean colorChanged = false;
        for (int i=0; i<bands.length; i++) {
            SampleDimension band = bands[i];
            final ColorModel candidate = (i==visibleBand) ? image.getColorModel()
                                                          :  band.getColorModel();
            if (!(candidate instanceof IndexColorModel)) {
                /*
                 * Source don't use an index color model.
                 */
                // TODO: localize this message.
                throw new IllegalArgumentException("Current implementation requires IndexColorModel");
            }
            final IndexColorModel  colors = (IndexColorModel) candidate;
            final int             mapSize = colors.getMapSize();
            final int[]              ARGB = new int[mapSize];
            colors.getRGBs(ARGB);
            band = transformColormap(ARGB, i, band, parameters);
            if (!bands[i].equals(band)) {
                bands[i]     = band;
                colorChanged = true;
            } else if (!colorChanged) {
                final int[] original = new int[mapSize];
                colors.getRGBs(original);
                colorChanged = Arrays.equals(original, ARGB);
            }
            if (i==visibleBand) {
                model = ImageUtilities.getIndexColorModel(ARGB, bands.length, visibleBand);
            }
        }
        if (!colorChanged) {
            return source;
        }
        final int computeType = (image instanceof OpImage) ?
                ((OpImage)image).getOperationComputeType() : OpImage.OP_COMPUTE_BOUND;

        final ImageLayout       layout = new ImageLayout().setColorModel(model);
        final RenderedImage   newImage = new NullOpImage(image, layout, null, computeType);
        GridCoverage target = new GridCoverage(visual.getName(null), newImage,
                                               visual.getCoordinateSystem(),
                                               visual.getGridGeometry().getGridToCoordinateSystem(),
                                               bands,
                                               new GridCoverage[] {visual},
                                               null);
        if (source != visual) {
            target = target.geophysics(true);
        }
        return target;
    }
    
    /**
     * Transform the supplied RGB colors. This method is automatically invoked by
     * {@link #doOperation(ParameterList)} for each band in the source {@link GridCoverage}.
     * The <code>ARGB</code> array contains the ARGB values from the current source and should
     * be overriden with new ARGB values for the destination image.
     *
     * @param ARGB Alpha, Red, Green and Blue components to transform.
     * @param band The band number, from 0 to the number of bands in the image -1.
     * @param sampleDimension The sample dimension of band <code>band</code>.
     * @param parameters The user-supplied parameters.
     * @return A sample dimension identical to <code>sampleDimension</code> except for
     *         the colors. Subclasses may conservatively returns <code>sampleDimension</code>.
     */
    protected abstract SampleDimension transformColormap(final int[] ARGB,
                                                         final int   band,
                                                         final SampleDimension sampleDimension,
                                                         final ParameterList   parameters);
}
