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
package org.geotools.data.sde;

import org.geotools.data.DataSourceException;
import java.util.*;
import java.util.logging.*;


/**
 * DOCUMENT ME!
 *
 * @author Gabriel Roldán
 * @version $Id: SdeConnectionPoolFactory.java,v 1.3 2003/11/14 17:21:04 groldan Exp $
 */
public class SdeConnectionPoolFactory
{
    /** DOCUMENT ME!  */
    private static Logger LOGGER = Logger.getLogger("org.geotools.data.sde");

    /** DOCUMENT ME!  */
    private static SdeConnectionPoolFactory singleton;

    /** DOCUMENT ME!  */
    private Map currentPools = new HashMap();

    /**
     * Creates a new SdeConnectionPoolFactory object.
     */
    private SdeConnectionPoolFactory()
    {
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public synchronized static SdeConnectionPoolFactory getInstance()
    {
        if (singleton == null)
            singleton = new SdeConnectionPoolFactory();

        return singleton;
    }

    /**
     * DOCUMENT ME!
     *
     * @param config DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws DataSourceException DOCUMENT ME!
     */
    public synchronized SdeConnectionPool getPoolFor(SdeConnectionConfig config)
        throws DataSourceException
    {
        SdeConnectionPool pool = (SdeConnectionPool) currentPools.get(config);

        if (pool == null)
        {
            pool = new SdeConnectionPool(config);

            pool.populate();

            currentPools.put(config, pool);
        }

        return pool;
    }

    /**
     * DOCUMENT ME!
     */
    public void clear()
    {
        closeAll();

        currentPools.clear();

        LOGGER.fine("sde connection pools creared");
    }

    /**
     * DOCUMENT ME!
     */
    public void closeAll()
    {
        for (Iterator it = currentPools.values().iterator(); it.hasNext();)
            ((SdeConnectionPool) it.next()).close();
    }

    /**
     * DOCUMENT ME!
     */
    public void finalize()
    {
        closeAll();
    }
}
