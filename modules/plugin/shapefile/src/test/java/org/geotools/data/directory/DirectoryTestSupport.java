package org.geotools.data.directory;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import org.geotools.TestData;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.junit.After;

public class DirectoryTestSupport {
    
    static final URI NSURI;
    static {
        try {
            NSURI = new URI("http://www.geotools.org");
        } catch(Exception e) {
            throw new RuntimeException("Impossible...");
        }
    }
    File tempDir = null;
    
    @After
    public void tearDown() throws IOException {
        if(tempDir != null)
            deleteDirectory(tempDir);
    }
    
    FileStoreFactory getFileStoreFactory() {
        return new ShapefileDataStoreFactory.ShpFileStoreFactory(new ShapefileDataStoreFactory(),
                Collections.singletonMap(ShapefileDataStoreFactory.NAMESPACEP.key, NSURI));
    }
    
    /**
     * Recursively deletes the contents of the specified directory 
     */
    public static void deleteDirectory(File directory) throws IOException {
        for (File f : directory.listFiles()) {
            if (f.isDirectory()) {
                deleteDirectory(f);
            } else {
                f.delete();
            }
        }
        directory.delete();
    }

    /**
     * Copies the specified shape file into the {@code test-data} directory, together with its
     * sibling ({@code .dbf}, {@code .shp}, {@code .shx} and {@code .prj} files).
     */
    protected File copyShapefiles(final String name) throws IOException {
        return copyShapefiles(name, null);
    }
    
    /**
     * Copies the specified shape file into the {@code test-data} directory, together with its
     * sibling ({@code .dbf}, {@code .shp}, {@code .shx} and {@code .prj} files).
     */
    protected File copyShapefiles(final String name, final String directoryName) throws IOException {
        assertTrue(TestData.copy(this, sibling(name, "dbf"), directoryName).canRead());
        assertTrue(TestData.copy(this, sibling(name, "shp"), directoryName).canRead());
        assertTrue(TestData.copy(this, sibling(name, "shx"), directoryName).canRead());
        try {
            assertTrue(TestData.copy(this, sibling(name, "prj"), directoryName).canRead());
        } catch (FileNotFoundException e) {
            // Ignore: this file is optional.
        }
        return TestData.copy(this, name, directoryName);
    }
    
//    /**
//     * Copies the specified shape file into the {@code test-data} directory, together with its
//     * sibling ({@code .dbf}, {@code .shp}, {@code .shx} and {@code .prj} files).
//     */
//    protected File copyFile(String name, String destDirName) throws IOException {
//        File directory = TestData.file(TestData.class, null);
//        InputStream is = this.getClass().getResourceAsStream(name);
//        File destDir = new File(directory, destDirName);
//        if(!destDir.exists())
//            destDir.mkdirs();
//        File file = new File(destDir, name);
//        file.deleteOnExit();
//        final OutputStream out = new FileOutputStream(file);
//        final byte[] buffer = new byte[4096];
//        int count;
//        while ((count = is.read(buffer)) >= 0) {
//            out.write(buffer, 0, count);
//        }
//        out.close();
//        is.close();
//        
//        return file;
//    }
   
    /**
     * Helper method for {@link #copyShapefiles}.
     */
    private static String sibling(String name, final String ext) {
        final int s = name.lastIndexOf('.');
        if (s >= 0) {
            name = name.substring(0, s);
        }
        return name + '.' + ext;
    }
}
