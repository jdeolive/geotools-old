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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import org.geotools.vpf.io.TableInputStream;

/**
 * Class VPFDataBase.java is responsible for 
 *
 * <p>
 * Created: Fri Apr 04 09:39:00 2003
 * </p>
 * @unittest on
 * @unittest_code "Put test code below"
 *  return true;
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version 1.0.0
 */

public class VPFDataBase 
{

  TableRow dataBaseInfo = null;
  TableRow[] libraries = null;
  
  public VPFDataBase(File directory)
    throws IOException
  {
    // read data base header info
    String vpfTableName = new File(directory, "dht").toString();
    TableInputStream vpfTable = new TableInputStream(vpfTableName);
    dataBaseInfo = (TableRow)vpfTable.readRow();
    vpfTable.close();

    // read libraries info
    vpfTableName = new File(directory, "lat").toString();;
    vpfTable = new TableInputStream(vpfTableName);
    ArrayList al = new ArrayList();
    TableRow tableRow = (TableRow)vpfTable.readRow();
    while (tableRow != null)
    {
      al.add(tableRow);
      tableRow = (TableRow)vpfTable.readRow();
    } // end of while (tableRow != null)
    vpfTable.close();
    libraries = (TableRow[])al.toArray(new TableRow[al.size()]);
  }
  
}// VPFDataBase
