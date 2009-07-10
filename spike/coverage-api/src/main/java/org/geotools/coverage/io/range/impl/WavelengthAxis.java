package org.geotools.coverage.io.range.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;

import org.geotools.coverage.io.range.Axis;
import org.geotools.coverage.io.range.AxisBin;
import org.geotools.coverage.io.range.RangeUtilities;
import org.geotools.feature.NameImpl;
import org.geotools.referencing.crs.DefaultEngineeringCRS;
import org.geotools.referencing.cs.DefaultCoordinateSystemAxis;
import org.geotools.referencing.cs.DefaultLinearCS;
import org.geotools.referencing.datum.DefaultEngineeringDatum;
import org.geotools.util.MeasurementRange;
import org.geotools.util.SimpleInternationalString;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.opengis.referencing.datum.EngineeringDatum;
import org.opengis.util.InternationalString;


public class WavelengthAxis extends Axis<Length>{
	
	/**
	 * Singleton instance of a {@link WavelengthAxis} that measures in nanometers.
	 */
	public final static WavelengthAxis WAVELENGTH_AXIS_NM= new WavelengthAxis("WAVELENGTH_AXIS_NM",RangeUtilities.WAVELENGTH_UOM);
	
    /**
	 * A bin for the wavelength axis
	 */
	public static class WavelengthBin extends AxisBin<MeasurementRange<Double>,Length>{
				
		/**
		 * 
		 */
		private static final long serialVersionUID = -3977921692927799401L;
		
		public WavelengthBin( Name name, double value, InternationalString description, WavelengthAxis axis ){
			super(
					name,
					description,
					axis,
					MeasurementRange.create(value, value, RangeUtilities.WAVELENGTH_UOM));
			
		}
	
		public WavelengthBin( String name, double value, String description, WavelengthAxis axis ){
			super(
					new NameImpl(name),
					new SimpleInternationalString(description),
					axis,
					MeasurementRange.create(value, value, RangeUtilities.WAVELENGTH_UOM));
			
		}		
		
		
		public WavelengthBin( Name name, double from, double to, InternationalString description, WavelengthAxis axis ) {
			super(
					name,
					description,
					axis,
					MeasurementRange.create(from, to, RangeUtilities.WAVELENGTH_UOM));
		}
		public WavelengthBin( String name, double from, double to, String description, WavelengthAxis axis ) {
			super(
					new NameImpl(name),
					new SimpleInternationalString(description),
					axis,
					MeasurementRange.create(from, to, RangeUtilities.WAVELENGTH_UOM));
		}		
	}

	/**
	 * Keys for this {@link Axis}.
	 */
	private ArrayList<Measure<MeasurementRange<Double>, Length>> keys;
	
	private NameImpl name;

	/** LANDSAT7 definition of BLUE */
	public static final WavelengthBin LANDSAT7_BLUE_AXIS_BIN= new WavelengthBin( "BLUE", 450, 520, "useful for soil/vegetation discrimination, forest type mapping, and identifying man-made features",WavelengthAxis.WAVELENGTH_AXIS_NM);

	/** Blue light between 450-495 nm */
	public static final WavelengthBin COLOR_BLUE_AXIS_BIN= new WavelengthBin( "Blue", 450, 495, "Visible light between 450-495 nm" ,WavelengthAxis.WAVELENGTH_AXIS_NM);

	/** Green light between 495-570 nm */	
	public static final WavelengthBin COLOR_GREEN_AXIS_BIN= new WavelengthBin( "Green", 495, 570, "Visible light between 495-570 nm" ,WavelengthAxis.WAVELENGTH_AXIS_NM);

	/** Orange light between 590-620 nm */	
	public static final WavelengthBin COLOR_ORANGE_AXIS_BIN= new WavelengthBin( "Green", 590,620, "Visible light between 590-620 nm",WavelengthAxis.WAVELENGTH_AXIS_NM );

	/** Red between 620-750 nm */
	public static final WavelengthBin COLOR_RED_AXIS_BIN= new WavelengthBin( "Yellow", 620, 750, "Visible light between 620-750 nm" ,WavelengthAxis.WAVELENGTH_AXIS_NM);

