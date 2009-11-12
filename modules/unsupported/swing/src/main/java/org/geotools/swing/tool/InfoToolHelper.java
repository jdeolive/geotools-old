/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.swing.tool;

import java.util.Map;
import org.geotools.geometry.DirectPosition2D;

/**
 * Abstract base class for helper classes used by {@code InfoTool} to query
 * {@code MapLayers}. The primary reason for having this class is to avoid
 * loading grid coverage classes unless they are really needed, and thus
 * avoid the need for users to have JAI in the classpath when working with
 * vector data.
 *
 * @see InfoTool
 *
 * @author Michael Bedward
 * @since 2.6
 * @source $Id$
 * @version $URL$
 */
public abstract class InfoToolHelper<T> {

    public static enum Type {
        GRID_HELPER, VECTOR_HELPER;
    }

    private final Type type;

    protected InfoToolHelper(Type type) {
        this.type = type;
    }

    public abstract T getInfo(DirectPosition2D pos, Object ...params) throws Exception;

    public Type getType() {
        return type;
    }

}

