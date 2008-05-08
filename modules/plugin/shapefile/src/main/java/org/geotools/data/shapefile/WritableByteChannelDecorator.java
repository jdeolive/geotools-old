/**
 * 
 */
package org.geotools.data.shapefile;

import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * A WritableByteChannel that delegates all calls to the underlying
 * WritableByteChannel but for {@link #close()} it also calls
 * ShapefileFiles.unlock method to release the lock on the URL.
 * 
 * @author jesse
 */
public class WritableByteChannelDecorator implements WritableByteChannel {

    private final WritableByteChannel wrapped;
    private final ShpFiles shapefileFiles;
    private final URL url;
    private final FileWriter requestor;
    private boolean closed;

    public WritableByteChannelDecorator(WritableByteChannel newChannel,
            ShpFiles shapefileFiles, URL url, FileWriter requestor) {
        this.wrapped = newChannel;
        this.shapefileFiles = shapefileFiles;
        this.url = url;
        this.requestor = requestor;
        closed = false;
    }

    public int write(ByteBuffer src) throws IOException {
        return wrapped.write(src);
    }

    public void close() throws IOException {
        try {
            wrapped.close();
        } finally {
            if (!closed) {
                closed = true;
                shapefileFiles.unlockWrite(url, requestor);
            }
        }
    }

    public boolean isOpen() {
        return wrapped.isOpen();
    }

}
