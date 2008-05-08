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
