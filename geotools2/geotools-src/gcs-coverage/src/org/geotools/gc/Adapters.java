/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Centre for Computational Geography
 * (C) 2002, Institut de Recherche pour le Développement
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
 */
package org.geotools.gc;

// J2SE dependencies
import java.io.IOException;
import java.rmi.RemoteException;
import java.awt.image.RenderedImage;

// JAI dependencies
import javax.media.jai.PlanarImage;
import javax.media.jai.PropertySource;
import javax.media.jai.Interpolation;
import javax.media.jai.InterpolationNearest;
import javax.media.jai.RenderedImageAdapter;
import javax.media.jai.remote.SerializableRenderedImage;

// OpenGIS dependencies
import org.opengis.gc.GC_GridRange;
import org.opengis.gc.GC_GridGeometry;
import org.opengis.gc.GC_GridCoverage;
import org.opengis.cv.CV_Coverage;

// Geotools dependencies
import org.geotools.cv.Coverage;
import org.geotools.cv.SampleDimension;
import org.geotools.cs.CoordinateSystem;


/**
 * <FONT COLOR="#FF6633">Provide methods for interoperability with
 * <code>org.opengis.gc</code> package.</FONT>  All methods accept
 * null argument. This class has no default instance, since the
 * {@link org.geotools.gp.Adapters org.geotools.<strong>gp</strong>.Adapters}
 * implementation cover this case.
 *
 * @version $Id: Adapters.java,v 1.3 2002/10/16 22:32:19 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @see org.geotools.gp.Adapters#getDefault()
 */
public class Adapters extends org.geotools.cv.Adapters {
    /**
     * Default constructor. A shared instance of <code>Adapters</code> can
     * be obtained with {@link org.geotools.gp.Adapters#getDefault()}.
     *
     * @param CS The underlying adapters from the <code>org.geotools.ct</code> package.
     */
    protected Adapters(final org.geotools.ct.Adapters CT) {
        super(CT);
    }

    /**
     * Returns an OpenGIS interface for a grid range.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public GC_GridRange export(final GridRange range) {
        if (range == null) {
            return null;
        }
        return (GC_GridRange) range.toOpenGIS(this);
    }

    /**
     * Returns an OpenGIS interface for a grid geometry.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public GC_GridGeometry export(final GridGeometry geometry) {
        if (geometry == null) {
            return null;
        }
        return (GC_GridGeometry) geometry.toOpenGIS(this);
    }

    /**
     * Returns an OpenGIS interface for a grid coverage.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    public GC_GridCoverage export(final GridCoverage coverage) {
        return (GC_GridCoverage) super.export(coverage);
    }

    /**
     * Performs the wrapping of a Geotools object. This method is invoked by
     * {@link #export(Coverage)} and {@link #export(GridCoverage)} if an
     * OpenGIS object is not already presents in the cache.
     *
     * @param  The Geotools object.
     * @return The OpenGIS  object. 
     */
    protected CV_Coverage doExport(final Coverage coverage) {
        if (coverage instanceof GridCoverage) {
            return ((GridCoverage) coverage).new Export(this);
        } else {
            return super.doExport(coverage);
        }
    }

    /**
     * Returns a grid range from an OpenGIS's interface.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     * @throws RemoteException if an operation failed while querying the OpenGIS object.
     */
    public GridRange wrap(final GC_GridRange range) throws RemoteException {
        if (range == null) {
            return null;
        }
        if (range instanceof GridRange.Export) {
            return ((GridRange.Export) range).unwrap();
        }
        final GridRange result = new GridRange(range.getLo(), range.getHi());
        result.proxy = range;
        return result;
    }

