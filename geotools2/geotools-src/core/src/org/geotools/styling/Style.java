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

package org.geotools.styling;

/**
 * @version $Id: Style.java,v 1.7 2002/10/22 17:02:04 ianturton Exp $
 * @author James Macgill
 */
public interface Style {
    
    String getName();
    void setName(String name);
    String getTitle();
    void setTitle(String title);
    String getAbstract();
    void setAbstract(String abstractStr);
    boolean isDefault();
    void setIsDefault(boolean isDefault);
    FeatureTypeStyle[] getFeatureTypeStyles();
    void setFeatureTypeStyles(FeatureTypeStyle[] types);
    void addFeatureTypeStyle(FeatureTypeStyle type);

}

