/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Institut de Recherche pour le Développement
 * (C) 1998, Pêches et Océans Canada
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

// J2SE dependencies
import java.awt.Shape;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.PathIterator;
import java.awt.geom.IllegalPathStateException;

// Geotools dependencies
import org.geotools.cs.Ellipsoid;
import org.geotools.ct.TransformException;
import org.geotools.math.Line;
import org.geotools.units.Unit;
import org.geotools.util.ProgressListener;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.Geometry;
import org.geotools.resources.XArray;


/**
 * Classes assembling pieces of polygons ({@link Polyline})  in order to create closed polygons
 * ({@link Polygon}). This class analyses all available {@link Polyline} and merge together the
 * polylines that look like parts of the same polygons. This class can also complete the polygons
 * that were cut by the map border.
 *
 * This class is usefull in the context of isolines digitalized from many consecutive maps
 * (for example the GEBCO digital atlas).  It is not possible to fill polygons with Java2D
 * if the polygons are broke in many pieces.  Running this class <strong>once</strong> for
 * a given set of isolines before renderering help to repair them. The algorithm is:
 *
 * <ol>
 *   <li>A list of all possible pairs of polylines is built.</li>
 *   <li>For any pair of polylines, the shortest distance between their extremities is
 *       computed. All combinaisons between the begining and the end of a polyline with
 *       the begining or end of the other polyline are taken in account.</li>
 *   <li>The pair with the shorest distance is identified. When the shortest distance
 *       from one polyline's extremity is the other extremity of the same polyline, then
 *       the polyline is identified as a closed polygon (e.g. an island or a lake).
 *       Otherwise, the closest polylines are merged together.</li>
 *   <li>The loop is reexecuted from step 1 until no more polyline has been merged.</li>
 * </ol>
 *
 * <blockquote>
 *     NOTE: L'implémentation actuelle de cette méthode ne prend pas en compte les
 *           cas où deux polylignes se chevaucheraient. (En fait, un début de prise
 *           en compte est fait et concerne les cas où des polylignes se chevauchent
 *           d'un seul point).
 * </blockquote>
 *
 * @version $Id: PolygonAssembler.java,v 1.2 2003/02/04 23:16:51 desruisseaux Exp $
 * @author Martin Desruisseaux
 *
 * @task TODO: Localize logging and progress messages.
 */
final class PolygonAssembler implements Comparator {
    /**
     * The level for logging messages.
     */
    private static final Level LEVEL = Level.FINEST;

    /**
     * The progress listener, or <code>null</code> if none.
     */
    private final ProgressListener progress;

    /**
     * Forme géométrique de la région dans laquelle ont été découpés les polygones.
     */
    private final Shape clip;

    /**
     * Constante à utiliser dans les appels de {@link Shape#getPathIterator(AffineTransform,double)}
     * afin d'obtenir une successions de segments de droites qui approximerait raisonablement les
     * courbes.
     */
    private final double flatness;

    /**
     * The list of polygons to process. May have a length of 0, but should never be null
     * after {@link #setIsoline} has been invoked.
     */
    private Polygon[] polygons;

    /**
     * Isoligne présentement en cours d'analyse.
     */
    private Isoline isoline;

    /**
     * Ellipsoïde à utiliser pour calculer les distances, ou <code>null</code> si le
     * système de coordonnées est cartésien. Cette information est déduite à partir
     * du système de coordonnées de l'isoligne {@link #isoline}.
     */
    private Ellipsoid ellipsoid;

    /**
     * Distance maximale (en mètres) autorisée entre deux extrémités de polylignes
     * pour permettre leur rattachement. En première approximation, la valeur
     * {@link Double.POSITIVE_INFINITY} donne des résultats acceptables dans plusieurs cas.
     */
    private final double dmax = Double.POSITIVE_INFINITY;

    /**
     * Table des polylignes à fusionner. Les valeurs de cette table seront des objets
     * {@link FermionPair}, tandis que les clés seront des objets {@link Fermion}.
     */
    private final Map fermions = new HashMap();

    /**
     * Instance d'une clé. Cette instance est créée une fois pour toute pour éviter d'avoir
     * à en créer à chaque appel de la méthode {@link #get(Polygon, boolean)}.  Les valeurs
     * de ses champs seront modifiés à chaque appels de cette méthode.
     */
    private final transient Fermion key = new Fermion();

    /**
     * Les variables suivantes servent aux calculs de distances. Elles sont
     * créées une fois pour toutes ici plutôt que d'allouer de la mémoire à
     * chaque exécution d'une boucle.
     */
    private final transient Point2D.Double jFirstPoint = new Point2D.Double();
    private final transient Point2D.Double jLastPoint  = new Point2D.Double();
    private final transient Point2D.Double iFirstPoint = new Point2D.Double();
    private final transient Point2D.Double iLastPoint  = new Point2D.Double();
    private final transient Point2D.Double tmpPoint    = new Point2D.Double();
    private final transient Line2D .Double pathLine    = new Line2D .Double();

    /**
     * Buffer réservé à un usage interne par la méthode {@link #nextSegment}.
     */
    private final transient double[] pitBuffer = new double[8];

    /**
     * Construit un objet qui assemblera les polygones de l'isoligne spécifiée.
     *
     * @param clip     Les limites de la carte exprimées selon le système de coordonnées
     *                 des isolignes qui seront à traiter.
     * @param progress Objet à utiliser pour informer des progrès, ou <code>null</code>
     *                 s'il n'y en a pas.
     *
     * @see #setIsoline
     */
    private PolygonAssembler(final Shape clip, final ProgressListener progress) {
        this.progress  = progress;
        this.flatness  = Polygon.getFlatness(clip);
        this.clip      = clip;
    }




    ///////////////////////////////////////////////////////////////////
    //////////                                               //////////
    //////////          H E L P E R   M E T H O D S          //////////
    //////////                                               //////////
    ///////////////////////////////////////////////////////////////////

    /**
     * Set the next isoline to process. This method must be invoked first, before any
     * processing. It is legal to invoke this method with the current isoline (i.e.
     * {@link #isoline}); it will update internal fields according the current state
     * of the isolone.
     *
     * @param isoline A new isoline to process, or {@link #isoline} for updating
     *        <code>PolygonAssembler</code> according the current isoline state.
     */
    private void setIsoline(final Isoline isoline) {
        if (isoline != this.isoline) {
            // TODO: localize
            GeoShape.LOGGER.log(LEVEL, "Assembling isoline "+isoline.value);
            this.isoline = isoline;
        }
        this.ellipsoid = CTSUtilities.getHeadGeoEllipsoid(isoline.getCoordinateSystem());
        final Collection set = isoline.getPolygons();
        polygons = (Polygon[]) XArray.resize(set.toArray(polygons), set.size());
        isoline.removeAll();
    }

    /**
     * Update the isoline with newly completed polygons.
     *
     * @throws TransformException if a transformation was needed and failed.
     */
    private void updateIsoline() throws TransformException {
        isoline.removeAll();
        for (int i=0; i<polygons.length; i++) {
            isoline.add(polygons[i]);
        }
    }

