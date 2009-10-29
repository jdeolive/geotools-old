package org.geotools.arcsde.gce;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageReaderSpi;

import com.sun.media.imageioimpl.plugins.raw.RawImageReader;

public class ArcSDEImageReader extends RawImageReader {

    protected ArcSDEImageReader(final ImageReaderSpi originatingProvider) {
        super(originatingProvider);
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        int height = super.getHeight(imageIndex);
        return height;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        IIOMetadata imageMetadata = super.getImageMetadata(imageIndex);
        return imageMetadata;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        Iterator imageTypes = super.getImageTypes(imageIndex);
        return imageTypes;
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        int numImages = super.getNumImages(allowSearch);
        return numImages;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        IIOMetadata streamMetadata = super.getStreamMetadata();
        return streamMetadata;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        int width = super.getWidth(imageIndex);
        return width;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        BufferedImage read = super.read(imageIndex, param);
        return read;
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        seekForwardOnly = true;
        super.setInput(input, seekForwardOnly, ignoreMetadata);
    }

    @Override
    public boolean isSeekForwardOnly() {
        boolean seekForwardOnly = super.isSeekForwardOnly();
        return seekForwardOnly ? seekForwardOnly : true;
    }
}
