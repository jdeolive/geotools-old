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

package org.geotools.swing.tool;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.ResourceBundle;
import javax.measure.unit.Unit;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.factory.GeoTools;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapLayer;
import org.geotools.swing.JMapPane;
import org.geotools.swing.JTextReporter;
import org.geotools.swing.TextReporterListener;
import org.geotools.swing.event.MapMouseEvent;
import org.opengis.feature.Feature;
import org.opengis.feature.Property;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.GeometryDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

/**
 * A cursor tool to retrieve information about features that the user clicks
 * on with the mouse.
 *
 * @author Michael Bedward
 * @since 2.6
 * @source $Id$
 * @version $URL$
 */
public class InfoTool extends CursorTool implements TextReporterListener {

    private static final ResourceBundle stringRes = ResourceBundle.getBundle("org/geotools/swing/widget");

    public static final String TOOL_NAME = stringRes.getString("tool_name_info");
    public static final String TOOL_TIP = stringRes.getString("tool_tip_info");
    public static final String CURSOR_IMAGE = "/org/geotools/swing/icons/mActionIdentify.png";
    public static final Point CURSOR_HOTSPOT = new Point(0, 0);
    public static final String ICON_IMAGE = "/org/geotools/swing/icons/mActionIdentify.png";

    /**
     * Default distance fraction. When the user clicks on the map, this tool
     * searches for point and line features that are within a given distance
     * (in world units) of the mouse location. That threshold distance is set
     * as the maximum map side length multiplied by the distance fraction.
     */
    public static final double DEFAULT_DISTANCE_FRACTION = 0.04d;

    private static boolean staticVarsInitialized;

    private static Cursor cursor;
    private static Icon icon;
    private static FilterFactory2 filterFactory;
    private static GeometryFactory geomFactory;

    private JTextReporter reporter;

    /**
     * Constructor
     *
     * @param pane the map pane that this tool is to work with
     */
    public InfoTool(JMapPane pane) {
        setMapPane(pane);
        if (!staticVarsInitialized) {
            icon = new ImageIcon(getClass().getResource(ICON_IMAGE));

            Toolkit tk = Toolkit.getDefaultToolkit();
            ImageIcon cursorIcon = new ImageIcon(getClass().getResource(CURSOR_IMAGE));

            int iconWidth = cursorIcon.getIconWidth();
            int iconHeight = cursorIcon.getIconHeight();

            Dimension bestCursorSize = tk.getBestCursorSize(cursorIcon.getIconWidth(), cursorIcon.getIconHeight());

            cursor = tk.createCustomCursor(cursorIcon.getImage(), CURSOR_HOTSPOT, TOOL_TIP);
            filterFactory = CommonFactoryFinder.getFilterFactory2(GeoTools.getDefaultHints());
            geomFactory = new GeometryFactory();

            staticVarsInitialized = true;
        }
    }

    @Override
    public void onMouseClicked(MapMouseEvent ev) {
        DirectPosition2D pos = ev.getMapPosition();
        Unit<?> uom = mapPane.getMapContext().getCoordinateReferenceSystem().getCoordinateSystem().getAxis(0).getUnit();

        ReferencedEnvelope mapEnv = mapPane.getEnvelope();
        double len = Math.max(mapEnv.getWidth(), mapEnv.getHeight());
        double thresholdDistance = len * DEFAULT_DISTANCE_FRACTION;
        String uomName = uom.toString();

        Geometry posGeom = geomFactory.createPoint(new Coordinate(pos.x, pos.y));

        for (MapLayer layer : mapPane.getMapContext().getLayers()) {
            if (layer.isSelected()) {
                FeatureIterator<? extends Feature> iter = null;
                Filter filter = null;
                try {
                    GeometryDescriptor geomDesc = layer.getFeatureSource().getSchema().getGeometryDescriptor();
                    String attrName = geomDesc.getLocalName();
                    Class<?> geomClass = geomDesc.getType().getBinding();

                    if (Polygon.class.isAssignableFrom(geomClass) ||
                            MultiPolygon.class.isAssignableFrom(geomClass)) {
                        /*
                         * For polygons we test if they contain mouse location
                         */
                        filter = filterFactory.intersects(
                                filterFactory.property(attrName),
                                filterFactory.literal(posGeom));
                    } else {
                        /*
                         * For point and line features we test if the are near
                         * the mouse location
                         */
                        filter = filterFactory.dwithin(
                                filterFactory.property(attrName),
                                filterFactory.literal(posGeom),
                                thresholdDistance, uomName);
                    }

                    FeatureCollection<? extends FeatureType, ? extends Feature> selectedFeatures =
                            layer.getFeatureSource().getFeatures(filter);

                    iter = selectedFeatures.features();
                    while (iter.hasNext()) {
                        report(iter.next());
                    }

                } catch (IOException ioEx) {
                } finally {
                    if (iter != null) {
                        iter.close();
                    }
                }
            }
        }
    }

    /**
     * Write the feature attribute names and values to a
     * {@code JTextReporter}
     *
     * @param feature the feature to report on
     */
    private void report(Feature feature) {
        createReporter();

        Collection<Property> props = feature.getProperties();
        String valueStr = null;

        for (Property prop : props) {
            String name = prop.getName().getLocalPart();
            Object value = prop.getValue();

            if (value instanceof Geometry) {
                name = "Geometry";
                valueStr = value.getClass().getSimpleName();
            } else {
                valueStr = value.toString();
            }

            reporter.append(name + ": " + valueStr);
            reporter.append("\n");
        }
        reporter.append("\n");
    }

    /**
     * Create and show a {@code JTextReporter} if one is not already active
     * for this tool
     */
    private void createReporter() {
        if (reporter == null) {
            reporter = new JTextReporter("Feature info");
            reporter.addListener(this);

            reporter.setSize(400, 400);
            reporter.setVisible(true);
        }
    }


    @Override
    public String getName() {
        return TOOL_NAME;
    }

    @Override
    public Icon getIcon() {
        return icon;
    }

    @Override
    public Cursor getCursor() {
        return cursor;
    }

    @Override
    public boolean drawDragBox() {
        return false;
    }

    /**
     * Called when a {@code JTextReporter} frame that was being used by this tool
     * is closed by the user
     *
     * @param ev event published by the {@code JTextReporter}
     */
    public void onReporterClosed(WindowEvent ev) {
        reporter = null;
    }

}
