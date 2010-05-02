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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

import javax.media.jai.PlanarImage;
import javax.sql.DataSource;

import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.jdbc.datasource.DataSourceFinder;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.sun.media.jai.codec.ByteArraySeekableStream;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageDecoder;
import com.sun.media.jai.codec.SeekableStream;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.WKBReader;

/**
 * This class is used for JDBC Access to the Oracle GeoRaster feature
 * 
 * @author Christian Mueller based on the code of Steve Way and Pablo Najarro
 * 
 * 
 **/

class JDBCAccessOracleGeoRaster implements JDBCAccess {

    static String StmtTemplatePixel = "SELECT "
            + " sdo_geor.getCellCoordinate(%s, %s, sdo_geometry(2001,%s,sdo_point_type(?,?,null), null,null)), "
            + " sdo_geor.getCellCoordinate(%s, %s, sdo_geometry(2001,%s,sdo_point_type(?,?,null), null,null)) "
            + " from %s where %s = ?";

    static String StmtTemplateExport = "declare " + "gr sdo_georaster; " + "lb blob; " + "begin "
            + "dbms_lob.createtemporary(lb,true); "
            + "select a.%s into gr from %s a where a.%s = ?; "
            + "sdo_geor.exportTo(gr, 'pLevel=%d cropArea=(%d,%d,%d,%d)', '%s', lb); " + "?:=lb; "
            + "end; ";

    private boolean isCellXYOrder;

    boolean isCellXYOrder() {
        return isCellXYOrder;
    }

    void setCellXYOrder(boolean isCellXYOrder) {
        this.isCellXYOrder = isCellXYOrder;
    }

    protected final static Logger LOGGER = Logger.getLogger(JDBCAccessOracleGeoRaster.class
            .getPackage().getName());

    protected List<ImageLevelInfo> levelInfos = new ArrayList<ImageLevelInfo>();

    protected Config config;

    protected DataSource dataSource;

    /**
     * 
     * @param config
     * Config from XML file passed to this class
     * 
     **/
    JDBCAccessOracleGeoRaster(Config config) throws IOException {
        super();
        this.config = config;
        this.dataSource = DataSourceFinder.getDataSource(config.getDataSourceParams());
    }

    /* (non-Javadoc)
     * @see org.geotools.gce.imagemosaic.jdbc.JDBCAccess#initialize()
     * 
     * Gathers the initial meta data needed
     */
    public void initialize() {

        LOGGER.fine("Starting GeoRaster Image Mosaic");

        Connection con = null;
        try {

            con = getConnection();
            int srid = getSRID(con);
            CoordinateReferenceSystem crs = getCRS();
            Envelope extent = getExtent(con);
            setCellXYOrder(con, srid, extent);
            double[] spatialResolutions = getSpatialResolutions(con);
            int numberOfPyramidLevels = getPyramidLevels(con);

            LOGGER.fine("Base Spatial Resolution X: " + spatialResolutions[0] + ", Y: "
                    + spatialResolutions[1]);
            LOGGER.fine("Number of Pyramids" + numberOfPyramidLevels);

            LOGGER.fine("minX " + extent.getMinX());
            LOGGER.fine("maxX " + extent.getMaxX());
            LOGGER.fine("maxY " + extent.getMaxY());
            LOGGER.fine("minY " + extent.getMinY());

            for (int i = 0; i < numberOfPyramidLevels; i++) {

                ImageLevelInfo imageLevel = new ImageLevelInfo();

                imageLevel.setCoverageName(config.getCoverageName());
                imageLevel.setSpatialTableName(new String(i + ""));
                imageLevel.setTileTableName(new String(i + ""));

                imageLevel.setResX(spatialResolutions[0] * (Math.pow(2, i)));
                imageLevel.setResY(spatialResolutions[1] * (Math.pow(2, i)));

                imageLevel.setExtentMinX(extent.getMinX());
                imageLevel.setExtentMinY(extent.getMinY());
                imageLevel.setExtentMaxX(extent.getMaxX());
                imageLevel.setExtentMaxY(extent.getMaxY());

                imageLevel.setSrsId(srid);
                imageLevel.setCrs(crs);
                levelInfos.add(imageLevel);

                LOGGER.fine("New Level Info for Coverage: " + config.getCoverageName()
                        + " Pyramid Level: " + imageLevel.getSpatialTableName());
                LOGGER.fine("Resolution X: " + imageLevel.getResX());
                LOGGER.fine("Resolution Y: " + imageLevel.getResY());
                LOGGER.fine("SRID: " + imageLevel.getSrsId());
                LOGGER.fine("CRS: " + imageLevel.getCrs());
            }

            LOGGER.fine("Image Level List Size: " + levelInfos.size());

        } finally {
            closeConnection(con);

        }

    }

