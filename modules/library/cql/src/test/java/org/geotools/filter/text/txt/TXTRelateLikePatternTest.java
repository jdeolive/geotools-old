/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

import java.util.List;

import org.geotools.filter.function.FilterFunction_buffer;
import org.geotools.filter.function.FilterFunction_relatePattern;
import org.geotools.filter.text.commons.CompilerUtil;
import org.geotools.filter.text.commons.Language;
import org.geotools.filter.text.cql2.CQLException;
import org.junit.Assert;
import org.junit.Test;
import org.opengis.filter.Not;
import org.opengis.filter.PropertyIsEqualTo;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.filter.expression.PropertyName;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKTReader;

/**
 * <p>
 * <pre>
 * &lt;like spatial-relation predicate&gt;  ::= &lt;relate&gt; [ "NOT" ] "LIKE" &lt;relate pattern&gt;
 * &lt;relate pattern&gt; ::= &lt;quote&gt;
 *              &lt;relate flag&gt;&lt;relate flag&gt;&lt;relate flag&gt;&lt;relate flag&gt;
 *              &lt;relate flag&gt;&lt;relate flag&gt;&lt;relate flag&gt;&lt;relate flag&gt;
 *              &lt;relate flag&gt;
 *      &lt;quote&gt;
 * 
 * &lt;relate flag&gt; ::= "T" | "F" | "*" | "0" | "1" | "2"
 * Sample:
 *      relate( geom1, geom2 ) like 'T*F****'
 *
 * </pre>
 * </p>
 * 
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
public final class TXTRelateLikePatternTest {


    /**
     * Sample: relate( geom1 ,geom2) like '2FFF1FFF2'
     * @throws Exception
     */
    @Test
    public void likeWithProperty() throws Exception{

        final String expectedProp1 = "geom1";
        final String expectedProp2 = "geom2";
        final String expectedPattern = "T**F*****";
        
        String stmt = "relate( "+expectedProp1+","+ expectedProp2+") like '"+expectedPattern+"'";
        
        PropertyIsEqualTo eq = (PropertyIsEqualTo)CompilerUtil.parseFilter(Language.TXT, stmt);
        
        Function f = (Function) eq.getExpression1();

        Assert.assertEquals(f.getName(),  "relatePattern");
        
        FilterFunction_relatePattern filterRelatePattern = (FilterFunction_relatePattern) f;
        
        List<?> args = filterRelatePattern.getParameters();
        
        PropertyName actualGeom1 = (PropertyName)args.get(0);
        PropertyName actualGeom2 = (PropertyName)args.get(1);
        Literal actualPattern = (Literal)args.get(2);

        Assert.assertEquals(expectedProp1, actualGeom1.toString());
        Assert.assertEquals(expectedProp2, actualGeom2.toString());
        Assert.assertEquals(expectedPattern, actualPattern.toString());
    }

    /**
     * Sample: relate( geom1 ,geom2) like '2FFF1FFF2'
     * @throws Exception
     */
    @Test
    public void notlikeWithProperty() throws Exception{

        final String expectedProp1 = "geom1";
        final String expectedProp2 = "geom2";
        final String expectedPattern = "T**F*****";
        
        String stmt = "relate( "+expectedProp1+","+ expectedProp2+") not like '"+expectedPattern+"'";
        
        Not notFilter = (Not)CompilerUtil.parseFilter(Language.TXT, stmt);

        PropertyIsEqualTo eq = (PropertyIsEqualTo)notFilter.getFilter();
        
        Function f = (Function) eq.getExpression1();

        Assert.assertEquals(f.getName(),  "relatePattern");
    }
    
    /**
     * Sample: relate( POLYGON((1 2, 1 10, 5 10, 1 2)),POLYGON((1 2, 1 10, 5 10, 1 2))) like '2FFF1FFF2'
     * @throws Exception
     */
    @Test
    public void likeWithGeometries() throws Exception{

        WKTReader reader = new WKTReader();

        final Geometry geom1 = reader.read("POLYGON((1 2, 1 10, 5 10, 1 2))");
        final Geometry geom2 = geom1;
        final String expectedPattern = "2FFF1FFF2"; // equals pattern
        
        String stmt = "relate( "+geom1.toString()+","+ geom2.toString()+") like '"+expectedPattern+"'";
        
        PropertyIsEqualTo eq = (PropertyIsEqualTo)CompilerUtil.parseFilter(Language.TXT, stmt);
        
        Function f = (Function) eq.getExpression1();

        Assert.assertEquals(f.getName(),  "relatePattern");
        
        FilterFunction_relatePattern filterRelatePattern = (FilterFunction_relatePattern) f;
        
        List<?> args = filterRelatePattern.getParameters();
        
        Literal actualGeom1 = (Literal)args.get(0);
        Literal actualGeom2 = (Literal)args.get(1);
        Literal actualPattern = (Literal)args.get(2);

        Assert.assertEquals(geom1.toString(), actualGeom1.getValue().toString());
        Assert.assertEquals(geom2.toString(), actualGeom2.getValue().toString());
        Assert.assertEquals(expectedPattern, actualPattern.toString());
    }
    
    /**
     * Sample: relate( POLYGON((1 2, 1 10, 5 10, 1 2)),buffer( the_geom , 10)) like '2FFF1FFF2'
     * @throws Exception
     */
    @Test
    public void likeWithFunctions() throws Exception{

        WKTReader reader = new WKTReader();

        final Geometry geom1 = reader.read("POLYGON((1 2, 1 10, 5 10, 1 2))");
        final String expectedPattern = "2FFF1FFF2"; // equals pattern
        
        String stmt = "relate( "+geom1.toString()+",buffer( the_geom , 10)) like '"+expectedPattern+"'";
        
        PropertyIsEqualTo eq = (PropertyIsEqualTo)CompilerUtil.parseFilter(Language.TXT, stmt);
        
        Function f = (Function) eq.getExpression1();

        Assert.assertEquals(f.getName(),  "relatePattern");
        
        FilterFunction_relatePattern filterRelatePattern = (FilterFunction_relatePattern) f;
        
        List<?> args = filterRelatePattern.getParameters();
        
        Literal actualGeom1 = (Literal)args.get(0);
        Function actualFunction = (Function)args.get(1);
        Literal actualPattern = (Literal)args.get(2);

        Assert.assertEquals(geom1.toString(), actualGeom1.getValue().toString());
        Assert.assertTrue(actualFunction instanceof FilterFunction_buffer);
        Assert.assertEquals(expectedPattern, actualPattern.toString());
    }

    /**
     * Sample1: relate( geom1,geom2) like '1*T***T**'
     * Sample2: relate( geom1,geom2) like '2FFF1FFF2'
     * @throws Exception 
     */
    @Test
    public void patternsDE9IM() throws Exception{

        // Lines overlap
        assertPattern("1*T***T**");
        
        // equals polygons
        assertPattern("2FFF1FFF2");

        // equals polygons
        assertPattern("2fff1fff2");
    }

    /**
     * Invalid character in DM-9IM 
     * @throws Exception
     */
    @Test(expected = CQLException.class)
    public void badPatternFlagDE9IM() throws Exception{
        // Lines overlap
        assertPattern("X*T***T**"); //X is an invalid character
        
    }
    /**
     * Invalid length of DM-9IM pattern
     *  
     * @throws Exception
     */
    @Test(expected = CQLException.class)
    public void badPatternLengthDE9IM() throws Exception{
        // Lines overlap
        assertPattern("*T***T**"); //should have 9 character
        
    }

    /**
     * Assertions to validate the DM-9IM pattern
     * 
     * @param expectedDE9IM
     * @throws Exception
     */
    private void assertPattern(final String expectedDE9IM) throws Exception{

        String stmt = "relate( geom1,geom2) like '"+expectedDE9IM+"'";
        
        PropertyIsEqualTo eq = (PropertyIsEqualTo)CompilerUtil.parseFilter(Language.TXT, stmt);
        
        Function f = (Function) eq.getExpression1();

        Assert.assertEquals(f.getName(),  "relatePattern");
        
        FilterFunction_relatePattern filterRelatePattern = (FilterFunction_relatePattern) f;
        
        List<?> args = filterRelatePattern.getParameters();
        
        Literal actualPattern = (Literal)args.get(2);

        Assert.assertEquals(expectedDE9IM, actualPattern.toString());
    }

}