    /**
     * Compare la distance séparant deux objets {@link IntersectionPoint}.
     * Cette méthode est réservée à un usage interne afin de classer des
     * liste de points d'intersections avant leur traitement.
     */
    public int compare(final Object a, final Object b) {
        final double dA = ((IntersectionPoint) a).minDistanceSq;
        final double dB = ((IntersectionPoint) b).minDistanceSq;
        if (dA < dB) return -1;
        if (dA > dB) return +1;
        return 0;
    }

    /**
     * Affecte à la ligne spécifiée les coordonnées du prochain segment de la forme
     * géométrique balayée par <code>it</code>. Ce segment doit être décrit par une
     * instruction <code>SEG_MOVETO</code> ou un <code>SEG_LINETO</code> suivie d'une
     * instruction <code>SEG_LINETO</code> ou <code>SEG_CLOSE</code>. Un seul appel à
     * <code>pit.next()</code> sera fait (de sorte que vous n'avez pas à l'appeller
     * vous-même), à moins qu'il y avait plusieurs instructions <code>SEG_MOVETO</code>
     * consécutifs. Dans ce dernier cas, seul le dernier sera pris en compte.
     * <p>
     * Le tableau <code>pitBuffer</code> sera utilisé lors des appels à la méthode
     * <code>pit.currentSegment(double[])</code>. Selon les spécifications de cette
     * dernière, le tableau doit avoir une longueur d'au moins 6 éléments. Toutefois
     * cette méthode exige un tableau de 8 éléments, car elle utilisera les éléments
     * 6 et 7 pour sauvegarder les coordonnées du dernier <code>SEG_MOVETO</code>
     * rencontré. Cette information est nécessaire pour permettre la fermeture correcte
     * d'un polygone lors de la prochaine instruction <code>SEG_CLOSE</code>.
     * <p>
     * L'exemple suivant affiche sur le périphérique de sortie standard les coordonnées
     * de toutes les droites qui composent la forme géométrique <var>s</var>.
     *
     * <blockquote><pre>
     * &nbsp;void exemple(Shape s) {
     * &nbsp;    PathIterator pit=s.getPathIterator(null, 1);
     * &nbsp;    while (!pit.isDone()) {
     * &nbsp;         boolean closed = nextSegment(pit);
     * &nbsp;         System.out.println({@link #pathLine});
     * &nbsp;         if (closed) break;
     * &nbsp;    }
     * &nbsp;}
     * </pre></blockquote>
     *
     * @param pit Objet balayant le contour d'une forme géométrique.
     *
     * @return <code>false</code> si la ligne s'est terminée par une instruction
     *         <code>SEG_LINETO</code>, <code>true</code> si elle s'est terminée
     *         par une instruction <code>SEG_CLOSE</code>. Cette information peut
     *         être vu comme une estimation de ce que devrait donner le prochain
     *         appel à la méthode <code>it.isDone()</code> après un appel à <code>it.next()</code>.
     *
     * @throws IllegalPathStateException si une instruction <code>SEG_QUADTO</code>
     *         ou <code>SEG_CUBICTO</code> a été rencontrée.
     *
     * @see java.awt.geom.PathIterator#currentSegment(double[])
     */
    private boolean nextSegment(final PathIterator pit) {
        loop: while(true) {
            switch (pit.currentSegment(pitBuffer)) {
                case PathIterator.SEG_MOVETO: {
                    System.arraycopy(pitBuffer,0, pitBuffer,6, 2);
                    // fall through
                }
                case PathIterator.SEG_LINETO: {
                    final double x=pitBuffer[0];
                    final double y=pitBuffer[1];
                    pit.next();
                    switch (pit.currentSegment(pitBuffer)) {
                        case PathIterator.SEG_MOVETO: {
                            System.arraycopy(pitBuffer,0, pitBuffer,6, 2);
                            continue loop;
                        }
                        case PathIterator.SEG_LINETO: {
                            pathLine.setLine(x,y,pitBuffer[0],pitBuffer[1]);
                            return false;
                        }
                        case PathIterator.SEG_CLOSE: {
                            pathLine.setLine(x,y,pitBuffer[6],pitBuffer[7]);
                            return true;
                        }
                    }
                }
            }
            throw new IllegalPathStateException();
        }
    }

    /**
     * Returns the squared distance between points <code>P1</code> and <code>P2</code>.
     * If {@link #ellipsoid} is non-null (i.e. if the underlying coordinate system is
     * a geographic one), then the orthordomic distance will be computed.
     */
    private double distanceSq(final Point2D P1, final Point2D P2) {
        if (ellipsoid != null) {
            final double distance = ellipsoid.orthodromicDistance(P1, P2);
            return distance*distance;
        } else {
            return P1.distanceSq(P2);
        }
    }

    /**
     * Indique si la ligne spécifiée représente une singularité, c'est-à-dire si les points
     * (<var>x<sub>1</sub></var>,<var>y<sub>1</sub></var>) et
     * (<var>x<sub>2</sub></var>,<var>y<sub>2</sub></var>) sont identiques.
     */
    private static boolean isSingularity(final Line2D.Double line) {
        return line.x1==line.x2 && line.y1==line.y2;
    }

    /**
     * Recherche un objet <code>FermionPair</code> qui contient au moins un lien vers
     * la polyligne <code>path</code> spécifiée avec la valeur <code>mergeEnd</code>
     * correspondante. L'objet <code>path</code> spécifié peut correspondre indifférement
     * à un champ <code>i.path</code> ou <code>j.path</code>. Si aucun objet répondant
     * aux critères ne fut trouvé, alors cette méthode retournera <code>null</code>.
     *
     * @param  path Polyligne à rechercher.
     * @param  mergeEnd Valeur de <code>mergeEnd</code> pour la polyligne à rechercher.
     * @return La paire de polylignes trouvée, ou <code>null</code> s'il n'y en a pas.
     */
    private FermionPair get(final Polygon path, final boolean mergeEnd) {
        key.path     = path;
        key.mergeEnd = mergeEnd;
        return (FermionPair) fermions.get(key);
    }

    /**
     * Remove a polygon from the {@link #polygons} list.
     */
    private void remove(final Polygon polygon) {
        for (int i=polygons.length; --i>=0;) {
            if (polygons[i] == polygon) {
                polygons = (Polygon[]) XArray.remove(polygons, i, 1);
                // There should be no other polygons, but continue just in case...
            }
        }
    }

    /**
     * Retire une paire de polylignes. Cette méthode est appellée après que les polylignes
     * en questions ont été fusionnées, de sorte qu'on a plus besoin des informations qui
     * y étaient associées.
     *
     * @param pair Paire de polylignes à retirer de la liste.
     */
    private void remove(final FermionPair pair) {
        fermions.remove(pair.i);
        fermions.remove(pair.j);
    }

    /**
     * Ajoute une paire de polylignes. Cette paire de polylignes sera identifiée par
     * ses deux clés {@link FermionPair#i} et {@link FermionPair#j}, de sorte qu'il
     * sera possible de la retrouver à partir de n'importe quelle de ces clés.
     *
     * @param pair Paire de polylignes à ajouter à la liste.
     */
    private void put(final FermionPair pair) {
        fermions.put(pair.i, pair);
        fermions.put(pair.j, pair);
    }

