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

import org.geotools.wms.gtserver.LayerEntry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

import java.awt.Color;
import java.awt.image.BufferedImage;

import java.io.*;

import java.net.*;

import java.util.*;
import java.util.logging.Logger;

import org.geotools.data.DataSource;
import org.geotools.data.DataSourceFactorySpi;
import org.geotools.data.DataSourceFinder;
import org.geotools.data.MemoryDataSource;
import org.geotools.data.postgis.*;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollectionDefault;

import org.geotools.filter.AbstractFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;

import org.geotools.map.*;

import org.geotools.renderer.*;
import org.geotools.renderer.lite.LiteRenderer;
import org.geotools.shapefile.ShapefileDataSource;



import org.geotools.styling.*;

import org.geotools.wms.*;


public class GtWmsServer implements WMSServer {
    //org.geotools.map.Map map;
    public static final String LAYERS_PROPERTY = "layersxml";
    private static final Logger LOGGER = Logger.getLogger(
    "org.geotools.wmsserver");
    
    /** The LayerEntry objects - one for each entry in the layers.xml file */
    HashMap layerEntries = new HashMap();
    HashMap features = new HashMap();
    HashMap styles = new HashMap();
    URL base;
    //    Java2DRenderer renderer = new Java2DRenderer();
    Renderer renderer = new LiteRenderer();
    
    public GtWmsServer() {
        //		loadLayers();
    }
    
