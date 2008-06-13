/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.data.jpox;

import java.io.IOException;

import javax.jdo.PersistenceManager;

import org.geotools.data.Transaction;
import org.geotools.data.Transaction.State;


public class JpoxTransactionState implements State {

	private PersistenceManager pm;
	
	public JpoxTransactionState( PersistenceManager pm ) {
		this.pm = pm;
	}
	
	public void addAuthorization( String AuthID ) throws IOException {
		throw new UnsupportedOperationException( "addAuthorization() not supported!" );
	}

	
	public void commit() throws IOException {
        // notify my collections that they should kill their cache!
		pm.currentTransaction().commit();
	}

	public void rollback() throws IOException {
        // notify my collections that they should kill their cache!        
		pm.currentTransaction().rollback();
	}

	public void setTransaction( Transaction transaction ) {
		if ( transaction == null ) {
			if ( pm.currentTransaction().isActive() ) {
				pm.currentTransaction().rollback();
			}
			pm = null;
		} else {
			//TODO: What now?
		}
	}

	PersistenceManager getPm() {
		return pm;
	}
	
	javax.jdo.Transaction getJpoxTransaction() {
		return pm.currentTransaction();
	}

}
