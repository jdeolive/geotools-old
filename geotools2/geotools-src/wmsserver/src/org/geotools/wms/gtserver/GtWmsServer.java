/**
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

import org.geotools.shapefile.*;
import org.geotools.map.*;
import org.geotools.renderer.*;
import org.geotools.styling.*;

import com.vividsolutions.jts.geom.Envelope;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollectionDefault;
import org.geotools.feature.FeatureCollection;

import org.apache.log4j.Category;

import org.geotools.wms.*;

public class GtWmsServer implements WMSServer {
    /** The LayerEntry objects - one for each entry in the layers.xml file */
    LayerEntry [] layers;
    
    HashMap features = new HashMap();
    HashMap styles = new HashMap();
    
    org.geotools.map.Map map;
    
    public static final String LAYERS_PROPERTY = "layersxml";
    
    private static Category _log = Category.getInstance(GtWmsServer.class.getName());
    
    public GtWmsServer() {
        //		loadLayers();
    }
    
    
    
    /** Loads the layers from layers.xml. Currently support the following data sources:
     * 	Shapefile (uk.ac.leeds.ccg.geotools.io.ShapefileReader).
     */
    private void loadLayers(String filename) {
        LayerReader reader = new LayerReader();
        
        try {
            System.out.println("loading " + new File(filename).toURL().toString());
            layers = reader.read((new File(filename)).toURL().openStream());
            // For each one, create and load a theme
            
            for (int i=0;i<layers.length;i++) {
                System.out.println("Layer : "+layers[i].id);
                
                // Get the type of datasource
                if (layers[i].datasource.equalsIgnoreCase("Shapefile")) {
                    Shapefile shapes = new Shapefile((new File(layers[i].properties.getProperty("filename"))).toURL());
                    ShapefileDataSource sds = new ShapefileDataSource(shapes);
                    FeatureCollectionDefault fc = new FeatureCollectionDefault(sds);
                    
                    Style style = new BasicPolygonStyle();//bad
                    
                    features.put(layers[i].id,fc);
                    styles.put(layers[i].id,style); 
                }     
            }
        }
        catch (Exception exp) {
            _log.info("Exception loading layers "+exp.getClass().getName()+" : "+exp.getMessage());
        }
    }
    
    /** Initialize this server
     */
    public void init(Properties properties) throws WMSException {
        // Load the layers from xml on the given path
        String filename = properties.getProperty(LAYERS_PROPERTY);
        System.out.println("Loading layers.xml from : "+filename);
        loadLayers(filename);
    }
    
    /** Gets a map image
     * @param layers The Identifying names of the Layers to display. These Layers are assumed to be installed and ready on the server.
     * @param styles The styles to use for each Layer - one for one
     * @param srs The Spatial Reference system to use
     * @param bbox The bounding box to use for the map. The values are dependant on the spatial reference system in use, and may be rounded off
     * @param width The width of the image, in pixels
     * @param height The height of the image, in pixels
     * @param transparent Whether the background of the map is transparent
     * @param bgcolor The background color of the map
     * @return A java.awt.Image object of the drawn map.
     */
    public BufferedImage getMap(String [] layers, String [] style, String srs, double [] bbox, int width, int height, boolean transparent, Color bgcolor) throws WMSException {
        System.out.println("layers : ");
        for (int i=0;i<layers.length;i++)
            System.out.println(layers[i]);
        System.out.println("available : ");
   
        // Make sure the requested layers exist on this server
        for (int i=0;i<layers.length;i++){
            if (features.get(layers[i])==null)
                throw new WMSException(WMSException.WMSCODE_LAYERNOTDEFINED, "The Layer '"+layers[i]+"' does not exist on this server");
        }
        // Check the SRS
        if (!srs.equalsIgnoreCase("EPSG:4326"))
            throw new WMSException(WMSException.WMSCODE_INVALIDSRS, "This server only supports EPSG:4326");
        
        
       // try {
            System.out.println("setting up map");
            map = new DefaultMap();
            for(int i = 0; i < layers.length; i++){
                System.out.println("style object is a " + styles.get(layers[i]));
                map.addFeatureTable((FeatureCollection)features.get(layers[i]),(org.geotools.styling.Style)styles.get(layers[i]));
            }
            System.out.println("map setup");
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Envelope env = new Envelope(bbox[0],bbox[2],bbox[1],bbox[3]);
            System.out.println("setting up renderer");
            Java2DRenderer renderer = new Java2DRenderer();
            java.awt.Graphics g = image.getGraphics();
            g.setColor(Color.white);
            g.fillRect(0,0,width,height);
            renderer.setOutput(image.getGraphics(), new java.awt.Rectangle(width,height));
            System.out.println("calling renderer");
            map.render(renderer, env);
            System.out.println("returning image");
            return image;
       // }
       // catch(Exception exp) {
       //     throw new WMSException(null, "Internal error : "+exp.getMessage());
       // }
    }
    
    /** Gets the capabilites of this server as an XML-formatted string.
     * @param version The requested version of the Capabilities XML - used for version Negotiation. This verion of the Geotools server allows a version of it's own "GT 1.0", which sends back the capabilities string as text, not xml (for use with non XML-happy scripting languages like php).
     * @return The capabilites of this server. The string must conform to the OGC WMS spec at http://www.digitalearth.gov/wmt/xml/capabilities_1_1_1.dtd
     */
    public Capabilities getCapabilities() throws WMSException {
        try {
            Capabilities cap = new Capabilities();
            
            // Add the layers to the capabilities object
            for (int i=0;i<layers.length;i++) {
                cap.addLayer(layers[i].id, layers[i].description, "EPSG:4326", new double[] {-120, 40, -60, 60});
            }
            
            // Send result back to server
            return cap;
        }
        catch(Exception exp) {
            throw new WMSException(null, "Internal error : "+exp.getMessage());
        }
    }
    
    /** Gets the Feature info for given Layers. The first 5 parameters are the same as those in the call to getMap. Feature Info can only be returned for
     * those layers for which the attribute queryable=1
     * @param layers The Identifying names of the Layers to display. These Layers are assumed to be installed and ready on the server.
     * @param srs The Spatial Reference system to use
     * @param bbox The bounding box to use for the map. The values are dependant on the spatial reference system in use, and may be rounded off
     * @param width The width of the image, in pixels
     * @param height The height of the image, in pixels
     * @param featureCount The maximum number of features which can be returned by this call
     * @param x A point of interest around which the request is centered - based on the returned bounding box (in transformed pixels) given to bbox.
     * @return An array of Feature objects.
     */
    public Feature [] getFeatureInfo(String [] layers, String srs, double [] bbox, int width, int height, int featureCount, int x, int y) throws WMSException {
        throw new WMSException(null, "getFeatureInfo not supported");
    }
    
    
}


