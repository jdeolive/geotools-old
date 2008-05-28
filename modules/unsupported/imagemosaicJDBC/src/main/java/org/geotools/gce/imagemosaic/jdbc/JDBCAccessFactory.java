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

        String type = config.getSpatialExtension();

        if (type == null) {
            throw new Exception("Property <spatialExtension> missing");
        }

        if (type.equalsIgnoreCase("DB2")) {
            jdbcAccess = new JDBCAccessDB2(config);
        } else if (type.equalsIgnoreCase("POSTGIS")) {
            jdbcAccess = new JDBCAccessPostGis(config);
        } else if (type.equalsIgnoreCase("MYSQL")) {
            jdbcAccess = new JDBCAccessMySql(config);
        } else if (type.equalsIgnoreCase("UNIVERSAL")) {
            jdbcAccess = new JDBCAccessUniversal(config);
        } else if (type.equalsIgnoreCase("ORACLE")) {
            jdbcAccess = new JDBCAccessOracle(config);
        } else {
            throw new Exception("spatialExtension: " + type + " not supported");
        }

        jdbcAccess.initialize();
        JDBCAccessMap.put(config.getXmlUrl(), jdbcAccess);

        return jdbcAccess;
    }
}
