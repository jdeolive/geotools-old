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
package org.geotools.validation.attributes;

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.validation.DefaultIntegrityValidation;
import org.geotools.validation.ValidationResults;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Ensure every feature has a unique Feature Id specified by uniqueID.
 * 
 * <p>
 * Please note that featureIDs are not attributes. Attributes may be checked
 * with the UniquityValidation class.
 * </p>
 * 
 * <p>
 * The FeatureTypes it checks against are defined by typeNames[]. If a
 * duplicate ID is detected, an error message returned via a Validation Result
 * used as a visitor in the validation() method.
 * </p>
 * 
 * <p>
 * Example Use:
 * </p>
 * <pre><code>
 * UniqueFIDIntegrityValidation x = new UniqueFIDIntegrityValidation("uniqueFID_road", "Checks if each feature has a unique ID", new String[] {"road", "river"}, "FID");
 * x.validate();
 * </code></pre>
 *
 * @author bowens, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: UniqueFIDValidation.java,v 1.2 2004/02/17 17:19:15 dmzwiers Exp $
 */
public class UniqueFIDValidation extends DefaultIntegrityValidation {
    /** Type Ref or "" for all */
    String typeRef;

    /**
     * UniqueFIDIntegrityValidation constructor.
     * 
     * <p>
     * An empty constructor placed here for Java Beans
     * </p>
     */
    public UniqueFIDValidation() {
    }

    /**
     * UniqueFIDIntegrityValidation constructor.
     * 
     * <p>
     * Initializes allinformation needed to perform the validation.
     * </p>
     *
     * @return DOCUMENT ME!
     */

    /*public UniqueFIDValidation(String name, String description, String[] typeNames, String uniqueID) {
       this.name = name;
       this.description = description;
       this.typeNames = typeNames;
       this.uniqueID = uniqueID;
       }*/

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
        return 10;
    }

    /**
     * Override validate.
     * 
     * <p>
     * Description ... This is supposed to go off and grab the necesary
     * features from the  database using the envelope with the typeNames. But
     * it doesn't yet.  It just uses the ones passed in through parameter
     * layers.
     * </p>
     *
     * @param layers a HashMap of key="TypeName" value="FeatureSource"
     * @param envelope The bounding box of modified features
     * @param results Storage for the error and warning messages
     *
     * @return True if there were no errors. False if there were errors.
     *
     * @throws Exception DOCUMENT ME!
     *
     * @see org.geotools.validation.IntegrityValidation#validate(java.util.Map,
     *      com.vividsolutions.jts.geom.Envelope,
     *      org.geotools.validation.ValidationResults)
     */
    public boolean validate(Map layers, Envelope envelope,
        ValidationResults results) throws Exception {
        HashMap FIDs = new HashMap(); // FIDs used for lookup to see if any match
        boolean result = true;
        Iterator it = layers.values().iterator();

        //TODO: get the needed layers from the database and use them instead
        while (it.hasNext()) // for each layer
         {
            FeatureSource featureSource = (FeatureSource) it.next();
            FeatureReader reader = featureSource.getFeatures().reader();

            try {
                while (reader.hasNext()) // for each feature
                 {
                    Feature feature = reader.next();
                    String fid = feature.getID();

                    if (FIDs.containsKey(fid)) // if a FID like this one already exists
                     {
                        results.error(feature, "FID already exists.");
                        result = false;
                    } else {
                        FIDs.put(fid, fid);
                    }
                }
            } finally {
                reader.close(); // this is an important line	
            }
        }

        return result;
    }

    /**
     * Implementation of getTypeNames.
     *
     * @return Array of typeNames, or empty array for all, null for disabled
     *
     * @see org.geotools.validation.Validation#getTypeNames()
     */
    public String[] getTypeRefs() {
        if (typeRef == null) {
            return null; // disabled
        } else if (typeRef.equals("*")) {
            return new String[0]; // apply to all
        } else {
            return new String[] { typeRef, };
        }
    }

    /**
     * Access typeRef property.
     *
     * @return Returns the typeRef.
     */
    public String getTypeRef() {
        return typeRef;
    }

    /**
     * Set typeRef to indicate type, or  for all.
     *
     * @param typeRef The typeRef to set.
     */
    public void setTypeRef(String typeRef) {
        this.typeRef = typeRef;
    }
}
