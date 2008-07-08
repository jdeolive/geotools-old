/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.gce.imagemosaic.jdbc;

import com.vividsolutions.jts.geom.Envelope;

import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.awt.image.ColorModel;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;


/**
 * Java Bean for Level Info
 *
 * @author mcr
 *
 */
class ImageLevelInfo implements Comparable<ImageLevelInfo> {
    private ColorModel colorModel;
    private CoordinateReferenceSystem crs;
    private Integer srsId;
    private String coverageName;
    private Double extentMinX;
    private Double extentMinY;
    private Double extentMaxX;
    private Double extentMaxY;
    private Double resX;
    private Double resY;
    private String tileTableName;
    private String spatialTableName;
    private Integer countFeature;
    private Integer countTiles;
    private double[] resolution = null;
    private Envelope envelope = null;

    String getCoverageName() {
        return coverageName;
    }

    void setCoverageName(String coverageName) {
        this.coverageName = coverageName;
    }

    Double getExtentMaxX() {
        return extentMaxX;
    }

    void setExtentMaxX(Double extentMaxX) {
        this.extentMaxX = extentMaxX;
        envelope = null;
    }

    Double getExtentMaxY() {
        return extentMaxY;
    }

    void setExtentMaxY(Double extentMaxY) {
        this.extentMaxY = extentMaxY;
        envelope = null;
    }

    Double getExtentMinX() {
        return extentMinX;
    }

    void setExtentMinX(Double extentMinX) {
        this.extentMinX = extentMinX;
        envelope = null;
    }

    Double getExtentMinY() {
        return extentMinY;
    }

    void setExtentMinY(Double extentMinY) {
        this.extentMinY = extentMinY;
        envelope = null;
    }

    Double getResX() {
        return resX;
    }

    void setResX(Double resX) {
        this.resX = resX;
        resolution = null;
    }

    Double getResY() {
        return resY;
    }

    void setResY(Double resY) {
        this.resY = resY;
        resolution = null;
    }

    String getSpatialTableName() {
        return spatialTableName;
    }

    void setSpatialTableName(String spatialTableName) {
        this.spatialTableName = spatialTableName;
    }

    String getTileTableName() {
        return tileTableName;
    }

    void setTileTableName(String tileTableName) {
        this.tileTableName = tileTableName;
    }

    @Override
    public String toString() {
        return "Coverage: " + getCoverageName() + ":" + getSpatialTableName() +
        ":" + getTileTableName();
    }

    public int compareTo(ImageLevelInfo other) {
        int res = 0;

        if ((res = getCoverageName().compareTo(other.getCoverageName())) != 0) {
            return res;
        }

        if ((res = getResX().compareTo(other.getResX())) != 0) {
            return res;
        }

        if ((res = getResY().compareTo(other.getResY())) != 0) {
            return res;
        }

        return 0;
    }

    double[] getResolution() {
        if (resolution != null) {
            return resolution;
        }

        resolution = new double[2];

        if (getResX() != null) {
            resolution[0] = getResX().doubleValue();
        }

        if (getResY() != null) {
            resolution[1] = getResY().doubleValue();
        }

        return resolution;
    }

    Envelope getEnvelope() {
        if (envelope != null) {
            return envelope;
        }

        if ((getExtentMaxX() == null) || (getExtentMaxY() == null) ||
                (getExtentMinX() == null) || (getExtentMinY() == null)) {
            return null;
        }

        envelope = new Envelope(getExtentMinX().doubleValue(),
                getExtentMaxX().doubleValue(), getExtentMinY().doubleValue(),
                getExtentMaxY().doubleValue());

        return envelope;
    }

    Integer getCountFeature() {
        return countFeature;
    }

    void setCountFeature(Integer countFeature) {
        this.countFeature = countFeature;
    }

    Integer getCountTiles() {
        return countTiles;
    }

    void setCountTiles(Integer countTiles) {
        this.countTiles = countTiles;
    }

    CoordinateReferenceSystem getCrs() {
        return crs;
    }

    void setCrs(CoordinateReferenceSystem crs) {
        this.crs = crs;
    }

    ColorModel getColorModel() {
        return colorModel;
    }

    void setColorModel(ColorModel colorModel) {
        this.colorModel = colorModel;
    }

    boolean calculateResolutionNeeded() {
        return (getResX() == null) || (getResY() == null);
    }

    boolean calculateExtentsNeeded() {
        return (getExtentMaxX() == null) || (getExtentMaxY() == null) ||
        (getExtentMinX() == null) || (getExtentMinY() == null);
    }

    String infoString() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        PrintWriter w = new PrintWriter(bout);
        w.print("Coveragename: ");
        w.println(getCoverageName());

        if (getCrs() != null) {
            w.print("CoordinateRefernceSystem: ");
            w.println(getCrs().getName());
        }

        if (getSrsId() != null) {
            w.print("SRS_ID: ");
            w.println(getSrsId());
        }

        w.print("Colormodel: ");
        w.println(getColorModel());

        w.print("Envelope: ");
        w.println(getEnvelope());

        w.print("Resolution X: ");
        w.println(getResX());

        w.print("Resolution Y: ");
        w.println(getResY());

        w.print("Tiletable: ");
        w.print(getTileTableName());

        if (getCountTiles() != null) {
            w.print(" #tiles: ");
            w.println(getCountTiles());
        }

        w.print("Spatialtable: ");
        w.print(getSpatialTableName());

        if (getCountFeature() != null) {
            w.print(" #geometries: ");
            w.println(getCountFeature());
        }

        w.close();

        return bout.toString();
    }

    Integer getSrsId() {
        return srsId;
    }

    void setSrsId(Integer srsId) {
        this.srsId = srsId;
    }

    boolean isImplementedAsTableSplit() {
        return getSpatialTableName().equals(getTileTableName()) == false;
    }
}
