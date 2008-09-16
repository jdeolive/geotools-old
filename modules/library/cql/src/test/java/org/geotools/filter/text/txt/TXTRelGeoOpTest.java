/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.filter.text.txt;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.Hints;
import org.geotools.filter.text.cql2.CQLRelGeoOpTest;
import org.geotools.filter.text.cql2.CompilerFactory.Language;
import org.opengis.filter.FilterFactory;

/**
 * Relation geo operation 
 *
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.6
 */
public class TXTRelGeoOpTest extends CQLRelGeoOpTest {
    protected static final FilterFactory FILTER_FACTORY = CommonFactoryFinder.getFilterFactory((Hints) null);

    public TXTRelGeoOpTest(){
        super(Language.TXT);
    }

}
