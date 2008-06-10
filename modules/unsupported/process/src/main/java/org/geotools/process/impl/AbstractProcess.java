package org.geotools.process.impl;

import java.util.HashMap;
import java.util.Map;

import org.geotools.process.Process;
import org.geotools.process.ProcessFactory;
import org.geotools.util.NullProgressListener;
import org.opengis.util.ProgressListener;

/**
 * Provide an implementation of the process method to implement your own Process.
 * <p>
 * This is a straight forward abstract process that has all the fields filled in.
 * </p>
 * @author gdavis
 */
public abstract class AbstractProcess implements Process {   
    protected ProcessFactory factory;
    protected AbstractProcess( ProcessFactory factory ){
        this.factory = factory;
    }    
     
}
