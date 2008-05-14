package org.geotools.utils;

import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.event.IIOWriteWarningListener;

/**
 * Simple adapter for {@link IIOWriteProgressListener} and
 * {@link IIOWriteWarningListener}.
 * 
 * @author Simone Giannecchini, GeoSolutions.
 * @see IIOWriteProgressListener
 * @see IIOWriteWarningListener
 * 
 */
public class WriteProgressListenerAdapter implements IIOWriteProgressListener,
		IIOWriteWarningListener {

	public void imageStarted(ImageWriter source, int imageIndex) {
	}

	public void imageProgress(ImageWriter source, float percentageDone) {
	}

	public void imageComplete(ImageWriter source) {
	}

	public void thumbnailStarted(ImageWriter source, int imageIndex,
			int thumbnailIndex) {
	}

	public void thumbnailProgress(ImageWriter source, float percentageDone) {
	}

	public void thumbnailComplete(ImageWriter source) {
	}

	public void writeAborted(ImageWriter source) {
	}

	public void warningOccurred(ImageWriter source, int imageIndex,
			String warning) {
	}

}
