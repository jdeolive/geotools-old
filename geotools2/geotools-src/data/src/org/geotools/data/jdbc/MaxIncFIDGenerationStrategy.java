/* $Id: MaxIncFIDGenerationStrategy.java,v 1.1 2004/01/08 04:27:48 seangeo Exp $
 * 
 * Created on 8/01/2004
 */
package org.geotools.data.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.geotools.data.DataSourceException;
import org.geotools.feature.Feature;

/** An FID generation strategy that manually increments the MAX FID.
 *  This method should be used when there is no auto-incrementing available.  
 *  It will take the MAX of the current FID and manually increment it by 1.  
 *  This is a last resort method when nothing else is available, it is not 
 *  the recommended method.
 * 
 * @author Sean Geoghegan, Defence Science and Technology Organisation
 * @author $Author: seangeo $
 * @version $Id: MaxIncFIDGenerationStrategy.java,v 1.1 2004/01/08 04:27:48 seangeo Exp $
 * Last Modified: $Date: 2004/01/08 04:27:48 $
 */
public final class MaxIncFIDGenerationStrategy implements FIDGenerationStrategy {    
    private final QueryData queryData;
    
    /**
     * 
     * @param info
     * @param queryData
     */
    public MaxIncFIDGenerationStrategy(QueryData queryData) {
        super();        
        this.queryData = queryData;
    }
    
    /** Generates an FID by manually incrementing the max fid by 1.
     * 
     * @see org.geotools.data.jdbc.FIDGenerationStrategy#generateFidFor(org.geotools.feature.Feature)
     * @param f The Feature.
     * @return The max fid in the column + 1.
     * @throws DataSourceException
     */
    public Object generateFidFor(Feature f) throws DataSourceException {
        JDBCDataStore.FeatureTypeInfo info = queryData.getFeatureTypeInfo();
        try {
            Integer newFid = null;
            Connection conn = queryData.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("Select MAX(" + info.getFidColumnName()
                            + ") from " + info.getFeatureTypeName());
            if (rs.next()) {
                try {
                    int maxFid = rs.getInt(1);
                    newFid = new Integer(maxFid + 1);
                } catch (SQLException e) {
                    throw new DataSourceException("Error getting max FID from result set." +
                            "It is likely that the fid column is not");
                }
            } else {
                throw new DataSourceException("Could not get MAX for " + info.getFeatureTypeName() 
                        + "." + info.getFidColumnName() + ": No result returned from query");
            }
            return newFid;
        } catch (SQLException e) {
            throw new DataSourceException("Error executing MAX query in FID Generation", e);
                    
        }
    }
}