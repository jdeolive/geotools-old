/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
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
 *    Created on October 15, 2003, 1:57 PM
 */
package org.geotools.text.filter;

import org.geotools.filter.text.cql2.CQLException;
import org.geotools.filter.text.cql2.IToken;


/**
 * @author  Ian Schneider
 * @source $URL$
 * @deprecated use the {@link CQLException} class instead, this one is going to be set to package visibility
 */
public class FilterBuilderException extends CQLException {
    /**
     * generated serial version uid
     */
    private static final long serialVersionUID = -8027243686579409436L;

    public FilterBuilderException(String message) {
        super(message);
    }

    public FilterBuilderException(String message, IToken token) {
        super(message, token,null);
    }

    public FilterBuilderException(String message, IToken token, Throwable cause) {
        super(message, token, cause, null);
    }
}
