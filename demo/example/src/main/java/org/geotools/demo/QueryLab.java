// docs start source
/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */

package org.geotools.demo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.feature.FeatureCollection;
import org.geotools.filter.text.cql2.CQL;
import org.geotools.swing.action.SafeAction;
import org.geotools.swing.data.JDataStoreWizard;
import org.geotools.swing.table.FeatureCollectionTableModel;
import org.geotools.swing.wizard.JWizard;
import org.opengis.filter.Filter;

/**
 * The Query Lab is an excuse to try out Filters and Expressions on your own data with a table to
 * show the results.
 * <p>
 * Remember when programming that you have other options then the CQL parser, you can directly make
 * a Filter using CommonFactoryFinder.getFilterFactory2(null).
 */
public class QueryLab {

    public static void main(String[] args) throws Exception {

        /*
         * We use a GeoTools wizard to prompt the user for an input ahapefile.
         *
         * To modify this example to work with a PostGIS database instead just
         * replace 'new ShapefileDataStoreFactory()' in the line below with
         * 'new PostgisDataStoreFactory()'
         */
        JDataStoreWizard wizard = new JDataStoreWizard(new ShapefileDataStoreFactory());
        int result = wizard.showModalDialog();
        if (result != JWizard.FINISH) {
            System.exit(0);
        }

        Map<String, Object> connectionParameters = wizard.getConnectionParameters();
        DataStore dataStore = DataStoreFinder.getDataStore(connectionParameters);
        if (dataStore == null) {
            JOptionPane.showMessageDialog(null, "Could not connect - check parameters");
            System.exit(0);
        }

        JFrame frame = new JQueryFrame(dataStore);
        frame.setVisible(true);
    }
// docs end main

// docs start query frame
    @SuppressWarnings("unchecked")
    static class JQueryFrame extends JFrame {
        DataStore datastore;

        JComboBox types;

        JTable table;

        JTextField text;

        public JQueryFrame(DataStore data) {
            this.datastore = data;
            this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            getContentPane().setLayout(new BorderLayout());

            try {
                types = new JComboBox(datastore.getTypeNames());
            } catch (IOException e1) {
                JOptionPane.showMessageDialog(null, "Unable to find any published content");
                System.exit(0);
            }

            text = new JTextField(80);
            text.setText("include"); // include selects everything!
            getContentPane().add(text, BorderLayout.NORTH);

            table = new JTable();
            table.setModel(new DefaultTableModel(5, 5));
            table.setPreferredScrollableViewportSize(new Dimension(500, 200));

            JScrollPane scrollPane = new JScrollPane(table);
            getContentPane().add(scrollPane, BorderLayout.CENTER);

            JMenuBar menubar = new JMenuBar();
            setJMenuBar(menubar);

            menubar.add(types);
            JMenu menu = new JMenu("Data");
            menubar.add(menu);

            menu.add(new SafeAction("Get features") {
                public void action(ActionEvent e) throws Throwable {
                    String typeName = (String) types.getSelectedItem();
                    FeatureSource source = datastore.getFeatureSource(typeName);

                    Filter filter = CQL.toFilter(text.getText());
                    FeatureCollection features = source.getFeatures(filter);
                    FeatureCollectionTableModel model = new FeatureCollectionTableModel(features);
                    table.setModel(model);
                }
            });
            pack();
        }
    }
}
// docs end source