	/** Violet light between 380-450 nm */
	public static final WavelengthBin COLOR_VIOLET_AXIS_BIN= new WavelengthBin( "Violet", 380, 450, "Visible light between 380-450 nm" ,WavelengthAxis.WAVELENGTH_AXIS_NM);

	/** Yellow between 570-590 nm */
	public static final WavelengthBin COLOR_YELLOW_AXIS_BIN= new WavelengthBin( "Yellow", 570,590, "Visible light between 570-590 nm" ,WavelengthAxis.WAVELENGTH_AXIS_NM);

	/** LANDSAT7 definition of GREEN */
	public static  final WavelengthBin GREEN_AXIS_BIN= new WavelengthBin( "GREEN", 520, 610, "penetrates clear water fairly well, and gives excellent contrast between clear and turbid (muddy) water.",WavelengthAxis.WAVELENGTH_AXIS_NM);

	/** LANDSAT7 definition of NIR */	
	public static  final WavelengthBin NIR_AXIS_BIN =new WavelengthBin("NIR", 750, 900, " good for mapping shorelines and biomass content",WavelengthAxis.WAVELENGTH_AXIS_NM);

	/** LANDSAT7 definition of RED */	
	public static  final WavelengthBin RED_AXIS_BIN= new WavelengthBin("RED",  630, 690, "useful for identifying vegetation types, soils, and urban (city and town) features",WavelengthAxis.WAVELENGTH_AXIS_NM);

	/** LANDSAT7 definition of SWIR */
	public static  final WavelengthBin SWIR_AXIS_BIN =new WavelengthBin("SWIR", 1550, 17560, "useful to measure the moisture content of soil and vegetation",WavelengthAxis.WAVELENGTH_AXIS_NM);

	/** LANDSAT7 definition of SWIR2 */
	public static  final WavelengthBin SWIR2_AXIS_BIN= new WavelengthBin("SWIR2", 2080, 23500, "useful to measure the moisture content of soil and vegetation",WavelengthAxis.WAVELENGTH_AXIS_NM);

	/** LANDSAT7 definition of TIR */
	public static  final WavelengthBin TIR_AXIS_BIN= new WavelengthBin("TIR", 10400, 12500, "useful to observe temperature",WavelengthAxis.WAVELENGTH_AXIS_NM);
	
	private static final DefaultEngineeringCRS CRS;
	static {
		final CoordinateSystemAxis csAxis = new DefaultCoordinateSystemAxis(
				new SimpleInternationalString("Light"),
				"\u03BB", // LAMBDA
				AxisDirection.OTHER, 	
				RangeUtilities.WAVELENGTH_UOM);
		final DefaultLinearCS lightCS = new DefaultLinearCS("Light",csAxis);
		final Map<String,Object> datumProperties = new HashMap<String,Object>();
		datumProperties.put("name", "light");
		
		final EngineeringDatum lightDatum = new DefaultEngineeringDatum( datumProperties );		
		CRS = new DefaultEngineeringCRS("Wave Length", lightDatum, lightCS );		
	}
	
	/**
	 * 
	 */
	public WavelengthAxis(final String name, final Unit<Length> uom) {
		super(new NameImpl(name),new SimpleInternationalString(name),uom);
	}
	/**
	 * These are units of length; as such the are
	 * not restricted to a coordinate reference system.
	 */
	public SingleCRS getCoordinateReferenceSystem() {		
		return CRS;
	}

	public InternationalString getDescription() {
		return new SimpleInternationalString("Spectral Information");
	}

	public Measure<MeasurementRange<Double>, Length> getKey(int keyIndex) {
		return this.keys.get(keyIndex);
	}

	public List<Measure<MeasurementRange<Double>, Length>> getKeys() {
		return Collections.unmodifiableList(keys);
	}

	public Name getName() {
		return name;
	}

	public int getNumKeys() {
		return keys.size();
	}

	public Unit<Length> getUnitOfMeasure() {
		return SI.MICRO(SI.METER);
	}
}