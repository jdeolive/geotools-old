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

import com.vividsolutions.jts.geom.Coordinate;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;

import org.geotools.shapefile.*;
import org.geotools.shapefile.DbaseFileReader;
import org.geotools.data.postgis.*;
import org.geotools.map.*;
import org.geotools.renderer.*;
import org.geotools.styling.*;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollectionDefault;
import org.geotools.feature.FeatureCollection;
import org.geotools.data.DataSource;
import org.geotools.data.MemoryDataSource;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;

import org.geotools.wms.*;

public class GtWmsServer implements WMSServer {
    /** The LayerEntry objects - one for each entry in the layers.xml file */
    HashMap layerEntries = new HashMap();
    HashMap features = new HashMap();
    HashMap styles = new HashMap();
    
    URL base;
    
    //org.geotools.map.Map map;
    
    public static final String LAYERS_PROPERTY = "layersxml";
    
    private static final Logger LOGGER = Logger.getLogger(
    "org.geotools.wmsserver");
    
    public GtWmsServer() {
        //		loadLayers();
    }
    
    
    
    /** Loads the layers from layers.xml. Currently support the following data sources:
     * 	Shapefile (uk.ac.leeds.ccg.geotools.io.ShapefileReader).
     */
    private void loadLayers(String filename) {
        LayerReader reader = new LayerReader();
        
        try {
            
            if(base != null){
                URL url = new URL(base,filename);
                System.out.println("loading " + url.toString());
                layerEntries = reader.read(url.openStream());
            }
            else {
                System.out.println("loading without base " + new File(filename).toURL());
                layerEntries = reader.read((new File(filename)).toURL().openStream());
            }
            // For each one, create and load a theme
            Iterator loop = layerEntries.keySet().iterator();
            while(loop.hasNext()){
                LayerEntry entry = (LayerEntry)layerEntries.get((String) loop.next());
                System.out.println("Layer : "+entry.id);
                
                // Get the type of datasource
                if (entry.datasource.equalsIgnoreCase("Shapefile")) {
                    File file = new File(entry.properties.getProperty("filename"));
                    URL url;
                    if(base != null){
                        url = new URL(base,file.toString());
                    }
                    else {
                        url = file.toURL();
                    }
                    ShapefileDataSource sds = new ShapefileDataSource(url);
                    Envelope bbox = sds.getBbox(false);
                    
                    MemoryDataSource cache = new MemoryDataSource();
                    GeometryFilter filter = FilterFactory.createFilterFactory().createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
                    filter.addLeftGeometry(FilterFactory.createFilterFactory().createBBoxExpression(bbox));
                    FeatureCollection temp = sds.getFeatures(filter);
                    Feature[] list = temp.getFeatures();
                    LOGGER.info("Caching " +list.length + " features for region " + bbox.getMinX() );
                    for(int i = 0; i < list.length; i++){
                        cache.addFeature(list[i]);
                    }
                    
                    Style style = new BasicPolygonStyle();//bad
                    
                    features.put(entry.id,cache);
                    styles.put(entry.id,style);
                }
                
                
                
                if (entry.datasource.equalsIgnoreCase("PostGIS")) {
                    System.out.println("pulling proeprties");
                    String host = entry.properties.getProperty("host");
                    String user = entry.properties.getProperty("user");
                    String passwd = entry.properties.getProperty("passwd");
                    String port = entry.properties.getProperty("port");
                    String database = entry.properties.getProperty("database");
                    String table = entry.properties.getProperty("table");
                    System.out.println(host+" "+user+" "+passwd+" "+port+" "+database+" "+table);
                    
                    PostgisConnection db = new PostgisConnection(host,port,database);
                    LOGGER.fine("created new db connection");
                    db.setLogin(user,passwd);
                    LOGGER.fine("set the login");
                    PostgisDataSource ds = new PostgisDataSource(db, table);
                    
                    
                    Style style = new BasicPolygonStyle();//bad
                    
                    features.put(entry.id,ds);
                    styles.put(entry.id,style);
                }
                
            }
        }
        catch (Exception exp) {
            System.out.println("Exception loading layers "+exp.getClass().getName()+" : "+exp.getMessage());
        }
    }
    
