package org.geotools.feature;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

/**
 * Utility class used by feature model implementation.
 * <p>
 * This class is only for use internally and is not meant to be called by 
 * client code.
 * </p>
 * @author Justin Deoliveira, The Open Planning Project
 *
 */
public class FeatureImplUtils {

    /**
     * Wraps a collection in an umodifiable collection based on the interface
     * the collection implements.
     * <p>
     * A list will result in an umodifiable list, a set in an unmodifiable set, 
     * etc..
     * </p>
     * 
     */
    public static Collection unmodifiable( Collection original ) {
        
        if ( original instanceof Set ) {
            if ( original instanceof SortedSet ) {
                return Collections.unmodifiableSortedSet((SortedSet) original);
            }
            
            return Collections.unmodifiableSet((Set)original);
        }
        else if ( original instanceof List ) {
            return Collections.unmodifiableList((List)original);
        }
        
        return Collections.unmodifiableCollection(original);
    }
}
