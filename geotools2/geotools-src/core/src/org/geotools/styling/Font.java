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
 * A system-independent object for holding SLD font infomation.
 * This holds information on the text font to use in text processing.
 * Font-family, font-style, font-weight and font-size.
 * $Id: Font.java,v 1.2 2002/07/12 15:39:30 loxnard Exp $
 * @author Ian Turton, CCG
 */
public interface Font {
    
    Expression getFontFamily();
    Expression getFontStyle();
    Expression getFontWeight();
    Expression getFontSize();
    
}
