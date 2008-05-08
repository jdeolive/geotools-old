/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *    (C) 2001, Institut de Recherche pour le Développement
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
 */
package org.geotools.image.io;

import java.io.IOException;
import java.io.InputStream;
import javax.imageio.stream.ImageInputStream;


/**
 * Wraps an {@link ImageInputStream} into a standard {@link InputStream}.
 *
 * @source $URL$
 * @version $Id$
 * @author Martin Desruisseaux
 */
final class InputStreamAdapter extends InputStream {
    /**
     * The wrapped image input stream.
     */
    private final ImageInputStream input;

    /**
     * Constructs a new input stream.
     */
    public InputStreamAdapter(final ImageInputStream input) {
        this.input=input;
    }

    /**
     * Reads the next byte of data from the input stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    public int read() throws IOException {
        return input.read();
    }

    /**
     * Reads some number of bytes from the input stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read(final byte[] b) throws IOException {
        return input.read(b);
    }

    /**
     * Reads up to {@code len} bytes of data from the input stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public int read(final byte[] b, final int off, final int len) throws IOException {
        return input.read(b, off, len);
    }

    /**
     * Skips over and discards {@code n} bytes of data from this input stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public long skip(final long n) throws IOException {
        return input.skipBytes(n);
    }

    /**
     * Returns always {@code true}.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public boolean markSupported() {
        return true;
    }

    /**
     * Marks the current position in this input stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void mark(final int readlimit) {
        input.mark();
    }

    /**
     * Repositions this stream to the position at the time
     * the {@code mark} method was last called.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void reset() throws IOException {
        input.reset();
    }

    /**
     * Closes this input stream.
     *
     * @throws IOException if an I/O error occurs.
     */
    @Override
    public void close() throws IOException {
        input.close();
    }
}
