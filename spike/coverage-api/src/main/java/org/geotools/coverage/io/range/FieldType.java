package org.geotools.coverage.io.range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.Measure;
import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.geotools.coverage.io.range.BandType.BandKey;
import org.opengis.coverage.SampleDimension;
import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;

public class FieldType<V,QA extends Quantity> {

	/**
	 * The {@link List} of {@link Axis} for this {@link Field}.
	 */
	private List <? extends Axis<?,?>>axes;
	/**
	 * The {@link Name} for this {@link Field}.
	 */
	private Name name;
	/**
	 * The {@link Unit} for this {@link Field}.
	 */
	private Unit<QA> uom;
	/**
	 * The list of {@link Name}s for the {@link Axis} instances of this {@link Field}.
	 */
	private List<Name> axesNames;
	/**
	 * The description for this {@link Field}.
	 */
	private InternationalString description;
	/**
	 * This {@link Map} holds the mapping between the Keys ( {@link Measure} instances) and the {@link SampleDimension}s 
	 * for this field.
	 * 
	 */
	private Map<BandKey<V,QA>,BandType> bandTypes;


	/**
	 * 
	 * @param name
	 * @param description
	 * @param unit
	 * @param axes
	 * @param samples
	 */
	public FieldType(
			final Name fieldName,
	        final InternationalString fieldDescription,
	        final Unit<QA> UoM,
	        final List<? extends Axis<V,QA>> axes,	
	        final Map<BandKey<V, QA>,BandType> bands) {
	    this.name = fieldName;
	    this.description = fieldDescription;
	    this.axes = new ArrayList<Axis<V,QA>>(axes);
	    this.uom = UoM;
        this.bandTypes = new HashMap<BandKey<V, QA>,BandType>(bands);
	    axesNames = new ArrayList<Name>(axes.size());
	    for (Axis<?,?> axis : axes) {
	        axesNames.add(axis.getName());
	    }
	}

	/**
	 * {@link List} of all the axes of the {@link Field}
	 * 
	 * @return a {@link List} of all the {@link Axis} instances for this
	 *         {@link Field}
	 */
	public List<Axis<?,?>> getAxes() {
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
	 * @return Axis instance or null if not found
	 */
	public Axis<?,?> getAxis(Name name) {
	    for (Axis<?,?> axis : axes) {
	        if (axis.getName().toString().equalsIgnoreCase(name.toString()) ||
	                axis.getName().getLocalPart().equalsIgnoreCase(name.getLocalPart()))
	            return  axis;
	    }
	    throw new IllegalArgumentException("Unable to find axis for the specified name.");
	}

	/**
	 * Retrieves the Unit of measure for the values described by this field.
	 *  
	 * <p>
	 * In case this {@link Field} is not made of measurable quantities we
	 * return <code>null</code>
	 * 
	 * @return the Unit of measure for the values described by this field or
	 *         <code>null</code> in case this {@link Field} is not made of
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
	    sb.append("Name:").append(name.toString()).append(lineSeparator);
	    sb.append("Description:").append(description.toString()).append(lineSeparator);
	    sb.append("UoM:").append(uom.toString()).append(lineSeparator);
	    sb.append("Axes:").append(lineSeparator);
	    for (Axis<?,?> axis : axes) {
	        sb.append("   axisName:").append(axis.getName().toString());
	        sb.append(" axisDescription:").append(axis.getDescription().toString());
	        sb.append(" axisUoM:").append(axis.getUnitOfMeasure().toString());
	        List<? extends Measure<?, ?>> axisKeys = axis.getKeys();
	        for (Measure<?, ?> measure: axisKeys){
	            sb.append(" key:").append(measure.toString());
	        }
	        sb.append(lineSeparator);
	    }
        
        sb.append("SampleDimensions:").append(lineSeparator);
        for (BandType band :  bandTypes.values()){
            sb.append("BandType: ").append(band.toString());
        }	    
	    return sb.toString();
	}

	/**
	 * Get the description of the {@link Field}
	 * 
	 * @return description of the {@link Field}
	 */
	public InternationalString getDescription() {
		return description;
	}

	/**
	 * Look up the SampleDimension by key (as described by Axis)
	 * 
	 * @param key
	 *                key of the SampleDimension
	 * @return {@link SampleDimension} instance or null if not found
	 */
	public BandType getBandType(BandKey<V,QA> key) {
		if(this.bandTypes.containsKey(key))
			return bandTypes.get(key);
		throw new IllegalArgumentException("Unable to find SampleDimension for the specified key.");
	}
	

	/**
	 * List the SampleDimensions of the measure.
	 * 
	 * @return Set of {@link SampleDimension} instances
	 */
	
	public List<BandType> getBandTypes() {
	    return new ArrayList<BandType>(this.bandTypes.values());
	}

}
