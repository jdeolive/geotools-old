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
package org.geotools.data;

/**
 * Thrown when there is an error in a datasource.
 * 
 * @version $Id: DataSourceException.java,v 1.5 2002/09/03 17:01:13 ianturton Exp $
 * @author Ray Gallagher
 */
public class DataSourceException extends Exception {
    /**
     * Constructs a new instance of DataSourceException
     * 
     * @param msg A message explaining the exception
     */
    public DataSourceException(String msg) {
        super(msg);
    }

    /**
     * Constructs a new instance of DataSourceException 
     * 
     * @param msg A message explaining the exception
     * @param exp the throwable object which caused this exception
     */
    public DataSourceException(String msg, Throwable exp) {
        super(msg, exp);
    }
}