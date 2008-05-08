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

import java.util.Collections;

import junit.framework.TestCase;

import org.geotools.data.DataUtilities;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.metadata.iso.citation.CitationImpl;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.filter.expression.Function;
import org.opengis.metadata.citation.Citation;


/**
 *
 * @since 2.4
 * @author Gabriel Roldan, Axios Engineering
 * @version $Id: PropertyExistsFunctionTest.java 24966 2007-03-30 11:33:47Z
 *          vmpazos $
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/library/cql/src/test/java/org/geotools/filter/function/PropertyExistsFunctionTest.java $
 */
public class PropertyExistsFunctionTest extends TestCase {
    private static final FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
    PropertyExistsFunction f;

    public void setUp() {
        f = new PropertyExistsFunction();
    }

    public void tearDown() {
        f = null;
    }

    public void testName() {
        assertEquals("propertyexists", f.getName().toLowerCase());
    }

    public void testFind() {
        Function function = ff.function("propertyexists", ff.property("testPropName"));
        assertNotNull(function);
    }

    public void testEvaluateFeature() throws SchemaException, IllegalAttributeException {
        SimpleFeatureType type = DataUtilities.createType("ns", "name:string,geom:Geometry");
        SimpleFeatureBuilder build = new SimpleFeatureBuilder(type);
        build.add("testName");
        build.add(null);
        SimpleFeature feature = build.buildFeature(null);

        f.setParameters(Collections.singletonList(ff.property("nonExistant")));
        assertEquals(Boolean.FALSE, f.evaluate(feature));

        f.setParameters(Collections.singletonList(ff.property("name")));
        assertEquals(Boolean.TRUE, f.evaluate(feature));

        f.setParameters(Collections.singletonList(ff.property("geom")));
        assertEquals(Boolean.TRUE, f.evaluate(feature));
    }

    public void testEvaluatePojo() {
        Citation pojo = new CitationImpl();

        f.setParameters(Collections.singletonList(ff.property("edition")));
        assertEquals(Boolean.TRUE, f.evaluate(pojo));

        f.setParameters(Collections.singletonList(ff.property("alternateTitles")));
        assertEquals(Boolean.TRUE, f.evaluate(pojo));

        // worng case (note the first letter)
        f.setParameters(Collections.singletonList(ff.property("AlternateTitles")));
        assertEquals(Boolean.FALSE, f.evaluate(pojo));

        f.setParameters(Collections.singletonList(ff.property("nonExistentProperty")));
        assertEquals(Boolean.FALSE, f.evaluate(pojo));
    }

    // @todo: REVISIT. don't we implement equals on functions/filters/etc?
    // public void testEquals(){
    // FilterFactory ff = CommonFactoryFinder.getFilterFactory(null);
    // Function actual = new PropertyExistsFunction();
    // f.setParameters(Collections.singletonList(ff.property("testPropName")));
    // actual.setParameters(Collections.singletonList(ff.property("testPropName")));
    // assertEquals(f, actual);
    // }
}
