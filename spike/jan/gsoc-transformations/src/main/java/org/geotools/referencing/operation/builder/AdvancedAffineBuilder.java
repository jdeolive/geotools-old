/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2008, GeoTools Project Managment Committee (PMC)
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
package org.geotools.referencing.operation.builder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.vecmath.MismatchedSizeException;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.geotools.referencing.operation.matrix.GeneralMatrix;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.geotools.referencing.operation.transform.ProjectiveTransform;


public class AdvancedAffineBuilder extends MathTransformBuilder {
    public static final String SX = "sx";
    public static final String SY = "sy";
    public static final String SXY = "sxy";
    public static final String PHI = "phi";
    public static final String TX = "tx";
    public static final String TY = "ty";

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
    Map<String, Double> valueConstrain = new HashMap();
    Map<String, String> relationConstrain = new HashMap();
  
    private GeneralMatrix resultMatrix;
    
    private final AffineTransform2D affineTrans;
    

    /**
     * Constructs builder from set of GCPs
     * @param vectors GCPs
     */
    public AdvancedAffineBuilder(final List<MappedPosition> vectors)  throws MismatchedSizeException, MismatchedDimensionException,
         MismatchedReferenceSystemException, FactoryException{
        super.setMappedPositions(vectors);

        /**
         * get approximate values
         */
       
            
            affineTrans = (AffineTransform2D) (new AffineTransformBuilder(vectors).getMathTransform());
            AffineToGeometric a2g = new  AffineToGeometric(affineTrans);
        
            sx = a2g.getXScale();
            sy = a2g.getYScale();
            sxy =a2g.getSkew();
            phi =a2g.getRotation();
            tx  =a2g.getXTranslate();
            ty  =a2g.getYTranslate();
                      
            
    }

    public void setConstrain(String param, double value) {
        valueConstrain.put(param, value);
    }

    public void setEqualConstrain(String param1, String param2) {
        relationConstrain.put(param1, param2);
    }

    protected GeneralMatrix getA() {
        GeneralMatrix A = new GeneralMatrix(2 * this.getMappedPositions().size(), 6);

        double cosphi = Math.cos(phi);
        double sinphi = Math.sin(phi);

        for (int j = 0; j < (A.getNumRow() / 2); j++) {
            double x = getSourcePoints()[j].getCoordinates()[0];
            double y = getSourcePoints()[j].getCoordinates()[1];

            /** derivation of fx by sx */
            double dxsx = (cosphi * x) - (sinphi * y);

            /** derivation of fy by sy */
            double dysy = (sinphi * x) + (cosphi * y);

            /** derivation fx by phi */
            double dxphi = (sxy * cosphi * x) - (sx * sinphi * x) - (sxy * sinphi * y)
                - (sx * cosphi * y);

            /** derivation fy by phi */
            double dyphi = (sy * cosphi * x) - (sy * sinphi * y);

            /** derivation fx by sxy */
            double dxsxy = (sinphi * x) + (cosphi * y);

            A.setRow(2 * j, new double[] { dxsx, 0, dxsxy, dxphi, 1, 0 });
            A.setRow((2 * j) + 1, new double[] { 0, dysy, 0, dyphi, 0, 1 });
        }

        return A;
    }

    protected GeneralMatrix getl() {
        GeneralMatrix l = new GeneralMatrix(2 * this.getMappedPositions().size(), 1);

        double cosphi = Math.cos(phi);
        double sinphi = Math.sin(phi);

        for (int j = 0; j < (l.getNumRow() / 2); j++) {
            double x = getSourcePoints()[j].getCoordinates()[0];
            double y = getSourcePoints()[j].getCoordinates()[1];

            /* a1 is target value - transfomed value*/
            double dx = getTargetPoints()[j].getOrdinate(0)
                - (((sxy * sinphi * x) + (sx * cosphi * x) + (sxy * cosphi * y))
                - (sx * sinphi * y) + tx);
            double dy = getTargetPoints()[j].getOrdinate(1)
                - ((sy * sinphi * getSourcePoints()[j].getOrdinate(0))
                + (sy * cosphi * getSourcePoints()[j].getOrdinate(1)) + ty);
            l.setElement(2 * j, 0, dx);
            l.setElement((2 * j) + 1, 0, dy);
        }

        return l;
    }

