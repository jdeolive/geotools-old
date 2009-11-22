/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *
 *   (C) 2003-2006, Geotools Project Managment Committee (PMC)
 *   (C) 2002, Centre for Computational Geography
 *   (C) 2001, Institut de Recherche pour le D�veloppement
 *   (C) 2000, Frank Warmerdam
 *   (C) 1999, Fisheries and Oceans Canada
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    This package contains formulas from the PROJ package of USGS.
 *    USGS's work is fully acknowledged here. This derived work has
 *    been relicensed under LGPL with Frank Warmerdam's permission.
 */
package org.geotools.referencing.operation.projection;

import java.awt.geom.Point2D;

import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.operation.projection.MapProjection.AbstractProvider;
import org.geotools.referencing.operation.projection.TransverseMercator.Provider;
import org.geotools.resources.i18n.ErrorKeys;
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.opengis.metadata.Identifier;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.operation.CylindricalProjection;
import org.opengis.referencing.operation.MathTransform;
/**
 * Cassini-Soldner Projection (EPSG code 9806).
 * The Cassini-Soldner Projection is the ellipsoidal version of the Cassini 
 * projection for the sphere. It is not conformal but as it is relatively simple
 * to construct it was extensively used in the last century and is still useful
 * for mapping areas with limited longitudinal extent. It has now largely
 * been replaced by the conformal Transverse Mercator which it resembles. Like this,
 * it has a straight central meridian along which the scale is true, all other
 * meridians and parallels are curved, and the scale distortion increases
 * rapidly with increasing distance from the central meridian. 
 */
public class CassiniSoldner extends MapProjection {

	/**
     * Maximum number of iterations for iterative computations.
     */
    private static final int MAXIMUM_ITERATIONS = 15;
	
	/**
     * Meridian distance at the {@code latitudeOfOrigin}.
     * Used for calculations for the ellipsoid.
     */
    private final double ml0;
	
	 /**
     * Constant needed for the <code>mlfn<code> method.
     * Setup at construction time.
     */
    private final double en0,en1,en2,en3,en4;
    
    /**
     * Constants used to calculate {@link #en0}, {@link #en1},
     * {@link #en2}, {@link #en3}, {@link #en4}.
     */
    private static final double C00= 1.0,
                                C02= 0.25,
                                C04= 0.046875,
                                C06= 0.01953125,
                                C08= 0.01068115234375,
                                C22= 0.75,
                                C44= 0.46875,
                                C46= 0.01302083333333333333,
                                C48= 0.00712076822916666666,
                                C66= 0.36458333333333333333,
                                C68= 0.00569661458333333333,
                                C88= 0.3076171875;
	
    /**
     * Contants used for the forward and inverse transform for the eliptical
     * case of the Cassini-Soldner.
     */
    private static final double  C1= 0.16666666666666666666,
    							 C2= 0.08333333333333333333,
    							 C3= 0.41666666666666666666,
    							 C4= 0.33333333333333333333,
    							 C5= 0.66666666666666666666;
    
    
    /**
     * Relative iteration precision used in the <code>mlfn<code> method. This 
     * overrides the value in the MapProjection class.
     */
    private static final double TOL = 1E-11;
    
	protected CassiniSoldner(ParameterValueGroup values)
			throws ParameterNotFoundException {
		super(values);
		//  Compute constants
		double t;
		en0 = C00 - excentricitySquared  *  (C02 + excentricitySquared  * 
	             (C04 + excentricitySquared  *  (C06 + excentricitySquared  * C08)));
        en1 =       excentricitySquared  *  (C22 - excentricitySquared  *
             (C04 + excentricitySquared  *  (C06 + excentricitySquared  * C08)));
        en2 =  (t = excentricitySquared  *         excentricitySquared) * 
             (C44 - excentricitySquared  *  (C46 + excentricitySquared  * C48));
        en3 = (t *= excentricitySquared) *  (C66 - excentricitySquared  * C68);
        en4 =   t * excentricitySquared  *  C88;
        ml0 = mlfn(latitudeOfOrigin, Math.sin(latitudeOfOrigin), Math.cos(latitudeOfOrigin));
	}
	
	/**
     * {@inheritDoc}
     */
	public ParameterDescriptorGroup getParameterDescriptors() {
		return Provider.PARAMETERS;
	}
	/**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate
     * and stores the result in {@code ptDst}.
     */
	protected Point2D inverseTransformNormalized(double x, double y,
			Point2D ptDst) throws ProjectionException {
		double ph1=inv_mlfn(ml0+y);
		double tn=Math.tan(ph1);
		double t=tn*tn;
		double n=Math.sin(ph1);
		double r=1.0/(1.0-excentricitySquared*n*n);
		n=Math.sqrt(r);
		r*=(1.0-excentricitySquared)*n;
		double dd=x/n;
		double d2=dd*dd;
		double phi=ph1-(n*tn/r)*d2*(0.5-(1.0+3.0*t)*d2*C3);
		double lam=dd*(1.0+t*d2*(-C4+(1.0+3.0*t)*d2*C5))/Math.cos(ph1);
		if (ptDst != null) {
            ptDst.setLocation(lam,phi);
            return ptDst;
        }
        return new Point2D.Double(lam,phi);
	}

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinate (units in radians)
     * and stores the result in {@code ptDst} (linear distance on a unit sphere).
     */
	protected Point2D transformNormalized(double lam, double phi, Point2D ptDst)
			throws ProjectionException {
		double sinphi = Math.sin(phi); 
        double cosphi = Math.cos(phi); 
        
        double n=1.0/(Math.sqrt(1.0-excentricitySquared*sinphi*sinphi));
        double tn=Math.tan(phi);
        double t=tn*tn;
        double a1=lam*cosphi;
        double c=cosphi*cosphi*excentricitySquared/(1-excentricitySquared);
        double a2=a1*a1;
                
        double x = n*a1*(1.0-a2*t*(C1-(8.0-t+8.0*c)*a2*C2));
        double y = (mlfn(phi, sinphi, cosphi))-ml0+n*tn*a2*(0.5+(5.0-t+6.0*c)*a2*C3);
        
        if (ptDst != null) {
            ptDst.setLocation(x,y);
            return ptDst;
        }
        return new Point2D.Double(x,y);
	}
	
