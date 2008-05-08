package org.geotools.utils;

import javax.media.jai.BorderExtender;
import javax.media.jai.Interpolation;

import org.geotools.coverage.processing.operation.FilteredSubsample;
import org.geotools.coverage.processing.operation.Scale;
import org.geotools.coverage.processing.operation.SubsampleAverage;

/**
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * 
 */
public class CoverageToolsConstants {

	/**
	 * Default interpolation.
	 */
	public final static Interpolation DEFAULT_INTERPOLATION = Interpolation
			.getInstance(Interpolation.INTERP_NEAREST);
	/** Default filter for subsampling averaged. */
	public final static float[] DEFAULT_KERNEL_GAUSSIAN = new float[] { 0.5F,
			1.0F / 3.0F, 0.0F, -1.0F / 12.0F };
	/**
	 * Default border extender.
	 */
	public final static BorderExtender DEFAULT_BORDER_EXTENDER = BorderExtender
			.createInstance(BorderExtender.BORDER_COPY);
	public final static FilteredSubsample FILTERED_SUBSAMPLE_FACTORY = new FilteredSubsample();
	public static final SubsampleAverage SUBSAMPLE_AVERAGE_FACTORY = new SubsampleAverage();
	public static final Scale SCALE_FACTORY = new Scale();

	public static final int DEFAULT_INTERNAL_TILE_WIDTH = 512;
	public static final int DEFAULT_INTERNAL_TILE_HEIGHT = 512;
	
	public final static String DEFAULT_COMPRESSION_SCHEME="LZW";
	
	public final static float  DEFAULT_COMPRESSION_RATIO=0.75f;
	/**
	 * Default number of resolution steps.. 
	 */
	public final int DEFAULT_RESOLUTION_STEPS = 5;

}
