/*
 * Created on 16/10/2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.geotools.data.oracle;

import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import oracle.jdbc.OracleConnection;
import oracle.sdoapi.OraSpatialManager;
import oracle.sdoapi.adapter.AdapterSDO;
import oracle.sdoapi.adapter.GeometryInputTypeNotSupportedException;
import oracle.sdoapi.adapter.GeometryOutputTypeNotSupportedException;
import oracle.sdoapi.geom.GeometryFactory;
import oracle.sdoapi.geom.InvalidGeometryException;
import oracle.sdoapi.sref.SRException;
import oracle.sdoapi.sref.SRManager;
import oracle.sdoapi.sref.SpatialReference;
import oracle.sql.STRUCT;

import org.geotools.data.DataSourceException;
import org.geotools.data.jdbc.QueryData;
import org.geotools.data.jdbc.ResultSetAttributeIO;
import org.geotools.data.jdbc.QueryData.RowData;
import org.geotools.feature.AttributeType;

import com.vividsolutions.jts.geom.Geometry;

/**
 * @author Sean Geoghegan, Defence Science and Technology Organisation.
 *
 */
public class OracleSDOAttributeReader extends ResultSetAttributeIO {
    
    private static final Logger LOGGER = Logger.getLogger("org.geotools.data.oracle");

    // geometry adpaters
    private AdapterJTS adapterJTS;
    private AdapterSDO adapterSDO;        
    private int columnIndex;    
    private QueryData queryData;
    
    /**
     * 
     * @param metaData
     * @param queryData
     * @param columnIndex
     * @throws DataSourceException
     */
    public OracleSDOAttributeReader(AttributeType metaData, QueryData queryData, int columnIndex) 
                throws DataSourceException {
        super(new AttributeType[]{metaData},queryData,columnIndex,columnIndex + 1);
        this.queryData = queryData;
        this.columnIndex = columnIndex;
        
        try {
            String tableName = queryData.getFeatureTypeInfo().getFeatureTypeName();
            String columnName = metaData.getName();
            LOGGER.fine("About to create Geometry convertor for " + tableName + "." + columnName);
            
            // TODO should check that it is an OracleConnection
            OracleConnection conn = (OracleConnection)queryData.getConnection();
            
            GeometryFactory gFact = null;
            
            int srid = queryData.getFeatureTypeInfo().getSRID(columnName);
            
            if (srid != -1) {
                SRManager srManager = OraSpatialManager.getSpatialReferenceManager(conn);
                SpatialReference sr = srManager.retrieve(srid);
                gFact = OraSpatialManager.getGeometryFactory(sr);                
            } else  {
                gFact = OraSpatialManager.getGeometryFactory();
            }
            
            adapterSDO = new AdapterSDO(gFact, conn);
            adapterJTS = new AdapterJTS(gFact);
        } catch (SQLException e) {
            queryData.close( e, this );
            String msg = "Error setting up SDO Geometry convertor";
            LOGGER.log(Level.SEVERE,msg,e);         
            throw new DataSourceException(msg + ":" + e.getMessage(), e);                                    
        } catch (SRException e) {
            throw new DataSourceException("Error setting up SDO Geometry convertor", e);
        }
    }

    /* (non-Javadoc)
     * @see org.geotools.data.AttributeReader#read(int)
     */
    public Object read(int i) throws IOException, ArrayIndexOutOfBoundsException {
        if (isClosed()) {
            throw new IOException("Close has already been called on this AttributeReader.");
        }
        
        // this Reader only reads one attribute so i should always be 0
        if (i != 0)  {
            throw new ArrayIndexOutOfBoundsException("This Reader only reads one attribute so i should always be 0");
        }
        
        try {
            RowData rd = queryData.getRowData(this);
            Object struct = rd.read(columnIndex);
            oracle.sdoapi.geom.Geometry sdoGeom = adapterSDO.importGeometry(struct);
            return adapterJTS.exportGeometry(Geometry.class, sdoGeom);
        } catch (SQLException e) {
            String msg = "SQL Exception reading geometry column";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        } catch (InvalidGeometryException e) {
            String msg = "Problem with the geometry";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        } catch (GeometryInputTypeNotSupportedException e) {
            String msg = "Geometry Conversion type error";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        } catch (GeometryOutputTypeNotSupportedException e) {
            String msg = "Geometry Conversion type error";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        }        
    }

    /* (non-Javadoc)
     * @see org.geotools.data.AttributeWriter#write(int, java.lang.Object)
     */
    public void write(int position, Object attribute) throws IOException {
        if (isClosed()) {
            throw new IOException("Close has already been called on this AttributeReader.");
        }
        
        // this Reader only reads one attribute so i should always be 0
        if (position != 0)  {
            throw new ArrayIndexOutOfBoundsException("This Reader only reads one attribute so i should always be 0");
        }
            
        try {            
            oracle.sdoapi.geom.Geometry sdoGeom = adapterJTS.importGeometry(attribute);
            Object o = adapterSDO.exportGeometry(STRUCT.class, sdoGeom);            
            RowData rd = queryData.getRowData(this);
            rd.write(o, columnIndex);
        } catch (SQLException sqlException ) {
            queryData.close( sqlException, this );
            String msg = "SQL Exception writing geometry column";
            LOGGER.log(Level.SEVERE, msg, sqlException);
            throw new DataSourceException(msg, sqlException );
        } catch (InvalidGeometryException e) {
            String msg = "Problem with the geometry";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        } catch (GeometryInputTypeNotSupportedException e) {
            String msg = "Geometry Conversion type error";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        } catch (GeometryOutputTypeNotSupportedException e) {
            String msg = "Geometry Conversion type error";
            LOGGER.log(Level.SEVERE, msg, e);
            throw new DataSourceException(msg, e);
        }
    }
}
