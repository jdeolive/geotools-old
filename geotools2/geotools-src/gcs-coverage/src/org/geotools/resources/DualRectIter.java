/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2001, Institut de Recherche pour le Développement
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 *
 *    This package contains documentation from OpenGIS specifications.
 *    OpenGIS consortium's work is fully acknowledged here.
 */
package org.geotools.resources;

// J2SE dependencies
import java.awt.image.RasterFormatException;

// JAI dependencies
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.WritableRectIter;


/**
 * A {@link WritableRectIter} that take source from {@link RectIter} and write to an other
 * {@link WritableRectIter}. This class is useful for implementing {@link PlanarImage#computeRect}
 * methods.
 *
 * @version $Id: DualRectIter.java,v 1.1 2002/07/23 17:53:37 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public final class DualRectIter implements WritableRectIter {
    /**
     * The string for error message.
     */
    private static final String ERROR = "Size mismatch";

    /**
     * The source.
     */
    private final RectIter src;

    /**
     * The destination.
     */
    private final WritableRectIter dst;

    /**
     * Construct a TransferRectIter object.
     */
    private DualRectIter(final RectIter src, final WritableRectIter dst) {
        this.src = src;
        this.dst = dst;
    }

    /**
     * Create a {@link WritableRectIter} for the specified source and destination iterator.
     * The two iterators must iterate over a rectangle of the same size, otherwise a
     * {@link RasterFormatException} may be thrown during the iteration.
     *
     * @param  src The source iterator.
     * @param  dst The destination iterator.
     * @return An iterator that read sample from <code>src</code> and write sample
     *         to <code>dst</code>. If <code>src==dst</code>, then the destination
     *         iterator itself is returned.
     */
    public static WritableRectIter create(final RectIter src, final WritableRectIter dst) {
        if (src == dst) {
            return dst;
        }
        return new DualRectIter(src, dst);
    }

    /**
     * Sets the iterator to the first line of its bounding rectangle.
     */
    public void startLines() {
        src.startLines();
        dst.startLines();
    }

    /**
     * Sets the iterator to the leftmost pixel of its bounding rectangle.
     */
    public void startPixels() {
        src.startPixels();
        dst.startPixels();
    }

    /**
     * Sets the iterator to the first band of the image.
     */
    public void startBands() {
        src.startBands();
        dst.startBands();
    }

    /**
     * Jumps downward num lines from the current position.
     */
    public void jumpLines(int num) {
        src.jumpLines(num);
        dst.jumpLines(num);
    }

    /**
     * Jumps rightward num pixels from the current position.
     */
    public void jumpPixels(int num) {
        src.jumpPixels(num);
        dst.jumpPixels(num);
    }

    /**
     * Sets the iterator to the next line of the image.
     */
    public void nextLine() {
        src.nextLine();
        dst.nextLine();
    }

    /**
     * Sets the iterator to the next pixel in image (that is, move rightward).
     */
    public void nextPixel() {
        src.nextPixel();
        dst.nextPixel();
    }

    /**
     * Sets the iterator to the next band in the image.
     */
    public void nextBand() {
        src.nextBand();
        dst.nextBand();
    }

    /**
     * Sets the iterator to the next line in the image,
     * and returns true if the bottom row of the bounding rectangle has been passed.
     */
    public boolean nextLineDone() {
        boolean check=src.nextLineDone();
        if (check ==  dst.nextLineDone()) {
            return check;
        }
        throw new RasterFormatException(ERROR);
    }

    /**
     * Sets the iterator to the next pixel in the image (that is, move rightward).
     */
    public boolean nextPixelDone() {
        boolean check=src.nextPixelDone();
        if (check ==  dst.nextPixelDone()) {
            return check;
        }
        throw new RasterFormatException(ERROR);
    }

    /**
     * Sets the iterator to the next band in the image,
     * and returns true if the max band has been exceeded.
     */
    public boolean nextBandDone() {
        boolean check=src.nextBandDone();
        if (check ==  dst.nextBandDone()) {
            return check;
        }
        throw new RasterFormatException(ERROR);
    }


    /**
     * Returns true if the bottom row of the bounding rectangle has been passed.
     */
    public boolean finishedLines() {
        boolean check=src.finishedLines();
        if (check ==  dst.finishedLines()) {
            return check;
        }
        throw new RasterFormatException(ERROR);
    }

    /**
     * Returns true if the right edge of the bounding rectangle has been passed.
     */
    public boolean finishedPixels() {
        boolean check=src.finishedPixels();
        if (check ==  dst.finishedPixels()) {
            return check;
        }
        throw new RasterFormatException(ERROR);
    }

    /**
     * Returns true if the max band in the image has been exceeded.
     */
    public boolean finishedBands() {
        boolean check=src.finishedBands();
        if (check ==  dst.finishedBands()) {
            return check;
        }
        throw new RasterFormatException(ERROR);
    }

    /**
     * Returns the samples of the current pixel from the image in an array of int.
     */
    public int[] getPixel(int[] array) {
        return src.getPixel(array);
    }

    /**
     * Returns the samples of the current pixel from the image in an array of float.
     */
    public float[] getPixel(float[] array) {
        return src.getPixel(array);
    }

   /**
    * Returns the samples of the current pixel from the image in an array of double.
    */
    public double[] getPixel(double[] array) {
        return src.getPixel(array);
    }

    /**
     * Returns the current sample as an integer.
     */
    public int getSample() {
        return src.getSample();
    }

    /**
     * Returns the specified sample of the current pixel as an integer.
     */
    public int getSample(int b) {
        return src.getSample(b);
    }

    /**
     * Returns the current sample as a float.
     */
    public float getSampleFloat() {
        return src.getSampleFloat();
    }

    /**
     * Returns the specified sample of the current pixel as a float.
     */
    public float getSampleFloat(int b) {
        return src.getSampleFloat(b);
    }

    /**
     * Returns the current sample as a double.
     */
    public double getSampleDouble() {
        return src.getSampleDouble();
    }

    /**
     * Returns the specified sample of the current pixel as a double.
     */
    public double getSampleDouble(int b) {
        return src.getSampleDouble(b);
    }

    /**
     * Sets all samples of the current pixel to a set of int values.
     */
    public void setPixel(int[] array) {
        dst.setPixel(array);
    }

    /**
     * Sets all samples of the current pixel to a set of float values.
     */
    public void setPixel(float[] array) {
        dst.setPixel(array);
    }

    /**
     * Sets all samples of the current pixel to a set of double values.
     */
    public void setPixel(double[] array) {
        dst.setPixel(array);
    }

    /**
     * Sets the current sample to an integral value.
     */
    public void setSample(int s) {
        dst.setSample(s);
    }

    /**
     * Sets the current sample to a float value.
     */
    public void setSample(float s) {
        dst.setSample(s);
    }

    /**
     * Sets the current sample to a double value.
     */
    public void setSample(double s) {
        dst.setSample(s);
    }

    /**
     * Sets the specified sample of the current pixel to an integral value.
     */
    public void setSample(int b, int s) {
        dst.setSample(b, s);
    }

    /**
     * Sets the specified sample of the current pixel to a float value.
     */
    public void setSample(int b, float s) {
        dst.setSample(b, s);
    }

    /**
     * Sets the specified sample of the current pixel to a double value.
     */
    public void setSample(int b, double s) {
        dst.setSample(b, s);
    }
}

