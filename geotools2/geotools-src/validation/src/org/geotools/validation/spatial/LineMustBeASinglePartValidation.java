/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation.spatial;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.geotools.feature.Feature;
import org.geotools.feature.FeatureType;
import org.geotools.validation.DefaultFeatureValidation;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;


/**
 * LineIsSingleSegmentFeatureValidation purpose.
 * <p>
 * Tests to see if a LineString is made of only one segment, meaning it only has
 * two points. If the LineString has more than two points, the test fails.
 * <p>
 * Capabilities:
 * <ul>
 * </ul>
 * Example Use:
 * <pre><code>
 * LineIsSingleSegmentFeatureValidation x = new LineIsSingleSegmentFeatureValidation("noSelfIntersectRoads", "Tests to see if a 
 * geometry intersects itself", new String[] {"road"});
 * </code></pre>
 * 
 * @author bowens, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: LineMustBeASinglePartValidation.java,v 1.1 2004/02/13 03:07:59 jive Exp $
 */
public class LineMustBeASinglePartValidation extends DefaultFeatureValidation {
    /** The logger for the validation module. */
    private static final Logger LOGGER = Logger.getLogger(
            "org.geotools.validation");
            
	/**
	 * LineIsSingleSegmentFeatureValidation constructor.
	 * <p>
	 * Description
	 * </p>
	 * 
	 */
	public LineMustBeASinglePartValidation() {
	}

	/**
	 * Override getPriority.
	 * <p>
	 * Sets the priority level of this validation.
	 * </p>
	 * @see org.geotools.validation.Validation#getPriority()
	 * 
	 * @return A made up priority for this validation.
	 */
	public int getPriority() {
		return 10;
	}

	/**
	 * Override validate.
	 * <p>
	 * Tests to see if a LineString is made of only one segment, meaning it only has
	 * two points. If the LineString has more than two points, the test fails.
	 * </p>
	 * @see org.geotools.validation.FeatureValidation#validate(org.geotools.feature.Feature, org.geotools.feature.FeatureTypeInfo, org.geotools.validation.ValidationResults)
	 * 
 	 * @param feature The Feature to be validated
	 * @param type The FeatureTypeInfo of the feature
	 * @param results The storage for error messages.
	 * @return True if the feature is simple (one segment).
	 */
	public boolean validate(
		Feature feature,
		FeatureType type,
		ValidationResults results){
		
		LOGGER.setLevel(Level.ALL);   
		
		Geometry geom =  feature.getDefaultGeometry();
        if( geom == null )
        {
			results.error(feature, "Geometry is null - cannot validate.");
			return false;
        }
        
    	if (geom instanceof LineString)
    	{
    		if (geom.getNumPoints() < 2)
    		{
				results.error(feature, "LineString contains too few points - cannot validate.");
    			return false;
    		}
    		
    		GeometryFactory gf = new GeometryFactory();
    		
			// get the LineString out of the Geometry
			LineString ls = (LineString)geom;
			if (ls.getNumPoints() > 2)
			{
				// log the error and return
				String message = "LineString is not a simple segment (more than 1 segment exists).";
				results.error(feature, message );
				LOGGER.log( Level.FINEST, getName()+"("+feature.getID()+"):"+message );                
				return false;
			}
	
    	}
    	else
    	{
			results.error(feature, "Geometry not a LineString - cannot validate.");
			return false;
    	}
            

        LOGGER.log( Level.FINEST, getName()+"("+feature.getID()+") passed" );
       
		return true;
	}
	

}
