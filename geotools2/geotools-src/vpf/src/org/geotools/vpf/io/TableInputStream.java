package org.geotools.vpf.io;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.geotools.vpf.TableHeader;
import org.geotools.vpf.TableRow;

/**
 * TableInputStream.java
 *
 *
 * Created: Thu Jan 02 22:32:27 2003
 *
 * @author <a href="mailto:kobit@users.sf.net">Artur Hefczyc</a>
 * @version 1.0
 */
public class TableInputStream extends FileInputStream 
{
  public static final int AHEAD_BUFFER_SIZE = 0;
  
  protected TableHeader header = null;
  protected List rowsReadAhead = new LinkedList();
  
  public TableInputStream(File file)
    throws IOException
  {
    super(file);
    readHeader();
  }

  public TableInputStream(FileDescriptor fdObj)
    throws IOException
  {
    super(fdObj);
    readHeader();
  }

  public TableInputStream(String file)
    throws IOException
  {
    super(file);
    readHeader();
  }

  public TableHeader getHeader()
  {
    return header;
  }

  protected void readHeader()
  {
  }

  public TableRow readRow()
  {
    return null;
  }

  public int readRows(TableRow[] rows)
  {
    return 0;
  }

  public int availableRows()
  {
    return rowsReadAhead.size();
  }
  
} // TableInputStream
