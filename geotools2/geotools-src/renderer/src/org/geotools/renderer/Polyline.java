/*
 * Geotools - OpenSource mapping toolkit
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
package org.geotools.renderer;

// Collections
import java.util.List;
import java.util.ArrayList;
import java.util.AbstractCollection;
import java.util.NoSuchElementException;
import java.lang.UnsupportedOperationException;

// Geometry
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.geotools.renderer.array.PointArray;
import org.geotools.renderer.array.PointIterator;

// Coordinate systems
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
import org.geotools.resources.XArray;
import org.geotools.resources.Geometry;
import org.geotools.resources.Utilities;
import org.geotools.resources.Statistics;
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
 * @version $Id: Polyline.java,v 1.2 2003/01/13 22:41:32 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class Polyline implements Serializable {
    /**
     * Numéro de version pour compatibilité avec des bathymétries
     * enregistrées sous d'anciennes versions.
     */
//    private static final long serialVersionUID = 3657087955800630894L;

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
    private Polyline(final PointArray array) {
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
    public static Polyline[] getInstances(final float[] data, final int lower, final int upper) {
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
     * Retourne le nombre de points de la polyligne spécifiée
     * ainsi que de tous les polylignes qui le suivent.
     *
     * @param scan Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *             mais cette méthode sera plus rapide si c'est le premier maillon.
     */
    public static int getPointCount(Polyline scan) {
        scan = getFirst(scan);
        int count=0;
        while (scan!=null) {
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
        while (scan!=null)
        {
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
                    it=data.iterator(0);
                }
            }
            if (points[j] == null) {
                points[j] = new Point2D.Float(it.nextX(), it.nextY());
            } else {
                points[j].setLocation(it.nextX(), it.nextY());
            }
        }
        assert Utilities.equals(getFirstPoint(scan, null), points[0]);
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
        scan=getLast(scan);
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
        }
        assert !it.hasNext();
        assert Utilities.equals(getLastPoint(scan, null), points[points.length-1]);
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
        scan = getFirst(scan);
        if (lower == upper) {
            return null;
        }
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
                int count=data.count();
                if (count < lower) {
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
    private void addBorder(final float[] data, final int lower, final int upper, boolean toEnd) {
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
        if (transformation!=null) {
            final MathTransform tr = transformation.getMathTransform();
            transform = !tr.isIdentity() ? (MathTransform2D) tr : null;
            final CoordinateSystem targetCS = transformation.getTargetCS();
            if (!Utilities.equals(targetCS.getUnits(0), targetCS.getUnits(1))) {
                throw new IllegalArgumentException(Resources.format(
                                            ResourceKeys.ERROR_NON_CARTESIAN_COORDINATE_SYSTEM_$1,
                                            targetCS.getName(null)));
            }
            ellipsoid = getEllipsoid(targetCS);
        } else {
            transform = null;
            ellipsoid = null;
        }
        /*
         * Compute statistics...
         */
        final Statistics stats = new Statistics();
        Point2D          point = new Point2D.Double();
        Point2D           last = new Point2D.Double();
        for (scan=getFirst(scan); scan!=null; scan=scan.next) {
            final PointArray array = scan.array;
            if (array == null) {
                continue;
            }
            final PointIterator it=array.iterator(0);
            if (it.hasNext()) {
                last.setLocation(it.nextX(), it.nextY());
                while (it.hasNext()) {
                    point.setLocation(it.nextX(), it.nextY());
                    if (transform != null) {
                        point=transform.transform(point, point);
                    }
                    stats.add(ellipsoid!=null ? ellipsoid.orthodromicDistance(last, point)
                                              : last.distance(point));
                    final Point2D swap = last;
                    last = point;
                    point = swap;
                }
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
            if (getEllipsoid(targetCS)!=null ||
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
            final PointArray points=scan.array;
            if (points == null) {
                continue;
            }
            /*
             * Obtiens les coordonnées projetées. Si ces coordonnées représentent des
             * degrés de longitudes et latitudes, alors une projection cartographique
             * sera obligatoire afin de faire correctement les calculs de distances.
             */
            float[] array=points.toArray();
            assert (array.length & 1)==0;
            if (transform!=null) {
                transform.transform(array, 0, array, 0, array.length/2);
            }
            if (array.length >= 2)
            {
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
     * Returns the ellipsoid used by the specified coordinate system,
     * providing that the two first dimensions use an instance of
     * {@link GeographicCoordinateSystem}. Otherwise (i.e. if the
     * two first dimensions are not geographic), returns <code>null</code>.
     */
    static Ellipsoid getEllipsoid(final CoordinateSystem coordinateSystem) {
        if (coordinateSystem instanceof GeographicCoordinateSystem) {
            final HorizontalDatum datum = ((GeographicCoordinateSystem) coordinateSystem).getHorizontalDatum();
            if (datum != null) {
                final Ellipsoid ellipsoid = datum.getEllipsoid();
                if (ellipsoid!=null) {
                    return ellipsoid;
                }
            }
            return Ellipsoid.WGS84; // Should not happen with a valid coordinate system.
        }
        if (coordinateSystem instanceof CompoundCoordinateSystem) {
            // Check only head CS. Do not check tail CS!
            return getEllipsoid(((CompoundCoordinateSystem) coordinateSystem).getHeadCS());
        }
        return null;
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
                last.suffix = (last.suffix!=null) ? last.suffix.insertAt(last.suffix.count(), scan.suffix, false) : scan.suffix;
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
                        // Déménage le tableau de points de 'previous' au début de celui de 'current' si aucune bordure ne les sépare.
                        current .array = (current.array!=null) ? current.array.insertAt(0, previous.array, false) : previous.array;
                        previous.array = null;
                    }
                } else {
                    if (current.array == null) {
                        // Déménage le suffix de 'previous' au début de celui de 'current' si rien ne les sépare.
                        current .suffix = (current.suffix!=null) ? current.suffix.insertAt(0, previous.suffix, false) : previous.suffix;
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
     * Retourne une copie de toutes les coordonnées des polylignes de la chaîne.
     *
     * @param  scan Polyligne. Cet argument peut être n'importe quel maillon d'une chaîne,
     *              mais cette méthode sera plus rapide si c'est le premier maillon.
     * @param  dest Tableau où mémoriser les données. Si ce tableau a exactement la
     *              longueur nécessaire, il sera utilisé et retourné. Sinon, cet argument
     *              sera ignoré et un nouveau tableau sera créé. Cet argument peut être nul.
     * @param  n    Décimation à effectuer. La valeur 1 n'effectue aucune
     *              décimation. La valeur 2 ne retient qu'une donnée sur 2,
     *              etc.
     * @return Tableau dans lequel furent mémorisées les données.
     */
    public static float[] toArray(Polyline poly, final float[] dest, final int n) {
        poly=getFirst(poly);
        float[] data=null;
        while (true) {
            /*
             * On fera deux passages dans cette boucle: un premier passage
             * pour mesurer la longueur qu'aura le tableau, et un second
             * passage pour copier les coordonnées dans le tableau.
             */
            int totalLength=0;
            for (Polyline scan=poly; scan!=null; scan=scan.next) {
                for (int i=FIRST_ARRAY; i<=LAST_ARRAY; i++) {
                    final PointArray array=scan.getArray(i);
                    if (array != null) {
                        // On ne décime pas les points de bordure (i!=0).
                        totalLength = array.toArray(data, totalLength, (i==0) ? n : 1);
                    }
                }
            }
            /*
             * Si on ne faisait que mesurer la longueur nécessaire, vérifie maintenant
             * que le tableau 'dest' a bien la longueur désirée. Si on vient plutôt de
             * finir de remplir le tableau 'dest', sort de la boucle.
             */
            if (data == null) {
                data = dest;
                if (data==null || data.length!=totalLength) {
                    data=new float[totalLength];
                }
            } else {
                assert data.length == totalLength;
                return data;
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
        buffer.append(" pts]");
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
        int code = 0;
        for (scan=getFirst(scan); scan!=null; scan=scan.next) {
            if (scan.array != null) {
                code ^= scan.array.hashCode();
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
     * @version $Id: Polyline.java,v 1.2 2003/01/13 22:41:32 desruisseaux Exp $
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
     * @version $Id: Polyline.java,v 1.2 2003/01/13 22:41:32 desruisseaux Exp $
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
        Utilities.unexpectedException("org.geotools.renderer", classe, method, exception);
    }
}
