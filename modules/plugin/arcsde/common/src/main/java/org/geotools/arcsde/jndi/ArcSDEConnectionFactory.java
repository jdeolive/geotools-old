/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2009, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.arcsde.jndi;

import static org.geotools.arcsde.session.ArcSDEConnectionConfig.CONNECTION_TIMEOUT_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.INSTANCE_NAME_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.MAX_CONNECTIONS_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.MIN_CONNECTIONS_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.PASSWORD_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.PORT_NUMBER_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.SERVER_NAME_PARAM_NAME;
import static org.geotools.arcsde.session.ArcSDEConnectionConfig.USER_NAME_PARAM_NAME;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

import org.geotools.arcsde.session.ArcSDEConnectionConfig;

/**
 * A JNDI {@link ObjectFactory} to create a
 * 
 * @author Gabriel Roldan (OpenGeo)
 * @version $Id$
 * @since 2.5.7
 */
public class ArcSDEConnectionFactory implements ObjectFactory {

    /**
     * @see ObjectFactory#getObjectInstance(Object, Name, Context, Hashtable)
     */
    public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx,
            final Hashtable<?, ?> environment) throws Exception {

        final Reference ref = (Reference) obj;

        System.out.println("\n\tArcSDEConnectionFactory: ref is " + ref);

        final String className = ref.getClassName();

        System.out.println("\n\tArcSDEConnectionFactory: className is " + className);

        Object dereferencedObject = null;
        if (ArcSDEConnectionConfig.class.getName().equals(className)) {
            ArcSDEConnectionConfig config = createConfig(ref);

            System.out.println("\n\tArcSDEConnectionFactory: config is " + config);
            dereferencedObject = config;
        } else {
            System.out.println("\n\tArcSDEConnectionFactory: not a config");
        }

        return dereferencedObject;
    }

    private ArcSDEConnectionConfig createConfig(final Reference ref) {
        System.out.println("\n\tArcSDEConnectionFactory: creating config");
        String server = getProperty(ref, SERVER_NAME_PARAM_NAME, null);
        String port = getProperty(ref, PORT_NUMBER_PARAM_NAME, null);
        String user = getProperty(ref, USER_NAME_PARAM_NAME, null);
        String password = getProperty(ref, PASSWORD_PARAM_NAME, null);

        if (server == null) {
            throw new IllegalArgumentException("Missing param: " + SERVER_NAME_PARAM_NAME);
        }
        if (port == null) {
            throw new IllegalArgumentException("Missing param: " + PORT_NUMBER_PARAM_NAME);
        } else {
            try {
                Integer.valueOf(port);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("port shall be a number: " + port);
            }
        }
        if (user == null) {
            throw new IllegalArgumentException("Missing param: " + USER_NAME_PARAM_NAME);
        }

        if (password == null) {
            throw new IllegalArgumentException("Missing param: " + PASSWORD_PARAM_NAME);
        }

        String instance = getProperty(ref, INSTANCE_NAME_PARAM_NAME, null);
        String minConnections = getProperty(ref, MIN_CONNECTIONS_PARAM_NAME, "1");
        String maxConnections = getProperty(ref, MAX_CONNECTIONS_PARAM_NAME, "6");
        String connTimeout = getProperty(ref, CONNECTION_TIMEOUT_PARAM_NAME, "500");

        ArcSDEConnectionConfig config = new ArcSDEConnectionConfig();
        config.setServerName(server);
        config.setPortNumber(Integer.parseInt(port));
        config.setDatabaseName(instance);
        config.setUserName(user);
        config.setPassword(password);
        config.setMinConnections(Integer.parseInt(minConnections));
        config.setMaxConnections(Integer.parseInt(maxConnections));
        config.setConnTimeOut(Integer.parseInt(connTimeout));
        return config;
    }

    protected String getProperty(final Reference ref, final String propName, final String defValue) {
        final RefAddr addr = ref.get(propName);
        if (addr == null) {
            return defValue;
        }
        return (String) addr.getContent();
    }

}
