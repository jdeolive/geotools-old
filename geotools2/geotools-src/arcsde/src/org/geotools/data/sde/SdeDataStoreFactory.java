package org.geotools.data.sde;

import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.DataStore;
import java.util.Map;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author not attributable
 * @version 0.1
 */

public class SdeDataStoreFactory implements DataStoreFactorySpi
{
  /** package's logger */
  protected static final Logger LOGGER = Logger.getLogger(
          "org.geotools.data.sde");

  /** friendly factory description */
  private static final String FACTORY_DESCRIPTION = "ESRI ArcSDE 8.x";

  private static Param[] paramMetadata = new Param[7];
  static{
    /**@task TODO: add connection pool parameters and propose
     * optional/mandatory parameter indentification*/
    paramMetadata[0] = new Param("namespace", String.class, "namespace asociated to this data store");
    paramMetadata[1] = new Param("dbtype", String.class, "fixed value, must be \"arcsde\"");
    paramMetadata[2] = new Param("server", String.class, "sever name where the ArcSDE gateway is running");
    paramMetadata[3] = new Param("port", Integer.class, "port number in wich the ArcSDE server is listening for connections.Generally it's 5151");
    paramMetadata[4] = new Param("instance", String.class, "database instance name");
    paramMetadata[5] = new Param("user", String.class, "database user name");
    paramMetadata[6] = new Param("password", String.class, "database user password");
  }

  /** factory of connection pools to different SDE databases */
  private static final SdeConnectionPoolFactory poolFactory = SdeConnectionPoolFactory
      .getInstance();

  /**
   * empty constructor
   */
  public SdeDataStoreFactory()
  {
  }
    
    //HACK: to get data-exp merge to compile.
    public DataStore createNewDataStore(java.util.Map map){
	return null;
    }

  /**
   * crates an SdeDataSource based on connection parameters holded in
   * <code>params</code>.
   *
   * <p>
   * Expected parameters are:
   *
   * <ul>
   *  <li><b>dbtype</b>: MUST be <code>"arcsde"</code></li>
   *  <li><b>server</b>: machine name where ArcSDE is running</li>
   *  <li><b>port</b>: por number where ArcSDE listens for connections on server</li>
   *  <li><b>instance</b>: database instance name to connect to</li>
   *  <li><b>user</b>: database user name with at least reading privileges
   *                   over SDE instance</li>
   *  <li><b>password</b>: database user password</li>
   *  </ul>
   * </p>
   *
   * @param params connection parameters
   *
   * @return a new <code>SdeDataStore</code> pointing to the database
   * defined by <code>params</code>
   *
   * @throws java.io.IOException if somthing goes wrong creating the
   *         datastore.
   */
  public DataStore createDataStore(Map params) throws java.io.IOException
  {
      SdeDataStore sdeDStore = null;
      SdeConnectionConfig config = new SdeConnectionConfig(params);
      SdeConnectionPool connPool = poolFactory.getPoolFor(config);
      sdeDStore = new SdeDataStore(connPool);

      return sdeDStore;
  }

  /**
   * A human friendly name for this data source factory
   *
   * @return this factory's description
   */
  public String getDescription()
  {
      return FACTORY_DESCRIPTION;
  }

  /**
   * DOCUMENT ME!
   *
   * @param params
   *
   * @return
   */
  public boolean canProcess(Map params)
  {
      boolean canProcess = true;

      try
      {
          new SdeConnectionConfig(params);
      }
      catch (NullPointerException ex)
      {
          canProcess = false;
      }
      catch (IllegalArgumentException ex)
      {
          canProcess = false;
      }

      return canProcess;
  }

  public DataStoreFactorySpi.Param[] getParametersInfo()
  {
    return paramMetadata;
  }
}
