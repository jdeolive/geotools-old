package org.geotools.vpf;

import org.geotools.vpf.ifc.DataTypesDefinition;

/**
 * RowField.java
 *
 *
 * Created: Mon Jan 27 13:58:34 2003
 *
 * @author <a href="mailto:kobit@users.sourceforge.net">Artur Hefczyc</a>
 * @version
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
	return value.toString();
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
