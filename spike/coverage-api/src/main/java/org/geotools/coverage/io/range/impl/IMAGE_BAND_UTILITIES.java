package org.geotools.coverage.io.range.impl;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.imageio.ImageTypeSpecifier;
import javax.measure.quantity.Dimensionless;
import javax.media.jai.IHSColorSpace;

import org.geotools.coverage.TypeMap;
import org.geotools.coverage.io.range.Axis;
import org.geotools.coverage.io.range.Axis.DimensionlessAxis;
import org.geotools.coverage.io.range.AxisBin.DimensionlessAxisBin;
import org.geotools.coverage.io.range.Band.BandKey;
import org.geotools.feature.NameImpl;
import org.geotools.util.SimpleInternationalString;
import org.opengis.coverage.ColorInterpretation;

/**
 * Process Color is a subtractive model used when working with pigment. This
 * model is often used when printing.
 * <p>
 * This is a normal Java 5 enum capturing the closed set of CMYK names. It is
 * used as a basis for the definition of an Axis built around these constants.
 * <p>
 * Please understand that this is not the only possible subtractive color model
 * - a commercial alternative is the Pantone (tm)) colors.
 * 
 */
@SuppressWarnings("deprecation")
public enum IMAGE_BAND_UTILITIES{

	X {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return X_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},		
	Y {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return Y_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},		
	Z {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return Z_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},	
	A {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return A_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},		
	B {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return B_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},		
	LIGHTNESS {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return LIGHTNESS_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},
	U {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return U_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},		
	V {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return V_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},	
	LUMA {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return LUMA_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},
	CHROMA_A {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return CHROMA_A_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},		
	CHROMA_B {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return CHROMA_B_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},
	INTENSITY {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return INTENSITY_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},
	ALPHA {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return ALPHA_BIN;
		}
		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},		
	GRAY {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return GRAY_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},	
	UNDEFINED {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return UNDEFINED_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},	
	PALETTE {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return PALETTE_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},		
	BLUE {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return BLUE_BIN;
		}
		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},	
	GREEN {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return GREEN_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},	
	RED {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return RED_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},	
	VALUE {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return SATURATION_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},
	SATURATION {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return SATURATION_BIN;
		}
		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},
	HUE {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return HUE_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	},
	CYAN {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return CYAN_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	}, MAGENTA {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return MAGENTA_BIN;
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	}, YELLOW {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return 						
				YELLOW_BIN;
		}
		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	}, KEY {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return 					
			KEY_BIN;
		}
		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	};

	/**
	 * Specific axis for controlling bins that looks up photographic bands, like RGB, CMYK, HSV, etc...
	 */
	public static final Axis<Dimensionless> PHOTOGRAPHIC_BANDS_AXIS= new DimensionlessAxis("SyntheticColorAxis" );
	
	
	private static final DimensionlessAxisBin KEY_BIN = new DimensionlessAxisBin(
						new NameImpl("BLACK"),
						new SimpleInternationalString("BLACK bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"BLACK");
	private static final DimensionlessAxisBin YELLOW_BIN = new DimensionlessAxisBin(
						new NameImpl("YELLOW"),
						new SimpleInternationalString("YELLOW bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"YELLOW");
	private static final DimensionlessAxisBin MAGENTA_BIN = new DimensionlessAxisBin(
						new NameImpl("MAGENTA"),
						new SimpleInternationalString("Magenta bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"MAGENTA");
	private static final DimensionlessAxisBin CYAN_BIN = new DimensionlessAxisBin(
						new NameImpl("CYAN"),
						new SimpleInternationalString("CYAN bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"CYAN");
	private static final DimensionlessAxisBin HUE_BIN = new DimensionlessAxisBin(
						new NameImpl("HUE"),
						new SimpleInternationalString("HUE bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"HUE");
	private static final DimensionlessAxisBin SATURATION_BIN = new DimensionlessAxisBin(
						new NameImpl("SATURATION"),
						new SimpleInternationalString("SATURATION bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"SATURATION");
	private static final DimensionlessAxisBin GREEN_BIN = new DimensionlessAxisBin(
						new NameImpl("RED"),
						new SimpleInternationalString("RED bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"RED");
	private static final DimensionlessAxisBin BLUE_BIN = new DimensionlessAxisBin(
						new NameImpl("BLUE"),
						new SimpleInternationalString("BLUE bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"BLUE");
	private static final DimensionlessAxisBin RED_BIN = GREEN_BIN;
	private static final DimensionlessAxisBin PALETTE_BIN = new DimensionlessAxisBin(
						new NameImpl("VALUE"),
						new SimpleInternationalString("VALUE bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"VALUE");
	private static final DimensionlessAxisBin UNDEFINED_BIN = new DimensionlessAxisBin(
						new NameImpl("UNDEFINED"),
						new SimpleInternationalString("UNDEFINED bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"UNDEFINED");
	private static final DimensionlessAxisBin GRAY_BIN = new DimensionlessAxisBin(
						new NameImpl("GRAY"),
						new SimpleInternationalString("GRAY bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"GRAY");
	private static final DimensionlessAxisBin ALPHA_BIN = new DimensionlessAxisBin(
						new NameImpl("ALPHA"),
						new SimpleInternationalString("ALPHA bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"ALPHA");
	private static final DimensionlessAxisBin INTENSITY_BIN = new DimensionlessAxisBin(
						new NameImpl("INTENSITY"),
						new SimpleInternationalString("INTENSITY bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"INTENSITY");
	private static final DimensionlessAxisBin CHROMA_B_BIN = new DimensionlessAxisBin(
						new NameImpl("CHROMA-B"),
						new SimpleInternationalString("CHROMA-B bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"CHROMA-B");
	private static final DimensionlessAxisBin CHROMA_A_BIN = new DimensionlessAxisBin(
						new NameImpl("CHROMA-A"),
						new SimpleInternationalString("CHROMA-A bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"CHROMA-A");
	private static final DimensionlessAxisBin LUMA_BIN = new DimensionlessAxisBin(
						new NameImpl("LUMA"),
						new SimpleInternationalString("LUMA bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"LUMA");
	private static final DimensionlessAxisBin V_BIN = new DimensionlessAxisBin(
						new NameImpl("V"),
						new SimpleInternationalString("V bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"V");
	private static final DimensionlessAxisBin U_BIN = new DimensionlessAxisBin(
						new NameImpl("U"),
						new SimpleInternationalString("U bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"U");
	private static final DimensionlessAxisBin LIGHTNESS_BIN = new DimensionlessAxisBin(
						new NameImpl("LIGHTNESS"),
						new SimpleInternationalString("LIGHTNESS bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"LIGHTNESS");
	private static final DimensionlessAxisBin B_BIN = new DimensionlessAxisBin(
						new NameImpl("B"),
						new SimpleInternationalString("B bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"B");
	private static final DimensionlessAxisBin A_BIN = new DimensionlessAxisBin(
						new NameImpl("A"),
						new SimpleInternationalString("A bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"A");
	private static final DimensionlessAxisBin Z_BIN = new DimensionlessAxisBin(
						new NameImpl("Z"),
						new SimpleInternationalString("Z bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"Z");
	private static final DimensionlessAxisBin Y_BIN = new DimensionlessAxisBin(
						new NameImpl("Y"),
						new SimpleInternationalString("Y bin"),
						PHOTOGRAPHIC_BANDS_AXIS,
						"Y");

	public abstract DimensionlessAxisBin getAxisBin();
	
	public abstract BandKey<String, Dimensionless> getBandKey();
	
	public static List<DimensionlessAxisBin> getBinsFromRenderedImage(final ColorModel cm, final SampleModel sm){
		if(cm==null)
			throw new IllegalArgumentException("Provided input image with null color model");	
		if(sm==null)
			throw new IllegalArgumentException("Provided input image with null SampleModel");			
		
		//get the color interpretation for the three bands
		final ColorInterpretation firstBandCI = TypeMap.getColorInterpretation(cm, 0);
				
		//		CMY - CMYK
		if(firstBandCI==ColorInterpretation.CYAN_BAND)
		{
			if(sm.getNumBands()==3)
			{
				return 
					Arrays.asList(
							IMAGE_BAND_UTILITIES.CYAN.getAxisBin(),
							IMAGE_BAND_UTILITIES.MAGENTA.getAxisBin(),						
							IMAGE_BAND_UTILITIES.YELLOW.getAxisBin()
						);
			}							
			else
			{	
				return 
					Arrays.asList(
							IMAGE_BAND_UTILITIES.CYAN.getAxisBin(),
							IMAGE_BAND_UTILITIES.MAGENTA.getAxisBin(),						
							IMAGE_BAND_UTILITIES.YELLOW.getAxisBin(),
							IMAGE_BAND_UTILITIES.KEY.getAxisBin()
					);		
			}
		}
		
		// HSV
		if(firstBandCI==ColorInterpretation.HUE_BAND)
		{
			return Collections.singletonList(IMAGE_BAND_UTILITIES.HUE.getAxisBin());
		}
		
		//RGBA
		if(firstBandCI==ColorInterpretation.RED_BAND)
		{
			if(sm.getNumBands()==3)
			{
				return 
					Arrays.asList(
						IMAGE_BAND_UTILITIES.RED.getAxisBin(),
						IMAGE_BAND_UTILITIES.GREEN.getAxisBin(),
						IMAGE_BAND_UTILITIES.BLUE.getAxisBin()
					);
			}							
			else
			{	
				return 
				Arrays.asList(
					IMAGE_BAND_UTILITIES.RED.getAxisBin(),
					IMAGE_BAND_UTILITIES.GREEN.getAxisBin(),
					IMAGE_BAND_UTILITIES.BLUE.getAxisBin(),
					IMAGE_BAND_UTILITIES.ALPHA.getAxisBin()
				);				
			}
		}
		
		//PALETTE
		if(firstBandCI==ColorInterpretation.PALETTE_INDEX)
			return Collections.singletonList(IMAGE_BAND_UTILITIES.PALETTE.getAxisBin());		
		
		// GRAY, GRAY+ALPHA
		if(firstBandCI==ColorInterpretation.GRAY_INDEX)
		{
			if(sm.getNumBands()==2)
				return Arrays.asList(
						IMAGE_BAND_UTILITIES.GRAY.getAxisBin(),
						IMAGE_BAND_UTILITIES.ALPHA.getAxisBin()
				);	
			else
				return Arrays.asList(
						IMAGE_BAND_UTILITIES.GRAY.getAxisBin()
				);	
				
		}
		
		
		final ColorSpace cs = cm.getColorSpace();
		//IHS
		if(cs instanceof IHSColorSpace)
			return Arrays.asList(
					IMAGE_BAND_UTILITIES.INTENSITY.getAxisBin(),
					IMAGE_BAND_UTILITIES.HUE.getAxisBin(),
					IMAGE_BAND_UTILITIES.SATURATION.getAxisBin()
			);	
	
		//YCbCr, LUV, LAB, HLS, IEXYZ 
		switch(cs.getType()){
		case ColorSpace.TYPE_YCbCr:
			return Arrays.asList(
					IMAGE_BAND_UTILITIES.LUMA.getAxisBin(),
					IMAGE_BAND_UTILITIES.CHROMA_A.getAxisBin(),
					IMAGE_BAND_UTILITIES.CHROMA_B.getAxisBin()
			);		
		case ColorSpace.TYPE_Luv:
			return Arrays.asList(
					IMAGE_BAND_UTILITIES.LIGHTNESS.getAxisBin(),
					IMAGE_BAND_UTILITIES.U.getAxisBin(),
					IMAGE_BAND_UTILITIES.V.getAxisBin()
			);					
		case ColorSpace.TYPE_Lab:
			return Arrays.asList(
					IMAGE_BAND_UTILITIES.LIGHTNESS.getAxisBin(),
					IMAGE_BAND_UTILITIES.A.getAxisBin(),
					IMAGE_BAND_UTILITIES.B.getAxisBin()
			);					
		case ColorSpace.TYPE_HLS:
			return Arrays.asList(
					IMAGE_BAND_UTILITIES.HUE.getAxisBin(),
					IMAGE_BAND_UTILITIES.LIGHTNESS.getAxisBin(),
					IMAGE_BAND_UTILITIES.SATURATION.getAxisBin()
			);				
		case ColorSpace.CS_CIEXYZ:
			return Arrays.asList(
					IMAGE_BAND_UTILITIES.X.getAxisBin(),
					IMAGE_BAND_UTILITIES.Y.getAxisBin(),
					IMAGE_BAND_UTILITIES.Z.getAxisBin()
			);				
			
		default:
			return null;
		
			
		}
	}
	/**
	 * Helper classes for creating {@link DimensionlessAxis} for the most common color models' bands.
	 * 
	 * <p>
	 * Suypported colorspaces incluse RGBA, GRAY, GRAYA, HSV,HLS, LAB, LUV, IHS, CI_XYZ, CMY(K).
	 * Notice that RGB is not handled here but through a wavelength axis.
	 * 
	 * <p>
	 * This method returns null if an unsupported {@link ColorModel} is provided.
	 * 
	 * @param raster a {@link RenderedImage} implementation from which to extract needed info, usually {@link ColorModel} and {@link SampleModel}.
	 * @return a {@link DimensionlessAxis} or null if an unsupported {@link ColorModel} is provided.
	 */
	public static List<DimensionlessAxisBin> getBinsFromRenderedImage(final RenderedImage raster){
		if(raster==null)
			throw new NullPointerException("Provided null input image");
		final ColorModel cm= raster.getColorModel();
		if(cm==null)
			throw new IllegalArgumentException("Provided input image with null color model");	
		final SampleModel sm= raster.getSampleModel();
		return getBinsFromRenderedImage(cm, sm);
		
	}
	
	public static List<DimensionlessAxisBin> getBinsFromRenderedImage(final ImageTypeSpecifier it){
		if(it==null)
			throw new NullPointerException("Provided null input ImageTypeSpecifier");
		final ColorModel cm= it.getColorModel();
		if(cm==null)
			throw new IllegalArgumentException("Provided input image with null color model");	
		final SampleModel sm= it.getSampleModel();
		return getBinsFromRenderedImage(cm, sm);
		
	}
	
//	
//	public static RangeDescriptor<String,Dimensionless> getFieldTypeFromRenderedImage(final RenderedImage raster){
//		if(raster==null)
//			throw new NullPointerException("Provided null input image");
//		
//		final ColorModel cm= raster.getColorModel();
//		if(cm==null)
//			throw new IllegalArgumentException("Provided input image with null color model");	
//		final SampleModel sm= raster.getSampleModel();
//		if(sm==null)
//			throw new IllegalArgumentException("Provided input image with null SampleModel");			
//		
//		//get the color interpretation for the three bands
//		final ColorInterpretation firstBandCI = TypeMap.getColorInterpretation(cm, 0);
//		
//		// get axis for this fieldtype and prepare the map for the band type
//		final HashMap<BandKey<String, Dimensionless>, BandDescriptor> bands= new HashMap<BandKey<String,Dimensionless>, BandDescriptor>();
//		
//		
//		//		CMY - CMYK
//		if(firstBandCI==ColorInterpretation.CYAN_BAND)
//		{
//			if(sm.getNumBands()==3)
//			{
//				bands.put(IMAGE_BAND_UTILITIES.CYAN.getBandKey(), IMAGE_BAND_UTILITIES.CYAN.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.MAGENTA.getBandKey(), IMAGE_BAND_UTILITIES.MAGENTA.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.YELLOW.getBandKey(), IMAGE_BAND_UTILITIES.YELLOW.getBandType());
//			}							
//			else
//			{	
//				bands.put(IMAGE_BAND_UTILITIES.CYAN.getBandKey(), IMAGE_BAND_UTILITIES.CYAN.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.MAGENTA.getBandKey(), IMAGE_BAND_UTILITIES.MAGENTA.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.YELLOW.getBandKey(), IMAGE_BAND_UTILITIES.YELLOW.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.KEY.getBandKey(), IMAGE_BAND_UTILITIES.KEY.getBandType());
//	
//			}
//		}
//		
//		// HSV
//		if(firstBandCI==ColorInterpretation.HUE_BAND)
//		{
//			bands.put(IMAGE_BAND_UTILITIES.HUE.getBandKey(), IMAGE_BAND_UTILITIES.HUE.getBandType());	
//		}
//		
//		//RGB(A)
//		if(firstBandCI==ColorInterpretation.RED_BAND)
//		{
//			if(sm.getNumBands()==3)
//			{
//				bands.put(IMAGE_BAND_UTILITIES.RED.getBandKey(), IMAGE_BAND_UTILITIES.RED.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.GREEN.getBandKey(), IMAGE_BAND_UTILITIES.GREEN.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.BLUE.getBandKey(), IMAGE_BAND_UTILITIES.BLUE.getBandType());
//			}							
//			else
//			{	
//				bands.put(IMAGE_BAND_UTILITIES.RED.getBandKey(), IMAGE_BAND_UTILITIES.RED.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.GREEN.getBandKey(), IMAGE_BAND_UTILITIES.GREEN.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.BLUE.getBandKey(), IMAGE_BAND_UTILITIES.BLUE.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.ALPHA.getBandKey(), IMAGE_BAND_UTILITIES.ALPHA.getBandType());
//			
//			}	
//		}
//		
//		//PALETTE
//		if(firstBandCI==ColorInterpretation.PALETTE_INDEX)
//		{
//			bands.put(IMAGE_BAND_UTILITIES.PALETTE.getBandKey(), IMAGE_BAND_UTILITIES.PALETTE.getBandType());	
//		}
//		
//		// GRAY, GRAY+ALPHA
//		if(firstBandCI==ColorInterpretation.GRAY_INDEX)
//		{
//			if(sm.getNumBands()==2)
//			{
//
//				bands.put(IMAGE_BAND_UTILITIES.GRAY.getBandKey(), IMAGE_BAND_UTILITIES.GRAY.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.ALPHA.getBandKey(), IMAGE_BAND_UTILITIES.ALPHA.getBandType());
//			}
//			else
//				bands.put(IMAGE_BAND_UTILITIES.GRAY.getBandKey(), IMAGE_BAND_UTILITIES.GRAY.getBandType());	
//				
//		}
//		
//		
//		final ColorSpace cs = cm.getColorSpace();
//		//IHS
//		if(cs instanceof IHSColorSpace){
//			bands.put(IMAGE_BAND_UTILITIES.INTENSITY.getBandKey(), IMAGE_BAND_UTILITIES.INTENSITY.getBandType());
//			bands.put(IMAGE_BAND_UTILITIES.HUE.getBandKey(), IMAGE_BAND_UTILITIES.HUE.getBandType());
//			bands.put(IMAGE_BAND_UTILITIES.SATURATION.getBandKey(), IMAGE_BAND_UTILITIES.SATURATION.getBandType());
//		}
//	
//			if(bands.isEmpty()){
//			//YCbCr, LUV, LAB, HLS, IEXYZ 
//			switch(cs.getType()){
//			case ColorSpace.TYPE_YCbCr:
//				bands.put(IMAGE_BAND_UTILITIES.LUMA.getBandKey(), IMAGE_BAND_UTILITIES.LUMA.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.CHROMA_A.getBandKey(), IMAGE_BAND_UTILITIES.CHROMA_A.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.CHROMA_B.getBandKey(), IMAGE_BAND_UTILITIES.CHROMA_B.getBandType());
//				break;
//		
//			case ColorSpace.TYPE_Luv:
//				bands.put(IMAGE_BAND_UTILITIES.LIGHTNESS.getBandKey(), IMAGE_BAND_UTILITIES.LIGHTNESS.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.U.getBandKey(), IMAGE_BAND_UTILITIES.U.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.V.getBandKey(), IMAGE_BAND_UTILITIES.V.getBandType());
//				break;			
//					
//			case ColorSpace.TYPE_Lab:
//				bands.put(IMAGE_BAND_UTILITIES.LIGHTNESS.getBandKey(), IMAGE_BAND_UTILITIES.LIGHTNESS.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.A.getBandKey(), IMAGE_BAND_UTILITIES.A.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.B.getBandKey(), IMAGE_BAND_UTILITIES.B.getBandType());
//				break;			
//					
//			case ColorSpace.TYPE_HLS:
//				bands.put(IMAGE_BAND_UTILITIES.HUE.getBandKey(), IMAGE_BAND_UTILITIES.HUE.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.LIGHTNESS.getBandKey(), IMAGE_BAND_UTILITIES.LIGHTNESS.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.SATURATION.getBandKey(), IMAGE_BAND_UTILITIES.SATURATION.getBandType());
//				break;				
//			case ColorSpace.CS_CIEXYZ:
//				bands.put(IMAGE_BAND_UTILITIES.X.getBandKey(), IMAGE_BAND_UTILITIES.X.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.Y.getBandKey(), IMAGE_BAND_UTILITIES.Y.getBandType());
//				bands.put(IMAGE_BAND_UTILITIES.Z.getBandKey(), IMAGE_BAND_UTILITIES.Z.getBandType());
//				break;			
//				
//			default:
//				throw new IllegalArgumentException("Unable to create RangeDescriptor for this rendered image");
//			
//			}
//		}
//		//build the field type
//		return new RangeDescriptor<String, Dimensionless>(
//				new NameImpl("RenderedImageFieldType"),
//				new SimpleInternationalString("RangeDescriptor for rendered image"),
//				Unit.ONE,
//				band);
//	}
	
	private final static DimensionlessAxisBin X_BIN=new DimensionlessAxisBin(
			new NameImpl("X"),
			new SimpleInternationalString("X bin"),
			PHOTOGRAPHIC_BANDS_AXIS,
			"X");
}