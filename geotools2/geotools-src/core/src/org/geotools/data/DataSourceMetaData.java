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
 * @version $Id: DataSourceMetaData.java,v 1.2 2003/05/08 19:01:25 cholmesny Exp $
 * @author Chris Holmes, TOPP
 */

public interface DataSourceMetaData {
    
    /**
     * Retrieves whether this datasource supports addFeatures.
     *
     * @return <tt>true</tt> the addFeatures method is supported, 
     * <tt>false</tt> otherwise.
     */
    boolean supportsAdd();

    /**
     * Retrieves whether this datasource supports removeFeatures.
     *
     * @return <tt>true</tt> the removeFeatures method is supported, 
     * <tt>false</tt> otherwise.
     */
    boolean supportsRemove();

     /**
     * Retrieves whether this datasource supports removeFeatures.
     *
     * @return <tt>true</tt> the modifyFeatures method is supported, 
     * <tt>false</tt> otherwise.
     */
    boolean supportsModify();

    /**
     * Retrieves whether this datasource implements the
     * setAutoCommit(boolean) and rollback() methods of the DataSource
     * Interface. 
     *
     * @return <tt>true</tt> if the rollback methods are supported, 
     * <tt>false</tt> otherwise.
     * @see DataSource#setAutoCommit(boolean)
     * @see DataSource#rollback()
     */
    boolean supportsRollbacks();
	
    /**
     * Retrieves whether the datasource supports the {@link DataSource#setFeatures(FeatureCollection) setFeatures} operation. 
     * operation.
     *
     * @return <tt>true</tt> if the setFeatures method is supported, 
     * <tt>false</tt> otherwise.
     */
    boolean supportsSetFeatures();


   
     /**
     * Retrieves whether this datasource supports the {@link DataSource#abortLoading() abortLoading} operation.
     *
     * @return <tt>true</tt> if the abortLoading method is supported, 
     * <tt>false</tt> otherwise.
     */
    boolean supportsAbort();

     /**
     * Retrieves whether this datasource returns meaningful results when
     * getBBox is called.
     *
     * @return <tt>true</tt> if the getBbox method is supported, 
     * <tt>false</tt> otherwise.
     */
    boolean supportsGetBbox();

    /**
     * Retrieves whether the getBbox operation of the datasource will return
     * relatively quickly.  Programmers who care about the speed of calculating
     * the bounding box should query this method before calling getBbox.
     *
     * @return <tt>true</tt> if a getBbox call will return quickly, 
     * <tt>false</tt> otherwise.
     */
    boolean hasFastBbox();
}

