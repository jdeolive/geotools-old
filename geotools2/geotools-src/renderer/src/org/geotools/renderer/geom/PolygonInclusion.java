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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

// Geotools dependencies
import org.geotools.resources.Utilities;
import org.geotools.util.ProgressListener;
import org.geotools.ct.TransformException;


/**
 * Classe privée utilisée pour identifier les lacs à l'intérieur d'une île ou d'un continent.
 * Chaque noeud contient une référence vers un objet {@link Polygon} et une liste de références
 * vers d'autres objets <code>PolygonInclusion</code> dont les polygones sont entièrement compris
 * à l'intérieur de celui de cet objet <code>PolygonInclusion</code>.
 *
 * @task TODO: This class is not yet used. It should be part of <code>PolygonAssembler</code>,
 *             work, but is not yet finished neither tested.
 *
 * @version $Id: PolygonInclusion.java,v 1.3 2003/05/27 18:22:43 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class PolygonInclusion {
    /**
     * Polygone associé à cet objet.
     */
    private final Polyline polygon;

    /**
     * Liste des objets <code>PolygonInclusion</code> fils. Les polygons de chacun de
     * ces fils sera entièrement compris dans le polygon {@link #polygon} de cet objet.
     */
    private Collection childs;

    /**
     * Construit un noeud qui enveloppera le polygone spécifié.
     * Ce noeud n'aura aucune branche pour l'instant.
     */
    private PolygonInclusion(Polyline polygon) {
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
    private void addChilds(final Collection polygons) {
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
    private static void buildTree(final Collection childs, final ProgressListener progress) {
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
                    it = childs.iterator(); // Need a new iterator since collection changed.
                }
            }
        }
    }

    /**
     * Add polygons in the <code>polygons</code> array.
     *
     * @param  nodes The collection of <code>PolygonInclusion</code> to process.
     * @param  polygons The destination in which to add {@link Polygon} objects.
     *
     * @throws TransformException if a transformation was required and failed.
     *         This exception should never happen if all polygons use the same
     *         coordinate system.
     */
    private static void createPolygons(final Collection nodes, final Collection polygons)
            throws TransformException
    {
        if (nodes != null) {
            for (final Iterator it=nodes.iterator(); it.hasNext();) {
                PolygonInclusion node = (PolygonInclusion) it.next();
                if (!node.polygon.isClosed()) {
                    polygons.add(node.polygon);
                    continue;
                }
                final Polygon polygon = new Polygon(node.polygon);
                polygons.add(polygon);
                if (node.childs != null) {
                    for (final Iterator it2=node.childs.iterator(); it2.hasNext();) {
                        node = (PolygonInclusion) it.next();
                        polygon.addHole(node.polygon);
                        createPolygons(node.childs, polygons);
                    }
                }
            }    
        }
    }

    /**
     * Examine tous les polygones spécifiés et tente de différencier les îles des lacs.
     *
     * @param  The source polylines.
     * @param  progres An optional progress listener.
     * @return The polygons.
     * @throws TransformException if a transformation was required and failed.
     *         This exception should never happen if all polygons use the same
     *         coordinate system.
     */
    static Collection process(final Polyline[] polygons, final ProgressListener progress)
            throws TransformException
    {
        if (progress != null) {
            // TODO: localize...
            progress.setDescription("Searching lakes");
            progress.started();
        }
        final List nodes = new LinkedList();
        for (int i=0; i<polygons.length; i++) {
            nodes.add(new PolygonInclusion(polygons[i]));
        }
        buildTree(nodes, progress);
        final List result = new ArrayList(polygons.length);
        createPolygons(nodes, result);
        return result;
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

