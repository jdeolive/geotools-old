/*
 * Created on Jan 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.attributes;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ResourceBundle;

import org.geotools.validation.DefaultFeatureValidationBeanInfo;

/**
 * SQLValidationBeanInfo purpose.
 * <p>
 * Description of SQLValidationBeanInfo ...
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: UniquityValidationBeanInfo.java,v 1.1 2004/02/13 03:08:00 jive Exp $
 */
public class UniquityValidationBeanInfo extends DefaultFeatureValidationBeanInfo {
	
	/**
	 * GazetteerNameValidationBeanInfo constructor.
	 * <p>
	 * Description
	 * </p>
	 * 
	 */
	public UniquityValidationBeanInfo() {
		super();
	}
	
	/**
	 * Implementation of getPropertyDescriptors.
	 * 
	 * @see java.beans.BeanInfo#getPropertyDescriptors()
	 * 
	 * @return
	 */
	public PropertyDescriptor[] getPropertyDescriptors(){
			PropertyDescriptor[] pd2 = super.getPropertyDescriptors();
			ResourceBundle resourceBundle = getResourceBundle(UniquityValidation.class);
			if(pd2 == null)
				pd2 = new PropertyDescriptor[0];
			PropertyDescriptor[] pd = new PropertyDescriptor[pd2.length + 1];
			int i=0;
			for(;i<pd2.length;i++)
				pd[i] = pd2[i];
			try{
				pd[i] = createPropertyDescriptor("attributeName",UniquityValidation.class,resourceBundle);
				pd[i].setExpert(false);

			}catch(IntrospectionException e){
				pd = pd2;
				// TODO error, log here
				e.printStackTrace();
			}
		return pd;
	}
}
