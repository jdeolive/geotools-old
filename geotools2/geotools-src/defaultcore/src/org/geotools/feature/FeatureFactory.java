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

package org.geotools.feature;

import java.util.*;
import org.apache.log4j.Category;
import org.geotools.data.*;

/** 
 * <p>A GeoTools representation of a simple feature.
 * A flat feature type enforces the following properties:<ul>
 * <li>Attribute types are restricted to Java primitives and Strings. 
 * <li>Attributes may only have one occurrence.
 * <li>Each feature has a single, non-changing geometry attribute.</ul></p>
 *
 * <p>Flat feature types define feature types that may be thought of as
 * 'layers' in traditional GIS parlance.  They are called flat because
 * they do not allow any nested elements, but they also restrict the
 * attribute objects to be very simple data types.</p>
 *
 * @version $Id: FeatureFactory.java,v 1.3 2002/07/11 16:51:57 loxnard Exp $
 * @author Rob Hranac, VFNY
 */
public class FeatureFactory {

    /** Standard logging instance */
    private static Category _log = Category.getInstance(FeatureFactory.class.getName());
    
    FeatureType schema; 

    /**
     * Constructor.
     */
    public FeatureFactory (FeatureType schema) {
        this.schema = schema;
    }


    /* ***********************************************************************
     * Handles all attribute interface implementation.                       *
     * ***********************************************************************/
    /**
     * Creates a new feature.
     *
     * @return Whether or not this represents a feature type (over a 'flat'
     *         attribute).
     */
    public Feature create(Object[] attributes)
        throws IllegalFeatureException {

        if (schema instanceof FeatureTypeFlat) {
            return new FeatureFlat((FeatureTypeFlat) schema, attributes);
        }
        else {
            return null;
        }
    }


}
