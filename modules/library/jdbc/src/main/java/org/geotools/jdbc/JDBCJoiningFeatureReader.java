package org.geotools.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.factory.Hints;
import org.opengis.feature.simple.SimpleFeatureType;

public class JDBCJoiningFeatureReader extends JDBCFeatureReader {

    List<JDBCFeatureReader> joinReaders;
    
    public JDBCJoiningFeatureReader(String sql, Connection cx, JDBCFeatureSource featureSource,
        SimpleFeatureType featureType, List<SimpleFeatureType> joinFeatureTypes, ListHints hints) throws SQLException {
        super(sql, cx, featureSource, featureType, hints);
        
        joinReaders = new ArrayList<JDBCFeatureReader>();
        int offset = featureType.getAttributeCount() + getPrimaryKey().getColumns().size();
        
        for (SimpleFeatureType ft : joinFeatureTypes) {
            joinReaders.add(new JDBCFeatureReader(rs, offset, cx,
                (featureSource.getDataStore().getFeatureSource(ft.getName()), 
        }
    }

}
