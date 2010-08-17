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
package org.geotools.data.shapefile.indexed;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.shapefile.FileWriter;
import org.geotools.data.shapefile.ShpFileType;
import org.geotools.data.shapefile.ShpFiles;
import org.geotools.data.shapefile.StorageFile;
import org.geotools.data.shapefile.shp.IndexFile;
import org.geotools.data.shapefile.shp.ShapefileHeader;
import org.geotools.data.shapefile.shp.ShapefileReader;
import org.geotools.data.shapefile.shp.ShapefileReader.Record;
import org.geotools.index.LockTimeoutException;
import org.geotools.index.TreeException;
import org.geotools.index.quadtree.Node;
import org.geotools.index.quadtree.QuadTree;
import org.geotools.index.quadtree.StoreException;
import org.geotools.index.quadtree.fs.FileSystemIndexStore;
import org.geotools.index.quadtree.fs.IndexHeader;
import org.geotools.util.NullProgressListener;
import org.geotools.util.logging.Logging;
import org.opengis.util.ProgressListener;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Utility class for Shapefile spatial indexing
 * 
 * @author Tommaso Nolli
 * @source $URL:
 *         http://svn.geotools.org/geotools/trunk/gt/modules/plugin/shapefile/src/main/java/org/geotools/data/shapefile/indexed/ShapeFileIndexer.java $
 */
public class ShapeFileIndexer implements FileWriter {
    private static final Logger LOGGER = Logging.getLogger(ShapeFileIndexer.class);
    
    private int max = -1;
    private int loadFactor = 8;
    private String byteOrder;
    private boolean interactive = false;
    private ShpFiles shpFiles;

