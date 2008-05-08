package org.geotools.geometry.jts.spatialschema.geometry;

import org.geotools.geometry.jts.JTSGeometry;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Helper class that notifies the containing geometry when the list has changed
 * so that it can invalidate any cached JTS objects it had.
 */
public class NotifyingArrayList<T> extends ArrayList<T> {
    private JTSGeometry parent;

    public NotifyingArrayList() {
        this( null );
    }
    public NotifyingArrayList(JTSGeometry parent) {
        this.parent = parent;
    }
    public void setJTSParent( JTSGeometry parent ){
        this.parent = parent;
    }
    public JTSGeometry getJTSParent(){
        return parent;
    }
    public void invalidateCachedJTSPeer(){
        if (parent != null) parent.invalidateCachedJTSPeer();
    }
    public void add(int index, T element) {
        super.add(index, element);
        if (parent != null) parent.invalidateCachedJTSPeer();
    }
    public boolean add(T o) {
        boolean result = super.add(o);
        if (parent != null) parent.invalidateCachedJTSPeer();
        return result;
    }
    public boolean addAll(Collection<? extends T> c) {
        boolean result = super.addAll(c);
        if (parent != null) parent.invalidateCachedJTSPeer();
        return result;
    }
    
    public boolean addAll(int index, Collection<? extends T> c) {
        boolean result = super.addAll(index, c);
        if (parent != null) parent.invalidateCachedJTSPeer();
        return result;
    }
    public void clear() {
        super.clear();
        if (parent != null) parent.invalidateCachedJTSPeer();
    }
    public T remove(int index) {
        T result = super.remove(index);
        if (parent != null) parent.invalidateCachedJTSPeer();
        return result;
    }
    public T set(int index, T element) {
        T result = super.set(index, element);
        if (parent != null) parent.invalidateCachedJTSPeer();
        return result;
    }
}