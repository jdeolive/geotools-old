/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.map;

/**
 * Store context information about a map display.  This object is based on the
 * OGC Web Map Context Specification.
 *
 * @version $Id: ContextImpl.java,v 1.1 2002/12/24 19:15:50 camerons Exp $
 * @author Cameron Shorter
 */

import java.lang.Cloneable;
import java.util.logging.Logger;
import org.geotools.map.BoundingBox;
import org.geotools.map.Context;
import org.geotools.map.LayerList;

public class ContextImpl implements Context {
    private BoundingBox bbox;
    private LayerList layerList;
    private String title;
    private String _abstract;
    private String contactInformation;
    private String[] keywords;
    private static final Logger LOGGER = Logger.getLogger("org.geotools.map.ContextImpl");

    /**
     * Initialise the context.
     * @param bbox The extent associated with this class.
     * @throws IllegalArgumentException if an argument is <code>null</code>.
     */
    public ContextImpl(
        BoundingBox bbox,
        LayerList layerList,
        String title,
        String _abstract,
        String[] keywords,
        String contactInformation) throws IllegalArgumentException
    {
        if ((bbox==null)||(layerList==null)||(title==null)){
            throw new IllegalArgumentException();
        }else{
            this.bbox=bbox;
            this.layerList=layerList;
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

    public BoundingBox getBbox() {
        return this.bbox;
    }

    public LayerList getLayerList() {
        return this.layerList;
    }

    public String getAbstract() {
        return this._abstract;
    }

    public void setAbstract(final String _abstract) {
        this._abstract = _abstract;
    }

    public String getContactInformation() {
        return this.contactInformation;
    }

    public void setContactInformation(final String contactInformation) {
        this.contactInformation = contactInformation;
    }

    public String[] getKeywords() {
        return this.keywords;
    }

    public void setKeywords(final String[] keywords) {
        this.keywords = keywords;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }
}