    /** Loads the layers from layers.xml. Currently support the following data sources:
     *         Shapefile (uk.ac.leeds.ccg.geotools.io.ShapefileReader).
     */
    private void loadLayers(String filename) {
        LayerReader reader = new LayerReader();
        Iterator formats = DataSourceFinder.getAvailableDataSources();
        LOGGER.info("Supported data sources");
        while(formats.hasNext()){
            LOGGER.info(((DataSourceFactorySpi)formats.next()).getDescription());
        }
        try {
            if (base != null) {
                URL url = new URL(base, filename);
                LOGGER.fine("loading " + url.toString());
                layerEntries = reader.read(url.openStream());
            } else {
                LOGGER.fine("loading without base " +
                new File(filename).toURL());
                layerEntries = reader.read((new File(filename)).toURL()
                .openStream());
            }
            
            // For each one, create and load a theme
            Iterator loop = layerEntries.keySet().iterator();
            
            while (loop.hasNext()) {
                LayerEntry entry = (LayerEntry) layerEntries.get(
                (String) loop.next());
                LOGGER.fine("Layer : " + entry.id);
                LOGGER.fine("pre mod url is " + entry.properties.getProperty("url"));
                URL home;
                if(base != null){
                    home = base;
                }
                else{
                    home = new File(".").toURL();
                }
                URL url;
                try{
                    url = new URL(entry.properties.getProperty(
                    "url"));
                }
                catch(MalformedURLException mue){
                    url = new URL(home,entry.properties.getProperty("url"));
                    entry.properties.setProperty("url", url.toExternalForm());
                }
                LOGGER.fine("after mod url is " + url);
                
                HashMap props = new HashMap(entry.properties);
                DataSource ds = DataSourceFinder.getDataSource(props);
                LOGGER.fine("Loading layer with " + ds);
                MemoryDataSource cache = new MemoryDataSource();
                
                
                FeatureCollection temp = ds.getFeatures(null);
                Feature[] list = temp.getFeatures();
                LOGGER.info("Caching " + list.length + " features for region ");
                
                for (int i = 0; i < list.length; i++) {
                    cache.addFeature(list[i]);
                }
                
                Style style = new BasicPolygonStyle(); //bad
                Envelope bbox = ds.getBbox(false);
                entry.bbox = new double[4];
                entry.bbox[0] = bbox.getMinX();
                entry.bbox[1] = bbox.getMinY();
                entry.bbox[2] = bbox.getMaxX();
                entry.bbox[3] = bbox.getMaxY();
                features.put(entry.id, cache);
                styles.put(entry.id, style);
                
                // Get the type of datasource
               /* if (entry.datasource.equalsIgnoreCase("Shapefile")) {
                    File file = new File(entry.properties.getProperty(
                                                 "filename"));
                    URL url;
                
                    if (base != null) {
                        url = new URL(base, file.toString());
                    } else {
                        url = file.toURL();
                    }
                
                    ShapefileDataSource sds = new ShapefileDataSource(url);
                    Envelope bbox = sds.getBbox(false);
                    entry.bbox = new double[4];
                    entry.bbox[0] = bbox.getMinX();
                    entry.bbox[1] = bbox.getMinY();
                    entry.bbox[2] = bbox.getMaxX();
                    entry.bbox[3] = bbox.getMaxY();
                
                    MemoryDataSource cache = new MemoryDataSource();
                    GeometryFilter filter = FilterFactory.createFilterFactory()
                                                         .createGeometryFilter(AbstractFilter.GEOMETRY_BBOX);
                    filter.addLeftGeometry(FilterFactory.createFilterFactory()
                                                        .createBBoxExpression(bbox));
                
                    FeatureCollection temp = sds.getFeatures(filter);
                    Feature[] list = temp.getFeatures();
                    LOGGER.info("Caching " + list.length +
                                " features for region " + bbox.getMinX());
                
                    for (int i = 0; i < list.length; i++) {
                        cache.addFeature(list[i]);
                    }
                
                    Style style = new BasicPolygonStyle(); //bad
                
                    features.put(entry.id, cache);
                    styles.put(entry.id, style);
                }
                
                if (entry.datasource.equalsIgnoreCase("PostGIS")) {
                    LOGGER.fine("pulling proeprties");
                
                    String host = entry.properties.getProperty("host");
                    String user = entry.properties.getProperty("user");
                    String passwd = entry.properties.getProperty("passwd");
                    String port = entry.properties.getProperty("port");
                    String database = entry.properties.getProperty("database");
                    String table = entry.properties.getProperty("table");
                    LOGGER.fine(host + " " + user + " " + passwd + " " +
                                port + " " + database + " " + table);
                
                    PostgisConnectionFactory db =
                        new PostgisConnectionFactory(host, port, database);
                    LOGGER.fine("created new db connection");
                    db.setLogin(user, passwd);
                    LOGGER.fine("set the login");
                
                    PostgisDataSource ds = new PostgisDataSource(db.getConnection(), table);
                    Envelope bbox = ds.getBbox(false);
                    entry.bbox = new double[4];
                    entry.bbox[0] = bbox.getMinX();
                    entry.bbox[1] = bbox.getMinY();
                    entry.bbox[2] = bbox.getMaxX();
                    entry.bbox[3] = bbox.getMaxY();
                
                    Style style = new BasicPolygonStyle(); //bad
                
                    features.put(entry.id, ds);
                    styles.put(entry.id, style);
                }*/
            }
        } catch (Exception exp) {
            LOGGER.severe("Exception loading layers " +
            exp.getClass().getName() + " : " +
            exp.getMessage());
        }
    }
    
