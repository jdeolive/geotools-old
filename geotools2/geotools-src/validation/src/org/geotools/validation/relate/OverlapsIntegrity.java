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
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import org.geotools.data.DefaultQuery;
import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.Filter;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @author bowens
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class OverlapsIntegrity extends RelationIntegrity 
{
	private static final Logger LOGGER = Logger.getLogger("org.geotools.validation");
	
	
	/**
	 * 
	 */
	public OverlapsIntegrity()
	{
		super();
	}
	
	
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
			return validateSingleLayer(geomSource1, false, results);	//TODO fixme not to be false, grab actual expected value from beaninfo
		else
		{
			LOGGER.finer( typeRef2 +": looking up FeatureSource " );        
			FeatureSource geomSource2 = (FeatureSource) layers.get( typeRef2 );
			LOGGER.finer( typeRef2 +": found "+ geomSource2.getSchema().getTypeName() );
			return validateMultipleLayers(geomSource1, geomSource2, false, results);
		}	
	
	}


	/**
	 * @param featureReaderA
	 * @param featureReaderB
	 * @return
	 */
	private boolean validateMultipleLayers(FeatureSource featureSourceA, FeatureSource featureSourceB, boolean expected, ValidationResults results) 
	{
		boolean success = true;
		
		return success;
	}


	/**
	 * @return
	 */
	private boolean validateSingleLayer(FeatureSource featureSourceA, boolean expected, ValidationResults results) 
	{
		boolean success = true;
		
		FeatureReader featureReader1 = null;
		try {
			featureReader1 = featureSourceA.getDataStore().getFeatureReader(null, null);//TODO fixme (jody)
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		FeatureReader featureReader2 = null;
		try {
			featureReader2 = featureSourceA.getDataStore().getFeatureReader(null, null);//TODO fixme (jody)
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try 
		{
			while (featureReader1.hasNext())
			{
				Feature f1 = featureReader1.next();
				Geometry g1 = f1.getDefaultGeometry();
				while (featureReader2.hasNext())
				{
					Feature f2 = featureReader2.next();
					Geometry g2 = f2.getDefaultGeometry();
					if (!f1.getID().equals(f2.getID()))	// if they are the same feature, move onto the next one
					{
						if(g1.overlaps(g2) != expected || g1.contains(g2) != expected)
						{
							results.error( f1, "Geometry "+getGeomTypeRefA()+" overlapped "+getGeomTypeRefA()+"("+f2.getID()+") was not "+expected );
							success = false;
						}
					}
				}		
			}
		} catch (NoSuchElementException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		} catch (IllegalAttributeException e2) {
			e2.printStackTrace();
		}
		
		return success;
	}
}
