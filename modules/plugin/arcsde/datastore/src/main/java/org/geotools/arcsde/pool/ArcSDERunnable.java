package org.geotools.arcsde.pool;

/**
 * Runnable used to interact with an ArcSDEConnection.
 * <p>
 * Instances of this class can the sent to ArcSDEDataStore.getConnection( ArcSDERunnable )
 * in order to be executed. An ArcSDERunnable has exclusive access to the Connection for the
 * duration. This facility is used to prevent a series of complicated locks and try/catch/finally
 * code.
 * 
 * @author Jody Garnett
 */
public abstract class ArcSDERunnable {
    /**
     * Executed to operate on an ArcSDEConnection, ArcTransactionState will be provided
     * when working on a transaction.
     * 
     * @param connection connection used to interact with ArcSDE
     * @param context Non null if working on a transaction
     */
    public abstract void run( Session connection );
}
