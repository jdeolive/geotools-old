package org.geotools.gce.imagemosaic.jdbc;

import java.util.HashMap;
import java.util.Map;


class JDBCAccessFactory {
    static Map<String, JDBCAccess> JDBCAccessMap = new HashMap<String, JDBCAccess>();

    static synchronized JDBCAccess getJDBCAcess(Config config)
        throws Exception {
        JDBCAccess jdbcAccess = JDBCAccessMap.get(config.getXmlUrl());

        if (jdbcAccess != null) {
            return jdbcAccess;
        }

        SpatialExtension type = config.getSpatialExtension();

        if (type == null) {
            throw new Exception("Property <spatialExtension> missing");
        }

        if (type == SpatialExtension.DB2) {
            jdbcAccess = new JDBCAccessDB2(config);
        } else if (type == SpatialExtension.POSTGIS) {
            jdbcAccess = new JDBCAccessPostGis(config);
        } else if (type == SpatialExtension.MYSQL) {
            jdbcAccess = new JDBCAccessMySql(config);
        } else if (type == SpatialExtension.UNIVERSAL) {
            jdbcAccess = new JDBCAccessUniversal(config);
        } else if (type == SpatialExtension.ORACLE) {
            jdbcAccess = new JDBCAccessOracle(config);
        } else {
            throw new Exception("spatialExtension: " + type + " not supported");
        }

        jdbcAccess.initialize();
        JDBCAccessMap.put(config.getXmlUrl(), jdbcAccess);

        return jdbcAccess;
    }
}
