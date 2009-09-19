/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2008, Open Source Geospatial Foundation (OSGeo)
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

package org.geotools.swing.styling;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import net.miginfocom.swing.MigLayout;
import org.geotools.data.AbstractDataStore;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryDescriptor;

/**
 * A dialog to prompt the user for feature style choices. It has a static
 * method to display the dialog and then create a new {@code Style} instance
 * using the {@code SimpleStyleHelper} class.
 * <p>
 * Example of use:
 * <pre><code>
 * ShapefileDataStore shapefile = ...
 * Style style = JSimpleStyleDialog.showDialog(shapefile, null);
 * if (style != null) {
 *    // create a map layer using this style
 * }
 * </code></pre>
 *
 * @author Michael Bedward
 * @since 2.6
 * @source $URL$
 * @version $Id$
 */
public class JSimpleStyleDialog extends JDialog {

    public static final Color DEFAULT_LINE_COLOR = Color.BLACK;
    public static final Color DEFAULT_FILL_COLOR = Color.WHITE;
    public static final float DEFAULT_LINE_WIDTH = 1.0f;
    public static final float DEFAULT_OPACITY = 1.0f;
    public static final float DEFAULT_POINT_SIZE = 3.0f;
    private static final String DEFAULT_POINT_SYMBOL_NAME = "Circle";
    
    private static int COLOR_ICON_SIZE = 16;

    private static StyleFactory sf = CommonFactoryFinder.getStyleFactory(null);

    /**
     * Constants for the geometry type that the style
     * preferences apply to
     */
    public static enum GeomType {
        UNDEFINED, POINT, LINE, POLYGON;
    }
    private GeomType geomType;

    private Color lineColor;
    private Color fillColor;
    private float lineWidth;
    private float opacity;
    private float pointSize;
    private String pointSymbolName;

    private JColorIcon lineColorIcon;
    private JLabel lineColorLabel;
    private JColorIcon fillColorIcon;
    private JLabel fillColorLabel;
    private JLabel geomTypeLabel;

    private static enum ControlCategory {
        LINE, FILL, POINT;
    }
    private Map<Component, ControlCategory> controls;

    private final AbstractDataStore store;
    private String[] typeNames;

    private boolean completed;


    /**
     * Static convenience method: displays a {@code JSimpleStyleDialog} to get
     * user choices and then creates a new {@code Style} instance.
     *
     * @param store data store with the features to be rendered
     * @param parent parent JFrame (may be null)
     *
     * @return a new Style instance or null if the user cancels the dialog
     */
    public static Style showDialog(AbstractDataStore store, JFrame parent) {
        Style style = null;

        JSimpleStyleDialog dialog = new JSimpleStyleDialog(store, parent);
        dialog.setVisible(true);

        if (dialog.completed()) {
            switch (dialog.getGeomType()) {
                case POLYGON:
                    style = SimpleStyleHelper.createPolygonStyle(
                            dialog.getLineColor(),
                            dialog.getFillColor(),
                            dialog.getOpacity());
                    break;

                case LINE:
                    style = SimpleStyleHelper.createLineStyle(
                            dialog.getLineColor(),
                            dialog.getLineWidth());
                    break;

                case POINT:
                    style = SimpleStyleHelper.createPointStyle(
                            dialog.getPointSymbolName(),
                            dialog.getLineColor(),
                            dialog.getFillColor(),
                            dialog.getOpacity(),
                            dialog.getPointSize());
                    break;
            }
        }

        dialog.dispose();
        return style;
    }


