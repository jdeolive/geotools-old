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
 *
 * @author Ian Schneider, USDA-ARS
 * @version $Id: AttributeTypeFactory.java,v 1.4 2003/08/05 21:33:26 cholmesny Exp $
 */
public abstract class AttributeTypeFactory implements Factory {
    /** The instance to be returned by {@link #defaultInstance()} */
    private static AttributeTypeFactory instance = null;

    /**
     * Returns the default attribute factory for the system - constucting a new
     * one if this is first time the method has been called.
     *
     * @return the default instance of AttributeTypeFactory.
     */
    public static AttributeTypeFactory defaultInstance() {
        if (instance == null) {
            instance = newInstance();
        }

        return instance;
    }

    /**
     * Returns a new instance of the current AttributeTypeFactory. If no
     * implementations are found then DefaultAttributeTypeFactory is returned.
     *
     * @return A new instance of an AttributeTypeFactory.
     */
    public static AttributeTypeFactory newInstance() {
        String attFactory = "org.geotools.feature.AttributeTypeFactory";

        return (AttributeTypeFactory) FactoryFinder.findFactory(attFactory,
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
     * @return A new AttributeType of name, clazz and isNillable.
     */
    public static AttributeType newAttributeType(String name, Class clazz,
        boolean isNillable) {
        return newInstance().createAttributeType(name, clazz, isNillable);
    }

    /**
     * Convenience method to just specify name and class.  Nulls are allowed as
     * attributes by default (isNillable = <code>true</code>).
     *
     * @param name The name of the AttributeType to be created.
     * @param clazz The class that objects will validate against.
     *
     * @return A new AttributeType of name and clazz.
     */
    public static AttributeType newAttributeType(String name, Class clazz) {
        return newAttributeType(name, clazz, true);
    }

    /**
     * Constucts a new AttributeType that accepts Features (specified by a
     * FeatureType)
     *
     * @param name The name of the AttributeType to be created.
     * @param type the FeatureType that features will validate agist
     * @param isNillable true iff nulls are allowed.
     *
     * @return A new AttributeType of name, type, and isNillable.
     */
    public static AttributeType newAttributeType(String name, FeatureType type,
        boolean isNillable) {
        return newInstance().createAttributeType(name, type, isNillable);
    }

    /**
     * Constucts a new AttributeType that accepts Feature (specified by a
     * FeatureType).  Nulls are allowed as attributes by default (isNillable =
     * <code>true</code>).
     *
     * @param name The name of the AttributeType to be created.
     * @param type the FeatureType that features will validate agist
     *
     * @return A new AttributeType of name and type.
     */
    public static AttributeType newAttributeType(String name, FeatureType type) {
        return newAttributeType(name, type, true);
    }

    /**
     * Method to handle the actual attributeCreation.  Must be implemented by
     * the extending class.
     *
     * @param name The name of the AttributeType to be created.
     * @param clazz The class that objects will validate against.
     * @param isNillable if nulls are allowed in the new type.
     *
     * @return the new AttributeType
     */
    protected abstract AttributeType createAttributeType(String name,
        Class clazz, boolean isNillable);

    /**
     * Method to handle the actual attributeCreation.  Must be implemented by
     * the extending class.
     *
     * @param name The name of the AttributeType to be created.
     * @param type The FeatureType that Features will validate against.
     * @param isNillable if nulls are allowed in the new type.
     *
     * @return the new AttributeType
     */
    protected abstract AttributeType createAttributeType(String name,
        FeatureType type, boolean isNillable);
}
