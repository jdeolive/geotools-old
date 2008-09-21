package org.geotools.gce.imagemosaic.jdbc;

import java.awt.image.BufferedImage;

import org.geotools.geometry.GeneralEnvelope;


/**
 * Class for holding tile info for composing the resulting image.
 * Objects of this class will be put in the queue from ImageDecoderThread
 * and read from ImageComposerThread
 * 
 * @author mcr
 *
 */
class TileQueueElement {
	
	static TileQueueElement ENDELEMENT = new TileQueueElement(null,null,null);   
	/**
	 * name of the tile 
	 */
	private String name;
	/**
	 * the BufferedImage 
	 */
	private BufferedImage tileImage;
	/**
	 *	The georeferencing information 
	 */
	private GeneralEnvelope envelope;
	GeneralEnvelope getEnvelope() {
		return envelope;
	}
	
	TileQueueElement(String name,BufferedImage tileImage,GeneralEnvelope envelope) {
		this.name=name;
		this.tileImage=tileImage;
		this.envelope=envelope;
	}
	
	String getName() {
		return name;
	}
	BufferedImage getTileImage() {
		return tileImage;
	}

	boolean isEndElement () {
		return getName()==null && getTileImage()==null && getEnvelope()==null;
	}
}
