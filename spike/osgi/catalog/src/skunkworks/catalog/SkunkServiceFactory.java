package skunkworks.catalog;


import java.util.ArrayList;
import java.util.List;

import org.geotools.gtcatalog.Catalog;
import org.geotools.gtcatalog.ServiceExtension;
import org.geotools.gtcatalog.ServiceFactory;
import org.geotools.gtcatalog.defaults.DefaultServiceFactory;

public class SkunkServiceFactory extends DefaultServiceFactory 
	implements ServiceFactory {

	List extensions = new ArrayList();
	
	public SkunkServiceFactory(Catalog catalog) {
		super(catalog);
	}
	
	protected List getServiceExtensions() {
		return extensions;
	}

	public void addServiceExtension(ServiceExtension ext) {
		extensions.add(ext);
	}
	
	public void removeServiceExtension(ServiceExtension ext) {
		extensions.remove(ext);
	}
	
}
