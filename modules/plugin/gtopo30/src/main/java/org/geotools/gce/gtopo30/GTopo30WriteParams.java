/**
 * 
 */
package org.geotools.gce.gtopo30;

import java.util.Locale;

import javax.imageio.ImageWriteParam;

import org.geotools.coverage.grid.io.imageio.GeoToolsWriteParams;

/**
 * @author Simone Giannecchini, GeoSolutions.
 * @since 2.3.x
 * 
 */
public final class GTopo30WriteParams extends GeoToolsWriteParams {



	public int getCompressionMode() {
		return compressionMode;
	}

	public String getCompressionType() {
		return compressionType;
	}

	public boolean hasController() {
		return false;
	}

	public void setCompressionMode(int compressionMode) {
		this.compressionMode = compressionMode;
	}


	public void setCompressionType(String ct) {
		compressionType= new String(ct);
	}

	/**
	 * Default constructor.
	 */
	public GTopo30WriteParams() {
		super(new ImageWriteParam(Locale.getDefault()));
		//allowed compression types
		compressionTypes= new String[]{"NONE","ZIP"};
		//default compression type
		compressionType="NONE";
		canWriteCompressed=true;
		canWriteProgressive=false;
		canWriteTiles=false;
		canOffsetTiles=false;
		controller=null;
		

	}

	public String[] getCompressionTypes() {
		return compressionTypes;
	}



}
