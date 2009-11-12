/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.swing.tool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.lang.ref.WeakReference;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.FeatureCollection;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapLayer;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

/**
 * Helper class used by {@code InfoTool} to query {@code MapLayers}
 * with vector feature data.
 * <p>
 * Implementation note: this class keeps only a weak reference to
 * the {@code MapLayer} it is working with to avoid memory leaks if
 * the layer is deleted.
 *
 * @see InfoTool
 * @see GridLayerHelper
 *
 * @author Michael Bedward
 * @since 2.6
 * @source $Id$
 * @version $URL$
 */
public class VectorLayerHelper extends InfoToolHelper<FeatureCollection> {
    
    private static final GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
    private static final FilterFactory2 filterFactory = CommonFactoryFinder.getFilterFactory2(null);
    
    private final WeakReference<MapLayer> layerReference;
    private final String attrName;
    private final boolean isPolygonGeometry;

    /**
     * Create a new helper to work with {@code MapLayers} having vector feature data.
     *
     * @param layer the map layer
     *
     * @param geomAttributeName the name of the geometry attribute for {@code Features}
     *
     * @param geomClass the geometry class
     */
    public VectorLayerHelper(MapLayer layer, String geomAttributeName, Class<?> geomClass) {
        super(Type.VECTOR_HELPER);

        System.out.println("Creating VectorLayerHelper instance");

        this.layerReference = new WeakReference<MapLayer>(layer);
        this.attrName = geomAttributeName;

        isPolygonGeometry = (Polygon.class.isAssignableFrom(geomClass) ||
                MultiPolygon.class.isAssignableFrom(geomClass));
    }

    /**
     * Get feature data at the given position.
     *
     * @param pos the location to query
     *
     * @param params a {@code Double} value for the search radius to use with
     *        point or line features
     *
     * @return
     *
     * @throws Exception
     */
    public FeatureCollection getInfo(DirectPosition2D pos, Object ...params)
            throws Exception {

        FeatureCollection<? extends FeatureType, ? extends Feature> collection = null;
        MapLayer layer = layerReference.get();

        if (layer != null) {
            Filter filter = null;
            if (isPolygonGeometry) {
                /*
                 * Polygon features - use an intersects filter
                 */
                Geometry posGeom = geometryFactory.createPoint(new Coordinate(pos.x, pos.y));
                filter = filterFactory.intersects(
                        filterFactory.property(attrName),
                        filterFactory.literal(posGeom));

        } else {
                /*
                 * Line or point features - use a bounding box filter
                 */
                double radius = ((Number) params[0]).doubleValue();

                ReferencedEnvelope searchBounds = new ReferencedEnvelope(
                        pos.x - radius,
                        pos.x + radius,
                        pos.y - radius,
                        pos.y + radius,
                        pos.getCoordinateReferenceSystem());

                filter = filterFactory.bbox(filterFactory.property(attrName), searchBounds);
            }

            collection = layer.getFeatureSource().getFeatures(filter);
        }

        return collection;
    }
    
}