    /**
     * getLevelInfo
     * 
     * @param level
     *            Pyramid Level Information
     * @return ImageLevelInfo
     **/

    public ImageLevelInfo getLevelInfo(int level) {
        LOGGER.fine("getLevelInfo Method");
        return levelInfos.get(level);
    }

    /**
     * getNumOverviews
     * 
     * 
     * @return int
     **/

    public int getNumOverviews() {
        LOGGER.fine("getNumOverviews Method");
        return levelInfos.size() - 1;
    }

    /**
     *getCRS
     * 
     * @return CoordinateReferenceSystem
     **/

    private CoordinateReferenceSystem getCRS() {

        LOGGER.fine("getCRS Method");

        CoordinateReferenceSystem crs = null;

        try {

            crs = CRS.decode(config.getCoordsys());
            LOGGER.fine("CRS get Identifier" + crs.getIdentifiers());

        } catch (Exception e) {            
            LOGGER.severe("Cannot parse Decode CRS from Config File " + e.getMessage());
            throw new RuntimeException(e);
        } finally {

        }

        LOGGER.fine("Returning CRS Result");

        return crs;

    }

    /**
     * getExtent of pyramid level 0
     * 
     * 
     * @return Envelope
     **/

    private Envelope getExtent(Connection con) {

        LOGGER.fine("Get Extent Method");

        String extentSelectLBX = "select sdo_geometry.get_wkb(sdo_geor.generateSpatialExtent("
                + config.getGeoRasterAttribute() + ")) from " + config.getMasterTable() + " where "
                + config.getCoverageNameAttribute() + "=?";

        PreparedStatement s = null;
        ResultSet r = null;
        Envelope extent = null;

        try {

            s = con.prepareStatement(extentSelectLBX);
            s.setString(1, config.getCoverageName());
            r = s.executeQuery();
            r.next();
            byte[] wkb = r.getBytes(1);
            Geometry geom = new WKBReader().read(wkb);
            extent = geom.getEnvelopeInternal();
            LOGGER.fine("creating Extent");

        } catch (Exception e) {
            throw new RuntimeException(e);

        } finally {

            closeResultSet(r);
            closePreparedStmt(s);

        }

        LOGGER.fine("returning Extent");
        return extent;
    }

    /**
     * getSRID
     * 
     * 
     * @return int
     **/

