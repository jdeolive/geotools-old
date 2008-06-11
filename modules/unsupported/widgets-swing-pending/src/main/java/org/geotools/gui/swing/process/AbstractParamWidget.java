package org.geotools.gui.swing.process;

import javax.swing.JComponent;

import org.geotools.data.Parameter;
import org.geotools.text.Text;

/**
 * Super class that provides additional helper methods
 * useful when implementing your own ParamWidget.

 * @author gdavis
 */
public abstract class AbstractParamWidget implements ParamWidget {
	protected final Parameter< ? > parameter;
	
	/**
	 * Holds on to the parameter so implementations
	 * can consult the type and metadata information.
	 * 
	 * @param parameter
	 */
	AbstractParamWidget( Parameter<?> parameter ){
	    this.parameter = parameter; 
	}
}
