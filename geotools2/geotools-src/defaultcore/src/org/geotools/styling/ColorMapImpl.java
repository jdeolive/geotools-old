/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 */
package org.geotools.styling;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple implementation of the color map interface.
 *
 * @author iant
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
        if ((type < TYPE_RAMP) || (type > TYPE_VALUES)) {
            throw new IllegalArgumentException();
        }

        this.type = type;
    }
}
