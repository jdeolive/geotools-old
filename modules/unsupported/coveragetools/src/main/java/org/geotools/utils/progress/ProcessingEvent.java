package org.geotools.utils.progress;

import java.util.EventObject;

/**
 * @author Simone Giannecchini, GeoSolutions.
 * 
 */
public class ProcessingEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6930580659705360225L;

	private String message = null;

	private double percentage = 0;

	/**
	 * @param source
	 */
	public ProcessingEvent(final Object source, final String message,
			final double percentage) {
		super(source);
		this.message = message;
		this.percentage = percentage;
	}

	public double getPercentage() {
		return percentage;
	}
    
    public String getMessage() {
        return message;
    }

}
