/*
 * TestCaseSupport.java
 *
 * Created on April 30, 2003, 12:16 PM
 */

package org.geotools.data.shapefile;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.*;
import java.io.*;
import junit.framework.*;
import java.net.*;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.zip.*;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;

/**
 *
 * @author  Ian Schneider
 */
public abstract class TestCaseSupport extends TestCase {
    
    // make sure unzipping of data only occurs once per suite
    static boolean prepared = false;
    // used for locating the base of testData, TestCaseSupportTest will modify
    // this to do its own tests.
    String baseDir = "";
    // store all temp files here - delete on tear down
    ArrayList tmpFiles = new ArrayList();
    
    /** Creates a new instance of TestCaseSupport */
    public TestCaseSupport(String name) {
        super(name);
        prepareData();
    }
    
    public TestCaseSupport(String name,String baseDir) {
        super(name);
        this.baseDir = baseDir;
        prepareData();
    }
    
    protected Geometry readGeometry(String wktResource) {
        WKTReader reader = new WKTReader();
        InputStream stream = getClass().getResourceAsStream( wktResource + ".wkt");
        try {
            return reader.read(new InputStreamReader(stream));
        } catch (ParseException pe) {
            throw new RuntimeException("parsing error in resource " + wktResource,pe);
        }
    }
    
    protected void tearDown() throws Exception {
        // it seems that not all files marked as temp will get erased, perhaps
        // this is because they have been rewritten? Don't know, don't _really_
        // care, so I'll just delete everything
        java.util.Iterator f = tmpFiles.iterator();
        while (f.hasNext()) {
            File tf = (File) f.next();
            sibling(tf,"dbf").delete();
            sibling(tf,"shx").delete();
            tf.delete();
            f.remove();
        }
    }
    
    private File sibling(File f,String ext) {
        String name = f.getName();
        name = name.substring(0,name.indexOf('.') + 1);
        return new File(f.getParent(),name + ext);
    }
    
    private void prepareData() {
        if (prepared) return;
        prepared = true;
        
        ZipInputStream zip = new ZipInputStream(getTestResourceAsStream("data.zip"));
        
        try {
            File base = new File(URLDecoder.decode(getTestResource(baseDir).getPath(),"UTF-8"));
            ZipEntry entry;
            byte[] bytes = new byte[8096];
            while ( (entry = zip.getNextEntry()) != null) {
                File dest = new File(base,entry.getName());
                // support for directories in zip file
                if (entry.isDirectory()) {
                    dest.mkdir();
                    continue;
                }
                FileOutputStream fout = new FileOutputStream(dest);
                int r;
                while (zip.available() == 1 && (r = zip.read(bytes)) != -1) {
                    fout.write(bytes,0,r);
                }
                fout.close();
            }
        } catch (java.io.IOException ioe) {
            throw new RuntimeException("Error extracting test data " + ioe.getMessage(),ioe);
        }
    }
    
    protected URL getTestResource(String name) {
        URL r = getClass().getResource("/testData/" + name);
        if (r == null)
            throw new RuntimeException("Could not locate resource : " + name);
        return r;
    }
    
    protected InputStream getTestResourceAsStream(String name) {
        InputStream in = getClass().getResourceAsStream("/testData/" + name);
        if (in == null)
            throw new RuntimeException("Could not locate resource : " + name);
        return in;
    }
    
    protected ReadableByteChannel getTestResourceChannel(String name) {
        return java.nio.channels.Channels.newChannel(getTestResourceAsStream(name));
    }
    
    protected ReadableByteChannel getReadableFileChannel(String name) throws IOException {
        URL resource = getTestResource(name);
        File f = new File(URLDecoder.decode(resource.getPath(),"UTF-8"));
        return new FileInputStream(f).getChannel();
    }
    
    protected Feature firstFeature(FeatureCollection fc) {
        return fc.features().next();
    }
    
    public static Test suite(Class c) {
        return new TestSuite(c);
    }
    
    protected File getTempFile() throws IOException {
        
        File tmpFile = File.createTempFile("test-shp",".shp");
        // keep track of all temp files so we can delete them
        tmpFiles.add(tmpFile);
        try {
            tmpFile.createNewFile();
        } catch (IOException ioe) {
            throw new RuntimeException("Couldn't setup temp file",ioe);
        }
        if (!tmpFile.exists())
            throw new RuntimeException("Couldn't setup temp file");
        tmpFile.deleteOnExit();
        return tmpFile;
    }
    
    
    
}