    /** Initialize this server
     */
    public void init(Properties properties) throws WMSException {
        // Load the layers from xml on the given path
        String filename = properties.getProperty(LAYERS_PROPERTY);
        System.out.println("Loading layers.xml from : "+filename);
        String home = properties.getProperty("base.url");
        try{
            if(home != null){
                base = new File(home).toURL();
                System.out.println("base set to " + base);
            }
        }
        catch(MalformedURLException mue){
            throw new WMSException("Initialization error",mue.toString());
        }
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
    public BufferedImage getMap(String [] layer, String [] style, String srs, double [] bbox, int width, int height, boolean transparent, Color bgcolor) throws WMSException {
        System.out.println("layers : ");
        for (int i=0;i<layer.length;i++)
            System.out.println(layer[i]);
        System.out.println("available : ");
        
        // Make sure the requested layers exist on this server
        for (int i=0;i<layer.length;i++){
            if (features.get(layer[i])==null)
                throw new WMSException(WMSException.WMSCODE_LAYERNOTDEFINED, "The Layer '"+layer[i]+"' does not exist on this server");
        }
        // Check the SRS
        //if (!srs.equalsIgnoreCase("EPSG:4326"))
        //    throw new WMSException(WMSException.WMSCODE_INVALIDSRS, "This server only supports EPSG:4326");
        
        
        try {
            System.out.println("setting up map");
            org.geotools.map.Map map = new DefaultMap();
            for(int i = 0; i < layer.length; i++){
                Style layerstyle;
                LayerEntry layerdefn = (LayerEntry) layerEntries.get(layer[i]);
                if (style != null && style[i] != ""){
                    
                    String sldpath = (String)layerdefn.styles.get(style[i]);
                    if (sldpath==null)
                            throw new WMSException(WMSException.WMSCODE_STYLENOTDEFINED, "The Style '"+style[i]+"' does not exist for " + layerdefn.id);

                    //System.out.println("looking for " + sldpath);
                    File file = new File(sldpath);
                    URL url;
                    if(base != null){
                        url = new URL(base,file.toString());
                    }
                    else {
                        url = file.toURL();
                    }
                    System.out.println("loading sld from " + url);
                    StyleFactory factory = StyleFactory.createStyleFactory();
                    SLDStyle stylereader = new SLDStyle(factory,url);
                    layerstyle = stylereader.readXML();
                    
                    System.out.println("sld loaded");
                }
                else{
                    if(layerdefn.defaultStyle != null){
                        String sldpath = (String)layerdefn.styles.get(layerdefn.defaultStyle);
                        System.out.println("looking for default:" + sldpath);
                        
                        File file = new File(sldpath);
                        URL url;
                        if(base != null){
                            url = new URL(base,file.toString());
                        }
                        else {
                            url = file.toURL();
                        }
                        //System.out.println("loading sld from " + url);
                        StyleFactory factory = StyleFactory.createStyleFactory();
                        SLDStyle stylereader = new SLDStyle(factory,url);
                        layerstyle = stylereader.readXML();

                        //System.out.println("sld loaded");
                    }
                    else{
                        layerstyle = (org.geotools.styling.Style)styles.get(layer[i]);
                    }
                }
                System.out.println("style object is a " + layerstyle);
                DataSource ds = (DataSource)features.get(layer[i]);
                FeatureCollectionDefault fc = new FeatureCollectionDefault(ds);
                map.addFeatureTable(fc,layerstyle);
            }
            System.out.println("map setup");
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Envelope env = new Envelope(bbox[0],bbox[2],bbox[1],bbox[3]);
            System.out.println("setting up renderer");
            Java2DRenderer renderer = new Java2DRenderer();
            java.awt.Graphics g = image.getGraphics();
            g.setColor(bgcolor);
            if(!transparent){
                g.fillRect(0,0,width,height);
            }
            renderer.setOutput(image.getGraphics(), new java.awt.Rectangle(width,height));
            System.out.println("calling renderer");
            map.render(renderer, env);
            System.out.println("returning image");
            return image;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw new WMSException(null, "Internal error : "+exp.getMessage());
        }
    }
    
    /** Gets the capabilites of this server as an XML-formatted string.
     * @param version The requested version of the Capabilities XML - used for version Negotiation. This verion of the Geotools server allows a version of it's own "GT 1.0", which sends back the capabilities string as text, not xml (for use with non XML-happy scripting languages like php).
     * @return The capabilites of this server. The string must conform to the OGC WMS spec at http://www.digitalearth.gov/wmt/xml/capabilities_1_1_1.dtd
     */
    public Capabilities getCapabilities() throws WMSException {
        try {
            Capabilities cap = new Capabilities();
            
            // Add the layers to the capabilities object
            Iterator layers = layerEntries.keySet().iterator();
            while (layers.hasNext()) {
                LayerEntry layer = (LayerEntry) layerEntries.get(layers.next());
                cap.addLayer(layer.id, layer.description, layer.srs, new double[] {-120, 36, -70, 42});
                if (layer.styles != null){
                    Iterator loop = layer.styles.keySet().iterator();
                    while (loop.hasNext()){
                        String styleid = (String)loop.next();
                        cap.addStyle(layer.id, styleid, styleid, null);
                    }
                }
            }
            cap.setSupportsGetFeatureInfo(true);
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
    public Feature [] getFeatureInfo(String [] layer, String srs, double [] bbox, int width, int height, int featureCount, int x, int y) throws WMSException {
        // throw new WMSException(null, "getFeatureInfo not supported");
        try {
            System.out.println("setting up map");
            org.geotools.map.Map map = new DefaultMap();
            for(int i = 0; i < layer.length; i++){
                
                
                DataSource ds = (DataSource)features.get(layer[i]);
                
                
                System.out.println("map setup");
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Envelope env = new Envelope(bbox[0],bbox[2],bbox[1],bbox[3]);
                System.out.println("setting up renderer");
                Java2DRenderer renderer = new Java2DRenderer();
                
                renderer.setOutput(image.getGraphics(), new java.awt.Rectangle(width,height));
                System.out.println("inverting coordinate");
                Coordinate c = renderer.pixelToWorld(x,y,env);
               
                FilterFactory filterFac = FilterFactory.createFilterFactory();
                GeometryFilter filter = filterFac.createGeometryFilter(AbstractFilter.GEOMETRY_WITHIN);
                GeometryFactory geomFac = new GeometryFactory();

                filter.addLeftGeometry(filterFac.createLiteralExpression(geomFac.createPoint(c)));
                FeatureCollection fc = ds.getFeatures(filter);
                Feature[] features = fc.getFeatures();
                return features;
            }
            
            return null;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw new WMSException(null, "Internal error : "+exp.getMessage());
        }
        
    }
    
    
}


