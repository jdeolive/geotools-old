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
package org.geotools.validation.relate;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.filter.AttributeExpression;
import org.geotools.filter.BBoxExpression;
import org.geotools.filter.Filter;
import org.geotools.filter.FilterFactory;
import org.geotools.filter.GeometryFilter;
import org.geotools.filter.IllegalFilterException;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;


/**
 * OverlapsIntegrity<br>
 * @author bowens, ptozer<br>
 * Created Apr 27, 2004<br>
 * @version <br>
 * 
 * <b>Puropse:</b><br>
 * <p>
 * Tests to see if a Geometry overlaps, partially or entirely, with another Geometry.
 * 
 * <b>Description:</b><br>
 * <p>
 * If only one layer is provided, the geometries of that layer are compared with each other.
 * If two layers are provided, then the geometries are compared across the layers.
 * </p>
 * 
 * <b>Usage:</b><br>
 * <p>
 * 		OverlapsIntegrity overlap = new OverlapsIntegrity();
 *		overlap.setExpected(false);
 *		overlap.setGeomTypeRefA("my:line");
 *		
 *		Map map = new HashMap();
 *		try
 *		{
 *			map.put("my:line", mds.getFeatureSource("line"));
 *		} catch (IOException e1)
 *		{
 *			e1.printStackTrace();
 *		}
 *		
 *		try
 *		{
 *			assertFalse(overlap.validate(map, lineBounds, vr));
 *		} catch (Exception e)
 *		{
 *			e.printStackTrace();
 *		}
 * </p>
 */
public class OverlapsIntegrity extends RelationIntegrity 
{
	private static final Logger LOGGER = Logger.getLogger("org.geotools.validation");
	private static HashSet usedIDs;
	
	/**
	 * OverlapsIntegrity Constructor
	 * 
	 */
	public OverlapsIntegrity()
	{
		super();
		usedIDs = new HashSet();	//TODO: remove me later, memory inefficient
	}
	
	
	/* (non-Javadoc)
	 * @see org.geotools.validation.IntegrityValidation#validate(java.util.Map, com.vividsolutions.jts.geom.Envelope, org.geotools.validation.ValidationResults)
	 */
	public boolean validate(Map layers, Envelope envelope,
			ValidationResults results) throws Exception 
	{
		LOGGER.finer("Starting test "+getName()+" ("+getClass().getName()+")" );
		String typeRef1 = getGeomTypeRefA();
		LOGGER.finer( typeRef1 +": looking up FeatureSource " );    	
		FeatureSource geomSource1 = (FeatureSource) layers.get( typeRef1 );
		LOGGER.finer( typeRef1 +": found "+ geomSource1.getSchema().getTypeName() );
		
		String typeRef2 = getGeomTypeRefB();
		if (typeRef2 == EMPTY || typeRef1.equals(typeRef2))
			return validateSingleLayer(geomSource1, isExpected(), results, envelope);
		else
		{
			LOGGER.finer( typeRef2 +": looking up FeatureSource " );        
			FeatureSource geomSource2 = (FeatureSource) layers.get( typeRef2 );
			LOGGER.finer( typeRef2 +": found "+ geomSource2.getSchema().getTypeName() );
			return validateMultipleLayers(geomSource1, geomSource2, isExpected(), results, envelope);
		}	
	
	}


