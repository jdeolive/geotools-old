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
 * Comprehensive information about the datasource's capabilities.  This 
 * interface is implemented by datasource writers to let users know the 
 * capabilities of the datasource they are using. <p>
 * A user for this interface is commonly a tool that needs to discover how
 * to deal with the represented Datasource, such as when a new datasource is
 * created using the dynamic datasource plugin system.  The metadata can
 * be queried before attempting unsupported operations that would just throw
 * exceptions.
 *
 * @version $Id: DataSourceMetaData.java,v 1.1 2003/03/28 19:14:13 cholmesny Exp $
 * @author Chris Holmes, TOPP
 */

public interface DataSourceMetaData {
    
    /**
     * Retrieves whether this datasource supports basic transactions:
     * updateFeatures, modifyFeatures, and removeFeatures.
     *
     * @return true if so, false otherwise.
     */
    boolean supportsTransactions();

    /**
     * Retrieves whether this datasource supports multi transaction operations:
     * startMultiTransaction and endMultiTransaction
     *
     * @return true if so, false otherwise.
     */
    boolean supportsMultiTransactions();
	
    /**
     * Retrieves whether this datasource supports multi the setFeatures 
     * operation.
     *
     * @return true if so, false otherwise.
     */
    boolean supportsSetFeatures();


    /**
     * Retrives whether this datasource supports the setSchema operation.
     *
     * @return true if so, false otherwise.
     */
    boolean supportsSetSchema();


     /**
     * Retrives whether this datasource supports the abortLoading operation.
     *
     * @return true if so, false otherwise.
     */
    boolean supportsAbort();

     /**
     * Retrives whether this datasource returns meaningful results when
     * getBBox is called.
     *
     * @return true if so, false otherwise.
     */
    boolean supportsGetBbox();
}

