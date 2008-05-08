package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import org.geotools.factory.Hints;
import org.opengis.feature.simple.SimpleFeature;

public class JDBCUpdateInsertFeatureWriter extends JDBCUpdateFeatureWriter {

    JDBCInsertFeatureWriter inserter;
    
    public JDBCUpdateInsertFeatureWriter(String sql, Connection cx,
            JDBCFeatureStore featureStore, Hints hints) throws SQLException,
            IOException {
        super(sql, cx, featureStore, hints);
    }
    
    public boolean hasNext() throws IOException {
        if ( inserter != null ) {
            return inserter.hasNext();
        }
        
        //check parent
        boolean hasNext = super.hasNext();
        if ( !hasNext ) {
            //update phase is up, switch to insert mode
            inserter = new JDBCInsertFeatureWriter( this );
            return inserter.hasNext();
        }
    
        return hasNext;
    }

    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        if ( inserter != null ) {
            return inserter.next();
        }
        
        return super.next();
    }
    
    public void remove() throws IOException {
        if ( inserter != null ) {
            inserter.remove();
            return;
        }
        
        super.remove();
    }
    
    public void write() throws IOException {
        if ( inserter != null ) {
            inserter.write();
            return;
        }
        
        super.write();
    }
    
    public void close() throws IOException {
        if ( inserter != null ) {
            //JD: do not call close because the inserter borrowed all of its state
            // from this reader... super will deal with it.
            inserter = null;
            //inserter.close();
        }
        
        super.close();
    }
    
}
