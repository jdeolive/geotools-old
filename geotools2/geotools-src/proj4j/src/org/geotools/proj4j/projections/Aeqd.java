/*
 * Aeqd.java
 *
 * Created on February 28, 2002, 3:12 PM
 */

package org.geotools.proj4j.projections;

import org.geotools.proj4j.*;
/**
 *
 * @author  jamesm
 */
public class Aeqd extends Projection implements Constants{

    public static final double TOL = 1.e-14;
    static final int N_POLE=0,S_POLE=1,EQUIT=2,OBLIQ=3;
    
    double sinph0,cosph0,en[],M1,N1,Mp,He,G;
    int mode;
    int type;
    static final int SPHERE=0,ELLIPSE=1,GUAM=2;
    
    /** Creates a new instance of Aeqd */
    public Aeqd() {
    }

    protected LP doInverse(XY xy) throws ProjectionException {
        switch(type){
            case SPHERE: return sInverse(xy);
            case ELLIPSE: return eInverse(xy);
            case GUAM: return egInverse(xy);
        }
        return null;// should never reach here!
    }
    
    protected XY doForward(LP lp) throws ProjectionException {
        switch(type){
            case SPHERE: return sForward(lp);
            case ELLIPSE: return eForward(lp);
            case GUAM: return egForward(lp);
        }
         return null;// should never reach here!
    }

