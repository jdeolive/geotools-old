/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2005, David Zwiers
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
package org.geotools.wfs.v_1_1_0.data;

import static org.geotools.wfs.v_1_1_0.data.DataTestSupport.GEOS_STATES;

import java.util.Collections;

import com.vividsolutions.jts.geom.MultiPolygon;

public class GeoServerOnlineTest extends AbstractWfsDataStoreOnlineTest {

    public static final String SERVER_URL = "http://localhost:8080/geoserver/wfs?service=WFS&request=GetCapabilities&version=1.1.0"; //$NON-NLS-1$

    public GeoServerOnlineTest() {
        super(SERVER_URL, GEOS_STATES, "the_geom", MultiPolygon.class, 49, ff.id(Collections
                .singleton(ff.featureId("states.1"))));
    }

}
