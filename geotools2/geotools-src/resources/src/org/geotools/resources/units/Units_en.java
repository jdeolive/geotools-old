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
 * Noms d'unités en langue anglaise. Les unités qui n'apparaissent
 * pas dans cette ressources ne seront pas localisées.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class Units_en extends Units
{
    /**
     * Liste des unités en anglais. Les clés sont les symboles
     * standards (sauf exception) des unités. Les valeurs sont
     * les noms en anglais de ces unités.
     */
    static final String[] contents = {
        "m",      "metre",
        "g",      "gram",
        "s",      "second",
        "A",      "ampere",
        "K",      "kelvin",
        "mol",    "mole",
        "cd",     "candela",
        "rad",    "radian",
        "sr",     "steradian",
        "Hz",     "hertz",
        "N",      "newton",
        "Pa",     "pascal",
        "J",      "joule",
        "W",      "watt",
        "C",      "coulomb",
        "V",      "volt",
        "F",      "farad",
        "\u03A9", "Ohm",
        "S",      "siemmens",
        "T",      "tesla",
        "Wb",     "weber",
        "lx",     "lux",
        "Bq",     "becquerel",
        "Gy",     "gray",
        "Sv",     "sievert",
        "H",      "henry",
        "lm",     "lumen",
        "min",    "minute",
        "h",      "hour",
        "d",      "day",
        "\u00b0", "degree of angle",
        "'",      "minute of angle",
        "\"",     "seconde of angle",
        "l",      "litre",
        "L",      "litre",
        "t",      "metric ton",
        "eV",     "electronvolt",
        "u",      "unified atomic mass unit",
        "ua",     "astronomical unit",
        "inch",   "inch",              // Symbole non-standard
        "foot",   "foot",              // Symbole non-standard
        "yard",   "yard",              // Symbole non-standard
        "fathom", "English fathom",    // Symbole non-standard
        "brasse", "French fathom",     // Symbole non-standard
        "mile",   "mile",              // Symbole non-standard
        "nmile",  "nautical mile",     // Symbole non-standard
        "knot",   "knot",              // Symbole non-standard
        "are",    "are",
        "ha",     "hectare",
        "bar",    "bar",
        "\u212B", "\u00E5ngstr\u00F6m",
        "barn",   "barn",
        "erg",    "erg",
        "dyn",    "dyne",
        "P",      "poise",
        "St",     "stokes",
        "G",      "gauss",
        "Oe",     "oersted",
        "Mx",     "maxwell",
        "sb",     "stilb",
        "ph",     "phot",
        "Gal",    "gal",
        "Ci",     "curie",
        "R",      "r\u00F6entgen",
        "rd",     "rad",
        "rem",    "rem",
        "Jy",     "jansky",
        "Torr",   "torr",
        "atm",    "standard atmosphere",
        "pound",  "pound",             // Symbole non-standard
        "onze",   "onze",              // Symbole non-standard
        "\u00b0C","Celcius degree",
        "\u00b0F","fahrenheit"         // Symbole non-standard
    };

    /**
     * Initialise les ressources anglaises.
     */
    public Units_en() {
        super(contents);
    }
}
