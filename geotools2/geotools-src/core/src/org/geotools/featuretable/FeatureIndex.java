package org.geotools.featuretable;


import org.geotools.datasource.*;
/** An Index is built up around a FeatureTable, using one of the columns in FeatureTable
 * as a comparable reference. An object in a column can be any object, but must either be a
 * java base-type Object (Integer, String, Character, etc.) or implement Comparable.
 * An Index built on such a column will sort its array of object references using
 * FeatureComparator. Implement this to perform more complex Index building.
 */
public interface FeatureIndex extends TableChangedListener {

    /** Gets an array of references to the rows currently held by this Index.
     */
    public Feature[] getFeatures();
}

