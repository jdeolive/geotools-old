/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation.attributes;

import java.util.Map;
import java.util.logging.Logger;

import org.geotools.validation.DefaultIntegrityValidation;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Tests to that an attribute's value is unique across the entire FeatureType.
 * <p>
 * For a starting point you may want to look at UniqueFIDIntegrityValidation
 * </p>
 *
 * @author Jody Garnett, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: UniquityValidation.java,v 1.1 2004/02/13 03:08:00 jive Exp $
 */
public class UniquityValidation extends DefaultIntegrityValidation {
    
    /** The logger for the validation module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.validation");

    /** Attribute name to check for uniquity */
    private String attributeName;
    /**
     * No argument constructor, required by the Java Bean Specification.
     */
    public UniquityValidation() {
    }

    /**
     * The priority level used to schedule this Validation.
     *
     * @return PRORITY_SIMPLE
     *
     * @see org.geotools.validation.Validation#getPriority()
     */
    public int getPriority() {
        return PRIORITY_SIMPLE;
    }

    /**
     * Check FeatureType for ...
     * <p>
     * Detailed description...
     * </p>
     * 
     * @param layers Map of FeatureSource by "dataStoreID:typeName"
     * @param envelope The bounding box that encloses the unvalidated data
     * @param results Used to coallate results information
     *
     * @return <code>true</code> if all the features pass this test.
     */
    public boolean validate(Map layers, Envelope envelope, ValidationResults results) throws Exception {
        return false;
    }
}
