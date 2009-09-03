/**
 * 
 */
package org.geotools.gce.imagemosaic;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enum that can be use to distinguish between relative paths and absolute paths
 * when trying to load a granule for a mosaic.
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 * 
 */
enum PathType {
	RELATIVE{

		@Override
		File resolvePath(final String parentLocation,final  String location) {
			// initial checks
			ImageMosaicUtils.ensureNonNull("parentLocation", parentLocation);
			ImageMosaicUtils.ensureNonNull("location", location);
			if(LOGGER.isLoggable(Level.FINE))
			{
				final StringBuilder builder = new StringBuilder();
				builder.append("Trying to resolve path:").append("\n");
				builder.append("type:").append(this.toString()).append("\n");
				builder.append("parentLocation:").append(parentLocation).append("\n");
				builder.append("location:").append(location);
				LOGGER.fine(builder.toString());
			}
			// create a file for the provided location, relative to parent location
			File rasterFile= new File(parentLocation,location);
			if(!ImageMosaicUtils.checkFileReadable(rasterFile))
			{		
				if (LOGGER.isLoggable(Level.INFO))
					LOGGER.info("Unable to read image for file "+ rasterFile.getAbsolutePath());

				return null;

			}		
			return rasterFile;

		}
		
	},
	
	ABSOLUTE{

		@Override
		File resolvePath(final String parentLocation,final  String location) {

			ImageMosaicUtils.ensureNonNull("location", location);
			if(LOGGER.isLoggable(Level.FINE))
			{
				final StringBuilder builder = new StringBuilder();
				builder.append("Trying to resolve path:").append("\n");
				builder.append("type:").append(this.toString()).append("\n");
				if(parentLocation!=null)
					builder.append("parentLocation:").append(parentLocation).append("\n");
				LOGGER.fine(builder.toString());	
			}
			// create a file for the provided location ignoring the parent type
			File rasterFile= new File(location);
			if(!ImageMosaicUtils.checkFileReadable(rasterFile))
			{		
				if (LOGGER.isLoggable(Level.INFO))
					LOGGER.info("Unable to read image for file "+ rasterFile.getAbsolutePath());
				return null;

			}		
			return rasterFile;
		}
		
	};
	
	/** Logger. */
	private final static Logger LOGGER = org.geotools.util.logging.Logging.getLogger(PathType.class);

	/**
	 * Resolve a path for a granule given the parent location and location
	 * itself.
	 * 
	 * <p>
	 * the location can never be null, while the parent location could be null,
	 * as an instance when the path is relative.
	 * 
	 * @param parentLocation
	 * @param location
	 * @return a {@link File} instance that points to a location which could be
	 *         relative or absolute depending on the flavor of the enum where
	 *         this method is applied. This method might return <code>null</code>
	 *         in case something bad happens.
	 */
	abstract File resolvePath(
			final String parentLocation,
			final String location);
	
}
