package org.geotools.wms;

import org.geotools.feature.Feature;
import java.io.OutputStream;

/** An interface for formatting a list of geotools Feature objects into a stream. IE, application/vnd.ogc.gml requests that the feature information be formatted in Geography Markup Language (GML)
 * FeatureFormatters are attached to the WMSServlet on initialization, and an arbitrary number can be attached
 */
public interface WMSFeatureFormatter
{
	/** Gets the mime-type of the stream written to by formatFeatures()
	 */
	public String getMimeType();
	
	/** Formats the given array of Features as this Formatter's mime-type and writes it to the given OutputStream
	 */
	public void formatFeatures(Feature [] features, OutputStream out);
}

