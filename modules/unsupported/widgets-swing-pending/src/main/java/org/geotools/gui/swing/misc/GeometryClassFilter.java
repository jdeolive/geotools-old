/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, GeoTools Project Managment Committee (PMC)
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
 */
package org.geotools.gui.swing.misc;

import java.util.ArrayList;
import java.util.List;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterVisitor;

/**
 *
 * @author Johann Sorel
 */
public class GeometryClassFilter implements Filter {

        private List<Class> valids = new ArrayList<Class>();

        public GeometryClassFilter(Class... classes) {
            for (Class cl : classes) {
                valids.add(cl);
            }
        }

        public boolean evaluate(Object obj) {
            if (obj instanceof SimpleFeature) {
                SimpleFeature sf = (SimpleFeature) obj;

                if (valids.contains(sf.getDefaultGeometry().getClass())) {
                    return true;
                }
            }
            return false;
        }

        public Object accept(FilterVisitor arg0, Object arg1) {
            return null;
        }
    }
