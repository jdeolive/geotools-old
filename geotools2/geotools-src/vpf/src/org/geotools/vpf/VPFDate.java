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

  protected SimpleDateFormat sdf =
	new SimpleDateFormat("yyyyMMddHHmmssZ");
  
  protected byte[] dateBin = null;

  public VPFDate(byte[] date)
  {
	dateBin = date;
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
