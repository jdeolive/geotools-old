/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
 */
package org.geotools.wms.gtserver;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import org.geotools.data.DataSource;
import org.geotools.data.DataSourceFactorySpi;
import org.geotools.data.DataSourceFinder;
import org.geotools.data.MemoryDataSource;
import org.geotools.data.DataSourceMetaData;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.filter.AbstractFilter;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;
import org.geotools.map.*;
import org.geotools.renderer.*;
import org.geotools.renderer.lite.LiteRenderer;
import org.geotools.styling.*;
import org.geotools.wms.*;
import org.geotools.wms.gtserver.LayerEntry;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Logger;
import org.apache.commons.collections.LRUMap;
import org.geotools.data.Query;
import org.geotools.renderer.LegendImageGenerator;


public class GtWmsServer implements WMSServer {
    //org.geotools.map.Map map;
    public static final String LAYERS_PROPERTY = "layersxml";
    private static final Logger LOGGER = Logger.getLogger(
    "org.geotools.wmsserver");
    static StyleFactory factory = StyleFactory.createStyleFactory();
    /** The LayerEntry objects - one for each entry in the layers.xml file */
    HashMap layerEntries = new HashMap();
    HashMap features = new HashMap();
    java.util.Map styles;
    URL base;
    
    //    Java2DRenderer renderer = new Java2DRenderer();
    Renderer renderer = new LiteRenderer();
    
    public GtWmsServer() {
        //		loadLayers();
        LRUMap stylebase = new LRUMap(50);
        styles = Collections.synchronizedMap(stylebase);
    }
    
