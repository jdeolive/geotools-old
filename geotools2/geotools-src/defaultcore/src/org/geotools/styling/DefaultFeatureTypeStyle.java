/*
 * DefaultFeatureTypeStyle.java
 *
 * Created on April 12, 2002, 2:28 PM
 */

package org.geotools.styling;

/**
 * Geotools - OpenSource mapping toolkit
 *            (C) 2002, Center for Computational Geography
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
 *
 * Contacts:
 *     UNITED KINDOM: James Macgill j.macgill@geog.leeds.ac.uk
 *
 *
 * @author jamesm
 */
public class DefaultFeatureTypeStyle implements org.geotools.styling.FeatureTypeStyle {

    /** Creates a new instance of DefaultFeatureTypeStyle */
    public DefaultFeatureTypeStyle() {
    }

    public String getFeatureTypeName() {
        return "feature";//HACK: - generic, catch all type name
    }
    
    public Rule[] getRules() {
        return new Rule[]{new DefaultRule()};
    }
    
    public String[] getSymantecTypeIdentifiers() {
        return new String[]{"generic:geometry"};//HACK: - generic catch all identifier
    }
    
}
