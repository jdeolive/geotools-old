/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, Geotools Project Managment Committee (PMC)
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
package org.geotools.arcsde.pool;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.pool.ObjectPool;
import org.geotools.data.DataSourceException;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeDelete;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeInsert;
import com.esri.sde.sdk.client.SeLayer;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeQuery;
import com.esri.sde.sdk.client.SeRasterColumn;
import com.esri.sde.sdk.client.SeRegistration;
import com.esri.sde.sdk.client.SeRelease;
import com.esri.sde.sdk.client.SeSqlConstruct;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeTable;
import com.esri.sde.sdk.client.SeUpdate;
import com.esri.sde.sdk.client.SeVersion;

/**
 * Provides thread safe access to an SeConnection.
 * <p>
 * This class has become more and more magic over time! It no longer represents a Connection but provides
 * "safe" access to a connection.
 * <p>
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.3.x
 * 
 */
public class Session  {
    
    private static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geotools.arcsde.pool");

    /**
     * Lock used to protect the connection
     */
	private Lock lock;
	
	/** Actual SeConnection being protected */
	SeConnection connection;
    
	/**
	 * ObjectPool used to manage open connections (shared).
	 */
	private ObjectPool pool;

	private ArcSDEConnectionConfig config;

	private static int connectionCounter;

	private int connectionId;

	private boolean transactionInProgress;

	private boolean isPassivated;

	private Map<String, SeTable> cachedTables = new WeakHashMap<String, SeTable>();
	private Map<String, SeLayer> cachedLayers = new WeakHashMap<String, SeLayer>();
	private Map<String, SeRasterColumn> cachedRasters = new HashMap<String, SeRasterColumn>();

	/**
	 * Provides safe access to an SeConnection.
	 * @param pool ObjectPool used to manage SeConnection
	 * @param config Used to set up a SeConnection
	 * @throws SeException If we cannot connect
	 */
	public Session(ObjectPool pool, ArcSDEConnectionConfig config) throws SeException {	    
		this.connection = new SeConnection( config.getServerName(), config.getPortNumber().intValue(), config.getDatabaseName(), config.getUserName(), config.getUserPassword());
	    this.config = config;
		this.pool = pool;
		this.lock = new ReentrantLock(false);
		this.connection.setConcurrency(SeConnection.SE_UNPROTECTED_POLICY);
		
		synchronized (Session.class) {
			connectionCounter++;
			connectionId = connectionCounter;
		}
	}

	public final Lock getLock() {
		return lock;
	}

	public final boolean isClosed() {
		return this.connection.isClosed();
	}

	/**
	 * Marks the connection as being active (i.e. its out of the pool and ready to be used).
	 * <p>
	 * Shall be called just before being returned from the connection pool
	 * </p>
	 * 
	 * @see #markInactive()
	 * @see #isPassivated
	 * @see #checkActive()
	 */
	void markActive() {
		this.isPassivated = false;
	}

	/**
	 * Marks the connection as being inactive (i.e. laying on the connection pool)
	 * <p>
	 * Shall be callled just before sending it back to the pool
	 * </p>
	 * 
	 * @see #markActive()
	 * @see #isPassivated
	 * @see #checkActive()
	 */
	void markInactive() {
		this.isPassivated = true;
	}

	/**
	 * Returns whether this connection is on the connection pool domain or not.
	 * 
	 * @return <code>true</code> if this connection has beed returned to the pool and thus cannot be used, <code>false</code> if its safe to keep using it.
	 */
	public boolean isPassivated() {
		return isPassivated;
	}

	/**
	 * Sanity check method called before every public operation delegates to the superclass.
	 * 
	 * @throws IllegalStateException
	 *             if {@link #isPassivated() isPassivated() == true} as this is a serious workflow breackage.
	 */
	private void checkActive() {
		if (isPassivated()) {
			throw new IllegalStateException("Unrecoverable error: " + toString() + " is passivated, shall not be used!");
		}
	}

	public synchronized SeLayer getLayer(final String layerName) throws DataSourceException {
		checkActive();
		if (!cachedLayers.containsKey(layerName)) {
			try {
				cacheLayers();
			} catch (SeException e) {
				throw new DataSourceException("Can't obtain layer " + layerName, e);
			}
		}
		SeLayer seLayer = cachedLayers.get(layerName);
		if (seLayer == null) {
			throw new NoSuchElementException("Layer '" + layerName + "' not found");
		}
		return seLayer;
	}

	public synchronized SeRasterColumn getRasterColumn(final String rasterName) throws DataSourceException {
		checkActive();
		if (!cachedRasters.containsKey(rasterName)) {
			try {
				cacheRasters();
			} catch (SeException e) {
				throw new DataSourceException("Can't obtain raster " + rasterName, e);
			}
		}
		SeRasterColumn raster = cachedRasters.get(rasterName);
		if (raster == null) {
			throw new NoSuchElementException("Raster '" + rasterName + "' not found");
		}
		return raster;
	}

	public synchronized SeTable getTable(final String tableName) throws DataSourceException {
		checkActive();
        if (!cachedTables.containsKey(tableName)) {
            try {
                cacheLayers();
            } catch (SeException e) {
                throw new DataSourceException("Can't obtain table " + tableName, e);
            }
        }
        SeTable seTable = (SeTable) cachedTables.get(tableName);
        if (seTable == null) {
            throw new NoSuchElementException("Table '" + tableName + "' not found");
        }
        return seTable;
	}

