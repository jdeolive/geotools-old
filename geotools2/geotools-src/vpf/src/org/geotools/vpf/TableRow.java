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
import java.util.Iterator;
import java.util.Map;



/**
 * TableRow.java
 *
 *
 * Created: Thu Jan 02 23:58:39 2003
 *
 * @author <a href="mailto:kobit@users.fs.net">Artur Hefczyc</a>
 * @version 1.0
 */
public class TableRow 
{

  protected RowField[] fieldsArr = null;
  protected HashMap fieldsMap = null;

  
  public TableRow(RowField[] fieldsArr, HashMap fieldsMap)
  {
    this.fieldsArr = fieldsArr;
    this.fieldsMap = fieldsMap;
  } // TableRow constructor

  public String toString()
  {
    StringBuffer buff = new StringBuffer(" ["+getClass().getName());
    buff.append(" (fieldsMap=");
    if (fieldsMap == null)
    {
      buff.append("null)");
    } // end of if (columnDefs == null)
    else
    {
      Iterator it = fieldsMap.entrySet().iterator();
      while (it.hasNext())
      {
        Map.Entry entry = (Map.Entry)it.next();
        buff.append("\n"+
                    entry.getKey().toString()+"="+
                    entry.getValue().toString());
      } // end of while (it.hasNext())
      buff.append("\n)");
    } // end of if (columnDefs == null) else
    buff.append("]");
    return buff.toString();
  }
  
  public String toStringDev()
  {
    StringBuffer buff = new StringBuffer();
    buff.append(" fields:");
    if (fieldsMap == null)
    {
      buff.append("null)");
    } // end of if (columnDefs == null)
    else
    {
      Iterator it = fieldsMap.entrySet().iterator();
      while (it.hasNext())
      {
        Map.Entry entry = (Map.Entry)it.next();
        RowField field = (RowField)entry.getValue();
        buff.append("\n"+
                    entry.getKey().toString()+"="+
                    field.toStringDev());
      } // end of while (it.hasNext())
      buff.append("\n)");
    } // end of if (columnDefs == null) else
    return buff.toString();
  }
  
  public RowField get(String name)
  {
    return (RowField)fieldsMap.get(name);
  }

  public RowField get(int idx)
  {
    return fieldsArr[idx];
  }

} // TableRow