    /**
     * Indique si on considère avoir terminé les comparaisons ou si on pense qu'il faudrait
     * en faire encore. Une réponse <code>true</code> indique qu'on a vraiement terminé les
     * comparaisons. Une réponse <code>false</code> n'implique pas nécessairement que les
     * nouvelles comparaisons seront concluantes.
     *
     * @param path Polyligne pour laquelle on veut vérifier si les comparaisons sont terminées.
     * @return <code>false</code> s'il vaudrait mieux continuer les comparaisons.
     */
    private boolean isDone(final Polygon path) {
        key.path = path;
        FermionPair pair;

        key.mergeEnd = false;
        pair=(FermionPair) fermions.get(key);
        if (pair==null || !pair.allComparisonsDone) {
            return false;
        }
        key.mergeEnd = true;
        pair=(FermionPair) fermions.get(key);
        if (pair==null || !pair.allComparisonsDone) {
            return false;
        }
        return true;
    }

    /**
     * Indique que toutes les comparaisons ont été faites. Cette méthode
     * est appellée après qu'une série de comparaisons ont été faites,
     * pour indiquer qu'il est inutile de les refaire.
     *
     * @param done <code>true</code> si toutes les comparaisons ont été faites.
     */
    private void setAllComparisonsDone(final boolean done) {
        for (final Iterator it=fermions.values().iterator(); it.hasNext();) {
            ((FermionPair) it.next()).allComparisonsDone = done;
        }
    }

    /**
     * Renverse l'ordre des données de la polyligne spécifiée. En plus d'inverser les données
     * elles-mêmes, cette méthode inversera aussi les champs <code>mergeEnd</code> des objets
     * <code>FermionPair</code> qui se référaient à cette polyligne, de sorte que la liste
     * restera à jour.
     *
     * @param path Polyligne à inverser.
     */
    private void reverse(final Polygon path) {
        path.reverse();
        key.path = path;
        key.mergeEnd=true;  final FermionPair op=(FermionPair) fermions.remove(key);
        key.mergeEnd=false; final FermionPair np=(FermionPair) fermions.remove(key);
        if (op != null) {
            if (op.i.path==path) {op.i.mergeEnd=!op.i.mergeEnd; fermions.put(op.i, op);}
            if (op.j.path==path) {op.j.mergeEnd=!op.j.mergeEnd; fermions.put(op.j, op);}
        }
        if (np!=null && np!=op) {
            if (np.i.path==path) {np.i.mergeEnd=!np.i.mergeEnd; fermions.put(np.i, np);}
            if (np.j.path==path) {np.j.mergeEnd=!np.j.mergeEnd; fermions.put(np.j, np);}
        }
    }

    /**
     * Inverse toutes les coordonnées contenu dans le tableau spécifié en
     * argument. Les coordonnées sont supposées regroupées par paires de
     * nombres réels (<var>x</var>,<var>y</var>). Rien ne sera fait si le
     * tableau spécifié est nul.
     */
    private static void reverse(final float array[]) {
        if (array != null) {
            int length = array.length;
            for (int i=0; i<length;) {
                float tmp2 = array[--length];   array[length] = array[i+1];
                float tmp1 = array[--length];   array[length] = array[i+0];
                array[i++] = tmp1;
                array[i++] = tmp2;
            }
        }
    }

    /**
     * Remplace toutes les occurences de la polyligne <code>searchFor</code> par la polyligne
     * <code>replaceBy</code>. Cette méthode est appellée après que ces deux polylignes aient
     * été fusionnées ensemble. Après la fusion, une des deux polylignes n'est plus nécessaire.
     * La rêgle voulant qu'il n'y ait jamais deux polylihnes avec la même valeur de
     * <code>mergeEnd</code> restera respectée si cette méthode n'est appellée qu'après
     * une fusion pour suprimer la polyligne en trop.
     *
     * @param searchFor Polyligne à remplacer.
     * @param replaceBy Polyligne remplaçant <code>searchFor</code>.
     */
    private void replace(final Polygon searchFor, final Polygon replaceBy) {
        key.path = searchFor;
        key.mergeEnd=true;  final FermionPair op=(FermionPair) fermions.remove(key);
        key.mergeEnd=false; final FermionPair np=(FermionPair) fermions.remove(key);
        if (op != null) {
            if (op.i.path==searchFor) {op.i.path=replaceBy; fermions.put(op.i, op);}
            if (op.j.path==searchFor) {op.j.path=replaceBy; fermions.put(op.j, op);}
        }
        if (np!=null && np!=op) {
            if (np.i.path==searchFor) {np.i.path=replaceBy; fermions.put(np.i, np);}
            if (np.j.path==searchFor) {np.j.path=replaceBy; fermions.put(np.j, np);}
        }
    }

    /**
     * Indique que la polyligne <code>polygon</code> pourrait représenter un polygon fermé.
     * Cette méthode vérifiera d'abord s'il existe d'autres objets {@link FermionPair} pour
     * cette polyligne. Si c'est le cas, et si la distance mesurée par ces {@link FermionPair}
     * est inférieure à la valeur de l'argument <code>sqrt(distanceSq)</code> de cette méthode,
     * alors rien ne sera fait.
     *
     * @param polygon Référence vers la polyligne représentant peut-être un polygone fermé.
     * @param distanceSq Carré de la distance entre le premier et dernier point de ce polygone.
     * @return <code>true</code> si une information précédemment calculée a dû
     *         être supprimée. Dans ce cas, toutes la boucle calculant ces
     *         information devra être refaite.
     *
     * @see #candidateToMerging
     */
    private boolean candidateToClosing(final Polygon polygon, final double distanceSq) {
        /*
         * Recherche les objets <code>FermionPair</code> se référant déjà à la polyligne
         * <code>polygon</code>. Au besoin, un nouvel objet sera créé si aucun n'avait été
         * définie. Les variables <code>op</code> et <code>np</code> contiendront des références
         * vers les deux objets <code>FermionPair</code> possibles. Elles ne seront jamais nulles,
         * mais peuvent avoir la même valeur toutes les deux.
         */
        FermionPair op = get(polygon, true);
        FermionPair np = get(polygon, false);
        if (np == null) {
            np = op;
            if (np == null) {
                np = new FermionPair();
            }
        }
        if (op == null) {
            op = np;
        }
        if (distanceSq<np.minDistanceSq && distanceSq<op.minDistanceSq) {
            /*
             * Élimine toutes les références vers <code>op</code> et <code>np</code>. Par
             * la suite, on choisira arbitrairement de laisser tomber <code>op</code> et
             * de réutiliser <code>np</code> pour mémoriser les nouvelles informations.
             */
            remove(op);
            remove(np);
            np.i.mergeEnd = true;
            np.j.mergeEnd = false;
            np.minDistanceSq = distanceSq;
            np.i.path = np.j.path = polygon;
            np.allComparisonsDone = false;
            put(np);
            return true;
        }
        return false;
    }

