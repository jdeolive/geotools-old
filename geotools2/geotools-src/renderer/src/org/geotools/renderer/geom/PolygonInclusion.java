/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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
import java.util.Set;
import java.util.List;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

// Geotools dependencies
import org.geotools.util.ProgressListener;
import org.geotools.resources.Utilities;


/**
 * Classe privée utilisée pour identifier les lacs à l'intérieur d'une île ou d'un continent.
 * Chaque noeud contient une référence vers un objet {@link Polygon} et une liste de références
 * vers d'autres objets <code>PolygonInclusion</code> dont les polygones sont entièrement compris
 * à l'intérieur de celui de cet objet <code>PolygonInclusion</code>.
 *
 * @task TODO: Avec un peu plus de code, il serait possible de faire apparaître ces objet comme
 *             un noeud dans une composante {@link javax.swing.JTree}. Ça pourrait être très
 *             intéressant, car l'utilisateur pourrait voir quels lacs sont contenus sur un
 *             continent et quelles îles sont contenues dans un lac donnée, de la même façon
 *             qu'on explore les dossiers d'un ordinateur.
 *
 * @version $Id: PolygonInclusion.java,v 1.2 2003/05/13 11:00:46 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class PolygonInclusion {
    /**
     * Polygone associé à cet objet.
     */
    private final Polygon polygon;

    /**
     * Liste des objets <code>PolygonInclusion</code> fils. Les polygons de chacun de
     * ces fils sera entièrement compris dans le polygon {@link #polygon} de cet objet.
     */
    private List childs;

    /**
     * Construit un noeud qui enveloppera le polygone
     * spécifié. Ce noeud n'aura aucune branche pour
     * l'instant.
     */
    private PolygonInclusion(Polygon polygon) {
        this.polygon = polygon;
    }

    /**
     * Vérifie si deux noeuds sont identiques.
     * Cette méthode ne doit pas être redéfinie.
     */
    public final boolean equals(final Object other) {
        return this == other;
    }

    /**
     * Retourne un code représentant ce noeud.
     * Cette méthode ne doit pas être redéfinie.
     */
    public final int hashCode() {
        return System.identityHashCode(this);
    }

    /**
     * Ajoute à la liste des polygons-fils ({@link #childs}) tous les polygones de la liste
     * spécifiée (<code>polygons</code>) qui sont entièrement compris dans {@link #polygon}.
     * Chaque polygone ajouté à {@link #childs} sera retiré de <code>polygons</code>, de
     * sorte qu'après l'appel de cette méthode, <code>polygons</code> ne contiendra plus
     * que les polygones qui ne sont pas compris dans {@link #polygon}.
     */
    private void addChilds(final List polygons) {
        for (final Iterator it=polygons.iterator(); it.hasNext();) {
            final PolygonInclusion node = (PolygonInclusion) it.next();
            if (node!=this && polygon.contains(node.polygon))  {
                if (childs == null) {
                    childs = new LinkedList();
                }
                childs.add(node);
                it.remove();
            }
        }
        buildTree(childs, null);
    }

    /**
     * Après avoir ajouté des polygones à la liste interne, on appel {@link #addChilds}
     * de façon récursive pour chacun des polygones de {@link #childs}. On obtiendra
     * ainsi une arborescence des polygones, chaque parent contenant entièrement 0, 1
     * ou plusieurs enfants. Par exemple appellons "Continent" le polygone référé par
     * {@link #polygon}. Supposons que "Continent" contient entièrement deux autres
     * polygones, "Lac" et "Île". Le code précédent avait ajouté "Lac" et "Île" à la
     * liste {@link #childs}. Maintenant on demandera à "Lac" d'examiner cette liste. Il
     * trouvera qu'il contient entièrement "Île" et l'ajoutera à sa propre liste interne
     * après l'avoir retiré de la liste {@link #child} de "Continent".
     */
    private static void buildTree(final List childs, final ProgressListener progress) {
        if (childs != null) {
            int count = 0;
            final Set alreadyProcessed = new HashSet(childs.size() + 64);
            for (Iterator it=childs.iterator(); it.hasNext();) {
                final PolygonInclusion node = (PolygonInclusion) it.next();
                if (alreadyProcessed.add(node)) {
                    if (progress != null) {
                        progress.progress(100f * (count++ / childs.size()));
                    }
                    node.addChilds(childs);
                    it = childs.iterator();
                }
            }
        }
    }

    /**
     * Examine tous les polygones spécifiés et tente de
     * différencier les îles des lacs. Les polygones de
     * la liste spécifiée seront automatiquement classés.
     */
    static void process(final Polygon[] polygons, final ProgressListener progress) {
        if (progress != null) {
            // TODO: localize...
            progress.setDescription("Searching lakes");
            progress.started();
        }
        final List childs = new LinkedList();
        for (int i=0; i<polygons.length; i++) {
            childs.add(new PolygonInclusion(polygons[i]));
        }
        buildTree(childs, progress);
        assert setType(childs, true, polygons, 0) == polygons.length;
    }

    /**
     * Définie comme étant une île ou un lac tous les noeuds
     * apparaissant dans la liste spécifiée. Les polygones-fils
     * des noeuds seront aussi définis avec le type opposé (une
     * île si le parent est un lac, et vis-versa). Au passage,
     * cette méthode recopiera les références dans le tableau
     * spécifié en argument. Elles seront ainsi classé.
     *
     * @param childs Liste des noeuds à définir.
     * @param land <code>true</code> s'il faut les définir comme des îles,
     *        land <code>false</code> s'il faut les définir comme des lacs.
     * @param polygons Tableau dans lequel recopier les références.
     * @param index Index à partir d'où copier les références.
     * @return Index suivant celui de la dernière référence copiée.
     */
    private static int setType(final List childs, final boolean land,
                               final Polygon[] polygons, int index)
    {
        if (childs != null) {
            /*
             * Commence par traiter tous les polygones-fils. On fait ceux-ci
             * en premier afin qu'ils apparaissent au début de la liste, avant
             * les polygones parents qui les contiennent.
             */
            for (final Iterator it=childs.iterator(); it.hasNext();) {
                final PolygonInclusion node = (PolygonInclusion) it.next();
                index = setType(node.childs, !land, polygons, index);
            }
            /*
             * Procède maintenant au traitement
             * des polygones de <code>childs</code>.
             */
            final InteriorType type = land ? InteriorType.ELEVATION : InteriorType.DEPRESSION;
            for (final Iterator it=childs.iterator(); it.hasNext();) {
                final PolygonInclusion node = (PolygonInclusion) it.next();
                if (node.polygon.getInteriorType() != null) {
                    node.polygon.close(type);
                }
                polygons[index++] = node.polygon;
            }
        }
        return index;
    }

    /**
     * Retourne une chaîne de caractères contenant le polygone
     * {@link #polygon} de ce noeud ainsi que de tous les noeuds-fils.
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        toString(buffer, 0);
        return buffer.toString();
    }

    /**
     * Implémentation de la méthode {@link #toString()}.
     * Cette méthode s'appellera elle-même de façon recursive.
     */
    private void toString(StringBuffer buffer, int indentation) {
        buffer.append(Utilities.spaces(indentation));
        buffer.append(polygon);
        buffer.append('\n');
        if (childs != null) {
            for (final Iterator it=childs.iterator(); it.hasNext();) {
                ((PolygonInclusion) it.next()).toString(buffer, indentation+2);
            }
        }
    }
}

