/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.filter.function;

import org.apache.commons.beanutils.PropertyUtils;

/**
 * Function expression that returns a Boolean indicating if a given property
 * exists in the structure of the object being evaluated.
 *
 * @since 2.4
 * @author Gabriel Roldan, Axios Engineering
 * @author Mauricio Pazos, Axios Engineering
 * @version $Id$
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/library/cql/src/main/java/org/geotools/filter/function/PropertyExistsFunction.java $
 */
import java.lang.reflect.InvocationTargetException;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;
import org.geotools.filter.FunctionExpressionImpl;
import org.geotools.resources.Utilities;

/**
 * A new function to check if a property exists.
 */
public class PropertyExistsFunction extends FunctionExpressionImpl {
    public PropertyExistsFunction() {
        super("PropertyExists");
    }

    public int getArgCount() {
        return 1;
    }

    private String getPropertyName() {
        Expression expr = (Expression) getParameters().get(0);

        return getPropertyName(expr);
    }

    private String getPropertyName(Expression expr) {
        String propertyName;

        if (expr instanceof Literal) {
            propertyName = String.valueOf(((Literal) expr).getValue());
        } else if (expr instanceof PropertyName) {
            propertyName = ((PropertyName) expr).getPropertyName();
        } else {
            throw new IllegalStateException("Not a property name expression: " + expr);
        }

        return propertyName;
    }

    /**
     * @return {@link Boolean#TRUE} if the <code>feature</code>'s
     *         {@link FeatureType} contains an attribute named as the property
     *         name passed as this function argument, {@link Boolean#FALSE}
     *         otherwise.
     */
    public Object evaluate(SimpleFeature feature) {
        String propName = getPropertyName();
        AttributeDescriptor attributeType = feature.getFeatureType().getAttribute(propName);

        return Boolean.valueOf(attributeType != null);
    }

    /**
     * @return {@link Boolean#TRUE} if the Class of the object passed as
     *         argument defines a property names as the property name passed as
     *         this function argument, following the standard Java Beans naming
     *         conventions for getters. {@link Boolean#FALSE} otherwise.
     */
    public Object evaluate(Object bean) {
        if (bean instanceof SimpleFeature) {
            return evaluate((SimpleFeature) bean);
        }

        final String propName = getPropertyName();

        Boolean propertyExists = Boolean.TRUE;

        try {
            PropertyUtils.getProperty(bean, propName);
        } catch (NoSuchMethodException e) {
            propertyExists = Boolean.FALSE;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }

        return propertyExists;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("PropertyExists('");
        sb.append(getPropertyName());
        sb.append("')");

        String stringVal = sb.toString();

        return stringVal;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Function)) {
            return false;
        }

        Function other = (Function) obj;

        if (!Utilities.equals(getName(), other.getName())) {
            return false;
        }

        final String propName = getPropertyName();

        Expression otherPropNameExpr = (Expression) other.getParameters().get(0);
        final String otherPropName = getPropertyName(otherPropNameExpr);

        return Utilities.equals(propName, otherPropName);
    }
}