    protected XY egForward(LP lp){ /* Guam elliptical */
        XY xy = new XY();
	double  cosphi, sinphi, t;

	cosphi = Math.cos(lp.phi);
	sinphi = Math.sin(lp.phi);
	t = 1. / Math.sqrt(1. - ellipse.es * sinphi * sinphi);
	xy.x = lp.lam * cosphi * t;
	xy.y = Functions.mlfn(lp.phi, sinphi, cosphi, en) - M1 +
		.5 * lp.lam * lp.lam * cosphi * sinphi * t;
	return (xy);
}
    protected XY eForward(LP lp){ /* elliptical */
         XY xy = new XY();
	double  coslam, cosphi, sinphi, rho, s, H, H2, c, Az, t, ct, st, cA, sA;

	coslam = Math.cos(lp.lam);
	cosphi = Math.cos(lp.phi);
	sinphi = Math.sin(lp.phi);
	switch (mode) {
	case N_POLE:
		coslam = - coslam;
	case S_POLE:
		xy.x = (rho = Math.abs(Mp - Functions.mlfn(lp.phi, sinphi, cosphi, en))) *
			Math.sin(lp.lam);
		xy.y = rho * coslam;
		break;
	case EQUIT:
	case OBLIQ:
		if (Math.abs(lp.lam) < EPS && Math.abs(lp.phi - phi0) < EPS) {
			xy.x = xy.y = 0.;
			break;
		}
		t = Math.atan2(ellipse.one_es * sinphi + ellipse.es * N1 * sinph0 *
			Math.sqrt(1. - ellipse.es * sinphi * sinphi), cosphi);
    
		ct = Math.cos(t); st = Math.sin(t);
		Az = Math.atan2(Math.sin(lp.lam) * ct, cosph0 * st - sinph0 * coslam * ct);
		cA = Math.cos(Az); sA = Math.sin(Az);
		s = Math.asin( Math.abs(sA) < TOL ?
			(cosph0 * st - sinph0 * coslam * ct) / cA :
			Math.sin(lp.lam) * ct / sA );//TODO: sin should be tolerent of small values above 1, look at aasin
		H = He * cA;
		H2 = H * H;
		c = N1 * s * (1. + s * s * (- H2 * (1. - H2)/6. +
			s * ( G * H * (1. - 2. * H2 * H2) / 8. +
			s * ((H2 * (4. - 7. * H2) - 3. * G * G * (1. - 7. * H2)) /
			120. - s * G * H / 48.))));
		xy.x = c * sA;
		xy.y = c * cA;
		break;
	}
	return (xy);
}
protected XY sForward(LP lp) throws ProjectionException { /* spherical */
        XY xy = new XY();
	double  coslam, cosphi, sinphi;

	sinphi = Math.sin(lp.phi);
	cosphi = Math.cos(lp.phi);
	coslam = Math.cos(lp.lam);
	switch (mode) {
	case EQUIT:
		xy.y = cosphi * coslam;

	case OBLIQ:
		if(mode!=EQUIT){xy.y = sinph0 * sinphi + cosph0 * cosphi * coslam;}

		if (Math.abs(Math.abs(xy.y) - 1.) < TOL)
			if (xy.y < 0.)
				throw new ProjectionException("FERROR");
			else
				xy.x = xy.y = 0.;
		else {
			xy.y = Math.cos(xy.y);//TODO: look at aacos
			xy.y /= Math.sin(xy.y);
			xy.x = xy.y * cosphi * Math.sin(lp.lam);
			xy.y *= (mode == EQUIT) ? sinphi :
		   		cosph0 * sinphi - sinph0 * cosphi * coslam;
		}
		break;
	case N_POLE:
		lp.phi = -lp.phi;
		coslam = -coslam;
	case S_POLE:
		if (Math.abs(lp.phi - HALFPI) < EPS) throw new ProjectionException("FERROR");;
		xy.x = (xy.y = (HALFPI + lp.phi)) * Math.sin(lp.lam);
		xy.y *= coslam;
		break;
	}
	return (xy);
}
protected LP egInverse(XY xy)throws ProjectionException{ /* Guam elliptical */
        LP lp = new LP();
	double x2, t=0;
	int i;

	x2 = 0.5 * xy.x * xy.x;
	lp.phi = phi0;
	for (i = 0; i < 3; ++i) {
		t = ellipse.e * Math.sin(lp.phi);
		lp.phi = Functions.invMlfn(M1 + xy.y -
			x2 * Math.tan(lp.phi) * (t = Math.sqrt(1. - t * t)), ellipse.es, en);
	}
	lp.lam = xy.x * t / Math.cos(lp.phi);
	return (lp);
}
protected LP eInverse(XY xy)throws ProjectionException{ /* elliptical */
    LP lp = new LP();
	double c, Az, cosAz, A, B, D, E, F, psi, t;
        c = (xy.x*xy.x+ xy.y*xy.y);
	if (c  < EPS) {
		lp.phi = phi0;
		lp.lam = 0.;
		return (lp);
	}
	if (mode == OBLIQ || mode == EQUIT) {
		cosAz = Math.cos(Az = Math.atan2(xy.x, xy.y));
		t = cosph0 * cosAz;
		B = ellipse.es * t / ellipse.one_es;
		A = - B * t;
		B *= 3. * (1. - A) * sinph0;
		D = c / N1;
		E = D * (1. - D * D * (A * (1. + A) / 6. + B * (1. + 3.*A) * D / 24.));
		F = 1. - E * E * (A / 2. + B * E / 6.);
		psi = Math.asin(sinph0 * Math.cos(E) + t * Math.sin(E));//TODO: see aasin
		lp.lam = Math.asin(Math.sin(Az) * Math.sin(E) / Math.cos(psi));//TODO: see aasin
		if ((t = Math.abs(psi)) < EPS)
			lp.phi = 0.;
		else if (Math.abs(t - HALFPI) < 0.)
			lp.phi = HALFPI;
		else
			lp.phi = Math.atan((1. - ellipse.es * F * sinph0 / Math.sin(psi)) * Math.tan(psi) /
				ellipse.one_es);//TODO: see aatan
	} else { /* Polar */
		lp.phi = Functions.invMlfn(mode == N_POLE ? Mp - c : Mp + c,
			ellipse.es, en);
		lp.lam = Math.atan2(xy.x, mode == N_POLE ? -xy.y : xy.y);
	}
	return (lp);
}
protected LP sInverse(XY xy)throws ProjectionException{ /* spherical */
    LP lp = new LP();
	double cosc, c_rh, sinc;

	if ((c_rh = Math.sqrt(xy.x*xy.x+xy.y*xy.y)) > PI) {
		if (c_rh - EPS > PI) throw new ProjectionException("IERROR");
		c_rh = PI;
	} else if (c_rh < EPS) {
		lp.phi = phi0;
		lp.lam = 0.;
		return (lp);
	}
	if (mode == OBLIQ || mode == EQUIT) {
		sinc = Math.sin(c_rh);
		cosc = Math.cos(c_rh);
		if (mode == EQUIT) {
			lp.phi = Math.asin(xy.y * sinc / c_rh);//TODO: see aasin
			xy.x *= sinc;
			xy.y = cosc * c_rh;
		} else {
			lp.phi = Math.sin(cosc * sinph0 + xy.y * sinc * cosph0 /
				c_rh);//TODO: see aasin
			xy.y = (cosc - sinph0 * Math.sin(lp.phi)) * c_rh;
			xy.x *= sinc * cosph0;
		}
		lp.lam = xy.y == 0. ? 0. : Math.atan2(xy.x, xy.y);
	} else if (mode == N_POLE) {
		lp.phi = HALFPI - c_rh;
		lp.lam = Math.atan2(xy.x, -xy.y);
	} else {
		lp.phi = c_rh - HALFPI;
		lp.lam = Math.atan2(xy.x, xy.y);
	}
	return (lp);
}

public void setParams(ParamSet params)throws ProjectionException{
    super.setParams(params);
	phi0 = params.getFloatParam("lat_0");
	if (Math.abs(Math.abs(phi0) - HALFPI) < EPS) {
		mode = phi0 < 0. ? S_POLE : N_POLE;
		sinph0 = phi0 < 0. ? -1. : 1.;
		cosph0 = 0.;
	} else if (Math.abs(phi0) < EPS) {
		mode = EQUIT;
		sinph0 = 0.;
		cosph0 = 1.;
	} else {
		mode = OBLIQ;
		sinph0 = Math.sin(phi0);
		cosph0 = Math.cos(phi0);
	}
	if (ellipse.es==0) {
            type=SPHERE;
	} else {
                en = Functions.enfn(ellipse.es);
		//if (en==0) throw new ProjectionException("E ERROR 0");
		if (params.contains("guam")) {
			M1 = Functions.mlfn(phi0, sinph0, cosph0, en);
			type = GUAM;
		} else {
			switch (mode) {
			case N_POLE:
				Mp = Functions.mlfn(HALFPI, 1., 0., en);
				break;
			case S_POLE:
				Mp = Functions.mlfn(-HALFPI, -1., 0., en);
				break;
			case EQUIT:
			case OBLIQ:
				type = ELLIPSE;
				N1 = 1. / Math.sqrt(1. - ellipse.es * sinph0 * sinph0);
				G = sinph0 * (He = ellipse.e / Math.sqrt(ellipse.one_es));
				He *= cosph0;
				break;
			}
			type=ELLIPSE;
		}
	}
}
}