    /** Initialize this server
     */
    public void init(Properties properties) throws WMSException {
        // Load the layers from xml on the given path
        String filename = properties.getProperty(LAYERS_PROPERTY);
        LOGGER.fine("Loading layers.xml from : " + filename);
        
        String home = properties.getProperty("base.url");
        
        try {
            if (home != null) {
                base = new File(home).toURL();
                LOGGER.fine("base set to " + base);
            }
        } catch (MalformedURLException mue) {
            throw new WMSException("Initialization error", mue.toString());
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
    public BufferedImage getMap(String[] layers, String[] styleNames, String srs,
    double[] bbox, int width, int height,
    boolean transparent, Color bgcolor)
    throws WMSException {
        LOGGER.fine("layers : ");
        
        for (int i = 0; i < layers.length; i++)
            LOGGER.fine(layers[i]);
        
        LOGGER.fine("available : ");
        
        // Make sure the requested layers exist on this server
        for (int i = 0; i < layers.length; i++) {
            if (features.get(layers[i]) == null) {
                throw new WMSException(WMSException.WMSCODE_LAYERNOTDEFINED,
                "The Layer '" + layers[i] +
                "' does not exist on this server");
            }
        }
        
        // Check the SRS
        //if (!srs.equalsIgnoreCase("EPSG:4326"))
        //    throw new WMSException(WMSException.WMSCODE_INVALIDSRS, "This server only supports EPSG:4326");
        try {
            LOGGER.fine("setting up map");
            
            org.geotools.map.Map map = new DefaultMap();
            
            for (int i = 0; i < layers.length; i++) {
                Style[] layerstyle = findStyles(layers, styleNames, i);
                
                LOGGER.fine("style object is a " + layerstyle[0]);
                
                DataSource ds = (DataSource) features.get(layers[i]);
                FeatureCollectionDefault fc = new FeatureCollectionDefault(ds);
                map.addFeatureTable(fc, layerstyle[0]);
            }
            
            LOGGER.fine("map setup");
            
            BufferedImage image = new BufferedImage(width, height,
            BufferedImage.TYPE_INT_RGB);
            Envelope env = new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
            LOGGER.fine("setting up renderer");
            
            java.awt.Graphics g = image.getGraphics();
            g.setColor(bgcolor);
            
            if (!transparent) {
                g.fillRect(0, 0, width, height);
            }
            
            renderer.setOutput(image.getGraphics(),
            new java.awt.Rectangle(width, height));
            LOGGER.fine("calling renderer");
            Date start = new Date();
            map.render(renderer, env);
            Date end = new Date();
            LOGGER.fine("returning image after render time of " + (end.getTime()-start.getTime()));
            
            return image;
        } catch (Exception exp) {
            exp.printStackTrace();
            throw new WMSException(null, "Internal error : " +
            exp.getMessage());
        }
    }

    /**
     * @param layer
     * @param style
     * @param i
     * @return
     * @throws MalformedURLException
     * @throws WMSException
     */
    private Style[] findStyles(final String[] layer, final String[] style, final int i)
            throws MalformedURLException, WMSException {

        Style[] layerstyle = new Style[1];
        LayerEntry layerdefn = (LayerEntry) layerEntries.get(layer[i]);
        
        if ((style != null) && (style[i] != "")) {
            layerstyle = loadStyle(style, i, layerstyle, layerdefn);
        } else {
            if (layerdefn.defaultStyle != null) {
                layerstyle = useDefaultStyle(layer, i, layerstyle, layerdefn);
            } else {
                layerstyle[0] = (org.geotools.styling.Style) styles.get(
                layer[i]);
            }
        }

        return layerstyle;
    }

    /**
     * @param layer
     * @param i
     * @param layerstyle
     * @param layerdefn
     * @return
     * @throws MalformedURLException
     */
    private Style[] useDefaultStyle(final String[] layer, final int i, Style[] layerstyle, final LayerEntry layerdefn)
            throws MalformedURLException {

        String sldpath = (String) layerdefn.styles.get(
        layerdefn.defaultStyle);
        LOGGER.fine("looking for default:" + sldpath);
        layerstyle[0] =  (Style)styles.get(sldpath);
        if(sldpath!=null&&layerstyle[0] == null){
            File file = new File(sldpath);
            URL url;
            
            if (base != null) {
                url = new URL(base, file.toString());
            } else {
                url = file.toURL();
            }
            
            //LOGGER.fine("loading sld from " + url);
            StyleFactory factory = StyleFactory.createStyleFactory();
            SLDStyle stylereader = new SLDStyle(factory, url);
            layerstyle = stylereader.readXML();
            styles.put(sldpath, layerstyle[0]);
            //LOGGER.fine("sld loaded");
        }else{
            layerstyle[0] = (org.geotools.styling.Style) styles.get(
            sldpath);
        }

        return layerstyle;
    }

    /**
     * @param style
     * @param i
     * @param layerstyle
     * @param layerdefn
     * @return
     * @throws MalformedURLException
     * @throws WMSException
     */
    private Style[] loadStyle(final String[] style, final int i, Style[] layerstyle, final LayerEntry layerdefn)
            throws MalformedURLException, WMSException {

        String sldpath = (String) layerdefn.styles.get(style[i]);
        LOGGER.finest("style != null, style[i] != null");
        if (sldpath == null) {
            throw new WMSException(WMSException.WMSCODE_STYLENOTDEFINED,
            "The Style '" + style[i] +
            "' does not exist for " +
            layerdefn.id);
        }
        layerstyle[0] =  (Style)styles.get(sldpath);
        if(layerstyle[0] == null){
            //LOGGER.fine("looking for " + sldpath);
            File file = new File(sldpath);
            URL url;
            
            if (base != null) {
                url = new URL(base, file.toString());
            } else {
                url = file.toURL();
            }
            
            LOGGER.fine("loading sld from " + url);
            
            StyleFactory factory = StyleFactory.createStyleFactory();
            SLDStyle stylereader = new SLDStyle(factory, url);
            layerstyle = stylereader.readXML();
            styles.put(sldpath, layerstyle[0]);
            LOGGER.fine("sld loaded");
            if(layerstyle[0].isDefault()){
                layerdefn.defaultStyle = sldpath;
            }
        }

        return layerstyle;
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
                cap.addLayer(layer.id, layer.description, layer.srs, layer.bbox);
                
                if (layer.styles != null) {
                    Iterator loop = layer.styles.keySet().iterator();
                    
                    while (loop.hasNext()) {
                        String styleid = (String) loop.next();
                        cap.addStyle(layer.id, styleid, styleid, null);
                    }
                }
            }
            
            cap.setSupportsGetFeatureInfo(true);
            
            // Send result back to server
            return cap;
        } catch (Exception exp) {
            throw new WMSException(null, "Internal error : " +
            exp.getMessage());
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
    public Feature[] getFeatureInfo(String[] layer, String srs, double[] bbox,
    int width, int height, int featureCount,
    int x, int y) throws WMSException {
        // throw new WMSException(null, "getFeatureInfo not supported");
        try {
            LOGGER.fine("setting up map");
            
            org.geotools.map.Map map = new DefaultMap();
            
            for (int i = 0; i < layer.length; i++) {
                DataSource ds = (DataSource) features.get(layer[i]);
                
                LOGGER.fine("map setup");
                
                BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
                Envelope env = new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
                LOGGER.fine("setting up renderer");
                
                Java2DRenderer renderer = new Java2DRenderer();
                
                renderer.setOutput(image.getGraphics(),
                new java.awt.Rectangle(width, height));
                LOGGER.fine("inverting coordinate");
                
                Coordinate c = renderer.pixelToWorld(x, y, env);
                
                FilterFactory filterFac = FilterFactory.createFilterFactory();
                GeometryFilter filter = filterFac.createGeometryFilter(
                AbstractFilter.GEOMETRY_WITHIN);
                GeometryFactory geomFac = new GeometryFactory();
                
                filter.addLeftGeometry(filterFac.createLiteralExpression(
                geomFac.createPoint(c)));
                
                FeatureCollection fc = ds.getFeatures(filter);
                Feature[] features = fc.getFeatures();
                
                return features;
            }
            
            return null;
        } catch (Exception exp) {
            exp.printStackTrace();
            throw new WMSException(null, "Internal error : " +
            exp.getMessage());
        }
    }
}
