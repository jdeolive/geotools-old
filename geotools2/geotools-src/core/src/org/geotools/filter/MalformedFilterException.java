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


/**
 * Defines a comparison filter (can be a math comparison or generic equals).
 *
 * This filter implements a comparison - of some sort - between two expressions.
 * The comparison may be a math comparison or a generic equals comparison.  If
 * it is a math comparison, only math expressions are allowed; if it is an
 * equals comparison, any expression types are allowed.
 *
 * Note that this comparison does not attempt to restrict its expressions to be
 * meaningful.  This means that it considers itself a valid filter as long as
 * the expression comparison returns a valid result.  It does no checking to
 * see whether or not the expression comparison is meaningful with regard
 * to checking feature attributes.  In other words, this is a valid filter:
 * <b>5 < 2<b>, even though it will always return the same result and could
 * be simplified away.  It is up to the filter creator, therefore, to attempt
 * to simplify/make meaningful filter logic.
 * 
 * @version $Id: MalformedFilterException.java,v 1.2 2002/06/04 15:15:02 loxnard Exp $
 * @author Rob Hranac, Vision for New York
 */
public class MalformedFilterException extends Exception {

    /**
     * Constructor with filter type.
     *
     */
    public MalformedFilterException () {
        super();
    }

            
    /**
     * Constructor with filter type.
     *
     */
    public MalformedFilterException (String message) {
        super(message);
    }

            
}