    /**
     * Loads the layers from layers.xml. Currently support the following data
     * sources: Shapefile (uk.ac.leeds.ccg.geotools.io.ShapefileReader).
     *
     * @param filename - the layer file
     */
    private void loadLayers(String filename) {
        Iterator formats = DataSourceFinder.getAvailableDataSources();
        LOGGER.info("Supported data sources");
        
        while (formats.hasNext()) {
            LOGGER.info(((DataSourceFactorySpi) formats.next()).getDescription());
        }
        
        try {
            LayerReader reader = new LayerReader();
            
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
                LayerEntry entry = (LayerEntry) layerEntries.get((String) loop.next());
                LOGGER.finer("Layer : " + entry.id);
                LOGGER.finer("pre mod url is " +
                entry.properties.getProperty("url"));
                
                URL home;
                
                if (base != null) {
                    home = base;
                } else {
                    home = new File(".").toURL();
                }
                
                URL url=null;
                
                try {
                    url = new URL(entry.properties.getProperty("url"));
                } catch (MalformedURLException mue) {
                    try{
                        url = new URL(home, entry.properties.getProperty("url"));
                        entry.properties.setProperty("url", url.toExternalForm());
                    }catch (MalformedURLException mfe){                        
                        //@HACK I have no idea what should happen here - Ask James?
                        LOGGER.warning("Layer from " + url + " not installed");
                        loop.remove();
                    
                        continue;
                    }
                }
                
                LOGGER.finer("after mod url is " + url);
                
                HashMap props = new HashMap(entry.properties);
                DataSource ds = DataSourceFinder.getDataSource(props);
                LOGGER.finer("Loading layer with " + ds);
                
                MemoryDataSource cache = new MemoryDataSource();
                
                Filter filter = null;
                FeatureCollection temp = ds.getFeatures(filter);
                
                if (temp == null) {
                    LOGGER.warning("Layer from " + url + " not installed");
                    loop.remove();
                    
                    continue;
                }
                
                Feature[] list = (Feature[])temp.toArray(new Feature[0]);
                
                if (list.length == 0) {
                    LOGGER.warning("Layer from " + url +
                    " contained no features");
                    loop.remove();
                    
                    continue;
                }
                
                LOGGER.info("Caching " + list.length + " features for region ");
                
                for (int i = 0; i < list.length; i++) {
                    cache.addFeature(list[i]);
                }
                
//                Style style = new BasicPolygonStyle(); //bad
                DataSourceMetaData meta = ds.getMetaData();
                
                Envelope bbox = null;
                if (!meta.supportsGetBbox()){
                    LOGGER.warning("Unable to obtain bounds for " +url);
                    loop.remove();
                    continue;
                }
                bbox = ds.getBbox();
                entry.bbox = new double[4];
                entry.bbox[0] = bbox.getMinX();
                entry.bbox[1] = bbox.getMinY();
                entry.bbox[2] = bbox.getMaxX();
                entry.bbox[3] = bbox.getMaxY();
                features.put(entry.id, cache);
//                styles.put(entry.id, style);
            }
        } catch (Exception exp) {
            exp.printStackTrace();
            LOGGER.severe("Exception loading layers " +
            exp.getClass().getName() + " : " + exp.getMessage());
        }
    }
    
    /**
     * Initialize this server
     *
     * @param properties DOCUMENT ME!
     *
     * @throws WMSException DOCUMENT ME!
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
    
    /**
     * Gets a map image
     *
     * @param layers The Identifying names of the Layers to display. These
     *        Layers are assumed to be installed and ready on the server.
     * @param styleNames The styles to use for each Layer - one for one
     * @param srs The Spatial Reference system to use
     * @param bbox The bounding box to use for the map. The values are
     *        dependant on the spatial reference system in use, and may be
     *        rounded off
     * @param width The width of the image, in pixels
     * @param height The height of the image, in pixels
     * @param transparent Whether the background of the map is transparent
     * @param bgcolor The background color of the map
     *
     * @return A java.awt.Image object of the drawn map.
     *
     * @throws WMSException DOCUMENT ME!
     */
    public BufferedImage getMap(String[] layers, String[] styleNames,
    String srs, double[] bbox, int width, int height, boolean transparent,
    Color bgcolor) throws WMSException {
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
            Style[] layerstyle = null;
            for (int i = 0; i < layers.length; i++) {
                if(styleNames!=null&&styleNames[i]!=null){ 
                    layerstyle = findStyles(layers[i], styleNames[i]);
                }else{
                    layerstyle = findStyles(layers[i],"");
                }
                
                //LOGGER.fine("style object is a " + layerstyle[0]);
                
                DataSource ds = (DataSource) features.get(layers[i]);
                FeatureCollection fc = ds.getFeatures(Query.ALL);
                map.addFeatureTable(fc, layerstyle[0]);
            }
            
            LOGGER.fine("map setup");
            //Renderer renderer = new LiteRenderer();
            BufferedImage image = new BufferedImage(width, height,
            BufferedImage.TYPE_INT_RGB);
            Envelope env = new Envelope(bbox[0], bbox[2], bbox[1], bbox[3]);
            LOGGER.fine("setting up renderer");
            
            java.awt.Graphics g = image.getGraphics();
            g.setColor(bgcolor);
            
            if (!transparent) {
                g.fillRect(0, 0, width, height);
            }
            synchronized(renderer){
                renderer.setOutput(image.getGraphics(),
                new java.awt.Rectangle(width, height));
                LOGGER.fine("calling renderer");

                Date start = new Date();
                map.render(renderer, env);

                Date end = new Date();
                LOGGER.fine("returning image after render time of " +
                (end.getTime() - start.getTime()));
                //renderer = null;
            }
            map = null;
            return image;
        } catch (Exception exp) {
            exp.printStackTrace();
            throw new WMSException(null, "Internal error : " +
            exp.getMessage());
        }
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param layer - a list of layer names
     * @param style - a list of styles
     * 
     *
     * @return
     *
     * @throws MalformedURLException
     * @throws WMSException
     */
    private Style[] findStyles(final String[] layer, final String[] style) throws MalformedURLException, WMSException {
        ArrayList styleList = new ArrayList();
        Style[] layerstyle = null;
        for(int i=0;i<layer.length;i++){
            
            layerstyle = findStyles(layer[i],style[i]);
            
            for(int j=0;j<layerstyle.length;j++){
                styleList.add(layerstyle[j]);
            }    
        }
        return (Style[]) styleList.toArray(layerstyle);
    }
    
    private Style[] findStyles(final String layer, final String style) throws MalformedURLException, WMSException {
        Style[] layerstyle = null;
        LayerEntry layerdefn = (LayerEntry) layerEntries.get(layer);
        if ((style != null) && (style != "")) {
                layerstyle = loadStyle(style, layerdefn);
                
            } else {
                layerstyle = useDefaultStyle(layer, layerdefn);

            }
        return layerstyle;
    }
    
    public BufferedImage getLegend(String[] layers, String[] styleNames,
     int width, int height, boolean transparent,
    Color bgcolor, double scale) throws WMSException {
        BufferedImage image = null;
        try{
            Style[] reqStyles = findStyles(layers, styleNames);
        
        LegendImageGenerator lig = new LegendImageGenerator(reqStyles,width,height); 
        lig.setScale(scale); 
        image = lig.getLegend(bgcolor);
        return image;
        }catch (MalformedURLException mfe){
            throw new WMSException("Problem in GetLegend",mfe.getMessage());
        }
    }
    /**
     * DOCUMENT ME!
     *
     * @param layer
     * @param i
     * @param layerstyle
     * @param layerdefn
     *
     * @return
     *
     * @throws MalformedURLException
     */
    private Style[] useDefaultStyle(final String layer, final LayerEntry layerdefn)
    throws MalformedURLException {
        Style[] layerstyle = new Style[1];
        LOGGER.fine("Default style "+layerdefn.defaultStyle);
        String sldpath = (String)layerdefn.styles.get(layerdefn.defaultStyle);
        LOGGER.fine("looking for default:" + sldpath);
        layerstyle[0] = (Style) styles.get(sldpath);
        LOGGER.fine("from the cache "+layerstyle[0]);
        if ((sldpath != null) && (layerstyle[0] == null)) {
            File file = new File(sldpath);
            URL url;
            
            if (base != null) {
                url = new URL(base, file.toString());
            } else {
                url = file.toURL();
            }
            LOGGER.fine("pulling default style from "+url.toString());
            //LOGGER.fine("loading sld from " + url);
            try{
                SLDStyle stylereader = new SLDStyle(factory, url);
                layerstyle = stylereader.readXML();
                styles.put(sldpath, layerstyle[0]);
            }catch (java.io.IOException fnfe){
                LOGGER.severe(fnfe.getMessage());
                throw new RuntimeException(fnfe);
            }
            
            //LOGGER.fine("sld loaded");
        } 
        
        return layerstyle;
    }
    
    /**
     * DOCUMENT ME!
     *
     * @param style
     * @param i
     * @param layerstyle
     * @param layerdefn
     *
     * @return
     *
     * @throws MalformedURLException
     * @throws WMSException
     */
    private Style[] loadStyle(final String style, final LayerEntry layerdefn)
    throws MalformedURLException, WMSException {
        String sldpath = (String) layerdefn.styles.get(style);
        LOGGER.fine("style != null "+(style != null));
        Style[] layerstyle= new Style[1];
        if (sldpath == null) {
            throw new WMSException(WMSException.WMSCODE_STYLENOTDEFINED,
            "The Style '" + style + "' does not exist for " +
            layerdefn.id);
        }
        
        layerstyle[0] = (Style) styles.get(sldpath);
        
        if (layerstyle[0] == null) {
            LOGGER.fine("looking for " + sldpath);
            File file = new File(sldpath);
            URL url;
            
            if (base != null) {
                url = new URL(base, file.toString());
            } else {
                url = file.toURL();
            }
            
            LOGGER.fine("loading sld from " + url);
            
            try{
                SLDStyle stylereader = new SLDStyle(factory, url);
                layerstyle = stylereader.readXML();
                if(layerstyle[0]!=null)styles.put(sldpath, layerstyle[0]);
            }catch (java.io.IOException ie){
                throw new RuntimeException(ie);
            }
            LOGGER.fine("sld loaded");
            
            if (layerstyle[0].isDefault()) {
                LOGGER.fine("Changeing defaultstyle from "+layerdefn.defaultStyle+ " to "+sldpath);
                layerdefn.defaultStyle = sldpath;
            }
        }
        
        return layerstyle;
    }
    
    /**
     * Gets the capabilites of this server as an XML-formatted string.
     *
     * @return The capabilites of this server. The string must conform to the
     *         OGC WMS spec at
     *         http://www.digitalearth.gov/wmt/xml/capabilities_1_1_1.dtd
     *
     * @throws WMSException DOCUMENT ME!
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
    
    /**
     * Gets the Feature info for given Layers. The first 5 parameters are the
     * same as those in the call to getMap. Feature Info can only be returned
     * for those layers for which the attribute queryable=1
     *
     * @param layer The Identifying names of the Layers to display. These
     *        Layers are assumed to be installed and ready on the server.
     * @param srs The Spatial Reference system to use
     * @param bbox The bounding box to use for the map. The values are
     *        dependant on the spatial reference system in use, and may be
     *        rounded off
     * @param width The width of the image, in pixels
     * @param height The height of the image, in pixels
     * @param featureCount The maximum number of features which can be returned
     *        by this call
     * @param x A point of interest around which the request is centered -
     *        based on the returned bounding box (in transformed pixels) given
     *        to bbox.
     * @param y DOCUMENT ME!
     *
     * @return An array of Feature objects.
     *
     * @throws WMSException DOCUMENT ME!
     */
    public Feature[] getFeatureInfo(String[] layer, String srs, double[] bbox,
    int width, int height, int featureCount, int x, int y)
    throws WMSException {
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
                
                //Java2DRenderer renderer = new Java2DRenderer();
                
                renderer.setOutput(image.getGraphics(),
                new java.awt.Rectangle(width, height));
                LOGGER.fine("inverting coordinate");
                
                Coordinate c = renderer.pixelToWorld(x, y, env);
                
                FilterFactory filterFac = FilterFactory.createFilterFactory();
                GeometryFilter filter = filterFac.createGeometryFilter(AbstractFilter.GEOMETRY_WITHIN);
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
