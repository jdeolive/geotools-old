/*
 * Transform.java
 *
 * Created on 19 February 2002, 23:10
 */

package org.geotools.proj4j;

/**
 *
 * @author  James Macgill
 */
public class Transformer {
    public static final double SRS_WGS84_SEMIMAJOR = 6378137.0;
    /** Creates a new instance of Transform */
    public Transformer() {
    }
    //instead of thowing, it should probably just set MAX_VALUE for all that fail
    public void transform( Projection srcdefn, Projection dstdefn, int point_count, int point_offset,
    double x[], double y[], double z[] ) throws ProjectionException
    
    {
        boolean       needDatumShift;
        if(dstdefn==null){
            dstdefn = srcdefn.getLatLongProjection();
        }
        else if(srcdefn==null){
            srcdefn = dstdefn.getLatLongProjection();
        }
        if( point_offset == 0 )
            point_offset = 1;//erm, why? (could be to avoid mult by 0)
        
        /* -------------------------------------------------------------------- */
        /*      Transform source points to lat/long, if they aren't             */
        /*      already.                                                        */
        /* -------------------------------------------------------------------- */
        if( !srcdefn.isLatLong ) {
            for(int i = 0; i < point_count; i++ ) {
                XY  projected_loc = new XY();
                LP	geodetic_loc = new LP();
                
                projected_loc.x = x[point_offset*i];
                projected_loc.y = y[point_offset*i];
                
                geodetic_loc = srcdefn.inverse( projected_loc );
                
                x[point_offset*i] = geodetic_loc.lam;
                y[point_offset*i] = geodetic_loc.phi;
            }
        }
        
        // --------------------------------------------------------------------
        //      Convert datums if needed, and possible.
        // --------------------------------------------------------------------
        datumTransform( srcdefn, dstdefn, point_count, point_offset,x, y, z );
        
        //--------------------------------------------------------------------
        //      Transform destination points to projection coordinates, if
        //      desired.
        // --------------------------------------------------------------------
        if( !dstdefn.isLatLong ) {
            for( int i = 0; i < point_count; i++ ) {
                XY         projected_loc;
                LP	       geodetic_loc = new LP();
                
                geodetic_loc.lam = x[point_offset*i];
                geodetic_loc.phi = y[point_offset*i];
                
                projected_loc = dstdefn.forward( geodetic_loc );
                
                x[point_offset*i] = projected_loc.x;
                y[point_offset*i] = projected_loc.y;
            }
        }
    }
    
    
    
    public void geodeticToGeocentric( double a, double es,
    int point_count, int point_offset,
    double x[], double y[], double z[] ) throws GeocentricException{
        double b;
        
        if( es == 0.0 ){
            b = a;
        }
        else{
            b = a * Math.sqrt(1-es);
        }
        Geocentric geocent = new Geocentric(a,b);
        double result[] = new double[3];
        int io;
        for(int  i = 0; i < point_count; i++ ) {
            io = i * point_offset;
            result = geocent.convertGeodeticToGeocentric( y[io], x[io], z[io]);
            x[io]=result[0];
            y[io]=result[1];
            z[io]=result[2];
        }
    }
    
    /************************************************************************/
    /*                     pj_geodetic_to_geocentric()                      */
    /************************************************************************/
    
    public void geocentricToGeodetic( double a, double es,
    int point_count, int point_offset,
    double x[], double y[], double z[] ) throws GeocentricException
    
    {
        double b;
        
        if( es == 0.0 )
            b = a;
        else
            b = a * Math.sqrt(1-es);
        
        Geocentric geocent = new Geocentric(a,b);
        int io;
        double result[];
        for(int i = 0; i < point_count; i++ ) {
            io = i * point_offset;
            
            result=geocent.convertGeocentricToGeodetic( x[io], y[io], z[io] );
            y[io]=result[0];
            x[io]=result[1];
            z[io]=result[2];
        }
    }
    
    /************************************************************************/
    /*                         pj_compare_datums()                          */
    /*                                                                      */
    /*      Returns TRUE if the two datums are identical, otherwise         */
    /*      FALSE.                                                          */
    /************************************************************************/
    
    private boolean compareDatums( Projection srcdefn,Projection dstdefn ) {
        return srcdefn.isDatumEqual(dstdefn);
    }
    
    private void geocentricToWSG84( Projection defn,
    int point_count, int point_offset,
    double x[], double y[], double z[] )
    
    {
        
        int io;
        if( defn.datum.datumType == Datum.PJD_3PARAM ) {
            for(int i = 0; i < point_count; i++ ) {
                io = i * point_offset;
                
                x[io] = x[io] + defn.datum.getParams()[0];
                y[io] = y[io] + defn.datum.getParams()[1];
                z[io] = z[io] + defn.datum.getParams()[2];
            }
        }
        else if( defn.datum.datumType == Datum.PJD_7PARAM ) {
            for(int i = 0; i < point_count; i++ ) {
                io = i * point_offset;
                double x_out, y_out, z_out;
                double Dx_BF = defn.datum.getParams()[0];//not elegent!
                double Dy_BF = defn.datum.getParams()[1];//and possibly expensive
                double Dz_BF = defn.datum.getParams()[2];//sigh, no macros in Java
                double Rx_BF = defn.datum.getParams()[3];//...
                double Ry_BF = defn.datum.getParams()[4];//still, keeps it readable
                double Rz_BF = defn.datum.getParams()[5];//if a little verbose
                double M_BF  = defn.datum.getParams()[6];//could do with a better solution though.
                x_out = M_BF*(       x[io] - Rz_BF*y[io] + Ry_BF*z[io]) + Dx_BF;
                y_out = M_BF*( Rz_BF*x[io] +       y[io] - Rx_BF*z[io]) + Dy_BF;
                z_out = M_BF*(-Ry_BF*x[io] + Rx_BF*y[io] +       z[io]) + Dz_BF;
                
                x[io] = x_out;
                y[io] = y_out;
                z[io] = z_out;
            }
        }
        
    }
    
