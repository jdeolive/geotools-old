/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 *
 *    (C) 2006-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This file is hereby placed into the Public Domain. This means anyone is
 *    free to do whatever they wish with this file. Use it well and enjoy!
 */

package org.geotools.demo.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.Timer;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.swing.JMapPane;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.geotools.swing.event.MapMouseAdapter;
import org.geotools.swing.event.MapMouseEvent;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * This is an example of extending JMapPane for specialized display.
 * Here we display a shapefile and draw animated random walks on
 * top of it.
 * <p>
 * When the application starts the user is prompted for a shapefile
 * to display. Then, clicking on the map will create a random
 * walk that starts from the mouse click position.
 * <p>
 * When a walk is display you can resize the pane and the walk will
 * re-display, in animated form, properly scaled to the map.
 *
 * @author Michael Bedward
 *
 * @source $URL$
 */
public class AnimatedMapPane extends JMapPane {

    private Timer timer;
    private int millisDelay = 100;
    private List<DirectPosition2D> route;

    private Color lineColor;
    private java.awt.Stroke lineStroke;
    private boolean drawRoute;
    private RoutePainter routePainter;

    /**
     * Constructor
     */
    public AnimatedMapPane(GTRenderer renderer, MapContext context) {
        super(renderer, context);

        timer = new Timer(millisDelay, null);
        timer.setInitialDelay(0);
        timer.setRepeats(true);

        routePainter = new RoutePainter();
        timer.addActionListener(routePainter);

        drawRoute = false;
    }

    /**
     * Set the route to draw. It is assumed that the CRS of
     * the route points matches that of the map, ie. no
     * checking or reprojection is done as would be the case
     * in a real application.
     */
    public void setRoute(List<DirectPosition2D> route) {
        this.route = new ArrayList<DirectPosition2D>();
        this.route.addAll(route);
    }

    /**
     * Set the line color and width for the animated route
     */
    public void setRouteStyle(Color color, float lineWidth) {
        lineColor = color;
        lineStroke = new BasicStroke(lineWidth);
    }

    /**
     * Turn route drawing on or off. This does not automatically
     * repaint the map.
     */
    public void enableRouteDrawing(boolean b) {
        drawRoute = b;
    }

    /**
     * Calls on JMapPane to paint the shapefile as required and then
     * draw the animated route if route display is enabled.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (drawRoute && route != null) {
            routePainter.setRoute(route);
            timer.start();
        }
    }


    /**
     * This class encapsulates the animated drawing routine
     */
    class RoutePainter implements ActionListener {
        private Graphics2D g2 = null;
        private AffineTransform tr;
        private ListIterator<DirectPosition2D> iter;
        private Point previous;
        private Point current;

        private boolean firstPoint = true;

        /**
         * Initializes the route painter
         *
         * @param route the route to draw
         */
        public void setRoute(List<DirectPosition2D> route) {
            tr = getWorldToScreenTransform();

            g2 = (Graphics2D) AnimatedMapPane.this.getGraphics();
            g2.setColor(lineColor);
            g2.setStroke(lineStroke);

            iter = route.listIterator();
            previous = new Point();
            current = new Point();

            if (iter.hasNext()) {
                tr.transform(iter.next(), previous);
            } else {
                finish();
            }
        }

        /**
         * This method is called by the animated map timer on each
         * time step. It draws the next segment in the route. If
         * there are no more segments to draw it stops the timer.
         */
        public void actionPerformed(ActionEvent e) {
            if (iter.hasNext()) {
                tr.transform(iter.next(), current);
                g2.drawLine(previous.x, previous.y, current.x, current.y);

                previous.setLocation(current);

            } else {
                finish();
            }
        }

        /**
         * Finish drawing. Stops the timer and discards the graphics.
         */
        private void finish() {
            timer.stop();
            g2.dispose();
        }

    }

    /**
     * Main method. Prompts the user for a shapefile and displays it. When
     * the user mouse clicks the map display a random walk is generated
     * and drawn in animated form.
     *
     * @param args ignored
     */
    public static void main(String[] args) throws IOException {
        File file = JFileDataStoreChooser.showOpenFile("shp", null);
        if (file == null) {
            return;
        }

        FileDataStore dataStore = FileDataStoreFinder.getDataStore(file);
        FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = dataStore.getFeatureSource();

        MapContext map = new DefaultMapContext();
        map.addLayer(featureSource, null);
        ReferencedEnvelope bounds = featureSource.getBounds();

        final AnimatedMapPane pane = new AnimatedMapPane(new StreamingRenderer(), map);
        pane.setRouteStyle(Color.RED, 2.0f);
        pane.enableRouteDrawing(true);
        pane.addMouseListener(new MapMouseAdapter() {
            @Override
            public void onMouseClicked(MapMouseEvent ev) {
                pane.setRoute(randomWalk(ev.getMapPosition(), pane.getDisplayArea(), 50));
                pane.repaint();
            }
        });

        JFrame frame = new JFrame("Click to start a randome walk");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(pane);
        frame.setSize(800, 600);
        frame.setVisible(true);
    }

    /**
     * Generate a random walk
     *
     * @param start start location
     * @param bounds bounds of the map display, used to scale the walk steps
     * @param N number of steps in the walk
     *
     * @return a new List of DirectPosition2D walk points
     */
    private static List<DirectPosition2D> randomWalk(DirectPosition2D start, ReferencedEnvelope bounds, int N) {
        final double stepLength = bounds.getWidth() / 100;
        final double maxTurn = Math.PI / 4;
        final CoordinateReferenceSystem crs = bounds.getCoordinateReferenceSystem();
        Random rand = new Random();

        List<DirectPosition2D> walk = new ArrayList<DirectPosition2D>();
        DirectPosition2D pos = new DirectPosition2D(crs, start.x, start.y);
        walk.add(pos);
        double lastx = pos.x;
        double lasty = pos.y;
        double angle = Math.PI * 2 * rand.nextDouble();

        for (int i = 1; i < N; i++) {
            angle += maxTurn * (1.0 - 2.0 * rand.nextDouble());
            double x = stepLength * Math.sin(angle);
            double y = stepLength * Math.cos(angle);
            walk.add(new DirectPosition2D(crs, lastx + x, lasty + y));
            lastx += x;
            lasty += y;
        }

        return walk;
    }
}

