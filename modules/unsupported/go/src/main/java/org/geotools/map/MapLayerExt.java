/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.geotools.map;

import java.util.Collection;

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
import org.opengis.style.PortrayalCatalog;

/**
 *
 * @author sorel
 */
public class MapLayerExt extends DefaultMapLayer {

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
    
    private PortrayalCatalog catalog;
    
    public void setSEStyle(PortrayalCatalog catalog){
        this.catalog = catalog;
    }
    
    public PortrayalCatalog getSEStyle(){
        return catalog;
    }
    
    
}