    private int getSRID(Connection con) {

        LOGGER.fine("getSRId Method");

        String SRSSelect = "select sdo_geor.getModelSRID(" + config.getGeoRasterAttribute()
                + ") from " + config.getMasterTable() + " where "
                + config.getCoverageNameAttribute() + "=?";

        PreparedStatement stmt = null;
        ResultSet rs = null;
        int srid = 0;

        try {
            stmt = con.prepareStatement(SRSSelect);

            stmt.setString(1, config.getCoverageName());
            rs = stmt.executeQuery();

            if (rs.next()) {
                srid = rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {

            closeResultSet(rs);
            closePreparedStmt(stmt);

        }

        return srid;

    }

    /**
     * getSpatialResolutions based on x/y cell order
     * 
     * 
     * @return double[]
     **/

    private double[] getSpatialResolutions(Connection con) {

        LOGGER.fine("getSpatialResolution Method");

        String sqlSpatialResolution = "select sdo_geor.getspatialresolutions("
                + config.getGeoRasterAttribute() + ") from " + config.getMasterTable() + " where "
                + config.getCoverageNameAttribute() + "=?";

        LOGGER.fine("Sptial Reso SQL:" + sqlSpatialResolution);

        double[] spatialResolution = new double[2];
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            stmt = con.prepareStatement(sqlSpatialResolution);
            stmt.setString(1, config.getCoverageName());
            rs = stmt.executeQuery();
            rs.next();
            Array array = rs.getArray(1);
            BigDecimal[] javaArray = (BigDecimal[]) array.getArray();
            if (isCellXYOrder()) {
                spatialResolution[0] = javaArray[0].doubleValue();
                spatialResolution[1] = javaArray[1].doubleValue();
            } else {
                spatialResolution[1] = javaArray[0].doubleValue();
                spatialResolution[0] = javaArray[1].doubleValue();

            }
            LOGGER.fine("Assigned X Value: " + spatialResolution[0]);
            LOGGER.fine("Assigned Y Value: " + spatialResolution[1]);
        } catch (Exception ex) {
            LOGGER.severe("Failure getting spatial resolution");
        } finally {
            closeResultSet(rs);
            closePreparedStmt(stmt);

        }

        LOGGER.fine("getSpatialResolution Finished");
        return spatialResolution;
    }

    /**
     * getPyramidLevels
     * 
     * 
     * @return Returns Number of Pyramids Available for Coverage
     **/

    private int getPyramidLevels(Connection con) {

        LOGGER.fine("getPyrmidLevels Method");

        String sqlPyramidLevels = "select sdo_geor.getPyramidMaxLevel("
                + config.getGeoRasterAttribute() + ") from " + config.getMasterTable() + " where "
                + config.getCoverageNameAttribute() + " = ?";

        PreparedStatement stmt = null;
        ResultSet rs = null;
        int numberOfPyramidLevels = 0;

        try {

            LOGGER.fine("get pyramid level sql: " + sqlPyramidLevels);
            stmt = con.prepareStatement(sqlPyramidLevels);
            stmt.setString(1, config.getCoverageName());
            rs = stmt.executeQuery();

            if (rs.next()) {
                LOGGER.fine("Assiging number of levels");
                numberOfPyramidLevels = rs.getInt(1) + 1;
                LOGGER.fine("Assigned number of levels");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            closeResultSet(rs);
            closePreparedStmt(stmt);
        }

        LOGGER.fine("Returning Pyramid Levels");
        return numberOfPyramidLevels;

    }

    /**
     * startTileDecoders
     * 
     * @param pixelDimension
     *            Not Used (passed as per interface requirement)
     * 
     * @param requestEnvelope
     *            Geographic Envelope of request
     * 
     * @param info
     *            Pyramid Level
     * 
     * @param tileQueue
     *            Queue to place retrieved tile into
     * 
     * @param coverageFactory
     *            not used (passed as per interface requirement)
     * 
     **/

    public void startTileDecoders(Rectangle pixelDimension, GeneralEnvelope requestEnvelope,
            ImageLevelInfo info, LinkedBlockingQueue<TileQueueElement> tileQueue,
            GridCoverageFactory coverageFactory) throws IOException {

        long start = System.currentTimeMillis();
        LOGGER.fine("Starting GeoRaster Tile Decoder");

        Connection con = null;

        con = getConnection();
        TileQueueElement tqe = getSingleTQElement(requestEnvelope, info, con);
        tileQueue.add(tqe);
        closeConnection(con);
        tileQueue.add(TileQueueElement.ENDELEMENT);

        LOGGER.fine("Finished GeoRaster Tile Decoder");

        LOGGER.info("GeoRaster Generation time: " + (System.currentTimeMillis() - start));

    }

    /**
     * Check for x y order of cell coordinates (EPSG:4326 has y/x order)
     * 
     * The test is done by getting cell coordinates for the ULC and URC of world coordinates. For
     * the ULC, the result must be 0,0
     * 
     * For the URC, either the first or the second value must be 0. URC first value == 0 --> y/x
     * order URC second value == 0 --> x/y order
     * 
     * 
     * @param con
     *            Jdbc Connection
     * @param srid
     *            Spatial Reference Identifier
     * @param extent
     *            Extent of pyramid level 0
     */
    protected void setCellXYOrder(Connection con, int srid, Envelope extent) {

        PreparedStatement s = null;
        ResultSet r = null;

        try {

            // Determine pixel x y order
            String pixelStmt = String.format(StmtTemplatePixel, config.getGeoRasterAttribute(),
                    "0", Integer.toString(srid), config.getGeoRasterAttribute(), "0", Integer
                            .toString(srid), config.getMasterTable(), config
                            .getCoverageNameAttribute());

            // Setting world coordinates for ULC and URC
            PreparedStatement ps = con.prepareStatement(pixelStmt);
            ps.setDouble(1, extent.getMinX());
            ps.setDouble(2, extent.getMaxY());
            ps.setDouble(3, extent.getMaxX());
            ps.setDouble(4, extent.getMaxY());
            ps.setString(5, config.getCoverageName());

            r = ps.executeQuery();
            if (r.next()) {
                BigDecimal[] pixelCoords1 = (BigDecimal[]) r.getArray(1).getArray();
                BigDecimal[] pixelCoords2 = (BigDecimal[]) r.getArray(2).getArray();
                int minx = pixelCoords1[0].intValue();
                int miny = pixelCoords1[1].intValue();
                int maxx = pixelCoords2[0].intValue();
                int miny2 = pixelCoords2[1].intValue();
                if (minx != 0 || miny != 0) {
                    throw new RuntimeException("Error, ULC must have pixelcoordinates 0/0");
                }
                if (miny2 == 0) // x,y order
                    setCellXYOrder(true);
                else if (maxx == 0) // y,x order
                    setCellXYOrder(false);
                else
                    throw new RuntimeException("Error, URC must have one pixel ordinate == 0");
            } else {
                throw new RuntimeException("Error, cannot determine pixel ordinate order");
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        } finally {
            closeResultSet(r);
            closeStmt(s);
        }

    }

    /**
     * getConnection
     * 
     * @return Connection
     **/

    protected Connection getConnection() {

        Connection con = null;
        try {

            con = dataSource.getConnection();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return con;
    }

    /**
     * closeConnection
     * 
     * @param conn
     *            Connection Object passed to be closed
     **/

    protected void closeConnection(Connection con) {
        try {

            if (con != null) {
                con.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * closePreparedStatement
     * 
     * @param stmt
     *            PreparedStatement Object passed to be closed
     **/

    protected void closePreparedStmt(PreparedStatement stmt) {
        try {

            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * closeStmt
     * 
     * @param stmt
     *            Statement Object passed to be closed
     **/

    protected void closeStmt(Statement stmt) {

        try {

            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * closeResultSet
     * 
     * @param rs
     *            ResultSet Object passed to be closed
     **/

    protected void closeResultSet(ResultSet rs) {
        try {

            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 
     * @param envelopeOrig  Envelope in world coords
     * @param info          ImageLevelInfo    
     * @param conn          Database Connection
     * @return  TileQueueElement containing the georeferenced 
     * cropped image
     */
    public TileQueueElement getSingleTQElement(GeneralEnvelope envelopeOrig, ImageLevelInfo info,
            Connection conn) {

        int level = Integer.parseInt(info.getTileTableName());
        BufferedImage bimg = null;
        PreparedStatement ps = null;
        CallableStatement cs = null;
        ResultSet r = null;

        // check a against the extent of the pyramid level          
        GeneralEnvelope envelope = new GeneralEnvelope(envelopeOrig);
        GeneralEnvelope intersectEnvelope = new GeneralEnvelope(new double[] {
                info.getExtentMinX(), info.getExtentMinY() }, new double[] { info.getExtentMaxX(),
                info.getExtentMaxY() });
        intersectEnvelope.setCoordinateReferenceSystem(envelope.getCoordinateReferenceSystem());
        envelope.intersect(intersectEnvelope);

        try {

            LOGGER.fine("Starting to Retrieve GeoRaster Image");

            
            // Query for the cell/pixel coordinates corresponding to the request envelope
            String pixelStmt = String.format(StmtTemplatePixel, config.getGeoRasterAttribute(),
                    Integer.toString(level), Integer.toString(info.getSrsId()), config
                            .getGeoRasterAttribute(), Integer.toString(level), Integer
                            .toString(info.getSrsId()), config.getMasterTable(), config
                            .getCoverageNameAttribute());

            ps = conn.prepareStatement(pixelStmt);

            ps.setDouble(1, envelope.getMinimum(0));
            ps.setDouble(2, envelope.getMaximum(1));
            ps.setDouble(3, envelope.getMaximum(0));
            ps.setDouble(4, envelope.getMinimum(1));
            ps.setString(5, config.getCoverageName());

            r = ps.executeQuery();
            BigDecimal[] pixelCoords1 = null;
            BigDecimal[] pixelCoords2 = null;

            if (r.next()) {
                pixelCoords1 = (BigDecimal[]) r.getArray(1).getArray();
                pixelCoords2 = (BigDecimal[]) r.getArray(2).getArray();
            } else {
                throw new RuntimeException("No cell/pixel coordinates for world Envelope "+envelope);
            }
            
            r.close();
            ps.close();

            // Export the georaster object, cropped by cell coordinates as a TIFF image into a BLOB
            String stmt = String.format(StmtTemplateExport, config.getGeoRasterAttribute(), config
                    .getMasterTable(), config.getCoverageNameAttribute(), level, pixelCoords1[0]
                    .intValue(), pixelCoords1[1].intValue(), pixelCoords2[0].intValue(),
                    pixelCoords2[1].intValue(), "TIFF");

            cs = conn.prepareCall(stmt);
            cs.setString(1, config.getCoverageName());

            cs.registerOutParameter(2, Types.BLOB);
            cs.execute();
            Blob blob = cs.getBlob(2);
            byte[] bytes = blob.getBytes(1, (int) blob.length());
            cs.close();

            // start creating a java Buffered image from the blob
            SeekableStream stream = new ByteArraySeekableStream(bytes);
            
            // find an ImageDecorder
            String decoderName = null;
            for (String dn : ImageCodec.getDecoderNames(stream)) {
                decoderName = dn;
                break;
            }
            // decode Image
            ImageDecoder decoder = ImageCodec.createImageDecoder(decoderName, stream, null);
            RenderedImage rimage = decoder.decodeAsRenderedImage();
            
            // Check for the color model, if there is none, create one
            ColorModel cm = rimage.getColorModel();
            if (cm == null)
                cm = PlanarImage.createColorModel(rimage.getSampleModel());
            
            // Convert to BufferedImage
            PlanarImage pimage = PlanarImage.wrapRenderedImage(rimage);
            bimg = pimage.getAsBufferedImage(null, cm);

            LOGGER.fine("Creating BufferedImage from GeoRaster Object");

            //LOGGER.fine("Writing Retrieved Image to disk (Should be for Debugging only!)");
            // ImageIO.write(bimg,"png", new File("/tmp/pics/test.png"));

            return new TileQueueElement(config.getCoverageName(), bimg, envelope);

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeResultSet(r);
            closeStmt(ps);
            closeStmt(cs);
        }
    }
}
