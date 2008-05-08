package skunkworks.catalog;

import java.util.Properties;

import org.geotools.gtcatalog.Catalog;
import org.geotools.gtcatalog.ServiceFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		//register the catalog and service factory
		Properties props = new Properties();
		props.put("Language", "English");
		
		Catalog catalog = new SkunkCatalog();
		ServiceFactory factory = new SkunkServiceFactory(catalog);
		
		context.registerService(Catalog.class.getName(),catalog,props);
		context.registerService(ServiceFactory.class.getName(),factory,props);
	}

	public void stop(BundleContext context) throws Exception {
		//do nothing
	}
	
}
