/*
 * Created on 16/10/2003
 *
 */
package org.geotools.data.jdbc;

/** QueryData Listener interfaces.  Used for notifying interested
 *  objects when a JDBCDataStore.QueryData object has been closed.
 * 
 *  <p>Im not sure if this should be an inner interface??? </p>
 * 
 * @author Sean  Geoghegan, Defence Science and Technology Organisation
 *
 */
public interface QueryDataListener {
    public void rowDeleted(JDBCDataStore.QueryData queryData);
    public void queryDataClosed(JDBCDataStore.QueryData queryData);
}
