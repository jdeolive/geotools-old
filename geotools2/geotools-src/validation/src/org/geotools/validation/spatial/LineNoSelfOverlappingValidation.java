/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation.spatial;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.validation.DefaultFeatureValidation;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;


/**
 * Ensure the defaultGeometry does not overlap (only works for LineString).
 * 
 * <p>
 * Tests to see if a LineString overlaps itself. It does this by breaking up
 * the LineString into two point segments then intersects them all. If a
 * segment has both of its points on another segment, then they overlap. This
 * is not true in all cases and this method has to be rewritten. If a segment
 * spans two segments, this method will say that they do not overlap when
 * clearly they do.
 * </p>
 *
 * @author bowens, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: LineNoSelfOverlappingValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public class LineNoSelfOverlappingValidation extends DefaultFeatureValidation {
    /** The logger for the validation module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.validation");

    /**
     * LineNoSelfOverlappingFeatureValidation constructor.
     * 
     * <p>
     * Description
     * </p>
     */
    public LineNoSelfOverlappingValidation() {
    }

    /**
     * Override getPriority.
     * 
     * <p>
     * Sets the priority level of this validation.
     * </p>
     *
     * @return A made up priority for this validation.
     *
     * @see org.geotools.validation.Validation#getPriority()
     */
    public int getPriority() {
        return PRIORITY_SIMPLE;
    }

    /**
     * Implementation of getTypeNames.
     *
     * @return Array of typeNames, or empty array for all, null for disabled
     *
     * @see org.geotools.validation.Validation#getTypeNames()
     */
    public String[] getTypeNames() {
        if (getTypeRef() == null) {
            return null;
        }

        if (getTypeRef().equals("*")) {
            return ALL;
        }

        return new String[] { getTypeRef(), };
    }

    /**
     * Tests to see if a LineString overlaps itself.
     * 
     * <p>
     * It does this by breaking up the LineString into two point segments then
     * intersects them all. If a segment has both of its points on another
     * segment, then they overlap. This is not true in all cases and this
     * method has to be rewritten. If a segment spans two segments, this
     * method will say that they do not overlap when clearly they do.
     * </p>
     *
     * @param feature The Feature to be validated
     * @param type The FeatureTypeInfo of the feature
     * @param results The storage for error messages.
     *
     * @return True if the feature does not overlap itself.
     *
     * @see org.geotools.validation.FeatureValidation#validate(org.geotools.feature.Feature,
     *      org.geotools.feature.FeatureTypeInfo,
     *      org.geotools.validation.ValidationResults)
     */
    public boolean validate(Feature feature, FeatureType type,
        ValidationResults results) {
        //BUG: refer to comments above.
        LOGGER.setLevel(Level.ALL);

        Geometry geom = feature.getDefaultGeometry();

        if (geom == null) {
            results.error(feature, "Geometry is null - cannot validate.");

            return false;
        }

        if (geom instanceof LineString) {
            if (geom.getNumPoints() < 2) {
                results.error(feature,
                    "LineString contains too few points - cannot validate.");

                return false;
            }

            GeometryFactory gf = new GeometryFactory();

            // get the LineString out of the Geometry
            LineString ls = (LineString) geom;
            int numPoints = ls.getNumPoints();

            // break up the LineString into line segments
            LineString[] segments = new LineString[numPoints - 1];

            for (int i = 0; i < (numPoints - 1); i++) {
                Coordinate[] coords = new Coordinate[] {
                        ls.getCoordinateN(i), ls.getCoordinateN(i + 1)
                    };
                segments[i] = gf.createLineString(coords);
            }

            // overlap all of the line segments with each other
            for (int i = 0; i < segments.length; i++) // for each line segment
             {
                for (int j = 0; j < segments.length; j++) // test with every other line segment
                 {
                    if ((i != j) && ((i - 1) != j) && ((i + 1) != j)) // if they aren't the same segment
                     {
                        // generate two points out of segment[i]
                        Point p1 = gf.createPoint(segments[i].getCoordinateN(0));
                        Point p2 = gf.createPoint(segments[i].getCoordinateN(0));

                        if (p1.touches(segments[j]) && p2.touches(segments[j])) // if they overlap
                         {
                            // log the error and return
                            String message = "LineString overlapped itself.";
                            results.error(feature, message);
                            LOGGER.log(Level.FINEST,
                                getName() + "(" + feature.getID() + "):"
                                + message);

                            return false;
                        }
                    }
                }
            }
        } else {
            results.error(feature,
                "Geometry not a LineString - cannot validate.");

            return false;
        }

        LOGGER.log(Level.FINEST, getName() + "(" + feature.getID() + ") passed");

        return true;
    }
}
