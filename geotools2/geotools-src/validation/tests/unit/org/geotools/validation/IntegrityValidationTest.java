/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation;

import org.geotools.data.DataTestCase;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.validation.attributes.UniqueFIDValidation;
import org.geotools.validation.spatial.IsValidGeometryValidation;
import java.util.HashMap;


/**
 * IntegrityValidationTest purpose.
 * 
 * <p>
 * Description of IntegrityValidationTest ...
 * </p>
 * 
 * <p></p>
 *
 * @author jgarnett, Refractions Research, Inc.
 * @author $Author: sploreg $ (last modification)
 * @version $Id: IntegrityValidationTest.java,v 1.1 2004/04/29 21:57:32 sploreg Exp $
 */
public class IntegrityValidationTest extends DataTestCase {
    MemoryDataStore store;

    /**
     * FeatureValidationTest constructor.
     * 
     * <p>
     * Run test <code>testName</code>.
     * </p>
     *
     * @param testName
     */
    public IntegrityValidationTest(String testName) {
        super(testName);
    }

    /**
     * Construct data store for use.
     *
     * @throws Exception
     *
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        store = new MemoryDataStore();
        store.addFeatures(roadFeatures);
        store.addFeatures(riverFeatures);
    }

    /**
     * Override tearDown.
     *
     * @throws Exception
     *
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception {
        store = null;
        super.tearDown();
    }

    public void testUniqueFIDIntegrityValidation() throws Exception {
        // the visitor
        RoadValidationResults validationResults = new RoadValidationResults();

        UniqueFIDValidation validator = new UniqueFIDValidation();
        validator.setName("isValidRoad");
        validator.setDescription("Tests to see if a road is valid");
        validator.setTypeRef( "*" );
        validationResults.setValidation(validator);

        HashMap layers = new HashMap();
        layers.put("road", store.getFeatureSource("road"));
        layers.put("river", store.getFeatureSource("river"));

        assertTrue(validator.validate(layers, null, validationResults)); // validate will return true
    }
}
