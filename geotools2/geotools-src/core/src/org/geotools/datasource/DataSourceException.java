package datasource;

/** Thrown when there is an error rebuilding an index
 */
public class DataSourceException extends Exception
{
	public DataSourceException(String msg)
	{
		super(msg);
	}
}

