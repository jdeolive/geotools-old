package org.geotools.data.coverage;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.geotools.factory.Hints;

/**
 * CoverageAccessFactory for working with formats based on a single URL.
 * 
 * @author Simone Giannecchini, GeoSolusions
 * @author Jody Garnett
 */
public interface CoverageFormatFactory extends CoverageAccessFactory {
    /**
     * The list of filename extensions handled by this factory.
     *
     * @return List of file extensions which can be read by this
     *         dataStore.
     */
    public List<String> getFileExtensions();
    
    /**
     * True if the {@link URL} can be handled by this factory.
     *
     * @param f URL a {@link URL} to a real file (may not be local)
     *
     * @return True when this dataStore can resolve and read the data specified
     *         by the {@link URL}.
     */
    public boolean canConnect(URL f);
    
    /**
     * A DataStore attached to the provided {@link URL}, may be created if needed.
     *
     * @param url A {@link URL} to the data location for the single featureType of this
     *        DataStore
     *
     * @return Returns an AbstractFileDataStore created from the data source
     *         provided.
     *
     * @throws IOException
     *
     * @see AbstractFileDataStore
     */
    public CoverageAccess connect(URL url,Hints hints) throws IOException;

}
