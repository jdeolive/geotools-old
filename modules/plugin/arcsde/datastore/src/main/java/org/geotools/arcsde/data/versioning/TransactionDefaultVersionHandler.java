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
package org.geotools.arcsde.data.versioning;

import java.io.IOException;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.arcsde.pool.Command;
import org.geotools.arcsde.pool.ISession;
import org.geotools.data.DataSourceException;

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
 * @version $Id: TransactionDefaultVersionHandler.java 30808 2008-06-25
 *          17:03:07Z groldan $
 * @since 2.5.x
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/arcsde/datastore/src/main/java/org/geotools/arcsde/data/versioning/TransactionDefaultVersionHandler.java $
 */
public class TransactionDefaultVersionHandler implements ArcSdeVersionHandler {

    private final ISession session;

    private final SeVersion defaultVersion;

    // private SeObjectId initialStateId;

    // private SeVersion thisTransactionVersion;

    /**
     * The state used for the transaction, its a sibling of the current state
     */
    private SeState transactionState;

    public TransactionDefaultVersionHandler(final ISession session) throws IOException {
        this.session = session;
        defaultVersion = session.getDefaultVersion();
    }

    /**
     * Called by ArcSdeFeatureWriter.createStream
     * 
     * @see ArcSdeVersionHandler#
     */
    public void setUpStream(final ISession session, final SeStreamOp streamOperation)
            throws IOException {

        session.issue(new Command<Void>() {
            @Override
            public Void execute(ISession session, SeConnection connection) throws SeException,
                    IOException {

                if (transactionState == null) {
                    try {
                        defaultVersion.getInfo();
                        final SeState currentState = new SeState(connection, defaultVersion
                                .getStateId());
                        final long currentStateId = currentState.getId().longValue();
                        transactionState = session.createChildState(currentStateId);

                        // grab a lock on the state
                        transactionState.lock();

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
            public Void execute(ISession session, SeConnection connection) throws SeException,
                    IOException {
                SeObjectId transactionStateId = transactionState.getId();
                defaultVersion.getInfo();
                defaultVersion.changeState(transactionStateId);

                transactionState.freeLock();
                // transactionState.trimTree(initialStateId,
                // transactionStateId);
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
            public Void execute(ISession session, SeConnection connection) throws SeException,
                    IOException {
                transactionState.freeLock();
                transactionState.delete();
                transactionState = null;

                // parent state is closed, create a new one for it
                // defaultVersion.getInfo();
                // final SeObjectId defaultVersionStateId =
                // defaultVersion.getStateId();
                // final SeState defaultVersionState = new SeState(connection,
                // defaultVersionStateId);
                // if (!defaultVersionState.isOpen()) {
                // // create a new open state as child of the current version
                // closed state
                // SeState newOpenState = new SeState(connection);
                // newOpenState.create(defaultVersionStateId);
                // final SeObjectId newStateId = newOpenState.getId();
                // defaultVersion.changeState(newStateId);
                // }
                return null;
            }
        });
    }

}
