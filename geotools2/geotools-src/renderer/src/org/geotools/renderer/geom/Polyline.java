/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
 * (C) 2001, Institut de Recherche pour le Développement
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

// Collections
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.AbstractCollection;
import java.util.NoSuchElementException;
import java.lang.UnsupportedOperationException;

// Geometry
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.GeneralPath;

// Arrays
import org.geotools.renderer.array.ArrayData;
import org.geotools.renderer.array.PointArray;
import org.geotools.renderer.array.PointIterator;

// Coordinate systems
import org.geotools.units.Unit;
import org.geotools.cs.Ellipsoid;
import org.geotools.cs.CoordinateSystem;
import org.geotools.cs.CompoundCoordinateSystem;
import org.geotools.cs.ProjectedCoordinateSystem;
import org.geotools.cs.GeographicCoordinateSystem;
import org.geotools.cs.HorizontalDatum;
import org.geotools.ct.MathTransform;
import org.geotools.ct.MathTransform2D;
import org.geotools.ct.TransformException;
import org.geotools.ct.CoordinateTransformation;

// Miscellaneous
import java.io.Serializable;
import org.geotools.math.Statistics;
import org.geotools.resources.XArray;
import org.geotools.resources.Geometry;
import org.geotools.resources.Utilities;
import org.geotools.resources.CTSUtilities;
import org.geotools.resources.renderer.Resources;
import org.geotools.resources.renderer.ResourceKeys;


