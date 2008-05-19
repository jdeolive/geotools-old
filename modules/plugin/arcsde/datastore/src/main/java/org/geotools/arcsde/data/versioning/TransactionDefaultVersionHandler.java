/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, Geotools Project Managment Committee (PMC)
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
package org.geotools.arcsde.data.versioning;

import java.io.IOException;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.pool.Command;
import org.geotools.arcsde.pool.Session;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeStreamOp;
import com.esri.sde.sdk.client.SeVersion;

/**
 * Handles a versioned table when in transaction mode
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/versioning/TransactionDefaultVersionHandler.java $
 */
public class TransactionDefaultVersionHandler implements ArcSdeVersionHandler {

    private final Session session;

    private final SeVersion defaultVersion;

    // private SeObjectId initialStateId;

    // private SeVersion thisTransactionVersion;

    private SeState transactionState;

    public TransactionDefaultVersionHandler(final Session session) throws IOException {
        this.session = session;
        defaultVersion = session.issue(new Command<SeVersion>() {
            @Override
            public SeVersion execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                SeVersion defaultVersion = new SeVersion(connection,
                        SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME);
                defaultVersion.getInfo();
                return defaultVersion;
            }
        });
    }

    /**
     * Called by ArcSdeFeatureWriter.createStream
     * 
     * @see ArcSdeVersionHandler#
     */
    public void setUpStream(final Session session, final SeStreamOp streamOperation)
            throws IOException {

        session.issue(new Command<Void>() {
            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {

                if (transactionState == null) {
                    try {
                        defaultVersion.getInfo();
                        SeState parentState = new SeState(connection, defaultVersion.getStateId());
                        if (parentState.isOpen()) {
                            parentState.close();
                        }
                        transactionState = new SeState(connection);
                        transactionState.create(parentState.getId());
                    } catch (SeException e) {
                        throw new ArcSdeException(e);
                    }
                }
                final SeObjectId differencesId = new SeObjectId(SeState.SE_NULL_STATE_ID);
                final SeObjectId currentStateId = transactionState.getId();
                streamOperation.setState(currentStateId, differencesId,
                        SeState.SE_STATE_DIFF_NOCHECK);
                return null;
            }
        });
    }

    /**
     * Not called at all
     * 
     * @see ArcSdeVersionHandler#editOperationWritten(SeStreamOp)
     */
    public void editOperationWritten(SeStreamOp editOperation) throws IOException {
        // intentionally blank
    }

    /**
     * Not called at all
     * 
     * @see ArcSdeVersionHandler#editOperationFailed(SeStreamOp)
     */
    public void editOperationFailed(SeStreamOp editOperation) throws IOException {
        // intentionally blank
    }

    /**
     * Called by ArcTransactionState.commit()
     * 
     * @see ArcSdeVersionHandler#commitEditState()
     */
    public void commitEditState() throws IOException {
        if (transactionState == null) {
            return;
        }
        session.issue(new Command<Void>() {
            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                SeObjectId transactionStateId = transactionState.getId();
                defaultVersion.getInfo();
                defaultVersion.changeState(transactionStateId);
                // transactionState.trimTree(initialStateId, transactionStateId);
                transactionState = null;
                return null;
            }
        });
    }

    /**
     * Called by ArcTransactionState.rollback()
     * 
     * @see ArcSdeVersionHandler#rollbackEditState()
     */
    public void rollbackEditState() throws IOException {
        if (transactionState == null) {
            return;
        }
        session.issue(new Command<Void>() {

            @Override
            public Void execute(Session session, SeConnection connection) throws SeException,
                    IOException {
                transactionState.delete();
                transactionState = null;

                // parent state is closed, create a new one for it
                defaultVersion.getInfo();
                final SeObjectId defaultVersionStateId = defaultVersion.getStateId();
                final SeState defaultVersionState = new SeState(connection, defaultVersionStateId);
                if (!defaultVersionState.isOpen()) {
                    // create a new open state as child of the current version closed state
                    SeState newOpenState = new SeState(connection);
                    newOpenState.create(defaultVersionStateId);
                    final SeObjectId newStateId = newOpenState.getId();
                    defaultVersion.changeState(newStateId);
                }
                return null;
            }
        });
    }

}
