package org.geotools.process.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class Utilities {

    private static int DEFAULT_SIZE = 1024 * 1024;

    public final static String GS_URL = "geoserver_base_url";

    public final static String GS_UID = "geoserver_uid";

    public final static String GS_PWD = "geoserver_pwd";
    
    public final static String OUTPUT_DIR = "output_dir";
    
    public final static String FILE_SEPARATOR = System.getProperty("file.separator");

    /**
     * Read input stream reader
     * @param is
     * @return
     */
    public static String readIs(InputStreamReader is) {
        char[] inCh = new char[1024];
        StringBuffer input = new StringBuffer();
        int r;

        try {
            while ((r = is.read(inCh)) > 0) {
                input.append(inCh, 0, r);
            }
        } catch (IOException e) {
            // if(LOGGER.isLoggable(Level.SEVERE))
            // LOGGER.log(Level.SEVERE,e.getLocalizedMessage(),e);
        }

        return input.toString();
    }

    /**
     * Copy {@link InputStream} to {@link OutputStream}.
     * 
     * @param sourceStream
     *                {@link InputStream} to copy from.
     * @param destinationStream
     *                {@link OutputStream} to copy to.
     * @param closeInput
     *                quietly close {@link InputStream}.
     * @param closeOutput
     *                quietly close {@link OutputStream}
     * @throws IOException
     *                 in case something bad happens.
     */
    public static void copyStream(InputStream sourceStream,
            OutputStream destinationStream, boolean closeInput,
            boolean closeOutput) throws IOException {
        copyStream(sourceStream, destinationStream, DEFAULT_SIZE, closeInput,
                closeOutput);
    }

    /**
     * Copy {@link InputStream} to {@link OutputStream}.
     * 
     * @param sourceStream
     *                {@link InputStream} to copy from.
     * @param destinationStream
     *                {@link OutputStream} to copy to.
     * @param size
     *                size of the buffer to use internally.
     * @param closeInput
     *                quietly close {@link InputStream}.
     * @param closeOutput
     *                quietly close {@link OutputStream}
     * @throws IOException
     *                 in case something bad happens.
     * 
     */
    public static void copyStream(InputStream sourceStream,
            OutputStream destinationStream, int size, boolean closeInput,
            boolean closeOutput) throws IOException {

        inputNotNull(sourceStream, destinationStream);
        byte[] buf = new byte[size];
        int n = -1;
        try {
            while (-1 != (n = sourceStream.read(buf))) {
                destinationStream.write(buf, 0, n);
                destinationStream.flush();
            }
        } finally {
            // closing streams and connections
            try {
                destinationStream.flush();
            } finally {
                try {
                    if (closeOutput)
                        destinationStream.close();
                } finally {
                    try {
                        if (closeInput)
                            sourceStream.close();
                    } finally {

                    }
                }
            }
        }
    }

    /**
     * Checks if the input is not null.
     * 
     * @param oList
     *                list of elements to check for null.
     */
    private static void inputNotNull(Object... oList) {
        for (Object o : oList)
            if (o == null)
                throw new NullPointerException("Input objects cannot be null");

    }

    private Utilities() {

    }
}
