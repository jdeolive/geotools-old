package org.geotools.gce.imagemosaic.properties.time;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.geotools.data.DataUtilities;
import org.geotools.gce.imagemosaic.Utils;
import org.geotools.gce.imagemosaic.properties.DefaultPropertiesCollectorSPI;
import org.geotools.gce.imagemosaic.properties.PropertiesCollector;
import org.geotools.gce.imagemosaic.properties.PropertiesCollectorSPI;

public class TimestampFileNameExtractorSPI extends
		DefaultPropertiesCollectorSPI implements PropertiesCollectorSPI {

	public TimestampFileNameExtractorSPI() {
		super("TimestampFileNameExtractorSPI");
	}

	public PropertiesCollector create(
			final Object o,
			final List<String> propertyNames) {
		URL source=null;
		if(o instanceof File)
		{
			source=DataUtilities.fileToURL((File) o);
		}
		else
			if(o instanceof String)
				try {
					source=new URL((String) o);
				} catch (MalformedURLException e) {
					return null;
				}
			else
				return null;
		// it is a url
		final Properties properties = Utils.loadPropertiesFromURL(source);
		if(properties.containsKey("regex"))
			return new TimestampFileNameExtractor(this,propertyNames,properties.getProperty("regex"));
		
		return null;
		
	}

}