    /**
     * Déclare que les polylignes <code>jPath</code> et <code>iPath</code> pourraient être
     * fusionnées. Cette information ne sera retenue que si la distance <code>sqrt(distanceSQ)</code>
     * spécifiée en argument est plus courte que ce qui avait été mémorisé précédemment.
     *
     * @param jPath      Pointeur vers une des polylignes à fusionner.
     * @param mergeEndJ  <code>true</code> si <code>distanceSq</code> est
     *                   mesurée par rapport à la fin de cette polyligne.
     * @param iPath      Pointeur vers l'autre polyligne à fusionner.
     * @param mergeEndI  <code>true</code> si <code>distanceSq</code> est
     *                   mesurée par rapport à la fin de cette polyligne.
     * @param distanceSq Carré de la distance entre les deux polylignes.
     * @return <code>true</code> si une information précédemment calculée a dû
     *         être supprimée. Dans ce cas, toutes la boucle calculant ces
     *         information devra être refaite.
     *
     * @see #candidateToClosing
     */
    private boolean candidateToMerging(final Polygon jPath, final boolean mergeEndJ,
                                       final Polygon iPath, final boolean mergeEndI,
                                       final double distanceSq)
    {
        assert (jPath != iPath);
        /*
         * Recherche s'il y avait des objets qui mémorisaient déjà iPath et/ou jPath avec le
         * paramètre <code>mergeEnd</code> approprié. Si aucun de ces objets ne fut trouvé, un
         * objet sera créé et ajouté à la liste.
         */
        FermionPair pi = get(iPath, mergeEndI);
        FermionPair pj = get(jPath, mergeEndJ);
        if (pi == null) {
            pi = pj;
            if (pi == null) {
                pi = new FermionPair();
            }
        }
        if (pj == null) {
            pj = pi;
        }
        /*
         * Vérifie maintenant si la distance spécifiée en argument est inférieure
         * aux distances qui avaient été mémorisées précédemment pour chacun des
         * objets <code>[i/j].path</code> concernés.
         */
        if (distanceSq<pi.minDistanceSq && distanceSq<pj.minDistanceSq) {
            remove(pi);
            remove(pj);
            pj.i.path = iPath;
            pj.j.path = jPath;
            pj.i.mergeEnd = mergeEndI;
            pj.j.mergeEnd = mergeEndJ;
            pj.minDistanceSq = distanceSq;
            pj.allComparisonsDone = false;
            put(pj);
            return true;
        }
        return false;
    }




    ///////////////////////////////////////////////////////////////////
    //////////                                               //////////
    //////////     P O L Y G O N S   A S S E M B L A G E     //////////
    //////////                                               //////////
    ///////////////////////////////////////////////////////////////////

    /**
     * Dresse une liste des paires de polylignes les plus rapprochées. Si cette liste
     * existait déjà, alors cette méthode ne fera que la remettre à jour en tentant
     * d'éviter de répéter certains calculs inutiles.
     */
    private void updateFermions() {
        if (progress != null) {
            progress.setDescription("Analyzing"); // TODO: localize
            progress.progress(0);
        }
        boolean hasChanged;
        while (true) {
            hasChanged = false;
            boolean tryAgain;
            do {
                tryAgain=false;
                for (int j=0; j<polygons.length; j++) {
                    if (progress != null) {
                        /*
                         * Utiliser 'j' directement pour informer des progrès ne donne pas
                         * une progression linéaire, car l'algorithme ci-dessous utilise
                         * deux blocs 'for' imbriqués. Le temps nécéssaire au calcul est de
                         * l'ordre de O(n²) ou n est le nombre de polylignes restant à traiter.
                         * On utilisera donc plutôt la formule ci-dessous, qui ferra paraître
                         * linéaire la progression.
                         */
                        progress.progress(100f * (j*(2*polygons.length-j)) /
                                          (polygons.length*polygons.length));
                    }
                    final Polygon jPath = polygons[j];
                    if (!isDone(jPath)) {
                        /*
                         * Le code de ce bloc est assez laborieux. Aussi, il ne sera exécuté que
                         * si les informations dans la cache ne sont plus valides pour cette
                         * polyligne. Les prochaines lignes calculent la distance entre le premier
                         * et le dernier point de la polyligne j. On part de l'hypothèse que j est
                         * un polygon fermé (île ou lac par exemple).
                         */
                        double minDistanceSq;
                        minDistanceSq = distanceSq(jPath.getFirstPoint(jFirstPoint),
                                                   jPath.getLastPoint (jLastPoint));
                        tryAgain |= candidateToClosing(jPath, minDistanceSq);
                        if (minDistanceSq != 0) { // Simple optimisation (pourrait être retirée)
                            /*
                             * On vérifie maintenant si, dans les prochaines polylignes, il y en
                             * aurait une dont le début ou la fin serait plus proche du début
                             * ou de la fin de la polyligne j. Si on trouve une telle polyligne,
                             * elle sera désignée par i et remplacera l'hypothèse précédente à
                             * l'effet que j est un polygon fermé.
                             */
                            for (int i=j+1; i<polygons.length; i++) {
                                final Polygon iPath = polygons[i];
                                if (!isDone(iPath)) {
                                    minDistanceSq = distanceSq(iPath.getFirstPoint(iFirstPoint),
                                                               iPath.getLastPoint (iLastPoint));
                                    tryAgain |= candidateToClosing(iPath, minDistanceSq);
                                    /*
                                     * Les conditions suivantes recherche avec quel agencement
                                     * des polylignes i et j on obtient la plus courte distance
                                     * possible. On commence par calculer cette distance en
                                     * supposant qu'aucune polyligne n'est inversée.
                                     */
                                    tryAgain |= candidateToMerging(jPath, true, iPath, false,
                                                        distanceSq(jLastPoint, iFirstPoint));
                                    /*
                                     * Distance si l'on suppose que la polyligne J est inversée.
                                     */
                                    tryAgain |= candidateToMerging(jPath, false, iPath, false,
                                                        distanceSq(jFirstPoint, iFirstPoint));
                                    /*
                                     * Distance si l'on suppose que la polyligne I est inversée.
                                     */
                                    tryAgain |= candidateToMerging(jPath, true, iPath, true,
                                                        distanceSq(jLastPoint, iLastPoint));
                                    /*
                                     * Distance si l'on suppose que les deux polylignes sont
                                     * inversées. Notez que fusionner I à J après avoir inversé
                                     * ces deux polylignes revient à fusionner J à I sans les
                                     * inverser, ce qui est plus rapide.
                                     */
                                    tryAgain |= candidateToMerging(jPath, false, iPath, true,
                                                        distanceSq(jFirstPoint, iLastPoint));
                                    /*
                                     * Si l'on vient de trouver que cette polyligne i est plus près
                                     * de la polyligne j que tous les autres jusqu'à maintenant
                                     * (incluant j lui-même), alors on mémorisera dans la cache
                                     * que j devrait être fusionnée avec i. Mais la boucle n'est
                                     * pas terminée et une meilleure combinaison peut encore
                                     * être trouvée...
                                     */
                                }
                            }
                        }
                    }
                }
                setAllComparisonsDone(true);
                hasChanged |= tryAgain;
            }
            while (tryAgain);
            if (!hasChanged) break;
            setAllComparisonsDone(false);
            /*
             * Après avoir fait un premier examen optimisée pour la vitesse, il est nécessaire
             * de refaire un second passage sans les optimisations. L'exemple suivant illustre
             * un cas où c'est nécessaire. Supposons que le premier passage a déterminé que la
             * polyligne A pourrait être fermée comme une île. Plus loin la polyligne B trouve
             * qu'elle pourrait se fusionner avec A pour former un segment AB, mais la distance
             * AB est plus grande que la distance AA, alors B laisse tomber. Supposons que plus
             * loin un segment C offre une distance AC plus courte que AA, de sorte que A n'est
             * plus une île. B n'a pas été informée que l'autre extrémité de A est devenu
             * disponible, d'où la nécessité d'un second passage. Si ce second passage n'a rien
             * changé, alors on aura vraiment terminé.
             */
        }
    }

