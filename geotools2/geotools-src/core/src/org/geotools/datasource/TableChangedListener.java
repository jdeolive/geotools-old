package org.geotools.datasource;

public interface TableChangedListener
{
	/** Gets called when a TableChangedEvent is fired
	 */
	public void tableChanged(TableChangedEvent tce);
}

