package org.geotools.datasource;

import com.sun.java.util.collections.List;
import com.sun.java.util.collections.Collections;
import com.sun.java.util.collections.Iterator;
import com.sun.java.util.collections.Vector;

/** The first, most straightforwd implementation of FeatureIndex
 */
public class SimpleIndex implements FeatureIndex
{
	/** The FeatureTable used as a source by this Index */
	protected FeatureTable f = null;
	/** The built array of references */
	protected Vector Features = null;
	
	protected int iColumnOffset = -1;
	protected String sColumnName = null;
	
	protected int state = STATE_INCOMPLETE;
	
	/** Initializes this Index around the given FeatureTable - sorted on a single column
	 */
	public SimpleIndex(FeatureTable ft, String columnName) throws IndexException
	{
		f = ft;
		
		// Check FeatureTable
		if (ft==null) throw new IndexException("Index: FeatureTable is null");
		// Check the column name
		if (columnName==null) throw new IndexException("Index: Column name is null");
		
		sColumnName = columnName;
		
		// Attach this index to the FeatureTable
		ft.addIndex(this);
	}

	/** Rebuilds this Index - usually called when the data this Index is based on changes
	 */
	public void rebuild() throws IndexException
	{
		if (iColumnOffset==-1)
		{
			// Gets the column offset of the column to be compared
			String [] sColumnNames = f.getColumnNames();
			if (sColumnNames==null)
				throw new IndexException("Index: FeatureTable Column Names are null");
			
			for (int i=0;i<sColumnNames.length;i++)
				if (sColumnNames[i].equalsIgnoreCase(sColumnName))
					iColumnOffset = i;
			if (iColumnOffset==-1)
				throw new IndexException("Index: Column name not found in FeatureTable("+sColumnName+")");		
		}
		
		// Rebuild index here
		state = STATE_BUILDING;
		
		List rows = f.getRows();
		Vector tempFeatures = new Vector();
		
		if (rows!=null)
		{
			Iterator it = rows.iterator();
			while (it.hasNext())
				tempFeatures.add(it.next());
		}
			
		try
		{
			// Sort columns
			Collections.sort(tempFeatures, new FeatureComparator(iColumnOffset));
		}
		catch (Exception exp)
		{
			throw new IndexException ("Index: Cannot sort Features :"+exp.getMessage());
		}
		
		Features = tempFeatures;
		
		state = STATE_BUILT;
		
		// Attached Indexes and other listener should be notified here
	}	
	
	/** Returns the state of the index
	 */
	public int getState()
	{
		return state;
	}
	
	/** Gets an array of references to the rows currently held by this Index.
	 */
	public List getFeatures()
	{
		return Features;
	}
	
}
