package org.geotools.demo.grid;

import java.awt.Color;
import java.net.URL;

import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.grid.Envelopes;
import org.geotools.grid.Grids;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.styling.SLD;
import org.geotools.swing.JMapFrame;


/**
 * This example creates a lat-long vector grid, then displays it over a
 * shapefile in a different map projection to illustrate how the grid with
 * 'densified' polygons (additional vertices added to each square) gives
 * a nice approximation of curves in the reprojected display.
 *
 * @author mbedward
 */
public class ReprojectedGridExample {

    public static void main(String[] args) throws Exception {
        URL url = ReprojectedGridExample.class.getResource("/data/shapefiles/bc_voting_areas.shp");
        FileDataStore store = FileDataStoreFinder.getDataStore(url);
        FeatureSource featureSource = store.getFeatureSource();

        ReferencedEnvelope featureBounds = featureSource.getBounds();
        ReferencedEnvelope latLonEnv = featureBounds.transform(DefaultGeographicCRS.WGS84, true);

        // create a grid of squares 2 degrees across
        final double gridSize = 2.0;

        // vertex spacing for 'densified' grid polygons
        final double vertexSpacing = gridSize / 20;
        ReferencedEnvelope roundedEnv = Envelopes.expandToInclude(latLonEnv, gridSize);

        // create the vector grid
        SimpleFeatureCollection grid = Grids.createSquareGrid(roundedEnv, gridSize, vertexSpacing);

        MapContext map = new DefaultMapContext();
        map.setTitle("Vector grid");
        map.setCoordinateReferenceSystem(featureBounds.getCoordinateReferenceSystem());
        map.addLayer(featureSource, SLD.createPolygonStyle(Color.BLUE, Color.CYAN, 1.0f));
        map.addLayer(grid, SLD.createPolygonStyle(Color.LIGHT_GRAY, null, 1.0f));

        JMapFrame.showMap(map);
    }
}
