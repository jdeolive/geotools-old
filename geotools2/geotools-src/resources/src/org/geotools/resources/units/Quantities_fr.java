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


/**
 * Noms de quantités en langue française. Les quantités qui n'apparaissent
 * pas dans cette ressources garderont leur nom neutre.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class Quantities_fr extends Quantities
{
    /**
     * Liste des quantités en français.
     */
    static final String[] contents = {
        "dimensionless",             "sans dimension",
        "length",                    "longueur",
        "mass",                      "masse",
        "time",                      "temps",
        "electric current",          "courant électrique",
        "thermodynamic temperature", "température thermodynamique",
        "amount of substance",       "quantité de matière",
        "luminous intensity",        "intensité lumineuse",
        "plane angle",               "angle plan",
        "solid angle",               "angle solide",
        "salinity",                  "salinité",
        "area",                      "superficie",
        "volume",                    "volume",
        "speed",                     "vitesse",
        "acceleration",              "accélération",
        "magnetic field strength",   "champ magnétique",
        "luminance",                 "luminance lumineuse",
        "frequency",                 "fréquence",
        "force",                     "force",
        "pressure",                  "pression",
        "energy",                    "énergie",
        "power",                     "puissance",
        "electric charge",           "charge électrique",
        "potential",                 "potentiel",
        "capacitance",               "capacité",
        "resistance",                "résistance",
        "conductance",               "conductance",
        "magnetic flux",             "flux magnétique",
        "magnetic flux density",     "induction magnétique",
        "inductance",                "inductance",
        "luminous flux",             "flux lumineux",
        "illuminance",               "éclairement lumineux",
        "activity",                  "activité",
        "absorbed dose",             "dose absorbée",
        "dose equivalent",           "équivalent de dose"
    };

    /**
     * Initialise les ressources françaises.
     */
    public Quantities_fr() {
        super(contents);
    }
}
