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
 * @author aaime
 */
public class ColorMapImpl implements ColorMap {
    private List list = new ArrayList();
    private int type = ColorMap.TYPE_RAMP;

    public void addColorMapEntry(ColorMapEntry entry) {
        list.add(entry);
    }

    public ColorMapEntry[] getColorMapEntries() {
        return (ColorMapEntry[]) list.toArray(new ColorMapEntry[0]);
    }

    public ColorMapEntry getColorMapEntry(int index) {
        return (ColorMapEntry) list.get(index);
    }

    /**
     * @see org.geotools.styling.ColorMap#getType()
     */
    public int getType() {
        return type;
    }

    /**
     * @see org.geotools.styling.ColorMap#setType(int)
     */
    public void setType(int type) {
        if(type < TYPE_RAMP || type > TYPE_VALUES) {
            throw new IllegalArgumentException();
        }
        
        this.type = type;        
    }
}