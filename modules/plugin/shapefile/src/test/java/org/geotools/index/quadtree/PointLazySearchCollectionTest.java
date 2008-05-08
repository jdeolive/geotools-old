package org.geotools.index.quadtree;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.geotools.data.shapefile.TestCaseSupport;
import org.geotools.data.shapefile.indexed.IndexedShapefileDataStore;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * @author Jesse
 */
public class PointLazySearchCollectionTest extends TestCaseSupport {

    private File file;
    private IndexedShapefileDataStore ds;
    private QuadTree tree;
    private Iterator iterator;
    private CoordinateReferenceSystem crs;

    public PointLazySearchCollectionTest() throws IOException {
        super("LazySearchIteratorTest");
    }

    protected void setUp() throws Exception {
        super.setUp();
        file = copyShapefiles("shapes/archsites.shp");
        ds = new IndexedShapefileDataStore(file.toURL());
        ds.buildQuadTree(0);
        tree = LineLazySearchCollectionTest.openQuadTree(file);
        crs = ds.getSchema().getCRS();
    }

    protected void tearDown() throws Exception {
        if (iterator != null)
            tree.close(iterator);
        tree.close();
        super.tearDown();
        file.getParentFile().delete();
    }

    public void testGetAllFeatures() throws Exception {
        ReferencedEnvelope env = new ReferencedEnvelope(585000, 610000,
                4910000, 4930000, crs);
        LazySearchCollection collection = new LazySearchCollection(tree, env);
        assertEquals(25, collection.size());
    }

    public void testGetOneFeatures() throws Exception {
        ReferencedEnvelope env = new ReferencedEnvelope(597867, 598068,
                4918863, 4919031, crs);
        LazySearchCollection collection = new LazySearchCollection(tree, env);
        assertEquals(1, collection.size());

    }

    public void testGetNoFeatures() throws Exception {
        ReferencedEnvelope env = new ReferencedEnvelope(592211, 597000,
                4910947, 4913500, crs);
        LazySearchCollection collection = new LazySearchCollection(tree, env);
        assertEquals(0, collection.size());
    }
}