    /**
     * Examine toutes les polylignes en mémoire et rattachent ensemble celles qui semblent faire
     * partie d'un même polygone fermé (par exemple un trait de côte ou une île).  Cette méthode
     * est très utile après la lecture d'un fichier de données bathymétriques généré par l'atlas
     * digitalisé GEBCO. Ce dernier ne dessine pas les côtes d'un seul trait.  Avoir un trait de
     * côte divisé en plusieurs segments nous empêche de déterminer si un point se trouve sur la
     * terre ferme ou sur la mer. Par le fait même, ça rend difficile tout remplissage des terres.
     * Cette méthode tente d'y remédier en procédant grosso-modo comme suit:<p>
     *
     * <ol>
     *   <li>Les polylignes seront examinées deux par deux. Toutes les combinaisons
     *       possibles de paires de polylignes seront évaluées.</li>
     *   <li>Pour deux polylignes données, la plus courte distance qui sépare deux extrémités
     *       sera calculée. Tous les agencements possibles entre le début ou la fin d'une
     *       polyligne avec le début ou la fin de l'autre seront évaluées.</li>
     *   <li>Parmis toutes les combinaisons possibles évaluées aux étapes 1 et 2, on recherche
     *       la plus courte distance entre deux extrémités. On ignore les cas où la polyligne la
     *       plus près d'une polyligne est elle-même (c'est-à-dire que la distance entre son début
     *       et sa fin est plus courte qu'avec tous autre agencement), car ils décrivent des
     *       polygones fermés.</li>
     *   <li>Tant qu'on a trouve deux polylignes très proches l'une de l'autre, on procède à leur
     *       fusion puis on recommence à l'étape 1.</li>
     * </ol>
     *
     * <blockquote>
     *     NOTE: L'implémentation actuelle de cette méthode ne prend pas en compte les
     *           cas où deux polylignes se chevaucheraient. (En fait, un début de prise
     *           en compte est fait et concerne les cas où des polylignes se chevauchent
     *           d'un seul point).
     * </blockquote>
     *
     * @throws TransformException if a transformation was needed and failed.
     */
    private void assemblePolygons() throws TransformException {
        updateFermions();
        final StringBuffer message;
        if (GeoShape.LOGGER.isLoggable(LEVEL)) {
            message = new StringBuffer();
        } else {
            message = null;
        }
        if (progress != null) {
            progress.setDescription("Assembling polygons"); // TODO: localize
            progress.progress(0);
        }
        int count = 0;
        final float progressScale = 100f / fermions.size();
        final double dmaxSq = dmax*dmax;
        Iterator it=fermions.values().iterator();
        while (it.hasNext()) {
            if (progress != null) {
                progress.progress(count++ * progressScale);
            }
            /*
             * Détermine quelles paires de polylignes sont séparées par la plus courte
             * distance. Cette paire sera retirée de la liste des polylignes à fusionner.
             */
            final FermionPair pair = (FermionPair) it.next();
            it.remove();
            if (pair.i.path!=pair.j.path && pair.minDistanceSq<=dmaxSq) {
                remove(pair); // Retire aussi l'autre référence
                /*
                 * Initialise des variables internes qui pointeront vers les données.
                 */
                final int overlap = (pair.minDistanceSq==0) ? 1 : 0;
                if (message != null) {
                    message.setLength(0);
                    message.append("Merging ");
                    message.append(pair);
                    message.append(' ');
                    // Will be completed later.
                }
                /*
                 * Procède à la fusion des polylignes. Il n'y aura pas de nouvelle
                 * allocation de mémoire. Ce sera un simple jeu de pointeurs. Si
                 * les deux polylignes sont à inverser, il sera plus rapide de les
                 * coller en ordre inverse (ça évite un appel à la fastidieuse
                 * méthode <code>reverse</code>).
                 */
                if (pair.i.mergeEnd == pair.j.mergeEnd) {
                    if (pair.i.path.getPointCount() <= pair.j.path.getPointCount()) {
                        reverse(pair.i.path);
                        pair.i.mergeEnd = !pair.i.mergeEnd;
                        if (message != null) {
                            message.append("(reverse #1) ");
                        }
                    } else {
                        reverse(pair.j.path);
                        pair.j.mergeEnd = !pair.j.mergeEnd;
                        if (message != null) {
                            message.append("(reverse #2) ");
                        }
                    }
                }
                if (pair.j.mergeEnd && !pair.i.mergeEnd) {
                    if (message != null) {
                        message.append("Append #1 to #2");
                        GeoShape.LOGGER.log(LEVEL, message.toString());
                    }
                    pair.j.path.append(pair.i.path.subpoly(overlap));
                    replace(pair.i.path, pair.j.path);
                    remove(pair.i.path);
                }
                else if (pair.i.mergeEnd && !pair.j.mergeEnd) {
                    if (message != null) {
                        message.append("Append #2 to #1");
                        GeoShape.LOGGER.log(LEVEL, message.toString());
                    }
                    pair.i.path.append(pair.j.path.subpoly(overlap));
                    replace(pair.j.path, pair.i.path);
                    remove(pair.j.path);
                }
                /*
                 * Les opérations précédentes ayant modifié la liste, on
                 * doit demander un nouvel itérateur pour pouvoir continuer.
                 */
                it = fermions.values().iterator();
            }
        }
    }




    ///////////////////////////////////////////////////////////////////
    //////////                                               //////////
    //////////     P O L Y G O N S   C O M P L E T I O N     //////////
    //////////                                               //////////
    ///////////////////////////////////////////////////////////////////

