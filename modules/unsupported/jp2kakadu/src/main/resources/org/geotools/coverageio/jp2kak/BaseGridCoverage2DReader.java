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
package org.geotools.coverageio.jp2kak;

import it.geosolutions.imageio.imageioimpl.imagereadmt.ImageReadDescriptorMT;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.media.jai.JAI;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.DataSourceException;
import org.geotools.data.DefaultResourceInfo;
import org.geotools.data.DefaultServiceInfo;
import org.geotools.data.PrjFileReader;
import org.geotools.data.ResourceInfo;
import org.geotools.data.ServiceInfo;
import org.geotools.data.WorldFileReader;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.LinearTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.coverage.CoverageUtilities;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.coverage.grid.GridRange;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

/**
 * Base class for GridCoverage data access
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
@SuppressWarnings("deprecation")
public abstract class BaseGridCoverage2DReader extends
        AbstractGridCoverage2DReader implements GridCoverageReader {

    /** Logger. */
    private final static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger("org.geotools.coverageio");

    /** registering ImageReadMT JAI operation (for multithreaded ImageRead) */
    static {
        ImageReadDescriptorMT.register(JAI.getDefaultInstance());
    }

    // ////////////////////////////////////////////////////////////////////////
    //    
    // Constant fields
    //    
    // ////////////////////////////////////////////////////////////////////////
    /** The default world file extension */
    private final static String DEFAULT_WORLDFILE_EXT = ".wld";

    /** The system-dependent default name-separator character. */
    private final static char SEPARATOR = File.separatorChar;

    /**
     * The format specific world file extension. As an instance, world file
     * related to ecw datasets have .eww extension while mrsid world file are
     * .sdw
     */
    private final String worldFileExt;

    /** Caches an {@code ImageReaderSpi}. */
    private final ImageReaderSpi readerSPI;

    /**
     * Implement this method to setup the coverage properties (Envelope, CRS,
     * GridRange) using the provided {@code ImageReader}
     */
    protected abstract void setCoverageProperties(ImageReader reader)
            throws IOException;

    // ////////////////////////////////////////////////////////////////////////
    //  
    // Referencing related fields (CRS and Envelope)
    //  
    // ////////////////////////////////////////////////////////////////////////

    /** Envelope read from file */
    private GeneralEnvelope coverageEnvelope = null;

    /** The CRS for the coverage */
    private CoordinateReferenceSystem coverageCRS = null;

    // ////////////////////////////////////////////////////////////////////////
    //  
    // Data source properties and field for management
    //  
    // ////////////////////////////////////////////////////////////////////////

    /** Source given as input to the reader */
    private File inputFile = null;

    /** Coverage name */
    private String coverageName = "geotools_coverage";

    /**
     * The base {@link GridRange} for the {@link GridCoverage2D} of this reader.
     */
    private GeneralGridRange coverageGridRange = null;

    /** Absolute path to the parent dir for this coverage. */
    private String parentPath;

    /**
     * Creates a new instance of a {@link BaseGridCoverage2DReader}. I assume
     * nothing about file extension.
     * 
     * @param input
     *                Source object for which we want to build a
     *                {@link BaseGridCoverage2DReader}.
     * @param hints
     *                Hints to be used by this reader throughout his life.
     * @param worldFileExtension
     *                the specific world file extension of the underlying format
     * @param formatSpecificSpi
     *                an instance of a proper {@code ImageReaderSpi}.
     * @throws DataSourceException
     */
    protected BaseGridCoverage2DReader(Object input, final Hints hints,
            final String worldFileExtension,
            final ImageReaderSpi formatSpecificSpi) throws DataSourceException {

        try {
            // //
            //
            // managing hints
            //
            // //
            if (this.hints == null)
                this.hints = new Hints();

            if (hints != null)
                this.hints.add(hints);

            this.coverageFactory = CoverageFactoryFinder
                    .getGridCoverageFactory(this.hints);

            readerSPI = formatSpecificSpi;
            worldFileExt = worldFileExtension;

            // //
            //
            // Source management
            //
            // //
            checkSource(input);
            final ImageReader reader = readerSPI.createReaderInstance();
            reader.setInput(inputFile);

            // //
            //
            // Setting Envelope, GridRange and CRS
            //
            // //
            setCoverageProperties(reader);

            // //
            //
            // Information about multiple levels and such
            //
            // //
            getResolutionInfo(reader);

            // //
            //
            // Reset and dispose reader
            //
            // //
            reader.reset();
            reader.dispose();
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);

            throw new DataSourceException(e);
        } catch (TransformException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);

            throw new DataSourceException(e);
        }
    }

    /** Package scope highest resolution info accessor */
    double[] getHighestRes() {
        return highestRes;
    }

    /** Package scope hints accessor */
    Hints getHints() {
        return hints;
    }

    /** Package scope grid to world transformation accessor */
    MathTransform getRaster2Model() {
        return raster2Model;
    }

    /**
     * Checks the input provided to this {@link BaseGridCoverage2DReader} and
     * sets all the other objects and flags accordingly.
     * 
     * @param input
     *                provided to this {@link BaseGridCoverage2DReader}.
     *                Actually supported input types are: {@code File}, {@code URL}
     *                pointing to a file and {@link FileImageInputStreamExt}
     * @param hints
     *                Hints to be used by this reader throughout his life.
     * 
     * @throws UnsupportedEncodingException
     * @throws DataSourceException
     * @throws IOException
     * @throws FileNotFoundException
     */
    private void checkSource(Object input) throws UnsupportedEncodingException,
            DataSourceException, IOException, FileNotFoundException {
        if (input == null) {
            final DataSourceException ex = new DataSourceException(
                    "No source set to read this coverage.");

            if (LOGGER.isLoggable(Level.SEVERE)) {
                LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
            }

            throw ex;
        }

        this.source = null;

        // //
        //
        // URL to FIle
        //
        // //
        // if it is an URL pointing to a File I convert it to a file.
        if (input instanceof URL) {
            // URL that point to a file
            final URL sourceURL = ((URL) input);
            this.source = sourceURL;

            if (sourceURL.getProtocol().compareToIgnoreCase("file") == 0) {
                this.inputFile = new File(URLDecoder.decode(
                        sourceURL.getFile(), "UTF-8"));
                input = this.inputFile;
            } else {
                throw new IllegalArgumentException("Unsupported input type");
            }
        }

        if (input instanceof FileImageInputStreamExt) {
            if (source == null) {
                source = input;
            }

            inputFile = ((FileImageInputStreamExt) input).getFile();
            input = inputFile;
        }

        // //
        //        
        // File
        //        
        // //
        if (input instanceof File) {
            final File sourceFile = (File) input;

            if (source == null) {
                source = sourceFile;
            }

            if (inputFile == null) {
                inputFile = sourceFile;
            }

            if (!sourceFile.exists() || sourceFile.isDirectory()
                    || !sourceFile.canRead()) {
                throw new DataSourceException(
                        "Provided file does not exist or is a directory or is not readable!");
            }

            this.parentPath = sourceFile.getParent();
            coverageName = sourceFile.getName();

            final int dotIndex = coverageName.indexOf(".");
            coverageName = (dotIndex == -1) ? coverageName : coverageName
                    .substring(0, dotIndex);

        } else {
            throw new IllegalArgumentException("Unsupported input type");
        }
    }

    /**
     * Gets resolution information about the coverage itself.
     * 
     * @param reader
     *                an {@link ImageReader} to use for getting the resolution
     *                information.
     * @throws IOException
     * @throws TransformException
     * @throws DataSourceException
     */
    private void getResolutionInfo(ImageReader reader) throws IOException,
            TransformException {
        // //
        //
        // get the dimension of the high resolution image and compute the
        // resolution
        //
        // //
        final Rectangle originalDim = new Rectangle(0, 0, reader.getWidth(0),
                reader.getHeight(0));

        if (getCoverageGridRange() == null) {
            setCoverageGridRange(new GeneralGridRange(originalDim));
        }

        // ///
        //
        // setting the higher resolution available for this coverage
        //
        // ///
        highestRes = CoverageUtilities
                .getResolution((AffineTransform) raster2Model);

        if (LOGGER.isLoggable(Level.FINE))
            LOGGER
                    .fine(new StringBuffer("Highest Resolution = [").append(
                            highestRes[0]).append(",").append(highestRes[1])
                            .toString());
    }

    /**
     * Returns a {@link GridCoverage} from this reader in compliance with the
     * specified parameters.
     * 
     * @param params
     *                a {@code GeneralParameterValue} array to customize the
     *                read operation.
     */
    public GridCoverage read(GeneralParameterValue[] params)
            throws IllegalArgumentException, IOException {

        // Setup a new coverage request
        final CoverageRequest request = new CoverageRequest(params);
        request.setBaseParameters(this);

        // compute the request.
        return (GridCoverage2D) requestCoverage(request).getGridCoverage();
    }

    /**
     * Gets the coordinate reference system that will be associated to the
     * {@link GridCoverage} by looking for a related PRJ.
     */
    protected void parsePRJFile() {
        String prjPath = null;

        setCoverageCRS(null);
        prjPath = new StringBuilder(this.parentPath).append(File.separatorChar)
                .append(coverageName).append(".prj").toString();

        // read the prj info from the file
        PrjFileReader projReader = null;

        try {
            final File prj = new File(prjPath);

            if (prj.exists()) {
                projReader = new PrjFileReader(new FileInputStream(prj)
                        .getChannel());
                setCoverageCRS(projReader.getCoordinateReferenceSystem());
            }
            // If some exception occurs, warn about the error but proceed
            // using a default CRS
        } catch (FileNotFoundException e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            }
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            }
        } catch (FactoryException e) {
            if (LOGGER.isLoggable(Level.WARNING)) {
                LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
            }
        } finally {
            if (projReader != null) {
                try {
                    projReader.close();
                } catch (IOException e) {
                    if (LOGGER.isLoggable(Level.WARNING)) {
                        LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
                    }
                }
            }
        }
    }

    /**
     * Checks whether a world file is associated with the data source. If found,
     * set a proper envelope.
     * 
     * @throws IllegalStateException
     * @throws IOException
     */
    protected void parseWorldFile() throws IOException {
        final String worldFilePath = new StringBuffer(this.parentPath).append(
                SEPARATOR).append(coverageName).toString();

        File file2Parse = null;
        boolean worldFileExists = false;
        // //
        //
        // Check for a world file with the format specific extension
        //
        // //
        if (worldFileExt != null && worldFileExt.length() > 0) {
            file2Parse = new File(worldFilePath + worldFileExt);
            worldFileExists = file2Parse.exists();
        }

        // //
        //
        // Check for a world file with the default extension
        //
        // //
        if (!worldFileExists) {
            file2Parse = new File(worldFilePath + DEFAULT_WORLDFILE_EXT);
            worldFileExists = file2Parse.exists();
        }

        if (worldFileExists) {
            final WorldFileReader reader = new WorldFileReader(file2Parse);
            raster2Model = reader.getTransform();

            // //
            //
            // In case we read from a real world file we have together the
            // envelope. World file transformation assumes to work in the
            // CELL_CENTER condition
            //
            // //
            final AffineTransform tempTransform = new AffineTransform(
                    (AffineTransform) raster2Model);
            tempTransform.translate(-0.5, -0.5);

            try {
                final LinearTransform gridToWorldTransform = ProjectiveTransform
                        .create(tempTransform);
                final Envelope gridRange = new GeneralEnvelope(
                        getCoverageGridRange().toRectangle());
                final GeneralEnvelope coverageEnvelope = CRS.transform(
                        gridToWorldTransform, gridRange);
                setCoverageEnvelope(coverageEnvelope);
            } catch (TransformException e) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
                }
            } catch (IllegalStateException e) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, e.getLocalizedMessage(), e);
                }
            }
        }
    }

    /**
     * Information about this source. Subclasses should provide additional
     * format specific information.
     * 
     * @return ServiceInfo describing getSource().
     */
    public ServiceInfo getInfo() {
        DefaultServiceInfo info = new DefaultServiceInfo();
        info.setDescription(source.toString());

        if (source instanceof URL) {
            URL url = (URL) source;
            info.setTitle(url.getFile());

            try {
                info.setSource(url.toURI());
            } catch (URISyntaxException e) {
            }
        } else if (source instanceof File) {
            File file = (File) source;
            String filename = file.getName();

            if ((filename == null) || (filename.length() == 0)) {
                info.setTitle(file.getName());
            }

            info.setSource(file.toURI());
        }

        return info;
    }

    /**
     * Information about the named gridcoverage.
     * 
     * @param subname
     *                Name indicing grid coverage to describe
     * @return ResourceInfo describing grid coverage indicated
     */
    public ResourceInfo getInfo(String subname) {
        DefaultResourceInfo info = new DefaultResourceInfo();
        info.setName(subname);
        info.setBounds(new ReferencedEnvelope(this.getOriginalEnvelope()));
        info.setCRS(this.getCrs());
        info.setTitle(subname);

        return info;
    }

    /**
     * Returns a {@link CoverageResponse} from the specified
     * {@link CoverageRequest}.
     * 
     * @param request
     *                a previously set {@link CoverageRequest} defining a set of
     *                parameters to get a specific coverage
     * @return the computed {@code CoverageResponse}
     * @todo Future versions may cache requestes<->responses using hashing
     */
    private CoverageResponse requestCoverage(CoverageRequest request) {
        final CoverageResponse response = new CoverageResponse(request,
                coverageFactory, readerSPI);
        try {
            response.compute();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        }
        return response;
    }

    /**
     * @param coverageCRS
     *                the coverageCRS to set
     */
    protected void setCoverageCRS(CoordinateReferenceSystem coverageCRS) {
        this.coverageCRS = coverageCRS;
    }

    /**
     * @return the coverageCRS
     */
    protected CoordinateReferenceSystem getCoverageCRS() {
        return coverageCRS;
    }

    /**
     * @param coverageEnvelope
     *                the coverageEnvelope to set
     */
    protected void setCoverageEnvelope(GeneralEnvelope coverageEnvelope) {
        this.coverageEnvelope = coverageEnvelope;
    }

    /**
     * @return the coverageEnvelope
     */
    protected GeneralEnvelope getCoverageEnvelope() {
        return coverageEnvelope;
    }

    /**
     * @param coverageGridRange
     *                the coverageGridRange to set
     */
    protected void setCoverageGridRange(GeneralGridRange coverageGridRange) {
        this.coverageGridRange = coverageGridRange;
    }

    /**
     * @return the coverageGridRange
     */
    protected GeneralGridRange getCoverageGridRange() {
        return coverageGridRange;
    }

    /**
     * @return the input file
     */
    protected File getInputFile() {
        return inputFile;
    }

    /**
     * @return the coverage name
     */
    public String getCoverageName() {
        return coverageName;
    }
}