/**
 * Ligne tracée sans lever le crayon. Cette ligne ne représente par forcément une forme fermée
 * (un polygone). Les objets <code>Polyline</code> ont deux caractéristiques particulières:
 *
 * <ul>
 *   <li>Ils mémorisent séparément les points qui ne font que former une bordure. Par exemple, si
 *       seulement la moitié d'une île apparaît sur une carte, les points qui servent à joindre
 *       les deux extrémités des polylignes (en suivant la bordure de la carte là où l'île est
 *       coupée) n'ont pas de réalité géographique. Dans chaque objet <code>Polyline</code>, il doit
 *       y avoir une distinction claire entre les véritable points géographique les "points de
 *       bordure". Ces points sont mémorisés séparéments dans les tableaux
 *       {@link #prefix}/{@link #suffix} et {@link #array} respectivement.</li>
 *
 *   <li>Ils peuvent être chaînés avec d'autres objets <code>Polyline</code>. Former une chaîne
 *       d'objets <code>Polyline</code> peut être utile lorsque les coordonnées d'une côte ont été
 *       obtenues à partir de la digitalisation de plusieurs cartes bathymétriques, que l'on joindra
 *       en une ligne continue au moment du traçage. Elle peut aussi se produire lorsqu'une ligne
 *       qui se trouve près du bord de la carte entre, sort, réentre et resort plusieurs fois du
 *       cadre.</li>
 * </ul>
 *
 * Par convention, toutes les méthodes statiques de cette classe peuvent agir
 * sur une chaîne d'objets {@link Polyline} plutôt que sur une seule instance.
 *
 * @version $Id: Polyline.java,v 1.8 2003/05/13 11:00:46 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class Polyline implements Serializable {
    /**
     * Numéro de version pour compatibilité avec des bathymétries
     * enregistrées sous d'anciennes versions.
     */
    private static final long serialVersionUID = -18866207694621371L;

    /**
     * Set to <code>true</code> for disallowing two consecutive points with the same value.
     * Some algorithms in this class and in {@link PathAnalyser} require the distance between
     * two consecutive points to be greater than 0 in all cases.
     */
    private static final boolean REMOVE_DOUBLONS = true;

    /**
     * Set to <code>true</code> for removing doublons in borders as well.
     *
     * @task TODO: Current algorithm prevent the creation of curved line. The algorithm
     *             should be modified in such a way that  doublons are removed from the
     *             first of last points of the main data instead of from the border (the
     *             should not be modified by '[append|prepend]Border'. The work done by
     *             'addBorder' is correct however). How to do: add the following methods:
     *             'remove[First|Last]Points(int n)', and use it in '[append|prepend]Border'.
     */
    private static final boolean REMOVE_DOUBLONS_IN_BORDER = false;

    /**
     * Set to <code>true</code> if {@link #freeze} should try to merge the {@link #array} of
     * two consecutive polylines. Experience has show that it was often a bad idea, since it
     * force a lot of copies during clipping.
     */
    private static final boolean MERGE_POLYLINE_DATA = false;

    /**
     * Polylignes précédentes et suivantes. La classe <code>Polyline</code> implémente une liste à
     * double liens. Chaque objet <code>Polyline</code> est capable d'accéder et d'agir sur les
     * autres éléments de la liste à laquelle il appartient. En conséquent, il n'est pas nécessaire
     * d'utiliser une classe séparée (par exemple {@link java.util.LinkedList}) comme conteneur.
     * Il ne s'agit pas forcément d'un bon concept de programmation, mais il est pratique dans le
     * cas particulier de la classe <code>Polyline</code> et offre de bonnes performances.
     */
    private Polyline previous, next;

    /**
     * Coordonnées formant la polyligne. Ces coordonnées doivent être celles d'un trait de côte ou
     * de toute autre forme géométrique ayant une signification cartographique. Les points qui
     * servent à "couper" un polygone (par exemple des points longeant la bordure de la carte)
     * doivent être mémorisés séparément dans le tableau <code>suffix</code>.
     */
    private PointArray array;

    /**
     * Coordonnées à retourner après celles de <code>array</code>. Ces coordonnées servent
     * généralement à refermer un polygone, par exemple en suivant le cadre de la carte. Ce
     * champ peut être nul s'il ne s'applique pas.
     */
    private PointArray suffix;

    /**
     * Valeur minimales et maximales autorisées comme arguments pour les méthodes {@link #getArray}
     * et {@link #setArray}. Lorsque ces valeurs sont utilisées en ordre croissant, {@link #getArray}
     * retourne dans l'ordre les tableaux {@link #prefix}, {@link #array} et {@link #suffix}.
     * <br><br>
     * Note: si les valeurs de ces constantes changent, alors il faudra revoir l'implémentation des
     * méthodes suivantes:
     *
     *    {@link #getArray},
     *    {@link #setArray},
     *    {@link #reverse},
     *    {@link #freeze},
     */
    private static final int FIRST_ARRAY=0, LAST_ARRAY=1;

    /**
     * Construit un objet qui enveloppera les points spécifiés.
     * Cette polyligne fera initialement partie d'aucune liste.
     */
    Polyline(final PointArray array) {
        this.array = array;
    }

    /**
     * Construit des objets mémorisant les coordonnées <code>data</code>. Les valeurs
     * <code>NaN</code> au début et à la fin de <code>data</code> seront ignorées. Celles
     * qui apparaissent au milieu auront pour effet de séparer le trait en plusieurs polylignes.
     *
     * @param data   Tableau de coordonnées (peut contenir des NaN).
     * @return       Tableau de polylignes. Peut avoir une longueur de 0, mais ne sera jamais nul.
     */
    public static Polyline[] getInstances(final float[] data) {
        return getInstances(data, 0, data.length);
    }

    /**
     * Construit des objets mémorisant les coordonnées <code>data</code> de l'index
     * <code>lower</code> inclusivement jusqu'à <code>upper</code> exclusivement. Ces
     * index doivent se référer à la position absolue dans le tableau <code>data</code>,
     * c'est-à-dire être le double de l'index de la coordonnée. Les valeurs <code>NaN</code>
     * au début et à la fin de <code>data</code> seront ignorées. Celles qui apparaissent au
     * milieu auront pour effet de séparer le trait en plusieurs polylignes.
     *
     * @param data   Tableau de coordonnées (peut contenir des NaN).
     * @param lower  Index de la première donnée à considérer.
     * @param upper  Index suivant celui de la dernière donnée.
     * @return       Tableau de polylignes. Peut avoir une longueur de 0, mais ne sera jamais nul.
     */
    public static Polyline[] getInstances(float[] data, int lower, int upper) {
        if (REMOVE_DOUBLONS) {
            final float[] candidate = removeDoublons(data, lower, upper);
            if (candidate != null) {
                data  = candidate;
                lower = 0;
                upper = data.length;
            }
        }
        final List polylines = new ArrayList();
        for (int i=lower; i<upper; i+=2) {
            if (!Float.isNaN(data[i]) && !Float.isNaN(data[i+1])) {
                final int lowerValid = i;
                while ((i+=2) < upper) {
                    if (Float.isNaN(data[i]) || Float.isNaN(data[i+1])) {
                        break;
                    }
                }
                final PointArray points = PointArray.getInstance(data, lowerValid, i);
                if (points != null) {
                    polylines.add(new Polyline(points));
                }
            }
        }
        return (Polyline[]) polylines.toArray(new Polyline[polylines.size()]);
    }

    /**
     * Remove consecutive identical points, since it hurt many algorithms in this
     * package. {@link Float#NaN} values are ignored (they may have doublons).
     *
     * @param  data  The data to examine.
     * @param  lower The lower index to examine in <code>data</code>, inclusive.
     * @param  upper The upper index to examine in <code>data</code>, inclusive.
     * @return <code>null</code> if no doublons was found in <code>data</code>,
     *         otherwise a new array without doublons.
     */
    private static float[] removeDoublons(final float[] data, final int lower, final int upper) {
        int dest = 0;
        float[] copy = null;
        for (int i=lower; (i+=2)<upper;) {
            if (data[i-2]==data[i] && data[i-1]==data[i+1]) {
                if (copy == null) {
                    dest = i-lower;
                    copy = new float[upper-lower-2];
                    System.arraycopy(data, lower, copy, 0, dest);
                }
                continue;
            }
            if (copy != null) {
                copy[dest++] = data[i  ];
                copy[dest++] = data[i+1];
            }
        }
        if (copy != null) {
            copy = XArray.resize(copy, dest);
        }
        return copy;
    }

    /**
     * Renvoie le premier élément de la liste à laquelle appartient la
     * polyligne. Cette méthode peut retourner <code>scan</code>, mais
     * jamais <code>null</code>  (sauf si l'argument <code>scan</code>
     * est nul).
     */
    private static Polyline getFirst(Polyline scan) {
        if (scan != null) {
            while (scan.previous != null) {
                scan = scan.previous;
                assert scan.previous != scan;
                assert scan.next     != scan;
            }
        }
        return scan;
    }

    /**
     * Renvoie le dernier élément de la liste à laquelle appartient la
     * polyligne. Cette méthode peut retourner <code>scan</code>, mais
     * jamais <code>null</code>  (sauf si l'argument <code>scan</code>
     * est nul).
     */
    private static Polyline getLast(Polyline scan) {
        if (scan != null) {
            while (scan.next != null) {
                scan = scan.next;
                assert scan.previous != scan;
                assert scan.next     != scan;
            }
        }
        return scan;
    }

    /**
     * Ajoute la polyligne <code>toAdd</code> à la fin de la polyligne <code>queue</code>.
     * Les arguments <code>queue</code> et <code>toAdd</code> peuvent être n'importe
     * quel maillon d'une chaîne, mais cette méthode sera plus rapide si <code>queue</code>
     * est le dernier maillon.
     *
     * @param  queue <code>Polyline</code> à la fin duquel ajouter <code>toAdd</code>. Si cet
     *               argument est nul, alors cette méthode retourne directement <code>toAdd</code>.
     * @param  toAdd <code>Polyline</code> à ajouter à <code>queue</code>. Cet objet sera ajouté
     *               même s'il est vide. Si cet argument est nul, alors cette méthode retourne
     *               <code>queue</code> sans rien faire.
     * @return <code>Polyline</code> résultant de la fusion. Les anciens objets <code>queue</code>
     *         et <code>toAdd</code> peuvent avoir été modifiés et ne devraient plus être utilisés.
     * @throws IllegalArgumentException si <code>toAdd</code> avait déjà été ajouté à
     *         <code>queue</code>.
     */
    public static Polyline append(Polyline queue, Polyline toAdd) throws IllegalArgumentException {
        // On doit faire l'ajout même si 'toAdd' est vide.
        final Polyline veryLast = getLast(toAdd);
        toAdd = getFirst(toAdd);
        queue = getLast (queue);
        if (toAdd == null) return queue;
        if (queue == null) return toAdd;
        if (queue == veryLast) {
            throw new IllegalArgumentException();
        }

        assert queue.next     == null;
        assert toAdd.previous == null;
        queue.next     = toAdd;
        toAdd.previous = queue;

        assert getFirst(queue) == getFirst(toAdd);
        assert getLast (queue) == getLast (toAdd);
        assert veryLast.next   == null;
        return veryLast;
    }

    /**
     * Supprime ce maillon de la chaîne. Ce maillon
     * conservera toutefois ses données.
     */
    private void remove() {
        if (previous != null) {
            previous.next = next;
        }
        if (next != null) {
            next.previous = previous;
        }
        previous = next = null;
    }

    /**
     * Indique si cette polyligne est vide. Une polyligne est vide si tous
     * ces tableaux sont nuls. Cette méthode ne vérifie pas l'état des
     * autres maillons de la chaîne.
     */
    private boolean isEmpty() {
        return array==null && suffix==null;
    }

    /**
     * Retourne un des tableaux de données de cette polyligne. Le tableau retourné
     * peut être {@link #prefix}, {@link #array} ou {@link #suffix} selon que
     * l'argument est -1, 0 ou +1 respectivement. Toute autre valeur lancera
     * une exception.
     *
     * @param arrayID Un code compris entre {@link #FIRST_ARRAY}
     *                et {@link #LAST_ARRAY} inclusivement.
     */
    private PointArray getArray(final int arrayID) {
        switch (arrayID) {
        //  case -1: return prefix;
            case  0: return array;
            case +1: return suffix;
            default: throw new IllegalArgumentException(String.valueOf(arrayID));
        }
    }

    /**
     * Modifie un des tableaux de données de cette polyligne. Le tableau modifié
     * peut être {@link #prefix}, {@link #array} ou {@link #suffix} selon que
     * l'argument est -1, 0 ou +1 respectivement.  Toute autre valeur lancera
     * une exception.
     *
     * @param arrayID Un code compris entre {@link #FIRST_ARRAY}
     *                et {@link #LAST_ARRAY} inclusivement.
     */
    private void setArray(final int arrayID, final PointArray data) {
        switch (arrayID) {
        //  case -1: prefix=data; break;
            case  0: array =data; break;
            case +1: suffix=data; break;
            default: throw new IllegalArgumentException(String.valueOf(arrayID));
        }
    }

    /**
     * Returns an estimation of memory usage in bytes.  This method is for information
     * purpose only. The memory really used by two polylines may be lower than the sum
     * of their  <code>getMemoryUsage()</code>  return values,  since polylines try to
     * share their data when possible. Furthermore, this method do not take in account
     * the extra bytes generated by Java Virtual Machine for each objects.
     *
     * @return An <em>estimation</em> of memory usage in bytes.
     */
    public static long getMemoryUsage(Polyline scan) {
        scan = getFirst(scan);
        long count = 16; // Take in account 4 internal fields of reference type (4 bytes each).
        while (scan != null) {
            for (int i=FIRST_ARRAY; i<=LAST_ARRAY; i++) {
                final PointArray data = scan.getArray(i);
                if (data!=null) {
                    count += data.getMemoryUsage();
                }
            }
            scan = scan.next;
        }
        return count;
    }

    /**
     * Retourne le nombre de points de la polyligne spécifiée
     * ainsi que de tous les polylignes qui le suivent.
     *
     * @param scan Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *             mais cette méthode sera plus rapide si c'est le premier maillon.
     */
    public static int getPointCount(Polyline scan) {
        scan = getFirst(scan);
        int count = 0;
        while (scan != null) {
            for (int i=FIRST_ARRAY; i<=LAST_ARRAY; i++) {
                final PointArray data = scan.getArray(i);
                if (data!=null) {
                    count += data.count();
                }
            }
            scan = scan.next;
        }
        return count;
    }

    /**
     * Returns <code>true</code> if at least one point of the specified polyline is a border.
     */
    public static boolean hasBorder(Polyline scan) {
        scan = getFirst(scan);
        while (scan != null) {
            if (scan.suffix != null) {
                return true;
            }
            scan = scan.next;
        }
        return false;
    }

    /**
     * Donne à la coordonnée spécifiée la valeur du premier point. Si une bordure a été
     * ajoutée avec la méthode {@link #prepend}, elle sera pris en compte. Si cet objet
     * <code>Polyline</code> ne contient aucun point, l'objet qui suit dans la chaîne
     * sera automatiquement interrogé.
     *
     * @param  scan  Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *               mais cette méthode sera plus rapide si c'est le premier maillon.
     * @param  point Point dans lequel mémoriser la coordonnée.
     * @return L'argument <code>point</code>, ou un nouveau point
     *         si <code>point</code> était nul.
     * @throws NoSuchElementException Si <code>scan</code> est nul
     *         ou s'il ne reste plus de points dans la chaîne.
     *
     * @see #getFirstPoints
     * @see #getLastPoint
     */
    public static Point2D getFirstPoint(Polyline scan, final Point2D point)
            throws NoSuchElementException
    {
        scan = getFirst(scan);
        while (scan != null) {
            for (int i=FIRST_ARRAY; i<=LAST_ARRAY; i++) {
                final PointArray data=scan.getArray(i);
                if (data != null) {
                    return data.getFirstPoint(point);
                }
            }
            scan = scan.next;
        }
        throw new NoSuchElementException();
    }

    /**
     * Donne à la coordonnée spécifiée la valeur du dernier point. Si une bordure a été
     * ajoutée avec la méthode {@link #append}, elle sera pris en compte.  Si cet objet
     * <code>Polyline</code> ne contient aucun point, l'objet qui précède dans la chaîne
     * sera automatiquement interrogé.
     *
     * @param  scan  Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *               mais cette méthode sera plus rapide si c'est le dernier maillon.
     * @param  point Point dans lequel mémoriser la coordonnée.
     * @return L'argument <code>point</code>, ou un nouveau point
     *         si <code>point</code> était nul.
     * @throws NoSuchElementException Si <code>scan</code> est nul
     *         ou s'il ne reste plus de points dans la chaîne.
     *
     * @see #getLastPoints
     * @see #getFirstPoint
     */
    public static Point2D getLastPoint(Polyline scan, final Point2D point)
            throws NoSuchElementException
    {
        scan = getLast(scan);
        while (scan != null) {
            for (int i=LAST_ARRAY; i>=FIRST_ARRAY; i--) {
                PointArray data=scan.getArray(i);
                if (data != null) {
                    return data.getLastPoint(point);
                }
            }
            scan = scan.previous;
        }
        throw new NoSuchElementException();
    }

    /**
     * Donne aux coordonnées spécifiées les valeurs des premiers points.
     *
     * @param scan   Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *               mais cette méthode sera plus rapide si c'est le premier maillon.
     * @param points Tableau dans lequel mémoriser les premières coordonnées. <code>points[0]</code>
     *               contiendra la première coordonnée, <code>points[1]</code> la seconde, etc. Si
     *               un élément de ce tableau est nul, un objet {@link Point2D} sera automatiquement
     *               créé.
     *
     * @throws NoSuchElementException Si <code>scan</code> est nul ou
     *         s'il ne reste pas suffisament de points dans la chaîne.
     */
    public static void getFirstPoints(Polyline scan, final Point2D points[])
            throws NoSuchElementException
    {
        scan = getFirst(scan);
        if (points.length == 0) {
            return;
        }
        if (scan == null) {
            throw new NoSuchElementException();
        }
        int      arrayID = FIRST_ARRAY;
        PointArray  data = null;
        PointIterator it = null;
        for (int j=0; j<points.length; j++) {
            while (it==null || !it.hasNext()) {
                if (arrayID > LAST_ARRAY) {
                    arrayID = FIRST_ARRAY;
                    scan    = scan.next;
                    if (scan == null) {
                        throw new NoSuchElementException();
                    }
                }
                data = scan.getArray(arrayID++);
                if (data != null) {
                    it = data.iterator(0);
                }
            }
            if (points[j] == null) {
                points[j] = new Point2D.Float(it.nextX(), it.nextY());
            } else {
                points[j].setLocation(it.nextX(), it.nextY());
            }
            if (REMOVE_DOUBLONS) {
                assert j==0 || !points[j].equals(points[j-1]) : scan;
            }
        }
        assert Utilities.equals(getFirstPoint(scan, null), points[0]) : scan;
    }

    /**
     * Donne aux coordonnées spécifiées les valeurs des derniers points.
     *
     * @param scan   Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *               mais cette méthode sera plus rapide si c'est le dernier maillon.
     * @param points Tableau dans lequel mémoriser les dernières coordonnées.
     *               <code>points[length-1]</code> contiendra la dernière coordonnée,
     *               <code>points[length-2]</code> l'avant dernière, etc. Si un élément de
     *               ce tableau est nul, un objet {@link Point2D} sera automatiquement créé.
     *
     * @throws NoSuchElementException Si <code>scan</code> est nul ou
     *         s'il ne reste pas suffisament de points dans la chaîne.
     */
    public static void getLastPoints(Polyline scan, final Point2D points[])
            throws NoSuchElementException
    {
        scan = getLast(scan);
        if (points.length == 0) {
            // Nécessaire pour l'implémentation ci-dessous.
            return;
        }
        if (scan == null) {
            throw new NoSuchElementException();
        }
        int startIndex = -points.length;
        int    arrayID = LAST_ARRAY+1;
        PointArray data;
        /*
         * Recherche la position à partir d'où lire les données.  A la
         * sortie de cette boucle, la première donnée valide sera à la
         * position <code>scan.getArray(arrayID).iterator(i)</code>.
         */
        do {
            do {
                if (--arrayID < FIRST_ARRAY) {
                    arrayID = LAST_ARRAY;
                    scan = scan.previous;
                    if (scan==null) {
                        throw new NoSuchElementException();
                    }
                }
                data = scan.getArray(arrayID);
            }
            while (data==null);
            startIndex += data.count();
        }
        while (startIndex < 0);
        /*
         * Procède à la mémorisation des coordonnées.   Note: parvenu à ce stade, 'data' devrait
         * obligatoirement être non-nul. Un {@link NullPointerException} dans le code ci-dessous
         * serait une erreur de programmation.
         */
        PointIterator it = data.iterator(startIndex);
        for (int j=0; j<points.length; j++) {
            while (!it.hasNext()) {
                do {
                    if (++arrayID > LAST_ARRAY) {
                        arrayID = FIRST_ARRAY;
                        scan = scan.next;
                    }
                    data = scan.getArray(arrayID);
                }
                while (data==null);
                it = data.iterator(0);
            }
            if (points[j] == null) {
                points[j]=new Point2D.Float(it.nextX(), it.nextY());
            } else {
                points[j].setLocation(it.nextX(), it.nextY());
            }
            if (REMOVE_DOUBLONS) {
                assert j==0 || !points[j].equals(points[j-1]) : scan;
            }
        }
        assert !it.hasNext();
        assert Utilities.equals(getLastPoint(scan, null), points[points.length-1]) : scan;
    }

    /**
     * Retourne une polyligne qui couvrira les données de cette polyligne
     * de l'index <code>lower</code> inclusivement jusqu'à l'index
     * <code>upper</code> exclusivement.
     *
     * @param scan  Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *              mais cette méthode sera plus rapide si c'est le premier maillon.
     * @param lower Index du premier point à retenir.
     * @param upper Index suivant celui du dernier point à retenir.
     * @return      Une chaîne de nouvelles polylignes, ou <code>scan</code> si aucun
     *              point n'a été ignorés. Si la polyligne obtenu ne contient aucun
     *              point, alors cette méthode retourne <code>null</code>.
     */
    public static Polyline subpoly(Polyline scan, int lower, int upper) {
        if (lower == upper) {
            return null;
        }
        scan = getFirst(scan);
        if (lower==0 && upper==getPointCount(scan)) {
            return scan;
        }
        Polyline queue=null;
        while (scan!=null) {
            Polyline toAdd = null;
            for (int i=FIRST_ARRAY; i<=LAST_ARRAY; i++) {
                PointArray data = scan.getArray(i);
                if (data == null) {
                    continue;
                }
                /*
                 * Vérifie si le tableau 'data' contient au moins quelques points
                 * à prendre en compte. Si ce n'est pas le cas, il sera ignoré en
                 * bloc.
                 */
                int count = data.count();
                if (count <= lower) {
                    lower -= count;
                    upper -= count;
                    continue;
                }
                /*
                 * Prend en compte les données de 'data' de 'lower' jusqu'à 'upper',
                 * mais sans dépasser la longueur du tableau. S'il reste encore des
                 * points à aller chercher (upper!=0), on examinera les tableaux suivants.
                 */
                if (count > upper) {
                    count = upper;
                }
                assert lower >= 0 : lower;
                assert count <= data.count() : count;
                data = data.subarray(lower, count);
                if (data != null) {
                    if (toAdd == null) {
                        toAdd = new Polyline(null);
                        queue = append(queue, toAdd);
                    }
                    assert toAdd.getArray(i)==null;
                    toAdd.setArray(i, data);
                }
                lower  = 0;
                upper -= count;
                if (upper==0) {
                    return queue;
                }
            }
            scan = scan.next;
        }
        throw new IndexOutOfBoundsException();
    }

    /**
     * Ajoute des points à la bordure de cette polyligne. Cette méthode est réservée
     * à un usage interne par {@link #prependBorder} et {@link #appendBorder}.
     */
    private void addBorder(float[] data, int lower, int upper, final boolean toEnd) {
        if (REMOVE_DOUBLONS) {
            final float[] candidate = removeDoublons(data, lower, upper);
            if (candidate != null) {
                data  = candidate;
                lower = 0;
                upper = data.length;
            }
        }
        if (suffix == null) {
            suffix = PointArray.getInstance(data, lower, upper);
        } else {
            suffix = suffix.insertAt(toEnd ? suffix.count() : 0, data, lower, upper, false);
        }
    }

    /**
     * Ajoute des points au début de cette polyligne. Ces points seront considérés comme
     * faisant partie de la bordure de la carte, et non comme des points représentant
     * une structure géographique.
     *
     * @param  scan  Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne.
     * @param  data  Coordonnées à ajouter sous forme de paires de nombres (x,y).
     * @param  lower Index du premier <var>x</var> à ajouter à la bordure.
     * @param  upper Index suivant celui du dernier <var>y</var> à ajouter à la bordure.
     * @return Polyline résultant. Ca sera en général <code>scan</code>.
     */
    public static Polyline prependBorder(Polyline scan, final float[] data, int lower, int upper) {
        if (REMOVE_DOUBLONS_IN_BORDER) {
            try {
                final Point2D check = getFirstPoint(scan, null);
                final float x = (float)check.getX();
                final float y = (float)check.getY();
                while (lower<upper && data[upper-2]==x && data[upper-1]==y) {
                    upper -= 2;
                }
            } catch (NoSuchElementException exception) {
                // No points in this polyline, no doublons, no problem. Continue...
            }
        }
        final int length = upper-lower;
        if (length > 0) {
            scan = getFirst(scan);
            if (scan==null || scan.array!=null) {
                scan = getFirst(append(new Polyline(null), scan));
                assert scan.array==null;
            }
            scan.addBorder(data, lower, upper, false);
        }
        return scan;
    }

    /**
     * Ajoute des points à la fin de cette polyligne. Ces points seront considérés comme
     * faisant partie de la bordure de la carte, et non comme des points représentant
     * une structure géographique.
     *
     * @param  scan  Polyline. Cet argument peut être n'importe quel maillon d'une chaîne.
     * @param  data  Coordonnées à ajouter sous forme de paires de nombres (x,y).
     * @param  lower Index du premier <var>x</var> à ajouter à la bordure.
     * @param  upper Index suivant celui du dernier <var>y</var> à ajouter à la bordure.
     * @return Polyligne résultante. Ca sera en général <code>scan</code>.
     */
    public static Polyline appendBorder(Polyline scan, final float[] data, int lower, int upper) {
        if (REMOVE_DOUBLONS_IN_BORDER) {
            try {
                final Point2D check = getLastPoint(scan, null);
                final float x = (float)check.getX();
                final float y = (float)check.getY();
                while (lower<upper && data[lower]==x && data[lower+1]==y) {
                    lower += 2;
                }
            } catch (NoSuchElementException exception) {
                // No points in this polyline, no doublons, no problem. Continue...
            }
        }
        final int length = upper-lower;
        if (length > 0) {
            scan = getLast(scan);
            if (scan == null) {
                scan = new Polyline(null);
            }
            scan.addBorder(data, lower, upper, true);
        }
        return scan;
    }

    /**
     * Inverse l'ordre de tous les points.  Cette méthode retournera le
     * premier maillon d'une nouvelle chaîne de polylignes qui contiendra
     * les données en ordre inverse.
     *
     * @param  scan Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *              mais cette méthode sera plus rapide si c'est le dernier maillon.
     */
    public static Polyline reverse(Polyline scan) {
        Polyline queue=null;
        for (scan=getLast(scan); scan!=null; scan=scan.previous) {
            for (int arrayID=LAST_ARRAY; arrayID>=FIRST_ARRAY; arrayID--) {
                PointArray array = scan.getArray(arrayID);
                if (array != null) {
                    array = array.reverse();
                    /*
                     * Tous les tableaux sont balayés dans cette boucle,
                     * un à un et dans l'ordre inverse. Les préfix doivent
                     * devenir des suffix, et les suffix doivent devenir
                     * des préfix.
                     */
                    if (arrayID == 0) {
                        queue = append(queue, new Polyline(array));
                    } else {
                        queue = getLast(queue); // Par précaution.
                        if (queue == null) {
                            queue = new Polyline(null);
                        }
                        assert queue.suffix==null;
                        queue.suffix=array;
                    }
                }
            }
        }
        return queue;
    }

    /**
     * Retourne les coordonnées d'une boîte qui englobe complètement tous
     * les points de la polyligne. Si cette polyligne ne contient aucun point,
     * alors cette méthode retourne <code>null</code>.
     *
     * @param  scan Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *              mais cette méthode sera plus rapide si c'est le premier maillon.
     * @param  transform Transformation à appliquer sur les données (nulle pour aucune).
     * @return Un rectangle englobeant toutes les coordonnées de cette polyligne et de
     *         ceux qui la suivent.
     * @throws TransformException Si une projection cartographique a échoué.
     */
    public static Rectangle2D getBounds2D(Polyline scan, final MathTransform2D transform)
            throws TransformException
    {
        float xmin = Float.POSITIVE_INFINITY;
        float xmax = Float.NEGATIVE_INFINITY;
        float ymin = Float.POSITIVE_INFINITY;
        float ymax = Float.NEGATIVE_INFINITY;
        final Point2D.Float point=new Point2D.Float();
        for (scan=getFirst(scan); scan!=null; scan=scan.next) {
            for (int arrayID=FIRST_ARRAY; arrayID<=LAST_ARRAY; arrayID++) {
                final PointArray array = scan.getArray(arrayID);
                if (array != null) {
                    final PointIterator it=array.iterator(0);
                    if (transform!=null && !transform.isIdentity()) {
                        while (it.hasNext()) {
                            point.x=it.nextX();
                            point.y=it.nextY();
                            transform.transform(point, point);
                            if (point.x<xmin) xmin=point.x;
                            if (point.x>xmax) xmax=point.x;
                            if (point.y<ymin) ymin=point.y;
                            if (point.y>ymax) ymax=point.y;
                        }
                    } else {
                        while (it.hasNext()) {
                            final float x=it.nextX();
                            final float y=it.nextY();
                            if (x<xmin) xmin=x;
                            if (x>xmax) xmax=x;
                            if (y<ymin) ymin=y;
                            if (y>ymax) ymax=y;
                        }
                    }
                }
            }
        }
        if (xmin<=xmax && ymin<=ymax) {
            return new Rectangle2D.Float(xmin, ymin, xmax-xmin, ymax-ymin);
        } else {
            return null;
        }
    }

    /**
     * Renvoie des statistiques sur la résolution d'un polyligne. Cette résolution sera
     * la distance moyenne entre deux points du polyligne,  mais sans prendre en compte
     * les "points de bordure"  (par exemple les points qui suivent le bord d'une carte
     * plutôt que de représenter une structure géographique réelle).
     * <br><br>
     * La résolution est calculée en utilisant le système de coordonnées spécifié. Les
     * unités du résultat seront donc  les unités des deux premiers axes de ce système
     * de coordonnées,  <strong>sauf</strong>  si les deux premiers axes utilisent des
     * coordonnées géographiques angulaires  (c'est le cas notamment des objets {@link
     * GeographicCoordinateSystem}).  Dans ce dernier cas,  le calcul utilisera plutôt
     * les distances orthodromiques sur l'ellipsoïde ({@link Ellipsoid}) du système de
     * coordonnées.   En d'autres mots, pour les systèmes cartographiques, le résultat
     * de cette méthode sera toujours exprimé en unités linéaires (souvent des mètres)
     * peu importe que le système de coordonnées soit {@link ProjectedCoordinateSystem}
     * ou {@link GeographicCoordinateSystem}.
     *
     * @param  scan Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *         mais cette méthode sera plus rapide si c'est le premier maillon.
     * @param  transformation Systèmes de coordonnées source et destination.
     *         <code>getSourceCS()</code> doit être le système interne des points
     *         des polylignes, tandis que  <code>getTargetCS()</code> doit être le
     *         système dans lequel faire le calcul. C'est <code>getTargetCS()</code>
     *         qui déterminera les unités du résultat. Cet argument peut être nul
     *         si aucune transformation n'est nécessaire. Dans ce cas, le système
     *         de coordonnées <code>getTargetCS()</code> sera supposé cartésien.
     * @return Statistiques sur la résolution. L'objet retourné ne sera jamais nul, mais les
     *         statistiques seront tous à NaN si cette courbe de niveau ne contenait aucun
     *         point. Voir la description de cette méthode pour les unités.
     * @throws TransformException Si une transformation de coordonnées a échouée.
     */
    static Statistics getResolution(Polyline scan, final CoordinateTransformation transformation)
            throws TransformException
    {
        /*
         * Checks the coordinate system validity. If valid and if geographic,
         * gets the ellipsoid to use for orthodromic distance computations.
         */
        final MathTransform2D transform;
        final Ellipsoid       ellipsoid;
        final Unit         xUnit, yUnit;
        if (transformation != null) {
            final MathTransform tr = transformation.getMathTransform();
            transform = !tr.isIdentity() ? (MathTransform2D) tr : null;
            final CoordinateSystem targetCS = transformation.getTargetCS();
            xUnit = targetCS.getUnits(0);
            yUnit = targetCS.getUnits(1);
            if (!Utilities.equals(xUnit, yUnit)) {
                throw new IllegalArgumentException(Resources.format(
                                            ResourceKeys.ERROR_NON_CARTESIAN_COORDINATE_SYSTEM_$1,
                                            targetCS.getName(null)));
            }
            ellipsoid = CTSUtilities.getHeadGeoEllipsoid(targetCS);
        } else {
            transform = null;
            ellipsoid = null;
            xUnit = yUnit = null;
        }
        /*
         * Compute statistics...
         */
        final Statistics stats = new Statistics();
        Point2D          point = new Point2D.Double();
        Point2D           last = null;
        for (scan=getFirst(scan); scan!=null; scan=scan.next) {
            final PointArray array = scan.array;
            if (array == null) {
                continue;
            }
            final PointIterator it = array.iterator(0);
            while (it.hasNext()) {
                point.setLocation(it.nextX(), it.nextY());
                if (transform != null) {
                    point = transform.transform(point, point);
                }
                final double distance;
                if (ellipsoid != null) {
                    point.setLocation(Unit.DEGREE.convert(point.getX(), xUnit),
                                      Unit.DEGREE.convert(point.getY(), yUnit));
                    if (last == null) {
                        last = (Point2D) point.clone();
                        continue;
                    }
                    distance = ellipsoid.orthodromicDistance(last, point);
                } else {
                    if (last == null) {
                        last = (Point2D) point.clone();
                        continue;
                    }
                    distance = last.distance(point);
                }
                stats.add(distance);
                final Point2D swap = last;
                last = point;
                point = swap;
            }
        }
        return stats;
    }

    /**
     * Modifie la résolution de cette carte. Cette méthode procèdera en interpolant les données
     * de façon à ce que chaque point soit séparé du précédent par la distance spécifiée.  Cela
     * peut se traduire par des économies importante de mémoire  si  une trop grande résolution
     * n'est pas nécessaire. Notez que cette opération est irreversible.  Appeler cette méthode
     * une seconde fois avec une résolution plus fine gonflera la taille des tableaux internes,
     * mais sans amélioration réelle de la précision.
     *
     * @param  scan Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *         mais cette méthode sera plus rapide si c'est le premier maillon.
     * @param  transformation Transformation permettant de convertir les coordonnées des polylignes
     *         vers des coordonnées cartésiennes. Cet argument peut être nul si les coordonnées de
     *         <code>this</code> sont déjà exprimées selon un système de coordonnées cartésiennes.
     * @param  resolution Résolution désirée, selon les mêmes unités que {@link #getResolution}.
     * @throws TransformException Si une erreur est survenue lors d'une projection cartographique.
     *
     * @see #getResolution
     */
    public static void setResolution(Polyline scan, final CoordinateTransformation transformation,
                                     double resolution)
            throws TransformException
    {
        /*
         * Checks arguments validity. This method do not support latitude/longitude
         * coordinates. Coordinates must be projected in some linear units.
         */
        if (!(resolution > 0)) {
            throw new IllegalArgumentException(String.valueOf(resolution));
        }
        final MathTransform2D transform;
        final MathTransform2D inverseTransform;
        if (transformation != null) {
            final CoordinateSystem targetCS = transformation.getTargetCS();
            if (CTSUtilities.getHeadGeoEllipsoid(targetCS)!=null ||
                !Utilities.equals(targetCS.getUnits(0), targetCS.getUnits(1)))
            {
                throw new IllegalArgumentException(Resources.format(
                                            ResourceKeys.ERROR_NON_CARTESIAN_COORDINATE_SYSTEM_$1,
                                            targetCS.getName(null)));
            }
            final MathTransform tr = transformation.getMathTransform();
            if (!tr.isIdentity()) {
                transform        = (MathTransform2D) tr;
                inverseTransform = (MathTransform2D) transform.inverse();
            } else {
                transform        = null;
                inverseTransform = null;
            }
        } else {
            transform        = null;
            inverseTransform = null;
        }
        /*
         * Performs the linear interpolations, assuming
         * that we are using a cartesian coordinate system.
         */
        for (scan=getFirst(scan); scan!=null; scan=scan.next) {
            final PointArray points = scan.array;
            if (points == null) {
                continue;
            }
            /*
             * Obtiens les coordonnées projetées. Si ces coordonnées représentent des
             * degrés de longitudes et latitudes, alors une projection cartographique
             * sera obligatoire afin de faire correctement les calculs de distances.
             */
            float[] array = points.toArray();
            assert (array.length & 1)==0;
            if (transform!=null && !transform.isIdentity()) {
                transform.transform(array, 0, array, 0, array.length/2);
            }
            if (array.length >= 2) {
                /*
                 * Effectue la décimation des coordonnées. La toute première
                 * coordonnée sera conservée inchangée. Il en ira de même de
                 * la dernière, à la fin de ce bloc.
                 */
                final Point2D.Float point = new Point2D.Float(array[0], array[1]);
                final Line2D.Float   line = new  Line2D.Float(0,0, point.x, point.y);
                int destIndex   = 2; // Ne touche pas au premier point.
                int sourceIndex = 2; // Le premier point est déjà lu.
                while (sourceIndex < array.length) {
                    line.x1 = line.x2;
                    line.y1 = line.y2;
                    line.x2 = array[sourceIndex++];
                    line.y2 = array[sourceIndex++];
                    Point2D next;
                    while ((next=Geometry.colinearPoint(line, point, resolution)) != null) {
                        if (destIndex == sourceIndex) {
                            final int extra = 256;
                            final float[] oldArray=array;
                            array=new float[array.length + extra];
                            System.arraycopy(oldArray, 0,         array, 0,                                  destIndex);
                            System.arraycopy(oldArray, destIndex, array, sourceIndex+=extra, oldArray.length-destIndex);
                        }
                        assert destIndex < sourceIndex;
                        array[destIndex++] = line.x1 = point.x = (float)next.getX();
                        array[destIndex++] = line.y1 = point.y = (float)next.getY();
                    }
                }
                /*
                 * La décimation est maintenant terminée. Vérifie si le dernier point
                 * apparaît dans le tableau décimé. S'il n'apparaît pas, on l'ajoutera.
                 * Ensuite, on libèrera la mémoire réservée en trop.
                 */
                if (array[destIndex-2] != line.x2  ||  array[destIndex-1] != line.y2) {
                    if (destIndex == array.length) {
                        array = XArray.resize(array, destIndex+2);
                    }
                    array[destIndex++] = line.x2;
                    array[destIndex++] = line.y2;
                }
                if (destIndex != array.length) {
                    array = XArray.resize(array, destIndex);
                }
            }
            /*
             * Les interpolations étant terminées, reconvertit les coordonnées
             * selon leur système de coordonnés initial et mémorise le nouveau
             * tableau décimé à la place de l'ancien.
             */
            if (inverseTransform != null) {
                inverseTransform.transform(array, 0, array, 0, array.length/2);
            }
            scan.array = PointArray.getInstance(array);
        }
    }

    /**
     * Déclare que les données de cette polyligne ne vont plus changer. Cette
     * méthode peut réaranger les tableaux de points d'une façon plus compacte.
     *
     * @param  scan     Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *                  mais cette méthode sera plus rapide si c'est le premier maillon.
     * @param  close    <code>true</code> pour indiquer que ces polylignes représentent une
     *                  forme géométrique fermée (donc un polygone).
     * @param  compress <code>true</code> pour compresser les données,  ou <code>false</code>
     *                  pour les laisser telle qu'elles sont (ce qui signifie que les données
     *                  déjà compressées ne seront pas décompressées).
     *
     * @return La polyligne compressée (habituellement <code>scan</code> lui-même),
     *         ou <code>null</code> si la polyligne ne contenait aucune donnée.
     */
    public static Polyline freeze(Polyline scan, final boolean close, final boolean compress) {
        scan = getFirst(scan);
        /*
         * Etape 1: Si on a demandé à fermer le polygone, vérifie si le premier maillon de
         *          la chaîne ne contenait qu'une bordure.  Si c'est le cas, on déménagera
         *          cette bordure à la fin du dernier maillon.
         */
        if (close && scan!=null && scan.suffix!=null && scan.array==null) {
            Polyline last = getLast(scan);
            if (last != scan) {
                if (last.suffix != null) {
                    last.suffix = last.suffix.insertAt(last.suffix.count(), scan.suffix, false);
                } else {
                    last.suffix = scan.suffix;
                }
                scan.suffix = null;
            }
        }
        /*
         * Etape 2: Fusionne ensemble des polylignes qui peuvent l'être.
         *          Deux polylignes peuvent être fusionnées ensemble si elles
         *          ne sont séparées par aucune bordure, ou si elle sont toutes
         *          deux des bordures.
         */
        if (scan != null) {
            Polyline previous = scan;
            Polyline current  = scan;
            while ((current=current.next) != null) {
                if (previous.suffix == null) {
                    if (previous.array != null) {
                        // Déménage le tableau de points de 'previous' au début
                        // de celui de 'current' si aucune bordure ne les sépare.
                        if (current.array != null) {
                            if (MERGE_POLYLINE_DATA) {
                                current.array = current.array.insertAt(0, previous.array, false);
                                previous.array = null;
                            }
                        } else {
                            current.array = previous.array;
                            previous.array = null;
                        }
                    }
                } else {
                    if (current.array == null) {
                        // Déménage le suffix de 'previous' au début de
                        // celui de 'current' si rien ne les sépare.
                        if (current.suffix != null) {
                            current.suffix = current.suffix.insertAt(0, previous.suffix, false);
                        } else {
                            current.suffix = previous.suffix;
                        }
                        previous.suffix = null;
                    }
                }
                previous=current;
            }
        }
        /*
         * Etape 3: Gèle et compresse les tableaux de points, et
         *          élimine les éventuels tableaux devenus inutile.
         */
        Polyline root=scan;
        while (scan!=null) {
            /*
             * Comprime tous les tableaux d'un maillon de la chaîne.
             * La compression maximale ("full") ne sera toutefois pas
             * appliquée sur les "points de bordure".
             */
            for (int arrayID=FIRST_ARRAY; arrayID<=LAST_ARRAY; arrayID++) {
                final PointArray array = scan.getArray(arrayID);
                if (array != null) {
                    scan.setArray(arrayID, array.getFinal(arrayID==0 && compress));
                }
            }
            /*
             * Supprime les maillons devenus vides. Ca peut avoir pour effet
             * de changer de maillon ("root") pour le début de la chaîne.
             */
            Polyline current=scan;
            scan = scan.next;
            if (current.isEmpty()) {
                current.remove();
                if (current == root) {
                    root = scan;
                }
            }
        }
        return root;
    }

    /**
     * Copy (<var>x</var>,<var>y</var>) coordinates in the specified destination array.
     * If <code>resolution</code> is greater than 0, then points that are closer than
     * <code>resolution</code> from previous one will be skiped.
     *
     * @param  The destination array. The coordinates will be filled in {@link ArrayData#array}
     *         from index {@link ArrayData#length}. The array will be expanded if needed, and
     *         {@link ArrayData#length} will be updated with index after the <code>array</code>'s
     *         element filled with the last <var>y</var> ordinates.
     * @param  resolution The minimum distance desired between points, in this polyline's
     *         coordinate system.
     * @param  transform The transform to apply, or <code>null</code> if none.
     * @throws TransformException if a transformation failed.
     */
    public static void toArray(Polyline poly, final ArrayData dest, float resolution,
                               final MathTransform2D transform) throws TransformException
    {
        resolution *= resolution;
        poly = getFirst(poly);
        int totalLength = 0;
        GeneralPath path = null;
        for (Polyline scan=poly; scan!=null; scan=scan.next) {
            for (int i=FIRST_ARRAY; i<=LAST_ARRAY; i++) {
                final PointArray array = scan.getArray(i);
                if (array != null) {
                    final int lower = dest.length();
                    // On ne décime pas les points de bordure (i!=0).
                    array.toArray(dest, (i==0) ? resolution : 0);
                    if (transform != null) {
                        if (i==0) {
                            // Transform the main data: fast way, no curves.
                            final float[] data = dest.array();
                            transform.transform(data, lower, data, lower, (dest.length()-lower)/2);
                        } else {
                            // Transform the borders: slower, can create curves.
                            if (path == null) {
                                path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
                            }
                            path.reset();
                            dest.extract(lower, path);
                            dest.append(transform.createTransformedShape(path));
                        }
                    }
                }
            }
        }
    }

    /**
     * Retourne une représentation de cet objet sous forme
     * de chaîne de caractères.  Cette représentation sera
     * de la forme <code>"Polyline[3 of 4; 47 pts]"</code>.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer(Utilities.getShortClassName(this));
        buffer.append('[');
        int index=1;
        for (Polyline scan=previous; scan!=null; scan=scan.previous) {
            index++;
        }
        buffer.append(index);
        for (Polyline scan=next; scan!=null; scan=scan.next) {
            index++;
        }
        buffer.append(" of ");
        buffer.append(index);
        buffer.append("; ");
        buffer.append(array!=null ? array.count() : 0);
        buffer.append(" points");
        if (suffix != null) {
            buffer.append(" + ");
            buffer.append(suffix.count());
            buffer.append(" in border");
        }
        buffer.append(']');
        return buffer.toString();
    }

    /**
     * Retourne un code représentant la polyligne spécifiée.
     *
     * @param  scan Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *              mais cette méthode sera plus rapide si c'est le premier maillon.
     * @return Un code calculé à partir de quelques points de la polyligne spécifiée.
     */
    public static int hashCode(Polyline scan) {
        int code = (int)serialVersionUID;
        for (scan=getFirst(scan); scan!=null; scan=scan.next) {
            if (scan.array != null) {
                code = 37*code + scan.array.hashCode();
            }
        }
        return code;
    }

    /**
     * Indique si deux polylignes contiennent les mêmes points. Cette méthode
     * retourne aussi <code>true</code> si les deux arguments sont nuls.
     *
     * @param poly1 Première polyligne. Cet argument peut être n'importe quel maillon d'une
     *              chaîne, mais cette méthode sera plus rapide si c'est le premier maillon.
     * @param poly2 Seconde polyligne. Cet argument peut être n'importe quel maillon d'une
     *              chaîne, mais cette méthode sera plus rapide si c'est le premier maillon.
     */
    public static boolean equals(Polyline poly1, Polyline poly2) {
        poly1 = getFirst(poly1);
        poly2 = getFirst(poly2);
        while (poly1 != poly2) {
            if (poly1==null || poly2==null) {
                return false;
            }
            for (int arrayID=FIRST_ARRAY; arrayID<=LAST_ARRAY; arrayID++) {
                final PointArray array1 = poly1.getArray(arrayID);
                final PointArray array2 = poly2.getArray(arrayID);
                if (!Utilities.equals(array1, array2)) {
                    return false;
                }
            }
            poly1 = poly1.next;
            poly2 = poly2.next;
        }
        return true;
    }

    /**
     * Retourne une copie de la polyligne spécifiée. Cette méthode ne copie que les références
     * vers une version immutable des tableaux de points. Les points eux-mêmes ne sont pas
     * copiés, ce qui permet d'éviter de consommer une quantité excessive de mémoire.
     *
     * @param  scan Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *              mais cette méthode sera plus rapide si c'est le premier maillon.
     * @return Copie de la chaîne <code>scan</code>.
     */
    public static Polyline clone(Polyline scan) {
        Polyline queue=null;
        for (scan=getFirst(scan); scan!=null; scan=scan.next) {
            final Polyline toMerge = new Polyline(null);
            for (int arrayID=FIRST_ARRAY; arrayID<=LAST_ARRAY; arrayID++) {
                PointArray array = scan.getArray(arrayID);
                if (array != null) {
                    array = array.getFinal(false);
                }
                toMerge.setArray(arrayID, array);
            }
            if (!toMerge.isEmpty()) {
                queue = append(queue, toMerge);
            }
        }
        return queue;
    }




    /**
     * A set of points ({@link Point2D}) from a polyline or a polygon.
     * This set of points is returned by {@link Polygon#getPoints}.
     *
     * @version $Id: Polyline.java,v 1.8 2003/05/13 11:00:46 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    static final class Collection extends AbstractCollection {
        /**
         * Première polyligne de la chaîne de points à balayer.
         */
        private final Polyline data;

        /**
         * Transformation à appliquer sur chacun des points.
         */
        private final MathTransform2D transform;

        /**
         * Construit un ensemble de points.
         */
        public Collection(final Polyline data, final MathTransform2D transform) {
            this.data = data;
            this.transform = transform;
        }

        /**
         * Retourne le nombre de points dans cet ensemble.
         */
        public int size() {
            return getPointCount(data);
        }

        /**
         * Retourne un itérateur balayant les points de cet ensemble.
         */
        public java.util.Iterator iterator() {
            return new Iterator(data, transform);
        }
    }




    /**
     * Iterateur balayant les coordonnées d'un polyligne ou d'un polygone.
     *
     * @version $Id: Polyline.java,v 1.8 2003/05/13 11:00:46 desruisseaux Exp $
     * @author Martin Desruisseaux
     */
    static final class Iterator implements java.util.Iterator {
        /**
         * Polyligne qui sert de point de départ à cet itérateur.
         * Cette informations est utilisée par {@link #rewind}.
         */
        private final Polyline start;

        /**
         * Polyligne qui sera balayée par les prochains appels de {@link #next}.
         * Ce champs sera mis à jour au fur et à mesure que l'on passera d'une
         * polyligne à l'autre.
         */
        private Polyline current;

        /**
         * Code indiquant quel champs de {@link #current} est présentement en cours d'examen:
         *
         *    -1 pour {@link Polyline#prefix},
         *     0 pour {@link Polyline#array} et
         *    +1 pour {@link Polyline#suffix}.
         */
        private int arrayID = FIRST_ARRAY-1;;

        /**
         * Itérateur balayant les données. Cet itérateur
         * aura été obtenu d'un tableau {@link PointArray}.
         */
        private PointIterator iterator;

        /**
         * Transformation à appliquer sur les coordonnées,
         * ou <code>null</code> s'il n'y en a pas.
         */
        private final MathTransform2D transform;

        /**
         * Point utilisé temporairement pour les projections.
         */
        private final Point2D.Float point = new Point2D.Float();

        /**
         * Initialise l'itérateur de façon à démarrer
         * les balayages à partir de la polyligne spécifiée.
         *
         * @param start Polyligne (peut être nul).
         * @param transform Transformation à appliquer sur les
         *        coordonnées, ou <code>null</code> s'il n'y en a pas.
         */
        public Iterator(final Polyline start, final MathTransform2D transform) {
            this.start = current = getFirst(start);
            this.transform = (transform!=null && !transform.isIdentity()) ? transform : null;
            nextArray();
        }

        /**
         * Avance l'itérateur au prochain tableau.
         */
        private void nextArray() {
            while (current != null) {
                while (++arrayID <= LAST_ARRAY) {
                    final PointArray array = current.getArray(arrayID);
                    if (array != null) {
                        iterator = array.iterator(0);
                        if (iterator.hasNext()) {
                            return;
                        }
                    }
                }
                arrayID = Polyline.FIRST_ARRAY-1;
                current = current.next;
            }
            iterator = null;
        }

        /**
         * Indique s'il reste des données que peut retourner {@link #next}.
         */
        public boolean hasNext() {
            while (iterator != null) {
                if (iterator.hasNext()) {
                    return true;
                }
                nextArray();
            }
            return false;
        }

        /**
         * Retourne les coordonnées du point suivant.
         *
         * @return Le point suivant comme un objet {@link Point2D}.
         */
        public Object next() throws NoSuchElementException {
            if (hasNext()) {
                Point2D point = (Point2D) iterator.next();
                if (transform != null) try {
                    point = transform.transform(point, point);
                } catch (TransformException exception) {
                    // Should not happen, since {@link Polygon#setCoordinateSystem}
                    // has already successfully projected every points.
                    unexpectedException("Polyline", "next", exception);
                    return null;
                }
                return point;
            } else {
                throw new NoSuchElementException();
            }
        }

        /**
         * Retourne les coordonnées du point suivant. Contrairement à la méthode {@link #next()},
         * celle-ci retourne <code>null</code> sans lancer d'exception s'il ne reste plus de point
         * à balayer.
         *
         * @param  dest Point dans lequel mémoriser le résultat. Si cet argument
         *         est nul, un nouvel objet sera créé et retourné pour mémoriser
         *         les coordonnées.
         * @return S'il restait des coordonnées à lire, le point <code>point</code> qui avait été
         *         spécifié en argument. Si <code>point</code> était nul, un objet {@link Point2D}
         *         nouvellement créé. S'il ne restait plus de données à lire, cette méthode retourne
         *         toujours <code>null</code>.
         */
        final Point2D.Float next(Point2D.Float dest) {
            while (hasNext()) {
                if (dest != null) {
                    dest.x = iterator.nextX();
                    dest.y = iterator.nextY();
                } else {
                    dest = new Point2D.Float(iterator.nextX(), iterator.nextY());
                }
                if (transform != null) try {
                    transform.transform(dest, dest);
                } catch (TransformException exception) {
                    // Should not happen, since {@link Polygon#setCoordinateSystem}
                    // has already successfully projected every points.
                    unexpectedException("Polyline", "next", exception);
                    continue;
                }
                return dest;
            }
            return null;
        }

        /**
         * Retourne les coordonnées du prochain point dans le champs
         * (<var>x2</var>,<var>y2</var>) de la ligne spécifiée. Les
         * anciennes coordonnées (<var>x2</var>,<var>y2</var>) seront
         * préalablement copiées dans (<var>x1</var>,<var>y1</var>).
         * Si cette méthode a réussie, elle retourne <code>true</code>.
         *
         * Si elle a échouée parce qu'il ne restait plus de points disponibles, elle
         * aura tout de même copié les coordonnées (<var>x2</var>,<var>y2</var>) dans
         * (<var>x1</var>,<var>y1</var>) (ce qui aura pour effet de donner à la ligne
         * une longueur de 0) et retournera <code>false</code>.
         */
        final boolean next(final Line2D.Float line) {
            line.x1 = line.x2;
            line.y1 = line.y2;
            while (hasNext()) {
                if (transform == null) {
                    line.x2 = iterator.nextX();
                    line.y2 = iterator.nextY();
                } else try {
                    point.x = iterator.nextX();
                    point.y = iterator.nextY();
                    transform.transform(point, point);
                    line.x2 = point.x;
                    line.y2 = point.y;
                } catch (TransformException exception) {
                    // Should not happen, since {@link Polygon#setCoordinateSystem}
                    // has already successfully projected every points.
                    unexpectedException("Polyline", "next", exception);
                    continue;
                }
                return true;
            }
            return false;
        }

        /**
         * Repositionne cet itérateur à son point de départ.
         */
        final void rewind() {
            current  = start;
            arrayID  = FIRST_ARRAY-1;
            nextArray();
        }

        /**
         * Cette opération n'est pas supportée.
         *
         * @throws UnsupportedOperationException Systématiquement lancée.
         */
        public void remove() throws UnsupportedOperationException {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Méthode appelée lorsqu'une erreur inatendue est survenue.
     *
     * @param source Nom de la classe dans laquelle est survenu l'exception.
     * @param method Nom de la méthode dans laquelle est survenu l'exception.
     * @param exception L'exception survenue.
     */
    static void unexpectedException(final String classe, final String method,
                                    final TransformException exception)
    {
        Utilities.unexpectedException("org.geotools.renderer.geom", classe, method, exception);
    }
}
