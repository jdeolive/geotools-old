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

import com.vividsolutions.jts.geom.Envelope;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceException;
import org.geotools.feature.AttributeType;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.Filter;

import java.util.logging.Logger;

/**
 * Class VPFDataSource.java is responsible for 
 *
 * <p>
 * Created: Fri Mar 28 13:02:00 2003
 * </p>
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version 1.0.0
 */

public class VPFDataSource implements DataSource {
  
  protected Logger log = Logger.getLogger("org.geotools.vpf");

  public VPFDataSource() 
  {
    
  }
  
  // Implementation of org.geotools.data.DataSource

  /**
   * Describe <code><code>getFeatures</code></code> method here.
   *
   * @param filter a <code><code>Filter</code></code> value
   * @return a <code><code>FeatureCollection</code></code> value
   * @exception DataSourceException if an error occurs
   */
  public FeatureCollection getFeatures(Filter filter) throws DataSourceException
  {
    return null;
  }

  /**
   * Describe <code><code>getFeatures</code></code> method here.
   *
   * @param featureCollection a <code><code>FeatureCollection</code></code> value
   * @param filter a <code><code>Filter</code></code> value
   * @exception DataSourceException if an error occurs
   */
  public void getFeatures(FeatureCollection featureCollection, Filter filter)
    throws DataSourceException
  {
    
  }

  /**
   * Describe <code><code>addFeatures</code></code> method here.
   *
   * @param featureCollection a <code><code>FeatureCollection</code></code> value
   * @exception DataSourceException if an error occurs
   */
  public void addFeatures(FeatureCollection featureCollection) throws DataSourceException
  {
    
  }

  /**
   * Describe <code><code>removeFeatures</code></code> method here.
   *
   * @param filter a <code><code>Filter</code></code> value
   * @exception DataSourceException if an error occurs
   */
  public void removeFeatures(Filter filter) throws DataSourceException
  {
    
  }

  /**
   * Describe <code><code>modifyFeatures</code></code> method here.
   *
   * @param attributeTypeArray an <code><code>AttributeType[]</code></code> value
   * @param objectArray an <code><code>Object[]</code></code> value
   * @param filter a <code><code>Filter</code></code> value
   * @exception DataSourceException if an error occurs
   */
  public void modifyFeatures(AttributeType[] attributeTypeArray, Object[] objectArray, Filter filter) throws DataSourceException
  {
    
  }

  /**
   * Describe <code><code>modifyFeatures</code></code> method here.
   *
   * @param attributeType an <code><code>AttributeType</code></code> value
   * @param object an <code><code>Object</code></code> value
   * @param filter a <code><code>Filter</code></code> value
   * @exception DataSourceException if an error occurs
   */
  public void modifyFeatures(AttributeType attributeType, Object object, Filter filter) throws DataSourceException
  {
    
  }

  /**
   * Describe <code><code>abortLoading</code></code> method here.
   *
   */
  public void abortLoading()
  {
    
  }

  /**
   * Describe <code><code>getBbox</code></code> method here.
   *
   * @param flag a <code><code>boolean</code></code> value
   * @return an <code><code>Envelope</code></code> value
   */
  public Envelope getBbox(boolean flag)
  {
    return null;
  }

  /**
   * Describe <code><code>getBbox</code></code> method here.
   *
   * @return an <code><code>Envelope</code></code> value
   */
  public Envelope getBbox()
  {
    return null;
  }
  
}// VPFDataSource
