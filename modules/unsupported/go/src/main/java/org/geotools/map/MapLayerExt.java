/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.map;

import java.util.Collection;

import java.util.HashMap;
import java.util.Map;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.memory.CollectionSource;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.styling.Style;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.TransformException;

/**
 *
 * @author Johann Sorel
 */
public class MapLayerExt extends DefaultMapLayer {

    private final Map<Class,GraphicBuilder> builders = new HashMap<Class,GraphicBuilder>();
    
    
    public MapLayerExt(FeatureSource<SimpleFeatureType, SimpleFeature> featureSource, Style style,String title) {
        super(featureSource, style, title);
    }

    public MapLayerExt(CollectionSource source, Style style, String title) {
        super(source,style,title);
    }

    public MapLayerExt(FeatureSource<SimpleFeatureType, SimpleFeature> featureSource, Style style) {
        super(featureSource,style);
    }

    public MapLayerExt(FeatureCollection<SimpleFeatureType, SimpleFeature> collection, Style style,String title) {
        super(collection, style, title);
    }

    public MapLayerExt(Collection collection, Style style, String title) {
        super(collection,style,title);
    }

    public MapLayerExt(FeatureCollection<SimpleFeatureType, SimpleFeature> collection, Style style) {
        super(collection,style);
    }

    public MapLayerExt(Collection collection, Style style) {
        super(collection,style);
    }

    public MapLayerExt(GridCoverage coverage, Style style) throws TransformException, FactoryRegistryException, SchemaException, IllegalAttributeException {
        super(coverage,style);
    }

    public MapLayerExt(AbstractGridCoverage2DReader reader, Style style, String title)
            throws TransformException, FactoryRegistryException, SchemaException, IllegalAttributeException {
        super(reader,style,title);
    }

    public MapLayerExt(AbstractGridCoverage2DReader reader, Style style)
            throws TransformException,
            FactoryRegistryException,
            SchemaException,
            IllegalAttributeException {
        super(reader,style);
    }

    public MapLayerExt(GridCoverage coverage, Style style, String title)
            throws TransformException, FactoryRegistryException, SchemaException, IllegalAttributeException {
        super(coverage,style,title);
    }
    
    
    //---------------------NEW OGC SE Styles------------------------------------------------------------------------
    
    private org.opengis.style.Style catalog;
    
    public void setSEStyle(org.opengis.style.Style catalog){
        this.catalog = catalog;
    }
    
    public org.opengis.style.Style getSEStyle(){
        return catalog;
    }
    
    
    public void addGraphicBuilder(Class c, GraphicBuilder builder){
        builders.put(c, builder);
    }
    
    /**
     * A layer may provide a graphic builder, this enable
     * special representations, like wind arrows for coverages.
     * 
     * @param <T>
     * @param classe
     * @return
     */
    public GraphicBuilder getGraphicBuilder(Class classe){
        
        if(builders.containsKey(classe)){
            return builders.get(classe);
        }
        
        return null;
    }
    
    
}
