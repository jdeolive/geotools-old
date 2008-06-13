/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.caching.firstdraft.demo;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.WindowConstants;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.geotools.caching.firstdraft.impl.InMemoryDataCache;
import org.geotools.data.FeatureSource;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.gui.swing.JMapPane;
import org.geotools.gui.swing.PanAction;
import org.geotools.gui.swing.ResetAction;
import org.geotools.gui.swing.SelectAction;
import org.geotools.gui.swing.ZoomInAction;
import org.geotools.gui.swing.ZoomOutAction;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLDParser;
import org.geotools.styling.StyleFactory;


/**
 * Sample application that may be used to try JMapPane from the command line.
 *
 * @author Ian Turton
 */
public class CacheDemoApp implements ActionListener {
    JFrame frame;
    JMapPane mp;
    JMapPane mp_cached;
    JToolBar jtb;
    JLabel text;
    final JFileChooser jfc = new JFileChooser();

    public CacheDemoApp() {
        frame = new JFrame("My Map Viewer");
        frame.setBounds(20, 20, 450, 400);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        Container content = frame.getContentPane();
        mp = new JMapPane();
        mp_cached = new JMapPane();
        //mp.addZoomChangeListener(this);
        content.setLayout(new BorderLayout());
        jtb = new JToolBar();

        JButton load = new JButton("Load file");
        load.addActionListener(this);
        jtb.add(load);

        Action zoomIn = new ZoomInAction(mp);
        Action zoomOut = new ZoomOutAction(mp);
        Action pan = new PanAction(mp);
        Action select = new SelectAction(mp);
        Action reset = new ResetAction(mp);
        jtb.add(zoomIn);
        jtb.add(zoomOut);
        jtb.add(pan);
        jtb.addSeparator();
        jtb.add(reset);
        jtb.addSeparator();
        jtb.add(select);

        final JButton button = new JButton();
        button.setText("CRS");
        button.setToolTipText("Change map prjection");
        button.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String code = JOptionPane.showInputDialog(button,
                            "Coordinate Reference System:", "EPSG:4326");

                    if (code == null) {
                        return;
                    }

                    try {
                        CoordinateReferenceSystem crs = CRS.decode(code);
                        setCRS(crs);
                    } catch (Exception fe) {
                        fe.printStackTrace();
                        JOptionPane.showMessageDialog(button, fe.getMessage(),
                            fe.getClass().toString(), JOptionPane.ERROR_MESSAGE);

                        return;
                    }
                }
            });
        jtb.add(button);

        content.add(jtb, BorderLayout.NORTH);

        JPanel dualdisplay = new JPanel();
        dualdisplay.setLayout(new GridLayout(2, 1));
        content.add(dualdisplay, BorderLayout.CENTER);

        //JComponent sp = mp.createScrollPane();
        mp.setSize(400, 200);
        dualdisplay.add(mp);
        mp_cached.setSize(400, 200);
        dualdisplay.add(mp_cached);
        mp.addPropertyChangeListener(mp_cached);
        mp.addMouseListener(mp_cached);
        mp.addMouseMotionListener(mp_cached);
        mp_cached.addPropertyChangeListener(mp);
        mp_cached.addMouseListener(mp);
        mp_cached.addMouseMotionListener(mp);

        content.doLayout();
        frame.setVisible(true);
    }

    /**
     * Method used to set the current map projection.
     *
     * @param crs A new CRS for the mappnae.
     */
    public void setCRS(CoordinateReferenceSystem crs) {
        mp.getContext().setAreaOfInterest(mp.getContext().getAreaOfInterest(), crs);
        mp.setReset(true);
        mp.repaint();
        mp_cached.getContext().setAreaOfInterest(mp_cached.getContext().getAreaOfInterest(), crs);
        mp_cached.setReset(true);
        mp_cached.repaint();
    }

    public void load(URL shape, URL sld) throws Exception {
        ShapefileDataStore ds = new ShapefileDataStore(shape);

        //InMemoryDataCache ds_cached = new InMemoryDataCache(ds) ;
        ShapefileDataStore ds_cached = new ShapefileDataStore(shape);

        FeatureSource fs = ds.getFeatureSource();
        FeatureSource fs_cached = ds_cached.getFeatureSource(ds_cached.getTypeNames()[0]);
        com.vividsolutions.jts.geom.Envelope env = fs.getBounds();
        mp.setMapArea(env);
        mp_cached.setMapArea(env);

        StyleFactory factory = CommonFactoryFinder.getStyleFactory(null);

        SLDParser stylereader = new SLDParser(factory, sld);
        org.geotools.styling.Style[] style = stylereader.readXML();

        CoordinateReferenceSystem crs = fs.getSchema().getPrimaryGeometry().getCoordinateSystem();

        if (crs == null) {
            crs = DefaultGeographicCRS.WGS84;
        }

        MapContext context = new DefaultMapContext(crs);
        MapContext context_cached = new DefaultMapContext(crs);
        context.addLayer(fs, style[0]);
        context.getLayerBounds();
        context_cached.addLayer(fs_cached, style[0]);
        context_cached.getLayerBounds();
        //mp.setHighlightLayer(context.getLayer(0));
        mp.setSelectionLayer(context.getLayer(0));
        mp_cached.setSelectionLayer(context.getLayer(0));

        GTRenderer renderer;

        if (true) {
            renderer = new StreamingRenderer();

            HashMap hints = new HashMap();
            hints.put("memoryPreloadingEnabled", Boolean.TRUE);
            renderer.setRendererHints(hints);
        } else {
            renderer = new StreamingRenderer();

            HashMap hints = new HashMap();
            hints.put("memoryPreloadingEnabled", Boolean.FALSE);
            renderer.setRendererHints(hints);
        }

        mp.setRenderer(renderer);
        mp.setContext(context);
        mp_cached.setRenderer(renderer);
        mp_cached.setContext(context_cached);

        //        mp.getRenderer().addLayer(new RenderedMapScale());
        frame.repaint();
        frame.doLayout();
    }

    public static URL aquireURL(String target) {
        if (new File(target).exists()) {
            try {
                return new File(target).toURL();
            } catch (MalformedURLException e) {
            }
        }

        try {
            return new URL(target);
        } catch (MalformedURLException e) {
            return null;
        }
    }

    public void actionPerformed(ActionEvent e) {
        int returnVal = jfc.showOpenDialog(frame);

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String pathname = jfc.getSelectedFile().getAbsolutePath();
            URL shape = aquireURL(pathname);

            if (shape == null) {
                JOptionPane.showMessageDialog(frame, "could not find file \"" + pathname + "\"",
                    "Could not find file", JOptionPane.ERROR_MESSAGE);
                System.err.println("Could not find shapefile: " + pathname);

                return;
            }

            String filepart = pathname.substring(0, pathname.lastIndexOf("."));
            URL sld = aquireURL(filepart + ".sld");

            if (sld == null) {
                JOptionPane.showMessageDialog(frame,
                    "could not find SLD file \"" + filepart + ".sld\"", "Could not find SLD file",
                    JOptionPane.ERROR_MESSAGE);
                System.err.println("Could not find sld file: " + filepart + ".sld");

                return;
            }

            try {
                this.load(shape, sld);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        CacheDemoApp mapV = new CacheDemoApp();

        if ((args.length == 0) || !args[0].toLowerCase().endsWith(".shp")) {
            /*System.out.println("java org.geotools.gui.swing.MapViewer shapefile.shp");
               System.out.println("Notes:");
               System.out.println(" Any provided shapefile.prj file or shapefile.sld will be used");
               System.exit(0);*/
        } else {
            String pathname = args[0];
            URL shape = aquireURL(pathname);

            if (shape == null) {
                System.err.println("Could not find shapefile: " + pathname);
                System.exit(1);
            }

            String filepart = pathname.substring(0, pathname.lastIndexOf("."));
            URL sld = aquireURL(filepart + ".sld");

            if (sld == null) {
                System.err.println("Could not find sld file: " + filepart + ".sld");
                System.exit(1);
            }

            mapV.load(shape, sld);
        }
    }
}
