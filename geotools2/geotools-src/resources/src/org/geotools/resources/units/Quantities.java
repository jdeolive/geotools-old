/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2002, Center for Computational Geography
 * (C) 2000, Institut de Recherche pour le Développement
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
 *                   THIS IS A TEMPORARY CLASS
 *
 *    This is a placeholder for future <code>Unit</code> class.
 *    This skeleton will be removed when the real classes from
 *    JSR-108: Units specification will be publicly available.
 */
package org.geotools.resources.units;

// Ressources
import java.util.MissingResourceException;


/**
 * Liste de noms de quantités qui dépendront de la langue de l'utilisateur.
 * L'usager ne devrait pas créer lui-même des instances de cette classe. Une
 * instance statique sera créée une fois pour toute lors du chargement de cette
 * classe, et les divers resources seront mises à la disposition du développeur
 * via les méthodes statiques.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class Quantities extends SymbolResources {
    /**
     * Instance statique crée une fois pour toute.
     * Tous les messages seront construits à partir
     * de cette instance.
     */
    private final static Quantities resources =
        (Quantities) getBundle("javax.units.resources.Quantities");

    /**
     * Initialise les ressources par défaut. Ces ressources ne seront pas
     * forcément dans la langue de l'utilisateur. Il s'agit plutôt de ressources
     * à utiliser par défaut si aucune n'est disponible dans la langue de
     * l'utilisateur. Ce constructeur est réservé à un usage interne et ne
     * devrait pas être appellé directement.
     */
    public Quantities() {
        super(Quantities_fr.contents);
    }

    /**
     * Initialise les ressources en
     * utilisant la liste spécifiée.
     */
    Quantities(Object[] contents) {
        super(contents);
    }

    /**
     * Retourne la valeur associée à la clée spécifiée, ou <code>key</code> s'il
     * n'y en a pas. A la différence de <code>format(String)</code>, cette méthode
     * ne lance pas d'exception si la resource n'est pas trouvée.
     */
    public static String localize(final String key) {
        if (key==null) {
            return key;
        }
        final Object res=resources.handleGetObject(key);
        return (res instanceof String) ? (String) res : key;
    }
}
