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

import org.geotools.factory.Factory;
import org.geotools.factory.FactoryFinder;



/**
 * Abstract class for AttributeType factories.  Extending classes need only
 * implement createAttributeType
 * @verson $Id: AttributeTypeFactory.java,v 1.2 2003/07/17 07:09:52 ianschneider Exp $
 * @author Ian Schneider, USDA-ARS
 */
public abstract class AttributeTypeFactory implements Factory {
  
    private static AttributeTypeFactory instance = null;
  
    public static AttributeTypeFactory defaultInstance() {
      if (instance == null)
        instance = newInstance();
      return instance;
    }
    /**
     * Returns a new instance of the current AttributeTypeFactory. If no
     * implementations are found then DefaultAttributeTypeFactory is returned.
     *
     * @return A new instance of an AttributeTypeFactory.
     */
    public static AttributeTypeFactory newInstance() {
        return (AttributeTypeFactory) FactoryFinder.findFactory("org.geotools.feature.AttributeTypeFactory",
            "org.geotools.feature.DefaultAttributeTypeFactory");
    }

    /**
     * Creates a new AttributeType with the given name, class and nillable
     * values.
     *
     * @param name The name of the AttributeType to be created.
     * @param clazz The class that objects will validate against.
     * @param isNillable if nulls are allowed in the new type.
     *
     * @return a new attributeType of name, clazz and isNillable.
     */
    public static AttributeType newAttributeType(String name, Class clazz,
        boolean isNillable) {

        return newInstance().createAttributeType(name, clazz, isNillable);
    }

    /**
     * Convenience method to just specify name and class.  Nulls are allowed as
     * attributes by default (isNillable = <code>true</code>
     *
     * @param name The name of the AttributeType to be created.
     * @param clazz The class that objects will validate against.
     *
     * @return a new attributeType of name and clazz
     */
    public static AttributeType newAttributeType(String name, Class clazz) {
        return newAttributeType(name, clazz, true);
    }
    
    public static AttributeType newAttributeType(String name, FeatureType type, boolean isNillable) {
      return newInstance().createAttributeType(name,type,isNillable);
    }
    
    public static AttributeType newAttributeType(String name, FeatureType type) {
      return newAttributeType(name,type, true); 
    }
    

    /**
     * Method to handle the actual attributeCreation.  Must be implemented by
     * the extending class.
     *
     * @param name The name of the AttributeType to be created.
     * @param clazz The class that objects will validate against.
     * @param isNillable if nulls are allowed in the new type.
     *
     * @return the new attributeType
     */
    protected abstract AttributeType createAttributeType(String name,
        Class clazz, boolean isNillable);
    
    protected abstract AttributeType createAttributeType(String name,
      FeatureType type,boolean isNillable);
}