    private void geocentricFromWSG84( Projection defn,
    int point_count, int point_offset,
    double x[], double y[], double z[] ) {
        
        int io;
        if( defn.datum.datumType == Datum.PJD_3PARAM ) {
            
            for(int i = 0; i < point_count; i++ ) {
                io = i * point_offset;
                
                x[io] = x[io] - defn.datum.getParams()[0];
                y[io] = y[io] - defn.datum.getParams()[1];
                z[io] = z[io] - defn.datum.getParams()[2];
            }
        }
        else if( defn.datum.datumType == Datum.PJD_7PARAM ) {
            for(int i = 0; i < point_count; i++ ) {
                io = i * point_offset;
                double x_out, y_out, z_out;
                double Dx_BF = defn.datum.getParams()[0];//not elegent!
                double Dy_BF = defn.datum.getParams()[1];//and possibly expensive
                double Dz_BF = defn.datum.getParams()[2];//sigh, no macros in Java
                double Rx_BF = defn.datum.getParams()[3];//...
                double Ry_BF = defn.datum.getParams()[4];//still, keeps it readable
                double Rz_BF = defn.datum.getParams()[5];//if a little verbose
                double M_BF  = defn.datum.getParams()[6];//could do with a better solution though.
                x_out = M_BF*(       x[io] + Rz_BF*y[io] - Ry_BF*z[io]) - Dx_BF;
                y_out = M_BF*(-Rz_BF*x[io] +       y[io] + Rx_BF*z[io]) - Dy_BF;
                z_out = M_BF*( Ry_BF*x[io] - Rx_BF*y[io] +       z[io]) - Dz_BF;
                
                x[io] = x_out;
                y[io] = y_out;
                z[io] = z_out;
            }
        }
    }
    
    
    /************************************************************************/
    /*                         pj_datum_transform()                         */
    /************************************************************************/
    
    private void datumTransform( Projection srcdefn, Projection dstdefn,
    int point_count, int point_offset,
    double x[], double y[], double z[] )throws ProjectionException,GeocentricException
    
    {
        double      src_a, src_es, dst_a, dst_es;
        
        /* -------------------------------------------------------------------- */
        /*      Short cut if the datums are identical.                          */
        /* -------------------------------------------------------------------- */
        if( compareDatums( srcdefn, dstdefn ) )
            return;
        
        src_a = srcdefn.ellipse.a;
        src_es = srcdefn.ellipse.es;
        
        dst_a = dstdefn.ellipse.a;
        dst_es = dstdefn.ellipse.es;
        
        /* -------------------------------------------------------------------- */
        /*	If this datum requires grid shifts, then apply it to geodetic   */
        /*      coordinates.                                                    */
        /* -------------------------------------------------------------------- */
        if( srcdefn.datum.datumType == Datum.PJD_GRIDSHIFT ) {
            throw new ProjectionException("Grid shifts not yet supported");
        /*
        pj_apply_gridshift( pj_param(srcdefn->params,"snadgrids").s, 0,
                            point_count, point_offset, x, y, z );
         
        if( pj_errno != 0 )
            return pj_errno;
         
        src_a = SRS_WGS84_SEMIMAJOR;
        src_es = 0.006694379990;
         */
        }
        
        if( dstdefn.datum.datumType == Datum.PJD_GRIDSHIFT ) {
            dst_a = SRS_WGS84_SEMIMAJOR;
            dst_es = 0.006694379990;
        }
        
        /* ==================================================================== */
        /*      Do we need to go through geocentric coordinates?                */
        /* ==================================================================== */
        if( srcdefn.datum.datumType == Datum.PJD_3PARAM
        || srcdefn.datum.datumType == Datum.PJD_7PARAM
        || dstdefn.datum.datumType == Datum.PJD_3PARAM
        || dstdefn.datum.datumType == Datum.PJD_7PARAM) {
            /* -------------------------------------------------------------------- */
            /*      Convert to geocentric coordinates.                              */
            /* -------------------------------------------------------------------- */
            geodeticToGeocentric( src_a, src_es,
            point_count, point_offset, x, y, z );
            
            
            /* -------------------------------------------------------------------- */
            /*      Convert between datums.                                         */
            /* -------------------------------------------------------------------- */
            if( srcdefn.datum.datumType != Datum.PJD_UNKNOWN
            && dstdefn.datum.datumType != Datum.PJD_UNKNOWN ) {
                geocentricToWSG84( srcdefn, point_count, point_offset,x,y,z);
                geocentricFromWSG84( dstdefn, point_count,point_offset,x,y,z);
            }
            
            /* -------------------------------------------------------------------- */
            /*      Convert back to geodetic coordinates.                           */
            /* -------------------------------------------------------------------- */
            geocentricToGeodetic( dst_a, dst_es,
            point_count, point_offset, x, y, z );
            
        }
        
        /* -------------------------------------------------------------------- */
        /*      Apply grid shift to destination if required.                    */
        /* -------------------------------------------------------------------- */
        if( dstdefn.datum.datumType == Datum.PJD_GRIDSHIFT ) {
            throw new ProjectionException("Grid shifts not yet supported");
        /*
        pj_apply_gridshift( pj_param(dstdefn->params,"snadgrids").s, 1,
                            point_count, point_offset, x, y, z );
         
        if( pj_errno != 0 )
            return pj_errno;
         */
        }
        
        
    }
    
    
}