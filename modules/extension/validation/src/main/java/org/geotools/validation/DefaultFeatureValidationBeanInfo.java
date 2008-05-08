/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 * 
 *    Created on Jan 24, 2004
 */
package org.geotools.validation;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ResourceBundle;


/**
 * DefaultFeatureValidationBeanInfo purpose.
 * 
 * <p>
 * Description of DefaultFeatureValidationBeanInfo ...
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @source $URL$
 * @version $Id$
 */
public class DefaultFeatureValidationBeanInfo extends ValidationBeanInfo {
    /**
     * DefaultFeatureValidationBeanInfo constructor.
     * 
     * <p>
     * super
     * </p>
     */
    public DefaultFeatureValidationBeanInfo() {
        super();
    }

    /**
     * Implementation of getPropertyDescriptors.  This method should be called
     * by all overriding sub-class methods.  Property names 'name',
     * 'description', 'typeNames'
     *
     *
     * @see java.beans.BeanInfo#getPropertyDescriptors()
     */
    public PropertyDescriptor[] getPropertyDescriptors() {
        try {
            PropertyDescriptor[] tmp = super.getPropertyDescriptors();
            PropertyDescriptor[] pd = new PropertyDescriptor[1 + tmp.length];
            ResourceBundle resourceBundle = getResourceBundle(DefaultFeatureValidation.class);
            int i = 0;

            for (; i < tmp.length; i++)
                pd[i] = tmp[i];

            pd[i] = createPropertyDescriptor("typeRef",
                    DefaultFeatureValidation.class, resourceBundle);
            pd[i].setExpert(false);

            return pd;
        } catch (IntrospectionException e) {
            // TODO error, log here
            e.printStackTrace();

            return new PropertyDescriptor[0];
        }
    }
}
