/* $Id: FIDGenerationStrategy.java,v 1.1 2004/01/07 00:53:27 seangeo Exp $
 * 
 * Created on 7/01/2004
 */
package org.geotools.data.jdbc;

import org.geotools.data.DataSourceException;
import org.geotools.feature.Feature;

/**
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: FIDGenerationStrategy.java,v 1.1 2004/01/07 00:53:27 seangeo Exp $
 * Last Modified: $Date: 2004/01/07 00:53:27 $ 
 */
public interface FIDGenerationStrategy {
    public Object generateFidFor(Feature f) throws DataSourceException;
}
