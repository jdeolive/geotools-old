/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.data;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * Simple Catalog so we can try out the api.
 * 
 * <p>
 * This class intends to track the Catalog API as it provides more metadata
 * information. It is intended to be an In Memory data structure.
 * </p>
 * 
 * <p>
 * Other projectswill produce more persistent Catalog implementations.
 * GeoServer for instance will back it's Catalog implementation with XML
 * files.
 * </p>
 *
 * @author Jody Garnett, Refractions Research
 */
public class DefaultCatalog implements Catalog {
    protected String defaultPrefix = null;
    protected Map namespaces = new HashMap();
    protected Set datastores = new HashSet();
    
    /**
     * Retrieve prefix set.
     * 
     * @see org.geotools.data.Catalog#getPrefixes()
     * 
     * @return Set of namespace prefixes
     */
    public Set getPrefixes() {
        return Collections.unmodifiableSet( namespaces.keySet() );
    }

    /**
     * Retrieve Namespace for prefix.
     * <p>
     * This class will lazily create the namespace as required.
     * </p>
     * @see org.geotools.data.Catalog#getNamespace(java.lang.String)
     * 
     * @param prefix Prefix used for Namespace
     * @return Namespace associated with Prefix
     */
    public synchronized NamespaceMetaData getNamespaceMetaData(String prefix) {
        if( namespaces.containsKey( prefix)){
            return (NamespaceMetaData) namespaces.get( prefix );
        }
        NamespaceMetaData namespace = createNamespaceMetaData( prefix ); 
        namespaces.put( prefix, namespace );         
        return namespace;
    }
    
    /**
     * Default implementation creates DefaultNamespaceMetaData.
     * <p>
     * You will need to override this to work with your own Namespace type.
     * This is the usual Factory method patter, as opposed to the
     * AbstractFactory used by most GeoTools2 based code.
     * </p>
     * @param prefix Prefix used for XML output of namespace 
     */
    protected NamespaceMetaData createNamespaceMetaData( String prefix ){
        return new DefaultNamespaceMetaData( prefix );        
    }
    
    /**
     * Implement lockExists.
     * 
     * @see org.geotools.data.Catalog#lockExists(java.lang.String)
     * 
     * @param lockID
     */
    public boolean lockExists(String lockID) {
        if( lockID == null ) return false;
        DataStore store;
        LockingManager lockManager;
                
        for( Iterator i=datastores.iterator(); i.hasNext(); ){
             store = (DataStore) i.next();
             lockManager = store.getLockingManager();
             if( lockManager == null ) continue; // did not support locking
             if( lockManager.exists( lockID ) ){
                 return true;
             }
        }
        return false;
    }
    /**
     * Implement lockRefresh.
     * <p>
     * Currently it is an error if the lockID is not found. Because if
     * we can't find it we cannot refresh it.
     * </p>
     * <p>
     * Since locks are time sensitive it is impossible to check
     * if a lockExists and then be sure it will still exist when you try to
     * refresh it. Nothing we do can protect client code from this fact, they
     * will need to do with the IOException when (not if) this situation
     * occurs.
     * </p>
     * @see org.geotools.data.Catalog#lockRefresh(java.lang.String, org.geotools.data.Transaction)
     * 
     * @param lockID Authorizataion of lock to refresh
     * @param transaction Transaction used to authorize refresh
     * @throws IOException If opperation encounters problems, or lock not found
     * @throws IllegalArgumentException if lockID is <code>null</code>
     */
    public boolean lockRefresh(String lockID, Transaction transaction) throws IOException{
        if( lockID == null ){
            throw new IllegalArgumentException("lockID required");
        }
        if( transaction == null || transaction == Transaction.AUTO_COMMIT ){
            throw new IllegalArgumentException("Tansaction required (with authorization for "+lockID+")");        
        }
        
        DataStore store;
        LockingManager lockManager;
        
        boolean refresh = false;
        for( Iterator i=datastores.iterator(); i.hasNext(); ){
             store = (DataStore) i.next();
             lockManager = store.getLockingManager();
             if( lockManager == null ) continue; // did not support locking
                          
             if( lockManager.release( lockID, transaction )){
                 refresh = true;    
             }                           
        }
        return refresh;        
    }

