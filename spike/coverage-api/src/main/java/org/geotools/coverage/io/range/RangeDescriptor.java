package org.geotools.coverage.io.range;

import javax.measure.quantity.Quantity;

import org.opengis.feature.type.Name;
import org.opengis.util.InternationalString;
/**
 * 
 * @author Simone Giannecchini, GeoSolutions
 *
 * @param <V>
 * @param <QA>
 */
public class RangeDescriptor<QA extends Quantity> {

	/**
	 * The {@link Name} for this {@link RangeDescriptor}.
	 */
	private Name name;
	
	/**
	 * The description for this {@link RangeDescriptor}.
	 */
	private InternationalString description;

	
	private BandDescription<QA> bandDescriptor;


	/**
	 * 
	 * @param name
	 * @param description
	 * @param unit
	 * @param axes
	 * @param samples
	 */
	public RangeDescriptor(
			final Name fieldName,
	        final InternationalString fieldDescription,
	        final BandDescription<QA> band 		
	        ) {
	    this.name = fieldName;
	    this.description = fieldDescription;
        this.bandDescriptor = band;
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
        sb.append("BandDescriptor: ").append(bandDescriptor.toString());
        sb.append(lineSeparator);    
	    return sb.toString();
	}

	/**
	 * Get the description of the {@link RangeDescriptor}
	 * 
	 * @return description of the {@link RangeDescriptor}
	 */
	public InternationalString getDescription() {
		return description;
	}

	
	public  BandDescription<? extends Quantity> getBandDescriptor() {
		return bandDescriptor;
	}
	


}