    public GeneralMatrix getDxMatrix() throws FactoryException {
        return getDxMatrix(0.00000001, 300);
    }

    private GeneralMatrix getDxMatrix(double tolerance, int maxSteps)
        throws FactoryException {
        // Matriix of new calculated coefficeients
        GeneralMatrix xNew = new GeneralMatrix(6, 1);

        // Matrix of coefficients claculated in previous iteration
        GeneralMatrix xOld = new GeneralMatrix(6, 1);

        // diference between each steps of old iteration
        GeneralMatrix dxMatrix = new GeneralMatrix(6, 1);

        GeneralMatrix oldDxMatrix = new GeneralMatrix(6, 1);
        
        GeneralMatrix zero = new GeneralMatrix(6, 1);
        zero.setZero();

        GeneralMatrix xk = new GeneralMatrix(6 + valueConstrain.size(), 1);

        // i is a number of iterations
        int i = 0;

        // iteration
        do {
             xOld.set(new double[] { sx, sy, sxy, phi, tx, ty });
             oldDxMatrix=dxMatrix.clone();
             
            GeneralMatrix A = getA();
            GeneralMatrix l = getl();

            GeneralMatrix AT = A.clone();
            AT.transpose();

            GeneralMatrix ATA = new GeneralMatrix(6, 6);
            GeneralMatrix ATl = new GeneralMatrix(6, 1);

            ATA.mul(AT, A);
            // ATA.invert();
            ATl.mul(AT, l);

            /**constrains**/
            GeneralMatrix AB = createAB(ATA, getB());

            AB.invert();
            AB.negate();

            GeneralMatrix AU = createAU(ATl, getU());
            xk.mul(AB, AU);

            xk.copySubMatrix(0, 0, 6, xk.getNumCol(), 0, 0, dxMatrix);
            dxMatrix.negate();

            /*
               // dx = A^T * A  * A^T * l
               ATA.mul(AT, A);
               ATA.invert();
               ATl.mul(AT, l);
               dxMatrix.mul(ATA, ATl);
             */

            // New values of x = dx + previous values
            xOld.negate();
            xNew.sub(dxMatrix, xOld);
                     

            // New values are setup for another iteration
            sx = xNew.getElement(0, 0);
            sy = xNew.getElement(1, 0);
            sxy = xNew.getElement(2, 0);
            phi = xNew.getElement(3, 0);
            tx = xNew.getElement(4, 0);
            ty = xNew.getElement(5, 0);

            i++;

           
         //  GeneralMatrix  test = dxMatrix.clone();
          //   test.sub(oldDxMatrix);
           // System.out.println(sxy);
          //  System.out.println(oldDxMatrix.getElement(0, 0));
          //  System.out.println(dxMatrix.getElement(0, 0));
            if (i > 200 ){//&& oldDxMatrix.getElement(0, 0) < dxMatrix.getElement(0, 0)){          	
                throw new FactoryException("Calculation of transformation is divergating");
            }
        } while ((!dxMatrix.equals(zero, tolerance)));
        
        xNew.transpose();
        return xNew;
    }

    @Override
    public int getMinimumPointCount() {
        GeneralMatrix M = new GeneralMatrix(3, 3);

        return 3;
    }

    protected GeneralMatrix getB() {
        GeneralMatrix B = new GeneralMatrix(valueConstrain.size(), 6);
        int i = 0;

        if (valueConstrain.containsKey(this.SX)) {
            B.setRow(i, new double[] { 1, 0, 0, 0, 0, 0 });
            i++;
        }

        if (valueConstrain.containsKey(this.SY)) {
            B.setRow(i, new double[] { 0, 1, 0, 0, 0, 0 });
            i++;
        }

        if (valueConstrain.containsKey(this.SXY)) {
            B.setRow(i, new double[] { 0, 0, 1, 0, 0, 0 });
            i++;
        }

        if (valueConstrain.containsKey(this.PHI)) {
            B.setRow(i, new double[] { 0, 0, 0, 1, 0, 0 });
            i++;
        }

        if (valueConstrain.containsKey(this.TX)) {
            B.setRow(i, new double[] { 0, 0, 0, 0, 1, 0 });
            i++;
        }

        if (valueConstrain.containsKey(this.TY)) {
            B.setRow(i, new double[] { 0, 0, 0, 0, 0, 1 });
            i++;
        }

        GeneralMatrix U = new GeneralMatrix(1, 1);

        //U.setZero();//.setRow(0, new double[]{0});
        return B;
    }

