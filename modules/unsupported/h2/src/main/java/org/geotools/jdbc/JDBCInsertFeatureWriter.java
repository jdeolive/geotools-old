package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.geotools.data.FeatureWriter;
import org.geotools.factory.Hints;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class JDBCInsertFeatureWriter extends JDBCFeatureReader implements FeatureWriter<SimpleFeatureType, SimpleFeature> {

    
    ResultSetFeature last;
    
    public JDBCInsertFeatureWriter(String sql, Connection cx,
            JDBCFeatureStore featureStore, Hints hints) throws SQLException, IOException {
        super(sql, cx, featureStore, hints);
        last = new ResultSetFeature( rs );
    }

    
    public JDBCInsertFeatureWriter(JDBCUpdateFeatureWriter other) {
        super(other);
        last = other.last;
    }

    public boolean hasNext() throws IOException {
        return false;
    }

    public SimpleFeature next() throws IOException {
        //init, setting id to null explicity since the feature is yet to be 
        // inserted
        last.init(null);
        return last;
    }

    public void remove() throws IOException {
        //noop
    }

    public void write() throws IOException {
        try {
            //do the insert
            dataStore.insert(last, featureType, st.getConnection());
            
            //the datastore sets as userData, grab it and update the fid
            String fid = (String) last.getUserData().get( "fid" );
            last.setID( fid );
        } catch (SQLException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    public void close() throws IOException {
        super.close();
        last = null;
    }
}
