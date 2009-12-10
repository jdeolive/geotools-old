package org.geotools.gce.imagemosaic.properties.time;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.geotools.gce.imagemosaic.properties.PropertiesCollector;
import org.geotools.gce.imagemosaic.properties.PropertiesCollectorSPI;
import org.geotools.util.logging.Logging;
import org.opengis.feature.simple.SimpleFeature;

public class TimestampFileNameExtractor extends PropertiesCollector {
	private final static Logger LOGGER= Logging.getLogger(TimestampFileNameExtractor.class);
	
	private static final TimeParser parser= new TimeParser();
	private Pattern pattern;

	@Override
	public TimestampFileNameExtractor collect(File file) {
		super.collect(file);
		
		// get name of the file
		final String name= FilenameUtils.getBaseName(file.getAbsolutePath());
		
		// get matches 
		final Matcher matcher = pattern.matcher(name);
		 while (matcher.find()) {
			 addMatch(matcher.group());
         }
		
		 return this;
	}

	public TimestampFileNameExtractor(
			PropertiesCollectorSPI spi,
			List<String> propertyNames,
			String regex) {
		super(spi,  propertyNames);
		pattern = Pattern.compile(regex);

	}

	@Override
	public void setProperties(SimpleFeature feature) {
		
		// get all the matches and convert them in times
		final List<Date> dates= new ArrayList<Date>();
		for(String match:getMatches()){
			// try to convert to date
			try {
				dates.addAll(parser.parse(match));
			} catch (ParseException e) {
				if(LOGGER.isLoggable(Level.FINE))
					LOGGER.log(Level.FINE,e.getLocalizedMessage(),e);
			}
			
		}
		
		// set the properties
		int index=0;
		for(String propertyName:getPropertyNames()){
			// set the property
			feature.setAttribute(propertyName, dates.get(index++));
			
			// do we have more dates?
			if(index>=dates.size())
				return;
		}
	}

}
