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
 * LegendTree.java
 *
 * Created on 14 June 2003, 23:04
 */
package org.geotools.gui.swing.legend;

import java.util.logging.Logger;

import javax.swing.JTree;

import org.geotools.map.Context;


/**
 * This Class actuall is the real base for the whole legend package which is  a
 * JTree Class with a new attribute Context. The Legend will put it in a
 * JScrollPane and then a JPanel, but all action or event are from this class.
 *
 * @author jianhuij
 */
public class LegendTree extends JTree {
    /** The logger for the Legend module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.gui.swing.legend");
    private Context context;

    /**
     * Creates a new instance of LegendTree
     */
    public LegendTree() {
        super();
    }

    public LegendTree(Context context) {
        super();
        setContext(context);
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Context getContext() {
        return this.context;
    }
}
