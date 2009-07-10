package org.geotools.coverage.io.range.impl;

import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;
import javax.media.jai.IHSColorSpace;

import org.geotools.coverage.TypeMap;
import org.geotools.coverage.io.range.Axis;
import org.geotools.coverage.io.range.BandDescription;
import org.geotools.coverage.io.range.RangeDescription;
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
public enum IMAGE_PROCESSING_ELEMENTS{

	X {
		@Override
		public DimensionlessAxisBin getAxisBin() {
			return X_BIN;
		}

		@Override
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.UNDEFINED,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.UNDEFINED,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.UNDEFINED,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.UNDEFINED,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.UNDEFINED,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.LIGHTNESS_BAND,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.UNDEFINED,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.UNDEFINED,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.LIGHTNESS_BAND,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.UNDEFINED,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.UNDEFINED,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.UNDEFINED,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.ALPHA_BAND,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.GRAY_INDEX,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.UNDEFINED,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.PALETTE_INDEX,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.BLUE_BAND,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.GREEN_BAND,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.RED_BAND,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.UNDEFINED,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.SATURATION_BAND,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.HUE_BAND,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.CYAN_BAND,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.MAGENTA_BAND,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.YELLOW_BAND,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
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
		public BandDescription getBandType() {
			return new BandDescription(
					ColorInterpretation.BLACK_BAND,
					null,
					null,
					null,
					toString(),
					toString(),
					null);
		}

		@Override
		public BandKey<String, Dimensionless> getBandKey() {
			return new BandKey<String, Dimensionless>(Arrays.asList(getAxisBin()));
		}
	};

