/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2004-2008, Open Source Geospatial Foundation (OSGeo)
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
 */
package org.geotools.map;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.data.DataUtilities;
import org.geotools.data.ows.Layer;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetFeatureInfoRequest;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.data.wms.response.GetFeatureInfoResponse;
import org.geotools.data.wms.response.GetMapResponse;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.ows.ServiceException;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.resources.coverage.FeatureUtilities;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.coverage.grid.Format;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.parameter.ParameterValue;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Wraps a WMS layer into a {@link MapLayer} for interactive rendering usage TODO: expose a
 * GetFeatureInfo that returns a feature collection TODO: expose the list of named styles and allow
 * choosing which style to use
 * 
 * @author Andrea Aime - OpenGeo
 */
public class WMSMapLayer extends DefaultMapLayer {
    /** The logger for the map module. */
    static public final Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.map");
    
    /**
     * The default raster style
     */
    static Style STYLE;

    static GridCoverageFactory gcf = new GridCoverageFactory();

    WMSCoverageReader reader;
    
    static {
        StyleFactory factory = CommonFactoryFinder.getStyleFactory(null);
        RasterSymbolizer symbolizer = factory.createRasterSymbolizer();

        Rule rule = factory.createRule();
        rule.symbolizers().add(symbolizer);

        FeatureTypeStyle type = factory.createFeatureTypeStyle();
        type.rules().add(rule);

        STYLE = factory.createStyle();
        STYLE.featureTypeStyles().add(type);
    }

    /**
     * Builds a new WMS alyer
     * 
     * @param wms
     * @param layer
     */
    public WMSMapLayer(WebMapServer wms, Layer layer) {
        super((SimpleFeatureSource) null, null, "");
        reader = new WMSCoverageReader(wms, layer);
        try {
            this.featureSource = DataUtilities.source(FeatureUtilities.wrapGridCoverageReader(
                    reader, null));
        } catch (Throwable t) {
            throw new RuntimeException("Unexpected exception occurred during map layer building", t);
        }

        this.style = STYLE;
    }

    public synchronized ReferencedEnvelope getBounds() {
        return reader.getBounds();
    }

    /**
     * Retrieves the feature info as text (assuming "text/plain" is a supported
     * feature info format)
     * @param pos
     * @return
     * @throws IOException
     */
    public String getFeatureInfoAsText(DirectPosition2D pos, int featureCount) throws IOException {
        BufferedReader br = null;
        try {
            InputStream is = reader.getFeatureInfo(pos, "text/plain", featureCount);
            br = new BufferedReader(new InputStreamReader(is));
            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();
        } catch(IOException e) {
            throw e;
        } catch (Throwable t) {
            throw (IOException) new IOException("Failed to grab feature info").initCause(t);
        } finally {
            if(br != null)
                br.close();
        }
    }
    
    /**
     * Retrieves the feature info as a generic input stream, it's the duty of the caller to
     * interpret the contents and ensure the stream is closed
     * feature info format)
     * @param pos
     * @param infoFormat The INFO_FORMAT parameter in the GetFeatureInfo request
     * @return
     * @throws IOException
     */
    public InputStream getFeatureInfo(DirectPosition2D pos, String infoFormat, int featureCount) throws IOException {
        return reader.getFeatureInfo(pos, infoFormat, featureCount);
    }   
    
    /**
     * Returns the {@link WebMapServer} used by this layer
     * @return
     */
    public WebMapServer getWebMapServer() {
        return reader.wms;
    }
    
    /**
     * Returns the WMS {@link Layer} used by this layer
     * @return
     */
    public Layer getWMSLayer() {
        return reader.layer;
    }
    
    /**
     * Returns the CRS used to make requests to the remote WMS
     * @return
     */
    public CoordinateReferenceSystem getCoordinateReferenceSystem() {
        return reader.getCrs();
    }
    
    /**
     * Returns last GetMap request performed by this layer
     * @return
     */
    public GetMapRequest getLastGetMap() {
        return reader.mapRequest;
    }
    
