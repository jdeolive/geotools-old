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

import java.util.*;
import org.geotools.data.*;
import org.geotools.datasource.extents.*;

import org.geotools.filter.*;

//Logging system
import org.apache.log4j.Logger;

/**
 * The default feature collection holds and passes out features promiscuously
 * to requesting clients.  It does not guarantee that features are of a certain
 * type or that they follow a specific schema. 
 * 
 * @version $Id: FeatureCollectionDefault.java,v 1.6 2002/07/21 19:32:42 jmacgill Exp $
 * @author  James Macgill, CCG<br>
 * @author  Rob Hranac, VFNY<br>
 */
public class FeatureCollectionDefault implements FeatureCollection {

    private static Logger log = Logger.getLogger("defaultcore");
    
    /* Internal feature storage list */
    private List features = new Vector();

    /* Internal listener storage list */
    private List listeners = new Vector();

    /* Pointer to the datasource */
    private DataSource data;

    /* The currently loaded extent */
    private Extent loadedExtent;
    
    /** 
     * Creates a new instance of DefaultFeatureTable
     *
     */
    public FeatureCollectionDefault() {
    }
    
    /** 
     * Creates a new instance of DefaultFeatureTable
     *
     * @param data 
     */
    public FeatureCollectionDefault(DataSource data){
        setDataSource(data);
    }



    /* ***********************************************************************
     * Managing data source and extents.
     * ***********************************************************************/
    /** 
     * Creates a new instance of DefaultFeatureTable.
     *
     * @param data 
     */
    public void setDataSource(DataSource data) {
        this.data = data;
    }

    /** 
     * Creates a new instance of DefaultFeatureTable.
     *
     * @return The datasource that the Feature Collection is currently attached
     *         to.
     */
    public DataSource getDataSource() {
        return this.data;
    }


    /**
     * Gets the loaded Extent of this FeatureTable.
     * The Extent of current loaded Features in this table.
     */
    public void setExtent(Extent extent) {
        this.loadedExtent = extent;
    }
    

    /**
     * Gets the loaded Extent of this FeatureTable.
     * The Extent of current loaded Features in this table.
     */
    public Extent getExtent() {
        return this.loadedExtent;
    }
    


    /* ***********************************************************************
     * Managing collection listeners.
     * ***********************************************************************/
    /** 
     * Adds a listener for table events.
     */
    public void addListener(CollectionListener spy) {
        listeners.add(spy);
    }
    
    /**
     * Removes a listener for table events.
     */
    public void removeListener(CollectionListener spy) {
        listeners.remove(spy);
    }
    

    /* ***********************************************************************
     * Managing features via the datasource.
     * ***********************************************************************/
    /** 
     * Gets the features in the datasource inside the loadedExtent.
     * Will not trigger a datasourceload.
     * Functionally equivalent to getFeatures(getLoadedExtent());
     *
     * @see #getfeatures(Extent ex)
     */
    public Feature[] getFeatures() {
        return (Feature[]) features.toArray(new Feature[features.size()]);
    }
    

    /** 
     * Gets the features in the datasource inside the Extent ex.
     * This may trigger a load on the datasource.
     */
    public Feature[] getFeatures(Extent ex) 
        throws DataSourceException {
        try{
        // TODO: 2
        // Replace this idiom with a loadedExtent = loadedExtent.or(extent)
        //  idiom.  I think?
        Extent toLoad[];
        if (loadedExtent != null){
            toLoad = loadedExtent.difference(ex);
        }
        else {
            toLoad = new Extent[]{ex};
        }
        
        for (int i = 0; i < toLoad.length; i++){
            //TODO: move this code to its own method?
            if (toLoad[i] != null){
                if (data != null){
                    log.debug("loading " + i);
                    org.geotools.filter.GeometryFilter gf =
                      new org.geotools.filter.GeometryFilter(AbstractFilter.GEOMETRY_BBOX);
                    ExpressionLiteral right = 
                      new BBoxExpression(((EnvelopeExtent)toLoad[i]).getBounds());
                    gf.addRightGeometry(right);
                    data.getFeatures(this,gf);
                }
                if (loadedExtent == null){
                    loadedExtent = toLoad[i];
                }
                else {
                    loadedExtent = loadedExtent.combine(toLoad[i]);
                }
            }
        }
        log.debug("calling getfeatures");
        return getFeatures();
        }
        catch(IllegalFilterException ife){
            throw new DataSourceException(ife.toString());
        }
    }
    
    

    /** 
     * Removes the features from this FeatureTable which fall into the
     * specified extent, notifying TableChangedListeners that the table has
     * changed.
     * @param ex The extent defining which features to remove
     */
    public void removeFeatures(Extent ex) {
        //TODO: remove the features
    }

    /**
     * Removes the features from this FeatureTable, notifying
     * TableChangedListeners that the table has changed.
     * @param features The Features to remove
     */ 
    public void removeFeatures(Feature[] features) {
        //TODO: remove the features
    }

    /** 
     * Adds the given List of Features to this FeatureTable.
     *
     * @param features The List of Features to add
     */
    public void addFeatures(Feature[] features) {
        this.features.addAll(Arrays.asList(features));
    }
    public void addFeatures(List features){
        this.features.addAll(features);
    }
    
    
}