	/**
	 * Axis covering the full {@link CMYK} range.
	 */
	public static final Axis<Dimensionless> SYNTHETIC_COLOR_AXIS= new Axis<Dimensionless>("SyntheticColorAxis",  Unit.ONE );
	private static final DimensionlessAxisBin KEY_BIN = new DimensionlessAxisBin(
						new NameImpl("BLACK"),
						new SimpleInternationalString("BLACK bin"),
						SYNTHETIC_COLOR_AXIS,
						"BLACK");
	private static final DimensionlessAxisBin YELLOW_BIN = new DimensionlessAxisBin(
						new NameImpl("YELLOW"),
						new SimpleInternationalString("YELLOW bin"),
						SYNTHETIC_COLOR_AXIS,
						"YELLOW");
	private static final DimensionlessAxisBin MAGENTA_BIN = new DimensionlessAxisBin(
						new NameImpl("MAGENTA"),
						new SimpleInternationalString("Magenta bin"),
						SYNTHETIC_COLOR_AXIS,
						"MAGENTA");
	private static final DimensionlessAxisBin CYAN_BIN = new DimensionlessAxisBin(
						new NameImpl("CYAN"),
						new SimpleInternationalString("CYAN bin"),
						SYNTHETIC_COLOR_AXIS,
						"CYAN");
	private static final DimensionlessAxisBin HUE_BIN = new DimensionlessAxisBin(
						new NameImpl("HUE"),
						new SimpleInternationalString("HUE bin"),
						SYNTHETIC_COLOR_AXIS,
						"HUE");
	private static final DimensionlessAxisBin SATURATION_BIN = new DimensionlessAxisBin(
						new NameImpl("SATURATION"),
						new SimpleInternationalString("SATURATION bin"),
						SYNTHETIC_COLOR_AXIS,
						"SATURATION");
	private static final DimensionlessAxisBin GREEN_BIN = new DimensionlessAxisBin(
						new NameImpl("RED"),
						new SimpleInternationalString("RED bin"),
						SYNTHETIC_COLOR_AXIS,
						"RED");
	private static final DimensionlessAxisBin BLUE_BIN = new DimensionlessAxisBin(
						new NameImpl("BLUE"),
						new SimpleInternationalString("BLUE bin"),
						SYNTHETIC_COLOR_AXIS,
						"BLUE");
	private static final DimensionlessAxisBin RED_BIN = GREEN_BIN;
	private static final DimensionlessAxisBin PALETTE_BIN = new DimensionlessAxisBin(
						new NameImpl("VALUE"),
						new SimpleInternationalString("VALUE bin"),
						SYNTHETIC_COLOR_AXIS,
						"VALUE");
	private static final DimensionlessAxisBin UNDEFINED_BIN = new DimensionlessAxisBin(
						new NameImpl("UNDEFINED"),
						new SimpleInternationalString("UNDEFINED bin"),
						SYNTHETIC_COLOR_AXIS,
						"UNDEFINED");
	private static final DimensionlessAxisBin GRAY_BIN = new DimensionlessAxisBin(
						new NameImpl("GRAY"),
						new SimpleInternationalString("GRAY bin"),
						SYNTHETIC_COLOR_AXIS,
						"GRAY");
	private static final DimensionlessAxisBin ALPHA_BIN = new DimensionlessAxisBin(
						new NameImpl("ALPHA"),
						new SimpleInternationalString("ALPHA bin"),
						SYNTHETIC_COLOR_AXIS,
						"ALPHA");
	private static final DimensionlessAxisBin INTENSITY_BIN = new DimensionlessAxisBin(
						new NameImpl("INTENSITY"),
						new SimpleInternationalString("INTENSITY bin"),
						SYNTHETIC_COLOR_AXIS,
						"INTENSITY");
	private static final DimensionlessAxisBin CHROMA_B_BIN = new DimensionlessAxisBin(
						new NameImpl("CHROMA-B"),
						new SimpleInternationalString("CHROMA-B bin"),
						SYNTHETIC_COLOR_AXIS,
						"CHROMA-B");
	private static final DimensionlessAxisBin CHROMA_A_BIN = new DimensionlessAxisBin(
						new NameImpl("CHROMA-A"),
						new SimpleInternationalString("CHROMA-A bin"),
						SYNTHETIC_COLOR_AXIS,
						"CHROMA-A");
	private static final DimensionlessAxisBin LUMA_BIN = new DimensionlessAxisBin(
						new NameImpl("LUMA"),
						new SimpleInternationalString("LUMA bin"),
						SYNTHETIC_COLOR_AXIS,
						"LUMA");
	private static final DimensionlessAxisBin V_BIN = new DimensionlessAxisBin(
						new NameImpl("V"),
						new SimpleInternationalString("V bin"),
						SYNTHETIC_COLOR_AXIS,
						"V");
	private static final DimensionlessAxisBin U_BIN = new DimensionlessAxisBin(
						new NameImpl("U"),
						new SimpleInternationalString("U bin"),
						SYNTHETIC_COLOR_AXIS,
						"U");
	private static final DimensionlessAxisBin LIGHTNESS_BIN = new DimensionlessAxisBin(
						new NameImpl("LIGHTNESS"),
						new SimpleInternationalString("LIGHTNESS bin"),
						SYNTHETIC_COLOR_AXIS,
						"LIGHTNESS");
	private static final DimensionlessAxisBin B_BIN = new DimensionlessAxisBin(
						new NameImpl("B"),
						new SimpleInternationalString("B bin"),
						SYNTHETIC_COLOR_AXIS,
						"B");
	private static final DimensionlessAxisBin A_BIN = new DimensionlessAxisBin(
						new NameImpl("A"),
						new SimpleInternationalString("A bin"),
						SYNTHETIC_COLOR_AXIS,
						"A");
	private static final DimensionlessAxisBin Z_BIN = new DimensionlessAxisBin(
						new NameImpl("Z"),
						new SimpleInternationalString("Z bin"),
						SYNTHETIC_COLOR_AXIS,
						"Z");
	private static final DimensionlessAxisBin Y_BIN = new DimensionlessAxisBin(
						new NameImpl("Y"),
						new SimpleInternationalString("Y bin"),
						SYNTHETIC_COLOR_AXIS,
						"Y");

	public abstract DimensionlessAxisBin getAxisBin();
	
	public abstract BandDescription getBandType();
	