    public static void main(String[] args) throws IOException {
        if ((args.length < 1) || (((args.length - 1) % 2) != 0)) {
            usage();
        }

        long start = System.currentTimeMillis();

        ShapeFileIndexer idx = new ShapeFileIndexer();
        idx.interactive = true;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-t")) {
                // idx.setIdxType(IndexType.valueOf(args[++i]));
                // just skip it for backwards compatibility
                i++;
            } else if (args[i].equals("-M")) {
                idx.setMax(Integer.parseInt(args[++i]));
            } else if (args[i].equals("-b")) {
                idx.setByteOrder(args[++i]);
            } else {
                if (!args[i].toLowerCase().endsWith(".shp")) {
                    System.out.println("File extension must be '.shp'");
                    System.exit(1);
                }

                idx.setShapeFileName(new ShpFiles(args[i]));
            }
        }

        try {
            System.out.print("Indexing ");

            int cnt = idx.index(true, new NullProgressListener());
            System.out.println();
            System.out.print(cnt + " features indexed ");
            System.out.println("in " + (System.currentTimeMillis() - start)
                    + "ms.");
            System.out.println();
        } catch (Exception e) {
            e.printStackTrace();
            usage();
            System.exit(1);
        }
    }

    private static void usage() {
        System.out.println("Usage: ShapeFileIndexer " + "-t <QIX> "
                + "[-M <max entries per node>] "
                + "[-s <split algorithm>] "
                + "[-b <byte order NL | NM>] " + "<shape file>");

        System.out.println();

        System.out.println("Options:");
        System.out.println("\t-t Index type: RTREE or QUADTREE");
        System.out.println();
        System.out.println("Following options apllies only to RTREE:");
        System.out.println("\t-M maximum number of entries per node");
        System.out.println("\t-m minimum number of entries per node");
        System.out.println("\t-s split algorithm to use");
        System.out.println();
        System.out.println("Following options apllies only to QUADTREE:");
        System.out.println("\t-b byte order to use: NL = LSB; "
                + "NM = MSB (default)");

        System.exit(1);
    }

    /**
     * Index the shapefile denoted by setShapeFileName(String fileName) If when
     * a thread starts, another thread is indexing the same file, this thread
     * will wait that the first thread ends indexing; in this case <b>zero</b>
     * is reurned as result of the indexing process.
     * 
     * @param verbose
     *                enable/disable printing of dots every 500 indexed records
     * @param listener
     *                DOCUMENT ME!
     * 
     * @return The number of indexed records (or zero)
     * 
     * @throws MalformedURLException
     * @throws IOException
     * @throws TreeException
     * @throws StoreException
     *                 DOCUMENT ME!
     * @throws LockTimeoutException
     */
    public int index(boolean verbose, ProgressListener listener)
            throws MalformedURLException, IOException, TreeException,
            StoreException, LockTimeoutException {
        
        if (this.shpFiles == null) {
            throw new IOException("You have to set a shape file name!");
        }

        int cnt = 0;

        ShapefileReader reader = null;

        // Temporary file for building...
        StorageFile storage = shpFiles.getStorageFile(ShpFileType.QIX);
        File treeFile = storage.getFile();

        try {
            reader = new ShapefileReader(shpFiles, true, false, new GeometryFactory());
            
            if(max == -1) {
                // compute a reasonable index max depth, considering a fully developed
                // 10 levels one already contains 200k index nodes, good for indexing up
                // to 3M features without consuming too much memory
                int features = reader.getCount(0);
                max = 1;
                int nodes = 1;
                while(nodes * 8 < features) {
                    max++;
                    nodes *= 4;
                }
                
                reader.close();
                reader = new ShapefileReader(shpFiles, true, false, new GeometryFactory());
            }
            
            cnt = this.buildQuadTree(reader, treeFile, verbose);
        } finally {
            if (reader != null)
                reader.close();
        }

        // Final index file
        storage.replaceOriginal();

        return cnt;
    }

    private int buildQuadTree(ShapefileReader reader, File file, boolean verbose)
            throws IOException, StoreException {
        LOGGER.fine("Building quadtree spatial index with depth " +  max + " for file " + file.getAbsolutePath());
        
        byte order = 0;

        if ((this.byteOrder == null) || this.byteOrder.equalsIgnoreCase("NM")) {
            order = IndexHeader.NEW_MSB_ORDER;
        } else if (this.byteOrder.equalsIgnoreCase("NL")) {
            order = IndexHeader.NEW_LSB_ORDER;
        } else {
            throw new StoreException("Asked byte order '" + this.byteOrder
                    + "' must be 'NL' or 'NM'!");
        }

        IndexFile shpIndex = new IndexFile(shpFiles, false);
        QuadTree tree = null;
        int cnt = 0;
        int numRecs = shpIndex.getRecordCount();
        ShapefileHeader header = reader.getHeader();
        Envelope bounds = new Envelope(header.minX(), header.maxX(), header
                .minY(), header.maxY());

        tree = new QuadTree(numRecs, max, bounds, shpIndex);
        try {
            Record rec = null;

            while (reader.hasNext()) {
                rec = reader.nextRecord();
                tree.insert(cnt++, new Envelope(rec.minX, rec.maxX, rec.minY,
                        rec.maxY));

                if (verbose && ((cnt % 1000) == 0)) {
                    System.out.print('.');
                }
                if (cnt % 100000 == 0)
                    System.out.print('\n');
            }
            if (verbose)
                System.out.println("done");
            FileSystemIndexStore store = new FileSystemIndexStore(file, order);
            
            if(loadFactor > 0) {
                System.out.println("Rebalancing the tree (this might take some time)");
                applyLoadFactor(tree, tree.getRoot(), 0, reader, shpIndex);
                System.out.println("Done");
            }
            
            printStats(tree);
            store.store(tree);
        } finally {
            tree.close();
        }
        return cnt;
    }

    private void applyLoadFactor(QuadTree tree, Node node, int level, ShapefileReader reader, IndexFile index) throws StoreException, IOException {
//        System.out.println("Analyzing node at level " + level);
        if(node.getNumShapeIds() > loadFactor && node.getNumSubNodes() == 0) {
            // ok, we need to split this baby further
            int[] shapeIds = node.getShapesId();
            int numShapesId = node.getNumShapeIds();
            node.clean();
            
//            System.out.println("Splitting node with " + numShapesId + " ids at level " + level);
            System.out.print(".");
            
            // get an estimate on how many more levels we need
            int extraLevels = 0;
            int nodes = 1;
            while(nodes * loadFactor < numShapesId) {
                extraLevels++;
                nodes *= 4;
            }
            
            for (int i = 0; i < numShapesId; i++) {
                final int shapeId = shapeIds[i];
                int offset = index.getOffsetInBytes(shapeId);
                reader.goTo(offset);
                Record rec = reader.nextRecord();
                Envelope env = new Envelope(rec.minX, rec.maxX, rec.minY, rec.maxY);
                tree.insert(node, shapeId, env, extraLevels);
            }
        }
        // recurse, with a check to avoid too deep recursion due to odd data that has a
        // number of superimposed points and the like
        if(node.getNumSubNodes() > 0 && level < max * 2) {
            // not a leaf, we cannot split it further
            for (int i = 0; i < node.getNumSubNodes(); i++) {
                applyLoadFactor(tree, node.getSubNode(i), level + 1, reader, index);
            }
        }

    }

    private void printStats(QuadTree tree) throws StoreException {
       Map<Integer, Integer> stats = new HashMap<Integer, Integer>();
       gatherStats(tree.getRoot(), stats);
       
       List<Integer> nums = new ArrayList<Integer>(stats.keySet());
       Collections.sort(nums);
       for (Integer num : nums) {
           System.out.println(num + " -> " + stats.get(num));
       }
    }

    void gatherStats(Node node, Map<Integer, Integer> stats) throws StoreException  {
        int num = node.getNumShapeIds();
        Integer count = stats.get(num);
        if(count == null) {
            stats.put(num, 1);
        } else {
            stats.put(num, count + 1);
        }
        for (int i = 0; i < node.getNumSubNodes(); i++) {
            gatherStats(node.getSubNode(i), stats);
        }
    }

    /**
     * For quad tree this is the max depth. I don't know what it is for RTree
     * 
     * @param i
     */
    public void setMax(int i) {
        max = i;
    }

    /**
     * DOCUMENT ME!
     * 
     * @param shpFiles
     */
    public void setShapeFileName(ShpFiles shpFiles) {
        this.shpFiles = shpFiles;
    }


    /**
     * DOCUMENT ME!
     * 
     * @param byteOrder
     *                The byteOrder to set.
     */
    public void setByteOrder(String byteOrder) {
        this.byteOrder = byteOrder;
    }

    public String id() {
        return getClass().getName();
    }
}
