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
/*
 * LegendNoteInfo.java
 *
 * Created on 11 July 2003, 22:26
 */
package org.geotools.gui.swing.legend;

import java.awt.Color;

import javax.swing.Icon;


/**
 * in a legend, three kind node involves: Root Node, Layer Node, Rule Node this
 * is a interface for simplify coding some common features in those three
 * class.
 *
 * @author jianhuij
 */
public abstract class LegendNodeInfo extends Object
    implements java.io.Serializable {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract String toString();

    public abstract Icon getIcon(boolean selected);

    public abstract Color getBackground(boolean selected);

    public abstract boolean isSelected();
}
