/*
 * MockFeatureCollection.java
 *
 * Created on August 12, 2003, 7:29 PM
 */

package org.geotools.feature;

/**
 *
 * @author  jamesm
 */
public class MockFeatureCollection implements org.geotools.feature.FeatureCollection {
    
    /** Creates a new instance of MockFeatureCollection */
    public MockFeatureCollection() {
    }
    
    public void addListener(CollectionListener listener) {
    }
    
    public FeatureIterator features() {
        return null;
    }
    
    public com.vividsolutions.jts.geom.Envelope getBounds() {
        return null;
    }
    
    public void removeListener(CollectionListener listener) {
       
    }
    
    public boolean add(Object o) {
        return false;
    }
    
    public boolean addAll(java.util.Collection c) {
        return false;
    }
    
    public void clear() {
    }
    
    public boolean contains(Object o) {
        return false;
    }
    
    public boolean containsAll(java.util.Collection c) {
        return false;
    }
    
    public boolean equals(Object o) {
        return false;
    }
    
    public int hashCode() {
        return 0;
    }
    
    public boolean isEmpty() {
        return false;
    }
    
    public java.util.Iterator iterator() {
        return null;
    }
    
    public boolean remove(Object o) {
        return false;
    }
    
    public boolean removeAll(java.util.Collection c) {
        return false;
    }
    
    public boolean retainAll(java.util.Collection c) {
        return false;
    }
    
    public int size() {
        return 0;
    }
    
    public Object[] toArray() {
        return null;
    }
    
    public Object[] toArray(Object[] a) {
        return null;
    }
    
}
