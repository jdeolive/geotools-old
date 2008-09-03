/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.data;

import java.io.IOException;
import org.opengis.util.InternationalString;
import org.geotools.factory.Factory;


/**
 * Constructs a live DataAccess connection from a set of parameters.
 * <p>
 * Parameters are specified using a Java Bean; the DataAccesFinder utility class will take care of
 * allowing you to work with Map<String,Serializable> as a transfer object of bean properties.
 * </p>
 *
 * @author Jody Garnett
 * @author Thomas Marti
 * @author Stefan Schmid
 *
 * @source $URL$
 * @version $Id$
 */
public interface DataAccessFactory extends Factory {
    /**
     * Display name for this DataAccess in the current locale.
     *
     * @return human readable display name
     */
    public InternationalString getName();

    /**
     * Test to ensure the correct environment is available for this Factory to function.
     * <p>
     * Implementations usually check such things as availablity of required JDBC drivers,
     * or Java Advanced Imaging formats that they intend to use.
     * </p>
     * @return <code>true</code>, if needed environment is found
     */
    public boolean isAvailable();

    /**
     * A java bean (with default properties values) describing connection parameters.
     *
     * @return Java Bean describing parameters required for data access
     */
    Object createAccessBean();

    /**
     * <p>Test to see if this factory is suitable for processing this connectionParamsBean.</p>
     *
     * <p>This method is often an <code>instanceof</code> check followed by ensuring required
     * bean properties (i.e. connection parameters) are not <code>null</code>.</p>
     *
     * @param connectionPrametersBean
     * @return <code>true</code>, if bean has valid parameters to attempt a connection
     */
    boolean canAccess(Object connectionPrametersBean);

    /**
     * <p>Connect to a physical data storage location and provide <code>DataAccess</code> class for
     * interaction.</p>
     *
     * <p>A new <code>DataAccess</code> class is created on each call; end-users should either store
     * this instance as a Singleton (gasp!) or make use of the GeoTools catalog facilities to manage
     * connections.</p>
     *
     * @param bean Bean capturing connection parameters, should be of the same type as provided by
     *        createConnectionBean
     * @return The created <code>DataAccess</code> instance
     * @throws IOException If there were any problems setting up the connection
     */
    DataAccess createAccess(Object bean) throws IOException;

    /**
     * Please note that creating a new physical storage location
     * may require additional parameters beyond that needed for
     * simple connection.
     *
     * @return Java Bean describing parameters required for creation
     */
    Object createContentBean();

    /**
     * <p>Confirm that this factory is suitable for creating the physical storage
     * location described by the provided bean.</p>
     *
     * <p>Implementations may also chose to check security concerns (such as the ability
     * to write to disk) as part of this method.</p>
     *
     * @param bean Bean capturing connection/creation parameters, should be of the same
     *             type as provided by {@link #createContentBean()}
     * @return <code>true</code>, if bean has valid parameters to attempt a connection
     */
    boolean canCreateContent(Object bean);

    /**
     * Set up a new physical storage location, and supply a <code>DataAccess</code> class
     * for interaction.
     *
     * @param bean Bean capturing connection/creation parameters, should be of the same
     *             type as provided by {@link #createContentBean()}
     * @return The created <code>DataAccess</code> instance
     */
    DataAccess createContent(Object bean);
}
