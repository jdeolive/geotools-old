/* $Id: ConnectionPoolManager.java,v 1.2 2003/11/21 18:51:20 jive Exp $
 * 
 * Created on 15/08/2003
 */
package org.geotools.data.jdbc;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.sql.ConnectionPoolDataSource;

/** Provides a Singleton manager of connection pools.
 * 
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: jive $
 * @version $Id: ConnectionPoolManager.java,v 1.2 2003/11/21 18:51:20 jive Exp $
 * Last Modified: $Date: 2003/11/21 18:51:20 $ 
 */
public class ConnectionPoolManager {
    /** The singleton instance of the ConnectionPoolManager. */
    private static ConnectionPoolManager instance;
    /** Map containing the connection pools. */
    private Map connectionPools = new HashMap();

    /** Private constructor to enforce Singleton
     * 
     */
    private ConnectionPoolManager() {        
    }
    
    /** Gets the instance of the ConnectionPoolManager.
     * 
     * @return The one and only instance of ConnectionPoolManager.
     */
    public static synchronized ConnectionPoolManager getInstance() {
        if (instance == null) {
            instance = new ConnectionPoolManager();            
        }

        return instance;
    }
    
    /** Gets a ConnectionPool for a ConnectionPoolDataSource.
     * 
     *  <p>This method will return a connection pool that contains
     *  the ConnectionPoolDataSource.  If a ConnectionPool exists that
     *  already contains the ConnectionPoolDataSource, it will be returned,
     *  otherwise a ConnectionPool will be created for the ConnectionPoolDataSource.
     * 
     * @param cpds The ConnectionPoolDataSource to get a ConnectionPool for.
     * @return The ConnectionPool.
     */
    public synchronized ConnectionPool getConnectionPool(final ConnectionPoolDataSource cpds) {
        ConnectionPool connectionPool = (ConnectionPool) connectionPools.get(cpds);
        
        if (connectionPool == null){
            connectionPool = new ConnectionPool(cpds);
            connectionPools.put(cpds, connectionPool);
        }
        
        return connectionPool;
    }
    public synchronized void free( ConnectionPool pool ){
        if( !pool.isClosed() ){
            pool.close();
        }
        connectionPools.values().remove( pool );                
    }
    public synchronized void closeAll() {
        for (Iterator iter = connectionPools.values().iterator(); iter.hasNext();) {
            ConnectionPool pool = (ConnectionPool) iter.next();
            iter.remove();
            pool.close();
        }
    }
}
