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

import java.util.ArrayList;
import org.geotools.display.renderer.GraphicBuilder;
import java.util.Collection;

import java.util.List;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.data.FeatureSource;
import org.geotools.data.memory.CollectionSource;
import org.geotools.factory.FactoryRegistryException;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.feature.SchemaException;
import org.geotools.styling.Style;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.display.primitive.Graphic;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.operation.TransformException;

/**
 * TODO : remove this class in 2.6.x, merge "graphicbuilders" with 
 * the MapLayer interface in render module DefaultMapLayer class.
 * 
 * @author Johann Sorel (Geomatys)
 */
public class MapLayerExt extends DefaultMapLayer {

    private final List<GraphicBuilder> builders = new ArrayList<GraphicBuilder>();
    
    
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
    
    /**
     * Returns the living list of all graphic builders linked to this 
     * map layer.
     * 
     * @return living list of graphic builders
     */
    public List<GraphicBuilder> graphicbuilders(){
        return builders;
    }
    
    /**
     * A layer may provide a graphic builder, this enable
     * special representations, like wind arrows for coverages.
     * A layer may have different builder for each kind of Graphic implementation.
     * This enable the possibility to have custom made graphic representation
     * and several builder, for 2D,3D or else...
     * 
     * @param type : the graphic type wanted
     * @return graphicBuilder<? extends type> or null
     */
    public <T extends Graphic> GraphicBuilder<? extends T> getGraphicBuilder( Class<T> type ){
        
        for(GraphicBuilder builder : builders){
            if(type.isAssignableFrom(builder.getGraphicType())){
                return builder;
            }
        }
        
        return null;
    }
    
    
}
