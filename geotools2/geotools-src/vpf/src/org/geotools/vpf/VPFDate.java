package org.geotools.vpf;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.text.SimpleDateFormat;
import java.text.ParseException;

/**
 * VPFDate.java
 *
 *
 * Created: Tue Jan 28 20:50:51 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version
 */

public class VPFDate {

  protected SimpleDateFormat sdf = null;
  protected byte[] dateBin = null;

  public VPFDate(byte[] date)
  {
	dateBin = (byte[])date.clone();
    initialize();
  }

  public VPFDate(String date)
  {
    dateBin = new byte[date.length()];
    for (int i = 0; i < date.length(); i++)
    {
      dateBin[i] = (byte)date.charAt(i);
    } // end of for (int i = 0; i < date.length(); i++)
    initialize();
  }

  protected void initialize()
  {
	for (int i = 0; i < dateBin.length; i++)
	{
	  if ((char)dateBin[i] == ' ')
	  {
		dateBin[i] = (byte)'0';
	  } // end of if (cr == ' ')
	} // end of for (int i = 0; i < dateBin.length; i++)
	sdf = new SimpleDateFormat("yyyyMMddHHmmss");
	StringBuffer sb = new StringBuffer();
	for (int i = 15; i < dateBin.length; i++)
	{
	  char cr = (char)dateBin[i];
	  if (i == 18)
	  {
		sb.append(':');
	  } // end of if (i == 18)
	  sb.append(cr);
	} // end of for (int i = 0; i < dateBin.length; i++)
	sdf.setTimeZone(TimeZone.getTimeZone(sb.toString()));
  }

  public String toString()
  {
	StringBuffer sb = new StringBuffer(dateBin.length);
	for (int i = 0; i < dateBin.length; i++) {
	  sb.append((char)dateBin[i]);
	} // end of for (int i = 0; i < dateBin.length; i++)
	return sb.toString();
  }

  public Date getDate()
  {
	try {
	  return sdf.parse(toString());
	} catch (ParseException e) {
      e.printStackTrace();
    } // end of try-catch
	return null;
  }

  public Calendar getCalendar()
  {
	try {
      sdf.parse(toString());
	  return sdf.getCalendar();
	} catch (ParseException e) {
      e.printStackTrace();
    } // end of try-catch
	return null;
  }
  
}// VPFDate
