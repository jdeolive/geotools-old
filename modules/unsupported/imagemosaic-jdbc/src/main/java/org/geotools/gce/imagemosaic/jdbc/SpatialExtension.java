package org.geotools.gce.imagemosaic.jdbc;

public enum SpatialExtension {DB2("DB2"),
    ORACLE("ORACLE"),
    POSTGIS("POSTGIS"),
    MYSQL("MYSQL"),
    UNIVERSAL("UNIVERSAL");
    private SpatialExtension(String name) {
        this.name = name;
    }

    private String name;

    public String toString() {
        return name;
    }

    static SpatialExtension fromString(String spatName) {
        if ("DB2".equalsIgnoreCase(spatName)) {
            return DB2;
        }

        if ("ORACLE".equalsIgnoreCase(spatName)) {
            return ORACLE;
        }

        if ("MYSQL".equalsIgnoreCase(spatName)) {
            return MYSQL;
        }

        if ("POSTGIS".equalsIgnoreCase(spatName)) {
            return POSTGIS;
        }

        if ("UNIVERSAL".equalsIgnoreCase(spatName)) {
            return UNIVERSAL;
        }

        return null;
    }
}
