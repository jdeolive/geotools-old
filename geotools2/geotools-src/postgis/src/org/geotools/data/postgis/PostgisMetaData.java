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

package org.geotools.data.postgis;

import java.util.logging.Logger;
import org.geotools.data.*;

/**
 * 
 *
 * @version $Id: PostgisMetaData.java,v 1.1 2003/03/28 19:23:47 cholmesny Exp $
 * @author Chris Holmes, TOPP
 */
public class PostgisMetaData implements DataSourceMetaData {

    /** Standard logging instance */
    private static final Logger LOGGER = 
        Logger.getLogger("org.geotools.defaultcore");
    
    public PostgisMetaData(){
    }

    /**
     * Retrieves whether this datasource supports basic transactions:
     * updateFeatures, modifyFeatures, and removeFeatures.
     *
     * @return true if so, false otherwise.
     */
    public boolean supportsTransactions(){
	return true;
    }

    /**
     * Retrieves whether this datasource supports multi transaction operations:
     * startMultiTransaction and endMultiTransaction
     *
     * @return true if so, false otherwise.
     */
    public boolean supportsMultiTransactions(){
	return true;
    }
	
    /**
     * Retrieves whether this datasource supports multi the setFeatures 
     * operation.
     *
     * @return true if so, false otherwise.
     */
    public boolean supportsSetFeatures(){
	return true;
    }


    /**
     * Retrives whether this datasource supports the setSchema operation.
     *
     * @return true if so, false otherwise.
     */
    public boolean supportsSetSchema(){
	return true;
    }


     /**
     * Retrives whether this datasource supports the abortLoading operation.
     *
     * @return true if so, false otherwise.
     */
    public boolean supportsAbort(){
	return false;
    }

     /**
     * Retrives whether this datasource returns meaningful results when
     * getBBox is called.
     *
     * @return true if so, false otherwise.
     */
    public boolean supportsGetBbox(){
	return false;
    }

}
