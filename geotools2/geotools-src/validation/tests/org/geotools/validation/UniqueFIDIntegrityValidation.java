/* Copyright (c) 2001, 2003 TOPP - www.openplans.org.  All rights reserved.
 * This code is licensed under the GPL 2.0 license, availible at the root
 * application directory.
 */
package org.geotools.validation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.geotools.data.FeatureReader;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;

import com.vividsolutions.jts.geom.Envelope;

/**
 * UniqueFIDIntegrityValidation purpose.
 * <p>
 * Description of UniqueFIDIntegrityValidation ...
 * <p>
 * Capabilities:
 * <ul>
 * </li></li>
 * </ul>
 * Example Use:
 * <pre><code>
 * UniqueFIDIntegrityValidation x = new UniqueFIDIntegrityValidation(...);
 * </code></pre>
 * 
 * @author bowens, Refractions Research, Inc.
 * @author $Author: dmzwiers $ (last modification)
 * @version $Id: UniqueFIDIntegrityValidation.java,v 1.1 2004/02/17 17:19:12 dmzwiers Exp $
 */
public class UniqueFIDIntegrityValidation implements IntegrityValidation {


	private String name;
	private String description;
	private String[] typeNames;
	private String uniqueID;
		
		
	/**
	 * UniqueFIDIntegrityValidation constructor.
	 * <p>
	 * Description
	 * </p>
	 * 
	 */
	public UniqueFIDIntegrityValidation() {
	}

	/**
	 * UniqueFIDIntegrityValidation constructor.
	 * <p>
	 * Description
	 * </p>
	 * @param name
	 * @param description
	 * @param typeNames
	 */
	public UniqueFIDIntegrityValidation(String name, String description, String[] typeNames, String uniqueID) {
		this.name = name;
		this.description = description;
		this.typeNames = typeNames;
		this.uniqueID = uniqueID;
	}

	/**
	 * Override setName.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#setName(java.lang.String)
	 * 
	 * @param name
	 * @return
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Override getName.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#getName()
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Override setDescription.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#setDescription(java.lang.String)
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Override getDescription.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#getDescription()
	 * 
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Override getPriority.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#getPriority()
	 * 
	 * @return
	 */
	public int getPriority() {
		return 10;
	}

	/**
	 * Override setTypeNames.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#setTypeNames(java.lang.String[])
	 * 
	 * @param names
	 */
	public void setTypeNames(String[] names) {
		this.typeNames = names;
	}

	/**
	 * Override getTypeNames.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.Validation#getTypeNames()
	 * 
	 * @return
	 */
	public String[] getTypeRefs() {
		return typeNames;
	}


	/**
	 * Override validate.
	 * <p>
	 * Description ...
	 * </p>
	 * @see org.geotools.validation.IntegrityValidation#validate(java.util.Map, com.vividsolutions.jts.geom.Envelope, org.geotools.validation.ValidationResults)
	 * 
	 * @param layers
	 * @param envelope
	 * @param results
	 * @return
	 */
	public boolean validate(Map layers, Envelope envelope, ValidationResults results) throws Exception{
		
		HashMap FIDs = new HashMap();
		boolean result = true;
		Iterator it = layers.values().iterator();
		
		while (it.hasNext())// for each layer
		{
			FeatureSource featureSource = (FeatureSource) it.next();
			FeatureReader reader = featureSource.getFeatures().reader();
			try {
				 
				while (reader.hasNext())	// for each feature
				{
					Feature feature = reader.next();
					String fid = feature.getID();
					if(FIDs.containsKey(fid))	// if a FID like this one already exists
					{
						results.error(feature, "FID already exists.");
						result = false;
					}
					else
						FIDs.put(fid, fid);
				}
			}
			finally {
				reader.close();		// this is an important line	
			}

		}
		
		return result;
	}
}
