/*
 * StyleFactoryCreationException.java
 *
 * Created on 22 October 2002, 15:29
 */

package org.geotools.styling;

/**
 * An exception that can be thrown by the StyleFactory if it fails to create the 
 * implementation of the StyleFactory.
 *
 * $Id: StyleFactoryCreationException.java,v 1.1 2002/10/22 17:02:03 ianturton Exp $
 * @author  iant
 */
public class StyleFactoryCreationException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>StyleFactoryCreationException</code> without detail message.
     */
    public StyleFactoryCreationException() {
    }
    
    
    /**
     * Constructs an instance of <code>StyleFactoryCreationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public StyleFactoryCreationException(String msg) {
        super(msg);
    }
    
    public StyleFactoryCreationException(Exception e){
        super(e);
    }
    
    public StyleFactoryCreationException(String msg, Exception e){
        super(msg,e);
    }
    
}
