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
package org.geotools.renderer.j2d;

// J2SE dependencies
import java.util.Locale;

// Geotools dependencies
import org.geotools.pt.CoordinateFormat;
import org.geotools.pt.CoordinatePoint;
import org.geotools.cs.CoordinateSystem;
import org.geotools.ct.TransformException;
import org.geotools.cs.GeographicCoordinateSystem;


/**
 * Formateurs des coordonnées pointées par le curseur de la souris. Les instances de cette classe
 * pourront écrire les coordonnées pointées ainsi qu'une éventuelle valeurs sous cette coordonnées
 * (par exemple la température sur une image satellitaire de température).
 *
 * The {@linkplain #getCoordinateSystem output coordinate system} may have an arbitrary
 * number of dimensions (as long as a transform exists from the two-dimensional
 * {@linkplain Renderer#getCoordinateSystem renderer's coordinate system}), but
 * is usually two-dimensional.
 *
 * @version $Id: MouseCoordinateFormat.java,v 1.4 2003/01/30 23:34:40 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class MouseCoordinateFormat extends CoordinateFormat {
    /**
     * The coordinate point to format.
     */
    private CoordinatePoint point;

    /**
     * Buffer pour l'écriture des coordonnées.
     */
    private final StringBuffer buffer = new StringBuffer();

    /**
     * Indique si la méthode {@link #format} doit écrire la valeur après la coordonnée.
     * Les valeurs sont obtenues en appelant la méthode {@link Tools#formatValue}.
     * Par défaut, les valeurs (si elles sont disponibles) sont écrites.
     */
    private boolean valueVisible = true;

    /**
     * Construct a coordinate format for the default locale.
     */
    public MouseCoordinateFormat() {
        super();
    }
    

    /**
     * Construit un objet qui écrira les coordonnées pointées par le
     * curseur de la souris. Les coordonnées seront écrites selon le
     * système de coordonnées par défaut "WGS 1984".
     *
     * @param locale The locale for formatting coordinates and numbers.
     */
    public MouseCoordinateFormat(final Locale locale) {
        super(locale);
    }

    /**
     * Indique si la méthode {@link #format} doit écrire la valeur après la coordonnée.
     * Les valeurs sont obtenues en appelant la méthode {@link Tools#formatValue}.
     * Par défaut, les valeurs (si elles sont disponibles) sont écrites.
     */
    public boolean isValueVisible() {
        return valueVisible;
    }

    /**
     * Spécifie si la méthode {@link #format} doit aussi écrire la valeur après la
     * coordonnée. Si la valeur doit être écrite, elle sera déterminée en appelant
     * {@link Tools#formatValue}.
     */
    public void setValueVisible(final boolean valueVisible) {
        this.valueVisible = valueVisible;
    }

    /**
     * Retourne une chaîne de caractères représentant les coordonnées pointées par le curseur
     * de la souris.  Les coordonnées seront écrites selon le système de coordonnées spécifié
     * lors du dernier appel de {@link #setCoordinateSystem}. Si une des couches peut ajouter
     * une valeur à la coordonnée (par exemple une couche qui représente une image satellitaire
     * de température) et que l'écriture des valeurs est autorisée (voir {@link #isValueVisible}),
     * alors la valeur sera écrite après les coordonnées. Ces valeurs sont obtenues par des appels
     * à {@link Tools#formatValue}.
     *
     * @param  event Evénements contenant les coordonnées de la souris.
     * @return Chaîne de caractères représentant les coordonnées pointées
     *         par le curseur de la souris.
     */
    public String format(final GeoMouseEvent event) {
        final Renderer renderer = event.renderer;
        try {
            point = event.getCoordinate(getCoordinateSystem(), point);
        } catch (TransformException exception) {
            return "ERROR";
        }
        buffer.setLength(0);
        format(point, buffer, null);
        if (valueVisible) {
            final int length = buffer.length();
            buffer.append("  (");
            if (renderer.formatValue(event, buffer)) {
                buffer.append(')');
            } else {
                buffer.setLength(length);
            }
        }
        return buffer.toString();
    }
}
