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
package org.geotools.filter.text.cql2;

import org.geotools.filter.text.generated.parsers.Token;

/**
 * Interface must be implemented by the specific compiler.
 * This will be used to send the token to the {@link CQLFilterBuilder}.
 * @author Mauricio Pazos (Axios Engineering)
 * @since 2.5
 */
public interface IToken {

    public String toString();

    public boolean hasNext();

    public IToken next();

    public int beginColumn();

    public int endColumn();

    public Token getAdapted();

}