    protected GeneralMatrix getU() {
        GeneralMatrix U = new GeneralMatrix(valueConstrain.size(), 1);
        int i = 0;

        if (valueConstrain.containsKey(SX)) {
            U.setRow(i, new double[] { -sx + valueConstrain.get(SX) });
            i++;
        }

        if (valueConstrain.containsKey(SY)) {
            U.setRow(i, new double[] { -sy + valueConstrain.get(SY) });
            i++;
        }

        if (valueConstrain.containsKey(SXY)) {
            U.setRow(i, new double[] { -sxy + valueConstrain.get(SXY) });
            i++;
        }

        if (valueConstrain.containsKey(PHI)) {
            U.setRow(i, new double[] { -phi + valueConstrain.get(PHI) });
            i++;
        }

        if (valueConstrain.containsKey(TX)) {
            U.setRow(i, new double[] { -tx + valueConstrain.get(TX) });
            i++;
        } else if (valueConstrain.containsKey(TY)) {
            U.setRow(i, new double[] { -ty + valueConstrain.get(TY) });
            i++;
        }

        return U;
    }

    private GeneralMatrix createAU(GeneralMatrix ATl, GeneralMatrix U) {
        GeneralMatrix AU = new GeneralMatrix(ATl.getNumRow() + U.getNumRow(), ATl.getNumCol());

        ATl.copySubMatrix(0, 0, ATl.getNumRow(), ATl.getNumCol(), 0, 0, AU);
        U.copySubMatrix(0, 0, U.getNumRow(), U.getNumCol(), ATl.getNumRow(), 0, AU);

        return AU;

        //AAB.copySubMatrix(arg0, arg1, arg2, arg3, arg4, arg5, arg6)
    }

    private GeneralMatrix createAB(GeneralMatrix ATA, GeneralMatrix B) {
        GeneralMatrix BT = B.clone();
        BT.transpose();

        GeneralMatrix AAB = new GeneralMatrix(ATA.getNumRow() + B.getNumRow(),
                ATA.getNumCol() + BT.getNumCol());

        ATA.copySubMatrix(0, 0, ATA.getNumRow(), ATA.getNumCol(), 0, 0, AAB);
        B.copySubMatrix(0, 0, B.getNumRow(), B.getNumCol(), ATA.getNumRow(), 0, AAB);
        BT.copySubMatrix(0, 0, BT.getNumRow(), BT.getNumCol(), 0, ATA.getNumCol(), AAB);

        GeneralMatrix zero = new GeneralMatrix(B.getNumRow(), B.getNumRow());
        zero.setZero();
        zero.copySubMatrix(0, 0, zero.getNumRow(), zero.getNumCol(), B.getNumCol(), B.getNumCol(),
            AAB);

        return AAB;

        //AAB.copySubMatrix(arg0, arg1, arg2, arg3, arg4, arg5, arg6)
    }

    protected GeneralMatrix getProjectiveMatrix() throws FactoryException {
    	
    	
        GeneralMatrix M = new GeneralMatrix(3, 3);

        // double[] param = generateMMatrix();
        double[] param = getDxMatrix().getElements()[0];

      /*  sx = param[0];
        sy =  param[1];
        sxy =  param[2];
        phi =  param[3];
        tx =  param[4];
        ty =  param[5];*/
        
        
        double a11 = (sxy * Math.sin(phi)) + (sx * Math.cos(phi));
        double a12 = (sxy * Math.cos(phi)) - (sx * Math.sin(phi));
        double a21 = sy * Math.sin(phi);
        double a22 = sy * Math.cos(phi);

        double[] m0 = { a11, a12, param[4] };
        double[] m1 = { a21, a22, param[5] };
        double[] m2 = { 0, 0, 1 };
        M.setRow(0, m0);
        M.setRow(1, m1);
        M.setRow(2, m2);       	
        
        return M;
    }  

    protected MathTransform computeMathTransform() throws FactoryException {
    	if(valueConstrain.size()==0){
    		
    		  return affineTrans;
    		
    	}
        return ProjectiveTransform.create(getProjectiveMatrix());
    }
}
