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
/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * ValidationProcessor Runs validation tests against Features and reports the
 * outcome of the tests.
 * 
 * <p>
 * The validation processor contains two main data structures. Each one is a
 * HashMap of ArrayLists that hold Validations. The first one, featureLookup,
 * holds per-feature validation tests (tests that operate on one feature at a
 * time with no knowledge of any other features. The second one,
 * integrityLookup,  holds integrity validations (validations that span
 * multiple features and/or  multiple feature types).
 * </p>
 * 
 * <p>
 * Each HashMap of validations is hashed with a key whose value is a
 * FeatureTypeName. This key provides access to an ArrayList of validations
 * that are to be performed on this FeatureTypeInfo.
 * </p>
 * 
 * <p>
 * Validations are added via the two addValidation() methods.
 * </p>
 * 
 * <p>
 * The validations are run when runFeatureTests() and runIntegrityTests() are
 * called. It is recommended that the user call runFeatureTests() before
 * runIntegrityTests() as it is usually the case that integrity tests are much
 * more time consuming. If a  Feature is incorrect, it can probably be
 * detected early on, and quickly, in the  feature validation tests.
 * </p>
 * 
 * <p>
 * For validations that are performed on every FeatureTypeInfo, a value called
 * ANYTYPENAME has been created and can be stored in the validationLookup
 * tables if a validation specifies that it is run against all FeatureTypes.
 * The value that causes a validation to be run against all FeatureTypes is
 * null. Or Validation.ALL
 * </p>
 * 
 * <p>
 * Results of the validation tests are handled using a Visitor pattern. This
 * visitor is a ValidationResults object that is passed into the
 * runFeatureTests() and runIntegrityTests() methods. Each individual
 * validation will record error messages  in the ValidationResults visitor.
 * </p>
 * 
 * <p>
 * Example Use:
 * <pre><code>
 * ValidationProcessor processor = new ValidationProcessor();<br>
 * processor.addValidation(FeatureValidation1);<br>
 * processor.addValidation(FeatureValidation2);<br>
 * processor.addValidation(IntegrityValidation1);<br>
 * processor.addValidation(FeatureValidation3);<br>
 * <p>
 * processor.runFeatureTests(FeatureTypeInfo, Feature, ValidationResults);<br>
 * processor.runIntegrityTests(layers, Envelope, ValidationResults);<br>
 * </code></pre>
 * </p>
 *
 * @author bowens, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: ValidationProcessor.java,v 1.4 2004/04/20 23:07:05 dmzwiers Exp $
 */
public class ValidationProcessor {
    // These are no longer used for Integrity Validation tests
    // used to hold a place in the lookup tables for ALL feature types
    public final static Object ANYTYPENAME = new Object();
    protected HashMap featureLookup; // a hashMap with featureTypes as keys that map to array lists 

    //    of feature validation tests
    protected HashMap integrityLookup; // a hashMap with featureTypes as keys that map to array lists 

    //	of integrity validation tests
    protected ArrayList modifiedFeatureTypes; // a list of feature types that have been modified
    protected HashMap errors;

    /**
     * ValidationProcessor constructor.
     * 
     * <p>
     * Initializes the data structure to hold the validations.
     * </p>
     */
    public ValidationProcessor() {
        featureLookup = new HashMap();
        integrityLookup = new HashMap();
        errors = new HashMap();
    }

    /**
     * addToLookup
     * 
     * <p>
     * Description: Add the validation test to the map for every featureType
     * that it validates. If the FeatureTypes array is ALL, then the
     * validation is added to the  ANYTYPENAME entry.
     * </p>
     *
     * @param validation
     */
    private void addToFVLookup(FeatureValidation validation) {
        String[] featureTypeList = validation.getTypeRefs();

        if (featureTypeList == Validation.ALL) // if null (ALL)
         {
            ArrayList tests = (ArrayList) featureLookup.get(ANYTYPENAME);

            if (tests == null) { // if an ALL test doesn't exist yet
                tests = new ArrayList(); // create it
            }

            tests.add(validation);
            featureLookup.put(ANYTYPENAME, tests); // add the ALL test to it
        } else // a non ALL FeatureTypeInfo validation
         {
            for (int i = 0; i < featureTypeList.length; i++) {
                ArrayList tests = (ArrayList) featureLookup.get(featureTypeList[i]);

                if (tests == null) { // if this FeatureTypeInfo doesn't have a validation test yet
                    tests = new ArrayList(); // put it in the list
                }

                tests.add(validation);
                featureLookup.put(featureTypeList[i], tests); // add a validation to it
            }
        }
    }

    /**
     * addToIVLookup
     * 
     * <p>
     * Add the validation test to the map for every featureType that it
     * validates. If the FeatureTypes array is ALL, then the validation is
     * added to the  ANYTYPENAME entry.
     * </p>
     *
     * @param validation
     */
    private void addToIVLookup(IntegrityValidation validation) {
        String[] integrityTypeList = validation.getTypeRefs();

        if (integrityTypeList == Validation.ALL) // if null (ALL)
         {
            ArrayList tests = (ArrayList) integrityLookup.get(ANYTYPENAME);

            if (tests == null) { // if an ALL test doesn't exist yet
                tests = new ArrayList(); // create it
            }

            tests.add(validation);
            integrityLookup.put(ANYTYPENAME, tests); // add the ALL test to it
        } else {
            for (int i = 0; i < integrityTypeList.length; i++) {
                ArrayList tests = (ArrayList) integrityLookup.get(integrityTypeList[i]);

                if (tests == null) { // if this FeatureTypeInfo doesn't have a validation test yet
                    tests = new ArrayList(); // put it in the list
                }

                tests.add(validation);
                integrityLookup.put(integrityTypeList[i], tests); // add a validation to it
            }
        }
    }

    /**
     * addValidation
     * 
     * <p>
     * Add a FeatureValidation to the list of Feature tests
     * </p>
     *
     * @param validation
     */
    public void addValidation(FeatureValidation validation) {
        addToFVLookup((FeatureValidation) validation);
    }

    /**
     * addValidation
     * 
     * <p>
     * Add an IntegrityValidation to the list of Integrity tests
     * </p>
     *
     * @param validation
     */
    public void addValidation(IntegrityValidation validation) {
        addToIVLookup((IntegrityValidation) validation);
    }

    /**
     * getDependencies purpose.
     * 
     * <p>
     * Gets all the FeatureTypes that this FeatureTypeInfo uses.
     * </p>
     *
     * @param typeName the FeatureTypeName
     *
     * @return all the FeatureTypes that this FeatureTypeInfo uses.
     */
    public Set getDependencies(String typeName) {
        ArrayList validations = (ArrayList) integrityLookup.get(typeName);
        HashSet s = new HashSet();

        if (validations != null) {
            for (int i = 0; i < validations.size(); i++) // for each validation
             {
                String[] types = ((Validation) validations.get(i)).getTypeRefs();

                for (int j = 0; j < types.length; j++) // for each FeatureTypeInfo

                    s.add(types[i]); // add it to the list
            }
        }

        return s;
    }

    /**
     * runFeatureTests
     * 
     * <p>
     * Performs a lookup on the FeatureTypeInfo name to determine what
     * FeatureTests need to be performed. Once these tests are gathered, they
     * are run on each  feature in the FeatureCollection. The first validation
     * test lookup checks to see if there are any validations that are to be
     * performed on every FeatureTypeInfo. An example of this could be an
     * isValid() test on all geometries in all FeatureTypes. Once those tests
     * have been gathered, a lookup is performed on the TypeName of the
     * FeatureTypeInfo to check for specific FeatureTypeInfo validation tests.
     * A list of validation tests is returned from each lookup, if any exist.
     * When all the validation tests have been gathered, each test is iterated
     * through then run on each Feature, with the ValidationResults coming
     * along for the ride, collecting error information.  Parameter
     * "FeatureCollection collection" should be changed later to take in a
     * FeatureSource so not everything is loaded into memory.
     * </p>
     *
     * @param type The FeatureTypeInfo of the features being tested.
     * @param collection The collection of features, of a particulare
     *        FeatureTypeInfo "type", that are to be validated.
     * @param results Storage for the results of the validation tests.
     * @param dsID data Store id.
     *
     * @throws Exception FeatureValidations throw Exceptions
     */
    public void runFeatureTests(String dsID, FeatureType type, FeatureCollection collection,
        ValidationResults results) throws Exception {
        // check for any tests that are to be performed on ALL features
        ArrayList tests = (ArrayList) featureLookup.get(ANYTYPENAME);

        // check for any FeatureTypeInfo specific tests
        String typeRef = dsID+":"+type.getTypeName();
        ArrayList FT_tests = (ArrayList) featureLookup.get(typeRef);

        // append featureType specific tests to the list of tests		
        if (FT_tests != null) {
            if (tests != null) {
                Iterator it = FT_tests.iterator();

                while (it.hasNext())
                    tests.add((FeatureValidation) it.next());
            } else {
                tests = FT_tests;
            }
        }

        if (tests != null) // if we found some tests to be performed on this FeatureTypeInfo
         {
            // for each test that is to be performed on this featureType
            for (int i = 0; i < tests.size(); i++) {
                FeatureValidation validator = (FeatureValidation) tests.get(i);
                results.setValidation(validator);

                Iterator it = collection.iterator();

                while (it.hasNext()) // iterate through each feature and run the test on it
                 {
                    Feature feature = (Feature) it.next();
                    try{
                    	validator.validate(feature, type, results);
                    }catch(Throwable e){
                    	results.error(feature,e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * runIntegrityTests
     * 
     * <p>
     * Performs a lookup on the FeatureTypeInfo name to determine what
     * IntegrityTests need to be performed. Once these tests are gathered,
     * they are run on the collection  features in the Envelope, defined by a
     * FeatureSource (not a FeatureCollection!). The first validation test
     * lookup checks to see if there are any validations that are to be
     * performed on every FeatureTypeInfo. An example of this could be a
     * uniqueID() test on a unique column value in all FeatureTypes. Once
     * those tests have been gathered, a lookup is performed on the TypeName
     * of the FeatureTypeInfo to check for specific Integrity validation
     * tests. A list of validation tests is returned from each lookup, if any
     * exist. When all the validation tests have been gathered, each test is
     * iterated through then run on each Feature, with the ValidationResults
     * coming along for the ride, collecting error information.
     * </p>
     *
     * @param stores the Map of modified features (Map of key=featureTypeName,
     *        value="featureSource"
     * @param envelope The bounding box that contains all modified Features
     * @param results Storage for the results of the validation tests.
     *
     * @throws Exception Throws an exception if the HashMap contains a value
     *         that is not a FeatureSource
     */
    public void runIntegrityTests(Map stores, Envelope envelope,
        ValidationResults results) throws Exception {
        // convert each HashMap element into FeatureSources
        FeatureSource[] sources = new FeatureSource[stores.size()];
        Object[] array = stores.values().toArray();

        for (int i = 0; i < stores.size(); i++) {
            if (array[i] instanceof FeatureSource) {
                sources[i] = (FeatureSource) array[i];
            } else { //TODO clean me up (just put this in here temporarily)
                throw new Exception("Not a FeatureSource");
            }
        }

        // for each modified FeatureTypeInfo
        for (int i = 0; i < sources.length; i++) {
            // check for any tests that are to be performed on ALL features
            ArrayList tests = (ArrayList) integrityLookup.get(ANYTYPENAME);

            // check for any FeatureTypeInfo specific tests
            ArrayList FT_tests = (ArrayList) integrityLookup.get(sources[i].getSchema()
                                                                           .getTypeName());

            // append featureType specific integrity tests to the list of tests		
            if (FT_tests != null) {
                if (tests != null) {
                    Iterator it = FT_tests.iterator();

                    while (it.hasNext())
                        tests.add((IntegrityValidation) it.next());
                } else {
                    tests = FT_tests;
                }
            }

            if (tests != null) // if we found some tests to be performed on this FeatureTypeInfo
             {
                // for each test that is to be performed on this featureType
                for (int j = 0; j < tests.size(); j++) {
                    IntegrityValidation validator = (IntegrityValidation) tests
                        .get(j);
                    results.setValidation(validator);
					try{
						validator.validate(stores, envelope, results);
					}catch(Throwable e){
						results.error(null,e.getMessage());
					}
                }
            }
        }

        // end for each modified featureType
    }

    /**
     * Access errors property.
     *
     * @return Returns the errors.
     */
    public HashMap getErrors() {
        return errors;
    }
}
