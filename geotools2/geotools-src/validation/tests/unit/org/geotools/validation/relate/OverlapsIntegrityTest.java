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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.validation.RoadValidationResults;

import com.vividsolutions.jts.geom.Envelope;


/**
 * OverlapsIntegrityTest<br>
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
public class OverlapsIntegrityTest extends SpatialTestCase
{

	/**
	 * Constructor for OverlapsIntegrityTest.
	 * @param arg0
	 */
	public OverlapsIntegrityTest(String arg0)
	{
		super(arg0);
	}

	public void testValidate()
	{
		OverlapsIntegrity overlap = new OverlapsIntegrity();
		overlap.setExpected(false);
		overlap.setGeomTypeRefA("my:line");
		
		Map map = new HashMap();
		try
		{
			map.put("my:line", mds.getFeatureSource("line"));
		} catch (IOException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try
		{
			System.out.println("Test Validate");
			assertFalse(overlap.validate(map, lineBounds, vr));
			//System.out.println("hi");
			//(RoadValidationResults)vr;
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void testValidateBBox()
		{
			OverlapsIntegrity overlap = new OverlapsIntegrity();
			overlap.setExpected(false);
			overlap.setGeomTypeRefA("my:line");
		
			Map map = new HashMap();
			try
			{
				map.put("my:line", mds.getFeatureSource("line"));
			} catch (IOException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		
			try
			{
				System.out.println("Test Validate BBox");
				assertFalse(overlap.validate(map, new Envelope(-1,2,-2,3), vr));
				//System.out.println("hi");
				//(RoadValidationResults)vr;
			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}

}
