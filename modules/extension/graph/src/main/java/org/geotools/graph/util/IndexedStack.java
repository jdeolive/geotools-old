/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
 *    (C) 2002, Refractions Reserach Inc.
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
 */
package org.geotools.graph.util;

import java.util.HashMap;

public class IndexedStack extends java.util.Stack {
  private HashMap m_index; //object to index in stack 
  
  public IndexedStack() {
    super();
    m_index = new HashMap();  
  }
  
  public Object push(Object item) {
    m_index.put(item, new Integer(size()));
    return super.push(item);
  }

  public Object pop() {
    Object value = super.pop();
    m_index.remove(value);
    return(value);
  }

  public boolean contains(Object elem) {
    return(m_index.get(elem) != null);
  }

}