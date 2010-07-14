/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.gce.imagemosaic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Testing multithreaded loader
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 * 
 */
@SuppressWarnings("unused")
public class ImageMosaicFormatFactoryTest extends Assert {

    @Test
    @Ignore
    public void testHomeLoaderUnbounded() throws IOException {

        final String homeDir = System.getProperty("user.home");
        assertNotNull(homeDir);

        final File file = new File(homeDir);
        assertTrue(file.exists() && file.isDirectory() && file.canWrite());

        // create file
        final File threadPoolFile = new File("mosaicthreadpoolconfig.properties");
        final Properties props = new Properties();
        props.setProperty("corePoolSize", "8");
        props.setProperty("maxPoolSize", "20");
        props.setProperty("keepAliveSeconds", "30");
        props.setProperty("queueType", "UNBOUNDED");

        // write down
        final FileOutputStream outStream = new FileOutputStream(threadPoolFile);
        try {
            props.store(outStream, null);
        } finally {
            IOUtils.closeQuietly(outStream);
        }

        // get mosaic factory

        final ImageMosaicFormatFactory spi = new ImageMosaicFormatFactory();
        final ThreadPoolExecutor thpe = (ThreadPoolExecutor) ImageMosaicFormatFactory.DefaultMultiThreadedLoader;

        assertEquals(props.getProperty("corePoolSize"), Integer.toString(thpe.getCorePoolSize()));
        assertEquals(props.getProperty("maxPoolSize"), Integer.toString(thpe.getMaximumPoolSize()));
        assertEquals(props.getProperty("keepAliveSeconds"),
                Long.toString(thpe.getKeepAliveTime(TimeUnit.SECONDS)));
        assertTrue("Wrong queue type "+thpe.getQueue().getClass().toString(),thpe.getQueue() instanceof LinkedBlockingQueue);
    }
    
    @Test
    @Ignore
    public void testHomeLoaderDirect() throws IOException {

        final String homeDir = System.getProperty("user.home");
        assertNotNull(homeDir);

        final File file = new File(homeDir);
        assertTrue(file.exists() && file.isDirectory() && file.canWrite());

        // create file
        final File threadPoolFile = new File("mosaicthreadpoolconfig.properties");
        final Properties props = new Properties();
        props.setProperty("corePoolSize", "8");
        props.setProperty("maxPoolSize", "22");
        props.setProperty("keepAliveSeconds", "30");
        props.setProperty("queueType", "DIRECT");

        // write down
        final FileOutputStream outStream = new FileOutputStream(threadPoolFile);
        try {
            props.store(outStream, null);
            
            

            // get mosaic factory

            final ImageMosaicFormatFactory spi = new ImageMosaicFormatFactory();
            final ThreadPoolExecutor thpe = (ThreadPoolExecutor) ImageMosaicFormatFactory.DefaultMultiThreadedLoader;

            assertEquals(props.getProperty("corePoolSize"), Integer.toString(thpe.getCorePoolSize()));
            assertEquals(props.getProperty("maxPoolSize"), Integer.toString(thpe.getMaximumPoolSize()));
            assertEquals(props.getProperty("keepAliveSeconds"),
                    Long.toString(thpe.getKeepAliveTime(TimeUnit.SECONDS)));
            assertTrue("Wrong queue type "+thpe.getQueue().getClass().toString(),thpe.getQueue() instanceof SynchronousQueue);            
        } finally {
            IOUtils.closeQuietly(outStream);
            file.delete();
        }

    }  
    
    @Test
    public void testSystemLoaderUnbounded() throws IOException {

        File file= org.geotools.TestData.file(this,"mosaicthreadpoolconfig.properties");
        System.setProperty("mosaic.threadpoolconfig.path",file.getAbsolutePath());
        final String dir =System.getProperty("mosaic.threadpoolconfig.path");
        assertNotNull(dir);
        file= new File(dir);

        assertTrue(file.exists() && file.isFile() && file.canRead());

        // read file
        final Properties props= new Properties();
        final FileInputStream inStream = new FileInputStream(file);
        try {
            props.load(inStream);
        } finally {
            IOUtils.closeQuietly(inStream);
        }

        // get mosaic factory

        final ImageMosaicFormatFactory spi = new ImageMosaicFormatFactory();
            final ThreadPoolExecutor thpe = (ThreadPoolExecutor) ImageMosaicFormatFactory.DefaultMultiThreadedLoader;

        assertEquals(props.getProperty("corePoolSize"), Integer.toString(thpe.getCorePoolSize()));
        assertEquals(props.getProperty("maxPoolSize"), Integer.toString(thpe.getMaximumPoolSize()));
        assertEquals(props.getProperty("keepAliveSeconds"),
                Long.toString(thpe.getKeepAliveTime(TimeUnit.SECONDS)));
        assertTrue("Wrong queue type "+thpe.getQueue().getClass().toString(),thpe.getQueue() instanceof LinkedBlockingQueue);
    }    
}
