package org.geotools.featuretable;

/**
 * Thrown when there is an error rebuilding an index.
 * @author ray
 */
public class IndexException extends Exception{
    
    /**
     * Constructs a new IndexException.
     * @param msg Message explaining reason for exception
     */
    public IndexException(String msg){
        super(msg);
    }
}

