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
 * Store context information about a map display.  This object is based on the
 * OGC Web Map Context Specification.
 *
 * @author Cameron Shorter
 * @version $Id: Context.java,v 1.7 2003/08/07 22:11:22 cholmesny Exp $
 */
public interface Context {
    /**
     * Returns a BoundingBox associated with this context.
     *
     * @return The BoundingBox.
     */
    BoundingBox getBbox();

    /**
     * Returns a list of layers associated with this context.
     *
     * @return The LayerList.
     */
    LayerList getLayerList();

    /**
     * Get the abstract which describes this interface, returns an empty string
     * if this has not been set yet.
     *
     * @return The Abstract.
     */
    String getAbstract();

    /**
     * Set an abstract which describes this context.
     *
     * @param conAbstract the Abstract.
     */
    void setAbstract(final String conAbstract);

    /**
     * Get the contact information associated with this context, returns an
     * empty string if contactInformation has not been set.
     *
     * @return the ContactInformation.
     */
    String getContactInformation();

    /**
     * Set contact inforation associated with this class.
     *
     * @param contactInformation the ContactInformation.
     */
    void setContactInformation(final String contactInformation);

    /**
     * Get an array of keywords associated with this context, returns an empty
     * string if no keywords have been set
     *
     * @return array of keywords
     */
    String[] getKeywords();

    /**
     * Set an array of keywords to associate with this context.
     *
     * @param keywords the Keywords.
     */
    void setKeywords(final String[] keywords);

    /**
     * Get the title, returns an empty string if it has not been set yet.
     *
     * @return the title, or an empty string if it has not been set.
     */
    String getTitle();

    /**
     * Set the title of this context.
     *
     * @param title the title.
     */
    void setTitle(final String title);
}
