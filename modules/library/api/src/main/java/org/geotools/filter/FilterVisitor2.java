/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.filter;

import org.opengis.filter.ExcludeFilter;
import org.opengis.filter.IncludeFilter;


/**
 * Transition class to handle Filter.INCLUDES and Filter.EXCLUDES
 *
 * @deprecated use {@link org.opengis.filter.FilterVisitor}
 */
public interface FilterVisitor2 extends FilterVisitor {
    void visit(IncludeFilter filter);

    void visit(ExcludeFilter filter);
}
