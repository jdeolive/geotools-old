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
package org.geotools.feature;

import sun.misc.Service;
import java.util.Iterator;


public class FeatureFactoryFinder {
    private FeatureFactoryFinder() {
    }

    public static FeatureFactory getFeatureFactory(FeatureType type) {
        Iterator ps = Service.providers(FeatureFactorySpi.class);

        while (ps.hasNext()) {
            FeatureFactorySpi fac = (FeatureFactorySpi) ps.next();

            if (fac.canCreate(type)) {
                return fac.getFactory(type);
            }
        }

        return null;
    }

    public static Iterator getAvailableFeatureFactories() {
        return Service.providers(FeatureFactorySpi.class);
    }
}
