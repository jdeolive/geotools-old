package org.geotools.demo.style;

import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.FeatureTypeConstraint;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.styling.UserLayer;
import org.opengis.filter.Filter;
import org.opengis.style.Graphic;


/**
 * The following style snippets are used to keep the wiki honest.
 * <p>
 * To view the context for these examples: http://docs.codehaus.org/display/GEOTDOC/04+Styling
 * 
 */
public class StyleSnippets {

    public void styleLayerDescriptor() {
        StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
        StyledLayerDescriptor sld = styleFactory.createStyledLayerDescriptor();
        sld.setName("example");
        sld.setAbstract("Example Style Layer Descriptor");
        {
            UserLayer layer = styleFactory.createUserLayer();
            layer.setName("layer");
            {
                FeatureTypeConstraint constraint = styleFactory.createFeatureTypeConstraint(
                        "Feature", Filter.INCLUDE, null);

                layer.layerFeatureConstraints().add(constraint);
            }
            {
                Style style = styleFactory.createStyle();
                
                style.getDescription().setTitle("Style");
                style.getDescription().setAbstract( "Definition of Style" );
                
                // define feature type styles used to actually
                // define how features are rendered
                //
                layer.userStyles().add(style);
            }
            sld.layers().add(layer);
        }        
    }
    
    public void featureTypeStyle(){
        StyleBuilder styleBuilder = new StyleBuilder();
        Style style = styleBuilder.createStyle();        
        {
            {   PointSymbolizer pointSymbolizer = styleBuilder.createPointSymbolizer();
            
                {   Graphic graphic = styleBuilder.createGraphic();
                    ExternalGraphic external = styleBuilder.createExternalGraphic( "file:///C:/images/house.gif", "image/gif");
                    graphic.graphicalSymbols().add( external );
                    graphic.graphicalSymbols().add( styleBuilder.createMark("circle"));
                    
                    pointSymbolizer.setGraphic(graphic);
                }
                Rule rule = styleBuilder.createRule(pointSymbolizer);            
                FeatureTypeStyle featureTypeStyle = styleBuilder.createFeatureTypeStyle("Feature", rule );
                style.featureTypeStyles().add( featureTypeStyle );
            }
        }
    }
    
}
