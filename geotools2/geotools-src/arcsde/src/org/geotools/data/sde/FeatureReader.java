/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
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
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.geotools.data.sde;

import org.geotools.feature.Feature;
import org.geotools.data.DataSourceException;

/**
 * First attempt to get a streaming low level api for feature fetching.
 * This interface will be replaced by new data-exp API, but it still
 * here to support the old style filling of FeatureCollection inside
 * SdeDataSource, so this interface is likely to die.
 * @author Gabriel Roldán
 * @version 0.1
 */
public interface FeatureReader
{
  public boolean hasNext() throws DataSourceException;

  public Feature next() throws DataSourceException;

  public void rewind() throws DataSourceException;

  public void close();

  public boolean isClosed();

  public int size();
}