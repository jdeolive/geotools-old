/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;


/**
 * Defined a per Feature validation test.
 * 
 * <p>
 * Each ValidationPlugIn is very specific in nature: it performs one test
 * extermly well.  This simplifies design decisions, documenation
 * configuration and use.
 * </p>
 * 
 * <p>
 * Following the lead the excelent design work in the JUnit testing framework
 * validation results are collected by a ValidationResults object. This
 * interface for the ValidationResults object also allows it to collect
 * warning information.
 * </p>
 * 
 * <p>
 * The PlugIn is also required to supply some metadata to aid in its
 * deployment, scripting, logging and execution and error recovery:
 * 
 * <ul>
 * <li>
 * name: user's name of validation test
 * </li>
 * <li>
 * description: user's description of validation test
 * </li>
 * <li>
 * priority: used to schedule validation test
 * </li>
 * <li>
 * typeNames: used to connect validaiton test to transaction opperation
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * Capabilities:
 * 
 * <ul>
 * <li>
 * Uses FeatureResults to allow environment to gather error/warning information
 * as required (transaction XML document, JTable, logging system, etc...)
 * </li>
 * <li>
 * Primiarly used as part of processing an Insert Element in the Transaction
 * opperation of a Web Feature Server. (Allows us to fail a Feature without
 * bothering the Database)
 * </li>
 * </ul>
 * </p>
 * 
 * <p>
 * Example Use (feature: id=1, name="foo", geom=linestring):
 * <pre><code>
 * RangeFeatureValidation test = new RangeFeatureValidation();
 * 
 * results.setValidation( test );
 * test.setMin(0);
 * test.validate( feature, featureType, results ); // true
 * test.setMin(2);
 * test.validate( feature, featureType, results ); // false
 * </code></pre>
 * </p>
 *
 * @author Jody Garnett, Refractions Research, Inc.
 * @version $Id: FeatureValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public interface FeatureValidation extends Validation {
    /**
     * Used to check features against this validation rule.
     *
     * @param feature Feature to be Validated
     * @param type FeatureTypeInfo schema of feature
     * @param results coallate results information
     *
     * @return True if all the features pass this test.
     */
    public boolean validate(Feature feature, FeatureType type,
        ValidationResults results) throws Exception;
}
