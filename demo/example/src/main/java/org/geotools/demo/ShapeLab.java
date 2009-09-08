package org.geotools.demo;

import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.type.FeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Stroke;
import org.geotools.swing.JMapFrame;

public class ShapeLab {

    static StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
    static FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);

    /**
     * Prompt the user for a file and open up ImageLab.
     * 
     * @param args
     *                filename of image
     */
    public static void main(String[] args) throws Exception {
        File file = getShapeFile(args);

        ShapefileDataStore shapefile = new ShapefileDataStore(file.toURI().toURL());
        String typeName = shapefile.getTypeNames()[0];
        FeatureSource featureSource = shapefile.getFeatureSource();
        FeatureType schema = featureSource.getSchema();
        CoordinateReferenceSystem crs = schema.getGeometryDescriptor()
                .getCoordinateReferenceSystem();

        MapContext map = new DefaultMapContext(crs);
        Style style = createStyle(file, schema);
        map.addLayer(featureSource, style);

        JMapFrame.showMap(map);
    }

    private static Style createStyle(File file, FeatureType schema) {
        File sld = toSLDFile(file);
        if (sld.exists()) {
            return createFromSLD(sld);
        }
        Class geomType = schema.getGeometryDescriptor().getType().getBinding();

        if (geomType.isAssignableFrom(Polygon.class)
                || geomType.isAssignableFrom(MultiPolygon.class)) {
            return createPolygonStyle();
        } else if (geomType.isAssignableFrom(LineString.class)
                || geomType.isAssignableFrom(MultiLineString.class)) {
            return createLineStyle();
        } else {
            return createPointStyle();
        }
    }

    private static Style createFromSLD(File sld) {
        SLDParser stylereader;
        try {
            stylereader = new SLDParser(styleFactory, sld.toURI().toURL());
            Style[] style = stylereader.readXML();
            return style[0];
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            System.exit(0);
        }
        return null;
    }

    /**
     * Create a Style to draw point features as circles with blue outlines
     * and cyan fill
     */
    private static Style createPointStyle() {
        Graphic gr = styleFactory.createDefaultGraphic();

        Mark mark = styleFactory.getCircleMark();

        mark.setStroke(styleFactory.createStroke(
                filterFactory.literal(Color.BLUE), filterFactory.literal(1)));
        
        mark.setFill(styleFactory.createFill(filterFactory.literal(Color.CYAN)));

        mark.setSize(filterFactory.literal(3));

        gr.graphicalSymbols().clear();
        gr.graphicalSymbols().add(mark);

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
        PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

    /**
     * Create a Style to draw line features as thin blue lines
     */
    private static Style createLineStyle() {
        Stroke stroke = styleFactory.createStroke(
                filterFactory.literal(Color.BLUE),
                filterFactory.literal(1));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
        LineSymbolizer sym = styleFactory.createLineSymbolizer(stroke, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

    /**
     * Create a Style to draw polygon features with a thin blue outline and
     * a cyan fill
     */
    private static Style createPolygonStyle() {

        // create a partially opaque outline stroke
        Stroke stroke = styleFactory.createStroke(
                filterFactory.literal(Color.BLUE),
                filterFactory.literal(1),
                filterFactory.literal(0.5));

        // create a partial opaque fill
        Fill fill = styleFactory.createFill(
                filterFactory.literal(Color.CYAN),
                filterFactory.literal(0.5));

        /*
         * Setting the geometryPropertyName arg to null signals that we want to
         * draw the default geomettry of features
         */
        PolygonSymbolizer sym = styleFactory.createPolygonSymbolizer(stroke, fill, null);

        Rule rule = styleFactory.createRule();
        rule.symbolizers().add(sym);
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);

        return style;
    }

    private static File getShapeFile(String[] args)
            throws FileNotFoundException {
        File file;
        if (args.length == 0) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Open Shapefile for Reprojection");
            chooser.setFileFilter(new FileFilter() {
                public boolean accept(File f) {
                    return f.isDirectory() || f.getPath().endsWith("shp")
                            || f.getPath().endsWith("SHP");
                }

                public String getDescription() {
                    return "Shapefiles";
                }
            });
            int returnVal = chooser.showOpenDialog(null);

            if (returnVal != JFileChooser.APPROVE_OPTION) {
                System.exit(0);
            }
            file = chooser.getSelectedFile();

            System.out
                    .println("You chose to open this file: " + file.getName());
        } else {
            file = new File(args[0]);
        }
        if (!file.exists()) {
            throw new FileNotFoundException(file.getAbsolutePath());
        }
        return file;
    }

    /** Figure out the URL for the "sld" file */
    public static File toSLDFile(File file)  {
        String filename = file.getAbsolutePath();
        if (filename.endsWith(".shp") || filename.endsWith(".dbf")
                || filename.endsWith(".shx")) {
            filename = filename.substring(0, filename.length() - 4);
            filename += ".sld";
        } else if (filename.endsWith(".SLD") || filename.endsWith(".SLD")
                || filename.endsWith(".SLD")) {
            filename = filename.substring(0, filename.length() - 4);
            filename += ".SLD";
        }
        return new File(filename);
    }
}
