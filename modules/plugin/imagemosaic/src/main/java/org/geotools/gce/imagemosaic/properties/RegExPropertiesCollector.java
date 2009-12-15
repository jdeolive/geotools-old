package org.geotools.gce.imagemosaic.properties;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

public abstract class RegExPropertiesCollector extends PropertiesCollector {
	
	public RegExPropertiesCollector(
			PropertiesCollectorSPI spi,
			List<String> propertyNames,
			String regex) {
		super(spi, propertyNames);
		pattern = Pattern.compile(regex);
	}

	private Pattern pattern;

	@Override
	public RegExPropertiesCollector collect(File file) {
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

}
