/*
 * ColorMapImpl.java
 *
 * Created on 13 November 2002, 13:51
 */

package org.geotools.styling;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author  iant
 */
public class ColorMapImpl implements ColorMap {
    List list = new ArrayList();
    
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
