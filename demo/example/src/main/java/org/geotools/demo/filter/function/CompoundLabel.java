/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */
package org.geotools.demo.filter.function;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.styling.TextSymbolizer;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.swing.wizard.JPage;
import org.geotools.swing.wizard.JWizard;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.filter.FilterFactory;

/**
 * @author Michael Bedward
 */
public class CompoundLabel {

    public static void main(String[] args) throws Exception {
        // display a data store file chooser dialog for shapefiles
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        CompoundLabel me = new CompoundLabel();
        me.displayShapefile(file);
    }

    private void displayShapefile(File file) throws Exception {
        FileDataStore store = FileDataStoreFinder.getDataStore(file);
        FeatureSource featureSource = store.getFeatureSource();

        Style style = createLabelStyle(featureSource);
        if (style != null) {
            MapContext map = new DefaultMapContext();
            map.setTitle("Quickstart");
            map.addLayer(featureSource, style);

            JMapFrame.showMap(map);
        }
    }

    private Style createLabelStyle(FeatureSource featureSource) {
        FeatureType schema = featureSource.getSchema();
        List<String> fieldNames = new ArrayList<String>();

        for (PropertyDescriptor desc : schema.getDescriptors()) {
            if (String.class.isAssignableFrom( desc.getType().getBinding() )) {
                fieldNames.add(desc.getName().getLocalPart());
            }
        }

        if (fieldNames.size() < 2) {
            JOptionPane.showMessageDialog(null,
                    "This example needs a feature type with at least two String fields",
                    "Bummer", JOptionPane.WARNING_MESSAGE);

            return null;
        }

        FieldWizard wizard = new FieldWizard(fieldNames);
        if (wizard.showModalDialog() == JWizard.FINISH) {
            return createStyle((SimpleFeatureType) schema, wizard.getSelections());
        }

        return null;
    }

    private Style createStyle(SimpleFeatureType schema, String[] fields) {
        Style style = SLD.createSimpleStyle(schema, Color.CYAN);

        StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);
        FilterFactory ff = CommonFactoryFinder.getFilterFactory2(null);

        TextSymbolizer sym = sf.createTextSymbolizer();
        sym.setLabel(ff.function("Concatenate", ff.property(fields[0]), ff.literal(":"), ff.property(fields[1])));
        SLD.rules(style)[0].symbolizers().add(sym);

        return style;
    }

    class FieldWizard extends JWizard {
        JComboBox field1CB;
        JComboBox field2CB;

        public FieldWizard(final List<String> items) {
            super("Fields for labels");

            JPage page = new JPage() {
                @Override
                public JPanel createPanel() {
                    JPanel panel = super.createPanel();
                    panel.setLayout(new MigLayout());

                    JLabel label1 = new JLabel("First label part");
                    panel.add(label1, "wrap");

                    field1CB = new JComboBox(items.toArray());
                    field1CB.setEditable(false);
                    field1CB.addActionListener(this.getJWizard().getController());

                    panel.add(field1CB, "gapbefore indent, wrap");
                    JLabel label2 = new JLabel("Second label part");
                    panel.add(label2, "wrap");

                    field2CB = new JComboBox(items.toArray());
                    field2CB.setEditable(false);
                    field2CB.addActionListener(this.getJWizard().getController());
                    panel.add(field2CB, "gapbefore indent, wrap");

                    return panel;
                }

                @Override
                public boolean isValid() {
                    String field1 = (String) field1CB.getSelectedItem();
                    String field2 = (String) field2CB.getSelectedItem();

                    if (field1.equals(field2)) {
                        return false;
                    }

                    return true;
                }
            };

            page.setPageIdentifier(JPage.DEFAULT);
            this.registerWizardPanel(page);
        }

        public String[] getSelections() {
            String[] selections = new String[2];
            selections[0] = (String) field1CB.getSelectedItem();
            selections[1] = (String) field2CB.getSelectedItem();
            return selections;
        }
    }
}
