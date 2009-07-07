package org.geotools.coverage.io.range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

import org.geotools.coverage.io.range.BandType.BandKey;
import org.opengis.coverage.SampleDimension;
import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;
/**
 * 
 * @author Simone Giannecchini, GeoSolutions
 *
 * @param <V>
 * @param <QA>
 */
public class FieldType<V,QA extends Quantity> {

	/**
	 * The {@link List} of {@link Axis} for this {@link FieldType}.
	 */
	private List <Axis<QA>>axes;
	
	/**
	 * The {@link Name} for this {@link FieldType}.
	 */
	private Name name;
	
	/**
	 * The {@link Unit} for this {@link FieldType}.
	 */
	private Unit<QA> uom;
	
	/**
	 * The list of {@link Name}s for the {@link Axis} instances of this {@link FieldType}.
	 */
	private List<Name> axesNames;
	
	/**
	 * The description for this {@link FieldType}.
	 */
	private InternationalString description;

	
	private HashMap<BandKey<V, QA>,BandType> bandTypes;


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
	        final List<Axis<QA>> axes,		// axes definitions cannot be repeated
	        final Map<? extends BandKey<V, QA>,? extends BandType> bands 		
	        ) {
	    this.name = fieldName;
	    this.description = fieldDescription;
	    this.axes = new ArrayList<Axis<QA>>(axes);
	    this.uom = UoM;
        this.bandTypes = new HashMap<BandKey<V, QA>,BandType>(bands);
	    axesNames = new ArrayList<Name>(axes.size());
	    for (Axis<QA> axis : axes) {
	        axesNames.add(axis.getName());
	    }
	}

	/**
	 * {@link List} of all the axes of the {@link FieldType}
	 * 
	 * @return a {@link List} of all the {@link Axis} instances for this
	 *         {@link FieldType}
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
	 * Retrieves the Unit of measure for the values described by this FieldType.
	 *  
	 * <p>
	 * In case this {@link FieldType} is not made of measurable quantities we
	 * return <code>null</code>
	 * 
	 * @return the Unit of measure for the values described by this FieldType or
	 *         <code>null</code> in case this {@link FieldType} is not made of
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
        
        sb.append("BandTypes:").append(lineSeparator);
        for (BandKey<V,QA> bk :  bandTypes.keySet()){
            sb.append("BandKey: ").append(bk.toString());
            sb.append("BandType: ").append(bandTypes.get(bk).toString());
            sb.append(lineSeparator);
        }	    
	    return sb.toString();
	}

	/**
	 * Get the description of the {@link FieldType}
	 * 
	 * @return description of the {@link FieldType}
	 */
	public InternationalString getDescription() {
		return description;
	}

	/**
	 * Look up the SampleDimension by key (as described by Axis)
	 * 
	 * @param key
	 *                key of the SampleDimension
	 * TODO improve me
	 * @return {@link SampleDimension} instance or null if not found
	 */
	public BandType getBandType(final Name name) {
		for(BandType type:bandTypes.values())
		{
			if(type.getName().equals(name))
				return type;
		}
		throw new IllegalArgumentException("Unable to find SampleDimension for the specified key.");
	}
	
	public  BandType getBandType(final BandKey<V, QA> key) {
		if(bandTypes.containsKey(key))
			return bandTypes.get(key);
		throw new IllegalArgumentException("Unable to find SampleDimension for the specified key.");
	}
	

	/**
	 * List the SampleDimensions of the measure.
	 * 
	 * @return Set of {@link SampleDimension} instances
	 */
	
	public Map<BandKey<V, QA>,BandType> getBandTypes() {
	    return bandTypes;
	}

}
