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

import org.geotools.vpf.ifc.DataTypesDefinition;

/**
 * RowField.java
 *
 *
 * Created: Mon Jan 27 13:58:34 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version $Id: RowField.java,v 1.4 2003/01/30 12:51:14 kobit Exp $
 */

public class RowField implements DataTypesDefinition
{

  protected Object value = null;
  protected char type = CHAR_NULL_VALUE;

  public RowField(Object value, char type)
  {
	this.value = value;
	this.type = type;
  }

  public String toString()
  {
    if (value != null)
    {
      return value.toString()+" ("+type+")";
    } // end of if (value != null)
    else
    {
      return "null ("+type+")";
    } // end of if (value != null) else
  }

  public char getType()
  {
	return type;
  }

  public Object getValue()
  {
	return value;
  }

  public String getAsString()
  {
    if (value != null)
    {
      return value.toString();
    } // end of if (value != null)
    else
    {
      return null;
    } // end of else
  }

  public int getAsInt()
  {
	return ((Number)value).intValue();
  }

  /**
   * <code>getAsLong</code> returns <code>long</code> value but it
   * is <code>java int</code> value conerted to <code>java long</code>.
   * VPF standard support <code>long int</code> values, however it is
   * type of 32 bits what is <code>java int</code> type indeed.
   *
   * @return a <code>long</code> value
   */
  public long getAsLong()
  {
	return ((Number)value).longValue();
  }

  public short getAsShort()
  {
	return ((Number)value).shortValue();
  }

  public float getAsFloat()
  {
	return ((Number)value).floatValue();
  }
  
  public double getAsDouble()
  {
	return ((Number)value).doubleValue();
  }
  
}// RowField
