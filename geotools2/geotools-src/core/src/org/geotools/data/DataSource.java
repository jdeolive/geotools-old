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


import org.geotools.feature.*;
/**
 * The source of data for Features. Shapefiles, database, etc. are referenced
 * through this interface.
 * @version $Id: DataSource.java,v 1.1 2002/05/14 21:28:16 robhranac Exp $
 * @author ray
 */
public interface DataSource {
    
    /**
     * Loads Feature rows for the given Extent from the datasource.
     * @param ft featureTable to load features into
     * @param ex an extent defining which features to load - null means all
     * features
     * @throws DataSourceException if anything goes wrong
     */
    public void importFeatures(FeatureCollection ft, Extent ex)
           throws DataSourceException;
    
    /**
     * Saves the given features to the datasource.
     * @param ft featureTable to get features from
     * @param ex extent to define which features to write - null means all
     * @throws DataSourceException if anything goes wrong or if exporting is
     * not supported
     */
    public void exportFeatures(FeatureCollection ft, Extent ex)
           throws DataSourceException;
    
    /**
     * Stops this DataSource from loading.
     */
    public void stopLoading();
    
    /** gets the extent of this data source using the default speed of 
     * this datasource as set by the implementer. 
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
     public Extent getExtent();
    
    /** gets the extent of this data source using the speed of 
     * this datasource as set by the parameter.
     * @param speed if true then a quick (and possibly dirty) estimate of
     * the extent is returned. If false then a slow but acurate extent
     * will be returned
     * @return the extent of the datasource or null if unknown and too
     * expensive for the method to calculate.
     */
    public Extent getExtent(boolean speed);
}

