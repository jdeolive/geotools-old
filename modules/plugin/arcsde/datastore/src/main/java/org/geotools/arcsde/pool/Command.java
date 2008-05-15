package org.geotools.arcsde.pool;

import java.io.IOException;

import com.esri.sde.sdk.client.SeConnection;

/**
 * Runnable used to interact with an ArcSDEConnection.
 * <p>
 * Instances of this class can the sent to {@link Session#execute(Command)} in order to be executed.
 * An ArcSDERunnable has exclusive access to the Connection for the duration. This facility is used
 * to prevent a series of complicated locks and try/catch/finally code.
 * 
 * @author Jody Garnett
 */
public abstract class Command {
    /**
     * Executed to operate on an SeConnection, a Command is scheduled for execution on a Session.
     * <p>
     * Please keep in mind that a Command should be short in duration; you are sharing this
     * SeConnection with other threads.
     * 
     * @param connection connection used to interact with ArcSDE
     */
    public abstract void execute(SeConnection connection) throws IOException;
}
