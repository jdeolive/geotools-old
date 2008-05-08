package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.FeatureWriter;
import org.geotools.factory.Hints;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Id;

public class JDBCUpdateFeatureWriter extends JDBCFeatureReader implements
        FeatureWriter<SimpleFeatureType, SimpleFeature> {

    ResultSetFeature last;
    
    public JDBCUpdateFeatureWriter(String sql, Connection cx,
            JDBCFeatureStore featureStore, Hints hints) throws SQLException, IOException {
        
        super(sql, cx, featureStore, hints);
        last = new ResultSetFeature( rs );
    }

    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        
        ensureNext();
        
        try {
            last.init();
        } catch (Exception e) {
            e.printStackTrace();
        }
     
        //reset next flag
        next = null;
        
        return last;
    }
    
    public void remove() throws IOException {
        try {
            dataStore.delete(featureType, last.getID(), st.getConnection());
        } catch (SQLException e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    public void write() throws IOException {
        try {
            //figure out what the fid is
            PrimaryKey key = dataStore.getPrimaryKey(featureType);
            String fid = key.encode(rs);

            Id filter = dataStore.getFilterFactory()
                                 .id(Collections.singleton(dataStore.getFilterFactory()
                                                                    .featureId(fid)));

            //figure out which attributes changed
            List<AttributeDescriptor> changed = new ArrayList<AttributeDescriptor>();
            List<Object> values = new ArrayList<Object>();

            for (AttributeDescriptor att : featureType.getAttributes()) {
                if (last.isDirrty(att.getLocalName())) {
                    changed.add(att);
                    values.add(last.getAttribute(att.getLocalName()));
                }
            }

            //do the write
            dataStore.update(featureType, changed, values, filter, st.getConnection());
        } catch (Exception e) {
            throw (IOException) new IOException().initCause(e);
        }
    }

    public void close() throws IOException {
        super.close();
        last = null;
    }
}
