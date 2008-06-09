/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.filter.text.txt;

import java.util.HashMap;
import java.util.Map;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.PropertyIsLessThan;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;


/**
 * Filter Samples for TXT language
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.5
 */
public final class FilterTXTSample {
    protected static final FilterFactory FACTORY = CommonFactoryFinder.getFilterFactory((Hints) null);

    // TXT Samples
    public static final String ABS_FUNCTION_LESS_PROPERTY = "abs(10) < aProperty";
    public static final String AREA_FUNCTION_LESS_NUMBER = "area( the_geom ) < 30000";
    public static final String EXPRESION_LESS_PROPERTY = "(1+3) > aProperty";
    
    public static final String FUNCTION_GREATER_PROPERTY = null;

    /** Maintains the TXT predicates (input) and the expected filters (output) */
    public static Map<String, Object> SAMPLES = new HashMap<String, Object>();

    static {
        Filter filter;

        //sample "(1+3) > prop1"
        filter = FACTORY.greater(FACTORY.add(FACTORY.literal(1), FACTORY.literal(3)),
                FACTORY.property("aProperty"));

        SAMPLES.put(EXPRESION_LESS_PROPERTY, filter);

        // abs(10) < aProperty
        Expression[] absArgs = new Expression[1];
        absArgs[0] = FACTORY.literal(10);

        Function abs = FACTORY.function("abs", absArgs);

        filter = FACTORY.less(abs, FACTORY.property("aProperty"));

        SAMPLES.put(ABS_FUNCTION_LESS_PROPERTY, filter);
        
        // area( the_geom ) < 30000
        Expression[] areaArgs = new Expression[1];
        areaArgs[0] = FACTORY.property("the_geom");

        Function area = FACTORY.function("area", areaArgs);

        filter = FACTORY.less(area, FACTORY.literal(30000));
        
        SAMPLES.put(AREA_FUNCTION_LESS_NUMBER, filter);
    }

    /**
     * @param predcateRequested
     * @return the filter expected for the predicate required
     */
    public static Filter getSample(final String predcateRequested) {
        Filter sample = (Filter) SAMPLES.get(predcateRequested);
        assert (sample != null) : "There is not a sample for " + predcateRequested;

        return sample;
    }
}
