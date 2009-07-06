package org.geotools.coverage.io.range;

import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.geotools.util.Range;
import org.geotools.util.Utilities;
import org.opengis.annotation.Extension;
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
 * @version <A HREF="http://www.opengis.org/docs/01-004.pdf">Grid Coverage specification 1.0</A>
 * @author  Martin Desruisseaux (IRD)
 * @since   GeoAPI 1.0
 */
public interface BandType {
    public static class BandKey<V,QA extends Quantity> {
		
		private final List<Measure<V,QA>> bins;
	
		public BandKey(final List<Measure<V, QA>> bins) {
			this.bins = bins;
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
			for(Measure<V,QA> bin:bins){
				builder.append("Description of bin:\t\t").append(bin.toString()).append("\n");
			}
			return builder.toString();
		}
		
		
	}

	/**
     * Sample dimension title or description.
     * This string may be null or empty if no description is present.
     *
     * @return A description for this sample dimension.
     */
    InternationalString getDescription();
    
    public Name getName() ;

    /**
     * A code value indicating grid value data type.
     * This will also indicate the number of bits for the data type.
     *
     * @return A code value indicating grid value data type.
     * TODO convert me into enum
     */
    SampleDimensionType getSampleDimensionType();


    /**
     * Color interpretation of the sample dimension.
     * A sample dimension can be an index into a color palette or be a color model
     * component. If the sample dimension is not assigned a color interpretation the
     * value is {@link ColorInterpretation#UNDEFINED UNDEFINED}.
     *
     * @return The color interpretation of the sample dimension.
     *
     * @deprecated No replacement.
     * TODO convert me into enum
     */
    ColorInterpretation getColorInterpretation();

    /**
     * Values to indicate no data values for the sample dimension.
     * For low precision sample dimensions, this will often be no data values.
     *
     * @return The values to indicate no data values for the sample dimension.
     *
     * @see #getMinimumValue
     * @see #getMaximumValue
     */
    double[] getDefaultNoDataValues();

    /**
     * The minimum value occurring in the sample dimension.
     * If this value is not available, this value can be determined from the
     * {@link org.opengis.coverage.processing.GridAnalysis#getMinValue} operation.
     * This value can be empty if this value is not provided by the implementation.
     *
     * @return The minimum value occurring in the sample dimension.
     *
     * @see #getMaximumValue
     * @see #getNoDataValues
     */
    Range<Double> getDefaultRange();


    /**
     * The unit information for this sample dimension.
     * This interface typically is provided with grid coverages which represent
     * digital elevation data.
     * This value will be {@code null} if no unit information is available.
     *
     * @return The unit information for this sample dimension.
     */
    Unit<?> getUnit();

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
    @Extension
    MathTransform1D getDefaultSampleTransformation();
}