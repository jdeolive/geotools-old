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

import it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageReaderSpi;
import it.geosolutions.imageio.plugins.jp2k.JP2KStreamMetadata;
import it.geosolutions.imageio.plugins.jp2k.box.UUIDBox;
import it.geosolutions.imageio.plugins.jp2k.box.UUIDBoxMetadataNode;
import it.geosolutions.imageio.utilities.ImageIOUtilities;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.spi.ImageReaderSpi;

import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GeneralGridRange;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.AbstractGridFormat;
import org.geotools.coverage.grid.io.imageio.geotiff.GeoTiffIIOMetadataDecoder;
import org.geotools.coverage.grid.io.imageio.geotiff.GeoTiffMetadata2CRSAdapter;
import org.geotools.data.DataSourceException;
import org.geotools.data.PrjFileReader;
import org.geotools.data.WorldFileReader;
import org.geotools.factory.Hints;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.LinearTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.coverage.CoverageUtilities;
import org.opengis.coverage.grid.Format;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridCoverageReader;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.GeneralParameterValue;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReader;
import com.sun.media.imageioimpl.plugins.tiff.TIFFImageReaderSpi;

/**
 * This class can read a JP2K data source and create a {@link GridCoverage2D}
 * from the data.
 * 
 * @author Daniele Romagnoli, GeoSolutions.
 * @author Simone Giannecchini (simboss), GeoSolutions
 * @since 2.5.x
 */
