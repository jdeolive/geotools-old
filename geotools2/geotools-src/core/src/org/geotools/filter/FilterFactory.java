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
package org.geotools.filter;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.feature.FeatureType;

// J2SE dependencies
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.geotools.factory.*;


public abstract class FilterFactory implements Factory {

    private static FilterFactory factory = null;

    /**
     * creates an instance of a Filter factory
     *
     * @return an instance of the Filter factory
     */
    public static FilterFactory createFilterFactory() 
    throws FactoryConfigurationError { 

      if (factory == null) {

        factory = (FilterFactory) FactoryFinder.findFactory(
          "org.geotools.filter.FilterFactory",
          "org.geotools.filter.FilterFactoryImpl"
        );
      }
      return factory;

        
    }

    public abstract LogicFilter createLogicFilter(Filter filter1,
        Filter filter2, short filterType) throws IllegalFilterException;

    public abstract LogicFilter createLogicFilter(short filterType)
        throws IllegalFilterException;

    public abstract LogicFilter createLogicFilter(Filter filter,
        short filterType) throws IllegalFilterException;

    public abstract BBoxExpression createBBoxExpression(Envelope env)
        throws IllegalFilterException;

    public abstract LiteralExpression createLiteralExpression(int i);

    public abstract MathExpression createMathExpression()
        throws IllegalFilterException;

    public abstract FidFilter createFidFilter();

    public abstract AttributeExpression createAttributeExpression(
        FeatureType schema, String path) throws IllegalFilterException;

    public abstract LiteralExpression createLiteralExpression(Object o)
        throws IllegalFilterException;

    public abstract CompareFilter createCompareFilter(short type)
        throws IllegalFilterException;

    public abstract LiteralExpression createLiteralExpression();

    public abstract LiteralExpression createLiteralExpression(String s);

    public abstract LiteralExpression createLiteralExpression(double d);

    public abstract AttributeExpression createAttributeExpression(
        FeatureType schema);

    public abstract MathExpression createMathExpression(short expressionType)
        throws IllegalFilterException;

    public abstract NullFilter createNullFilter();

    public abstract BetweenFilter createBetweenFilter()
        throws IllegalFilterException;

    public abstract GeometryFilter createGeometryFilter(short filterType)
        throws IllegalFilterException;

    public abstract GeometryDistanceFilter createGeometryDistanceFilter(
        short filterType) throws IllegalFilterException;

    public abstract FidFilter createFidFilter(String fid);

    public abstract LikeFilter createLikeFilter();

    public abstract FunctionExpression createFunctionExpression(String name);


}
