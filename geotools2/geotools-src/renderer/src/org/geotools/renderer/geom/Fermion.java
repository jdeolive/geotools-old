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


/**
 * Référence vers un trait de côte {@link Polygon}. Cette classe contient aussi un valeur
 * booléenne qui sera prise en compte par la méthode {@link #hashCode}. Cette valeur booléenne
 * agit comme le spin d'un électron. Deux instances de <code>Fermion</code> peuvent référer au
 * même segment {@link Polygon} s'ils n'ont pas la même valeur booléenne ("spin"). Cette classe
 * est réservée à un usage interne par {@link PolygonAssembler}.
 *
 * @see FermionPair
 *
 * @version $Id: Fermion.java,v 1.1 2003/02/04 12:30:52 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
final class Fermion {
    /**
     * Référence vers le trait de côte représenté par cet objet. Dans l'analogie
     * avec la mécanique quantique, ça serait le "niveau atomique" d'un Fermion.
     */
    Polygon path;

    /**
     * Si <code>true</code>, la fin de du trait de côte <code>path</code> devra être fusionné
     * avec un autre trait (inconnu de cet objet). Si <code>false</code>, c'est le début du trait
     * <code>path</code> qui devra être fusionné. Dans l'analogie avec la mécanique quantique,
     * c'est le "spin" d'un Fermion.
     */
    boolean mergeEnd;

    /**
     * Indique si deux clés sont identiques. Deux clés sont considérés identiques si elles
     * se réfèrent au même trait de côte {@link #path} avec la même valeur booléenne
     * {@link #mergeEnd}.
     */
    public boolean equals(final Object o) {
        if (o instanceof Fermion) {
            final Fermion k=(Fermion) o;
            return k.path==path && k.mergeEnd==mergeEnd;
        } else {
            return false;
        }
    }

    /**
     * Retourne une valeur à peu près unique pour cet objet. Cette valeur sera bâtie à
     * partir de la référence {@link #path} et de la valeur booléenne {@link #mergeEnd}.
     */
    public int hashCode() {
        final int code = System.identityHashCode(path);
        return mergeEnd ? code : ~code;
    }

    /**
     * Renvoie une représentation sous forme de chaîne de caractères de cet objet.
     * Cette représentation sera de la forme "Fermion[52 pts]".
     */
    public String toString() {
        final StringBuffer buffer = new StringBuffer("Fermion[");
        if (path != null) {
            buffer.append(path.getPointCount());
            buffer.append(" pts");
        }
        buffer.append(']');
        return buffer.toString();
    }
}
