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
 * @version $Id: Context.java,v 1.2 2003/03/24 21:03:34 camerons Exp $
 * @author Cameron Shorter
 */

import java.lang.Cloneable;
import org.geotools.map.BoundingBox;
import org.geotools.map.LayerList;

public interface Context {

    public BoundingBox getBbox();

    public LayerList getLayerList();
    
    public SelectedTool getSelectedTool();

    public String getAbstract();

    public void setAbstract(final String _abstract);

    public String getContactInformation();

    public void setContactInformation(final String contactInformation);

    public String[] getKeywords();

    public void setKeywords(final String[] keywords);

    public String getTitle();

    public void setTitle(final String title);
}
