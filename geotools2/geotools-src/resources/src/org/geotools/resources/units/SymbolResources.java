/*
 * Geotools 2 - OpenSource mapping toolkit
 * (C) 2003, Geotools Project Managment Committee (PMC)
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

// Utilitaires
import java.util.Enumeration;
import java.util.ListResourceBundle;
import java.util.NoSuchElementException;
import java.util.MissingResourceException;


/**
 * Liste de ressources s'adaptant à la langue de l'utilisateur. Cette classe
 * s'apparente à la classe {@link java.util.ListResourceBundle} standard du
 * Java, excepté qu'elle est légèrement plus économe en mémoire.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class SymbolResources extends java.util.ResourceBundle {
    /**
     * Table des ressources adaptées
     * à la langue de l'utilisateur.
     */
    private final Object[] map;

    /**
     * Construit la table des ressources.
     *
     * @param contents Liste des clés et des valeurs qui y sont associées.
     *        Les objets se trouvant aux index pairs (0, 2, 4...) sont les
     *        clés, et les objets se trouvant aux index impairs sont les
     *        valeurs.
     *
     * @throws IllegalArgumentException Si une clé a été répétée deux fois.
     */
    protected SymbolResources(final Object[] contents) throws IllegalArgumentException {
        map=contents;
        for (int i=0; i<contents.length; i+=2) {
            final String key=contents[i].toString();
            for (int j=i; (j+=2)<contents.length;) {
                if (key.equals(contents[j])) {
                    throw new IllegalArgumentException("Duplicated key: "+key);
                }
            }
        }
    }

    /**
     * Renvoie un énumérateur qui balayera toutes
     * les clés que possède cette liste de ressources.
     */
    public final Enumeration getKeys() {
        return new Enumeration() {
            private int i=0;

            public boolean hasMoreElements() {
                return i<map.length;
            }

            public Object nextElement() {
                if (i<map.length) {
                    final int i=this.i;
                    this.i += 2;
                    return map[i];
                } else {
                    throw new NoSuchElementException();
                }
            }
        };
    }

    /**
     * Renvoie la ressource associée à une clé donnée. Cette méthode est définie
     * pour répondre aux exigences de la classe {@link java.util.ResourceBundle}
     * et n'a généralement pas besoin d'être appellée directement.
     *
     * @param  key Clé désignant la ressouce désirée (ne doit pas être <code>null</code>).
     * @return La ressource demandée, ou <code>null</code> si aucune ressource n'est
     *         définie pour cette clé.
     */
    protected final Object handleGetObject(final String key) {
        for (int i=0; i<map.length; i+=2) {
            if (key.equals(map[i])) {
                return map[i+1];
            }
        }
        return null;
    }
}
