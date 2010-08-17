/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2010, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.xml;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link AppSchemaValidator}.
 * 
 * @author Ben Caradoc-Davies, CSIRO Earth Science and Resource Engineering
 */
public class AppSchemaValidatorTest {

    /**
     * Test that validation succeeds for a known-valid XML instance document.
     */
    @Test
    public void validateErMineralOccurrence() {
        AppSchemaValidator.validate("/test-data/er_MineralOccurrence.xml");
    }

    /**
     * Test that validation fails with an expected error message for a known-invalid XML instance
     * document.
     */
    @Test
    public void validateErMineralOccurrenceWithErrors() {
        try {
            AppSchemaValidator.validate("/test-data/er_MineralOccurrence_with_errors.xml");
            Assert.fail("Unexpected validation success for known-invalid input");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), e.getMessage().startsWith(
                    "Schema validation failures: 2" + System.getProperty("line.separator")));
        }
    }

    /**
     * Test that validation succeeds for a WFS 2.0 / GML 3.2 example from an annex of a draft of the
     * WFS 2.0 specification.
     */
    @Test
    public void validateWfs20Example01() {
        AppSchemaValidator.validate("/test-data/Example01.xml");
    }

}
