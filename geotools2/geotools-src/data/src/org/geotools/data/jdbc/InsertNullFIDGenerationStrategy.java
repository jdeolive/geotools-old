/* $Id: InsertNullFIDGenerationStrategy.java,v 1.1 2004/01/08 04:27:48 seangeo Exp $
 * 
 * Created on 8/01/2004
 */
package org.geotools.data.jdbc;

import org.geotools.data.DataSourceException;
import org.geotools.feature.Feature;

/** An FID generation strategy that produces null fids.
 * 
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: InsertNullFIDGenerationStrategy.java,v 1.1 2004/01/08 04:27:48 seangeo Exp $
 * Last Modified: $Date: 2004/01/08 04:27:48 $ 
 */
public class InsertNullFIDGenerationStrategy implements FIDGenerationStrategy {

    /**
     * 
     */
    public InsertNullFIDGenerationStrategy() { }

    /** Returns a null FID.
     * 
     * @see org.geotools.data.jdbc.FIDGenerationStrategy#generateFidFor(org.geotools.feature.Feature)
     * @param f The Feature.
     * @return A null fid.
     * @throws DataSourceException This never happens.
     */
    public Object generateFidFor(Feature f) throws DataSourceException {
        return null;
    }

}
