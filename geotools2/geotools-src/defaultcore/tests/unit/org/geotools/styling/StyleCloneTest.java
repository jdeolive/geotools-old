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
package org.geotools.styling;

import junit.framework.TestCase;


/** Tests style cloning
 *
 * @author Sean Geoghegan
 */
public class StyleCloneTest extends TestCase {
    private StyleFactory styleFactory;

    /**
     * Constructor for StyleCloneTest.
     *
     * @param arg0
     */
    public StyleCloneTest(String arg0) {
        super(arg0);
    }

    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        styleFactory = StyleFactory.createStyleFactory();
    }

    /*
     * @see TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        styleFactory = null;
    }

    public void testStyleClone() throws Exception {
        Style style = styleFactory.getDefaultStyle();
        Style clone = (Style) style.clone();
        assertTrue("Style was not cloned", style != clone);
        assertEquals(style.getName(), clone.getName());
    }

    public void testFeatureTypeStyleClone() throws Exception {
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle();
        FeatureTypeStyle clone = (FeatureTypeStyle) fts.clone();
        assertTrue("FeatureTypeStyle was not cloned", fts != clone);
        assertEquals(fts.getName(), fts.getName());
    }

    public void testRuleClone() throws Exception {
        Rule rule = styleFactory.createRule();
        Rule clone = (Rule) rule.clone();
        assertTrue("Rule was not cloned", rule != clone);
        assertEquals(rule.getName(), clone.getName());
    }
}
