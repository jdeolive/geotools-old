/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2006, Geotools Project Managment Committee (PMC)
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
 *
 */
package org.geotools.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.geotools.referencing.operation.LinearTransform;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.opengis.referencing.operation.MathTransform;

/**
 * This class is responsible for parsing a world file in order to build an
 * affine transform using the parameters provided in the file itself.
 * 
 * <p>
 * The parameters found in the file should be as follows:
 * <ol>
 * <li>size of pixel in x direction</li>
 * <li>rotation term for row</li>
 * <li>rotation term for column</li>
 * <li>size of pixel in y direction</li>
 * <li>x coordinate of centre of upper left pixel in map units</li>
 * <li>y coordinate of centre of upper left pixel in map units</li>
 * </ol>
 * <strong>Note that the last two coordinates refer to the centre of the pixel!</strong>
 * 
 * 
 * <p>
 * It is worth to point out that various data sources describe the parameters in
 * the world file as the mapping from the pixel centres' to the associated world
 * coords. Here we directly build the needed grid to world transform and we DO
 * NOT add any half a pixel translation given that, as stated above, the values
 * we receive should map to the centre of the pixel.
 * 
 * 
 * @author Simone Giannecchini
 * @since 2.3
 * 
 */
public final class WorldFileReader {

	/**
	 * Default size for the underlying buffer,
	 */
	private final static int DEFAULT_BUFFER_SIZE = 8192;

	/** Resolution on the first dimension. */
	private double xPixelSize = 0.0;

	/** Rotation on the first dimension. */
	private double rotationX = 0.0;

	/** Resolution on the second dimension. */
	private double rotationY = 0.0;

	/** Resolution on the second dimension. */
	private double yPixelSize = 0.0;

	/** Upper left centre coordinate of first dimension. */
	private double xULC = 0.0;

	/** Upper left centre coordinate of second dimension. */
	private double yULC = 0.0;

	/** Resulting linear transform. */
	private LinearTransform transform;

	/**
	 * Constructor.
	 * 
	 * @param worldFile
	 * @throws IOException
	 */
	public WorldFileReader(final File worldFile) throws IOException {
		this(worldFile, DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Constructor.
	 * 
	 * @param worldFile
	 * @param bufferSize
	 * @throws IOException
	 */
	public WorldFileReader(final File worldFile, final int bufferSize)
			throws IOException {
		final BufferedReader bufferedreader = new BufferedReader(
				new FileReader(worldFile));

		int index = 0;
		double value = 0;
		String str;
		while ((str = bufferedreader.readLine()) != null) {

			value = 0;

			try {
				value = Double.parseDouble(str.trim());
			} catch (NumberFormatException e) {
				// A trick to bypass invalid lines ...
				continue;
			}

			switch (index) {
			case 0:
				xPixelSize = value;

				break;

			case 1:
				rotationX = value;

				break;

			case 2:
				rotationY = value;

				break;

			case 3:
				yPixelSize = value;

				break;

			case 4:
				xULC = value;

				break;

			case 5:
				yULC = value;

				break;

			default:
				break;
			}

			index++;
		}
		bufferedreader.close();

		// did we find all we were looking for?
		if (index < 5)
			throw new IOException(
					"Not all the values were found for this world file!");
	}

	public double getRotationX() {
		return rotationX;
	}

	public double getRotationY() {
		return rotationY;
	}

	public double getXPixelSize() {
		return xPixelSize;
	}

	public double getXULC() {
		return xULC;
	}

	public double getYPixelSize() {
		return yPixelSize;
	}

	public double getYULC() {
		return yULC;
	}

	public synchronized MathTransform getTransform() {
		if (transform == null) {
			// building the transform
			final GeneralMatrix gm = new GeneralMatrix(3); // identity

			// compute an "offset and scale" matrix
			gm.setElement(0, 0, xPixelSize);
			gm.setElement(1, 1, yPixelSize);
			gm.setElement(0, 1, rotationX);
			gm.setElement(1, 0, rotationY);

			gm.setElement(0, 2, xULC);
			gm.setElement(1, 2, yULC);

			// make it a LinearTransform
			transform = ProjectiveTransform.create(gm);
		}
		return transform;
	}

}
