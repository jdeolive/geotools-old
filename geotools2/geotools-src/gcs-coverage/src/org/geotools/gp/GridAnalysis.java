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

// Geotools dependencies
import org.geotools.gc.GridCoverage;


/**
 * Performs various analysis operations on a grid coverage.
 * Note: this class is not yet implemented.
 *
 * @version $Id: GridAnalysis.java,v 1.3 2003/05/13 10:59:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
abstract class GridAnalysis extends GridCoverage {
    /**
     * Construct a grid analysis for the specified coverage.
     */
    public GridAnalysis(final GridCoverage coverage) {
        super(coverage);
    }
    
    /**
     * Returns the minimum value occurring in a sample dimension.
     */
    public abstract double getMinimumValue(int sampleDimension);
    
    /**
     * Returns the maximum value occurring in a sample dimension.
     */
    public abstract double getMaximumValue(int sampleDimension);
    
    /**
     * Determine the mode grid value in a sample dimension.
     */
    public abstract double getModeValue(int sampleDimension);
    
    /**
     * Determine the median grid value in a sample dimension.
     */
    public abstract double getMedianValue(int sampleDimension);
    
    /**
     * Determine the mean grid value in a sample dimension.
     */
    public abstract double getMeanValue(int sampleDimension);
    
    /**
     * Determine the standard deviation from the mean
     * of the grid values in a sample dimension.
     */
    public abstract double getStandardDeviation(int sampleDimension);
    
    /*
     * Determine the histogram of grid values for this coverage.
     */
/*  public Histogram getHistogram(final boolean geophysics)
    {
        final List<SampleDimension> sampleDimensions = getSampleDimensions();
        final int    dimension = sampleDimensions.size();
        final double[] minimum = new double[dimension];
        final double[] maximum = new double[dimension];
        Arrays.fill(minimum, Double.POSITIVE_INFINITY);
        Arrays.fill(maximum, Double.NEGATIVE_INFINITY);
        for (int i=0; i<dimension; i++)
        {
            final CategoryList categories = sampleDimensions.get(i).getCategoryList();
            if (categories!=null)
            {
                final Range range = categories.getRange(geophysics);
                if (range!=null)
                {
                    minimum[i] = ((Number)range.getMinValue()).doubleValue();
                    maximum[i] = ((Number)range.getMaxValue()).doubleValue();
                }
            }
        }
        // TODO
        return null;
    }
 */
    /*
     * Determine the histogram of grid values for this coverage.
     *
     * @param  miniumEntryValue Minimum value stored in the first histogram entry.
     * @param  maximumEntryValue Maximum value stored in the last histogram entry.
     * @param  numberEntries Number of entries in the histogram.
     * @return The histogram.
     */
    //  public Histogram getHistogram(double minimumEntryValue, double maximumEntryValue, int numberEntries)
    //  {return null;}
}
