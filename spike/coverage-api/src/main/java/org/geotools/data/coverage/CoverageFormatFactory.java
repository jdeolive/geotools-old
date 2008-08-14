package org.geotools.data.coverage;

import java.io.IOException;
import java.net.URL;

/**
 * CoverageAccessFactory for working with formats based on a single URL.
 */
public interface CoverageFormatFactory extends CoverageAccessFactory {
    /**
     * The list of filename extensions handled by this factory.
     *
     * @return List of file extensions which can be read by this
     *         dataStore.
     */
    public String[] getFileExtensions();
    
    /**
     * True if the url can be handled by this factory.
     *
     * @param f URL a url to a real file (may not be local)
     *
     * @return True when this dataStore can resolve and read the data specified
     *         by the URL.
     */
    public boolean canProcess(URL f);
    
    /**
     * A DataStore attached to the provided URL, may be created if needed.
     *
     * @param url A URL to the data location for the single featureType of this
     *        DataStore
     *
     * @return Returns an AbstractFileDataStore created from the data source
     *         provided.
     *
     * @throws IOException
     *
     * @see AbstractFileDataStore
     */
    public CoverageAccess connect(URL url) throws IOException;

}
