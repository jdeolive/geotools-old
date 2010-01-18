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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.IOException;
import java.net.URL;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.Timer;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.swing.JMapPane;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * Animation of a sprite over a map.
 *
 * @author Michael Bedward
 */
public class FlyingSaucer extends JMapPane {

    private static final Random rand = new Random();

    private static final Image SPRITE_IMAGE;
    static {
        SPRITE_IMAGE = new ImageIcon(FlyingSaucer.class.getResource("/images/compass_100.png")).getImage();
    }

    private double movementDistance = 3.0;
    private int xdir = 1;
    private int ydir = 1;

    private ReferencedEnvelope spriteEnv;
    private Raster spriteBackground;

    private boolean firstDisplay = true;


    private Timer animationTimer = new Timer(200, new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            drawSprite();
        }
    });


    @Override
    protected void paintComponent(Graphics g) {
        animationTimer.stop();
        super.paintComponent(g);
    }


    @Override
    public void onRenderingCompleted() {
        super.onRenderingCompleted();
        spriteBackground = null;
        animationTimer.start();
    }

    private void drawSprite() {
        if (firstDisplay) {
            setSpritePosition();
            firstDisplay = false;
        }

        Graphics2D gr2D = (Graphics2D) getGraphics();
            eraseSprite(gr2D);
            moveSprite();
            paintSprite(gr2D);
    }

    private void eraseSprite(Graphics2D gr2D) {
        if (spriteBackground != null) {
            Rectangle rect = spriteBackground.getBounds();

            BufferedImage image = new BufferedImage(
                    rect.width, rect.height, BufferedImage.TYPE_INT_ARGB);

            Raster child = spriteBackground.createChild(
                    rect.x, rect.y, rect.width, rect.height, 0, 0, null);

            image.setData(child);

            gr2D.setBackground(getBackground());
            gr2D.clearRect(rect.x, rect.y, rect.width, rect.height);
            gr2D.drawImage(image, rect.x, rect.y, null);
            spriteBackground = null;
        }
    }

    private void moveSprite() {
        ReferencedEnvelope displayArea = getDisplayArea();

        DirectPosition2D lower = new DirectPosition2D();
        DirectPosition2D upper = new DirectPosition2D();

        double xdelta = 0, ydelta = 0;

        boolean done = false;
        while (!done) {
            lower.setLocation(spriteEnv.getLowerCorner());
            upper.setLocation(spriteEnv.getUpperCorner());

            xdelta = xdir * movementDistance;
            ydelta = ydir * movementDistance;

            lower.setLocation(lower.getX() + xdelta, lower.getY() + ydelta);
            upper.setLocation(upper.getX() + xdelta, upper.getY() + ydelta);

            boolean lowerIn = displayArea.contains(lower);
            boolean upperIn = displayArea.contains(upper);

            if (lowerIn && upperIn) {
                done = true;

            } else if (!lowerIn) {
                if (lower.x < displayArea.getMinX()) {
                    xdir = -xdir;
                } else if (lower.y < displayArea.getMinY()) {
                    ydir = -ydir;
                }

            } else if (!upperIn) {
                if (upper.x > displayArea.getMaxX()) {
                    xdir = -xdir;
                } else if (upper.y > displayArea.getMaxY()) {
                    ydir = -ydir;
                }
            }
        }

        spriteEnv.translate(xdelta, ydelta);
    }

    private void paintSprite(Graphics2D gr2D) {
        Rectangle bounds = getSpriteScreenPos();
        spriteBackground = getBaseImage().getData(bounds);
        gr2D.drawImage(SPRITE_IMAGE, bounds.x, bounds.y, null);
    }

    private void setSpritePosition() {
        ReferencedEnvelope worldBounds = null;
        try {
            worldBounds = getMapContext().getLayerBounds();
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }

        CoordinateReferenceSystem crs = worldBounds.getCoordinateReferenceSystem();

        Rectangle screenBounds = getVisibleRect();
        int w = SPRITE_IMAGE.getWidth(null);
        int h = SPRITE_IMAGE.getHeight(null);

        int x = screenBounds.x + rand.nextInt(screenBounds.width - w);
        int y = screenBounds.y + rand.nextInt(screenBounds.height - h);

        Rectangle r = new Rectangle(x, y, w, h);
        AffineTransform tr = getScreenToWorldTransform();
        Rectangle2D rworld = tr.createTransformedShape(r).getBounds2D();

        spriteEnv = new ReferencedEnvelope(rworld, crs);
    }

    private Rectangle getSpriteScreenPos() {
        AffineTransform tr = getWorldToScreenTransform();

        Point2D lowerCorner = new Point2D.Double(spriteEnv.getMinX(), spriteEnv.getMinY());
        Point2D upperCorner = new Point2D.Double(spriteEnv.getMaxX(), spriteEnv.getMaxY());

        Point2D p0 = tr.transform(lowerCorner, null);
        Point2D p1 = tr.transform(upperCorner, null);

        Rectangle r = new Rectangle();
        r.setFrameFromDiagonal(p0, p1);
        return r;
    }

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("Animation example");
        FlyingSaucer mapPane = new FlyingSaucer();
        frame.getContentPane().add(mapPane);
        frame.setSize(800, 500);

        URL url = FlyingSaucer.class.getResource("/data/shapefiles/countries.shp");
        FileDataStore store = FileDataStoreFinder.getDataStore(url);
        FeatureSource featureSource = store.getFeatureSource();

        // Create a map context and add our shapefile to it
        MapContext map = new DefaultMapContext();
        Style style = SLD.createPolygonStyle(Color.BLACK, Color.CYAN, 1.0F);
        map.addLayer(featureSource, style);

        mapPane.setMapContext(map);
        mapPane.setRenderer(new StreamingRenderer());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

}
