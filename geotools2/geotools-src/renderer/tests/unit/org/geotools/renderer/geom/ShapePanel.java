/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Management Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
 * (C) 1999, Pêches et Océans Canada
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *
 * Contacts:
 *     UNITED KINGDOM: James Macgill
 *             mailto:j.macgill@geog.leeds.ac.uk
 *
 *     FRANCE: Surveillance de l'Environnement Assistée par Satellite
 *             Institut de Recherche pour le Développement / US-Espace
 *             mailto:seasnet@teledetection.fr
 *
 *     CANADA: Observatoire du Saint-Laurent
 *             Institut Maurice-Lamontagne
 *             mailto:osl@osl.gc.ca
 */
package org.geotools.renderer.geom;

// Geometry
import java.awt.Shape;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

// Rendering
import java.awt.Paint;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.BasicStroke;

// User interface
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.ButtonGroup;

// Events
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


/**
 * A Swing component for testing geometric {@link Shape}s. This component tests
 * {@link Shape#contains(Rectangle2D)} and {@link Shape#intersects(Rectangle2D)}
 * methods and display marks in different colors according the result. This is
 * usefull for debugging {@link Shape} implementations. The coordinate system
 * origin (0,0) is located in the lower left corner, as in the usual geometric
 * convention.
 *
 * @version $Id: ShapePanel.java,v 1.2 2003/05/13 11:00:48 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class ShapePanel extends JPanel {
    /**
     * Interface des objets capables de produire des formes géométriques.
     * Ces formes sont généralement calculées au hasard à des fins de test.
     */
    public static interface Producer {
        /**
         * Retourne un tableau de formes géométriques.
         */
        public abstract Object[] getShapes();
    }

    /**
     * Nombre de pixels à laisser entre le bord
     * de la forme géométrique et le bord de la
     * fenêtre.
     */
    private static final int MARGIN = 32;

    /**
     * Diamètre des cercles, en pixels. Ces cercles
     * servent à vérifier si les formes géométriques
     * fonctionnent correctement.
     */
    private static final int CIRCLE_SIZE = 6;

    /**
     * Largeur et hauteur par défaut de la fenêtre
     * qui affichera les formes géométriques.
     */
    private static final int DEFAULT_SIZE = 400;

    /**
     * Couleur des lignes ainsi que du
     * contour des formes géométriques.
     */
    private static final Paint DRAW_COLOR = Color.white;

    /**
     * Couleur de remplissage
     * des formes géométriques.
     */
    private static final Paint FILL_COLOR = Color.blue;

    /**
     * Couleurs des points.
     */
    private static final Paint POINT_COLOR = Color.yellow;

    /**
     * Couleur des points désactivés. Les points
     * en dehors de la forme géométrique auront
     * typiquement cette couleur.
     */
    private static final Paint DISABLED_COLOR = Color.gray;

    /**
     * Couleur des points de contrôles de
     * {@link Line2D}, {@link QuadCurve2D}
     * et {@link CubicCurve2D}.
     */
    private static final Paint CONTROL_COLOR = Color.red;

    /**
     * Couleur des boîtes englobant
     * les formes géométriques.
     */
    private static final Paint BOUNDS_COLOR = Color.pink;

    /**
     * Constante indiquant qu'on ne testera pas
     * l'intérieur des formes géométriques tracées.
     */
    private static final int TEST_NONE = 0;

    /**
     * Constante indiquant qu'on testera la méthode
     * {@link Shape#contains(Point2D)} des formes
     * géométriques.
     */
    private static final int TEST_CONTAINS_POINT = 1;

    /**
     * Constante indiquant qu'on testera la méthode
     * {@link Shape#contains(Rectangle2D)} des formes
     * géométriques.
     */
    private static final int TEST_CONTAINS_SHAPE = 2;

    /**
     * Constante indiquant qu'on testera la méthode
     * {@link Shape#intersects(Rectangle2D)} des
     * formes géométriques.
     */
    private static final int TEST_INTERSECTS_SHAPE = 3;

    /**
     * The next frame location. Used by {@link #show} methods.
     */
    private static int nextFramePosition = 0;

    /**
     * Forme à utiliser pour tracer des cercles représentant un point.
     * Cette forme est utilisée pour convertir un forme dessinable des
     * points sans dimensions.
     */
    private transient Ellipse2D.Double circle;

    /**
     * Indique si les points de contrôles des formes {@link Line2D},
     * {@link QuadCurve2D} et {@link CubicCurve2D} doivent être tracés.
     */
    private boolean controlPointVisible;

    /**
     * Indique si les boîtes englobant les formes
     * géométriques ("bounds") doivent être affichées.
     */
    private boolean boundsVisible;

    /**
     * Indique si les formes géométriques doivent être
     * modifiées de façon à entrer entièrement dans la
     * fenêtre.
     */
    private boolean packShapes;

    /**
     * Constante indiquant si on doit tester les méthodes {@link Shape#contains(Point2D)},
     * {@link Shape#contains(Rectangle2D)} ou {@link Shape#intersects(Rectangle2D)} des
     * formes géométriques.
     */
    private int test = TEST_NONE;

    /**
     * Objet ayant la charge de produire
     * des formes géométriques.
     */
    private final Producer producer;

    /**
     * Liste des formes géométriques à afficher. En plus des
     * objets {@link Shape}, cette liste peut aussi contenir
     * des objets {@link Point2D}.
     */
    private Object[] shapes;

    /**
     * Formes géométriques qui n'ont pas été
     * transformées par <code>packShapes()</code>.
     */
    private Object[] unpackShapes;

    /**
     * Construit un afficheur qui tracera la forme géométrique spécifiée.
     * Si la forme géométrique est un {@link RectangularShape} vide, alors
     * une copie de la forme avec une dimension par défaut sera utilisée.
     */
    public ShapePanel(final Shape shape) {
        this(new Shape[] {shape});
    }

    /**
     * Construit un afficheur qui tracera les formes géométriques spécifiées. Si des
     * formes géométriques sont des {@link RectangularShape} vides, alors des copies
     * des formes avec une dimension par défaut seront utilisées. Si des objets sont
     * des points {@link Point2D}, alors ces points seront remplacés par des cercles
     * au moment du traçage.
     */
    public ShapePanel(final Object[] shapes) {
        this((Producer) null);
        setShapes(shapes);
    }

    /**
     * Construit un afficheur qui tracera les formes géométriques
     * spécifiés par l'objet <code>producer</code>. De nouvelles
     * formes géométriques seront produites à chaque fois que
     * l'utilisateur clique sur le bouton "nouveau".
     */
    ShapePanel(final Producer producer) {
        this.producer = producer;
        setBackground(Color.black);
        if (producer != null) {
            setShapes(producer.getShapes());
        }
        addComponentListener(new ComponentAdapter() {
            public void componentResized(final ComponentEvent event) {
                if (packShapes) {
                    packShapes();
                }
            }
        });
    }

    /**
     * Spécifie les formes géométriques à afficher. Si des formes géométriques sont des
     * {@link RectangularShape} vides, alors des copies des formes avec une dimension par
     * défaut seront utilisées. Si des objets sont des points {@link Point2D}, alors ces
     * points seront remplacés par des cercles au moment du traçage.
     */
    private void setShapes(final Object[] shapes) {
        this.shapes = unpackShapes = new Object[shapes.length];
        for (int i=0; i<shapes.length; i++) {
            final Object shape = shapes[i];
            if (shape instanceof RectangularShape) {
                RectangularShape rect=(RectangularShape) shape;
                if (rect.isEmpty()) {
                    rect = (RectangularShape) rect.clone();
                    rect.setFrame(MARGIN, MARGIN, DEFAULT_SIZE, DEFAULT_SIZE);
                }
                this.shapes[i]=rect;
            } else {
                this.shapes[i] = shape;
            }
        }
        if (packShapes) {
            packShapes();
        }
    }

    /**
     * Modifie les positions et dimensions des formes géométriques
     * de façon à ce qu'elles entrent complètement dans la fenêtre.
     */
    private void packShapes() {
        if (shapes == unpackShapes) {
            shapes = new Object[unpackShapes.length];
        }
        /*
         * Obtient les coordonnées d'un rectangle
         * qui engloberait toutes les formes.
         */
        Rectangle2D bounds=null;
        for (int i=0; i<shapes.length; i++) {
            final Object object = unpackShapes[i];
            if (object instanceof Point2D) {
                final Point2D point = (Point2D) object;
                if (bounds!=null) {
                    bounds.add(point);
                } else {
                    bounds = new Rectangle2D.Double(point.getX(), point.getY(), 0, 0);
                }
            } else if (object instanceof Shape) {
                final Rectangle2D r=((Shape) object).getBounds2D();
                if (bounds==null) {
                    bounds=r;
                } else {
                    bounds.add(r);
                }
            }
        }
        /*
         * Crée une transformation affine, puis applique cette
         * transformation affine sur toutes les formes et points.
         */
        final double scale = Math.min(( getWidth()-2*MARGIN)/bounds.getWidth(),
                                      (getHeight()-2*MARGIN)/bounds.getHeight());
        final AffineTransform tr=AffineTransform.getTranslateInstance(MARGIN, MARGIN);
        tr.scale(scale, scale);
        tr.translate(-bounds.getX(), -bounds.getY());
        for (int i=0; i<shapes.length; i++) {
            final Object object = unpackShapes[i];
            if (object instanceof Point2D) {
                shapes[i] = tr.transform((Point2D) object, null);
            } else if (object instanceof Shape) {
                shapes[i] = tr.createTransformedShape((Shape) object);
            }
        }
    }

    /**
     * Indique si au moins une des formes en
     * mémoire intersepte le rectangle spécifiée.
     */
    private boolean intersects(final Rectangle2D bounds) {
        for (int i=0; i<shapes.length; i++) {
            final Object object = shapes[i];
            if (object instanceof Point2D) {
                if (bounds.contains((Point2D) object)) {
                    return true;
                }
                continue;
            }
            if (object instanceof Shape) {
                if (((Shape) object).intersects(bounds)) {
                    return true;
                }
                continue;
            }
        }
        return false;
    }

    /**
     * Construit une barre des menus pour cette fenêtre. La barre des menus
     * permettra par exemple d'activer ou de désactiver l'affichage des points
     * de contrôles.
     */
    private JMenuBar createMenuBar() {
        final JMenuBar menubar=new JMenuBar();
        JMenu menu;
        /*
         * Menu AFFICHAGE
         */
        menu = menubar.add(new JMenu("View"));
        /*
         * Menu AFFICHAGE - Points à l'intérieur
         */
        if (true) {
            final ButtonGroup group = new ButtonGroup();
            final String[] labels = new String[] {
                "Interior points",
                "Interior rectangles",
                "Intersecting rectangles",
                "None"
            };
            final int[] actions = new int[] {
                TEST_CONTAINS_POINT,
                TEST_CONTAINS_SHAPE,
                TEST_INTERSECTS_SHAPE,
                TEST_NONE
            };
            for (int i=0; i<labels.length; i++) {
                final int action=actions[i];
                final JRadioButtonMenuItem item=new JRadioButtonMenuItem(labels[i], actions[i]==test);
                item.addActionListener(new ActionListener() {
                    public void actionPerformed(final ActionEvent event) {
                        test=action;
                        repaint();
                    }
                });
                menu.add(item);
                group.add(item);
            }
        }
        /*
         * Menu AFFICHAGE - Points de contrôles
         */
        if (true) {
            final JCheckBoxMenuItem item = new JCheckBoxMenuItem("Control points", controlPointVisible);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent event) {
                    controlPointVisible=item.isSelected();
                    repaint();
                }
            });
            menu.addSeparator();
            menu.add(item);
        }
        /*
         * Menu AFFICHAGE - Boîtes cadres
         */
        if (true) {
            final JCheckBoxMenuItem item = new JCheckBoxMenuItem("Bounding box", boundsVisible);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent event) {
                    boundsVisible=item.isSelected();
                    repaint();
                }
            });
            menu.add(item);
        }
        /*
         * Menu AFFICHAGE - Pleine fenêtre
         */
        if (true) {
            final JCheckBoxMenuItem item = new JCheckBoxMenuItem("Full window", packShapes);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent event) {
                    packShapes = item.isSelected();
                    if (packShapes) {
                        packShapes();
                    } else {
                        shapes = unpackShapes;
                    }
                    repaint();
                }
            });
            menu.addSeparator();
            menu.add(item);
        }
        /*
         * Menu NOUVEAU
         */
        if (true) {
            final JMenuItem item = new JMenuItem("New");
            menubar.add(item);
            item.setEnabled(producer!=null);
            item.addActionListener(new ActionListener() {
                public void actionPerformed(final ActionEvent event) {
                    setShapes(producer.getShapes());
                    repaint();
                }
            });
        }
        return menubar;
    }

    /**
     * Retourne une forme géométrique représentant le point spécifié.
     * Cette méthode est utilisée pour convertir en forme dessinable
     * un point sans dimension.
     */
    private Shape toShape(final Point2D point) {
        if (circle == null) {
            return circle = new Ellipse2D.Double(point.getX()-CIRCLE_SIZE/2,
                                                 point.getY()-CIRCLE_SIZE/2,
                                                 CIRCLE_SIZE, CIRCLE_SIZE);
        } else {
            circle.x = point.getX()-CIRCLE_SIZE/2;
            circle.y = point.getY()-CIRCLE_SIZE/2;
            return circle;
        }
    }

    /**
     * Procède au traçage des formes géométriques. Des traitements particuliers seront
     * fait pour les formes {@link Line2D}, {@link QuadCurve2D} et {@link CubicCurve2D},
     * pour éventuellement afficher leurs points de contrôles.
     */
    protected void paintComponent(final Graphics g) {
        super.paintComponent(g);
        final Graphics2D gr=(Graphics2D) g;
        final double buffer[]=new double[6];
        final Point2D.Double point=new Point2D.Double();
        /*
         * Place l'origine (0,0) en bas à gauche,
         * selon la convention géométrique.
         */
        gr.translate(0, getHeight());
        gr.scale(1,-1);
        /*
         * Dessine les bordures des formes géométriques,
         * si cette option a été demandée.
         */
        if (boundsVisible) {
            gr.setPaint(BOUNDS_COLOR);
            for (int i=0; i<shapes.length; i++) {
                if (shapes[i] instanceof Shape) {
                    final Shape shape=(Shape) shapes[i];
                    gr.draw(shape.getBounds2D());
                }
            }
        }
        /*
         * Dessine les formes géométriques. Ce code
         * tiendra compte de certains cas particuliers.
         */
        for (int i=0; i<shapes.length; i++) {
            final Object object=shapes[i];
            if (object instanceof Point2D) {
                final Shape shape=toShape((Point2D) object);
                gr.setPaint(POINT_COLOR);
                gr.fill(shape);
                continue;
            }
            /*
             * Après les cas particulier, prend maintenant en
             * compte le cas général de n'importe quelle forme.
             */
            if (object instanceof Shape) {
                final Shape shape=(Shape) object;
                if (!((object instanceof Line2D)       ||
                      (object instanceof QuadCurve2D)  ||
                      (object instanceof CubicCurve2D)))
                {
                    gr.setPaint(FILL_COLOR);
                    gr.fill(shape);
                }
                gr.setPaint(DRAW_COLOR);
                gr.draw(shape);
                /*
                 * Affiche les points de contrôles.
                 */
                if (controlPointVisible) {
                    gr.setPaint(CONTROL_COLOR);
                    for (final PathIterator it=shape.getPathIterator(null); !it.isDone(); it.next()) {
                        int nPts;
                        switch (it.currentSegment(buffer)) {
                            case PathIterator.SEG_MOVETO:  nPts=1; break;
                            case PathIterator.SEG_LINETO:  nPts=1; break;
                            case PathIterator.SEG_QUADTO:  nPts=2; break;
                            case PathIterator.SEG_CUBICTO: nPts=3; break;
                            default: continue;
                        }
                        while (--nPts>=0) {
                            point.x = buffer[(nPts << 1) + 0];
                            point.y = buffer[(nPts << 1) + 1];
                            gr.fill(toShape(point));
                        }
                    }
                }
            }
        }
        /*
         * Effectue maintenant des tests pour vérifier si des points
         * tombent à l'intérieur ou à l'extérieur de la forme géométrique.
         */
        if (test != TEST_NONE) {
            for (point.x=0; point.x<(DEFAULT_SIZE+2*MARGIN); point.x+=(CIRCLE_SIZE+CIRCLE_SIZE/2)) {
                for (point.y=0; point.y<(DEFAULT_SIZE+2*MARGIN); point.y+=(CIRCLE_SIZE+CIRCLE_SIZE/2)) {
                    boolean inside=false;
                    Shape draw=toShape(point);
            check:  for (int i=0; i<shapes.length; i++) {
                        if (shapes[i] instanceof Shape) {
                            final Shape shape=(Shape) shapes[i];
                            switch (test) {
                                case TEST_CONTAINS_POINT: {
                                    if (shape.contains(point)) {
                                        inside=true;
                                        break check;
                                    }
                                    break;
                                }
                                case TEST_CONTAINS_SHAPE: {
                                    final Rectangle2D bounds = draw.getBounds2D();
                                    draw = bounds;
                                    if (shape.contains(bounds)) {
                                        inside = true;
                                        break check;
                                    }
                                    break;
                                }
                                case TEST_INTERSECTS_SHAPE: {
                                    final Rectangle2D bounds = draw.getBounds2D();
                                    draw = bounds;
                                    if (shape.intersects(bounds)) {
                                        inside = true;
                                        break check;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                    gr.setPaint(inside ? POINT_COLOR : DISABLED_COLOR);
                    gr.fill(draw);
                }
            }
        }
    }

    /**
     * Retourne un point dont les coordonnées
     * auront été déterminées au hasard. Les
     * coordonnées de ces points seront comprises
     * dans la dimension typique de la fenêtre
     * de <code>ShapePanel</code>.
     */
    private static Point2D getRandomPoint() {
        return new Point2D.Double(MARGIN+DEFAULT_SIZE*Math.random(),
                                  MARGIN+DEFAULT_SIZE*Math.random());
    }

    /**
     * Retourne un tableau de points. Les points seront soit choisis au hasard, ou soit construit
     * avec les coordonnées spécifiées. Ils seront choisis au hasard si aucune coordonnée n'a été
     * spécifiée, c'est-à-dire si le tableau <code>args</code> a une longueur de <code>0</code>.
     * Sinon (si le tableau <code>args</code> a une longueur non-nulle), alors
     * on exigera qu'il y ait tout juste le nombre d'arguments suffisants pour représenter
     * <code>count</code> points. C'est points seront alors construits en supposant que les
     * arguments apparaissent dans l'ordre (x,y).
     *
     * @param  count Nombre de points demandés.
     * @param  args  Tableau de points dans l'ordre (x,y). Si ce tableau est nul ou a une
     *               longueur nulle, alors les coordonnées des points seront choisits au hasard.
     * @param  i     Index à partir d'où interpréter les arguments dans <code>args</code>.
     * @return       Tableau de points.
     * @throws IllegalArgumentException si <code>args</code> n'a pas la longueur requise.
     */
    public static Point2D[] getPoints(final int count, final String[] args)
            throws IllegalArgumentException
    {
        return getPoints(count, args, 0);
    }

    /**
     * Retourne un tableau de points. Les points seront soit choisis au hasard, ou soit construit
     * avec les coordonnées spécifiées. Ils seront choisis au hasard si aucune coordonnée n'a été
     * spécifiée, c'est-à-dire si le tableau <code>args</code> a une longueur de <code>i</code>.
     * Sinon (si le tableau <code>args</code> a une longueur supérieure à <code>i</code>), alors
     * on exigera qu'il y ait tout juste le nombre d'arguments suffisants pour représenter
     * <code>count</code> points. C'est points seront alors construits en supposant que les
     * arguments apparaissent dans l'ordre (x,y).
     *
     * @param  count Nombre de points demandés.
     * @param  args  Tableau de points dans l'ordre (x,y). Si ce tableau est nul ou a une
     *               longueur égale ou inférieure à <code>i</code>, alors les coordonnées
     *               des points seront choisis au hasard.
     * @param  i     Index à partir d'où interpréter les arguments dans <code>args</code>.
     * @return       Tableau de points.
     * @throws IllegalArgumentException si <code>args</code> n'a pas la longueur requise.
     */
    private static Point2D[] getPoints(final int count, final String[] args, int i)
            throws IllegalArgumentException
    {
        final Point2D[] points=new Point2D[count];
        if (args==null || args.length<=i) {
            for (int j=0; j<points.length; j++) {
                points[j]=ShapePanel.getRandomPoint();
            }
        } else if (args.length == 2*count+i) {
            for (int j=0; j<points.length; j++) {
                points[j] = new Point2D.Double(Double.parseDouble(args[i++]),
                                               Double.parseDouble(args[i++]));
            }
        } else {
            throw new IllegalArgumentException("Required "+count+" points.");
        }
        return points;
    }

    /**
     * Affiche la forme géométrique spécifiée
     * dans une fenêtre. Cette méthode sert à
     * vérifier si une forme est dessinée
     * correctement.
     */
    public static JFrame show(final Shape shape) {
        return show(new ShapePanel(shape));
    }

    /**
     * Affiche les formes géométriques spécifiées
     * dans une fenêtre. Cette méthode sert à
     * vérifier si des formes sont dessinées
     * correctement.
     */
    public static JFrame show(final Object[] shapes) {
        return show(new ShapePanel(shapes));
    }

    /**
     * Affiche les formes géométriques spécifiées
     * dans une fenêtre. Cette méthode sert à
     * vérifier si des formes sont dessinées
     * correctement.
     */
    public static JFrame show(final Producer producer) {
        return show(new ShapePanel(producer));
    }

    /**
     * Affiche les formes géométriques spécifiées
     * dans une fenêtre. Cette méthode sert à
     * vérifier si des formes sont dessinées
     * correctement.
     */
    private static JFrame show(final ShapePanel panel) {
        if (!panel.intersects(new Rectangle(0, 0, DEFAULT_SIZE+2*MARGIN, DEFAULT_SIZE+2*MARGIN))) {
            panel.packShapes = true;
        }
        final JFrame frame=new JFrame("Geometric shapes");
        panel.setPreferredSize(new Dimension(DEFAULT_SIZE+2*MARGIN, DEFAULT_SIZE+2*MARGIN));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(panel.createMenuBar());
        frame.setContentPane(panel);
        frame.pack();
        frame.show();
        if (panel.packShapes) {
            panel.packShapes();
        }
        frame.setLocation(nextFramePosition, nextFramePosition);
        nextFramePosition += 32;
        return frame;
    }

    /**
     * Affiche la forme géométrique spécifiée. Le nom de la classe de la forme doit
     * être spécifiée en argument. Un exemple de nom de classe valide serait
     * <code>java.awt.geom.RoundRectangle2D$Float</code>.
     *
     * @throws ClassNotFoundException si la classe spécifiée n'a pas été trouvée.
     * @throws IllegalAccessException si la classe spécifiée n'est pas accessible.
     * @throws InstantiationException si la classe spécifiée ne peut pas créer d'objet sans argument.
     * @throws ClassCastException     si la classe spécifiée n'implémente pas l'interface {@link Shape}.
     */
    public static void main(final String[] args) throws ClassNotFoundException,
                                                        IllegalAccessException,
                                                        InstantiationException,
                                                        ClassCastException
    {
        show(args.length==0 ? new Ellipse2D.Float() :
             (Shape) Class.forName(args[0]).newInstance());
    }
}
