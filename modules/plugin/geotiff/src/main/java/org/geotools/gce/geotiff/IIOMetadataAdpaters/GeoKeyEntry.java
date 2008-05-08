/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2005-2006, GeoTools Project Managment Committee (PMC)
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
package org.geotools.gce.geotiff.IIOMetadataAdpaters;

/**
 * This class is a holder for a GeoKey record containing four short values as
 * specified in the GeoTiff spec. The values are a GeoKey ID, the TIFFTag number
 * of the location of this data, the count of values for this GeoKey, and the
 * offset (or value if the location is 0).
 * 
 * <p>
 * If the Tiff Tag location is 0, then the value is a Short and is contained in
 * the offset. Otherwise, there is one or more value in the specified external
 * Tiff tag. The number is specified by the count field, and the offset into the
 * record is the offset field.
 * </p>
 * 
 * @author Simone Giannecchini
 * @author Mike Nidel
 */
public final class GeoKeyEntry {
	/** ID of this key. */
	private int myKeyID;

	private int myTiffTagLocation;

	private int myCount;

	private int myValueOffset;

	/**
	 * Constructor of a {@link GeoKeyEntry}.
	 * 
	 * @param keyID
	 *            the id of this {@link GeoKeyEntry}.
	 * @param tagLoc
	 *            the location of this tag.
	 * @param count
	 * @param offset
	 */
	public GeoKeyEntry(int keyID, int tagLoc, int count, int offset) {
		myKeyID = keyID;
		myTiffTagLocation = tagLoc;
		myCount = count;
		myValueOffset = offset;
	}

	public int getKeyID() {
		return myKeyID;
	}

	public int getTiffTagLocation() {
		return myTiffTagLocation;
	}

	public int getCount() {
		return myCount;
	}

	public int getValueOffset() {
		return myValueOffset;
	}

	public void setCount(int myCount) {
		this.myCount = myCount;
	}

	public void setKeyID(int myKeyID) {
		this.myKeyID = myKeyID;
	}

	public void setTiffTagLocation(int myTiffTagLocation) {
		this.myTiffTagLocation = myTiffTagLocation;
	}

	public void setValueOffset(int myValueOffset) {
		this.myValueOffset = myValueOffset;
	}

	public int[] getValues() {
		return new int[] { myKeyID, myTiffTagLocation, myValueOffset, myCount };
	}
} 