    /**
     * Examine toutes les polylignes en mémoire et rattachent ensemble celles qui semblent faire
     * partie d'un même polygone fermé. Cette méthode va aussi tenter de complèter les polygones
     * de façon à pouvoir les remplir. Elle est généralement appellée pour l'isoligne correspondant
     * au niveau 0. Les autres profondeurs utiliseront {@link #assemblePolygons} afin de ne pas
     * complèter les polygones.
     *
     * Pour pouvoir complèter correctement les polygones, cette méthode a besoin de connaître
     * la forme géométrique des limites de la carte. Il ne s'agit pas des limites que vous
     * souhaitez donner à la carte, mais des limites qui avaient été spécifiées au logiciel
     * qui a produit les données des polylignes. Dans la très grande majorité des cas, cette
     * forme est un rectangle. Mais cette méthode accepte aussi d'autres formes telles qu'un
     * cercle ou un triangle. La principale restriction est que cette forme doit contenir une
     * et une seule surface fermée. Cette forme géométrique aura été spécifiée lors de la
     * construction de cet objet <code>PolygonAssembler</code>.
     *
     * @param ptRef  La coordonnée d'un point en mer, selon le système de coordonnées de l'isoligne
     *               en cours ({@link #isoline}). Ce point <u>doit</u> être sur l'un des bords de
     *               la carte (gauche, droit, haut ou bas si {@link #clip} est un rectangle). S'il
     *               n'est pas exactement sur un des bords, il sera projeté sur le bord le plus
     *               proche.
     * @param inside Normalement <code>false</code>. Si toutefois l'argument <code>sea</code>
     *               ne représente non pas un point en mer mais plutot un point sur la
     *               terre ferme, alors spécifiez <code>true</code> pour cet argument.
     *
     * @throws TransformException if a transformation was needed and failed.
     * @throws IllegalStateException si une erreur est survenue lors du traitement des isolignes.
     * @throws IllegalArgumentException Si un problème d'unités est survenu. En principe, cette
     *         erreur ne devrait pas se produre.
     */
    private void completePolygons(final Point2D ptRef, final boolean inside)
            throws TransformException
    {
        if (progress != null) {
            progress.setDescription("Creating map border"); // TODO: localize
            progress.progress(0);
        }
        final List intersections = new ArrayList();
        IntersectionPoint startingPoint;
        /*
         * Projète sur la bordure de la carte la position du point de référence.
         * Au passage, cette méthode mémorisera le numéro du segment de droite
         * de la bordure sur laquelle se trouve le point, ainsi que son produit
         * scalaire. Ces informations serviront plus tard à déterminer dans quel
         * ordre rattacher les polylignes.
         */
        startingPoint                  = new IntersectionPoint(ptRef);
        startingPoint.border           = -1;
        startingPoint.scalarProduct    = Double.NaN;
        startingPoint.minDistanceSq    = Double.POSITIVE_INFINITY;
        startingPoint.coordinateSystem = isoline.getCoordinateSystem();
        PathIterator pit               = clip.getPathIterator(null, flatness);
        for (int border=0; !pit.isDone(); border++) {
            final boolean closed = nextSegment(pit); // Update 'pathLine'
            if (!isSingularity(pathLine)) {
                final Point2D projected = Geometry.nearestColinearPoint(pathLine, ptRef);
                final double distanceSq = ptRef.distanceSq(projected);
                if (distanceSq < startingPoint.minDistanceSq) {
                    startingPoint.setLocation(projected, pathLine, border);
                    startingPoint.minDistanceSq = distanceSq;
                }
            }
            if (closed) break;
        }
        GeoShape.LOGGER.log(LEVEL, "Reference point: "+startingPoint);
        if (startingPoint.minDistanceSq > flatness) {
            throw new IllegalStateException("Reference point is too far away"); // TODO: localize
        }
        updateFermions();
        /*
         * Les variables suivantes sont créées ici une fois pour toutes plutôt
         * que d'être créées à chaque exécution de la prochaine boucle.
         */
        final Point2D.Double[] iPoints = {iFirstPoint, iLastPoint};
        final Line           interpole = new Line();
        /*
         * Construit une liste des points d'intersections entre les polylignes et la bordure de
         * la carte. Après l'exécution de ce bloc, une série d'objets {@link IntersectionPoint}
         * aura été placée dans la liste {@link #intersections}. Pour chaque polyligne dont le
         * début ou la fin intercepte avec un des bords de la carte, {@link IntersectionPoint}
         * contiendra la coordonnée de ce point d'intersection ainsi qu'une information indiquant
         * si le calcul fut fait à partir des données du début ou de la fin du segment.
         */
        intersections.clear();
        for (int i=0; i<polygons.length; i++) {
            final Polygon iPath = polygons[i];
            /*
             * Met à jour la boîte de dialogue informant des
             * progrès de l'opération. Les progrès seront à
             * peu près proportionnels à <var>i</var>.
             */
            if (progress != null) {
                progress.progress(100f * i / polygons.length);
            }
            /*
             * La boucle suivante ne sera exécutée que deux fois. Le premier
             * passage examine les points se trouvant au début de la polyligne,
             * tandis que le second passage examine refait exactement le même
             * traitement mais avec les points se trouvant à la fin de la polyligne.
             */
            boolean append = false;
            do {
                Point2D.Double extremCoord;
                if (!append) {
                    iPath.getFirstPoints(iPoints);
                    extremCoord = iFirstPoint;
                } else {
                    iPath.getLastPoints(iPoints);
                    extremCoord = iLastPoint;
                }
                /*
                 * Interpole linéairement les éventuels points d'intersections avec les bords
                 * de la carte et vérifie si la distance entre les points originaux et les
                 * points interpolés est plus petite que celles qui ont été calculées par
                 * la méthode {@link #updateFermions}.
                 */
                FermionPair info = null;
                IntersectionPoint intersectPoint = null;
                interpole.setLine(iFirstPoint, iLastPoint);
                double minDistanceSq = Double.POSITIVE_INFINITY;
                pit = clip.getPathIterator(null, flatness);
                for (int border=0; !pit.isDone(); border++) {
                    final boolean closed = nextSegment(pit); // Update 'pathLine'
                    if (!isSingularity(pathLine)) {
                        final Point2D intPt = interpole.intersectionPoint(pathLine);
                        if (intPt != null) {
                            /*
                             * <code>intPt</code> contient le point d'intersection de la polyligne
                             * <code>iPath</code> avec le bord {@link #clip} de la carte. Les
                             * points <code>extrem*</code> (calculés précédemment) contiennent les
                             * coordonnées du point à l'extrémité (début ou fin) de la polyligne.
                             */
                            double compare = distanceSq(intPt, extremCoord);
                            if (compare < minDistanceSq) {
                                minDistanceSq = compare;
                                /*
                                 * On vérifie maintenant si la distance entre les deux points
                                 * calculée précédemment est plus courte que la plus courte
                                 * distance entre cette polyligne et n'importe quelle autre
                                 * polyligne.
                                 */
                                if (info == null) {
                                    info = get(iPath, append);
                                }
                                if (minDistanceSq <= info.minDistanceSq) {
                                    if (intersectPoint == null) {
                                        intersectPoint = new IntersectionPoint();
                                        intersections.add(intersectPoint);
                                    }
                                    intersectPoint.setLocation(intPt, pathLine, border);
                                    intersectPoint.path             = iPath;
                                    intersectPoint.append           = append;
                                    intersectPoint.coordinateSystem = isoline.getCoordinateSystem();
                                    intersectPoint.minDistanceSq    = minDistanceSq;
                                    info.allComparisonsDone = false;
                                }
                            }
                        }
                    }
                    if (closed) break;
                }
            }
            while ((append=!append) == true);
        }
        /*
         * Construit une bordure pour les polylignes. Pour fonctionner, cette méthode a besoin
         * qu'on lui ait calculé à l'avance les points d'intersections entre tous les polylignes
         * et la bordure de la carte. Ces points d'intersections doivent être fournis sous forme
         * d'objets {@link IntersectionPoint} regroupés dans l'ensemble <code>intersections</code>.
         * Ce code a aussi besoin d'un point de référence, <code>startingPoint</code>. Ce point
         * ne doit pas être égal à un des points d'intersection de <code>intersections</code>.
         * Il doit s'agir d'un point se trouvant soit à l'intérieur, soit à l'extérieur des
         * polylignes mais jamais sur leurs contours. L'argument <code>inside</code> indique
         * si ce point se trouve à l'intérieur ou à l'extérieur des polylignes.
         */
        pit = null;
        /*
         * La variable suivante indiquent le nombre de points d'intersections qui, pour
         * une raison quelconque, devraient être ignorés. Le nombre d'intersections avec
         * la bordure de la carte devrait en principe être un nombre pair. Toutefois, si
         * ce nombre est impair, alors cette variable sera incrémentée de 1 afin d'ignorer
         * le point d'intersection qui semble le moins approprié (celui qui est le plus loin
         * de la bordure de la carte).
         */
        int countIntersectionsToRemove = 0;
        /*
         * Le bloc suivant calcule les valeurs dans le tableau <code>intersectPoint</code>
         * Pour chaque segment dont le début ou la fin intersecte avec un des bords de la
         * carte, intersectPoint[...] contiendra la coordonnée de ce point d'intersection
         * ainsi qu'une information indiquant si le calcul fut fait à partir des données
         * du début ou de la fin du segment.
         */
        IntersectionPoint[] intersectPoints = new IntersectionPoint[intersections.size()];
        intersectPoints = (IntersectionPoint[]) intersections.toArray(intersectPoints);
        countIntersectionsToRemove += (intersectPoints.length & 1);
        if (countIntersectionsToRemove > 0) {
            if (countIntersectionsToRemove > intersectPoints.length) {
                countIntersectionsToRemove = intersectPoints.length;
            }
            Arrays.sort(intersectPoints, this);
            if (GeoShape.LOGGER.isLoggable(LEVEL)) {
                final StringBuffer message = new StringBuffer("Too many intersection points");
                int index = intersectPoints.length;
                final String lineSeparator = System.getProperty("line.separator", "\n");
                for (int i=countIntersectionsToRemove; --countIntersectionsToRemove>=0;) {
                    message.append(lineSeparator);
                    message.append("    Removing ");
                    message.append(intersectPoints[--index]);
                }
                GeoShape.LOGGER.log(LEVEL, message.toString());
            }
            intersectPoints = (IntersectionPoint[]) XArray.resize(intersectPoints,
                              intersectPoints.length-countIntersectionsToRemove);
        }
        if (intersectPoints.length == 0) {
            return;
        }
        /*
         * Maintenant que nous disposons des points                P1            P2
         * d'intersections, on les parcourera dans le         +-----o------------o---+
         * sens normal ou inverse des aiguilles d'une         |     :.           :...o P3
         * montre, selon l'implémentation de l'itérateur      |       :  ....        |
         * {@link PathIterator} utilisé. Un classement sera   |        :.:   :.      |
         * d'abord fait avec {@link Arrays#sort(Object[])}.   +---------------o------+
         * Les intersections seront jointes dans cet ordre.                  P4
         */
        Arrays.sort(intersectPoints);
        int indexNextIntersect = ~Arrays.binarySearch(intersectPoints, startingPoint);
        // Note: ~indexNextIntersect == -1-indexNextIntersect
        if (indexNextIntersect < 0) {
            // TODO: localize
            throw new IllegalArgumentException("Reference point too close from border");
        }
        if (inside) {
            indexNextIntersect++;
        }
        indexNextIntersect %= intersectPoints.length;
        if (GeoShape.LOGGER.isLoggable(LEVEL)) {
            final StringBuffer message = new StringBuffer("Sorted list of intersection points");
            final String lineSeparator = System.getProperty("line.separator", "\n");
            for (int j=0; j<intersectPoints.length; j++) {
                message.append(lineSeparator);
                message.append(j==indexNextIntersect ? "==> " : "    ");
                message.append(intersectPoints[j]);
            }
            GeoShape.LOGGER.log(LEVEL, message.toString());
        }
        /*
         * Procède maintenant à la création du cadre. On balayera tous les points
         * d'intersections, dans l'ordre dans lequels ils viennent d'être classés.
         * Le balayage commencera à partir du premier point d'intersection qui suit
         * le <code>startingPoint</code>. L'index de ce premier point à été calculé
         * plus haut dans <code>indexNextIntersect</code>.
         */
        int               pitBorder          = -1;
        float             buffer[]           = new float[16];
        boolean           traceLine          = false;
        IntersectionPoint lastIntersectPoint = null;
        IntersectionPoint intersectPoint;
        while ((intersectPoint=intersectPoints[indexNextIntersect]) != null) {
            final int indexLastIntersect = indexNextIntersect++;
            if (indexNextIntersect >= intersectPoints.length) {
                    indexNextIntersect=0;
            }
            intersectPoints[indexLastIntersect] = null;
            /*
             * Si l'on vient d'atteindre le premier point d'une terre, ne fait rien
             * et continue la boucle. Lorsqu'on aurra atteint le deuxième point de
             * la terre, alors on exécutera le bloc ci-dessous pour relier ces deux
             * points par une ligne.
             */
            if (traceLine) {
                buffer[0] = (float) lastIntersectPoint.x;
                buffer[1] = (float) lastIntersectPoint.y;
                int length = 2;
                /*
                 * Parvenu à ce stade, <code>nextIntersect</code> contient l'index du
                 * prochain point d'intersection d'une ligne de niveau avec la bordure
                 * de la carte. On suivra maintenant la bordure de la carte jusqu'à ce
                 * que l'on atteigne ce point.
                 */
                if (intersectPoint.border != lastIntersectPoint.border) {
                    /*
                     * Positionne l'itérateur sur le premier bord à considérer.
                     * C'est nécessaire lorsque tous les points précédemment
                     * fusionnés se trouvaient toujours sur le même bord. Dans
                     * ce cas, l'itérateur n'avait pas été incrémenté car on
                     * n'en avait peut-être plus de besoin.
                     */
                    while (pitBorder != lastIntersectPoint.border) {
                        if (pit==null || pit.isDone()) {
                            pit = clip.getPathIterator(null, flatness);
                            pitBorder = -1;
                        }
                        if (nextSegment(pit)) {
                            pit = null;
                            pitBorder = -1;
                        }
                        pitBorder++;
                    }
                    /*
                     * Suit les bords en mémorisant leurs coordonnées au passage,
                     * jusqu'à ce qu'on aie atteint le dernier bord. Les coordonnées
                     * seront mémorisées dans <code>buffer</code>, un tableau qui sera
                     * agrandi au grés des besoins.
                     */
                    do {
                        buffer[length++] = (float) pathLine.x2;
                        buffer[length++] = (float) pathLine.y2;
                        if (buffer[length-4]==buffer[length-2] &&
                            buffer[length-3]==buffer[length-1])
                        {
                            length -= 2;
                        }
                        if (length >= buffer.length) {
                            // It's ok to put this code here
                            buffer = XArray.resize(buffer, 2*length);
                        }
                        if (pit==null || pit.isDone()) {
                            pit = clip.getPathIterator(null, flatness);
                            pitBorder = -1;
                        }
                        if (nextSegment(pit)) {
                            pit = null;
                            pitBorder = -1;
                        }
                    }
                    while (++pitBorder != intersectPoint.border);
                }
                /*
                 * Mémorise les coordonnées du dernier point, de la même façon
                 * qu'on avait mémorisé ceux du premier point plus haut dans
                 * <code>iFirstPoint</code>.
                 */
                buffer[length++] = (float) intersectPoint.x;
                buffer[length++] = (float) intersectPoint.y;
                /*
                 * On dispose maintenant des coordonnées d'une ligne partant de la dernière
                 * intersection jusqu'à la prochaine. Ces coordonnées seront ajoutées au
                 * début ou à la fin du segment qui effectue la précédente intersection.
                 */
                buffer = XArray.resize(buffer, length);
                if (lastIntersectPoint.append) {
                    lastIntersectPoint.path.appendBorder(buffer, 0, length);
                } else {
                    reverse(buffer);
                    lastIntersectPoint.path.prependBorder(buffer, 0, length);
                }
                if (GeoShape.LOGGER.isLoggable(LEVEL)) {
                    final StringBuffer message = new StringBuffer("    Polygon[");
                    message.append(lastIntersectPoint.path.getPointCount());
                    message.append(" pts].");
                    message.append(lastIntersectPoint.append ? "append[" : "prepend[");
                    message.append(length/2);
                    message.append(" pts]");
                    GeoShape.LOGGER.log(LEVEL, message.toString());
                }
            }
            lastIntersectPoint = intersectPoint;
            traceLine = !traceLine;
        }
        if (traceLine) {
            throw new AssertionError("Odd intersects");
        }
        assemblePolygons();
        for (int i=0; i<polygons.length; i++) {
            polygons[i].close(InteriorType.FLAT);
        }
        /*
         * It is up to {@link Isoline} to decide if polygons are elevation
         * (e.g. island) or depression (e.g. lake). It will be done later.
         */
    }

