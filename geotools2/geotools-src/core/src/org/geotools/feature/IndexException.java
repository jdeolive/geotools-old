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

/**
 * Thrown when there is an error rebuilding an index.
 *
 * @version $Id: IndexException.java,v 1.3 2003/01/02 19:51:30 cholmesny Exp $
 * @author Ray Gallagher
 */
public class IndexException extends Exception{
    
    /**
     * Constructs a new IndexException.
     * @param msg Message explaining reason for exception
     */
    public IndexException(String msg){
        super(msg);
    }
}

