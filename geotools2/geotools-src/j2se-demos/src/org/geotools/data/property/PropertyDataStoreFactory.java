package org.geotools.data.property;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFactorySpi;
import java.io.File;
import java.io.IOException;
import java.util.Map;


public class PropertyDataStoreFactory implements DataStoreFactorySpi {
    public DataStore createDataStore(Map params) throws IOException {
        File dir = (File) params.get("directory");
        
        return new PropertyDataStore( dir );
    }
    public DataStore createNewDataStore(Map params) throws IOException {
        File dir = (File) params.get("directory");
        if (dir.exists()) {
            throw new IOException(dir + " already exists");
        }

        boolean created;
        
        created = dir.mkdir();

        if (!created) {
            throw new IOException("Could not create the directory" + dir);
        }
        return new PropertyDataStore(dir);
    }
    public String getDescription() {
        return "Allows access to Java Property files containing Feature information";
    }
    public Param[] getParametersInfo() {
        Param directory = new Param("directory", File.class,
                "Directory containting property files", true);

        return new Param[] { directory, };
    }
    
	/**
	 * Test to see if this datastore is available, if it has all the
	 * appropriate libraries to construct a datastore.  This datastore just
	 * returns true for now.  This method is used for gui apps, so as to
	 * not advertise data store capabilities they don't actually have.
	 *
	 * @return <tt>true</tt> if and only if this factory is available to create
	 *         DataStores.
	 *
	 * @task REVISIT: I'm just adding this method to compile, maintainer should
	 *       revisit to check for any libraries that may be necessary for
	 *       datastore creations. ch.
	 */
	public boolean isAvailable() {
		return true;
	}
    
    public boolean canProcess(Map params) {
        return (params != null) && params.containsKey("directory")
        && params.get("directory") instanceof File;
    }
}
