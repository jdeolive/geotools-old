/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation.spatial;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.validation.DefaultFeatureValidation;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Geometry;


/**
 * Tests to see if a geometry is valid by calling Geometry.isValid().
 * <p>
 * The geometry is first tested to see if it is null, and if it is null,  then it
 * is tested to see if it is allowed to be null by calling isNillable().
 * </p>
 * 
 * @author bowens, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: IsValidGeometryValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public class IsValidGeometryValidation extends DefaultFeatureValidation {
    /** The logger for the validation module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.validation");
    
    /**
     * IsValidGeometryFeatureValidation constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public IsValidGeometryValidation() {
    }

    /**
     * Override getPriority.
     * 
     * <p>
     * Sets the priority level of this validation.
     * </p>
     *
     * @return A made up priority for this validation.
     *
     * @see org.geotools.validation.Validation#getPriority()
     */
    public int getPriority() {
        return PRIORITY_TRIVIAL;
    }

    /**
     * Override getTypeNames.
     * 
     * <p>
     * Returns the TypeNames of the FeatureTypes used in this particular
     * validation.
     * </p>
     *
     * @return An array of TypeNames
     *
     * @see org.geotools.validation.Validation#getTypeNames()
     */
    public String[] getTypeNames() {
        return new String[]{ getTypeRef(), };
    }

    /**
     * Tests to see if a geometry is valid by calling Geometry.isValid().
     * <p>
     * The geometry is first tested to see if it is null, and if it is null,  then
     * it is tested to see if it is allowed to be null by calling
     * isNillable().
     * </p>
     *
     * @param feature The Feature to be validated
     * @param type The FeatureTypeInfo of the feature
     * @param results The storage for error messages.
     *
     * @return True if the feature is a valid geometry.
     *
     * @see org.geotools.validation.FeatureValidation#validate(org.geotools.feature.Feature,
     *      org.geotools.feature.FeatureTypeInfo,
     *      org.geotools.validation.ValidationResults)
     */
    public boolean validate(Feature feature, FeatureType type,
        ValidationResults results) {
        
        Geometry geom = feature.getDefaultGeometry();

        if (geom == null) {
            if (type.getDefaultGeometry().isNillable()) {
                LOGGER.log(Level.FINEST,
                    getName() + "(" + feature.getID() + ") passed");

                return true;
            } else {
                String message = "Geometry was null but is not nillable.";
                results.error(feature, message);
                LOGGER.log(Level.FINEST,
                    getName() + "(" + feature.getID() + "):" + message);

                return false;
            }
        }

        if (!geom.isValid()) {
            String message = "Not a valid geometry. isValid() failed";
            LOGGER.log(Level.FINEST,
                getName() + "(" + feature.getID() + "):" + message);
            results.error(feature, message);

            return false;
        }

        LOGGER.log(Level.FINEST, getName() + "(" + feature.getID() + ") passed");

        return true;
    }

}