	/**
	 * <b>validateMultipleLayers Purpose:</b> <br>
	 * <p>
	 * This validation tests for a geometry overlaps another geometry. 
	 * Uses JTS' Geometry.overlaps(Geometry) and  Geometry.contains(Geometry)method.
	 * The DE-9IM intersection matrix for overlaps is:
     * T*T***T** (for two points or two surfaces)
     * 1*T***T** (for two curves) 
     * Contains DE-9IM intersection matrix is T*F**F***.
	 * </p>
	 * 
	 * <b>Description:</b><br>
	 * <p>
	 * The function filters the FeatureSources using the given bounding box.
	 * It creates iterators over both filtered FeatureSources. It calls overlaps() and contains()using the
	 * geometries in the FeatureSource layers. Tests the results of the method call against
	 * the given expected results. Returns true if the returned results and the expected results 
	 * are true, false otherwise.
	 * 
	 * </p>
	 * 
	 * Author: bowens<br>
	 * Created on: Apr 27, 2004<br>
	 * @param featureSourceA - the FeatureSource to pull the original geometries from. This geometry is the one that is tested for overlaping with the other
	 * @param featureSourceB - the FeatureSource to pull the other geometries from - these geometries will be those that may overlap the first geometry
	 * @param expected - boolean value representing the user's expected outcome of the test
	 * @param results - ValidationResults
	 * @param bBox - Envelope - the bounding box within which to perform the overlaps() and contains()
	 * @return boolean result of the test
	 * @throws Exception - IOException if iterators improperly closed
	 */
	private boolean validateMultipleLayers(	FeatureSource featureSourceA, 
											FeatureSource featureSourceB, 
											boolean expected, 
											ValidationResults results, 
											Envelope bBox) 
	throws Exception
	{
		boolean success = true;
		
		FilterFactory ff = FilterFactory.createFilterFactory();
		Filter filter = null;

		filter = (Filter) ff.createBBoxExpression(bBox);

		FeatureResults featureResultsA = featureSourceA.getFeatures(filter);
		FeatureResults featureResultsB = featureSourceB.getFeatures(filter);
		
		FeatureReader fr1 = null;
		FeatureReader fr2 = null;
		try 
		{
			fr1 = featureResultsA.reader();

			if (fr1 == null)
				return success;
						
			while (fr1.hasNext())
			{
				Feature f1 = fr1.next();
				Geometry g1 = f1.getDefaultGeometry();
				fr2 = featureResultsB.reader();
				
				while (fr2 != null && fr2.hasNext())
				{
					Feature f2 = fr2.next();
					Geometry g2 = f2.getDefaultGeometry();
					System.out.println("Do the two overlap?->" + g1.overlaps(g2));
					System.out.println("Does the one contain the other?->" + g1.contains(g2));
					if(g1.overlaps(g2) != expected || g1.contains(g2) != expected)
					{
						results.error( f1, f1.getDefaultGeometry().getGeometryType()+" "+getGeomTypeRefA()+" overlapped "+getGeomTypeRefB()+"("+f2.getID()+"), Result was not "+expected );
						success = false;
					}
				}		
			}
		}finally
		{
			/** Close the connections to the feature readers*/
			try {
				fr1.close();
				if (fr2 != null)
					fr2.close();
			} catch (IOException e4) {
				e4.printStackTrace();
				throw e4;
			}
		}
		
		return success;
	}

