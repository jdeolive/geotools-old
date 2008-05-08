package skunkworks.catalog.wms;

import org.geotools.gtcatalog.ServiceFactory;
import org.geotools.gtcatalog.wms.WMSServiceExtension;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import skunkworks.catalog.SkunkServiceFactory;

public class Activator implements BundleActivator {

	WMSServiceExtension ext;

	public void start(BundleContext context) throws Exception {
		
		//get the catalog service and register the service extension
		ext = new WMSServiceExtension();
		
		ServiceReference ref = 
			context.getServiceReference(ServiceFactory.class.getName());
		SkunkServiceFactory factory = (SkunkServiceFactory) context.getService(ref);
		factory.addServiceExtension(ext);
	}

	public void stop(BundleContext context) throws Exception {
		//unregister the service extension
		ServiceReference ref = 
			context.getServiceReference(ServiceFactory.class.getName());
		SkunkServiceFactory factory = (SkunkServiceFactory) context.getService(ref);
		factory.removeServiceExtension(ext);
	}

}
