package org.geotools.datasource;

public class TableChangedEvent
{
	/** The code returned when the table extents changed successfully - this is the default */
	public static final int TABLE_OK = 0;
	/** The code returned when there was an error during the change in Extent (ie a load problem) */
	public static final int TABLE_ERROR = 1;
	
	private FeatureTable ft = null;
	private Extent requestedExtent = null;
	private DataSourceException exp = null;
	private int iCode = TABLE_OK;
	
	public TableChangedEvent (FeatureTable ft, Extent requestedExtent)
	{
		this.ft = ft;
		this.requestedExtent = requestedExtent;
	}
	
	public TableChangedEvent (int code, FeatureTable ft, Extent requestedExtent, DataSourceException exp)
	{
		iCode = code;
		this.ft = ft;
		this.requestedExtent = requestedExtent;
		this.exp = exp;
	}
	
	public FeatureTable getFeatureTable()
	{
		return ft;
	}
	
	public Extent getRequestedExtent()
	{
		return requestedExtent;
	}

	public Exception getException()
	{
		return exp;
	}
	
	public int getCode()
	{
		return iCode;
	}
}

