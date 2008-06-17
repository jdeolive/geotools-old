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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
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
import org.geotools.filter.CategorizeFunction;
import org.geotools.filter.FunctionFactory;
import org.geotools.gce.image.WorldImageReader;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling2.SymbolizerBuilder;
import org.geotools.styling2.raster.CategorizeOperation;
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
 * Raster function test
 * 
 * 
 * @author Johann Sorel
 */
public class RasterFunctionTest {

    private final File gridFile = new File("/home/sorel/GIS_DATA/GIS/2000-0264-2259-OL.tif");
    
    private void testSimpleRaster(){
        final RasterSymbolizer symbol = createRasterSymbolizer();
        final GridCoverage coverage = createGridCoverage(gridFile);
        
        
        System.out.println("bands = " + coverage.getNumSampleDimensions());
        
        
        
        //-------------------grab the image ------------------------------------
        
        RenderableImage ri = coverage.getRenderableImage(0, 1);
        RenderedImage img = ri.createDefaultRendering();
        
        BufferedImage buffer = new BufferedImage(1680, 1050, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) buffer.getGraphics();
        g2.drawRenderedImage(img, new AffineTransform());
        g2.dispose();
        
        //------------------- apply the style ----------------------------------
        
        // createImageOp returns a useful image filter
        BufferedImageOp op = createImageOp(symbol);

        BufferedImage output = new BufferedImage(1680, 1050, BufferedImage.TYPE_INT_ARGB);
        
        output = op.filter(buffer, output);
        
        showImage(output);
        
    }
    
    private BufferedImageOp createImageOp(RasterSymbolizer symbol){
        
        BufferedImageOp op = new CategorizeOperation(symbol.getColorMap().getFunction());
                
        ChannelSelection selection = symbol.getChannelSelection();
        ColorMap colorMap = symbol.getColorMap();
        ContrastEnhancement enchance = symbol.getContrastEnhancement();
        Symbolizer outline = symbol.getImageOutline();
        Expression opacity = symbol.getOpacity();
        OverlapBehavior behavior = symbol.getOverlap();
        ShadedRelief relief = symbol.getShadedRelief();
        
        return op;
    }
    
    
    
    private GridCoverage createGridCoverage(File file){
        GridCoverage cover = null;
        
        try {
            WorldImageReader reader = new WorldImageReader(file);
            cover = (GridCoverage2D) reader.read(null);
        } catch (DataSourceException ex) {
            cover = null;
            ex.printStackTrace();
        }catch (IOException ex){
            cover = null;
            ex.printStackTrace();
        }
        
        return cover;
    }
    
    
    private RasterSymbolizer createRasterSymbolizer(){
        final SymbolizerBuilder symbolBuilder = new SymbolizerBuilder();
        final StyleBuilder styleBuilder = new StyleBuilder();
        final FunctionFactory functionBuilder = new FunctionFactory();
        
        //the interesting part ---------------------------------------------------------------------------------
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
        Expression lookupValue = styleBuilder.literalExpression(CategorizeFunction.RASTER_DATA);
        Literal lessInfinity = (Literal) styleBuilder.literalExpression(Color.RED);
        Literal middle = (Literal) styleBuilder.literalExpression(160);
        Literal plusInfinity = (Literal) styleBuilder.literalExpression(Color.BLUE);
        Expression succeeding = styleBuilder.literalExpression("succeeding");
        parameters.add(lookupValue);
        parameters.add( styleBuilder.literalExpression(Color.RED) );
        parameters.add( styleBuilder.literalExpression(150) );
        parameters.add( styleBuilder.literalExpression(Color.ORANGE) );
        parameters.add( styleBuilder.literalExpression(160) );
        parameters.add( styleBuilder.literalExpression(Color.YELLOW) );
        parameters.add( styleBuilder.literalExpression(170) );
        parameters.add( styleBuilder.literalExpression(Color.WHITE) );
        parameters.add( styleBuilder.literalExpression(180) );
        parameters.add( styleBuilder.literalExpression(Color.GREEN) );
        parameters.add( styleBuilder.literalExpression(190) );
        parameters.add( styleBuilder.literalExpression(Color.CYAN) );
        parameters.add( styleBuilder.literalExpression(200) );
        parameters.add( styleBuilder.literalExpression(Color.BLUE) );
        parameters.add(succeeding);
        Literal fallback = (Literal) styleBuilder.colorExpression(Color.BLUE);
        Function categorizeFunction = functionBuilder.createFunction(catagorizeName, parameters, fallback);
        //the interesting part ----------------------------------------------------------------------------------
        
        
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
    
    
    private void showImage(final Image image){
        
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
        new RasterFunctionTest().testSimpleRaster();
    }
    
    
    
    
}
