/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation;

import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Used to check geospatial information for integrity.
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
 * Primiarly used as part of processing the Transaction opperation of a Web
 * Feature Server. Used to ensure that the DataStore is consistent before
 * commiting a Transaction.
 * </li>
 * </ul>
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public interface IntegrityValidation extends Validation {
    /**
     * Used to check features against this validation rule.
     * 
     * <p>
     * The layers Map is still under developement, current thinking involves
     * storing a FeatureSource of the correct typeName requested by
     * getTypeNames(), using the current geotools2 Transaction as the
     * opperation being validated.
     * </p>
     * 
     * <p>
     * We may need to extend this information to provide:
     * 
     * <ul>
     * <li>
     * FeatureTypeMetaData: we may with to configure against metadata
     * </li>
     * <li>
     * Networks: networks are expensive to produce, we may be able to have the
     * ValidationProcessor cache a network for later.
     * </li>
     * </ul>
     * </p>
     *
     * @param layers Map of FeatureSource by "dataStoreID:typeName"
     * @param envelope The bounding box that encloses the unvalidated data
     * @param results Used to coallate results information
     *
     * @return <code>true</code> if all the features pass this test.
     */
    public boolean validate(Map layers, Envelope envelope,
        ValidationResults results) throws Exception;
}
