package org.geotools.coverage.io.range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;
/**
 * 
 * @author Simone Giannecchini, GeoSolutions
 *
 * @param <V>
 * @param <QA>
 */
public class RangeDescription<V,QA extends Quantity> {

	/**
	 * The {@link List} of {@link Axis} for this {@link RangeDescription}.
	 */
	private List <Axis<QA>>axes;
	
	/**
	 * The {@link Name} for this {@link RangeDescription}.
	 */
	private Name name;
	
	/**
	 * The {@link Unit} for this {@link RangeDescription}.
	 */
	private Unit<QA> uom;
	
	/**
	 * The list of {@link Name}s for the {@link Axis} instances of this {@link RangeDescription}.
	 */
	private List<Name> axesNames;
	
	/**
	 * The description for this {@link RangeDescription}.
	 */
	private InternationalString description;

	
	private BandDescription bandDescription;


	/**
	 * 
	 * @param name
	 * @param description
	 * @param unit
	 * @param axes
	 * @param samples
	 */
	public RangeDescription(
			final Name fieldName,
	        final InternationalString fieldDescription,
	        final Unit<QA> UoM,
	        final List<Axis<QA>> axes,		// axes definitions cannot be repeated
	        final BandDescription band 		
	        ) {
	    this.name = fieldName;
	    this.description = fieldDescription;
	    this.axes = new ArrayList<Axis<QA>>(axes);
	    this.uom = UoM;
        this.bandDescription = band;
	    axesNames = new ArrayList<Name>(axes.size());
	    for (Axis<QA> axis : axes) {
	        axesNames.add(axis.getName());
	    }
	}

	/**
	 * {@link List} of all the axes of the {@link RangeDescription}
	 * 
	 * @return a {@link List} of all the {@link Axis} instances for this
	 *         {@link RangeDescription}
	 */
	public List<? extends Axis<QA>> getAxes() {
		return Collections.unmodifiableList(axes);
	}

	/**
	 * {@link List} of all the {@link Axis} instances
	 * {@link org.opengis.feature.type.Name}s.
	 * 
	 * @return a {@link List} of all the {@link Axis} instances
	 *         {@link org.opengis.feature.type.Name}s.
	 */
	public List<Name> getAxesNames() {
	    return Collections.unmodifiableList(axesNames);
	}

	/**
	 * Get the Axis by name
	 * 
	 * @param name
	 *                name of the Axis
	 * TODO improve me             
	 * @return Axis instance or null if not found
	 */
	public Axis<QA> getAxis(Name name) {
	    for (Axis<QA> axis : axes) {
	        if (axis.getName().toString().equalsIgnoreCase(name.toString()) ||
	                axis.getName().getLocalPart().equalsIgnoreCase(name.getLocalPart()))
	            return  axis;
	    }
	    throw new IllegalArgumentException("Unable to find axis for the specified name.");
	}

	/**
	 * Retrieves the Unit of measure for the values described by this RangeDescription.
	 *  
	 * <p>
	 * In case this {@link RangeDescription} is not made of measurable quantities we
	 * return <code>null</code>
	 * 
	 * @return the Unit of measure for the values described by this RangeDescription or
	 *         <code>null</code> in case this {@link RangeDescription} is not made of
	 *         measurable quantities
	 */
	public Unit<? extends Quantity> getUnitOfMeasure() {
		return uom; // TODO Is this duplicated with sample dimensions ?
	}

	/**
	 * Simple Implementation of toString method for debugging purpose.
	 */
	public String toString(){
	    final StringBuilder sb = new StringBuilder();
	    final String lineSeparator = System.getProperty("line.separator", "\n");
	    sb.append("FIELD TYPE description:").append(lineSeparator);
	    sb.append("Name:").append("\t\t").append(name.toString()).append(lineSeparator);
	    sb.append("Description:").append("\t").append(description.toString()).append(lineSeparator);
	    sb.append("UoM:").append("\t\t").append(uom.toString()).append(lineSeparator);
	    sb.append("Axes:").append(lineSeparator);
	    for (Axis<QA> axis : axes) {
	        sb.append(axis.toString()).append(lineSeparator);
	    }
        sb.append("BandDescription: ").append(bandDescription.toString());
        sb.append(lineSeparator);    
	    return sb.toString();
	}

	/**
	 * Get the description of the {@link RangeDescription}
	 * 
	 * @return description of the {@link RangeDescription}
	 */
	public InternationalString getDescription() {
		return description;
	}

	
	public  BandDescription getBandType() {
		return bandDescription;
	}
	


}