    /**
     * Implement lockRelease.
     * <p>
     * Currently it is <b>not</b> and error if the lockID is not found, it may
     * have expired. Since locks are time sensitive it is impossible to check
     * if a lockExists and then be sure it will still exist when you try to
     * release it.
     * </p>
     * @see org.geotools.data.Catalog#lockRefresh(java.lang.String, org.geotools.data.Transaction)
     * 
     * @param lockID Authorizataion of lock to refresh
     * @param transaction Transaction used to authorize refresh
     * @throws IOException If opperation encounters problems
     * @throws IllegalArgumentException if lockID is <code>null</code>
     */
    public boolean lockRelease(String lockID, Transaction transaction) throws IOException{
        if( lockID == null ){
            throw new IllegalArgumentException("lockID required");
        }
        if( transaction == null || transaction == Transaction.AUTO_COMMIT ){
            throw new IllegalArgumentException("Tansaction required (with authorization for "+lockID+")");        
        }
    
        DataStore store;
        LockingManager lockManager;
                
        boolean release = false;                
        for( Iterator i=datastores.iterator(); i.hasNext(); ){
             store = (DataStore) i.next();
             lockManager = store.getLockingManager();
             if( lockManager == null ) continue; // did not support locking
         
             if( lockManager.release( lockID, transaction )){
                 release = true;    
             }             
        }
        return release;        
    }

    /**
     * Implement getDefaultPrefix.
     * <p>
     * Description ...
     * </p>
     * @see org.geotools.data.Catalog#getDefaultPrefix()
     * 
     * @return
     */
    public String getDefaultPrefix() {
        return defaultPrefix;
    }
    public void setDefaultPrefix( String prefix ){
        defaultPrefix = prefix;
    }
    /**
     * Implement registerDataStore.
     * <p>
     * Description ...
     * </p>
     * @see org.geotools.data.Catalog#registerDataStore(org.geotools.data.DataStore)
     * 
     * @param dataStore
     * @throws IOException
     */
    public void registerDataStore(DataStore dataStore) throws IOException {
        if( datastores.contains( dataStore )){
            throw new IOException("DataStore already registered with Catalog");
        }
        // TODO: register FeatureTypes with namespace ...
    }

    /**
     * Implement getDataStores.
     * <p>
     * Description ...
     * </p>
     * @see org.geotools.data.Catalog#getDataStores(java.lang.String)
     * 
     * @param namespace
     * @return
     */
    public Set getDataStores(String namespace) {
        return Collections.unmodifiableSet( datastores );
    }
    
    /**
     * Convience method for Accessing FeatureSource by prefix:typeName.
     * <p>
     * This method is part of the public Catalog API. It allows the Validation
     * framework to be writen using only public Geotools2 interfaces.
     * </p>
     * @see org.geotools.data.Catalog#getFeatureSource(java.lang.String, java.lang.String)
     * 
     * @param prefix Namespace prefix in which the FeatureType available
     * @param typeName typeNamed used to identify FeatureType
     * @return
     */
    public FeatureSource getFeatureSource(String prefix, String typeName) throws IOException {
        NamespaceMetaData namespace = getNamespaceMetaData( prefix );
        FeatureTypeMetaData featureType = namespace.getFeatureTypeMetaData( typeName );
        DataStoreMetaData dataStore = featureType.getDataStoreMetaData();
        
        return dataStore.getDataStore().getFeatureSource( typeName );       
    }
    /**
     * Access to the set of DataStores in use by GeoServer.
     * <p>
     * The provided Set may not be modified :-)
     * </p>
     * @see org.geotools.data.Catalog#getDataStores(java.lang.String)
     * 
     * @param namespace
     * @return
     */
    public Set getDataStores() {
        return Collections.unmodifiableSet( datastores );
    }
}
