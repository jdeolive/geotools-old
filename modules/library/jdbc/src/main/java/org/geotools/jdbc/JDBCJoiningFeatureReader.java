package org.geotools.jdbc;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.factory.Hints;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

public class JDBCJoiningFeatureReader extends JDBCFeatureReader {

    List<JDBCFeatureReader> joinReaders;

    public JDBCJoiningFeatureReader(String sql, Connection cx, JDBCFeatureSource featureSource,
        SimpleFeatureType featureType, List<SimpleFeatureType> joinFeatureTypes, Hints hints) 
        throws SQLException, IOException {

        super(sql, cx, featureSource, retype(featureType, joinFeatureTypes), hints);

        joinReaders = new ArrayList<JDBCFeatureReader>();
        int offset = featureType.getAttributeCount() + getPrimaryKey().getColumns().size();

        for (SimpleFeatureType ft : joinFeatureTypes) {
            joinReaders.add(new JDBCFeatureReader(rs, cx, offset,
                featureSource.getDataStore().getAbsoluteFeatureSource(ft.getTypeName()), ft, hints)); 
        }
    }
    
    @Override
    public boolean hasNext() throws IOException {
        boolean next = super.hasNext();
        for (JDBCFeatureReader r : joinReaders) {
            r.setNext(next);
        }
        return next;
    }

    @Override
    public SimpleFeature next() throws IOException, IllegalArgumentException,
            NoSuchElementException {
        //read the regular feature
        SimpleFeature f = super.next();

        //add additional attributes for joined features
        for (int i = 0; i < joinReaders.size(); i++) {
            JDBCFeatureReader r = joinReaders.get(i);
            f.setAttribute(f.getAttributeCount() - joinReaders.size() + i, r.next());
        }

        return f;
    }

    static SimpleFeatureType retype(SimpleFeatureType featureType, List<SimpleFeatureType> joinFeatureTypes) {
        SimpleFeatureTypeBuilder b = new SimpleFeatureTypeBuilder();
        b.init(featureType);
        
        for (SimpleFeatureType ft : joinFeatureTypes) {
            b.add(ft.getTypeName(), SimpleFeature.class);
        }
        return b.buildFeatureType();
    }
}
