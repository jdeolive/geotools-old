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
    
    public void transform( Projection srcdefn, Projection dstdefn, int point_count, int point_offset,
                  double x[], double y[], double z[] )

{
    boolean       needDatumShift;

    if( point_offset == 0 )
        point_offset = 1;//erm, why? (could be to avoid mult by 0)

/* -------------------------------------------------------------------- */
/*      Transform source points to lat/long, if they aren't             */
/*      already.                                                        */
/* -------------------------------------------------------------------- */
    if( !srcdefn.is_latlong )
    {
        for(int i = 0; i < point_count; i++ )
        {
            XY  projected_loc = new XY();
            LP	geodetic_loc = new LP();

            projected_loc.u = x[point_offset*i];
            projected_loc.v = y[point_offset*i];

            geodetic_loc = pj_inv( projected_loc, srcdefn );

            x[point_offset*i] = geodetic_loc.u;
            y[point_offset*i] = geodetic_loc.v;
        }
    }
    
/* -------------------------------------------------------------------- */
/*      Convert datums if needed, and possible.                         */
/* -------------------------------------------------------------------- */
    if( pj_datum_transform( srcdefn, dstdefn, point_count, point_offset, 
                            x, y, z ) != 0 )
        return pj_errno;

/* -------------------------------------------------------------------- */
/*      Transform destination points to projection coordinates, if      */
/*      desired.                                                        */
/* -------------------------------------------------------------------- */
    if( !dstdefn->is_latlong )
    {
        for( i = 0; i < point_count; i++ )
        {
            XY         projected_loc;
            LP	       geodetic_loc;

            geodetic_loc.u = x[point_offset*i];
            geodetic_loc.v = y[point_offset*i];

            projected_loc = pj_fwd( geodetic_loc, dstdefn );
            if( pj_errno != 0 )
                return pj_errno;

            x[point_offset*i] = projected_loc.u;
            y[point_offset*i] = projected_loc.v;
        }
    }

    return 0;
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
    
    public boolean compareDatums( Projection srcdefn,Projection dstdefn ) {
        return srcdefn.isDatumEqual(dstdefn);
    }
    
    public void geocentricToWSG84( Projection defn,
    int point_count, int point_offset,
    double x[], double y[], double z[] )
    
    {
        
        int io;
        if( defn.datum_type == Projection.PJD_3PARAM ) {
            for(int i = 0; i < point_count; i++ ) {
                io = i * point_offset;
                
                x[io] = x[io] + defn.datum_params[0];
                y[io] = y[io] + defn.datum_params[1];
                z[io] = z[io] + defn.datum_params[2];
            }
        }
        else if( defn.datum_type == Projection.PJD_7PARAM ) {
            for(int i = 0; i < point_count; i++ ) {
                io = i * point_offset;
                double x_out, y_out, z_out;
                double Dx_BF = defn.datum_params[0];//not elegent!
                double Dy_BF = defn.datum_params[1];//and possibly expensive
                double Dz_BF = defn.datum_params[2];//sigh, no macros in Java
                double Rx_BF = defn.datum_params[3];//...
                double Ry_BF = defn.datum_params[4];//still, keeps it readable
                double Rz_BF = defn.datum_params[5];//if a little verbose
                double M_BF  = defn.datum_params[6];//could do with a better solution though.
                x_out = M_BF*(       x[io] - Rz_BF*y[io] + Ry_BF*z[io]) + Dx_BF;
                y_out = M_BF*( Rz_BF*x[io] +       y[io] - Rx_BF*z[io]) + Dy_BF;
                z_out = M_BF*(-Ry_BF*x[io] + Rx_BF*y[io] +       z[io]) + Dz_BF;
                
                x[io] = x_out;
                y[io] = y_out;
                z[io] = z_out;
            }
        }
        
    }
    
    public void geocentricFromWSG84( Projection defn,
    int point_count, int point_offset,
    double x[], double y[], double z[] ) {
        
        int io;
        if( defn.datum_type == Projection.PJD_3PARAM ) {
            
            for(int i = 0; i < point_count; i++ ) {
                io = i * point_offset;
                
                x[io] = x[io] - defn.datum_params[0];
                y[io] = y[io] - defn.datum_params[1];
                z[io] = z[io] - defn.datum_params[2];
            }
        }
        else if( defn.datum_type == Projection.PJD_7PARAM ) {
            for(int i = 0; i < point_count; i++ ) {
                io = i * point_offset;
                double x_out, y_out, z_out;
                double Dx_BF = defn.datum_params[0];//not elegent!
                double Dy_BF = defn.datum_params[1];//and possibly expensive
                double Dz_BF = defn.datum_params[2];//sigh, no macros in Java
                double Rx_BF = defn.datum_params[3];//...
                double Ry_BF = defn.datum_params[4];//still, keeps it readable
                double Rz_BF = defn.datum_params[5];//if a little verbose
                double M_BF  = defn.datum_params[6];//could do with a better solution though.
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
    
    public void datumTransform( Projection srcdefn, Projection dstdefn,
    int point_count, int point_offset,
    double x[], double y[], double z[] )throws ProjectionException,GeocentricException
    
    {
        double      src_a, src_es, dst_a, dst_es;
        
        /* -------------------------------------------------------------------- */
        /*      Short cut if the datums are identical.                          */
        /* -------------------------------------------------------------------- */
        if( compareDatums( srcdefn, dstdefn ) )
            return;
        
        src_a = srcdefn.a;
        src_es = srcdefn.es;
        
        dst_a = dstdefn.a;
        dst_es = dstdefn.es;
        
        /* -------------------------------------------------------------------- */
        /*	If this datum requires grid shifts, then apply it to geodetic   */
        /*      coordinates.                                                    */
        /* -------------------------------------------------------------------- */
        if( srcdefn.datum_type == Projection.PJD_GRIDSHIFT ) {
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
        
        if( dstdefn.datum_type == Projection.PJD_GRIDSHIFT ) {
            dst_a = SRS_WGS84_SEMIMAJOR;
            dst_es = 0.006694379990;
        }
        
        /* ==================================================================== */
        /*      Do we need to go through geocentric coordinates?                */
        /* ==================================================================== */
        if( srcdefn.datum_type == Projection.PJD_3PARAM
        || srcdefn.datum_type == Projection.PJD_7PARAM
        || dstdefn.datum_type == Projection.PJD_3PARAM
        || dstdefn.datum_type == Projection.PJD_7PARAM) {
            /* -------------------------------------------------------------------- */
            /*      Convert to geocentric coordinates.                              */
            /* -------------------------------------------------------------------- */
            geodeticToGeocentric( src_a, src_es,
            point_count, point_offset, x, y, z );
            
            
            /* -------------------------------------------------------------------- */
            /*      Convert between datums.                                         */
            /* -------------------------------------------------------------------- */
            if( srcdefn.datum_type != Projection.PJD_UNKNOWN
            && dstdefn.datum_type != Projection.PJD_UNKNOWN ) {
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
        if( dstdefn.datum_type == Projection.PJD_GRIDSHIFT ) {
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