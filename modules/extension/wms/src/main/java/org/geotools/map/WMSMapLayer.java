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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Logger;


import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.DataUtilities;
import org.geotools.data.ows.Layer;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.wms.WebMapServer;
import org.geotools.data.wms.request.GetMapRequest;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.RendererUtilities;
import org.geotools.resources.coverage.FeatureUtilities;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.sun.org.apache.bcel.internal.generic.ASTORE;

/**
 * Wraps a WMS layer into a {@link MapLayer} for interactive rendering usage TODO: expose a
 * GetFeatureInfo that returns a feature collection TODO: expose the list of named styles and allow
 * choosing which style to use
 * 
 * @author Andrea Aime - OpenGeo
 */
public class WMSMapLayer extends MapLayer {
    /**
     * Builds a new WMS layer
     * 
     * @param wms
     * @param layer
     */
    public WMSMapLayer(WebMapServer wms, Layer layer) {
        super( new WMSLayer( wms, layer ) );
    }

    @Override
    public WMSLayer toLayer() {
        return (WMSLayer) super.toLayer();
    }

}
