package org.geotools.wms;

import java.awt.image.BufferedImage;
import java.awt.Color;
import org.geotools.feature.Feature;

/** Interface for any class wishing to comply to the OGC Web Map Service (WMS) specification. 
 * Certain details have been left out, such as a FORMAT parameter on the getMap call - this is because the format is not specified here, the return value being a BufferedImage.
 */
public interface WMSServer
{
	/** Gets the capabilites of this server as an XML-formatted string. 
	 * @param version The requested version of the Capabilities XML - used for version Negotiation
	 * @return The capabilites of this server. The string must conform to the OGC WMS spec at http://www.digitalearth.gov/wmt/xml/capabilities_1_1_1.dtd
	 */
	public String getCapabilities(String version) throws WMSException;

	/** Gets a map image
	 * @param layers The Identifying names of the Layers to display. These Layers are assumed to be installed and ready on the server. The layers are drawn from the order in which they are in the list (first bottommost)
	 * @param styles The styles to use for each Layer - one for one
	 * @param srs The Spatial Reference system to use
	 * @param bbox The bounding box to use for the map. In the form minx, miny, maxx, maxy (lower left, upper right), in SRS units. The values are dependant on the spatial reference system in use, and may be rounded off
	 * @param width The width of the image, in pixels
	 * @param height The height of the image, in pixels
	 * @param transparent Whether the background of the map is transparent
	 * @param bgcolor The background color of the map
	 * @return A java.awt.Image object of the drawn map.
	 */
	public BufferedImage getMap(String [] layers, String [] styles, String srs, double [] bbox, int width, int height, boolean transparent, Color bgcolor) throws WMSException;
	
	/** Gets the Feature info for given Layers. The first 5 parameters are the same as those in the call to getMap. Feature Info can only be returned for 
	 * those layers for which the attribute queryable=1
	 * @param layers The Identifying names of the Layers to display. These Layers are assumed to be installed and ready on the server. The layers are drawn from the order in which they are in the list (first bottommost)
	 * @param srs The Spatial Reference system to use
	 * @param bbox The bounding box to use for the map. The values are dependant on the spatial reference system in use, and may be rounded off
	 * @param width The width of the image, in pixels
	 * @param height The height of the image, in pixels
	 * @param featureCount The maximum number of features which can be returned by this call
	 * @param x A point of interest around which the request is centered - based on the returned bounding box (in transformed pixels) given to bbox.
	 * @return An array of Feature objects.
	 */	
	public Feature [] getFeatureInfo(String [] layers, String srs, double [] bbox, int width, int height, int featureCount, int x, int y) throws WMSException;
}

