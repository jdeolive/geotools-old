/*
 *    GeoTools - OpenSource mapping toolkit
 *    http://geotools.org
 *    (C) 2002-2006, GeoTools Project Managment Committee (PMC)
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.vecmath.MismatchedSizeException;

import org.geotools.geometry.GeneralEnvelope;
import org.geotools.parameter.ParameterGroup;
import org.geotools.referencing.operation.builder.algorithm.TPSInterpolation;
import org.geotools.referencing.operation.transform.NADCONTransform;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.geometry.MismatchedReferenceSystemException;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchIdentifierException;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;


public class NADCONBuilder extends WarpGridBuilder {
	
private GeneralEnvelope env;
    
	public NADCONBuilder(List<MappedPosition> vectors, double xStep, double yStep, GeneralEnvelope env)
        throws MismatchedSizeException, MismatchedDimensionException,
            MismatchedReferenceSystemException, TransformException, NoSuchIdentifierException {
		
	
        super(vectors,	GridParameters.createGridParameters(env, xStep, yStep, null, false));
        this.env = env;
     /*   this.xStep = xStep;
        this.yStep = yStep;
        this.env = env;
        this.NUM_X = (new Double(Math.ceil(env.getLength(0)/ xStep))).intValue();
        this.NUM_Y = (new Double(Math.ceil(env.getLength(1)/ yStep))).intValue();
        xStart = env.getMinimum(0);
        yStart = env.getMinimum(1);*/
       
      //  ProjectiveTransform.createTranslation(0, 0) ;
    }

    private Map<DirectPosition, Float> convertMappedPos(List<MappedPosition> vectors, int dim){
    	
    	Map<DirectPosition, Float>  points = new HashMap<DirectPosition, Float>(); 
    	for (Iterator<MappedPosition> i = vectors.iterator(); i.hasNext();){
    		MappedPosition mp = i.next();
    		points.put(mp.getSource(), (new Float(mp.getTarget().getOrdinate(dim) - mp.getSource().getOrdinate(dim))));
    	}
    	return points;
    }
    @Override
    protected float[] computeWarpGrid(GridParameters gridParams)
        throws TransformException, FactoryException {
      
    	TPSInterpolation dxInterpolation = new TPSInterpolation(buildPositionsMap(0));
    	TPSInterpolation dyInterpolation = new TPSInterpolation(buildPositionsMap(1));
    	
        return interpolateWarpGrid(gridParams, dxInterpolation, dyInterpolation); 
    }

	@Override
	protected MathTransform computeMathTransform() throws FactoryException {
		// TODO Auto-generated method stub
		
		/*NADCONTransform.Provider pro =  new NADCONTransform.Provider();
		pro.getParameters();
		
		ParameterValueGroup parameters = new ParameterGroup(pro.getParameters());
		
		parameters.parameter("LAT_DIFF_FILE").setValue("");
		parameters.parameter("LONG_DIFF_FILE").setValue("");*/
	/*	builder.writeDeltaFile(0, "/home/jezekjan/code/NADCON/zk.laa");
		builder.writeDeltaFile(1, "/home/jezekjan/code/NADCON/zk.loa");*/
		try {
			String tmpdir = System.getProperty("java.io.tmpdir");
			NADCONTransform trans = new NADCONTransform(this.writeDeltaFile(1, tmpdir+"/nadcon.laa").getAbsolutePath(),
					                                    this.writeDeltaFile(0, tmpdir+"/nadcon.loa").getAbsolutePath());
			
			
			return trans;
		} catch (ParameterNotFoundException e) {
			e.printStackTrace();
			return null;
			//Shoul never heppend ignore
		} catch (IOException e) {
		       throw new FactoryException(e);
		}
	}     	
}
