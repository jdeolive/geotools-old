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
import org.geotools.arcsde.pool.Session;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeObjectId;
import com.esri.sde.sdk.client.SeState;
import com.esri.sde.sdk.client.SeStreamOp;
import com.esri.sde.sdk.client.SeVersion;

/**
 * Handles a versioned table when in auto commit mode, meaning it sets up streams to edit directly
 * the default version.
 * 
 * @author Gabriel Roldan (TOPP)
 * @version $Id$
 * @since 2.5.x
 * @source $URL$
 */
public class AutoCommitDefaultVersionHandler implements ArcSdeVersionHandler {

    private SeVersion defaultVersion;

    public AutoCommitDefaultVersionHandler() throws IOException {
        //
    }

    public void setUpStream(final Session session, SeStreamOp streamOperation)
            throws IOException {

        try {
            if (defaultVersion == null) {
                defaultVersion = session.createSeVersion(SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME);
                defaultVersion.getInfo();
                SeState currentState = session.createSeState(defaultVersion.getStateId());
                if (!currentState.isOpen()) {
                    SeState newState = session.createSeState(null);
                    newState.create(currentState.getId());
                    defaultVersion.changeState(newState.getId());
                }
            }
            SeObjectId differencesId = new SeObjectId(SeState.SE_NULL_STATE_ID);
            defaultVersion.getInfo();
            SeObjectId currentStateId = defaultVersion.getStateId();
            streamOperation.setState(currentStateId, differencesId, SeState.SE_STATE_DIFF_NOCHECK);
        } catch (SeException e) {
            throw new ArcSdeException(e);
        }
    }

    public void editOperationWritten(SeStreamOp editOperation) throws IOException {
        //
    }

    public void editOperationFailed(SeStreamOp editOperation) throws IOException {
        //
    }

    /**
     * This method should not be called, but {@link #editOperationFailed(SeStreamOp)} instead, as
     * this is a handler for auto commit mode
     * 
     * @throws UnsupportedOperationException
     * @see {@link ArcSdeVersionHandler#rollbackEditState()}
     */
    public void commitEditState() throws IOException {
        throw new UnsupportedOperationException("commit shouldn't be called for"
                + " an autocommit versioning handler ");
    }

    /**
     * This method should not be called, but {@link #editOperationWritten(SeStreamOp)} instead, as
     * this is a handler for auto commit mode
     * 
     * @throws UnsupportedOperationException
     * @see {@link ArcSdeVersionHandler#rollbackEditState()}
     */
    public void rollbackEditState() throws IOException {
        throw new UnsupportedOperationException("rollback shouldn't be called for"
                + " an autocommit versioning handler ");
    }

}
