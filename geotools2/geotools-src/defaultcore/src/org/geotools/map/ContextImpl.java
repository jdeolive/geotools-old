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

import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.map.LayerList;
import java.awt.Rectangle;
import java.lang.Cloneable;
import java.util.logging.Logger;


/**
 * Store context information about a map display.  This object is based on the
 * OGC Web Map Context Specification.
 *
 * @author $author$
 * @version $Revision: 1.8 $
 */
public class ContextImpl implements Context {
    private static final Logger LOGGER =
        Logger.getLogger("org.geotools.map.ContextImpl");
    private BoundingBox bbox;
    private LayerList layerList;
    private String title;
    private String _abstract;
    private String contactInformation;
    private String[] keywords;
    private ToolList toolList;

    /**
     * Initialise the context.
     *
     * @param bbox The extent associated with this class.
     * @param layerList The list of layers associated with this context.
     * @param toolList The list of tools which can be displayed with this map.
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
    protected ContextImpl(
        BoundingBox bbox,
        LayerList layerList,
        ToolList toolList,
        String title,
        String _abstract,
        String[] keywords,
        String contactInformation
    ) throws IllegalArgumentException {
        if (
            (bbox == null) || (layerList == null) || (toolList == null)
                || (title == null)
        ) {
            throw new IllegalArgumentException();
        } else {
            this.bbox = bbox;
            this.layerList = layerList;
            this.toolList = toolList;
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
     * Returns a BoundingBox associated with this context.
     *
     * @return The BoundingBox.
     */
    public BoundingBox getBbox() {
        return this.bbox;
    }

    /**
     * Returns a list of layers associated with this context.
     *
     * @return The LayerList.
     */
    public LayerList getLayerList() {
        return this.layerList;
    }

    /**
     * Get the abstract which describes this interface, returns an empty string
     * if this has not been set yet.
     *
     * @return The Abstract.
     */
    public String getAbstract() {
        if (this._abstract == null) {
            return "";
        } else {
            return this._abstract;
        }
    }

    /**
     * Set an abstract which describes this context.
     *
     * @param _abstract the Abstract.
     */
    public void setAbstract(final String _abstract) {
        this._abstract = _abstract;
    }

    /**
     * Get the contact information associated with this context, returns an
     * empty string if contactInformation has not been set.
     *
     * @return the ContactInformation.
     */
    public String getContactInformation() {
        if (this.contactInformation == null) {
            return "";
        } else {
            return this.contactInformation;
        }
    }

    /**
     * Set contact inforation associated with this class.
     *
     * @param contactInformation the ContactInformation.
     */
    public void setContactInformation(final String contactInformation) {
        this.contactInformation = contactInformation;
    }

    /* Get an array of keywords associated with this context, returns an
     * empty string if no keywords have been set */
    public String[] getKeywords() {
        if (this.keywords == null) {
            return new String[0];
        } else {
            return this.keywords;
        }
    }

    /**
     * Set an array of keywords to associate with this context.
     *
     * @param keywords the Keywords.
     */
    public void setKeywords(final String[] keywords) {
        this.keywords = keywords;
    }

    /**
     * Get the title, returns an empty string if it has not been set yet.
     *
     * @return the title, or an empty string if it has not been set.
     */
    public String getTitle() {
        if (this.title == null) {
            return "";
        } else {
            return this.title;
        }
    }

    /**
     * Set the title of this context.
     *
     * @param title the title.
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Get the toolList.
     *
     * @return the ToolList.
     */
    public ToolList getToolList() {
        return this.toolList;
    }
}
