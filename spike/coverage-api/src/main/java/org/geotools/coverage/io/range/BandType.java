package org.geotools.coverage.io.range;


import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Quantity;

import org.geotools.feature.NameImpl;
import org.geotools.util.NumberRange;
import org.geotools.util.SimpleInternationalString;
import org.geotools.util.Utilities;
import org.opengis.coverage.ColorInterpretation;
import org.opengis.coverage.Coverage;
import org.opengis.coverage.SampleDimensionType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.operation.MathTransform1D;
import org.opengis.util.InternationalString;


/**
 * Contains information for an individual sample dimension of {@linkplain Coverage coverage}.
 * This interface is applicable to any coverage type.
 * For {@linkplain org.opengis.coverage.grid.GridCoverage grid coverages},
 * the sample dimension refers to an individual band.
 *
 * <P>&nbsp;</P>
 * <TABLE WIDTH="80%" ALIGN="center" CELLPADDING="18" BORDER="4" BGCOLOR="#FFE0B0">
 *   <TR><TD>
 *     <P align="justify"><STRONG>WARNING: THIS CLASS WILL CHANGE.</STRONG> Current API is derived from OGC
 *     <A HREF="http://www.opengis.org/docs/01-004.pdf">Grid Coverages Implementation specification 1.0</A>.
 *     We plan to replace it by new interfaces derived from ISO 19123 (<CITE>Schema for coverage geometry
 *     and functions</CITE>). Current interfaces should be considered as legacy and are included in this
 *     distribution only because they were part of GeoAPI 1.0 release. We will try to preserve as much
 *     compatibility as possible, but no migration plan has been determined yet.</P>
 *   </TD></TR>
 * </TABLE>
 *
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 */
@SuppressWarnings("deprecation")
public class BandType {	

	/**
	 * 
	 * @author Simone Giannecchini, GeoSolutions S.A.S.
	 *
	 * @param <V>
	 * @param <QA>
	 */
	public static class BandKey<V,QA extends Quantity> {
		
		private final List<AxisBin<V,QA>> bins;
	
		public BandKey(final List<? extends AxisBin<V, QA>> bins) {
			this.bins = new ArrayList<AxisBin<V,QA>>(bins);
		}
	
	
		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			if(this==obj)
				return true;
			if(!(obj instanceof BandKey))
				return false;
			final BandKey that=(BandKey) obj;
			return Utilities.deepEquals(this.bins, that.bins);
		}
	
		@Override
		public int hashCode() {
			return Utilities.deepHashCode(this.bins);
		}
	
		@Override
		public String toString() {
			final StringBuilder builder= new StringBuilder();
			builder.append("Description of band key:").append("\n");
			for(AxisBin<V,QA> bin:bins){
				builder.append("Description of bin:\n").append(bin.toString()).append("\n");
			}
			return builder.toString();
		}
		
		
	}

	public BandType(ColorInterpretation colorInterpretation,
			double[] defaultNoDatavalues, NumberRange<Double> defaultRange,
			MathTransform1D defaultSampleTransformation, String name,
			String description,
			SampleDimensionType sampleDimensionType) {
		super();
		this.defaultColorInterpretation = colorInterpretation;
		this.defaultNoDatavalues = defaultNoDatavalues;
		this.defaultRange = defaultRange;
		this.defaultSampleTransformation = defaultSampleTransformation;
		this.name = new NameImpl(name);
		this.description = new SimpleInternationalString(description);
		this.defaultSampleDimensionType = sampleDimensionType;
	}
	public BandType(ColorInterpretation colorInterpretation,
			double[] defaultNoDatavalues, NumberRange<Double> defaultRange,
			MathTransform1D defaultSampleTransformation, Name name,
			InternationalString description,
			SampleDimensionType sampleDimensionType) {
		super();
		this.defaultColorInterpretation = colorInterpretation;
		this.defaultNoDatavalues = defaultNoDatavalues;
		this.defaultRange = defaultRange;
		this.defaultSampleTransformation = defaultSampleTransformation;
		this.name = name;
		this.description = description;
		this.defaultSampleDimensionType = sampleDimensionType;
	}
	public ColorInterpretation getDefaultColorInterpretation() {
		return defaultColorInterpretation;
	}

	public double[] getDefaultNoDatavalues() {
		return defaultNoDatavalues;
	}

	public NumberRange<Double> getDefaultRange() {
		return defaultRange;
	}

	public MathTransform1D getDefaultSampleTransformation() {
		return defaultSampleTransformation;
	}

	public Name getName() {
		return name;
	}

	public InternationalString getDescription() {
		return description;
	}

	private ColorInterpretation defaultColorInterpretation;
	
	private double[] defaultNoDatavalues;
	
	private NumberRange<Double> defaultRange;
	
	private MathTransform1D defaultSampleTransformation;
	
	private Name name;
	
	private InternationalString description;
	
	private SampleDimensionType defaultSampleDimensionType;

	public SampleDimensionType getDefaultSampleDimensionType() {
		return defaultSampleDimensionType;
	}
}
