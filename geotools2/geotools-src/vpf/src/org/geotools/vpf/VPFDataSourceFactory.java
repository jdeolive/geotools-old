/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * version 2.1 of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */
package org.geotools.vpf;

import java.util.HashMap;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.data.DataSourceFactorySpi;



/**
 * Class VPFDataSourceFactory.java is responsible for 
 *
 * <p>
 * Created: Fri Mar 28 15:54:32 2003
 * </p>
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version 1.0.0
 */

public class VPFDataSourceFactory implements DataSourceFactorySpi {
  public VPFDataSourceFactory() 
  {
    
  }
  
  // Implementation of org.geotools.data.DataSourceFactorySpi

  /**
   * Describe <code><code>createDataSource</code></code> method here.
   *
   * @param hashMap a <code><code>HashMap</code></code> value
   * @return a <code><code>DataSource</code></code> value
   * @exception DataSourceException if an error occurs
   */
  public DataSource createDataSource(HashMap hashMap) throws DataSourceException
  {
    return null;
  }

  /**
   * Describe <code><code>getDescription</code></code> method here.
   *
   * @return a <code><code>String</code></code> value
   */
  public String getDescription()
  {
    return null;
  }

  /**
   * Describe <code><code>canProcess</code></code> method here.
   *
   * @param hashMap a <code><code>HashMap</code></code> value
   * @return a <code><code>boolean</code></code> value
   */
  public boolean canProcess(HashMap hashMap)
  {
    return false;
  }
  
}// VPFDataSourceFactory
