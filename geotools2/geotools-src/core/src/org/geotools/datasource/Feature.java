package datasource;

/** Represents one row of a table. A Feature is a primary Object (can be any Object, usually a Geography)
 * plus any attributes which are associated with the Object.
 * Usually, the primary is at offset 0 in "row".
 * The column names are there for help, usually a reference to the same String[] as is returned
 * from DataSource.getColumnNames().
 */
public class Feature
{
	/** The row of Objects this Feature represents */
	public Object [] row;
	/** The names of the columns for this Feature */
	public String [] columnNames;
}

