/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
/*
 * Created on Jan 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.spatial;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ResourceBundle;

import org.geotools.validation.DefaultIntegrityValidationBeanInfo;


/**
 * LineAbstractValidationBeanInfopurpose.
 * 
 * <p>
 * Description of LineAbstractValidationBeanInfo...
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: PointLineAbstractValidationBeanInfo.java,v 1.2 2004/02/25 18:40:55 dmzwiers Exp $
 */
public class PointLineAbstractValidationBeanInfo extends DefaultIntegrityValidationBeanInfo{
    /**
     * LineAbstractValidationBeanInfoconstructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public PointLineAbstractValidationBeanInfo(){
        super();
    }

    /**
     * Implementation of getPropertyDescriptors.
     *
     * @return
     *
     * @see java.beans.BeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
    	PropertyDescriptor[] pd2 = super.getPropertyDescriptors();
    	ResourceBundle resourceBundle = getResourceBundle(PointLineAbstractValidation.class);

    	if (pd2 == null) {
    		pd2 = new PropertyDescriptor[0];
    	}

    	PropertyDescriptor[] pd = new PropertyDescriptor[pd2.length + 2];
    	int i = 0;

    	for (; i < pd2.length; i++)
    		pd[i] = pd2[i];

    	try {
    		pd[i] = createPropertyDescriptor("restrictedLineTypeRef",
    				PointLineAbstractValidation.class, resourceBundle);
    		pd[i+1].setExpert(true);
    		pd[i+1] = createPropertyDescriptor("pointTypeRef",
    				PointLineAbstractValidation.class, resourceBundle);
    		pd[i].setExpert(true);
    	} catch (IntrospectionException e) {
    		pd = pd2;

    		// TODO error, log here
    		e.printStackTrace();
    	}

    	return pd;
    }
}