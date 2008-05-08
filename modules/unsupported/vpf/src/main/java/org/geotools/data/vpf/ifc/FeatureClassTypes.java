/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2004-2006, Geotools Project Managment Committee (PMC)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.data.vpf.ifc;

/*
 * FeatureTypes.java
 *
 * Created on 21. april 2004, 15:35
 *
 * @author  <a href="mailto:knuterik@onemap.org">Knut-Erik Johnsen</a>, Project OneMap
 * @source $URL$
 */
public interface FeatureClassTypes {

    public static final char FEATURE_POINT = 'P';

    public static final char FEATURE_LINE = 'L';

    public static final char FEATURE_TEXT = 'T';

    public static final char FEATURE_AREA = 'A';

    public static final String FIELD_CLASS = "fclass";

    public static final String FIELD_TYPE = "type";

    public static final String FIELD_DESCRIPTION = "descr";
}
