package org.geotools.gce.imagemosaic;

import java.awt.image.IndexColorModel;

import org.geotools.geometry.Envelope2D;


/**
 * Very simple bean to hold the configuration of the mosaic.
 * 
 * @author Simone Giannecchini, GeoSolutions S.A.S.
 * @author Stefan Alfons Krueger (alfonx), Wikisquare.de : Support for jar:file:foo.jar/bar.properties URLs
 */
public class MosaicConfigurationBean {

	/**
	 * <code>true</code> it tells us if the mosaic points to absolute paths or to relative ones. (in case of <code>false</code>).
	 */
	private boolean absolutePath;
	
	/**
	 * <code>true</code> if we need to expand to RGB(A) the single tiles in case they use a different {@link IndexColorModel}.
	 */
	private boolean expandToRGB;
	
	/** The envelope for the whole mosaic.**/
	private Envelope2D envelope2D;
	
	/** OverviewLevel levels */
	private double[][] levels;
	
	/** name for the mosaic.*/
	private String name;
	
	/** number of levels*/
	private int levelsNum;
	
	/** location attribute name*/
	private String locationAttribute;
	
	/**Suggested SPI for the various tiles. May be null.**/
	private String suggestedSPI;
	
	/** time attribute name. <code>null</code> if absent.*/
	private String timeAttribute;

	public String getTimeAttribute() {
		return timeAttribute;
	}
	public void setTimeAttribute(String timeAttribute) {
		this.timeAttribute = timeAttribute;
	}
	/**
	 * @return the suggestedSPI
	 */
	public String getSuggestedSPI() {
		return suggestedSPI;
	}
	/**
	 * @param suggestedSPI the suggestedSPI to set
	 */
	public void setSuggestedSPI(String suggestedSPI) {
		this.suggestedSPI = suggestedSPI;
	}
	
	public boolean isAbsolutePath() {
		return absolutePath;
	}
	public void setAbsolutePath(boolean absolutePath) {
		this.absolutePath = absolutePath;
	}
	public boolean isExpandToRGB() {
		return expandToRGB;
	}
	public void setExpandToRGB(boolean expandToRGB) {
		this.expandToRGB = expandToRGB;
	}
	public Envelope2D getEnvelope2D() {
		return envelope2D;
	}
	public void setEnvelope2D(Envelope2D envelope2D) {
		this.envelope2D = envelope2D;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getLevelsNum() {
		return levelsNum;
	}
	public void setLevelsNum(int levelsNum) {
		this.levelsNum = levelsNum;
	}
	public double[][] getLevels() {
		return levels.clone();
	}
	public void setLevels(double[][] levels) {
		this.levels = levels.clone();
	}
	public String getLocationAttribute() {
		return locationAttribute;
	}
	public void setLocationAttribute(String locationAttribute) {
		this.locationAttribute = locationAttribute;
	}
	
	


}