    /**
     * Caches both tables and layers
     * @throws SeException
     */
    @SuppressWarnings("unchecked")
    private void cacheLayers() throws SeException {
        Vector/* <SeLayer> */layers = connection.getLayers();
        String qualifiedName;
        SeLayer layer;
        SeTable table;
        cachedTables.clear();
        cachedLayers.clear();
        for (Iterator it = layers.iterator(); it.hasNext();) {
            layer = (SeLayer) it.next();
            qualifiedName = layer.getQualifiedName();
            table = new SeTable(connection, qualifiedName);
            cachedLayers.put(qualifiedName, layer);
            cachedTables.put(qualifiedName, table);
        }
    }

	@SuppressWarnings("unchecked")
	private void cacheRasters() throws SeException {
		Vector<SeRasterColumn> rasters = this.connection.getRasterColumns();
		cachedRasters.clear();
		for (SeRasterColumn raster : rasters) {
			cachedRasters.put(raster.getQualifiedTableName(), raster);
		}
	}

	public void startTransaction() throws SeException {
		checkActive();
		this.connection.startTransaction();
		transactionInProgress = true;
	}

	public void commitTransaction() throws SeException {
		checkActive();
		this.connection.commitTransaction();
		transactionInProgress = false;
	}

	/**
	 * Returns whether a transaction is in progress over this connection
	 * <p>
	 * As for any other public method, this one can't be called if {@link #isPassivated()} is true.
	 * </p>
	 * 
	 * @return
	 */
	public boolean isTransactionActive() {
		checkActive();
		return transactionInProgress;
	}

	public void rollbackTransaction() throws SeException {
		checkActive();
	    this.connection.rollbackTransaction();
		transactionInProgress = false;
	}

	/**
	 * Return to the pool (may not close the internal connection, depends on pool settings).
	 * 
	 * @throws IllegalStateException
	 *             if close() is called while a transaction is in progress
	 * @see #destroy()
	 */
	public void close() throws IllegalStateException {
		checkActive();
		if (transactionInProgress) {
			throw new IllegalStateException("Transaction is in progress, should commit or rollback before closing");
		}

		try {
			if (LOGGER.isLoggable(Level.FINER)) {
				// StackTraceElement[] stackTrace =
				// Thread.currentThread().getStackTrace();
				// String caller = stackTrace[3].getClassName() + "." +
				// stackTrace[3].getMethodName();
				// System.err.println("<- " + caller + " returning " +
				// toString() + " to pool");
				LOGGER.finer("<- returning " + toString() + " to pool");
			}
			this.pool.returnObject(this);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	@Override
	public String toString() {
		return "ArcSDEPooledConnection[" + connectionId + "]";
	}

	/**
	 * Actually closes the connection
	 */
	void destroy() {
		try {
			this.connection.close();
		} catch (SeException e) {
			LOGGER.info("closing connection: " + e.getMessage());
		}
	}

	/**
	 * Compares for reference equality
	 */
	@Override
	public boolean equals(Object other) {
		return other == this;
	}

	@Override
	public int hashCode() {
		return 17 ^ this.config.hashCode();
	}
	//
	// Helper method that delgates to internal connection
	//
    @SuppressWarnings("unchecked")
    public List<SeLayer> getLayers() throws SeException {
        return connection.getLayers();
    }
    
    public String getUser() throws SeException {
        return connection.getUser();
    }

    public SeRelease getRelease() {
        return connection.getRelease();
    }
    public String getDatabaseName() throws SeException {
        return connection.getDatabaseName();
    }
    
    public void setConcurrency( int policy ) throws SeException {
        connection.setConcurrency( policy );
    }

    public void setTransactionAutoCommit( int auto ) throws SeException {
       connection.setTransactionAutoCommit( auto );
    }
    //
    // Factory methods that make use of internal connection
    // Q: How "long" are these objects good for? until the connection closes - or longer...
    //
    public SeLayer createSeLayer() throws SeException {
        return new SeLayer(connection);
    }
    public SeLayer createSeLayer( String tableName, String shape) throws SeException {
        return new SeLayer(connection, tableName, shape );
    }
    public SeQuery createSeQuery() throws SeException {
        return new SeQuery(connection);
    }
    public SeQuery createSeQuery( String[] propertyNames, SeSqlConstruct sql ) throws SeException {
        return new SeQuery(connection, propertyNames, sql );
    }
    
    public SeRegistration createSeRegistration( String typeName ) throws SeException {
        return new SeRegistration(connection, typeName);
    }

    public SeTable createSeTable( String qualifiedName ) throws SeException {
        return new SeTable(connection, qualifiedName);
    }

    public SeInsert createSeInsert() throws SeException {
        return new SeInsert(connection);
    }

    public SeUpdate createSeUpdate() throws SeException {
        return new SeUpdate(connection);
    }

    public SeDelete createSeDelete() throws SeException {
        return new SeDelete(connection);
    }

    public SeVersion createSeVersion( String versionName ) throws SeException {
        return new SeVersion( connection, versionName );
    }
    /**
     * Create an SeState for the provided id.
     * @param stateId stateId to use, or null
     * @return SeState
     * @throws SeException
     */
    public SeState createSeState( SeObjectId stateId ) throws SeException {
        if( stateId == null ){
            return createSeState();
        }
        return new SeState(connection, stateId );
    }
    public SeState createSeState() throws SeException {
        return new SeState( connection );
    }

    public SeRasterColumn createSeRasterColumn() throws SeException {
        return new SeRasterColumn( connection );
    }
    public SeRasterColumn createSeRasterColumn( SeObjectId rasterColumnId ) throws SeException {
        return new SeRasterColumn( connection, rasterColumnId );
    }
    /**
     * Schedule the provided Command for execution.
     * 
     * @param command
     */
    public void execute( Command command ){
    	try {
    		lock.lock();
            command.execute( connection );            
        }
        finally {
        	lock.unlock();
        }
    }    
}