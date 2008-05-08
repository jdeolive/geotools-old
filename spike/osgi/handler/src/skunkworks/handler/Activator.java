package skunkworks.handler;

import java.util.Properties;

import org.geotools.gtcatalog.Catalog;
import org.geotools.gtcatalog.ServiceFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;

public class Activator implements BundleActivator {

	public void start(BundleContext context) throws Exception {
		//get a reference to the service factory + catalog
		Catalog catalog = (Catalog) context
			.getService(context.getServiceReference(Catalog.class.getName()));
		ServiceFactory factory = (ServiceFactory)context
			.getService(context.getServiceReference(ServiceFactory.class.getName()));
		
		
		HttpService service = (HttpService)context
			.getService(context.getServiceReference(HttpService.class.getName()));
	
		service.registerServlet("/Add",new AddServiceHandler(catalog,factory),new Properties(),null);
		service.registerServlet("/List",new ListServicesHandler(catalog),new Properties(),null);
	}

	public void stop(BundleContext context) throws Exception {
		HttpService service = (HttpService)context
			.getService(context.getServiceReference(HttpService.class.getName()));

		service.unregister("/Add");
		service.unregister("/List");
	}

}
