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

package org.geotools.gml;

/**
 * An exception used to represent any GML related errors.
 *
 * @version $Id: GMLException.java,v 1.4 2003/08/03 00:32:09 dledmonds Exp $
 * @author Ian Turton, CCG
 */
public class GMLException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>GMLException</code>
     * without detail message.
     */
    public GMLException() {
    }
    
    
    /**
     * Constructs an instance of <code>GMLException</code> 
     * with the specified detail message.
     *
     * @param msg the detail message.
     */
    public GMLException(String msg) {
        super(msg);
    }
}


