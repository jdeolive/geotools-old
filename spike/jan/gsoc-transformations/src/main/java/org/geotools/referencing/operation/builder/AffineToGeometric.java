package org.geotools.referencing.operation.builder;

import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.referencing.FactoryException;

public class AffineToGeometric {

	  /** translation in x */
    private double tx;

    /** translation in y */
    private double ty;

    /** scale in x */
    private double sx;

    /** scale in y */
    private double sy;

    /** rotation in radians */
    private double phi;

    /** skew */
    private double sxy;
    
	private final AffineTransform2D trans;
	
	public AffineToGeometric(AffineTransform2D trans){
		this.trans = trans;
		
	    sy = Math.pow(Math.pow(trans.getScaleY(), 2) + Math.pow(trans.getShearY(), 2), 0.5);

        phi =  (Math.signum(trans.getShearY()))*Math.asin(trans.getShearY() / sy);
        
               
        sx = ((Math.cos(phi) * trans.getScaleX()) -(Math.sin(phi) * trans.getShearX()));
                  


        sxy = ((trans.getScaleX() - (sx * Math.cos(phi)))) / Math.sin(phi);
        tx = trans.getTranslateX();
        ty = trans.getTranslateY();    
	}

	  public double getXScale() throws FactoryException {	    
	        return sx;
	    }
	    public double getYScale() throws FactoryException {	    
	        return sy;
	    }
	    public double getSkew() throws FactoryException {	    
	        return sxy;
	    }
	    public double getXTranslate() throws FactoryException {
	    
	        return tx;
	    }
	    public double getYTranslate() throws FactoryException {    	
	        return ty;
	    }
	    public double getRotation() throws FactoryException {  
	        return phi;
	    }
}