    /**
     * A grid coverage readers backing onto a WMS server by issuing GetMap 
     */
    static class WMSCoverageReader extends AbstractGridCoverage2DReader {
        /**
         * The WMS server
         */
        WebMapServer wms;

        /**
         * The layer
         */
        Layer layer;

        /**
         * The layer bounds
         */
        ReferencedEnvelope bounds;

        /**
         * The chosen SRS name
         */
        String srsName;

        /**
         * The format to use for requests
         */
        String format;

        /**
         * The last GetMap request
         */
        private GetMapRequest mapRequest;
        
        /**
         * The last GetMap response
         */
        GridCoverage2D grid;

        private ReferencedEnvelope requestedEnvelope;

        private int width;

        private int  height;

        /**
         * Builds a new WMS coverage reader
         * @param wms
         * @param layer
         */
        public WMSCoverageReader(WebMapServer wms, Layer layer) {
            this.wms = wms;
            this.layer = layer;

            // compute the reader bounds and crs
            Set<String> srs = layer.getSrs();
            srsName = srs.iterator().next();

            if (srs.contains("EPSG:4326")) {
                // really we should get the underlying
                // map pane CRS from viewport
                srsName = "EPSG:4326";
            } else {
                srsName = (String) srs.iterator().next();
            }
            CoordinateReferenceSystem crs = null;
            try {
                crs = CRS.decode(srsName);
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Bounds unavailable for layer" + layer);
            }
            GeneralEnvelope general = layer.getEnvelope(crs);
            this.bounds = new ReferencedEnvelope(general);
            this.originalEnvelope = new GeneralEnvelope(bounds);
            this.crs = crs;

            // best guess at the format with a preference for PNG (since it's normally transparent)
            List<String> formats = wms.getCapabilities().getRequest().getGetMap().getFormats();
            this.format = formats.iterator().next();
            for (String format : formats) {
                if ("image/png".equals(format) || "image/png24".equals(format)
                        || "png".equals(format) || "png24".equals(format))
                    this.format = format;
            }
        }

        /**
         * Issues GetFeatureInfo against a point using the params of the last GetMap request
         * @param pos
         * @return
         * @throws IOException
         */
        public InputStream getFeatureInfo(DirectPosition2D pos, String infoFormat, int featureCount) throws IOException {
            GetFeatureInfoRequest request = wms.createGetFeatureInfoRequest(mapRequest);
            request.setFeatureCount(1);
            request.setQueryLayers(Collections.singleton(layer));
            request.setInfoFormat(infoFormat);
            request.setFeatureCount(featureCount);
            try {
                AffineTransform tx = RendererUtilities.worldToScreenTransform(requestedEnvelope, new Rectangle(width, height));
                Point2D dest = new Point2D.Double();
                Point2D src = new Point2D.Double(pos.x, pos.y);
                tx.transform(src, dest);
                request.setQueryPoint((int) dest.getX(), (int) dest.getY());
            } catch (Exception e) {
                throw (IOException) new IOException("Failed to grab feature info").initCause(e);
            }

            try {
                GetFeatureInfoResponse response = wms.issueRequest(request);
                return response.getInputStream();
            } catch(IOException e) {
                throw e;
            } catch (Throwable t) {
                throw (IOException) new IOException("Failed to grab feature info").initCause(t);
            }
        }

