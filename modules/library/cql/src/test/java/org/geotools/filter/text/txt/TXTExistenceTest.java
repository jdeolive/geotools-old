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

import org.geotools.filter.text.cql2.CQLExistenceTest;
import org.geotools.filter.text.cql2.CompilerFactory.Language;

/**
 * Test for TXT Existence Predicate
 * 
 * <p>
 * 
 * </p>
 * 
 * @author Mauricio Pazos (Axios Engineering)
 * @since 
 */
public class TXTExistenceTest extends CQLExistenceTest {
    
    public TXTExistenceTest(){
        super(Language.TXT);
    }
}
