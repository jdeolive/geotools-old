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
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.gp;

// J2SE dependencies
import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

// Java Advanced Imaging dependencies
import javax.media.jai.JAI;
import javax.media.jai.ImageLayout;
import javax.media.jai.ParameterList;
import javax.media.jai.ParameterListDescriptor;
import javax.media.jai.ParameterListDescriptorImpl;
import javax.media.jai.WritablePropertySource;

// Geotools dependencies
import org.geotools.cv.SampleDimension;
import org.geotools.gc.GridCoverage;
import org.geotools.resources.GCSUtilities;
import org.geotools.resources.ColorUtilities;


/**
 * A grid coverage containing a subset of an other GridCoverage's sample dimensions,
 * and/or a different {@link ColorModel}. A common reason why we want to change the
 * color model is to select a different visible band. Consequently, the "SelectSampleDimension"
 * name still appropriate in this context.
 *
 * @version $Id: SelectSampleDimension.java,v 1.5 2003/08/04 19:07:23 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class SelectSampleDimension extends GridCoverage {
    /**
     * The mapping to bands in the source grid coverage.
     * May be <code>null</code> if all bands were keeped.
     */
    private final int[] bandIndices;

    /**
     * Construct a new <code>SelectSampleDimension</code> grid coverage. This grid coverage will
     * use the same coordinate system and the same geometry than the source grid coverage.
     *
     * @param source      The source coverage.
     * @param image       The image to use.
     * @param bands       The sample dimensions to use.
     * @param bandIndices The mapping to bands in <code>source</code>. Not used
     *                    by this constructor, but keeped for futur reference.
     *
     * @task HACK: It would be nice if we could use always the "BandSelect" operation
     *             without the "Null" one. But as of JAI-1.1.1, "BandSelect" is not
     *             wise enough to detect the case were no copy is required.
     */
    private SelectSampleDimension(final GridCoverage source, final RenderedImage image,
                                  final SampleDimension[] bands, final int[] bandIndices)
    {
        super(source.getName(null),         // The grid source name
              image,                        // The underlying data
              source.getCoordinateSystem(), // The coordinate system.
              source.getGridGeometry().getGridToCoordinateSystem(),
              bands,                        // The sample dimensions
              new GridCoverage[]{source},   // The source grid coverages.
              null);                        // Properties

        this.bandIndices = bandIndices;
        assert bandIndices==null || bandIndices.length==bands.length;
    }

    /**
     * Apply the band select operation to a grid coverage.
     *
     * @param  parameters List of name value pairs for the parameters.
     * @param  A set of rendering hints, or <code>null</code> if none.
     * @return The result as a grid coverage.
     */
    static GridCoverage create(final ParameterList parameters, RenderingHints hints) {
        RenderedImage image;
        GridCoverage source = (GridCoverage)parameters.getObjectParameter("Source");
        int[]   bandIndices = (int[])       parameters.getObjectParameter("SampleDimensions");
        Integer visibleBand = (Integer)     parameters.getObjectParameter("VisibleSampleDimension");
        int        visibleSourceBand;
        int        visibleTargetBand;
        SampleDimension[] sourceBands;
        SampleDimension[] targetBands;
        if (bandIndices != null) {
            bandIndices = (int[]) bandIndices.clone();
        }
        do {
            sourceBands = source.getSampleDimensions();
            targetBands = sourceBands;
            /*
             * Construct an array of target bands.   If the 'bandIndices' parameter contains
             * only "identity" indices (0, 1, 2...), then we will work as if no band indices
             * were provided. It will allow us to use the "Null" operation rather than
             * "BandSelect", which make it possible to avoid to copy raster data.
             */
            if (bandIndices != null) {
                if (bandIndices.length!=sourceBands.length || !isIdentity(bandIndices)) {
                    targetBands = new SampleDimension[bandIndices.length];
                    for (int i=0; i<bandIndices.length; i++) {
                        targetBands[i] = sourceBands[bandIndices[i]];
                    }
                } else {
                    bandIndices = null;
                }
            }
            image             = source.getRenderedImage();
            visibleSourceBand = GCSUtilities.getVisibleBand(image);
            visibleTargetBand = (visibleBand!=null) ? visibleBand.intValue() :
                                (bandIndices!=null) ? bandIndices[visibleSourceBand] :
                                                                  visibleSourceBand;
            if (bandIndices==null && visibleSourceBand==visibleTargetBand) {
                return source;
            }
            if (!(source instanceof SelectSampleDimension)) {
                break;
            }
            /*
             * If the source coverage was the result of an other "BandSelect" operation, go up
             * the chain and checks if an existing GridCoverage could fit. We do that in order
             * to avoid to create new GridCoverage everytime the user is switching the visible
             * band. For example we could change the visible band from 0 to 1, and then come
             * back to 0 later.
             */
            final int[] parentIndices = ((SelectSampleDimension)source).bandIndices;
            if (parentIndices != null) {
                if (bandIndices != null) {
                    for (int i=0; i<bandIndices.length; i++) {
                        bandIndices[i] = parentIndices[bandIndices[i]];
                    }
                } else {
                    bandIndices = (int[])parentIndices.clone();
                }
            }
            final GridCoverage[] sources = source.getSources();
            assert sources.length==1 : sources.length;
            source = sources[0];
        }
        while (true);
        /*
         * Creates the operation. A color model will be defined only if the user didn't
         * specify an explicit one.
         *
         * @task HACK: It would be nice if we could use always the "BandSelect" operation
         *             without the "Null" one. But as of JAI-1.1.1, "BandSelect" is not
         *             wise enough to detect the case were no copy is required.
         */
        String operation = "Null";
        ImageLayout layout = null;
        if (hints != null) {
            layout = (ImageLayout) hints.get(JAI.KEY_IMAGE_LAYOUT);
        }
        if (layout == null) {
            layout = new ImageLayout();
        }
        if (visibleBand!=null || !layout.isValid(ImageLayout.COLOR_MODEL_MASK)) {
            ColorModel colors = image.getColorModel();
            if (colors instanceof IndexColorModel &&
                sourceBands[visibleSourceBand].equals(targetBands[visibleTargetBand]))
            {
                /*
                 * If the source color model was an instance of  IndexColorModel,  reuse
                 * its color mapping. It may not matches the category colors if the user
                 * provided its own color model. We are better to use what the user said.
                 */
                final IndexColorModel indexed = (IndexColorModel) colors;
                final int[] ARGB = new int[indexed.getMapSize()];
                indexed.getRGBs(ARGB);
                colors = ColorUtilities.getIndexColorModel(ARGB, targetBands.length,
                                                                 visibleTargetBand);
            } else {
                colors = targetBands[visibleTargetBand]
                      .getColorModel(visibleTargetBand, targetBands.length);
            }
            layout.setColorModel(colors);
            if (hints != null) {
                hints = (RenderingHints) hints.clone();
                hints.put(JAI.KEY_IMAGE_LAYOUT, layout);
            } else {
                hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
            }
        }
        if (visibleBand==null) {
            visibleBand = new Integer(visibleTargetBand);
        }
        ParameterBlock params = new ParameterBlock().addSource(image);
        if (targetBands != sourceBands) {
            operation = "BandSelect";
            params = params.add(bandIndices);
        }
        image = OperationJAI.getJAI(hints).createNS(operation, params, hints);
        ((WritablePropertySource) image).setProperty("GC_VisibleBand", visibleBand);
        return new SelectSampleDimension(source, image, targetBands, bandIndices);
    }

    /**
     * Returns <code>true</code> if the specified array contains increasing values 0, 1, 2...
     */
    private static boolean isIdentity(final int[] bands) {
        for (int i=0; i<bands.length; i++) {
            if (bands[i] != i) {
                return false;
            }
        }
        return true;
    }

    /**
     * An operation for selecting bands.
     *
     * @version $Id: SelectSampleDimension.java,v 1.5 2003/08/04 19:07:23 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    final static class Operation extends org.geotools.gp.Operation {
        /**
         * Serial number for interoperability with different versions.
         */
        private static final long serialVersionUID = 6889502343896409135L;

        /**
         * Construct a default "SelectSampleDimension" operation.
         */
        public Operation() {
            super("SelectSampleDimension", new ParameterListDescriptorImpl(
                    null,           // the object to be reflected upon for enumerated values
                    new String[] {  // the names of each parameter.
                        "Source",
                        "SampleDimensions",
                        "VisibleSampleDimension"
                    },
                    new Class[] {   // the class type of each parameter.
                        GridCoverage.class,
                        int[].class,
                        Integer.class
                    },
                    new Object[] {  // the default values for each parameter
                        ParameterListDescriptor.NO_PARAMETER_DEFAULT,
                        null,
                        null
                    },
                    new Object[] {  // defines the valid values for each parameter.
                        null,
                        null,
                        RANGE_0
                    }
            ));
        }

        /**
         * Apply the band select operation to a grid coverage.
         *
         * @param  parameters List of name value pairs for the parameters.
         * @param  A set of rendering hints, or <code>null</code> if none.
         * @return The result as a grid coverage.
         */
        protected GridCoverage doOperation(final ParameterList parameters, RenderingHints hints) {
            return SelectSampleDimension.create(parameters, hints);
        }
    }
}
