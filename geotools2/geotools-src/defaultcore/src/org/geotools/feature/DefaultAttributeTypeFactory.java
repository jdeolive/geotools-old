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
package org.geotools.feature;

/**
 * @version $Id: DefaultAttributeTypeFactory.java,v 1.2 2003/07/17 07:09:53 ianschneider Exp $
 * @author Ian Schneider
 */
public class DefaultAttributeTypeFactory extends AttributeTypeFactory {
  
  protected AttributeType createAttributeType(String name, Class clazz, boolean isNillable) {
    if (Number.class.isAssignableFrom(clazz)) {
      return new DefaultAttributeType.Numeric(name,clazz,isNillable); 
    }
    return new DefaultAttributeType(name,clazz, isNillable);
  }
  
  protected AttributeType createAttributeType(String name, FeatureType type, boolean isNillable) {
    return new DefaultAttributeType.Feature(name,type,isNillable);
  }
  
}
