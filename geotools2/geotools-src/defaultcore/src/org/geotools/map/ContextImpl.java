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
package org.geotools.map;


/**
 * Legacy implementation of {@link Context}
 *
 * @author Cameron Shorter
 * @version $Id: ContextImpl.java,v 1.13 2003/08/20 20:51:16 cholmesny Exp $
 *
 * @deprecated Use {@link DefaultContext} instead.
 */
public class ContextImpl extends DefaultContext {
    /**
     * Initialise the context.
     *
     * @param bbox The extent associated with this class.
     * @param layerList The list of layers associated with this context.
     * @param title The name of this context.  Must be set.
     * @param _abstract A description of this context.  Optional, set to null
     *        if none exists.
     * @param keywords An array of keywords to be used when searching for this
     *        context.  Optional, set to null if none exists.
     * @param contactInformation Contact details for the person who created
     *        this context.  Optional, set to null if none exists.
     *
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    protected ContextImpl(final BoundingBox bbox,
                          final LayerList   layerList,
                          final String      title,
                          final String      c_abstract,
                          final String[]    keywords,
                          final String      contactInformation)
            throws IllegalArgumentException {
        super(bbox, layerList, title, c_abstract, keywords, contactInformation);
    }
}
