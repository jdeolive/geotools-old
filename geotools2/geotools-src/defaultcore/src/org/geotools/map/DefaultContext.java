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
 * Default implementation of {@link Context}
 *
 * @author Cameron Shorter
 * @version $Revision: 1.1 $
 */
public class DefaultContext implements Context {
    /**
     * The bounding box, or <code>null</code> if none..
     *
     * @see #getBbox
     */
    private BoundingBox bbox;

    /**
     * The layer list, or <code>null</code> if none..
     *
     * @see #getLayerList
     */
    private LayerList layerList;

    /**
     * The title, or <code>null</code> if none..
     *
     * @see #getTitle
     * @see #setTitle
     */
    private String title;

    /**
     * The abstract that describes this context, or <code>null</code> if none..
     *
     * @see #getAbstract
     * @see #setAbstract
     */
    private String _abstract;

    /**
     * The contact information, or <code>null</code> if none.
     *
     * @see #getContactInformation
     * @see #setContactInformation
     */
    private String contactInformation;

    /**
     * A list of keywords, or <code>null</code> if none.
     */
    private String[] keywords;

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
    protected DefaultContext(final BoundingBox bbox,
                             final LayerList   layerList,
                             final String      title,
                             final String      _abstract,
                             final String[]    keywords,
                             final String      contactInformation)
            throws IllegalArgumentException
    {
        if ((bbox == null) || (layerList == null) || (title == null)) {
            throw new IllegalArgumentException();
        } else {
            this.bbox = bbox;
            this.layerList = layerList;
            this.setTitle(title);
            this.setAbstract(_abstract);
            this.setKeywords(keywords);
            this.setContactInformation(contactInformation);
        }
    }

    //    /*
    //     * Create a copy of this class
    //     */
    //    public Object clone() {
    //        return new ContextImpl(
    //            bbox,
    //            layerList,
    //            title, 
    //            _abstract,
    //            keywords, 
    //            contactInformation);
    //    }

    /**
     * {@inheritDoc}
     */
    public BoundingBox getBbox() {
        return this.bbox;
    }

    /**
     * {@inheritDoc}
     */
    public LayerList getLayerList() {
        return this.layerList;
    }

    /**
     * {@inheritDoc}
     */
    public String getAbstract() {
        if (this._abstract == null) {
            return "";
        } else {
            return this._abstract;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setAbstract(final String _abstract) {
        this._abstract = _abstract;
    }

    /**
     * {@inheritDoc}
     */
    public String getContactInformation() {
        if (this.contactInformation == null) {
            return "";
        } else {
            return this.contactInformation;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setContactInformation(final String contactInformation) {
        this.contactInformation = contactInformation;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getKeywords() {
        if (this.keywords == null) {
            return new String[0];
        } else {
            return (String[]) this.keywords.clone();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setKeywords(String[] keywords) {
        if (keywords != null) {
            keywords = (String[]) keywords.clone();
        }
        this.keywords = keywords;
    }

    /**
     * {@inheritDoc}
     */
    public String getTitle() {
        if (this.title == null) {
            return "";
        } else {
            return this.title;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setTitle(final String title) {
        this.title = title;
    }
}
