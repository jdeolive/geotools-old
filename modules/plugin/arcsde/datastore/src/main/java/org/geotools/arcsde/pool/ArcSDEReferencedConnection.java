package org.geotools.arcsde.pool;

import java.io.IOException;

import org.apache.commons.pool.ObjectPool;

/**
 * There can be only one! It ends up being modal based on a transaction being around. If supplied
 * with Transaction.AUTO_COMMIT the connection is viewed as being read only.
 * 
 * @author Jody Garnett
 */
public class ArcSDEReferencedConnection extends Session {

    public ArcSDEReferencedConnection(ObjectPool pool, ArcSDEConnectionConfig config) throws IOException {
        super(pool, config);
    }

}
