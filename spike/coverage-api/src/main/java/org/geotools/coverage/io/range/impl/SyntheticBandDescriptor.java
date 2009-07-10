package org.geotools.coverage.io.range.impl;

import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageTypeSpecifier;
import javax.measure.quantity.Quantity;

import org.geotools.coverage.io.range.AxisBin;
import org.geotools.coverage.io.range.BandDescriptor;
import org.geotools.feature.NameImpl;
import org.geotools.util.NumberRange;
import org.geotools.util.SimpleInternationalString;
import org.opengis.coverage.SampleDimensionType;
import org.opengis.referencing.operation.MathTransform1D;
/**
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class SyntheticBandDescriptor extends BandDescriptor<DigitalNumber> {
	
	/**
	 * 
	 * @author Simone Giannecchini, GeoSolutions SAS
	 *
	 */
	public static class SyntheticBandDescriptorBuilder{
		
		private double[] defaultNoDatavalues;
		
		private NumberRange<Double> defaultRange;
		
		private MathTransform1D defaultSampleTransformation;
				
		private Set<SampleDimensionType> defaultSampleDimensionTypes;

		private  List<? extends AxisBin <?, ? extends Quantity>> defaultAxisBins;

		public SyntheticBandDescriptorBuilder() {
		}
		
		public SyntheticBandDescriptorBuilder setDefaultNoDatavalues(final double[] defaultNoDatavalues){
			this.defaultNoDatavalues=defaultNoDatavalues;	
			return this;
		}

		public SyntheticBandDescriptorBuilder setDefaultRange(NumberRange<Double> defaultRange) {
			this.defaultRange = defaultRange;
			return this;
		}

		public SyntheticBandDescriptorBuilder setDefaultSampleTransformation(
				MathTransform1D defaultSampleTransformation) {
			this.defaultSampleTransformation = defaultSampleTransformation;
			return this;
		}

		public SyntheticBandDescriptorBuilder setDefaultSampleDimensionTypes(
				Set<SampleDimensionType> defaultSampleDimensionTypes) {
			this.defaultSampleDimensionTypes = defaultSampleDimensionTypes;
			return this;
		}

		public SyntheticBandDescriptorBuilder setDefaultAxisBins(
				List<? extends AxisBin<?, ? extends Quantity>> defaultAxisBins) {
			this.defaultAxisBins = defaultAxisBins;
			return this;
		}
		
		public SyntheticBandDescriptorBuilder setDefaultAxisBins(final ImageTypeSpecifier it) {
			this.defaultAxisBins = IMAGE_BAND_UTILITIES.getBinsFromRenderedImage(it);
			return this;
		}
		
		public SyntheticBandDescriptorBuilder setDefaultAxisBins(final RenderedImage ri) {
			this.defaultAxisBins = IMAGE_BAND_UTILITIES.getBinsFromRenderedImage(ri);
			return this;
		}
		
		public SyntheticBandDescriptorBuilder setDefaultAxisBins(final ColorModel cm, final SampleModel sm) {
			this.defaultAxisBins = IMAGE_BAND_UTILITIES.getBinsFromRenderedImage(cm,sm);
			return this;
		}

		public SyntheticBandDescriptor build(){
			return new SyntheticBandDescriptor(
					this.defaultNoDatavalues,
					this.defaultRange,
					this.defaultSampleTransformation,
					this.defaultSampleDimensionTypes,
					this.defaultAxisBins);
		}
		
	}
	

	public SyntheticBandDescriptor(
			final double[] defaultNoDatavalues, 
			final NumberRange<Double> defaultRange,
			final MathTransform1D defaultSampleTransformation, 
			final Set<SampleDimensionType> sampleDimensionTypes, 
			final List<? extends AxisBin <?, ? extends Quantity>> defaultAxisBins) {
		super(
				new DigitalNumber(), 
				BandInterpretation.SYNTHETIC_VALUE, 
				defaultNoDatavalues, 
				defaultRange,
				defaultSampleTransformation, 
				new NameImpl("ImageSyntheticBand"), 
				new SimpleInternationalString("band description for data synthetically created"), 
				sampleDimensionTypes,
				Collections.singletonList(IMAGE_BAND_UTILITIES.PHOTOGRAPHIC_BANDS_AXIS),
				defaultAxisBins);
	}

}