    /**
     * Examine toutes les polylignes en mémoire et rattachent ensemble celles qui
     * semblent faire partie d'un même polygone fermé. Cette méthode est identique
     * à {@link #completePolygons(Point2D,boolean)}, excepté qu'elle déterminera elle-même
     * le point de référence à partir de l'isoligne spécifiée. L'isoligne spécifiée servira
     * uniquement de référence. Il ne doit pas être le même que celui qui a été spécifié
     * lors de la construction de cet objet.
     *
     * @throws IllegalStateException si une erreur est survenue lors du traitement des isolignes.
     * @throws NullPointerException si <code>otherIsoline</code> est nul ou ne contient pas de données.
     * @throws IllegalArgumentException Si un problème d'unités est survenu. En principe, cette erreur
     *         ne devrait pas se produre.
     * @throws TransformException if a transformation was needed and failed.
     */
    private void completePolygons(final Isoline otherIsoline) throws TransformException {
        if (otherIsoline.value == isoline.value) {
            throw new IllegalArgumentException("Same isoline level");
        }
        /*
         * 'refPt' contiendra le point de référence, qui servira à différencier
         * la terre de la mer. La création d'un objet {@link Point2D.Double} est
         * nécessaire car la méthode {@link #updateFermions} écrasera les valeurs
         * des autres coordonnées internes à cet objet.
         */
        final Point2D.Double refPt = new Point2D.Double();
        double minDistanceSq       = Double.POSITIVE_INFINITY;
        final PathIterator pit     = clip.getPathIterator(null, flatness);
        while (!pit.isDone()) {
            final boolean closed = nextSegment(pit);
            if (!isSingularity(pathLine)) {
                /*
                 * 'pathLine' représente maintenant une des bordure de la carte.
                 * On recherchera maintenant le polygone qui se termine le plus
                 * près de cette bordure.
                 */
                final Iterator iterator = otherIsoline.getPolygons().iterator();
                while (iterator.hasNext()) {
                    final Polygon jPath=(Polygon) iterator.next();
                    boolean first = true;
                    do { // Cette boucle sera exécutée deux fois
                        double distanceSq;
                        if (first) {
                            jPath.getFirstPoint(tmpPoint);
                        } else {
                            jPath.getLastPoint(tmpPoint);
                        }
                        /*
                         * Calcule la distance entre l'extrémité
                         * du polygone et la bordure de la carte.
                         */
                        final Point2D projected = Geometry.nearestColinearPoint(pathLine, tmpPoint);
                        distanceSq = distanceSq(tmpPoint, projected);
                        if (distanceSq < minDistanceSq) {
                            minDistanceSq = distanceSq;
                            refPt.setLocation(tmpPoint);
                        }
                    }
                    while ((first=!first) == false);
                }
            }
            if (closed) break;
        }
        completePolygons(refPt, isoline.value < otherIsoline.value);
    }




