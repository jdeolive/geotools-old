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
package org.geotools.gc;

// J2SE dependencies
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;
import java.awt.image.RenderedImage;

// JAI dependencies
import javax.media.jai.Histogram;

// Geotools dependencies
import org.geotools.cv.SampleDimension;
import org.geotools.cv.SampleDimensionType;
import org.geotools.cv.ColorInterpretation;
import org.geotools.resources.gcs.Resources;
import org.geotools.resources.gcs.ResourceKeys;


/**
 * Describes the band values for a grid coverage.
 *
 * @version $Id: GridSampleDimension.java,v 1.6 2003/03/14 12:35:48 desruisseaux Exp $
 * @author <A HREF="www.opengis.org">OpenGIS</A>
 * @author Martin Desruisseaux
 */
final class GridSampleDimension extends SampleDimension {
    /**
     * Band number for this sample dimension.
     */
    private final int band;

    /**
     * The number of bands in the {@link GridCoverage} who own this sample dimension.
     */
    private final int numBands;

    /**
     * The grid value data type.
     */
    private final SampleDimensionType type;
    
    /**
     * Construct a sample dimension with a set of categories from an other sample dimension.
     *
     * @param band  The originating sample dimension.
     * @param image The image to be wrapped by {@link GridCoverage}.
     * @param bandNumber The band number.
     */
    private GridSampleDimension(final SampleDimension band,
                                final RenderedImage   image,
                                final int             bandNumber)
    {
        super(band);
        final SampleModel model = image.getSampleModel();
        this.band     = bandNumber;
        this.numBands = model.getNumBands();
        this.type     = SampleDimensionType.getEnum(model, bandNumber);
    }

    /**
     * Create a set of sample dimensions for the given image. The array length of both
     * arguments must matches the number of bands in the supplied <code>image</code>.
     *
     * @param  image The image for which to create a set of sample dimensions.
     * @param  src   User-provided sample dimensions, or <code>null</code> if none.
     * @param  dst   The array where to put sample dimensions.
     * @return <code>true</code> if all sample dimensions are geophysics, or <code>false</code>
     *         if all sample dimensions are <strong>not</strong> geophysics.
     * @throws IllegalArgumentException if geophysics and non-geophysics dimensions are mixed.
     */
    public static boolean create(final RenderedImage   image,
                                 final SampleDimension[] src,
                                 final SampleDimension[] dst)
    {
        final int numBands = image.getSampleModel().getNumBands();
        if (src!=null && src.length!=numBands) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_NUMBER_OF_BANDS_MISMATCH_$2,
                    new Integer(numBands), new Integer(src.length)));
        }
        if (dst.length != numBands) {
            throw new IllegalArgumentException(Resources.format(
                    ResourceKeys.ERROR_NUMBER_OF_BANDS_MISMATCH_$2,
                    new Integer(numBands), new Integer(dst.length)));
        }
        int nGeo = 0;
        int nInt = 0;
        for (int i=0; i<numBands; i++) {
            SampleDimension sd = (src!=null) ? src[i] : null;
            sd = new GridSampleDimension(sd, image, i);
            dst[i] = sd;
            if (sd.geophysics(true ) == sd) nGeo++;
            if (sd.geophysics(false) == sd) nInt++;
        }
        if (nGeo == numBands) {
            return true;
        }
        if (nInt == numBands) {
            return false;
        }
        throw new IllegalArgumentException(Resources.format(ResourceKeys.ERROR_MIXED_CATEGORIES));
    }

    /**
     * Returns a code value indicating grid value data type.
     * This will also indicate the number of bits for the data type.
     *
     * @return a code value indicating grid value data type.
     */
    public SampleDimensionType getSampleDimensionType() {
        return type;
    }
    
    /**
     * Returns the color interpretation of the sample dimension.
     */
    public ColorInterpretation getColorInterpretation() {
        return ColorInterpretation.getEnum(getColorModel(band, numBands), band);
    }

    /**
     * Returns a color model for this sample dimension.
     */
    public ColorModel getColorModel() {
        return getColorModel(band, numBands);
    }
    
    /**
     * Returns the minimum value occurring in this sample dimension.
     */
    //  public double getMinimumValue()
    //  {return getHistogram().getLowValue(band);}
    
    /**
     * Returns the maximum value occurring in this sample dimension.
     */
    //  public double getMaximumValue()
    //  {return getHistogram().getHighValue(band);}
    
    /**
     * Determine the mode grid value in this sample dimension.
     */
    //  public double getModeValue()
    //  {throw new UnsupportedOperationException("Not implemented");}
    
    /**
     * Determine the median grid value in this sample dimension.
     */
    //  public double getMedianValue()
    //  {throw new UnsupportedOperationException("Not implemented");}
    
    /**
     * Determine the mean grid value in this sample dimension.
     */
    //  public double getMeanValue()
    //  {return getHistogram().getMean()[band];}
    
    /**
     * Determine the standard deviation from the mean
     * of the grid values in a sample dimension.
     */
    //  public double getStandardDeviation()
    //  {return getHistogram().getStandardDeviation()[band];}
    
    /**
     * Gets the histogram for the underlying grid coverage.
     */
    //  private Histogram getHistogram()
    //  {throw new UnsupportedOperationException("Not implemented");}
}
