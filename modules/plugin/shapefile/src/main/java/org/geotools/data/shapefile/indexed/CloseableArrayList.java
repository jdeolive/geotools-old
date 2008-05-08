/**
 * 
 */
package org.geotools.data.shapefile.indexed;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;

import org.geotools.index.CloseableCollection;
import org.geotools.index.Data;

/**
 * Currently just wraps ArrayList and delegates to that class
 * @author jesse
 */
public class CloseableArrayList extends AbstractList<Data> implements
        CloseableCollection<Data> {

    private final ArrayList<Data> container ;

    public CloseableArrayList(int length) {
        container = new ArrayList<Data>(length);
    }

    public CloseableArrayList() {
        container = new ArrayList<Data>();
    }

    @Override
    public Data get(int index) {
        return container.get(index);
    }

    @Override
    public int size() {
        return container.size();
    }

    
    public void close() throws IOException {
        // do nothing
        
    }

    public boolean add(Data o) {
        return container.add(o);
    }

    public void closeIterator( Iterator<Data> iter ) throws IOException {
        // do nothing
    }
    
}
