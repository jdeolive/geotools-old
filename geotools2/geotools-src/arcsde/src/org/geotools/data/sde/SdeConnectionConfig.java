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
package org.geotools.data.sde;

import java.util.Map;


/**
 * Represents a set of ArcSDE database connection parameters. Instances of this
 * class are used to validate ArcSDE connection params as in
 * <code>DataSourceFactory.canProcess(java.util.Map)</code> and serves as keys
 * for maintaining single <code>SdeConnectionPool</code>'s by each set of
 * connection properties
 *
 * @author Gabriel Roldán
 * @version 0.1
 */
public class SdeConnectionConfig
{
    /**
     * message of the exception thrown if a mandatory parameter is not supplied
     */
    private static final String NULL_ARGUMENTS_MSG =
        "Illegal arguments. At least one of them was null. Check to pass "
        + "correct values to dbtype, server, port, database, user and password parameters";

    /** DOCUMENT ME! */
    private static final String ILLEGAL_ARGUMENT_MSG = " is not valid for parameter ";

    /** must equals to <code>"arcsde"</code> */
    public static final String DBTYPE_PARAM = "dbtype";

    /** constant to pass "arcsde" as DBTYPE_PARAM */
    public static final String DBTYPE_PARAM_VALUE = "arcsde";

    /** ArcSDE server parameter name */
    public static final String SERVER_NAME_PARAM = "server";

    /** ArcSDE server port parameter name */
    public static final String PORT_NUMBER_PARAM = "port";

    /** ArcSDE databse name parameter name */
    public static final String INSTANCE_NAME_PARAM = "instance";

    /** ArcSDE database user name parameter name */
    public static final String USER_NAME_PARAM = "user";

    /** ArcSDE database user password parameter name */
    public static final String PASSWORD_PARAM = "password";

    /**
     * parameter name who's value represents the feature class for wich an
     * <code>SdeDataSource</code> will be created
     *
     * @task TODO: should this constant be moved to the SdeDataSource class?
     *       since SdeConnectionConfig thoes not validates the table param
     */
    protected static final String TABLE_NAME_PARAM = "table";

    /** name or IP of the ArcSDE server to connect to */
    String serverName;

    /** port number where the ArcSDE instance listens for connections */
    Integer portNumber;

    /** name of the ArcSDE database to connect to */
    String databaseName;

    /** database user name to connect as */
    String userName;

    /** database user password */
    String userPassword;

    /**
     * DOCUMENT ME!
     *
     * @param params
     *
     * @throws NullPointerException if at least one mandatory parameter is
     * @throws IllegalArgumentException if at least one mandatory parameter is
     *         present but has no a "valid" value.
     */
    public SdeConnectionConfig(Map params)
        throws NullPointerException, IllegalArgumentException
    {
        this((String) params.get(DBTYPE_PARAM),
            (String) params.get(SERVER_NAME_PARAM),
            String.valueOf(params.get(PORT_NUMBER_PARAM)),
            (String) params.get(INSTANCE_NAME_PARAM),
            (String) params.get(USER_NAME_PARAM),
            (String) params.get(PASSWORD_PARAM));
    }

    /**
     * DOCUMENT ME!
     *
     * @param dbType
     * @param serverName DOCUMENT ME!
     * @param portNumber DOCUMENT ME!
     * @param databaseName DOCUMENT ME!
     * @param userName DOCUMENT ME!
     * @param userPassword DOCUMENT ME!
     *
     * @throws NullPointerException
     * @throws IllegalArgumentException
     */
    public SdeConnectionConfig(String dbType, String serverName,
        String portNumber, String databaseName, String userName,
        String userPassword)
        throws NullPointerException, IllegalArgumentException
    {
        Integer port = checkParams(dbType, serverName, portNumber,
                databaseName, userName, userPassword);

        this.serverName = serverName;

        this.portNumber = port;

        this.databaseName = databaseName;

        this.userName = userName;

        this.userPassword = userPassword;
    }

    /**
     * DOCUMENT ME!
     *
     * @param dbType DOCUMENT ME!
     * @param serverName DOCUMENT ME!
     * @param portNumber DOCUMENT ME!
     * @param databaseName DOCUMENT ME!
     * @param userName DOCUMENT ME!
     * @param userPassword DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     * @throws NullPointerException DOCUMENT ME!
     */
    private Integer checkParams(String dbType, String serverName,
        String portNumber, String databaseName, String userName,
        String userPassword)
        throws IllegalArgumentException, NullPointerException
    {
        //check if dbtype is 'arcsde'
        if (!(DBTYPE_PARAM_VALUE.equals(dbType)))
        {
            throw new IllegalArgumentException("parameter dbtype must be "
                + DBTYPE_PARAM_VALUE);
        }

        //check for nullity
        if ((serverName == null) || (portNumber == null)
                || (databaseName == null) || (userName == null)
                || (userPassword == null))
        {
            throw new NullPointerException(NULL_ARGUMENTS_MSG);
        }

        if (serverName.length() == 0)
        {
            throwIllegal(SERVER_NAME_PARAM, serverName);
        }

        if (databaseName.length() == 0)
        {
            throwIllegal(INSTANCE_NAME_PARAM, databaseName);
        }

        if (userName.length() == 0)
        {
            throwIllegal(USER_NAME_PARAM, userName);
        }

        if (userPassword.length() == 0)
        {
            throwIllegal(PASSWORD_PARAM, userPassword);
        }

        Integer port = null;

        try
        {
            port = Integer.valueOf(portNumber);
        }
        catch (NumberFormatException ex)
        {
            throwIllegal(PORT_NUMBER_PARAM, portNumber);
        }

        return port;
    }

    /**
     * DOCUMENT ME!
     *
     * @param paramName DOCUMENT ME!
     * @param paramValue DOCUMENT ME!
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    private void throwIllegal(String paramName, String paramValue)
        throws IllegalArgumentException
    {
        throw new IllegalArgumentException("'" + paramValue + "'"
            + ILLEGAL_ARGUMENT_MSG + paramValue);
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getDatabaseName()
    {
        return databaseName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public Integer getPortNumber()
    {
        return portNumber;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getServerName()
    {
        return serverName;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * accessor method for retrieving the user password of the ArcSDE
     * connection properties holded here
     *
     * @return the ArcSDE user password
     */
    public String getUserPassword()
    {
        return userPassword;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public int hashCode()
    {
        return getServerName().hashCode() * getPortNumber().hashCode() * getDatabaseName()
                                                                             .hashCode() * getUserName()
                                                                                               .hashCode();
    }

    /**
     * checks for equality over another <code>SdeConnectionConfig</code>
     *
     * @param o DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean equals(Object o)
    {
        if (o == this)
        {
            return true;
        }

        if (!(o instanceof SdeConnectionConfig))
        {
            return false;
        }

        SdeConnectionConfig config = (SdeConnectionConfig) o;

        return config.getServerName().equals(getServerName())
        && config.getPortNumber().equals(getPortNumber())
        && config.getDatabaseName().equals(getDatabaseName())
        && config.getUserName().equals(getUserName());
    }
}
