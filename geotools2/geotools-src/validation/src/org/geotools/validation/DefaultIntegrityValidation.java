/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation;

import java.util.Map;
import java.util.logging.Logger;

import com.vividsolutions.jts.geom.Envelope;


/**
 * Tests to see if a Feature ...
 * 
 * <p>
 * This class is ment to be copied as a starting point for implementing
 * IntegrityValidation. Chances are you are not working against a single
 * typeName when performing an integrity test.
 * </p>
 *
 * @author Jody Garnett, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: DefaultIntegrityValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public class DefaultIntegrityValidation implements IntegrityValidation {
    
    /** The logger for the validation module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.validation");

    /**
     * User's Name of this integrity test.
     */
    private String name; 

    /**
     * User's description of this integrity test.
     */
    private String description;

    /**
     * No argument constructor, required by the Java Bean Specification.
     */
    public DefaultIntegrityValidation() {
    }

    /**
     * Override setName.
     * 
     * <p>
     * Sets the name of this validation.
     * </p>
     *
     * @param name The name of this validation.
     *
     * @see org.geotools.validation.Validation#setName(java.lang.String)
     */
    public final void setName(String name) {
        this.name = name;
    }

    /**
     * Override getName.
     * 
     * <p>
     * Returns the name of this particular validation.
     * </p>
     *
     * @return The name of this particular validation.
     *
     * @see org.geotools.validation.Validation#getName()
     */
    public final String getName() {
        return name;
    }

    /**
     * Override setDescription.
     * 
     * <p>
     * Sets the description of this validation.
     * </p>
     *
     * @param description The description of the validation.
     *
     * @see org.geotools.validation.Validation#setDescription(java.lang.String)
     */
    public final void setDescription(String description) {
        this.description = description;
    }

    /**
     * Override getDescription.
     * 
     * <p>
     * Returns the description of this validation as a string.
     * </p>
     *
     * @return The description of this validation.
     *
     * @see org.geotools.validation.Validation#getDescription()
     */
    public final String getDescription() {
        return description;
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
     * Implementation of getTypeNames.
     *
     * @return Array of typeNames, or empty array for all, null for disabled
     *
     * @see org.geotools.validation.Validation#getTypeNames()
     */
    public String[] getTypeRefs() {
        return null; // disabled by default
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
        results.warning( null, "Validation not yet implemented" );
        return false;
    }
}
