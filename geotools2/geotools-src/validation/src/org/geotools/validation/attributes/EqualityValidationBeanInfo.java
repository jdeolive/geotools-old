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
 * GazetteerNameValidationBeanInfo purpose.
 * <p>
 * Description of GazetteerNameValidationBeanInfo ...
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: EqualityValidationBeanInfo.java,v 1.1 2004/02/13 03:08:00 jive Exp $
 */
public class EqualityValidationBeanInfo extends DefaultFeatureValidationBeanInfo {
	
	/**
	 * GazetteerNameValidationBeanInfo constructor.
	 * <p>
	 * Description
	 * </p>
	 * 
	 */
	public EqualityValidationBeanInfo() {
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
			ResourceBundle resourceBundle = getResourceBundle(EqualityValidation.class);
			if(pd2 == null)
				pd2 = new PropertyDescriptor[0];
			PropertyDescriptor[] pd = new PropertyDescriptor[pd2.length + 3];
			int i=0;
			for(;i<pd2.length;i++)
				pd[i] = pd2[i];
			try{
				pd[i] = createPropertyDescriptor("attributeName",EqualityValidation.class,resourceBundle);
				pd[i].setExpert(false);
				pd[i+1] = createPropertyDescriptor("expected",EqualityValidation.class,resourceBundle);
				pd[i+1].setExpert(true);
				pd[i+2] = createPropertyDescriptor("filter",EqualityValidation.class,resourceBundle);
				pd[i+2].setExpert(true);
			}catch(IntrospectionException e){
				pd = pd2;
				// TODO error, log here
				e.printStackTrace();
			}
		return pd;
	}
}
