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

package org.geotools.data.oracle;

import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSourceFactorySpi;
import java.sql.SQLException;
import java.util.Map;


/**
 * A Factory class for the OracleDataSource.
 *
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: cholmesny $
 * @version $Id: OracleDataSourceFactory.java,v 1.1 2003/07/08 15:50:48 cholmesny Exp $
 */
public class OracleDataSourceFactory implements DataSourceFactorySpi {
    public OracleDataSourceFactory() {
    }

    /**
     * Creates an Oracle Data Source based on the given parameters.
     *
     * @param params A Map of parameters.
     *
     * @return The new OracleDataSource.
     *
     * @throws DataSourceException If an error occurs when creating the DataSource
     *
     * @see org.geotools.data.DataSourceFactorySpi#createDataSource(java.util.HashMap)
     */
    public DataSource createDataSource(Map params) throws DataSourceException {
        if (!canProcess(params)) {
            return null;
        }

        /* There are no defaults here. Calling canProcess verifies that
         * all these variables exist.
         */
        String host = (String) params.get("host");
        String port = (String) params.get("port");
        String instance = (String) params.get("instance");
        String user = (String) params.get("user");
        String passwd = (String) params.get("passwd");
        String tableName = (String) params.get("table");

        try {
            OracleConnectionFactory ocFactory = new OracleConnectionFactory(host, port, instance);
            ocFactory.setLogin(user, passwd);

            OracleDataSource ds = new OracleDataSource(ocFactory.getOracleConnection(), tableName);

            return ds;
        } catch (SQLException e) {
            throw new DataSourceException("Error creating oracle DataSource");
        }
    }

    /**
     * Gets a description of the data source.
     *
     * @return A description of the data source.
     */
    public String getDescription() {
        return "Oracle Spatial Database";
    }

    /**
     * Determines whether DataSources created by this factory can process the parameters.  Required
     * Parameters are:
     * 
     * <ul>
     * <li>
     * <code>dbtype</code> - must equal "oracle"
     * </li>
     * <li>
     * <code>host</code>
     * </li>
     * <li>
     * <code>port</code>
     * </li>
     * <li>
     * <code>user</code>
     * </li>
     * <li>
     * <code>passwd</code>
     * </li>
     * <li>
     * <code>instance</code>
     * </li>
     * <li>
     * <code>table</code>
     * </li>
     * </ul>
     * 
     * <p>
     * There are no defaults since each parameter must be explicitly defined by the user, or
     * another DataSourceFactorySpi should be used. This behaviour is defined in the
     * DataSourceFactorySpi contract.
     * </p>
     *
     * @param params The parameter to check.
     *
     * @return True if all the required parameters are supplied.
     */
    public boolean canProcess(Map params) {
        return (params.containsKey("dbtype") && params.get("dbtype").equals("oracle") &&
        params.containsKey("host") && params.containsKey("port") && params.containsKey("passwd") &&
        params.containsKey("instance") && params.containsKey("table"));
    }
}
