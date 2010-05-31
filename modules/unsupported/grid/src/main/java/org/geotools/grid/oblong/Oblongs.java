/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.grid.oblong;

import java.util.HashMap;
import java.util.Map;

import com.vividsolutions.jts.geom.Envelope;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.grid.AttributeSetter;
import org.geotools.grid.GridElement;
import org.geotools.grid.Neighbor;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author michael
 */
public class Oblongs {

    /**
     * Creates a new {@code Oblong} object.
     *
     * @param minX the min X ordinate
     *
     * @param minY the min Y ordinate
     *
     * @param width the width
     *
     * @param height the height
     *
     * @param crs the coordinate reference system (may be {@code null})
     *
     * @return a new {@code Oblong} object
     *
     * @throws IllegalArgumentException if either {@code width} or {@code height}
     *         are {@code <=} 0
     */
    public static Oblong create(double minX, double minY, double width, double height,
            CoordinateReferenceSystem crs) {
        return new OblongImpl(minX, minY, width, height, crs);
    }

    /**
     * Creates a new {@code Oblong} positioned at the given neighbor position
     * relative to the reference element.
     *
     * @param el the reference oblong
     *
     * @param neighbor a neighbour position
     *
     * @return a new {@code Oblong} object
     *
     * @throws IllegalArgumentException if either argument is {@code null} or
     *         if {@code el} is not an instance of {@code Oblong}
     */
    public static Oblong createNeighbor(GridElement el, Neighbor neighbor) {
        if (el == null || neighbor == null) {
            throw new IllegalArgumentException(
                    "el and neighbour position must both be non-null");
        }

        if (!(el instanceof Oblong)) {
            throw new IllegalArgumentException("el must be an instance of Oblong");
        }

        Oblong oblong = (Oblong) el;
        ReferencedEnvelope bounds = oblong.getBounds();
        double dx, dy;

        switch (neighbor) {
            case LEFT:
                dx = -bounds.getWidth();
                dy = 0.0;
                break;

            case LOWER:
                dx = 0.0;
                dy = -bounds.getHeight();
                break;

            case LOWER_LEFT:
                dx = -bounds.getWidth();
                dy = -bounds.getHeight();
                break;

            case LOWER_RIGHT:
                dx = bounds.getWidth();
                dy = -bounds.getHeight();
                break;

            case RIGHT:
                dx = bounds.getWidth();
                dy = 0.0;
                break;

            case UPPER:
                dx = 0.0;
                dy = bounds.getHeight();
                break;

            case UPPER_LEFT:
                dx = -bounds.getWidth();
                dy = bounds.getHeight();
                break;

            case UPPER_RIGHT:
                dx = bounds.getWidth();
                dy = bounds.getHeight();
                break;

            default:
                throw new IllegalArgumentException("Unrecognized value for neighbor");
        }

        return create(bounds.getMinX() + dx, bounds.getMinY() + dy,
                bounds.getWidth(), bounds.getHeight(),
                bounds.getCoordinateReferenceSystem());

    }

    /**
     * Creates a new grid of oblongs within a bounding rectangle with grid elements
     * represented by densified polygons (ie. additional vertices added to each
     * edge).
     *
     * @param bounds the bounding rectangle
     *
     * @param width oblong width
     *
     * @param height oblong height
     *
     * @param vertexSpacing maximum distance between adjacent vertices in a grid
     *        element; if {@code <= 0} or {@code >= min(width, height) / 2.0} it
     *        is ignored and the polygons will not be densified
     *
     * @param setter an instance of {@code AttributeSetter}
     *
     * @return a new grid
     */
    public static SimpleFeatureCollection createGrid(ReferencedEnvelope bounds, 
            double width, double height, double vertexSpacing, AttributeSetter setter) {

        final SimpleFeatureCollection fc = FeatureCollections.newCollection();
        final SimpleFeatureBuilder builder = new SimpleFeatureBuilder(setter.getType());
        String geomPropName = setter.getType().getGeometryDescriptor().getLocalName();

        final boolean densify =
                vertexSpacing > 0.0 && vertexSpacing < Math.min(width, height);

        Oblong el0 = create(bounds.getMinX(), bounds.getMinY(), width, height,
                bounds.getCoordinateReferenceSystem());
        Oblong el = el0;

        while (el.getBounds().getMinY() <= bounds.getMaxY()) {
            while (el.getBounds().getMaxX() <= bounds.getMaxX()) {
                if (((Envelope)bounds).contains(el.getBounds())) {
                    Map<String, Object> attrMap = new HashMap<String, Object>();
                    setter.setAttributes(el, attrMap);

                    if (densify) {
                        builder.set(geomPropName, el.toDensePolygon(vertexSpacing));
                    } else {
                        builder.set(geomPropName, el.toPolygon());
                    }
                    for (String propName : attrMap.keySet()) {
                        builder.set(propName, attrMap.get(propName));
                    }

                    fc.add(builder.buildFeature(setter.getFeatureID(el)));
                }

                el = createNeighbor(el, Neighbor.RIGHT);
            }

            el0 = createNeighbor(el0, Neighbor.UPPER);
            el = el0;
        }

        return fc;
    }


    /**
     * Creates a new grid of oblongs within a bounding rectangle with grid elements
     * represented by simple (ie. undensified) polygons.
     *
     * @param bounds the bounding rectangle
     *
     * @param width oblong width
     *
     * @param height oblong height
     *
     * @param vertexSpacing maximum distance between adjacent vertices in a grid
     *        element; if {@code <= 0} or {@code >= min(width, height) / 2.0} it
     *        is ignored and the polygons will not be densified
     *
     * @param setter an instance of {@code AttributeSetter}
     *
     * @return a new grid
     */
    public static SimpleFeatureCollection createGrid(ReferencedEnvelope bounds,
            double width, double height, AttributeSetter setter) {
        return createGrid(bounds, width, height, -1.0, setter);
    }

}
