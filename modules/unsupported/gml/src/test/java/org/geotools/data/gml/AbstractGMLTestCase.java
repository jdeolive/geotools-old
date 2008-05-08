package org.geotools.data.gml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

import org.geotools.TestData;

import junit.framework.TestCase;

public class AbstractGMLTestCase extends TestCase {

	protected File gmlFile;
	
    protected void setUp() throws Exception {
        super.setUp();
        synchronized (this) {
        	URL resource = getClass().getResource("test-data/placeholder");
			String path = resource.getFile();
			File f=new File(path);
        	gmlFile = new File(f.getParent()+"/Streams.gml");
        	gmlFile.createNewFile();
        	gmlFile.deleteOnExit();
        	copy(TestData.url("xml/gml/Streams.gml"), gmlFile);
		}

    }

    protected void tearDown() throws Exception {
    	synchronized (this) {
    		gmlFile.delete();
		}
    }

    void copy(URL src, File dst) throws IOException {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = src.openStream();
            out = new FileOutputStream(dst);

            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
        } finally {
            if (in != null) {
                in.close();
            }

            if (out != null) {
                out.close();
            }
        }
    }

}
