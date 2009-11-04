package org.geotools.arcsde.raster.jai;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;

import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.media.jai.PlanarImage;

import org.geotools.arcsde.raster.io.TileReader;

public class ArcSDEImageReader extends ImageReader {

    private TileReader tileReader;

    private ImageTypeSpecifier typeSpec;

    public ArcSDEImageReader(final ImageTypeSpecifier typeSpec) {
        super(null);
        this.typeSpec = typeSpec;
    }

    @Override
    public int getWidth(int imageIndex) throws IOException {
        int w = tileReader.getTilesWide() * tileReader.getTileWidth();
        return w;
    }

    @Override
    public int getHeight(int imageIndex) throws IOException {
        int h = tileReader.getTilesHigh() * tileReader.getTileHeight();
        return h;
    }

    @Override
    public IIOMetadata getImageMetadata(int imageIndex) throws IOException {
        return null;
    }

    @Override
    public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) throws IOException {
        return Collections.singleton(typeSpec).iterator();
    }

    @Override
    public int getNumImages(boolean allowSearch) throws IOException {
        return 1;
    }

    @Override
    public IIOMetadata getStreamMetadata() throws IOException {
        return null;
    }

    @Override
    public RenderedImage readAsRenderedImage(int imageIndex, ImageReadParam param)
            throws IOException {
        // return read(imageIndex, param);

        checkIndex(imageIndex);
        clearAbortRequest();
        processImageStarted(0);

        RenderedImage image = new ArcSDETiledRenderedImage(tileReader, typeSpec);

        if (abortRequested())
            processReadAborted();
        else
            processImageComplete();

        return image;
    }

    @Override
    public BufferedImage read(int imageIndex, ImageReadParam param) throws IOException {
        if (param == null) {
            param = getDefaultReadParam();
        }

        checkIndex(imageIndex);
        clearAbortRequest();
        processImageStarted(imageIndex);

        BufferedImage bi = param.getDestination();

        RenderedImage rendered = readAsRenderedImage(imageIndex, param);
        final BufferedImage bufferedImage;
        if (bi == null) {
            bufferedImage = PlanarImage.wrapRenderedImage(rendered).getAsBufferedImage();
        } else {
            // REVISIT: this is too naive, though it doesn't look like being used
            AffineTransform identity = new AffineTransform();
            Graphics2D graphics = (Graphics2D) bi.getGraphics();
            graphics.drawRenderedImage(rendered, identity);
            graphics.dispose();
            bufferedImage = bi;
        }
        return bufferedImage;
    }

    private void checkIndex(int imageIndex) throws IOException {
        if (imageIndex < 0 || imageIndex >= getNumImages(true)) {
            throw new IndexOutOfBoundsException("Image index: " + imageIndex);
        }
    }

    @Override
    public void setInput(Object input, boolean seekForwardOnly, boolean ignoreMetadata) {
        this.tileReader = (TileReader) input;
    }
}
