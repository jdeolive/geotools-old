package datasource;

import com.sun.java.util.collections.List;

/** An Index is built up around a FeatureTable, using one of the columns in FeatureTable 
 * as a comparable reference. An object in a column can be any object, but must either be a 
 * java base-type Object (Integer, String, Character, etc.) or implement Comparable. 
 * An Index built on such a column will sort its array of object references using 
 * FeatureComparator. Implement this to perform more complex Index building.
 */
public interface FeatureIndex
{
	public static final int STATE_BUILT = 0;
	public static final int STATE_BUILDING = 1;
	public static final int STATE_INCOMPLETE = 2;

	/** Rebuilds this Index - usually called when the data this Index is based on changes
	 */
	public void rebuild() throws IndexException;
	
	/** Returns the state of the index
	 */
	public int getState();
	
	/** Gets an array of references to the rows currently held by this Index.
	 */
	public List getFeatures();
}