	public abstract BandKey<String, Dimensionless> getBandKey();

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
							IMAGE_PROCESSING_ELEMENTS.CYAN.getAxisBin(),
							IMAGE_PROCESSING_ELEMENTS.MAGENTA.getAxisBin(),						
							IMAGE_PROCESSING_ELEMENTS.YELLOW.getAxisBin()
						);
			}							
			else
			{	
				return 
					Arrays.asList(
							IMAGE_PROCESSING_ELEMENTS.CYAN.getAxisBin(),
							IMAGE_PROCESSING_ELEMENTS.MAGENTA.getAxisBin(),						
							IMAGE_PROCESSING_ELEMENTS.YELLOW.getAxisBin(),
							IMAGE_PROCESSING_ELEMENTS.KEY.getAxisBin()
					);		
			}
		}
		
		// HSV
		if(firstBandCI==ColorInterpretation.HUE_BAND)
		{
			return Collections.singletonList(IMAGE_PROCESSING_ELEMENTS.HUE.getAxisBin());
		}
		
		//RGBA
		if(firstBandCI==ColorInterpretation.RED_BAND)
		{
			if(sm.getNumBands()==3)
			{
				return 
					Arrays.asList(
						IMAGE_PROCESSING_ELEMENTS.RED.getAxisBin(),
						IMAGE_PROCESSING_ELEMENTS.GREEN.getAxisBin(),
						IMAGE_PROCESSING_ELEMENTS.BLUE.getAxisBin()
					);
			}							
			else
			{	
				return 
				Arrays.asList(
					IMAGE_PROCESSING_ELEMENTS.RED.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.GREEN.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.BLUE.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.ALPHA.getAxisBin()
				);				
			}
		}
		
		//PALETTE
		if(firstBandCI==ColorInterpretation.PALETTE_INDEX)
			return Collections.singletonList(IMAGE_PROCESSING_ELEMENTS.PALETTE.getAxisBin());		
		
		// GRAY, GRAY+ALPHA
		if(firstBandCI==ColorInterpretation.GRAY_INDEX)
		{
			if(sm.getNumBands()==2)
				return Arrays.asList(
						IMAGE_PROCESSING_ELEMENTS.GRAY.getAxisBin(),
						IMAGE_PROCESSING_ELEMENTS.ALPHA.getAxisBin()
				);	
			else
				return Arrays.asList(
						IMAGE_PROCESSING_ELEMENTS.GRAY.getAxisBin()
				);	
				
		}
		
		
		final ColorSpace cs = cm.getColorSpace();
		//IHS
		if(cs instanceof IHSColorSpace)
			return Arrays.asList(
					IMAGE_PROCESSING_ELEMENTS.INTENSITY.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.HUE.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.SATURATION.getAxisBin()
			);	
	
		//YCbCr, LUV, LAB, HLS, IEXYZ 
		switch(cs.getType()){
		case ColorSpace.TYPE_YCbCr:
			return Arrays.asList(
					IMAGE_PROCESSING_ELEMENTS.LUMA.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.CHROMA_A.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.CHROMA_B.getAxisBin()
			);		
		case ColorSpace.TYPE_Luv:
			return Arrays.asList(
					IMAGE_PROCESSING_ELEMENTS.LIGHTNESS.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.U.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.V.getAxisBin()
			);					
		case ColorSpace.TYPE_Lab:
			return Arrays.asList(
					IMAGE_PROCESSING_ELEMENTS.LIGHTNESS.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.A.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.B.getAxisBin()
			);					
		case ColorSpace.TYPE_HLS:
			return Arrays.asList(
					IMAGE_PROCESSING_ELEMENTS.HUE.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.LIGHTNESS.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.SATURATION.getAxisBin()
			);				
		case ColorSpace.CS_CIEXYZ:
			return Arrays.asList(
					IMAGE_PROCESSING_ELEMENTS.X.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.Y.getAxisBin(),
					IMAGE_PROCESSING_ELEMENTS.Z.getAxisBin()
			);				
			
		default:
			return null;
		
			
		}
	}
	
	public static RangeDescription<String,Dimensionless> getFieldTypeFromRenderedImage(final RenderedImage raster){
		if(raster==null)
			throw new NullPointerException("Provided null input image");
		
		final ColorModel cm= raster.getColorModel();
		if(cm==null)
			throw new IllegalArgumentException("Provided input image with null color model");	
		final SampleModel sm= raster.getSampleModel();
		if(sm==null)
			throw new IllegalArgumentException("Provided input image with null SampleModel");			
		
		//get the color interpretation for the three bands
		final ColorInterpretation firstBandCI = TypeMap.getColorInterpretation(cm, 0);
		
		// get axis for this fieldtype and prepare the map for the band type
		final HashMap<BandKey<String, Dimensionless>, BandDescription> bands= new HashMap<BandKey<String,Dimensionless>, BandDescription>();
		
		
		//		CMY - CMYK
		if(firstBandCI==ColorInterpretation.CYAN_BAND)
		{
			if(sm.getNumBands()==3)
			{
				bands.put(IMAGE_PROCESSING_ELEMENTS.CYAN.getBandKey(), IMAGE_PROCESSING_ELEMENTS.CYAN.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.MAGENTA.getBandKey(), IMAGE_PROCESSING_ELEMENTS.MAGENTA.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.YELLOW.getBandKey(), IMAGE_PROCESSING_ELEMENTS.YELLOW.getBandType());
			}							
			else
			{	
				bands.put(IMAGE_PROCESSING_ELEMENTS.CYAN.getBandKey(), IMAGE_PROCESSING_ELEMENTS.CYAN.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.MAGENTA.getBandKey(), IMAGE_PROCESSING_ELEMENTS.MAGENTA.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.YELLOW.getBandKey(), IMAGE_PROCESSING_ELEMENTS.YELLOW.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.KEY.getBandKey(), IMAGE_PROCESSING_ELEMENTS.KEY.getBandType());
	
			}
		}
		
		// HSV
		if(firstBandCI==ColorInterpretation.HUE_BAND)
		{
			bands.put(IMAGE_PROCESSING_ELEMENTS.HUE.getBandKey(), IMAGE_PROCESSING_ELEMENTS.HUE.getBandType());	
		}
		
		//RGB(A)
		if(firstBandCI==ColorInterpretation.RED_BAND)
		{
			if(sm.getNumBands()==3)
			{
				bands.put(IMAGE_PROCESSING_ELEMENTS.RED.getBandKey(), IMAGE_PROCESSING_ELEMENTS.RED.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.GREEN.getBandKey(), IMAGE_PROCESSING_ELEMENTS.GREEN.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.BLUE.getBandKey(), IMAGE_PROCESSING_ELEMENTS.BLUE.getBandType());
			}							
			else
			{	
				bands.put(IMAGE_PROCESSING_ELEMENTS.RED.getBandKey(), IMAGE_PROCESSING_ELEMENTS.RED.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.GREEN.getBandKey(), IMAGE_PROCESSING_ELEMENTS.GREEN.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.BLUE.getBandKey(), IMAGE_PROCESSING_ELEMENTS.BLUE.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.ALPHA.getBandKey(), IMAGE_PROCESSING_ELEMENTS.ALPHA.getBandType());
			
			}	
		}
		
		//PALETTE
		if(firstBandCI==ColorInterpretation.PALETTE_INDEX)
		{
			bands.put(IMAGE_PROCESSING_ELEMENTS.PALETTE.getBandKey(), IMAGE_PROCESSING_ELEMENTS.PALETTE.getBandType());	
		}
		
		// GRAY, GRAY+ALPHA
		if(firstBandCI==ColorInterpretation.GRAY_INDEX)
		{
			if(sm.getNumBands()==2)
			{

				bands.put(IMAGE_PROCESSING_ELEMENTS.GRAY.getBandKey(), IMAGE_PROCESSING_ELEMENTS.GRAY.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.ALPHA.getBandKey(), IMAGE_PROCESSING_ELEMENTS.ALPHA.getBandType());
			}
			else
				bands.put(IMAGE_PROCESSING_ELEMENTS.GRAY.getBandKey(), IMAGE_PROCESSING_ELEMENTS.GRAY.getBandType());	
				
		}
		
		
		final ColorSpace cs = cm.getColorSpace();
		//IHS
		if(cs instanceof IHSColorSpace){
			bands.put(IMAGE_PROCESSING_ELEMENTS.INTENSITY.getBandKey(), IMAGE_PROCESSING_ELEMENTS.INTENSITY.getBandType());
			bands.put(IMAGE_PROCESSING_ELEMENTS.HUE.getBandKey(), IMAGE_PROCESSING_ELEMENTS.HUE.getBandType());
			bands.put(IMAGE_PROCESSING_ELEMENTS.SATURATION.getBandKey(), IMAGE_PROCESSING_ELEMENTS.SATURATION.getBandType());
		}
	
			if(bands.isEmpty()){
			//YCbCr, LUV, LAB, HLS, IEXYZ 
			switch(cs.getType()){
			case ColorSpace.TYPE_YCbCr:
				bands.put(IMAGE_PROCESSING_ELEMENTS.LUMA.getBandKey(), IMAGE_PROCESSING_ELEMENTS.LUMA.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.CHROMA_A.getBandKey(), IMAGE_PROCESSING_ELEMENTS.CHROMA_A.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.CHROMA_B.getBandKey(), IMAGE_PROCESSING_ELEMENTS.CHROMA_B.getBandType());
				break;
		
			case ColorSpace.TYPE_Luv:
				bands.put(IMAGE_PROCESSING_ELEMENTS.LIGHTNESS.getBandKey(), IMAGE_PROCESSING_ELEMENTS.LIGHTNESS.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.U.getBandKey(), IMAGE_PROCESSING_ELEMENTS.U.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.V.getBandKey(), IMAGE_PROCESSING_ELEMENTS.V.getBandType());
				break;			
					
			case ColorSpace.TYPE_Lab:
				bands.put(IMAGE_PROCESSING_ELEMENTS.LIGHTNESS.getBandKey(), IMAGE_PROCESSING_ELEMENTS.LIGHTNESS.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.A.getBandKey(), IMAGE_PROCESSING_ELEMENTS.A.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.B.getBandKey(), IMAGE_PROCESSING_ELEMENTS.B.getBandType());
				break;			
					
			case ColorSpace.TYPE_HLS:
				bands.put(IMAGE_PROCESSING_ELEMENTS.HUE.getBandKey(), IMAGE_PROCESSING_ELEMENTS.HUE.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.LIGHTNESS.getBandKey(), IMAGE_PROCESSING_ELEMENTS.LIGHTNESS.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.SATURATION.getBandKey(), IMAGE_PROCESSING_ELEMENTS.SATURATION.getBandType());
				break;				
			case ColorSpace.CS_CIEXYZ:
				bands.put(IMAGE_PROCESSING_ELEMENTS.X.getBandKey(), IMAGE_PROCESSING_ELEMENTS.X.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.Y.getBandKey(), IMAGE_PROCESSING_ELEMENTS.Y.getBandType());
				bands.put(IMAGE_PROCESSING_ELEMENTS.Z.getBandKey(), IMAGE_PROCESSING_ELEMENTS.Z.getBandType());
				break;			
				
			default:
				throw new IllegalArgumentException("Unable to create RangeDescription for this rendered image");
			
			}
		}
		//build the field type
		return new RangeDescription<String, Dimensionless>(
				new NameImpl("RenderedImageFieldType"),
				new SimpleInternationalString("RangeDescription for rendered image"),
				Unit.ONE,
				band);
	}
	
	private final static DimensionlessAxisBin X_BIN=new DimensionlessAxisBin(
			new NameImpl("X"),
			new SimpleInternationalString("X bin"),
			SYNTHETIC_COLOR_AXIS,
			"X");
}