/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Management Committee (PMC)
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
package org.geotools.arcsde.data;

import java.io.IOException;

import org.geotools.arcsde.pool.ArcSDEConnectionPool;
import org.geotools.arcsde.pool.ArcSDEConnectionReference;
import org.geotools.arcsde.pool.Session;
import org.geotools.data.Transaction;

/**
 * Subclass of ArcSDEDataStore with enough hacks for it to limp along
 * with a SINGLE connection.
 * 
 * @author Jody Garnett
 */
public class ArcSDEMinimalDataStore extends ArcSDEDataStore {
    
    public ArcSDEMinimalDataStore( ArcSDEConnectionReference connPool ) {
        super(connPool);
    }

    @Override
    ArcSDEConnectionReference getConnectionPool() {
        return (ArcSDEConnectionReference) super.getConnectionPool();
    };
    
    protected synchronized FeatureTypeInfo getFeatureTypeInfo( String typeName, ArcSDEConnectionPool pool ) throws IOException {
        final ArcSDEConnectionReference reference = (ArcSDEConnectionReference) pool;
        final Session session = reference.getSession( Transaction.AUTO_COMMIT ); // we are doing the read only thing
        try {
            return getFeatureTypeInfo(typeName, session );
        } finally {
            session.close();
        }
    }
}
