/*
 * StyleFactoryCreationException.java
 *
 * Created on 22 October 2002, 15:29
 */

package org.geotools.filter;

/**
 * An exception that can be thrown by the StyleFactory if it fails to create the 
 * implementation of the StyleFactory.
 *
 * $Id: FilterFactoryCreationException.java,v 1.1 2002/10/24 16:51:53 ianturton Exp $
 * @author  iant
 */
public class FilterFactoryCreationException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>StyleFactoryCreationException</code> without detail message.
     */
    public FilterFactoryCreationException() {
    }
    
    
    /**
     * Constructs an instance of <code>StyleFactoryCreationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public FilterFactoryCreationException(String msg) {
        super(msg);
    }
    
    public FilterFactoryCreationException(Exception e){
        super(e);
    }
    
    public FilterFactoryCreationException(String msg, Exception e){
        super(msg,e);
    }
    
}
