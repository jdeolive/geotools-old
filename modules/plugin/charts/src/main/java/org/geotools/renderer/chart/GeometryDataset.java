/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2002-2010, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.renderer.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.xy.AbstractXYDataset;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.Polygon;

/**
 * A dataset for plotting Geometry objects .
 * <pre>
 *   Geometry g1 = ...
 *   Geometry g2 = ...
 *   
 *   GeometryDataset data = new GeometryDataset(g1,g2);
 *   XYPlot plot = data.createPlot();
 *   
 *   ...
 * </pre>
 * @author Justin Deoliveira, OpenGeo
 *
 */
public class GeometryDataset extends AbstractXYDataset {

    List<Geometry> geoms;
    List<Integer> groups;
    Map<Integer,Integer> holes;
    
    public GeometryDataset(Geometry... geometries) {
        if (geometries.length == 0) {
            throw new IllegalArgumentException("No geometries specified");
        }
        
        geoms = new ArrayList();
        groups = new ArrayList();
        holes = new LinkedHashMap();
        
        for (Geometry g : geometries) {
            flatten(g, geoms, holes);
            groups.add(geoms.size());
        }
    }
    
    void flatten(Geometry g, List<Geometry> flat, Map<Integer,Integer> holes) {
        
        LinkedList<Geometry> q = new LinkedList();
        q.add(g);
        
        while(!q.isEmpty()) {
            g = q.removeFirst();
            if (g instanceof GeometryCollection) {
                for (int i = g.getNumGeometries()-1; i > -1; i--) {
                    q.addFirst(g.getGeometryN(i));
                }
            }
            else if (g instanceof Polygon) {
                Polygon p = (Polygon) g;
                if (p.getNumInteriorRing() > 0) {
                    for (int i = p.getNumInteriorRing()-1; i > -1; i--) {
                        q.addFirst(p.getInteriorRingN(i));
                    }
                    holes.put(flat.size(), p.getNumInteriorRing());
                }
                q.addFirst(p.getExteriorRing());
            }
            else {
                flat.add(g);
            }
        }
    }
    
    @Override
    public int getSeriesCount() {
        return geoms.size();
    }

    @Override
    public Comparable getSeriesKey(int index) {
        return index;
    }

    public int getItemCount(int series) {
        return geometry(series).getNumPoints();
    }

    public Number getX(int series, int item) {
        return geometry(series).getCoordinates()[item].x;
    }

    public Number getY(int series, int item) {
        return geometry(series).getCoordinates()[item].y;
    }

    Geometry geometry(int series) {
        return geoms.get(series);
    }

    public NumberAxis[] getAxis() {
        Envelope e = geoms.get(0).getEnvelopeInternal();
        for (int i = 1; i < geoms.size(); i++) {
            e.expandToInclude(geoms.get(i).getEnvelopeInternal());
        }
        
        NumberAxis x = new NumberAxis();
        x.setRange(e.getMinX()-1, e.getMaxX()+1);
        
        NumberAxis y = new NumberAxis();
        y.setRange(e.getMinY()-1, e.getMaxY()+1);
        
        return new NumberAxis[]{x, y};
    }
    
    public XYAreaRenderer createRenderer() {
        XYAreaRenderer renderer = new XYAreaRenderer(XYAreaRenderer.SHAPES_AND_LINES);
        renderer.setBaseStroke(new BasicStroke(2));
        renderer.setBaseShape(new Ellipse2D.Float(0, 0, 1, 1));
        
        Random r = new Random();

        //paint the actual geometries by group
        int j = 0;
        for (int i = 0; i < groups.size(); i++) {
            Color c = new Color(r.nextInt(256), r.nextInt(256), r.nextInt(256), 200);
            while(j < groups.get(i)) {
                
                renderer.setSeriesShape(j, new Ellipse2D.Float(-3, -3, 6, 6));
                renderer.setSeriesPaint(j++, c);
            }
        }
       
        //set holes to be a bit thinner
        for (Map.Entry<Integer, Integer> e : holes.entrySet()) {
            int index = e.getKey();
            int nholes = e.getValue();
            
            for (int i = 0; i < nholes; i++) {
                renderer.setSeriesStroke(index+i+1, new BasicStroke(1));
            }
        }
        renderer.setBaseSeriesVisibleInLegend(false);
        return renderer;
    }
    
    public XYPlot createPlot() {
        NumberAxis[] xy = getAxis();
        return new XYPlot(this, xy[0], xy[1], createRenderer());
    }
    
}
