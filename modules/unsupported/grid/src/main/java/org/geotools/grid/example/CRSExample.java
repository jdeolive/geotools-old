package org.geotools.grid.example;

import java.awt.Color;
import java.net.URL;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.grid.Grids;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.SLD;
import org.geotools.swing.JMapFrame;

public class CRSExample {

    public static void main(String[] args) throws Exception {
        URL url = CRSExample.class.getResource("bc_voting_areas.shp");
        new CRSExample().displayMap(url);
    }

    private void displayMap(URL url) throws Exception {
        FileDataStore store = FileDataStoreFinder.getDataStore(url);
        FeatureSource featureSource = store.getFeatureSource();

        ReferencedEnvelope featureBounds = featureSource.getBounds();
        ReferencedEnvelope latLonEnv = featureBounds.transform(DefaultGeographicCRS.WGS84, true);
        ReferencedEnvelope roundedEnv = new EnvelopeRounder().expandToInclude(latLonEnv, 5.0);

        // Create a map context and add our shapefile to it
        MapContext map = new DefaultMapContext();
        map.setTitle("Vector grid");
        map.setCoordinateReferenceSystem(featureBounds.getCoordinateReferenceSystem());
        map.addLayer(featureSource, SLD.createPolygonStyle(Color.BLUE, Color.CYAN, 1.0f));
        map.addLayer(Grids.createSquareGrid(roundedEnv, 5.0, 0.02),
                SLD.createPolygonStyle(Color.LIGHT_GRAY, null, 1.0f));

        // Now display the map
        JMapFrame.showMap(map);
    }

    class EnvelopeRounder {

        private static final double EPS = 1.0E-8D;

        /**
         * Include the provided envelope, expanding as necessary and rounding
         * the bounding coordinates such that they are multiples of the
         * specified resolution. For example, if {@code resolution} is 100 then the
         * min and max bounding coordinates of this envelope will set to mutliples
         * of 100 by rounding down the min values and rounding up the max values
         * if required.
         * <pre><code>
         * // Example, create a new envelope that cntains an input envlope and
         * // whose boundind coordinates are multiples of 100
         * //
         * ReferencedEnvelope inputEnv = ...
         * ReferencedEnvelope roundedEnv = new ReferencedEnvelope();
         * roundedEnv.expandToInclude(inputEnv, 100);
         * </code></pre>
         *
         * @param srcEnv the envelope to include
         * @param resolution resolution (in world distance units) of the resulting
         *        boundary coordinates
         *
         * @return the expanded envelope
         */
        public ReferencedEnvelope expandToInclude(ReferencedEnvelope srcEnv, double resolution) {
            double minX = roundOrdinate(srcEnv.getMinX(), resolution, false);
            double maxX = roundOrdinate(srcEnv.getMaxX(), resolution, true);
            double minY = roundOrdinate(srcEnv.getMinY(), resolution, false);
            double maxY = roundOrdinate(srcEnv.getMaxY(), resolution, true);

            ReferencedEnvelope expanded = new ReferencedEnvelope(srcEnv);
            expanded.expandToInclude(minX, minY);
            expanded.expandToInclude(maxX, maxY);
            return expanded;
        }

        /**
         * Helper method to round ordinate values up or down to a specified resolution.
         * The returned value will be a multiple of the specified resolution.
         * <pre><code>
         * double ordinate = 1234.56;
         * double resolution = 100;
         * double rounded;
         *
         * // this will return 1200
         * rounded = roundOrdinate(ordinate, resolution, false);
         *
         * // this will return 1300
         * rounded = roundOrdinate(ordinate, resolution, true);
         * </code></pre>
         * @param ordinate the ordinate to round up or down.
         * @param resolution the desired resolution
         * @param roundUp true to round up; false to round down
         *
         * @return the rounded ordinate value
         */
        private double roundOrdinate(double ordinate, double resolution, boolean roundUp) {
            double unsigned = Math.abs(ordinate);
            boolean negative = ordinate < 0.0;
            if (negative) {
                roundUp = !roundUp;
            }

            double rounded;
            if (roundUp) {
                double x = unsigned / resolution;
                int up = (x - (long) x) > EPS ? 1 : 0;
                rounded = resolution * (up + (long) x);
            } else {
                rounded = resolution * (long) (unsigned / resolution);
            }

            return negative ? -rounded : rounded;
        }
    }
}
