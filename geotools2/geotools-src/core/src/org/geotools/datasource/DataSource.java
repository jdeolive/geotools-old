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
package org.geotools.datasource;


import org.geotools.featuretable.*;
/**
 * The source of data for Features. Shapefiles, database, etc. are referenced through
 * this interface.
 * @version $Id: DataSource.java,v 1.6 2002/04/16 16:20:03 jmacgill Exp $
 */
public interface DataSource {
    /** Loads Feature rows for the given Extent from the datasource
     * @param ft featureTable to load features into
     * @param ex an extent defining which features to load - null means all features
     * @throws DataSourceException if anything goes wrong
     */
    public void importFeatures(FeatureTable ft, Extent ex) throws DataSourceException;
    
    /** Saves the given features to the datasource
     * @param ft feature table to get features from
     * @param ex extent to define which features to write - null means all
     * @throws DataSourceException if anything goes wrong or if exporting is not supported
     */
    public void exportFeatures(FeatureTable ft, Extent ex) throws DataSourceException;
    
    /** Stops this DataSource from loading
     */
    public void stopLoading();
    
}

