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
package org.geotools.validation.spatial;

import java.beans.PropertyDescriptor;

import org.geotools.validation.DefaultFeatureValidationBeanInfo;


/**
 * LineAbstractValidationBeanInfopurpose.
 * 
 * <p>
 * Description of LineAbstractValidationBeanInfo...
 * </p>
 *
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: LineNoSelfIntersectValidationBeanInfo.java,v 1.2 2004/04/21 11:07:10 jive Exp $
 */
public class LineNoSelfIntersectValidationBeanInfo extends DefaultFeatureValidationBeanInfo {
    /**
     * LineAbstractValidationBeanInfoconstructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public LineNoSelfIntersectValidationBeanInfo(){
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
        return super.getPropertyDescriptors();
    }
}