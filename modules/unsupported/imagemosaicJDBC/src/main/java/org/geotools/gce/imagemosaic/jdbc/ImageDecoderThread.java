package org.geotools.gce.imagemosaic.jdbc;

import com.sun.media.jai.codec.ByteArraySeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;

import org.geotools.geometry.GeneralEnvelope;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import java.io.IOException;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.PlanarImage;


/**
 * This class is used for multithreaded decoding of the tiles read from the database.
 * <p>
 * Decoding errors result in a log message and the result is <code>null</code>.
 * <p>
 * Creating an ImageDocoderThread object with  a null or empty bytearray result in a <code>null</code> value for <code>g {@link #getBufferedImage()}</code>
 * <p>
 * @author mcr
 *
 */
class ImageDecoderThread extends AbstractThread {
    /** Logger. */
    protected final static Logger LOGGER = Logger.getLogger(ImageDecoderThread.class.getPackage()
                                                                                    .getName());
    private byte[] imageBytes;
    private String location;
    private GeneralEnvelope tileEnvelope;

    /**
     * @param bytes        the bytes to decode
     * @param location        the location name of the tile
     */
    ImageDecoderThread(byte[] bytes, String location,
        GeneralEnvelope tileEnvelope, Rectangle pixelDimension,
        GeneralEnvelope requestEnvelope, ImageLevelInfo levelInfo,
        LinkedBlockingQueue<Object> tileQueue, Config config) {
        super(pixelDimension, requestEnvelope, levelInfo, tileQueue, config);

        this.imageBytes = bytes;
        this.location = location;
        this.tileEnvelope = tileEnvelope;
    }

    /**
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        if ((imageBytes == null) || (imageBytes.length == 0)) { // nothing to do ?

            return;
        }

        try {
            SeekableStream stream = new ByteArraySeekableStream(imageBytes);
            String decoderName = null;

            for (String dn : ImageCodec.getDecoderNames(stream)) {
                decoderName = dn;

                break;
            }

            BufferedImage bufferedImage = null;
            BufferedImage clippedImage = null;

            ImageDecoder decoder = ImageCodec.createImageDecoder(decoderName,
                    stream, null);
            PlanarImage img = PlanarImage.wrapRenderedImage(decoder.decodeAsRenderedImage());
            bufferedImage = img.getAsBufferedImage();

            if (requestEnvelope.contains(tileEnvelope, true) == false) {
                GeneralEnvelope savedTileEnvelope = new GeneralEnvelope(tileEnvelope);
                tileEnvelope.intersect(requestEnvelope);

                double scaleX = savedTileEnvelope.getLength(0) / bufferedImage.getWidth();
                double scaleY = savedTileEnvelope.getLength(1) / bufferedImage.getHeight();
                int x = (int) (Math.round((tileEnvelope.getMinimum(0) -
                        savedTileEnvelope.getMinimum(0)) / scaleX));
                int y = (int) (Math.round((savedTileEnvelope.getMaximum(1) -
                        tileEnvelope.getMaximum(1)) / scaleY));
                int width = (int) (Math.round(bufferedImage.getWidth() / savedTileEnvelope.getLength(
                            0) * tileEnvelope.getLength(0)));
                int height = (int) (Math.round(bufferedImage.getHeight() / savedTileEnvelope.getLength(
                            1) * tileEnvelope.getLength(1)));

                if ((width > 0) && (height > 0)) {
                    clippedImage = new BufferedImage(width, height,
                            BufferedImage.TYPE_INT_ARGB);

                    Graphics2D g2D = (Graphics2D) clippedImage.getGraphics();
                    g2D.drawImage(bufferedImage,
                        AffineTransform.getTranslateInstance(-x, -y), null);
                    tileQueue.add(coverageFactory.create(location,
                            clippedImage, tileEnvelope));
                }
            } else {
                tileQueue.add(coverageFactory.create(location, bufferedImage,
                        tileEnvelope));
            }
        } catch (IOException ex) {
            LOGGER.severe("Decorde error for tile " + location);
            LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
        }
    }
}