    /**
     * Returns a grid geometry from an OpenGIS's interface.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     * @throws RemoteException if an operation failed while querying the OpenGIS object.
     */
    public GridGeometry wrap(final GC_GridGeometry geometry) throws RemoteException {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof GridGeometry.Export) {
            return ((GridGeometry.Export) geometry).unwrap();
        }
        final GridGeometry result = new GridGeometry(wrap(geometry.getGridRange()),
                                        CT.wrap(geometry.getGridToCoordinateSystem()));
        result.proxy = geometry;
        return result;
    }

    /**
     * Returns a grid coverage from an OpenGIS's interface.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     * @throws IOException if an operation failed while querying the OpenGIS object.
     *         <code>IOException</code> is declared instead of {@link RemoteException}
     *         because the {@link GridCoverage} implementation may needs to open a
     *         socket connection in order to send image data through the network.
     */
    public GridCoverage wrap(final GC_GridCoverage coverage) throws IOException {
        return (GridCoverage) super.wrap(coverage);
    }

    /**
     * Performs the wrapping of an OpenGIS's interface. This method is invoked by
     * {@link #wrap(CV_Coverage)} and {@link #wrap(GC_GridCoverage)} if a Geotools
     * object is not already presents in the cache.
     *
     * @param  The OpenGIS  object.
     * @return The Geotools object. 
     * @throws IOException if an operation failed while querying the OpenGIS object.
     *         <code>IOException</code> is declared instead of {@link RemoteException}
     *         because the {@link GridCoverage} implementation may needs to open a
     *         socket connection in order to send image data through the network.
     *
     * @task REVISIT: What to do with interpolation? We can query the interpolation with
     *                <code>GridCoverage.Renderable.getInterpolation()</code> and invoke
     *                <code>GridCoverageProcessor.doOperation("Interpolate", ...),   but
     *                the later would introduce a dependency to the "gp" package. An other
     *                solution is to override this method in <code>org.geotools.gp.Adapters</code>.
     *
     * @task TODO: Implement a {@link RenderedImage} constructing tiles uppon request
     *             by invoking {@link GC_GridCoverage#getPackedDataBlock}. It would be
     *             used when a {@link SerializableRenderedImage} is not available.
     */
    protected Coverage doWrap(final CV_Coverage coverage) throws IOException {
        if (coverage instanceof GC_GridCoverage) {
            final GC_GridCoverage   grid  = (GC_GridCoverage) coverage;
            final SampleDimension[] bands = new SampleDimension[grid.getNumSampleDimensions()];
            for (int i=0; i<bands.length; i++) {
                bands[i] = wrap(grid.getSampleDimension(i));
            }
            final RenderedImage image;
            if (coverage instanceof GridCoverage.Renderable) {
                image = ((GridCoverage.Renderable) coverage).getRenderedImage();
            } else {
                // Implementing the general case is possible using
                // CV_GridCoverage.getPackedDataBlock(...), but it
                // would be a lot of work.
                throw new UnsupportedOperationException("Not yet implemented");
            }
            final GridCoverage gridCoverage = new GridCoverage(
                    bands[0].getDescription(null),
                    new ImageProxy(image, getPropertySource(grid)),
                    CS.wrap(grid.getCoordinateSystem()),
                    wrap(grid.getGridGeometry()).getGridToCoordinateSystem(),
                    bands,
                    null,   // Sources GridCoverage (ignored)
                    null);  // Map of properties (ignored)
            return gridCoverage;
        } else {
            return super.doWrap(coverage);
        }
    }

    /**
     * A wrapper for a non-writable {@link RenderedImage}. The tile layout, sample model,
     * and so forth are preserved.  Calls to {@link #getTile()}, {@link #getData()}, and
     * {@link #copyData()} are forwarded to the image being adapted. The source image is
     * assumed to be immutable. If the image source implements {@link WritableRenderedImage},
     * a {@link javax.media.jai.WritableRenderedImageAdapter} should be used.
     * <br><br>
     * If the wrapped image is an instance of {@link SerializableRenderedImage}, then
     * invoking {@link #dispose} will also dispose the serializable image,  which may
     * close socket connection.
     *
     * @version $Id: Adapters.java,v 1.3 2002/10/16 22:32:19 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    private static final class ImageProxy extends RenderedImageAdapter {
        /**
         * Construct a {@link RenderedImage} adapter.
         *
         * @param  image  A {@link RenderedImage} to be wrapped as a {@link PlanarImage}.
         * @param  source Properties to add to this image (may be null). The actual property
         *         values are not requested at this time but instead an entry for the name of
         *         each property emitted by the {@link PropertySource} is added to the
         *         <code>name-PropertySource</code> mapping.
         * @throws IOException If a Remote Method Invocation failed.
         */
        public ImageProxy(RenderedImage image, PropertySource source) throws RemoteException {
            super(image);
            try {
                properties.addProperties(source);
            } catch (RuntimeException exception) {
                final Throwable cause = exception.getCause();
                if (cause instanceof RemoteException) {
                    // May occurs when the WritablePropertySourceImpl class
                    // fetch values from the CoverageProperties adapter.
                    throw (RemoteException) cause;
                }
                throw exception;
            }
        }

        /**
         * Provides a hint that an image will no longer be accessed from a reference in
         * user space. The results are equivalent to those that occur when the program
         * loses its last reference to this image, the garbage collector discovers this,
         * and finalize is called. This can be used as a hint in situations where waiting
         * for garbage collection would be overly conservative. The results of referencing
         * an image after a call to <code>dispose()</code> are undefined.
         */
        public void dispose() {
            super.dispose();
            if (theImage instanceof PlanarImage) {
                ((PlanarImage) theImage).dispose();
            } else if (theImage instanceof SerializableRenderedImage) {
                ((SerializableRenderedImage) theImage).dispose();
            }
        }
    }
}
