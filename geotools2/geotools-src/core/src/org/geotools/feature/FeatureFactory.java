package org.geotools.feature;


public interface FeatureFactory {

    /* ***********************************************************************
     * Handles all attribute interface implementation.                       *
     * ***********************************************************************/
    /**
     * Creates a new feature.
     * @param attributes the array of attribute values
     * @return The created feature
     */
    Feature create(Object[] attributes) throws IllegalFeatureException;

    /**
     * Creates a new feature, with the proper featureID.
     *
     * @param attributes the array of attribute values. 
     * @param featureID the feature ID.
     * @return the created feature.
     */
    Feature create(Object[] attributes, String featureID) throws IllegalFeatureException;

 
    
}
