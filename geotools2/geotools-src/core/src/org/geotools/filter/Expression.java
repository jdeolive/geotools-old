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

package org.geotools.filter;

import org.geotools.feature.Feature;

/**
 * Defines a complex filter (could also be called logical filter).
 *
 * This filter holds one or more filters together and relates
 * them logically in an internally defined manner.
 *
 * @version $Id: Expression.java,v 1.4 2002/07/10 14:42:07 robhranac Exp $
 * @author Rob Hranac, Vision for New York
 */
public interface Expression {


    /**
     * Gets the type of this expression.
     *
     * @return Expression type.
     */
    public short getType();


    /**
     * Returns a value for this expression.
     *
     * @param feature Specified feature to use when returning value.
     * @return Value of the feature object.
     */
    public Object getValue(Feature feature);
    
}
