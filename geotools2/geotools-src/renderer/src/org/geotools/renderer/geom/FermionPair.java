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


/**
 * Paire de traits de côte à fusionner ensemble. Cet objet contient deux références
 * vers deux traits de côtes {@link Polygon}, désignés {@link #i} et {@link #j}. Les
 * champs <code>[i/j].mergeEnd</code> indiquent de quelle façon il faut fusionner ces
 * segments (par exemple faut-il ajouter <var>i</var> à la fin de <var>j</var> ou
 * l'inverse?).
 * <p>
 * Pour les amateurs de physique quantique, vous pouvez voir un objet <code>FermionPair</code>
 * comme une paire de Fermions. Tout comme il ne peut pas y avoir deux Fermions au même niveau
 * avec le même spin, on ne doit pas avoir nulle part dans la liste deux pointeurs
 * <code>[i/j].path</code> identiques associés à la même valeur booléenne <code>[i/j].mergeEnd</code>.
 *
 * @version $Id: FermionPair.java,v 1.2 2003/05/13 11:00:46 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class FermionPair {
    /**
     * Pointeur vers les polylignes à fusionner. L'ordre dans laquel ces polylignes seront
     * fusionnés <u>n'est pas</u> déterminé par cet objet. La décision d'ajouter
     * <code>i.path</code> à la fin de <code>j.path</code> ou inversement est laissée aux
     * méthode utilisant ces objets.
     * <p>
     * Les champs <code>mergeEnd</code> indiquent de quelle façon les polylignes peuvent être
     * fusionnés. Il peut arriver qu'il faille inverser l'ordre des données d'un des polylignes.
     * Si un des ces champs a la valeur <code>true</code>, cela signifie que la distance
     * <code>minDistance</code> est mesurée par rapport à la fin de ce polyligne. Sinon, elle est
     * mesurée par rapport au début. Si vous fusionnez <code>j.path</code> à la fin de
     * <code>i.path</code>, alors il faut inverser l'ordre des données de <code>i.path</code>
     * si <code>i.mergeEnd</code> a la valeur <code>false</code>, et inverser les données de
     * <code>j.path</code> si <code>j.mergeEnd</code> a la valeur <code>true</code>.
     */
    final Fermion i=new Fermion(), j=new Fermion();

    /**
     * Distance au carré entre le début ou la fin de <code>j.path</code> avec le début ou la
     * fin de <code>i.path</code>. Les champs <code>[i/j].mergeEnd</code> indiquent quelles
     * extrémitées de <code>Polygon</code> sont comparées.
     */
    double minDistanceSq=Double.POSITIVE_INFINITY;

    /**
     * Indique si la paire <code>i.path</code> et <code>j.path</code> est le résultat
     * des comparaisons de toutes les combinaisons possibles de segments.
     */
    boolean allComparisonsDone;

    /**
     * Renvoie une représentation sous forme de chaîne de caractères de cet objet. Cette
     * représentation sera généralement de la forme "Polylines[23+56 pts; D=0.3 km]" ou
     * "Polygon[65 pts; D=0.2 km]". Cette information est utile à des fins de déboguage.
     */
    public String toString() {
        StringBuffer buffer = new StringBuffer(j.path==i.path ? "Polygon[" : "Polyline[");
        buffer.append(i.path.getPointCount());
        if (j.path != i.path) {
            buffer.append('+');
            buffer.append(j.path.getPointCount());
        }
        buffer.append(" pts; D=");
        buffer.append((float) (Math.sqrt(minDistanceSq)/1000));
        buffer.append(" km]");
        return buffer.toString();
    }
}
