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
 * Factory for creating DefaultAttributeTypes.
 *
 * @author Ian Schneider
 * @version $Id: DefaultAttributeTypeFactory.java,v 1.4 2003/09/10 17:29:56 ianschneider Exp $
 */
public class DefaultAttributeTypeFactory extends AttributeTypeFactory {
    /**
     * Creates the DefaultAttributeType.
     *
     * @param name The name of the AttributeType to create.
     * @param clazz the class of the AttributeType to create.
     * @param isNillable whether the AttributeType should allow nulls.
     *
     * @return the newly created AttributeType
     */
    protected AttributeType createAttributeType(String name, Class clazz,
        boolean isNillable) {
        if (Number.class.isAssignableFrom(clazz)) {
            return new DefaultAttributeType.Numeric(name, clazz, isNillable);
        } else if (CharSequence.class.isAssignableFrom(clazz)) {
            return new DefaultAttributeType.Textual(name,isNillable);
        } else if (java.util.Date.class.isAssignableFrom(clazz)) {
            return new DefaultAttributeType.Temporal(name,isNillable);
        }
            

        return new DefaultAttributeType(name, clazz, isNillable);
    }

    /**
     * Creates the DefaultAttributeType.Feature
     *
     * @param name The name of the AttributeType to create.
     * @param type To use for validation.
     * @param isNillable whether the AttributeType should allow nulls.
     *
     * @return the newly created feature AttributeType.
     */
    protected AttributeType createAttributeType(String name, FeatureType type,
        boolean isNillable) {
        return new DefaultAttributeType.Feature(name, type, isNillable);
    }
}
