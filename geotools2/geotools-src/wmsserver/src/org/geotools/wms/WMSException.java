package org.geotools.wms;

/** An Exception object in the same form as a WMS 1.1.1 exception message, which uese a code value and a detailed message to send erors back to the client.
 * This Exception type, when thrown by a implementor of WMSServer, will cause a valid WMS 1.1.1 exception code to be sent back to the client.
 */
public class WMSException extends Exception
{
	/** InvalidFormat Request contains a Format not offered by the service instance.*/
	public static final String WMSCODE_INVALIDFORMAT			= "InvalidFormat";
	/** InvalidSRS Request contains an SRS not offered by the service instance for one or more of the Layers in the request. */
	public static final String WMSCODE_INVALIDSRS				= "InvalidSRS";
	/** LayerNotDefined Request is for a Layer not offered by the service instance. */
	public static final String WMSCODE_LAYERNOTDEFINED			= "LayerNotDefined";
	/** StyleNotDefined Request is for a Layer in a Style not offered by the service instance. */
	public static final String WMSCODE_STYLENOTDEFINED			= "StyleNotDefined";
	/** LayerNotQueryable GetFeatureInfo request is applied to a Layer which is not declared queryable. */
	public static final String WMSCODE_LAYERNOTQUERYABLE		= "LayerNotQueryable";
	/** CurrentUpdateSequence Value of (optional) UpdateSequence parameter in GetCapabilities request is equal to current value of Capabilities XML update sequence number.*/
	public static final String WMSCODE_CURRENTUPDATESEQUENCE	= "CurrentUpdateSequence";
	/** InvalidUpdateSequence Value of (optional) UpdateSequence parameter in GetCapabilities request is greater than current value of Capabilities XML update sequence number.*/
	public static final String WMSCODE_INVALIDUPDATESEQUENCE	= "InvalidUpdateSequence";
	/** MissingDimensionValue Request does not include a sample dimension value, and the service instance did not declare a default value for that dimension. */
	public static final String WMSCODE_MISSINGDIMENSIONVALUE	= "MissingDimensionValue";
	/** InvalidDimensionValue Request contains an invalid sample dimension value. */
	public static final String WMSCODE_INVALIDDIMENSIONVALUE	= "InvalidDimensionValue";

	private String code = null;

	/** Constructs this Exception
	 * @param code A value for the exception code - can be null, or one of the above defined values
	 * @param msg A detailed message describing the error
	 */	
	public WMSException(String code, String msg)
	{
		super(msg);
		this.code = code;
	}
	
	/** Gets the exception code for this exception. Can be null, or one of the above defined values
	 * @return A valid WMS Exception code
	 */
	public String getCode()
	{
		return code;
	}
}