    ///////////////////////////////////////////////////////////////////
    //////////                                               //////////
    //////////             M E A N   M E T H O D             //////////
    //////////                                               //////////
    ///////////////////////////////////////////////////////////////////

    /**
     * Mean method. Assemble all polygons in the specified set of isolines.
     * The isolines are updated in place.
     *
     * @param  isolines Isolines to assemble.
     * @param  toComplete {@link Isoline#value} of isoline to complete with map border.
     *         Usually, only the coast line is completed (<code>value==0</code>).
     * @param  The boundind shape of the map, or <code>null</code> for assuming a rectangular
     *         map inferred from the <code>isolines</code>. This is the bounding shape of the
     *         software which that isoline data, not an arbitrary clip that the application
     *         would like.
     * @param  progress An optional progress listener (<code>null</code> in none).
     * @throws TransformException if a transformation was needed and failed.
     */
    public static void assemble(final Isoline[]        isolines,
                                final float[]          toComplete,
                                      Shape            mapBounds,
                                final ProgressListener progress)
            throws TransformException
    {
        if (progress != null) {
            progress.setDescription("Analyzing"); // TODO: localize
            progress.started();
        }
        Arrays.sort(isolines);
        Arrays.sort(toComplete);
        if (mapBounds == null) {
            Rectangle2D bounds = null;
            for (int i=0; i<isolines.length; i++) {
                final Rectangle2D toAdd = isolines[i].getBounds2D();
                if (bounds == null) {
                    bounds = toAdd;
                } else {
                    bounds.add(toAdd);
                }
            }
            mapBounds = bounds;
        }
        final PolygonAssembler assembler = new PolygonAssembler(mapBounds, progress);
        for (int i=0; i<isolines.length; i++) {
            assembler.setIsoline(isolines[i]);
            if (isolines.length > 1  &&  Arrays.binarySearch(toComplete, isolines[i].value) >= 0) {
                assembler.completePolygons(isolines[i!=0 ? 0 : isolines.length-1]);
            } else {
                assembler.assemblePolygons();
            }
            assembler.updateIsoline();
        }
        if (progress != null) {
            progress.complete();
        }
    }
}
