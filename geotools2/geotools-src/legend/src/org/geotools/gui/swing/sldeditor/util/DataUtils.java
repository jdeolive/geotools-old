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
 * DataUtils.java
 *
 * Created on 1 dicembre 2003, 8.04
 */
package org.geotools.gui.swing.sldeditor.util;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DataUtils {
    /**
     * Creates a new instance of DataUtils
     */
    public DataUtils() {
    }

    public static Feature getSample(FeatureSource fs) {
        Feature sample = null;
        try {
            FeatureReader fr = fs.getFeatures().reader();
            if (fr.hasNext()) {
                sample = fr.next();
            }
        } catch (Exception e) {
            // 
        }

        return sample;
    }
}
