/*
 *    Geotools2 - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002, Geotools Project Managment Committee (PMC)
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
/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
/*
 * Created on Apr 29, 2004
 *
 */
package org.geotools.validation.relate;

import org.geotools.data.DataUtilities;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import junit.framework.TestCase;

/**
 * SpatialTestCase<br>
 * @author bowens<br>
 * Created Apr 29, 2004<br>
 * @version <br>
 *
 * <b>Puropse:</b><br>
 * <p>
 * DOCUMENT ME!!
 * </p>
 *
 * <b>Description:</b><br>
 * <p>
 * DOCUMENT ME!!
 * </p>
 *
 * <b>Usage:</b><br>
 * <p>
 * DOCUMENT ME!!
 * </p>
 */
public class SpatialTestCase extends TestCase
{
	protected GeometryFactory gf;
	protected FeatureType lineType;
	protected Feature[] lineFeatures;
	protected Envelope lineBounds;
	protected LineString ls0, ls1, ls2, ls3;
	protected String namespace;
	protected FilterFactory filterFactory;
	protected Filter lineFilter;

	MemoryDataStore mds;		// assumes a consistant data type
	ValidationResults vr;

	/**
	 * Constructor for OverlapsIntegrityTest.
	 * @param arg0
	 */
	public SpatialTestCase(String arg0)
	{
		super(arg0);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 *
	 * <code><pre>
	 *
	 * 			 (0,2)				(2.6,2)
	 * 			    x					x
	 * 				 \  				|
	 * 				  \ls1				|ls2
	 * 				   \	 			|
	 * 			  (1,1)+				+
	 * 				   |				|
	 * 				   |				|
	 * 		ls0		   |	   (2,0.1)	|						ls3
	 * (0,0)x----------+----------+----------+==========x----------x
	 * 				   |				|			  (4,0)		 (5,0.1)
	 * 				   |				|
	 * 				   |				|
	 * 				   x				x
	 * 				(1,-1)			  (2,-1)
	 *
	 * </pre></code>
	 */
	protected void setUp() throws Exception
	{
		gf = new GeometryFactory();
		mds = new MemoryDataStore();
		namespace = getName();
		vr = null; // new RoadValidationResults();

		lineFeatures = new Feature[4];
		ls0 = gf.createLineString(new Coordinate[]{	new Coordinate(0,0),
													new Coordinate(2,0.1),
													new Coordinate(3,0),
													new Coordinate(4,0)} );
		ls1 = gf.createLineString(new Coordinate[]{	new Coordinate(0,2),
													new Coordinate(1,1),
													new Coordinate(1,0),
													new Coordinate(1,-1)} );
		ls2 = gf.createLineString(new Coordinate[]{	new Coordinate(2.6,2),
													new Coordinate(2.5,1),
													new Coordinate(2.5,-1)} );
		ls3 = gf.createLineString(new Coordinate[]{	new Coordinate(3,0),
													new Coordinate(4,0),
													new Coordinate(5,0.1)} );

		lineType = DataUtilities.createType("my.line",
											"id:0,geom:LineString,name:String");
		lineFeatures[0] = lineType.create(new Object[] {
										new Integer(0),
										ls0,
										"line0"},
									"line.line0");
		lineFeatures[1] = lineType.create(new Object[] {
										new Integer(1),
										ls1,
										"line1"},
									"line.line1");
		lineFeatures[2] = lineType.create(new Object[] {
										new Integer(2),
										ls2,
										"line2"},
									"line.line2");
		lineFeatures[3] = lineType.create(new Object[] {
										new Integer(3),
										ls3,
										"line3"},
									"line.line3");
		lineBounds = new Envelope();
		lineBounds.expandToInclude( lineFeatures[0].getBounds() );
		lineBounds.expandToInclude( lineFeatures[1].getBounds() );
		lineBounds.expandToInclude( lineFeatures[2].getBounds() );
		lineBounds.expandToInclude( lineFeatures[3].getBounds() );

//		filterFactory = FilterFactory.createFilterFactory();
//		BBoxExpression bbex = filterFactory.createBBoxExpression(lineBounds);

		mds.addFeature(lineFeatures[0]);
		mds.addFeature(lineFeatures[1]);
		mds.addFeature(lineFeatures[2]);
		mds.addFeature(lineFeatures[3]);
	}

	protected void tearDown() throws Exception
	{
		gf = null;
		lineType = null;
		lineFeatures = null;
		lineBounds = null;
		ls0 = null;
		ls1 = null;
		ls2 = null;
		ls3 = null;
		namespace = null;
		vr = null;
	}

}
