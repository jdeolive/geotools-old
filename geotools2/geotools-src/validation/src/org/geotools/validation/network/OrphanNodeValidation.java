/*
 * Created on Jan 24, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.geotools.validation.network;

import java.util.Map;

import org.geotools.data.FeatureResults;
import org.geotools.data.FeatureSource;
import org.geotools.feature.Feature;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.graph.Graph;
import org.geotools.graph.GraphComponent;
import org.geotools.graph.build.LineGraphBuilder;
import org.geotools.graph.traverse.BasicGraphTraversal;
import org.geotools.graph.traverse.GraphTraversal;
import org.geotools.graph.traverse.GraphVisitor;
import org.geotools.graph.traverse.SimpleGraphWalker;
import org.geotools.validation.DefaultIntegrityValidation;
import org.geotools.validation.ValidationResults;

import com.vividsolutions.jts.geom.Envelope;

/**
 * OrphanNodeValidation purpose.
 * <p>
 * Builds a network, and looks for orphaned nodes.
 * </p>
 * 
 * @author dzwiers, Refractions Research, Inc.
 * @author $Author: jive $ (last modification)
 * @version $Id: OrphanNodeValidation.java,v 1.1 2004/02/13 03:08:00 jive Exp $
 */
public class OrphanNodeValidation extends DefaultIntegrityValidation {

	/** the FeatureSource name datastoreId:typename*/
	private String typeName;
	
	/**
	 * StarNodeValidation constructor.
	 * <p>
	 * Description
	 * </p>
	 * 
	 */
	public OrphanNodeValidation() {
		super();
	}

	/**
	 * Check FeatureType for ...
	 * <p>
	 * Detailed description...
	 * </p>
	 * 
	 * @param layers Map of FeatureSource by "dataStoreID:typeName"
	 * @param envelope The bounding box that encloses the unvalidated data
	 * @param results Used to coallate results information
	 *
	 * @return <code>true</code> if all the features pass this test.
	 */
	public boolean validate(Map layers, Envelope envelope, final ValidationResults results) throws Exception {
		LineGraphBuilder lgb = new LineGraphBuilder();
		FeatureSource fs = (FeatureSource)layers.get(typeName);
		FeatureResults fr = fs.getFeatures();
		FeatureCollection fc = fr.collection();
		FeatureIterator f = fc.features();
		while(f.hasNext()){
			Feature ft = f.next();
			if(envelope.contains(ft.getBounds()))
				lgb.add(ft);
		}
		// lgb is loaded
		Graph g = lgb.build();
		class OrphanVisitor implements GraphVisitor{
			private int count = 0;
			public int getCount(){return count;}
			public int visit(GraphComponent element){
				if(element.getAdjacentElements().size()==0)
					count++;
				results.error(element.getFeature(),"Orphaned");
				return GraphTraversal.CONTINUE;
			}
		}
		OrphanVisitor ov = new OrphanVisitor();
		SimpleGraphWalker sgv = new SimpleGraphWalker(ov);
		BasicGraphTraversal bgt = new BasicGraphTraversal(g,sgv);
		bgt.walkNodes();
		if(ov.getCount()==0)
			return true;
		else
			return false;
	}

	/**
	 * Access typeName property.
	 * 
	 * @return Returns the typeName.
	 */
	public String getTypeName() {
		return typeName;
	}

	/**
	 * Set typeName to typeName.
	 *
	 * @param typeName The typeName to set.
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

}
