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

import java.util.ListIterator;



/**
 * Class ImmutableListIterator.java is responsible for 
 *
 * <p>
 * Created: Fri Jan 24 14:50:15 2003
 * </p>
 * @version $Id: ImmutableListIterator.java,v 1.1 2003/01/24 15:02:23 kobit Exp $
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 */

public class ImmutableListIterator implements ListIterator {

  protected ListIterator listIterator = null;
  
  public ImmutableListIterator(ListIterator lit) 
  {
    listIterator = lit;
  }
  
  // Implementation of java.util.ListIterator

  /**
   * Method <code>add</code> is used to perform 

   *
   * @param object an <code>Object</code> value
   */
  public void add(Object object)
  {
    throw new
      UnsupportedOperationException("It is immutable iterator implementation");
  }

  /**
   * Method <code>next</code> is used to perform 

   *
   * @return an <code>Object</code> value
   */
  public Object next()
  {
    return listIterator.next();
  }

  /**
   * Method <code>hasNext</code> is used to perform 

   *
   * @return a <code>boolean</code> value
   */
  public boolean hasNext()
  {
    return listIterator.hasNext();
  }

  /**
   * Method <code>remove</code> is used to perform 

   *
   */
  public void remove()
  {
    throw new
      UnsupportedOperationException("It is immutable iterator implementation");
  }

  /**
   * Method <code>set</code> is used to perform 

   *
   * @param object an <code>Object</code> value
   */
  public void set(Object object)
  {
    throw new
      UnsupportedOperationException("It is immutable iterator implementation");
  }

  /**
   * Method <code>previousIndex</code> is used to perform 

   *
   * @return an <code>int</code> value
   */
  public int previousIndex()
  {
    return listIterator.previousIndex();
  }

  /**
   * Method <code>previous</code> is used to perform 

   *
   * @return an <code>Object</code> value
   */
  public Object previous()
  {
    return listIterator.previous();
  }

  /**
   * Method <code>nextIndex</code> is used to perform 

   *
   * @return an <code>int</code> value
   */
  public int nextIndex()
  {
    return listIterator.nextIndex();
  }

  /**
   * Method <code>hasPrevious</code> is used to perform 

   *
   * @return a <code>boolean</code> value
   */
  public boolean hasPrevious()
  {
    return listIterator.hasPrevious();
  }
  
}// ImmutableListIterator
