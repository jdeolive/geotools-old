/*
 * ColorMapImpl.java
 *
 * Created on 13 November 2002, 13:51
 */

package org.geotools.styling;

import java.util.ArrayList;


/**
 *
 * @author  iant
 */
public class ColorMapImpl implements ColorMap {
    ArrayList list = new ArrayList();
    /** Creates a new instance of ColorMapImpl */
    public ColorMapImpl() {
    }
    
    public void addColorMapEntry(ColorMapEntry entry) {
        list.add(entry);
    }
    
    public ColorMapEntry[] getColorMapEntries() {
        return (ColorMapEntry[])list.toArray(new ColorMapEntry[0]);
    }
    
    public ColorMapEntry getColorMapEntry(int i) {
        return (ColorMapEntry)list.get(i);
    }
    
}
