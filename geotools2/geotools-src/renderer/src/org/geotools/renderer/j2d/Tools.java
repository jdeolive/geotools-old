/*
 * Geotools - OpenSource mapping toolkit
 * (C) 2003, Centre for Computational Geography
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
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.awt.AWTEvent;
import javax.swing.Action;


/**
 * Actions that may be trigged by some mouse of keyboard events. Each layer
 * ({@link RenderedLayer}) can have its own tool. When a mouse event occurs,
 * the tools of the topermost layer is queried first. If it didn't {@linkplain
 * AWTEvent#consume consumed} the event, then the next layer's tools is queried,
 * etc. If no {@link RenderedLayer} consumed the event, then the {@link Renderer}'s
 * tools is queried last.
 *
 * @version $Id: Tools.java,v 1.2 2003/01/23 12:13:20 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class Tools {
    /**
     * Default constructor.
     */
    protected Tools() {
    }

    /**
     * Méthode appelée automatiquement pour construire une chaîne de caractères représentant la
     * valeur pointée par la souris. En général (mais pas obligatoirement), lorsque cette méthode
     * est appelée, le buffer <code>toAppendTo</code> contiendra déjà les coordonnées géographiques
     * de la souris. Cette méthode est appelée pour donner une chance d'ajouter d'autres
     * informations pertinentes. Par exemple des couches qui représentent une image satellitaire
     * de température peuvent ajouter à <code>toAppendTo</code> un texte du genre "12°C" (sans
     * espaces au début). L'implémentation par défaut de cette méthode retourne toujours
     * <code>false</code> sans rien faire.
     *
     * @param  event Coordonnées du curseur de la souris.
     * @param  toAppendTo Le buffer dans lequel ajouter des informations.
     * @return <code>true</code> si cette méthode a ajouté des informations dans
     *         <code>toAppendTo</code>. Dans ce cas, les couches en-dessous ne seront
     *         pas interrogées.
     */
    protected boolean getLabel(final GeoMouseEvent event, final StringBuffer toAppendTo) {
        return false;
    }

    /**
     * Retourne le texte à afficher dans une bulle lorsque la souris traîne sur la couche.
     * L'implémentation par défaut retourne toujours <code>null</code>, ce qui signifie que
     * cette couche n'a aucun texte à afficher (les autres couches seront alors interrogées).
     * Les classes dérivées peuvent redéfinir cette méthode pour retourner un texte après avoir
     * vérifié que les coordonnées de <code>event</code> correspondent bien à un point de la
     * couche.
     *
     * @param  event Coordonnées du curseur de la souris.
     * @return Le texte à afficher lorsque la souris traîne sur la couche.
     *         Ce texte peut être nul pour signifier qu'il ne faut rien écrire.
     */
    protected String getToolTipText(final GeoMouseEvent event) {
        return null;
    }

    /**
     * Returns a contextual menu for the specified mouse event. On Windows and Solaris platforms,
     * this method is invoked when the user press the right button. Subclasses should verify if
     * the mouse point on a feature of this layer. If so and if there is a popup menu to display,
     * then this method returns the actions for this menu. Otherwise, returns <code>null</code>.
     * The default implementation returns always <code>null</code>.
     *
     * @param  event The mouse event.
     * @return Actions for the popup menus, or <code>null</code> if none or if the mouse isn't
     *         over a feature of this layer. If this array is non-null but contains null elements,
     *         then the null elements will be understood as menu separator.
     */
    protected Action[] getPopupMenu(final GeoMouseEvent event) {
        return null;
    }

    /**
     * Méthode appellée chaque fois que le bouton de la souris a été cliqué sur une couche qui
     * pourrait être <code>this</code>. L'implémentation par défaut ne fait rien. Les classes
     * dérivées qui souhaite entrepredre une action doivent d'abord vérifier si les coordonnées
     * de <code>event</code> correspondent bien à un point de cette couche. Si oui, alors elles
     * doivent aussi appeler {@link GeoMouseEvent#consume} après leur action, pour que le clic
     * de la souris ne soit pas transmis aux autres couches en-dessous de celle-ci.
     */
    protected void mouseClicked(final GeoMouseEvent event) {
    }
}
