package org.geotools.data.mysql;

import org.geotools.data.AttributeWriter;
import org.geotools.data.jdbc.QueryData;
import org.geotools.feature.AttributeType;

/**
 * A MySQL-specific AttributeWriter.<br>
 * <br>
 * TODO This ought to handle MySQL 4.1's geometric datatypes, but it does not work.
 * This is because 4.1 sends geometric data in a different packet format than other
 * datatypes, and because of this the MySQL driver does not allow ResultSet objects
 * with geometric data to be updatable.  I have not found anything about this MySQL
 * bug in the MySQL bug database, so I will add a new bug there.  In the meantime,
 * this package should work fine for writing non-geometric data.
 * @author Gary Sheppard garysheppard@psu.edu
 */
public class MySQLAttributeWriter extends MySQLAttributeReader implements AttributeWriter {
    
    public MySQLAttributeWriter(AttributeType metaData, QueryData queryData, int columnIndex) {
        super(metaData, queryData, columnIndex);
    }
        
}