@SuppressWarnings("deprecation")
public final class JP2KReader extends AbstractGridCoverage2DReader implements
        GridCoverageReader {

	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(JP2KReader.class);

	final static ExecutorService multiThreadedLoader= new ThreadPoolExecutor(4,8,30,TimeUnit.SECONDS,new LinkedBlockingQueue<Runnable>());

	/** The system-dependent default name-separator character. */
    private final static char SEPARATOR = File.separatorChar;
	
    private final static short[] geoJp2UUID = new short[] { 0xb1, 0x4b, 0xf8,
            0xbd, 0x08, 0x3d, 0x4b, 0x43, 0xa5, 0xae, 0x8c, 0xd7, 0xd5, 0xa6,
            0xce, 0x03 };

    static{
		try{
			//check if our tiff plugin is in the path
			final String kakaduJp2Name=it.geosolutions.imageio.plugins.jp2k.JP2KKakaduImageReaderSpi.class.getName();
			Class.forName(kakaduJp2Name);

			// imageio tiff reader
			final String standardJp2Name=com.sun.media.imageioimpl.plugins.jpeg2000.J2KImageReaderSpi.class.getName();
			
			final boolean succeeded=ImageIOUtilities.replaceProvider(ImageReaderSpi.class, kakaduJp2Name, standardJp2Name, "JPEG2000");
        	if(!succeeded)
        		LOGGER.warning("Unable to set ordering between tiff readers spi");	
	        
		} catch (ClassNotFoundException e) {
			LOGGER.log(Level.SEVERE,"Unable to load specific TIFF reader spi",e);
		} 
        
	}
    
    /**
     * Creates a new instance of a {@link JP2KReader}. I assume nothing about
     * file extension.
     * 
     * @param input
     *                Source object for which we want to build an JP2KReader.
     * @throws DataSourceException
     */
    public JP2KReader(Object input) throws IOException {
        this(input, null);
    }

	/**
     * Setting Envelope, GridRange and CRS from the given {@code ImageReader}
     * 
     * @param reader
     *                the {@code ImageReader} from which to retrieve metadata
     *                (if available) for setting properties
     * @throws IOException
     */
    protected void setCoverageProperties(ImageReader reader) throws IOException {
        // //
        //
        // Getting stream metadata from the underlying layer
        //
        // //
        final IIOMetadata metadata = reader.getStreamMetadata();
        int hrWidth = reader.getWidth(0);
        int hrHeight = reader.getHeight(0);
        final Rectangle actualDim = new Rectangle(0, 0, hrWidth,
                hrHeight);
        this.originalGridRange = new GeneralGridRange(actualDim);
        if (this.crs == null) {
            parsePRJFile();
        }
        if (this.originalEnvelope == null)
        	parseWorldFile();

        if (this.crs == null || this.originalEnvelope == null)
        	checkGeoJP2CRS(metadata);	
        // //
        //
        // If no sufficient information have been found to set the
        // envelope, try other ways, such as looking for a WorldFile
        //
        // //
        if (this.originalEnvelope == null) {
                throw new DataSourceException(
                        "Unavailable envelope for this coverage");
        }

        // setting the coordinate reference system for the envelope
        originalEnvelope.setCoordinateReferenceSystem(crs);

        // Additional settings due to "final" methods getOriginalXXX
    }

    private boolean isGeoJP2(byte[] id) {
        for (int i = 0; i < geoJp2UUID.length; i++) {
            if ((id[i] & 0xFF) != geoJp2UUID[i])
                return false;
        }
        return true;
    }
    
    private void checkGeoJP2CRS(final IIOMetadata metadata) throws IOException{
    	if (!(metadata instanceof JP2KStreamMetadata)) {
            if (LOGGER.isLoggable(Level.WARNING))
            		LOGGER.warning("Unexpected error! Metadata should be an instance of the expected class:"
                            + " JP2KStreamMetadata.");
        }
    	// //
        //
        // Looking for the UUIDBoxMetadataNode
        //
        // //
        CoordinateReferenceSystem coordinateReferenceSystem = null;
        IIOMetadataNode uuidBoxMetadataNode = ((JP2KStreamMetadata) metadata)
                .searchFirstOccurrenceNode(UUIDBox.BOX_TYPE);
        if (uuidBoxMetadataNode != null
                && uuidBoxMetadataNode instanceof UUIDBoxMetadataNode) {
            UUIDBoxMetadataNode uuid = (UUIDBoxMetadataNode) uuidBoxMetadataNode;
            final byte[] id = uuid.getUuid();
            final boolean isGeoJP2 = isGeoJP2(id);

            if (isGeoJP2) {
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(
                        uuid.getData());
                final TIFFImageReader tiffreader = (TIFFImageReader) new TIFFImageReaderSpi()
                        .createReaderInstance();
                tiffreader
                        .setInput(ImageIO.createImageInputStream(inputStream));
                final IIOMetadata tiffmetadata = tiffreader.getImageMetadata(0);
                final GeoTiffIIOMetadataDecoder metadataDecoder = new GeoTiffIIOMetadataDecoder(
                        tiffmetadata);
                final GeoTiffMetadata2CRSAdapter adapter = new GeoTiffMetadata2CRSAdapter(
                        null);
                try {
                    coordinateReferenceSystem = adapter
                            .createCoordinateSystem(metadataDecoder);
                    if (coordinateReferenceSystem != null) {
                    	if (this.crs == null)
                    		this.crs = coordinateReferenceSystem;
                    }
                    if (this.raster2Model == null){
	                    this.raster2Model = adapter
	                            .getRasterToModel(metadataDecoder);
	                    final AffineTransform tempTransform = new AffineTransform(
	                            (AffineTransform) raster2Model);
	                    tempTransform.translate(-0.5, -0.5);
	                    GeneralEnvelope envelope = CRS.transform(
	                            ProjectiveTransform.create(tempTransform),
	                            new GeneralEnvelope(originalGridRange.toRectangle()));
	                    envelope
	                            .setCoordinateReferenceSystem(crs);
	                    this.originalEnvelope = envelope;
                    }

                } catch (FactoryException e) {
                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.log(Level.FINE,
                                "Unable to parse CRS from underlying TIFF", e);
                    coordinateReferenceSystem = null;
                } catch (TransformException e) {
                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.log(Level.FINE,
                                "Unable to parse CRS from underlying TIFF", e);
                    coordinateReferenceSystem = null;
                } catch (UnsupportedOperationException e) {
                    if (LOGGER.isLoggable(Level.FINE))
                        LOGGER.log(Level.FINE,
                                "Unable to parse CRS from underlying TIFF due to an unsupported CRS", e);
                    coordinateReferenceSystem = null;
                } finally {
                    if (inputStream != null)
                        try {
                            inputStream.close();
                        } catch (IOException ioe) {
                            // Eat exception.
                        }
                }
            }
        }
    }

	/**
	 * Number of coverages for this reader is 1
	 * 
	 * @return the number of coverages for this reader.
	 */
	@Override
	public int getGridCoverageCount() {
		return 1;
	}

	/**
	 * Releases resources held by this reader.
	 * 
	 */
	@Override
	public synchronized void dispose() {
		super.dispose();
		rasterManager.dispose();
	}


	/**
	 * The source {@link URL} 
	 */
	URL sourceURL;

	boolean expandMe;

	private RasterManager rasterManager;

	private String parentPath;

	/**
	 * Constructor.
	 * 
	 * @param source
	 *            The source object.
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 * 
	 */
	public JP2KReader(Object source, Hints uHints) throws IOException {
		// //
		//
		// managing hints
		//
		// //
		if (this.hints == null)
			this.hints= new Hints();	
		if (uHints != null) {
			this.hints.add(uHints);
		}
		this.coverageFactory= CoverageFactoryFinder.getGridCoverageFactory(this.hints);

		// /////////////////////////////////////////////////////////////////////
		//
		// Check source
		//
		// /////////////////////////////////////////////////////////////////////
		if (source == null) {
			final IOException ex = new IOException(
					"JP2KReader:No source set to read this coverage.");
			if (LOGGER.isLoggable(Level.WARNING))
				LOGGER.log(Level.WARNING, ex.getLocalizedMessage(), ex);
			throw new DataSourceException(ex);
		}
		this.source = source;		
		this.sourceURL=Utils.checkSource(source);

        if(this.sourceURL==null)
			throw new DataSourceException("This plugin accepts only File,  URL and String pointing to a file");

        final File inputFile = new File(URLDecoder.decode(sourceURL.getFile(), "UTF-8"));
        parentPath = inputFile.getParent();
        final ImageReader reader = new JP2KKakaduImageReaderSpi().createReaderInstance();
        reader.setInput(inputFile);
        
		coverageName = inputFile.getName();

        final int dotIndex = coverageName.lastIndexOf(".");
        coverageName = (dotIndex == -1) ? coverageName : coverageName
                .substring(0, dotIndex);
		
		// //
		//
		// get the crs if able to
		//
		// //
		final Object tempCRS = this.hints.get(Hints.DEFAULT_COORDINATE_REFERENCE_SYSTEM);
		if (tempCRS != null) {
			this.crs = (CoordinateReferenceSystem) tempCRS;
			LOGGER.log(Level.WARNING, new StringBuffer(
					"Using forced coordinate reference system ").append(
					crs.toWKT()).toString());
		} else {
			
			setCoverageProperties(reader);
			
			if (crs == null) {
				// use the default crs
				crs = AbstractGridFormat.getDefaultCRS();
				LOGGER.log(Level.WARNING,"Unable to find a CRS for this coverage, using a default one: "+crs.toWKT());
			} 
		}
		setResolutionInfo(reader);
		reader.reset();
        reader.dispose();

		// creating the raster manager
		rasterManager = new RasterManager(this);
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#getFormat()
	 */
	public Format getFormat() {
		return new JP2KFormat();
	}

	/**
	 * @see org.opengis.coverage.grid.GridCoverageReader#read(org.opengis.parameter.GeneralParameterValue[])
	 */
	public GridCoverage read(GeneralParameterValue[] params) throws IOException {

		if (LOGGER.isLoggable(Level.FINE)) {
			LOGGER.fine("Reading image from " + sourceURL.toString());
			LOGGER.fine(new StringBuffer("Highest res ").append(highestRes[0])
					.append(" ").append(highestRes[1]).toString());
		}

		final Collection<GridCoverage2D> response = rasterManager.read(params);
		if(response.isEmpty())
			throw new DataSourceException("Unable to create a coverage for this request ");
		else
			return response.iterator().next();
	}

	/**
	 * Package private accessor for {@link Hints}.
	 * 
	 * @return this {@link Hints} used by this reader.
	 */
	Hints getHints(){
		return super.hints;
	}
	
	/**
	 * Package private accessor for the highest resolution values.
	 * 
	 * @return the highest resolution values.
	 */
	double[] getHighestRes(){
		return super.highestRes;
	}
	
	/**
	 * 
	 * @return
	 */
	double[][] getOverviewsResolution(){
		return super.overViewResolutions;
	}
	
	int getNumberOfOvervies(){
		return super.numOverviews;
	}
	

    /** Package scope grid to world transformation accessor */
    MathTransform getRaster2Model() {
        return raster2Model;
    }
    
    /**
     * Let us retrieve the {@link GridCoverageFactory} that we want to use.
     * 
     * @return
     * 			retrieves the {@link GridCoverageFactory} that we want to use.
     */
    GridCoverageFactory getGridCoverageFactory(){
    	return coverageFactory;
    }

	String getName() {
		return super.coverageName;
	}
	
	
	/**
     * Gets the coordinate reference system that will be associated to the
     * {@link GridCoverage} by looking for a related PRJ.
	 * @throws UnsupportedEncodingException 
     */
    protected void parsePRJFile() throws UnsupportedEncodingException {
        String prjPath = null;
        prjPath = new StringBuilder(parentPath).append(SEPARATOR)
                .append(coverageName).append(".prj").toString();

        // read the prj info from the file
        PrjFileReader projReader = null;

        try {
            final File prj = new File(prjPath);

            if (prj.exists()) {
                projReader = new PrjFileReader(new FileInputStream(prj)
                        .getChannel());
                this.crs = projReader.getCoordinateReferenceSystem();
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
        file2Parse = new File(worldFilePath + ".j2w");
        worldFileExists = file2Parse.exists();
    
        // //
        //
        // Check for a world file with the default extension
        //
        // //
        if (!worldFileExists) {
            file2Parse = new File(worldFilePath + ".wld");
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
            tempTransform.preConcatenate(Utils.CENTER_TO_CORNER);

            try {
                final LinearTransform gridToWorldTransform = ProjectiveTransform
                        .create(tempTransform);
                final Envelope gridRange = new GeneralEnvelope(
                        originalGridRange.toRectangle());
                final GeneralEnvelope coverageEnvelope = CRS.transform(
                        gridToWorldTransform, gridRange);
                originalEnvelope = coverageEnvelope;
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
     * Gets resolution information about the coverage itself.
     * 
     * @param reader
     *                an {@link ImageReader} to use for getting the resolution
     *                information.
     * @throws IOException
     * @throws TransformException
     * @throws DataSourceException
     */
    private void setResolutionInfo(ImageReader reader) throws IOException {
        // //
        //
        // get the dimension of the high resolution image and compute the
        // resolution
        //
        // //
        final Rectangle originalDim = new Rectangle(0, 0, reader.getWidth(0),
                reader.getHeight(0));

        if (originalGridRange == null) {
        	originalGridRange = new GeneralGridRange(originalDim);
        }

        // ///
        //
        // setting the higher resolution available for this coverage
        //
        // ///
        highestRes = CoverageUtilities
                .getResolution((AffineTransform) raster2Model);
        if (LOGGER.isLoggable(Level.FINE))
            LOGGER.fine(new StringBuffer("Highest Resolution = [").append(
                            highestRes[0]).append(",").append(highestRes[1])
                            .toString());
        numOverviews = 0;
//		final double[][] resolutions = configuration.getLevels();
		overViewResolutions = numOverviews >= 1 ? new double[numOverviews][2]: null;
//		highestRes = new double[2];
//		highestRes[0] = resolutions[0][0];
//		highestRes[1] =resolutions[0][1];
//		if(numOverviews>0){
//	   		for (int i = 0; i < numOverviews; i++) {     			
//				overViewResolutions[i][0] = resolutions[i+1][0];
//				overViewResolutions[i][1] = resolutions[i+1][1];
//	   		}	
//		}
        

    }
}

