/*
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
 *     UNITED KINGDOM: James Macgill.  j.macgill@geog.leeds.ac.uk
 */

package org.geotools.styling;
import org.geotools.filter.Expression;
/**
 * A Displacement gives X and Y offset displacements to use for rendering
 * a text label near a point.
 *
 * $Id: Displacement.java,v 1.2 2002/07/12 15:38:35 loxnard Exp $
 * @author  Ian Turton, CCG
 */
public interface Displacement {
    Expression getDisplacementX();
    Expression getDisplacementY();
}
