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
package org.geotools.vpf.util;

import java.util.Iterator;



/**
 * Class ImmutableIterator.java is responsible for 
 *
 * <p>
 * Created: Fri Jan 24 14:32:48 2003
 * </p>
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version 1.0.0
 */

public class ImmutableIterator implements Iterator {

  protected Iterator iterator = null;

  public ImmutableIterator(Iterator it) 
  {
    iterator = it;
  }
  
  // Implementation of java.util.Iterator

  /**
   * Method <code>next</code> is used to perform 
   * @return an <code>Object</code> value
   */
  public Object next()
  {
    return iterator.next();
  }

  /**
   * Method <code>hasNext</code> is used to perform 
   * @return a <code>boolean</code> value
   */
  public boolean hasNext()
  {
    return iterator.hasNext();
  }

  /**
   * Method <code>remove</code> is used to perform 
   */
  public void remove()
  {
    throw new
      UnsupportedOperationException("It is immutable iterator implementation");
  }
  
}// ImmutableIterator