        @Override
        public GridCoverage2D read(GeneralParameterValue[] parameters)
                throws IllegalArgumentException, IOException {
            // try to get request params from the request
            Envelope requestedEnvelope = null;
            int width = -1;
            int height = -1;
            if (parameters != null) {
                for (GeneralParameterValue param : parameters) {
                    final ReferenceIdentifier name = param.getDescriptor().getName();
                    if (name.equals(AbstractGridFormat.READ_GRIDGEOMETRY2D.getName())) {
                        final GridGeometry2D gg = (GridGeometry2D) ((ParameterValue) param)
                                .getValue();
                        requestedEnvelope = gg.getEnvelope();
                        // the range high value is the highest pixel included in the raster,
                        // the actual width and height is one more than that
                        width = gg.getGridRange().getHigh(0) + 1;
                        height = gg.getGridRange().getHigh(1) + 1;
                        break;
                    }
                }
            }

            // fill in a reasonable default if we did not manage to get the params
            if (requestedEnvelope == null) {
                requestedEnvelope = getOriginalEnvelope();
                width = 640;
                height = (int) Math.round(requestedEnvelope.getSpan(1)
                        / requestedEnvelope.getSpan(0) * 640);
            }

            // if the structure did not change reuse the same response 
            if (grid != null && grid.getGridGeometry().getGridRange2D().getWidth() == width
                    && grid.getGridGeometry().getGridRange2D().getHeight() == height
                    && grid.getEnvelope().equals(requestedEnvelope))
                return grid;

            grid = getMap(toReferencedEnvelope(requestedEnvelope), width, height);
            return grid;
        }

        /**
         * Execute the GetMap request
         */
        GridCoverage2D getMap(ReferencedEnvelope requestedEnvelope, int width, int height)
                throws IOException {
            // build the request
            ReferencedEnvelope gridEnvelope = requestedEnvelope;
            try {
                // first see if we can cascade the request in its native SRS
                String code = CRS.lookupIdentifier(requestedEnvelope.getCoordinateReferenceSystem(), false);
                if(code != null && layer.getSrs().contains(code)) {
                    initMapRequest(requestedEnvelope, code, width, height);
                } else {
                    // first reproject to the map CRS
                    gridEnvelope = requestedEnvelope.transform(getCrs(), true);
                    
                    // then adjust the form factor
                    if(gridEnvelope.getWidth() < gridEnvelope.getHeight()) {
                        height = (int) Math.round(width * gridEnvelope.getHeight() / gridEnvelope.getWidth());
                    } else {
                        width = (int) Math.round(height * gridEnvelope.getWidth() / gridEnvelope.getHeight());
                    }
                    
                    initMapRequest(gridEnvelope, srsName, width, height);
                }
            } catch(Exception e) {
                throw (IOException) new IOException("Could not reproject the request envelope").initCause(e);
            }
            

            // issue the request and wrap response in a grid coverage
            InputStream is = null;
            try {
                GetMapResponse response = wms.issueRequest(mapRequest);
                is = response.getInputStream();
                BufferedImage image = ImageIO.read(is);
                LOGGER.fine("GetMap completed");
                return gcf.create(layer.getTitle(), image, gridEnvelope);
            } catch(ServiceException e) {
                throw (IOException) new IOException("GetMap failed").initCause(e);
            }
        }

        void initMapRequest(ReferencedEnvelope requestedEnvelope, String srsName, int width, int height) {
            GetMapRequest mapRequest = wms.createGetMapRequest();
            mapRequest.addLayer(layer);
            mapRequest.setDimensions(width, height);
            mapRequest.setFormat(format);
            mapRequest.setSRS(srsName);
            mapRequest.setBBox(requestedEnvelope);
            mapRequest.setTransparent(true);
            
            this.mapRequest = mapRequest;
            this.requestedEnvelope = requestedEnvelope;
            this.width = width;
            this.height = height;
        }

        /**
         * Converts a {@link Envelope} into a {@link ReferencedEnvelope}
         * @param envelope
         * @return
         */
        ReferencedEnvelope toReferencedEnvelope(Envelope envelope) {
            ReferencedEnvelope env = new ReferencedEnvelope(envelope.getCoordinateReferenceSystem());
            env.expandToInclude(envelope.getMinimum(0), envelope.getMinimum(1));
            env.expandToInclude(envelope.getMaximum(0), envelope.getMaximum(1));
            return env;
        }

        public Format getFormat() {
            // this reader has not backing format
            return null;
        }

        /**
         * Returns the layer bounds
         * @return
         */
        public ReferencedEnvelope getBounds() {
            return bounds;
        }
    }

}
