package org.geotools.process;

import java.util.Map;
import java.util.concurrent.Future;

/**
 * Used to report on the progress of a running {@link Process}.
 * <p>
 * The contents of the Map returned by get() are described by {@link ProcessFactory.getResultInfo()}
 * description.
 * 
 * @author Jody
 */
public interface Progress extends Future<Map<String,Object>> {
    /**
     * Value of getProgress used to represent an undefined amount of work. 
     */
    public static float WORKING = -1.0f; 
    
    /**
     * Amount of work completed.
     * 
     * @return Percent completed, or WORKING if amount of work is unknown. 
     */
    public float getProgress();
    
    // consider these if anyone is interested on the user interface side
    // addChangeListener
    // removeChangeListener    
}