    /**
     * Constructor. Creates a new dialog object initialized on attributes
     * read from the given data store.
     *
     * @param store the data store
     * @param parent the parent JFrame (may be null)
     *
     * @throws IllegalStateException if the data store cannot be accessed
     */
    public JSimpleStyleDialog(AbstractDataStore store, JFrame parent) {
        super(parent, "Basic Style Maker", true);
        setResizable(false);

        this.store = store;
        try {
            this.typeNames = store.getTypeNames();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        lineColor = DEFAULT_LINE_COLOR;
        fillColor = DEFAULT_FILL_COLOR;
        lineWidth = DEFAULT_LINE_WIDTH;
        opacity = DEFAULT_OPACITY;
        pointSize = DEFAULT_POINT_SIZE;
        pointSymbolName = DEFAULT_POINT_SYMBOL_NAME;
        geomType = GeomType.UNDEFINED;
        completed = false;

        try {
            initComponents();
            setType(0);
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Query if the dialog was completed (user clicked the Apply button)
     * @return true if completed; false otherwise
     */
    public boolean completed() {
        return completed;
    }

    /**
     * Get the {@linkplain GeomType} constant for the selected
     * feature type. If the user cancelled the dialog this will
     * be {@linkplain GeomType#UNDEFINED}.
     *
     * @return GeomType constant
     */
    public GeomType getGeomType() {
        return geomType;
    }

    /**
     * Get the selected line color
     *
     * @return line color
     */
    public Color getLineColor() {
        return lineColor;
    }

    /**
     * Get the selected fill color
     *
     * @return fill color
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Get the fill opacity
     *
     * @return fill opacity between 0 and 1
     */
    public float getOpacity() {
        return opacity;
    }

    /**
     * Get the selected line width
     *
     * @return line width
     */
    public float getLineWidth() {
        return lineWidth;
    }

    /**
     * Get the selected point size
     *
     * @return point size
     */
    public float getPointSize() {
        return pointSize;
    }

    /**
     * Get the selected point symbol name
     *
     * @return symbol name
     */
    public String getPointSymbolName() {
        return pointSymbolName;
    }

    /**
     * Create and layout the controls
     */
    private void initComponents() {
        MigLayout layout = new MigLayout();
        JPanel panel = new JPanel(layout);
        controls = new HashMap<Component, ControlCategory>();

        JLabel label = null;
        JButton btn = null;

        ComboBoxModel model = new DefaultComboBoxModel(typeNames);

        final JComboBox typeCBox = new JComboBox(model);
        typeCBox.setEditable(false);
        typeCBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    setType(typeCBox.getSelectedIndex());
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        });
        panel.add(typeCBox, "span 2");

        geomTypeLabel = new JLabel();
        panel.add(geomTypeLabel, "wrap");

        /*
         * Line style items
         */
        label = new JLabel("Line");
        panel.add(label, "wrap");

        btn = new JButton("Color");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseLineColor();
            }
        });
        panel.add(btn);
        controls.put(btn, ControlCategory.LINE);

        lineColorIcon = new JColorIcon(COLOR_ICON_SIZE, COLOR_ICON_SIZE, DEFAULT_LINE_COLOR);
        lineColorLabel = new JLabel(lineColorIcon);
        panel.add(lineColorLabel, "gapafter 20px");

        label = new JLabel("Width");
        panel.add(label, "split 2");

        Object[] widths = new Object[5];
        for (int i = 1; i <= widths.length; i++) { widths[i-1] = Integer.valueOf(i); }
        final JComboBox lineWidthCBox = new JComboBox(widths);
        lineWidthCBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                lineWidth = ((Number)lineWidthCBox.getModel().getSelectedItem()).intValue();
            }
        });
        panel.add(lineWidthCBox, "wrap");
        controls.put(lineWidthCBox, ControlCategory.LINE);


        /*
         * Fill style items
         */
        panel.add( new JLabel("Fill"), "wrap" );

        btn = new JButton("Color");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                chooseFillColor();
            }
        });
        panel.add(btn);
        controls.put(btn, ControlCategory.FILL);

        fillColorIcon = new JColorIcon(COLOR_ICON_SIZE, COLOR_ICON_SIZE, DEFAULT_FILL_COLOR);
        fillColorLabel = new JLabel(fillColorIcon);
        panel.add(fillColorLabel, "gapafter 20px");

        label = new JLabel("% opacity");
        panel.add(label, "split 2");

        final JSlider slider = new JSlider(0, 100, 100);
        slider.setPaintLabels(true);
        slider.setMajorTickSpacing(20);
        slider.addChangeListener(new ChangeListener() {

            public void stateChanged(ChangeEvent e) {
                opacity = (float)slider.getValue() / 100;
            }
        });
        panel.add(slider, "wrap");
        controls.put(slider, ControlCategory.FILL);


        /*
         * Point style items
         */
        panel.add( new JLabel("Point"), "wrap" );

        label = new JLabel("Size");
        panel.add(label, "split 2");

        Object[] sizes = new Object[10];
        for (int i = 1; i <= sizes.length; i++) { sizes[i-1] = Integer.valueOf(i); }
        final JComboBox pointSizeCBox = new JComboBox(sizes);
        pointSizeCBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pointSize = ((Number)pointSizeCBox.getModel().getSelectedItem()).intValue();
            }
        });
        panel.add(pointSizeCBox);
        controls.put(pointSizeCBox, ControlCategory.POINT);

        label = new JLabel("Symbol");
        panel.add(label, "skip, split 2");

        final Object[] marks = {"Circle", "Cross", "X", "Triangle", "Star"};
        final JComboBox pointSymbolCBox = new JComboBox(marks);
        pointSymbolCBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pointSymbolName = marks[pointSymbolCBox.getSelectedIndex()].toString();
            }
        });
        panel.add(pointSymbolCBox, "wrap");
        controls.put(pointSymbolCBox, ControlCategory.POINT);

        /*
         * Apply and Cancel buttons
         */
        btn = new JButton("Apply");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                completed = true;
                setVisible(false);
            }
        });
        panel.add(btn, "skip 2, split 2, align right");

        btn = new JButton("Cancel");
        btn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                geomType = GeomType.UNDEFINED;
                setVisible(false);
            }
        });
        panel.add(btn);

        getContentPane().add(panel);
        pack();
    }

    /**
     * Set up the dialog to work with a given feature type
     *
     * @param typeIndex index into the typeNames member field
     */
    private void setType(int typeIndex) throws IOException {
        SimpleFeatureType type = null;
        type = store.getSchema(typeNames[typeIndex]);

        GeometryDescriptor desc = type.getGeometryDescriptor();
        Class<?> clazz = desc.getType().getBinding();

        if (Polygon.class.isAssignableFrom(clazz) ||
                MultiPolygon.class.isAssignableFrom(clazz)) {
            geomType = GeomType.POLYGON;
            geomTypeLabel.setText("Polygon features");

        } else if (LineString.class.isAssignableFrom(clazz) ||
                MultiLineString.class.isAssignableFrom(clazz)) {
            geomType = GeomType.LINE;
            geomTypeLabel.setText("Line features");

        } else if (Point.class.isAssignableFrom(clazz) ||
                MultiPoint.class.isAssignableFrom(clazz)) {
            geomType = GeomType.POINT;
            geomTypeLabel.setText("Point features");

        } else {
            throw new UnsupportedOperationException("No style method for " + clazz.getName());
        }

        for (Component c : controls.keySet()) {
            switch (controls.get(c)) {
                case LINE:
                    // nothing to do at present
                    break;

                case FILL:
                    c.setEnabled(geomType != GeomType.LINE);
                    break;

                case POINT:
                    c.setEnabled(geomType == GeomType.POINT);
                    break;
            }
        }
    }

    /**
     * Display a color chooser dialog to set the line color
     */
    private void chooseLineColor() {
        Color color = JColorChooser.showDialog(this, "Choose line color", lineColor);
        if (color != null) {
            lineColor = color;
            lineColorIcon.setColor(color);
            lineColorLabel.repaint();
        }
    }

    /**
     * Display a color chooser dialog to set the fill color
     */
    private void chooseFillColor() {
        Color color = JColorChooser.showDialog(this, "Choose fill color", fillColor);
        if (color != null) {
            fillColor = color;
            fillColorIcon.setColor(color);
            fillColorLabel.repaint();
        }
    }

}
