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
package org.geotools.data;

/**
 * Used to lock features when used with LockingDataSource.
 * 
 * <p>
 * A FeatureLockFactory is used to generate FeatureLocks.
 * </p>
 *
 * @author jgarnett, Refractions Research, Inc.
 * @version $Id: FeatureLock.java,v 1.1 2003/09/22 17:44:35 cholmesny Exp $
 *
 * @see <a
 *      href="http://vwfs.refractions.net/docs/Database_Research.pdf">Database
 *      Reseach</a>
 * @see <a
 *      href="http://vwfs.refractions.net/docs/Transactional_WFS_Design.pdf">Transactional
 *      WFS Design</a>
 * @see <a
 *      href="http://vwfs.refractions.net/docs/Design_Implications.pdf">Design
 *      Implications</a>
 * @see FeatureLockFactory
 */
public interface FeatureLock {
    /**
     * LockId used for transaction authorization.
     *
     * @return A string of the LockId.
     */
    String getAuthorization();

    /**
     * Time from now the lock will expire
     *
     * @return A long of the time till the lock expires.
     */
    long getDuration();
}