	/**
	 * <b>validateSingleLayer Purpose:</b> <br>
	 * <p>
	 * This validation tests for a geometry that overlaps with itself. 
	 * Uses JTS' Geometry.overlaps(Geometry) and  Geometry.contains(Geometry)method.
	 * The DE-9IM intersection matrix for overlaps is:
     * T*T***T** (for two points or two surfaces)
     * 1*T***T** (for two curves) 
     * Contains DE-9IM intersection matrix is T*F**F***.
	 * </p>
	 * 
	 * <b>Description:</b><br>
	 * <p>
	 * The function filters the FeatureSource using the given bounding box.
	 * It creates iterators over the filtered FeatureSource. It calls overlaps() and contains() using the
	 * geometries in the FeatureSource layer. Tests the results of the method calls against
	 * the given expected results. Returns true if the returned results and the expected results 
	 * are true, false otherwise.
	 * 
	 * </p>	 * 
	 * Author: bowens<br>
	 * Created on: Apr 27, 2004<br>
	 * @param featureSourceA - the FeatureSource to pull the original geometries from. This geometry is the one that is tested for overlapping itself
	 * @param expected - boolean value representing the user's expected outcome of the test
	 * @param results - ValidationResults
	 * @param bBox - Envelope - the bounding box within which to perform the overlaps() and contains()
	 * @return boolean result of the test
	 * @throws Exception - IOException if iterators improperly closed
	 */
	private boolean validateSingleLayer(FeatureSource featureSourceA, 
										boolean expected, 
										ValidationResults results, 
										Envelope bBox) 
	throws Exception
	{
		boolean success = true;
		int errors = 0;
		Date date1 = new Date();
		int countInterval = 100;
		int counter = 0;
		FeatureType ft = featureSourceA.getSchema();
		
		Filter filter = filterBBox(bBox, ft);

		//FeatureResults featureResults = featureSourceA.getFeatures(filter);
		FeatureResults featureResults = featureSourceA.getFeatures();
		
		FeatureReader fr1 = null;
		FeatureReader fr2 = null;
		try 
		{
			fr1 = featureResults.reader();

			if (fr1 == null)
				return success;
		
			while (fr1.hasNext())
			{
				counter++;
				Feature f1 = fr1.next();
				//System.out.println(f1.getID() + ".envelope = " + f1.getDefaultGeometry().getEnvelope());
				
				Geometry g1 = f1.getDefaultGeometry();
				Filter filter2 = filterBBox(g1.getEnvelope().getEnvelopeInternal(), ft);

				FeatureResults featureResults2 = featureSourceA.getFeatures(filter2);
				//FeatureResults featureResults2 = featureSourceA.getFeatures();
				
				fr2 = featureResults2.reader();
				try {
					//System.out.println("featureResults length = " + featureResults2.getCount());	
					while (fr2 != null && fr2.hasNext())
					{
						Feature f2 = fr2.next();
						Geometry g2 = f2.getDefaultGeometry();
						if (!usedIDs.contains(f2.getID()))
						{
							
							if (!f1.getID().equals(f2.getID()))	// if they are the same feature, move onto the next one
							{
								if(g1.overlaps(g2) != expected || g1.contains(g2) != expected)
								{
									//results.error( f1, f1.getDefaultGeometry().getGeometryType()+" "+getGeomTypeRefA()+"("+f1.getID()+")"+" overlapped "+getGeomTypeRefA()+"("+f2.getID()+"), Result was not "+expected );
									results.error( f1, getGeomTypeRefA()+"("+f1.getID()+")"+" overlapped "+getGeomTypeRefA()+"("+f2.getID()+")");
									System.out.println(f1.getDefaultGeometry().getGeometryType()+" "+getGeomTypeRefA()+"("+f1.getID()+")"+" overlapped "+getGeomTypeRefA()+"("+f2.getID()+"), Result was not "+expected);
									success = false;
									errors++;
								}
							}
						}
					}
					usedIDs.add(f1.getID());
					if (counter%countInterval == 0)
						System.out.println("count: " + counter);
						
				}finally{
					if (fr2 != null)
						fr2.close();
				}
			}// end while 1
		}finally
		{
			Date date2 = new Date();
			float dt = date2.getTime() - date1.getTime();
			System.out.println("########## Validation duration: " + dt);
			System.out.println("########## Validation errors: " + errors);
			
			/** Close the connections to the feature readers*/
			try {
				fr1.close();
				if (fr2 != null)
					fr2.close();
			} catch (IOException e4) {
				e4.printStackTrace();
				throw e4;
			}
		}
		
		return success;
	}
	
	
	
	public Filter filterBBox(Envelope bBox, FeatureType ft)
		throws FactoryConfigurationError, IllegalFilterException
	{
		FilterFactory ff = FilterFactory.createFilterFactory();
		BBoxExpression bboxExpr = ff.createBBoxExpression(bBox);
		//GeometryFilter bbFilter = ff.createGeometryFilter(Filter.GEOMETRY_BBOX);
		AttributeExpression geomExpr = ff.createAttributeExpression(ft, ft.getDefaultGeometry().getName());
		GeometryFilter disjointFilter = ff.createGeometryFilter(Filter.GEOMETRY_DISJOINT);
		disjointFilter.addLeftGeometry(geomExpr);
		disjointFilter.addRightGeometry(bboxExpr);
		Filter filter = disjointFilter.not();
		
		return filter;
	}
}
