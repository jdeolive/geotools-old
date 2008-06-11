package org.geotools.gui.swing.process;

import javax.swing.JComponent;
import javax.swing.JTextField;

import org.geotools.data.Parameter;
import org.geotools.text.Text;

/**
 *  Widget for double values
 * 
 * @author gdavis
 */
public class JDoubleField extends AbstractParamWidget {

    private JTextField blah;
    
	public JDoubleField(Parameter<?> parameter) {
		super( parameter );
	}
	
	public JComponent doLayout() {
		blah = new JTextField(16); 
	    return blah;
	}

	public Object getValue() {
	    String val = blah.getText();
	    if (val == null || val.equals("")) {
	    	return new Double(0);
	    }
	    try {
	    	return new Double(val);
	    }
	    catch (NumberFormatException e) {
	    	return new Double(0);
	    }
	}

	public void setValue(Object value) {
	    blah.setText(((Double)value).toString());
	}

	public boolean validate() {
		String val = blah.getText();
		try {
		    Double d = Double.parseDouble(val);
		}
		catch (NumberFormatException e) {
		    return false;
		}
		return true;
	}

}
