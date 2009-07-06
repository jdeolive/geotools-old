package org.geotools.coverage.io.range;

import java.util.Set;

import javax.media.jai.Histogram;

import org.geotools.coverage.io.metadata.MetadataNode;
import org.geotools.util.Range;
import org.opengis.annotation.Extension;
import org.opengis.feature.type.Name;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

public abstract class Band {
	
	
	public abstract Histogram getHistogram();
	
	/**
	 * TODO add formal parameters
	 * @return
	 */
	public abstract Histogram getApproximatedHistogram();
	
	public abstract Range<Double> getRange();
	
	/**
	 * TODO add formal parameters
	 * @return
	 */
	public abstract double[] getApproximatedExtrema();
	
	public abstract BandType getType();

	/**
	 * Values to indicate no data values for the sample dimension.
	 * For low precision sample dimensions, this will often be no data values.
	 *
	 * @return The values to indicate no data values for the sample dimension.
	 *
	 * @see #getMinimumValue
	 * @see #getMaximumValue
	 */
	public abstract double[] getNoDataValues();

	public abstract MetadataNode getMetadata(String metadataDomain,final ProgressListener listener);

	public abstract Name getName() ;

	/**
	 * Sample dimension title or description.
	 * This string may be null or empty if no description is present.
	 *
	 * @return A description for this sample dimension.
	 */
	public abstract InternationalString getDescription();

	/**
	 * The transform which is applied to grid values for this sample dimension.
	 * This transform is often defined as
	 * <var>y</var> = {@linkplain #getOffset offset} + {@link #getScale scale}&times;<var>x</var> where
	 * <var>x</var> is the grid value and <var>y</var> is the geophysics value.
	 * However, this transform may also defines more complex relationship, for
	 * example a logarithmic one. In order words, this transform is a generalization of
	 * {@link #getScale}, {@link #getOffset} and {@link #getNoDataValues} methods.
	 *
	 * @return The transform from sample to geophysics values, or {@code null} if
	 *         it doesn't apply.
	 *
	 * @see #getScale
	 * @see #getOffset
	 * @see #getNoDataValues
	 */
	public abstract MathTransform1D getSampleTransformation();

	/**
	 * TODO
	 * 
	 * @param metadataDomain
	 * @return
	 */
	public MetadataNode getMetadata(String metadataDomain){
		return null;
		
	}
	
}
