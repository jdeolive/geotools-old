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
 * Created on Jan 22, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.attributes;

import org.geotools.validation.DefaultFeatureValidationBeanInfo;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ResourceBundle;


/**
 * RangeValidationBeanInfo purpose.
 * 
 * <p>
 * Description of RangeValidationBeanInfo ...
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: RangeValidationBeanInfo.java,v 1.3 2004/04/17 00:02:59 jive Exp $
 */
public class RangeValidationBeanInfo extends DefaultFeatureValidationBeanInfo {
    /**
     * GazetteerNameValidationBeanInfo constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public RangeValidationBeanInfo() {
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
        ResourceBundle resourceBundle = getResourceBundle(RangeValidation.class);

        if (pd2 == null) {
            pd2 = new PropertyDescriptor[0];
        }

        PropertyDescriptor[] pd = new PropertyDescriptor[pd2.length + 3];
        int i = 0;

        for (; i < pd2.length; i++)
            pd[i] = pd2[i];

        try {
            pd[i] = createPropertyDescriptor("name", RangeValidation.class,
                    resourceBundle);
            pd[i].setExpert(false);
            pd[i + 1] = createPropertyDescriptor("max", RangeValidation.class,
                    resourceBundle);
            pd[i + 1].setExpert(false);
            pd[i + 2] = createPropertyDescriptor("min", RangeValidation.class,
                    resourceBundle);
            pd[i + 2].setExpert(false);
        } catch (IntrospectionException e) {
            pd = pd2;

            // TODO error, log here
            e.printStackTrace();
        }

        return pd;
    }
}
