package org.geotools.datasource;

import com.sun.java.util.collections.Comparator;

/** Compares one row in a featureTable with another
 */
public class FeatureComparator implements Comparator
{
	private FeatureTable ft = null;	// The FeatureTable being sorted
	private int iColumnOffset = 0;		// The column offset to compare for each row 
	private int iColumnType = -1;	// The class type of the selected column
	
	private static final int CLASS_INTEGER = 0;
	private static final int CLASS_DOUBLE = 1;
	private static final int CLASS_FLOAT = 2;
	private static final int CLASS_STRING = 3;
	private static final int CLASS_BYTE = 4;
	private static final int CLASS_CHARACTER = 5;
	private static final int CLASS_LONG = 6;
	private static final int CLASS_SHORT = 7;
	private static final int CLASS_COMPARABLE = 10;
	
	public FeatureComparator(int iColumnOffset) throws IndexException
	{
		this.iColumnOffset = iColumnOffset;
	}
	
	public int compare(Object o1, Object o2)
	{
		// Both objects are Features (two rows of a FeatureTable) 
		// We need the Object at the right offset
		o1 = ((Feature)o1).row[iColumnOffset];
		o2 = ((Feature)o2).row[iColumnOffset];
		
		// Set the column type if it hasn't already been set
		if (iColumnType==-1)
		{
			if (o1 instanceof java.lang.Integer)
				iColumnType = CLASS_INTEGER;
			if (o1 instanceof java.lang.Double)
				iColumnType = CLASS_DOUBLE;
			if (o1 instanceof java.lang.Float)
				iColumnType = CLASS_FLOAT;
			if (o1 instanceof java.lang.String)
				iColumnType = CLASS_STRING;
			if (o1 instanceof java.lang.Byte)
				iColumnType = CLASS_BYTE;
			if (o1 instanceof java.lang.Character)
				iColumnType = CLASS_CHARACTER;
			if (o1 instanceof java.lang.Long)
				iColumnType = CLASS_LONG;
			if (o1 instanceof java.lang.Short)
				iColumnType = CLASS_SHORT;
			if (o1 instanceof java.lang.Comparable)
				iColumnType = CLASS_COMPARABLE;
		}
		
		switch (iColumnType)
		{
			case CLASS_INTEGER:
				return ((Integer)o1).intValue() - ((Integer)o2).intValue();
			case CLASS_DOUBLE:
				return (int)(((Double)o1).doubleValue() - ((Double)o2).doubleValue());
			case CLASS_FLOAT:
				return (int)(((Float)o1).floatValue() - ((Float)o2).floatValue());
			case CLASS_STRING:
				return ((String)o1).compareTo((String)o2);
			case CLASS_BYTE:
				return (int)(((Byte)o1).byteValue() - ((Byte)o2).byteValue());
			case CLASS_CHARACTER:
				return (int)(((Character)o1).charValue() - ((Character)o2).charValue());
			case CLASS_LONG:
				return (int)(((Long)o1).longValue() - ((Long)o2).longValue());
			case CLASS_SHORT:
				return (int)(((Short)o1).shortValue() - ((Short)o2).shortValue());
			case CLASS_COMPARABLE:
				return ((Comparable)o1).compareTo(o2);
				
		}
		return 0;
	}
	
	public boolean equals(Object obj)
	{
		return false;
	}
}

