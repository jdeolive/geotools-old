/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.geotools.grid.oblong;

import com.vividsolutions.jts.geom.Envelope;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.grid.AttributeSetter;
import org.geotools.grid.GridElement;
import org.geotools.grid.Neighbor;

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
     * @return a new {@code Oblong} object
     *
     * @throws IllegalArgumentException if either {@code width} or {@code height}
     *         are {@code <=} 0
     */
    public static Oblong create(double minX, double minY, double width, double height) {
        return new OblongImpl(minX, minY, width, height);
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

        Envelope bounds = oblong.getBounds();
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
                bounds.getWidth(), bounds.getHeight());

    }

    /**
     * Creates a new grid of oblongs within a bounding rectangle.
     *
     * @param bounds the bounding rectangle
     *
     * @param width oblong width
     *
     * @param height oblong height
     *
     * @param setter an instance of {@code AttributeSetter}
     *
     * @return a new grid
     */
    public static SimpleFeatureCollection createGrid(Envelope bounds, double width, double height, AttributeSetter setter) {

        final SimpleFeatureCollection fc = FeatureCollections.newCollection();
        final SimpleFeatureBuilder builder = new SimpleFeatureBuilder(setter.getType());
        String geomPropName = setter.getType().getGeometryDescriptor().getLocalName();

        Oblong el0 = create(bounds.getMinX(), bounds.getMinY(), width, height);
        Oblong el = el0;

        while (el.getBounds().getMinY() <= bounds.getMaxY()) {
            while (el.getBounds().getMaxX() <= bounds.getMaxX()) {
                if (bounds.contains(el.getBounds())) {
                    Map<String, Object> attrMap = new HashMap<String, Object>();
                    setter.setAttributes(el, attrMap);

                    builder.set(geomPropName, el.toPolygon());
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

}
