/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; 
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *    
 */
package org.geotools.wms.gtserver;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;

import uk.ac.leeds.ccg.geotools.*;
import uk.ac.leeds.ccg.geotools.io.*;
import uk.ac.leeds.ccg.shapefile.*;

import org.geotools.feature.Feature;

import org.apache.log4j.Category;

import org.geotools.wms.*;

/**
 * @version $Id: GtWmsServer.java,v 1.2 2002/07/15 16:10:23 loxnard Exp $
 * @author Ray Gallagher
 */
public class GtWmsServer implements WMSServer {
    /**
     * The LayerEntry objects - one for each entry in
     * the layers.xml file.
     */
    LayerEntry [] layers;
    /**
     * The loaded Theme objects - loaded according to
     * the entries in the layers.xml file.
     */
    Hashtable themes;
    /** The Viewer object to use for drawing. */
    MiniViewer viewer;
    
    /**
     * Specialized version number for sending back capabilities
     * as a text string (not WMS compatible).
     */
    public static final String GEO_VERSION = "GT1.0";
    
    private static Category _log = Category.getInstance(GtWmsServer.class.getName());
    
    public GtWmsServer() {
        loadLayers();
    }
    
    /**
     * Sets up a Viewer object with the specified layers.
     */
    private void setUpViewer(String [] layers, int width, int height, Color bgcolor) {
        // Create the viewer
        if (bgcolor == null)
            bgcolor = Color.white;
        viewer = new MiniViewer(width, height, bgcolor);
        // Add the themes to the viewer
        for (int i = 0; i < layers.length; i++)
            viewer.addTheme((Theme) themes.get(layers[i]));
    }
    
    /**
     * Loads the layers from layers.xml. Currently supports the
     * following data sources:
     * Shapefile (uk.ac.leeds.ccg.geotools.io.ShapefileReader).
     */
    private void loadLayers() {
        LayerReader reader = new LayerReader();
        themes = new Hashtable();
        
        try {
            layers = reader.read(this.getClass().getResourceAsStream("layers.xml"));
            // For each one, create and load a theme
            for (int i = 0; i < layers.length; i++) {
                System.out.println("Layer : " + layers[i].id);
                Theme t = null;
                // Get the type of datasource
                if (layers[i].datasource.equalsIgnoreCase("Shapefile")) {
                    // Create shapefile, using the parameters given
                    ShapefileReader sfr = new ShapefileReader((new File(layers[i].properties.getProperty("filename"))).toURL());
                    t = sfr.getTheme();
                }
                
                // Add theme to the viewer
                if (t != null)
                    themes.put(layers[i].id, t);
            }
        }
        catch (Exception exp) {
            _log.info("Exception loading layers " + exp.getClass().getName() + " : " + exp.getMessage());
        }
    }
    
    /**
     * Gets a map image.
     * @param layers The identifying names of the Layers to display.
     *        These Layers are assumed to be installed and ready on the server.
     * @param styles The styles to use for each Layer - one for one.
     * @param srs The Spatial Reference system to use.
     * @param bbox The bounding box to use for the map. The values are
     *        dependant on the spatial reference system in use and may be
     *        rounded off.
     * @param width The width of the image, in pixels.
     * @param height The height of the image, in pixels.
     * @param transparent Whether the background of the map is transparent.
     * @param bgcolor The background color of the map.
     * @return A java.awt.Image object of the drawn map.
     */
    public BufferedImage getMap(String [] layers, String [] styles, String srs,
                                double [] bbox, int width, int height, boolean transparent,
                                Color bgcolor) throws WMSException {
        System.out.println("layers : ");
        for (int i = 0; i < layers.length; i++)
            System.out.println(layers[i]);
        System.out.println("available : ");
        Iterator it = themes.keySet().iterator();
        while (it.hasNext())
            System.out.println(it.next().toString());
        // Make sure the requested layers exist on this server
        for (int i = 0; i < layers.length; i++)
            if (themes.get(layers[i]) == null)
                throw new WMSException(WMSException.WMSCODE_LAYERNOTDEFINED,
                                       "The Layer '" + layers[i] + 
                                       "' does not exist on this server");
        
        // Check the SRS
        if (!srs.equalsIgnoreCase("EPSG:4326"))
            throw new WMSException(WMSException.WMSCODE_INVALIDSRS,
                                   "This server only supports EPSG:4326");
        
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            
            setUpViewer(layers, width, height, bgcolor);
            
            viewer.setExtent(new GeoRectangle(bbox[0], bbox[1], bbox[2] - bbox[0],
                                              bbox[3] - bbox[1]));
            
            viewer.paintThemes(image.getGraphics());
            
            return image;
        }
        catch(Exception exp) {
            throw new WMSException(null, "Internal error : " + exp.getMessage());
        }
    }
    
    /**
     * Gets the capabilities of this server as an XML-formatted string.
     * @param version The requested version of the Capabilities XML - used for
     *        version Negotiation. This verion of the Geotools server allows a
     *        version of its own "GT 1.0", which sends back the capabilities
     *        string as text, not xml (for use with non XML-happy scripting
     *        languages like php).
     * @return The capabilites of this server. The string must conform to the
     *         OGC WMS spec at
     *         http://www.digitalearth.gov/wmt/xml/capabilities_1_1_1.dtd
     */
    public String getCapabilities(String version) throws WMSException {
        try {
            if (version.equalsIgnoreCase(GEO_VERSION))
                return getCapabilitiesAsText();
            
            InputStream is = this.getClass().getResourceAsStream("capabilities.xml");
            StringBuffer sb = new StringBuffer();
            int length = 0;
            byte [] b = new byte[100];
            while ((length = is.read(b)) != -1)
                sb.append(new String(b, 0, length));
            return sb.toString();
        }
        catch(Exception exp) {
            throw new WMSException(null, "Internal error : " + exp.getMessage());
        }
    }
    
    /**
     * This method is a slight hack. It's for returning a version of the
     * capabilities xml, but in plain text, to allow for non-xml happy
     * scripting languages like php.
     * @return A properties file containing the installed layers.
     */
    private String getCapabilitiesAsText() {
        System.out.println("Sending Capabilities as plaintext");
        String cap = "";
        for (int i = 0; i < layers.length; i++)
            cap += layers[i].id + "=" + layers[i].description + "\n";
        return cap;
    }
    
    /**
     * Gets the Feature info for given Layers. The first 5 parameters are the
     * same as those in the call to getMap. Feature Info can only be returned
     * for those layers for which the attribute queryable = 1.
     * @param layers The Identifying names of the Layers to display. These
     *        Layers are assumed to be installed and ready on the server.
     * @param srs The Spatial Reference system to use.
     * @param bbox The bounding box to use for the map. The values are
     *        dependant on the spatial reference system in use, and may be
     *        rounded off.
     * @param width The width of the image, in pixels.
     * @param height The height of the image, in pixels.
     * @param featureCount The maximum number of features which can be
     *        returned by this call.
     * @param x A point of interest around which the request is centered
     *        - based on the returned bounding box (in transformed pixels)
     *        given to bbox.
     * @return An array of Feature objects.
     */
    public Feature [] getFeatureInfo(String [] layers, String srs, double [] bbox, int width,
                                     int height, int featureCount, int x, int y)
                                     throws WMSException {
        throw new WMSException(null, "getFeatureInfo not supported");
    }
    
    
}


