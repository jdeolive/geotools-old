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

import java.util.*;


/**
 * Represents a set of ArcSDE database connection parameters. Instances of this
 * class are used to validate ArcSDE connection params as in
 * <code>DataSourceFactory.canProcess(java.util.Map)</code> and serves as keys
 * for maintaining single <code>SdeConnectionPool</code>'s by each set of
 * connection properties
 *
 * @author Gabriel Roldán
 * @version $Id: SdeConnectionConfig.java,v 1.4 2003/11/25 17:41:20 groldan Exp $
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

    public static final String MIN_CONNECTIONS_PARAM = "pool.minConnections";
    public static final String MAX_CONNECTIONS_PARAM = "pool.maxConnections";
    public static final String CONNECTIONS_INCREMENT_PARAM = "pool.increment";
    public static final String CONNECTION_TIMEOUT_PARAM = "pool.timeOut";

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

    Integer minConnections = null;
    Integer maxConnections = null;
    Integer connTimeOut = null;
    Integer increment = null;

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
      init(params);
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
      Map params = new HashMap();
      params.put(DBTYPE_PARAM, dbType);
      params.put(SERVER_NAME_PARAM, serverName);
      params.put(PORT_NUMBER_PARAM, portNumber);
      params.put(INSTANCE_NAME_PARAM, databaseName);
      params.put(USER_NAME_PARAM, userName);
      params.put(PASSWORD_PARAM, userPassword);
      init(params);
    }

    private void init(Map params)
    throws NumberFormatException, IllegalArgumentException
    {
      String dbtype = (String)params.get(DBTYPE_PARAM);
      String server = (String)params.get(SERVER_NAME_PARAM);
      String port = (String)params.get(PORT_NUMBER_PARAM);
      String instance = (String)params.get(INSTANCE_NAME_PARAM);
      String user = (String)params.get(USER_NAME_PARAM);
      String pwd = (String)params.get(PASSWORD_PARAM);

      Integer _port = checkParams(dbtype, server, port, instance, user, pwd);
      this.serverName = server;
      this.portNumber = _port;
      this.databaseName = instance;
      this.userName = user;
      this.userPassword = pwd;

      setUpOptionalParams(params);
    }

    private void setUpOptionalParams(Map params)
    {
        this.minConnections = getInt(params.get(MIN_CONNECTIONS_PARAM),
                                        SdeConnectionPool.DEFAULT_CONNECTIONS);
        this.maxConnections = getInt(params.get(MAX_CONNECTIONS_PARAM),
                                        SdeConnectionPool.DEFAULT_MAX_CONNECTIONS);
        this.increment = getInt(params.get(CONNECTIONS_INCREMENT_PARAM),
                                   SdeConnectionPool.DEFAULT_INCREMENT);
        this.connTimeOut = getInt(params.get(CONNECTION_TIMEOUT_PARAM),
                                     SdeConnectionPool.DEFAULT_MAX_WAIT_TIME);
    }

    private static final Integer getInt(Object value, int defaultValue)
    {
      if(value == null)
        return new Integer(defaultValue);

      String sVal = String.valueOf(value);
      try {
        return Integer.valueOf(sVal);
      }
      catch (NumberFormatException ex) {
        return new Integer(defaultValue);
      }
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
  public Integer getConnTimeOut()
  {
    return connTimeOut;
  }
  public Integer getIncrement()
  {
    return increment;
  }
  public Integer getMaxConnections()
  {
    return maxConnections;
  }
  public Integer getMinConnections()
  {
    return minConnections;
  }

  public String toString()
  {
    StringBuffer sb = new StringBuffer(getClass().getName() + "[");
    sb.append("dbtype=");
    sb.append(this.DBTYPE_PARAM_VALUE);
    sb.append(", server=");
    sb.append(this.serverName);
    sb.append(", port=");
    sb.append(this.portNumber);
    sb.append(", instance=");
    sb.append(this.databaseName);
    sb.append(", user=");
    sb.append(this.userName);
    sb.append(", password=");
    sb.append(this.userPassword);
    sb.append(", minConnections=");
    sb.append(this.minConnections);
    sb.append(", maxConnections=");
    sb.append(this.maxConnections);
    sb.append(", connTimeOut=");
    sb.append(this.connTimeOut);
    sb.append(", connIncrement=");
    sb.append(this.increment);
    sb.append("]");
    return sb.toString();
  }
}
