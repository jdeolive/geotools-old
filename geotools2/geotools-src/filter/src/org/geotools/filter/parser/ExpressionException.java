/*
 * ExpressionException.java
 *
 * Created on October 15, 2003, 1:57 PM
 */

package org.geotools.filter.parser;


/**
 *
 * @author  Ian Schneider
 */
public class ExpressionException extends ParseException {
    
    Throwable cause;
    
    public ExpressionException(String message,Token token) {
        this(message,token,null);
    }
    
    public ExpressionException(String message,Token token,Throwable cause) {
        super(message);
        this.currentToken = token;
        this.cause = cause;
    }
    
    public Throwable getCause() {
        return cause;
    }
    
    public String getMessage() {
        if (currentToken == null) return super.getMessage();
        
        return super.getMessage() + ", Current Token : " + currentToken.image;
    }
}
