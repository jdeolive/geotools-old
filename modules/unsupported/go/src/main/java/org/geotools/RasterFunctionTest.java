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
package org.geotools;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.filter.CategorizeFunction;
import org.geotools.filter.FunctionFactory;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling2.SymbolizerBuilder;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.filter.capability.FunctionName;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.Function;
import org.opengis.filter.expression.Literal;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ColorMap;
import org.opengis.style.ContrastEnhancement;
import org.opengis.style.Description;
import org.opengis.style.OverlapBehavior;
import org.opengis.style.RasterSymbolizer;
import org.opengis.style.ShadedRelief;
import org.opengis.style.Symbolizer;

/**
 *
 * @author Johann Sorel
 */
public class RasterFunctionTest {

    private static final File gridFile = new File("/home/sorel/GIS_DATA/f41078a1.tif");
    
    private static void testSimpleRaster(){
        final RasterSymbolizer symbol = createRasterSymbolizer();
        final GridCoverage coverage = createGridCoverage(gridFile);
        
        ChannelSelection selection = symbol.getChannelSelection();
        ColorMap colorMap = symbol.getColorMap();
        ContrastEnhancement enchance = symbol.getContrastEnhancement();
        Symbolizer outline = symbol.getImageOutline();
        Expression opacity = symbol.getOpacity();
        OverlapBehavior behavior = symbol.getOverlap();
        ShadedRelief relief = symbol.getShadedRelief();
        
        System.out.println(coverage);
        
    }
    
    private static GridCoverage createGridCoverage(File file){
        GridCoverage cover = null;        
        
        return cover;
    }
    
    
    private static RasterSymbolizer createRasterSymbolizer(){
        final SymbolizerBuilder symbolBuilder = new SymbolizerBuilder();
        final StyleBuilder styleBuilder = new StyleBuilder();
        final FunctionFactory functionBuilder = new FunctionFactory();
        
        //the interesting part
        
        Collection<FunctionName> names = functionBuilder.getFunctions().getFunctionNames();
        for(FunctionName name : names){
            System.out.println("FUNCTION : " + name.getName());
            List<String> arguments = name.getArgumentNames();
            for(String argument : arguments){
                System.out.println("  --> argument : " + argument);
            }
        }
        
        String catagorizeName = CategorizeFunction.NAME.getName();
        List<Expression> parameters = new ArrayList<Expression>();
        Expression lookupValue = styleBuilder.literalExpression("");
        Expression lessInfinity = styleBuilder.literalExpression(Color.RED);
        Expression succeeding = styleBuilder.literalExpression("succeeding");
        parameters.add(lookupValue);
        parameters.add(lessInfinity);
        parameters.add(succeeding);
        Literal fallback = (Literal) styleBuilder.colorExpression(Color.BLUE);
        Function categorizeFunction = functionBuilder.createFunction(catagorizeName, parameters, fallback);
        
        Expression opacity = styleBuilder.literalExpression(1f);
        ChannelSelection selection = SymbolizerBuilder.DEFAULT_RASTER_CHANNEL_RGB;
        OverlapBehavior overlap = OverlapBehavior.LATEST_ON_TOP;
        ColorMap colorMap = symbolBuilder.createColorMap(categorizeFunction);
        ContrastEnhancement enchance = SymbolizerBuilder.DEFAULT_RASTER_CONTRAST_ENCHANCEMENT;
        ShadedRelief relief = SymbolizerBuilder.DEFAULT_RASTER_SHADED_RELIEF;
        Symbolizer outline = symbolBuilder.createDefaultLineSymbolizer();
        String uom = Symbolizer.UOM_DISPLAY;
        String geom = SymbolizerBuilder.DEFAULT_GEOM;
        String name = "raster symbol name";
        Description desc = SymbolizerBuilder.DEFAULT_DESCRIPTION;
        
        RasterSymbolizer symbol = symbolBuilder.createRasterSymbolizer(opacity, selection, overlap, colorMap, enchance, relief, outline, uom, geom, name, desc);
        
        return symbol;
    }
    
    
    private static void showImage(final Image image){
        
        JFrame frm = new JFrame();
        
        JPanel panel = new JPanel(){

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                
                g.drawImage(image, 0, 0, null);
            }
            
        };
        
        panel.setSize(image.getWidth(null), image.getHeight(null));
        
        JScrollPane jsp = new JScrollPane(panel);
        
        frm.setContentPane(jsp);
        
        frm.setSize(800, 600);
        frm.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frm.setLocationRelativeTo(null);
        frm.setVisible(true);
        
    }
    
    public static void main(String[] args){
        
        
        testSimpleRaster();
        
        
        
        
    }
    
}
