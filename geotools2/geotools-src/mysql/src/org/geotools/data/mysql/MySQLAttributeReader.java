package org.geotools.data.mysql;

import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.io.IOException;
import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.ResultSetAttributeIO;
import org.geotools.feature.AttributeType;

/**
 * A MySQL-specific AttributeReader, which handles MySQL 4.1's geometric datatypes.
 * @author Gary Sheppard garysheppard@psu.edu
 */
public class MySQLAttributeReader extends ResultSetAttributeIO {
    
    public MySQLAttributeReader(AttributeType metaData, QueryData queryData, int columnIndex) {
        this(new AttributeType[] {metaData}, queryData, columnIndex, columnIndex + 1);
    }
    
    public MySQLAttributeReader(AttributeType[] metaData, QueryData queryData, int startColumn, int endColumn) {
        super(metaData, queryData, startColumn, endColumn);
    }
    
    public Object read(final int i) throws IOException, ArrayIndexOutOfBoundsException {
        Object obj = super.read(i);
        
        if (obj != null) {
            if (metaData[i].isGeometry()) {
                try {
                    obj = new WKTReader().read((String) obj);
                } catch (ParseException pe) {
                    throw new DataSourceException("Given String (" + obj + ") not parseable WKT", pe);
                }
            }
            metaData[i].validate(obj);
        }
        
        return obj;
    }
        
}