	/**
     * Calculates the meridian distance. This is the distance along the central 
     * meridian from the equator to {@code phi}. Accurate to < 1e-5 meters 
     * when used in conjuction with typical major axis values.
     *
     * @param phi latitude to calculate meridian distance for.
     * @param sphi sin(phi).
     * @param cphi cos(phi).
     * @return meridian distance for the given latitude.
     */
    private final double mlfn(final double phi, double sphi, double cphi) {        
        cphi *= sphi;
        sphi *= sphi;
        return en0 * phi - cphi *
              (en1 + sphi *
              (en2 + sphi *
              (en3 + sphi *
              (en4))));
    }
    
    /**
     * Calculates the latitude ({@code phi}) from a meridian distance.
     * Determines phi to TOL (1e-11) radians, about 1e-6 seconds.
     * 
     * @param arg meridian distance to calulate latitude for.
     * @return the latitude of the meridian distance.
     * @throws ProjectionException if the itteration does not converge.
     */
    private final double inv_mlfn(double arg) throws ProjectionException {
        double s, t, phi, k = 1.0/(1.0 - excentricitySquared);
        int i;
        phi = arg;
        for (i=MAXIMUM_ITERATIONS; true;) { // rarely goes over 5 iterations
            if (--i < 0) {
                throw new ProjectionException(Errors.format(ErrorKeys.NO_CONVERGENCE));
            }
            s = Math.sin(phi);
            t = 1.0 - excentricitySquared * s * s;
            t = (mlfn(phi, s, Math.cos(phi)) - arg) * (t * Math.sqrt(t)) * k;
            phi -= t;
            if (Math.abs(t) < TOL) {
                return phi;
            }
        }
    }
	
	/**
     * Provides the transform equations for the spherical case of the
     * CassiniSoldner projection.
     * 
     */
	private static final class Spherical extends CassiniSoldner {

		protected Spherical(ParameterValueGroup values)
				throws ParameterNotFoundException {
			 super(values);
	         assert isSpherical;			
		}
		
		/**
         * {@inheritDoc}
         */
        protected Point2D transformNormalized(double x, double y, Point2D ptDst)
                throws ProjectionException 
        {
            double x1=Math.asin(Math.cos(y)*Math.sin(x));
            double y1=Math.atan2(Math.tan(y),Math.cos(x))-latitudeOfOrigin;
            if (ptDst != null) {
                ptDst.setLocation(x1,y1);
                return ptDst;
            }
            return new Point2D.Double(x1,y1);
        }        
        
        /**
         * {@inheritDoc}
         */
        protected Point2D inverseTransformNormalized(double x, double y, Point2D ptDst) 
                throws ProjectionException 
        {
        	double dd=y+latitudeOfOrigin;
            double phi=Math.asin(Math.sin(dd)*Math.cos(x));
            double lam=Math.atan2(Math.tan(x),Math.cos(dd));
            if (ptDst != null) {
                ptDst.setLocation(lam,phi);
                return ptDst;
            }
            return new Point2D.Double(lam,phi);
        }
		
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////
    ////////                                                                          ////////
    ////////                                 PROVIDER                                 ////////
    ////////                                                                          ////////
    //////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * The {@link org.geotools.referencing.operation.MathTransformProvider}
     * for a {@link CassiniSoldner} projection.
     *
     */
    public static class Provider extends AbstractProvider {
        /**
         * Returns a descriptor group for the specified parameters.
         */
    	
        static ParameterDescriptorGroup createDescriptorGroup(final ReferenceIdentifier[] identifiers) {
            return createDescriptorGroup(identifiers, new ParameterDescriptor[] {
                SEMI_MAJOR,       SEMI_MINOR,
                CENTRAL_MERIDIAN, LATITUDE_OF_ORIGIN,
                SCALE_FACTOR,     FALSE_EASTING,
                FALSE_NORTHING
            });
        }

        /**
         * The parameters group.
         */
        static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
                new NamedIdentifier(Citations.OGC,      "Cassini_Soldner"),
                new NamedIdentifier(Citations.EPSG,     "Cassini-Soldner"),
                new NamedIdentifier(Citations.EPSG,     "9806"),
                new NamedIdentifier(Citations.GEOTIFF,  "CT_CassiniSoldner"),
                new NamedIdentifier(Citations.ESRI,     "Cassini_Soldner"),                
                new NamedIdentifier(Citations.GEOTOOLS, Vocabulary.formatInternational(
                                    VocabularyKeys.CASSINI_SOLDNER_PROJECTION))
            });

        /**
         * Constructs a new provider.
         */
        public Provider() {
            super(PARAMETERS);
        }

        /**
         * Constructs a new provider with the specified parameters.
         */
        Provider(final ParameterDescriptorGroup descriptor) {
            super(descriptor);
        }

        

        /**
         * Creates a transform from the specified group of parameter values.
         *
         * @param  parameters The group of parameter values.
         * @return The created math transform.
         * @throws ParameterNotFoundException if a required parameter was not found.
         */
        public MathTransform createMathTransform(final ParameterValueGroup parameters)
                throws ParameterNotFoundException
        {
            if (isSpherical(parameters)) {
                return new Spherical(parameters);
            } else {
                return new CassiniSoldner(parameters);
            }
        }
    }

}
