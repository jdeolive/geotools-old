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
    public boolean canProcess(Map params) {
        return (params != null) && params.containsKey("directory")
        && params.get("directory") instanceof File;
    }
}
