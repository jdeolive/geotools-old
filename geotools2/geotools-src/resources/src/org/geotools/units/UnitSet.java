/*
 * Units - Temporary implementation for Geotools 2
 * Copyright (C) 1998 University Corporation for Atmospheric Research (Unidata)
 *               1998 Bill Hibbard & al. (VisAD)
 *               1999 Pêches et Océans Canada
 *               2000 Institut de Recherche pour le Développement
 *               2002 Centre for Computational Geography
 *
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Library General Public
 *    License as published by the Free Software Foundation; either
 *    version 2 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *    Library General Public License for more details (http://www.gnu.org/).
 *
 *
 *    This package is inspired from the units package of VisAD.
 *    Unidata and Visad's work is fully acknowledged here.
 *
 *                   THIS IS A TEMPORARY CLASS
 *
 *    This is a placeholder for future <code>Unit</code> class.
 *    This skeleton will be removed when the real classes from
 *    JSR-108: Units specification will be publicly available.
 */
package org.geotools.units;

// Entrés/sorties
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

// Divers
import org.geotools.resources.Utilities;


/**
 * Banque d'unités standards ou courantes. La méthode {@link #main}
 * de cette classe construit un tableau de type <code>Unit[]</code> et l'enregistre
 * en binaire dans le fichier {@link #PATHNAME}. Cette banque d'unités pourra être
 * relue par {@link Unit} lorsque des unités sont demandées.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
final class UnitSet implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -7375402196770611553L;

    /**
     * Nom et chemin du fichier dans lequel
     * enregistrer les unités sous une forme
     * binaire.
     */
    public static final String PATHNAME = "org/geotools/units/database.serialized";

    /**
     * Liste des préfix standards du système SI.
     */
    private final PrefixSet prefix;

    /**
     * Liste de plusieurs unités du système SI
     * ainsi que quelques autres unités.
     */
    private final Unit[] units;

    /**
     * Crée une liste d'unités par défaut. Cette liste contiendra
     * la plupart des unités définies dans la brochure SI.
     */
    private UnitSet() {
        prefix = PrefixSet.getPrefixSet(new Prefix[] {
            Prefix.getPrefix("yocto", "y",  1E-24),
            Prefix.getPrefix("zepto", "z",  1E-21),
            Prefix.getPrefix("atto",  "a",  1E-18),
            Prefix.getPrefix("femto", "f",  1E-15),
            Prefix.getPrefix("pico",  "p",  1E-12),
            Prefix.getPrefix("nano",  "n",  1E-09),
            Prefix.getPrefix("micro", "µ",  1E-06),
            Prefix.getPrefix("milli", "m",  1E-03),
            Prefix.getPrefix("centi", "c",  1E-02),
            Prefix.getPrefix("deci",  "d",  1E-01),
            Prefix.getPrefix("",      "",   1E+00),
            Prefix.getPrefix("deca",  "da", 1E+01),
            Prefix.getPrefix("hecto", "h",  1E+02),
            Prefix.getPrefix("kilo",  "k",  1E+03),
            Prefix.getPrefix("mega",  "M",  1E+06),
            Prefix.getPrefix("giga",  "G",  1E+09),
            Prefix.getPrefix("tera",  "T",  1E+12),
            Prefix.getPrefix("peta",  "P",  1E+15),
            Prefix.getPrefix("exa",   "E",  1E+18),
            Prefix.getPrefix("zetta", "Z",  1E+21),
            Prefix.getPrefix("yotta", "Y",  1E+24)
        });

        final PrefixSet ALL=prefix;
        final PrefixSet NONE=null;
        final BaseUnit METRE     = BaseUnit.getInstance("length",                    "m",      ALL); // mètre
        final BaseUnit KILOGRAM  = BaseUnit.getInstance("mass",                      "kg",     ALL); // kilogramme
        final BaseUnit SECOND    = BaseUnit.getInstance("time",                      "s",      ALL); // seconde
        final BaseUnit AMPERE    = BaseUnit.getInstance("electric current",          "A",      ALL); // ampère
        final BaseUnit KELVIN    = BaseUnit.getInstance("thermodynamic temperature", "K",      ALL); // kelvin
        final BaseUnit MOLE      = BaseUnit.getInstance("amount of substance",       "mol",    ALL); // mole
        final BaseUnit CANDELA   = BaseUnit.getInstance("luminous intensity",        "cd",     ALL); // candela
        final BaseUnit RADIAN    = BaseUnit.getInstance("plane angle",               "rad",   NONE); // radian
        final BaseUnit STERADIAN = BaseUnit.getInstance("solid angle",               "sr",    NONE); // stéradian
        final BaseUnit PSS78     = BaseUnit.getInstance("salinity",                  "PSS78", NONE); // salinité

        final SimpleUnit METRE2 = DerivedUnit.getInstance("area", "m²", NONE, new Factor[] {
            Factor.getFactor(METRE, +2)
        });

        final SimpleUnit METRE3 = DerivedUnit.getInstance("volume", "m³", NONE, new Factor[] {
            Factor.getFactor(METRE, +3)
        });

        final SimpleUnit METRE_PER_SECOND = DerivedUnit.getInstance("speed", "m/s", NONE, new Factor[] {
            Factor.getFactor(METRE,    +1),
            Factor.getFactor(SECOND,   -1)
        });

        final SimpleUnit METRE_PER_SECOND2 = DerivedUnit.getInstance("acceleration", "m/s²", NONE, new Factor[] {
            Factor.getFactor(METRE,  +1),
            Factor.getFactor(SECOND, -2)
        });
    
        final SimpleUnit METRE2_PER_SECOND = DerivedUnit.getInstance(null, "m²/s", NONE, new Factor[] {
            Factor.getFactor(METRE,    +2),
            Factor.getFactor(SECOND,   -1)
        });

        final SimpleUnit PASCAL_SECOND = DerivedUnit.getInstance(null, "Pa·s", NONE, new Factor[] {
            Factor.getFactor(KILOGRAM, +1),
            Factor.getFactor(METRE,    -1),
            Factor.getFactor(SECOND,   -1)
        });

        final SimpleUnit AMPERE_PER_METRE = DerivedUnit.getInstance("magnetic field strength", "A/m", NONE, new Factor[] {
            Factor.getFactor(METRE,  -1),
            Factor.getFactor(AMPERE, +1)
        });

        final SimpleUnit COULOMB_PER_KILOGRAM = DerivedUnit.getInstance(null, "C/kg", NONE, new Factor[] {
            Factor.getFactor(KILOGRAM, -1),
            Factor.getFactor(SECOND,   +1),
            Factor.getFactor(AMPERE,   +1)
        });

        final SimpleUnit CANDELA_PER_METRE2 = DerivedUnit.getInstance("luminance", "cd/m²", NONE, new Factor[] {
            Factor.getFactor(METRE,   -2),
            Factor.getFactor(CANDELA, +1)
        });

        final SimpleUnit WATT_SECOND_PER_METRE2 = DerivedUnit.getInstance(null, "W/(m²·Hz)", NONE, new Factor[] {
            Factor.getFactor(KILOGRAM, +1),
            Factor.getFactor(SECOND,   -2)
        });

        final SimpleUnit HERTZ = DerivedUnit.getInstance("frequency", "Hz", ALL, new Factor[] {
            Factor.getFactor(SECOND,   -1)
        });

        final SimpleUnit NEWTON = DerivedUnit.getInstance("force", "N", ALL, new Factor[] {
            Factor.getFactor(METRE,    +1),
            Factor.getFactor(KILOGRAM, +1),
            Factor.getFactor(SECOND,   -2)
        });

        final SimpleUnit PASCAL = DerivedUnit.getInstance("pressure", "Pa", ALL, new Factor[] {
            Factor.getFactor(KILOGRAM, +1),
            Factor.getFactor(METRE,    -1),
            Factor.getFactor(SECOND,   -2)
        });

        final SimpleUnit JOULE = DerivedUnit.getInstance("energy", "J", ALL, new Factor[] {
            Factor.getFactor(METRE,    +2),
            Factor.getFactor(KILOGRAM, +1),
            Factor.getFactor(SECOND,   -2)
        });

        final SimpleUnit WATT = DerivedUnit.getInstance("power", "W", ALL, new Factor[] {
            Factor.getFactor(METRE,    +2),
            Factor.getFactor(KILOGRAM, +1),
            Factor.getFactor(SECOND,   -3)
        });

        final SimpleUnit COULOMB = DerivedUnit.getInstance("electric charge", "C", ALL, new Factor[] {
            Factor.getFactor(SECOND,   +1),
            Factor.getFactor(AMPERE,   +1)
        });

        final SimpleUnit VOLT = DerivedUnit.getInstance("potential", "V", ALL, new Factor[] {
            Factor.getFactor(METRE,    +2),
            Factor.getFactor(KILOGRAM, +1),
            Factor.getFactor(SECOND,   -3),
            Factor.getFactor(AMPERE,   -1)
        });

        final SimpleUnit FARAD = DerivedUnit.getInstance("capacitance", "F", ALL, new Factor[] {
            Factor.getFactor(METRE ,   -2),
            Factor.getFactor(KILOGRAM, -1),
            Factor.getFactor(SECOND,   +4),
            Factor.getFactor(AMPERE,   +2),
        });

        final SimpleUnit OHM = DerivedUnit.getInstance("resistance", "\u03A9", ALL, new Factor[] {
            Factor.getFactor(METRE,    +2),
            Factor.getFactor(KILOGRAM, +1),
            Factor.getFactor(SECOND,   -3),
            Factor.getFactor(AMPERE,   -2)
        });

        final SimpleUnit SIEMMENS = DerivedUnit.getInstance("conductance", "S", ALL, new Factor[] {
            Factor.getFactor(METRE,    -2),
            Factor.getFactor(KILOGRAM, -1),
            Factor.getFactor(SECOND,   +3),
            Factor.getFactor(AMPERE,   +2)
        });

        final SimpleUnit WEBER = DerivedUnit.getInstance("magnetic flux", "Wb", ALL, new Factor[] {
            Factor.getFactor(METRE ,   +2),
            Factor.getFactor(KILOGRAM, +1),
            Factor.getFactor(SECOND,   -2),
            Factor.getFactor(AMPERE,   -1)
        });

        final SimpleUnit TESLA = DerivedUnit.getInstance("magnetic flux density", "T", ALL, new Factor[] {
            Factor.getFactor(KILOGRAM, +1),
            Factor.getFactor(SECOND,   -2),
            Factor.getFactor(AMPERE,   -1)
        });

        final SimpleUnit HENRY = DerivedUnit.getInstance("inductance", "H", ALL, new Factor[] {
            Factor.getFactor(METRE ,   +2),
            Factor.getFactor(KILOGRAM, +1),
            Factor.getFactor(SECOND,   -2),
            Factor.getFactor(AMPERE,   -2)
        });

        final SimpleUnit LUMEN = DerivedUnit.getInstance("luminous flux", "lm", NONE, new Factor[] {
            Factor.getFactor(CANDELA,  +1),
            Factor.getFactor(STERADIAN,+1)
        });

        final SimpleUnit LUX = DerivedUnit.getInstance("illuminance", "lx", NONE, new Factor[] {
            Factor.getFactor(METRE,    -2),
            Factor.getFactor(CANDELA,  +1),
            Factor.getFactor(STERADIAN,+1)
        });

        final SimpleUnit BECQUEREL = DerivedUnit.getInstance("activity", "Bq", NONE, new Factor[] {
            Factor.getFactor(SECOND,   -1)
        });

        final SimpleUnit GRAY = DerivedUnit.getInstance("absorbed dose", "Gy", NONE, new Factor[] {
            Factor.getFactor(METRE,    +2),
            Factor.getFactor(SECOND,   -2)
        });

        final SimpleUnit SIEVERT = DerivedUnit.getInstance("dose equivalent", "Sv", NONE, new Factor[] {
            Factor.getFactor(METRE,    +2),
            Factor.getFactor(SECOND,   -2)
        });

        units = new Unit[] {
            METRE,
            KILOGRAM,
            SECOND,
            AMPERE,
            KELVIN,
            MOLE,
            CANDELA,
            RADIAN,
            STERADIAN,
            PSS78,
            HERTZ,
            NEWTON,
            PASCAL,
            JOULE,
            WATT,
            COULOMB,
            VOLT,
            FARAD,
            OHM,
            SIEMMENS,
            WEBER,
            TESLA,
            HENRY,
            LUMEN,
            LUX,
            BECQUEREL,
            GRAY,
            SIEVERT,

            ScaledUnit.getInstance(60,                  SECOND,                 "min",    NONE), // minute
            ScaledUnit.getInstance(60*60,               SECOND,                 "h",      NONE), // heure
            ScaledUnit.getInstance(24*60*60,            SECOND,                 "d",      NONE), // jour
            ScaledUnit.getInstance(Math.PI/180,         RADIAN,                 "°",      NONE), // degré d'angle
            ScaledUnit.getInstance(Math.PI/(180*60),    RADIAN,                 "'",      NONE), // minute d'angle
            ScaledUnit.getInstance(Math.PI/(180*60*60), RADIAN,                 "\"",     NONE), // seconde d'angle
            ScaledUnit.getInstance(0.001,               METRE3,                 "l",       ALL), // litre
            ScaledUnit.getInstance(0.001,               METRE3,                 "L",       ALL), // litre
            ScaledUnit.getInstance(0.001,               KILOGRAM,               "g",       ALL), // gramme
            ScaledUnit.getInstance(1000,                KILOGRAM,               "t",      NONE), // tonne métrique
            ScaledUnit.getInstance(1.6021773349E-19,    JOULE,                  "eV",      ALL), // électronvolt
            ScaledUnit.getInstance(1.660540210E-27,     KILOGRAM,               "u",      NONE), // unité de masse atomique unifiée
            ScaledUnit.getInstance(1.4959787069130E+11, METRE,                  "ua",     NONE), // unité astronomique
            ScaledUnit.getInstance(0.0254,              METRE,                  "inch",   NONE), // pouce
            ScaledUnit.getInstance(0.3048,              METRE,                  "foot",   NONE), // pied
            ScaledUnit.getInstance(0.9144,              METRE,                  "yard",   NONE), // yard
            ScaledUnit.getInstance(1.8288,              METRE,                  "fathom", NONE), // brasse anglaise
            ScaledUnit.getInstance(1.624,               METRE,                  "brasse", NONE), // brasse française
            ScaledUnit.getInstance(1609.0,              METRE,                  "mile",   NONE), // mille
            ScaledUnit.getInstance(1852.0,              METRE,                  "nmile",  NONE), // mille marin
            ScaledUnit.getInstance(1852.0/3600,         METRE_PER_SECOND,       "knot",   NONE), // noeud
            ScaledUnit.getInstance(1E+2,                METRE2,                 "are",    NONE), // are
            ScaledUnit.getInstance(1E+4,                METRE2,                 "ha",     NONE), // hectare
            ScaledUnit.getInstance(1E+5,                PASCAL,                 "bar",     ALL), // bar
            ScaledUnit.getInstance(1E-10,               METRE,                  "Å",      NONE), // ångström
            ScaledUnit.getInstance(1E-28,               METRE2,                 "barn",   NONE), // barn
            ScaledUnit.getInstance(1E-7,                JOULE,                  "erg",    NONE), // erg
            ScaledUnit.getInstance(1E-5,                NEWTON,                 "dyn",    NONE), // dyne
            ScaledUnit.getInstance(0.1,                 PASCAL_SECOND,          "P",      NONE), // poise
            ScaledUnit.getInstance(1E-4,                METRE2_PER_SECOND,      "St",     NONE), // stokes
            ScaledUnit.getInstance(1E-4,                TESLA,                  "G",      NONE), // gauss
            ScaledUnit.getInstance(1000/(4*Math.PI),    AMPERE_PER_METRE,       "Oe",     NONE), // oersted
            ScaledUnit.getInstance(1E-8,                WEBER,                  "Mx",     NONE), // maxwell
            ScaledUnit.getInstance(1E+4,                CANDELA_PER_METRE2,     "sb",     NONE), // stilb
            ScaledUnit.getInstance(1E+4,                LUX,                    "ph",     NONE), // phot
            ScaledUnit.getInstance(1E-2,                METRE_PER_SECOND2,      "Gal",    NONE), // gal
            ScaledUnit.getInstance(3.7E+10,             BECQUEREL,              "Ci",     NONE), // curie
            ScaledUnit.getInstance(2.58E-4,             COULOMB_PER_KILOGRAM,   "R",      NONE), // röntgen
            ScaledUnit.getInstance(1E-2,                GRAY,                   "rd",     NONE), // rad
            ScaledUnit.getInstance(1E-2,                SIEVERT,                "rem",    NONE), // rem
            ScaledUnit.getInstance(1E-26,               WATT_SECOND_PER_METRE2, "Jy",     NONE), // jansky
            ScaledUnit.getInstance(101325.0/760,        PASCAL,                 "Torr",   NONE), // torr
            ScaledUnit.getInstance(101325.0,            PASCAL,                 "atm",    NONE), // atmosphère normale
            ScaledUnit.getInstance(0.4535,              KILOGRAM,               "pound",  NONE), // livre
            ScaledUnit.getInstance(0.4535/16,           KILOGRAM,               "onze",   NONE), // onze
            OffsetUnit.getInstance(273.15,              KELVIN,                 "°C",     NONE), // degré celcius
            OffsetUnit.getInstance(459.66999999999996, ScaledUnit.getInstance(0.5555555555555556, KELVIN), "°F", NONE)
            // NOTE: Le code exacte serait:
            //
            //       Unit FAHRENHEIT=new OffsetUnit(459.67, new ScaledUnit(5./9, KELVIN), "°F").intern();
            //
            //       Mais on réalise que ce code entraîne des erreurs d'arrondissements. Par exemple 0°C
            //       est convertie en 31.999999999999943°F au lieu de 32°F. Avec les constantes utilisées,
            //       ces erreurs d'arrondissements semblent s'annuler. Ces constantes ont été obtenues par
            //       le code ci-dessous, qui suit le même chemin que les méthodes "Unit.convert".
            //
            //       Unit FAHRENHEIT=CELSIUS.scale(5./9).shift(-32);
        };
    }

    /**
     * Saute une ligne vierge.
     */
    private static void print() {
        System.out.println();
    }

    /**
     * Envoie les nombres spécifié vers le périphérique de sortie standard.
     * Au passage, vérifie si ces nombres correspondent à ceux qui étaient
     * attendues.
     */
    private static void print(final String message, final double converted, final double expected) {
        System.out.print(message);
        System.out.print(Utilities.spaces(30-message.length()));
        System.out.print(' ');
        System.out.print(converted);
        if (converted!=expected) {
            System.err.print("  ERROR! expected ");
            System.err.println(expected);
            System.exit(1);
        }
        System.out.println();
    }

    /**
     * Envoie les unités spécifié vers le périphérique de sortie standard.
     * Au passage, vérifie si ces unités correspondent à celles qui étaient
     * attendues.
     */
    private static void print(final String message, final Unit unit, final String expected) {
        System.out.print(message);
        System.out.print(Utilities.spaces(30-message.length()));
        System.out.print(' ');
        System.out.print(unit);
        final Unit check=Unit.get(expected);
        System.out.print(Utilities.spaces(10-unit.toString().length()));
        System.out.print(' ');
        System.out.print(check);
        if (!unit.equalsIgnoreSymbol(check)) {
            System.err.print("  ERROR! expected ");
            System.err.println(expected);
            System.exit(1);
        }
        if (unit.toString().equals(check.toString()) && unit!=check) {
            System.err.println("  ERROR! intern() failed");
            System.exit(1);
        }
        System.out.println();
    }

    /**
     * Procède à la création de la banque des unités. La banque sera enregistrée en binaire dans le fichier
     * {@link #PATHNAME}. Avant d'effectuer l'enregistrement, cette méthode effectue quelques tests pour
     * vérifier que les unités fonctionnent correctement.
     *
     * @throws UnitException Si une erreur est survenue lors des test.
     * @throws IOException Si une erreur est survenue lors de l'écriture de fichier.
     */
    public static void main(final String[] args) throws UnitException, IOException {
        final UnitSet units=new UnitSet();
        /*
         * Effectue quelques tests.
         */
        if (args.length==0 || !args[0].equals("silent")) {
            final Unit KILOGRAM = Unit.get("kg");

            print("[kg].pow(2)"             , KILOGRAM.pow(2),                       "kg^2");
            print("[kg].scale(1E-3)"        , KILOGRAM.scale(1.0E-3),                "g"   );
            print("[kg].scale(1E-6)"        , KILOGRAM.scale(1.0E-6),                "mg"  );
            print("[kg].convert(5000,[g])"  , KILOGRAM.convert(5000, Unit.get("g")), 5     );
            print();

            final Unit METRE  = Unit.get("m");
            final Unit SECOND = Unit.get("s");

            print("[m].pow(2)"              , METRE.pow(2),                          "m^2");
            print("[m]*[s]"                 , METRE.multiply(SECOND),                "m*s");
            print("[m]/[s]"                 , METRE.divide  (SECOND),                "m/s");
            print("[m].scale(1000)"         , METRE.scale   (1000.0),                "km" );
            print("[m].scale(1E-6)"         , METRE.scale   (1.0E-6),                "µm" );
            print("[m].convert(4,[cm])"     , METRE.convert (4, Unit.get("cm")),     0.04 );
            print();

            final Unit METRE_PER_SECOND = Unit.get("m/s");

            print("[m/s].pow(2)"            , METRE_PER_SECOND.pow(2),               "m²/s^2");
            print("[m/s]*[m]"               , METRE_PER_SECOND.multiply(METRE),      "m^2/s" );
            print("[m]*[m/s]"               , METRE.multiply(METRE_PER_SECOND),      "m^2/s" );
            print("[m/s]/[m]"               , METRE_PER_SECOND.divide(METRE),        "s^-1"  );
            print("[m]/[m/s]"               , METRE.divide(METRE_PER_SECOND),        "s^1"   );
            print("[m/s].convert(25,[km/s])", METRE_PER_SECOND.convert(25, Unit.get("km/s")), 25000);
            print();

            final Unit JOULE  = METRE_PER_SECOND.pow(2).multiply(KILOGRAM);         print("[J]", JOULE,  "J");
            final Unit NEWTON = KILOGRAM.multiply(METRE_PER_SECOND).divide(SECOND); print("[N]", NEWTON, "N");
            final Unit KILOGRAM_PER_SECOND = DerivedUnit.getInstance(null, null, null, new Factor[] {
                Factor.getFactor((BaseUnit) KILOGRAM,      +3),
                Factor.getFactor((BaseUnit) SECOND,        -1),
                Factor.getFactor((BaseUnit) KILOGRAM,      -2),
                Factor.getFactor((BaseUnit) Unit.get("K"),  0)
            });
            print("[kg/s]", KILOGRAM_PER_SECOND, "kg*s^-1");

            final Unit POUND_PER_SECOND = ScaledUnit.getInstance(0.4535, (SimpleUnit) KILOGRAM_PER_SECOND, null, null);
            final Unit MILE_PER_HOUR    = Unit.get("mile/h");

            print();

            print("[mile/h].pow(2)",            MILE_PER_HOUR   .pow(2),                        "mile²/h²"          );
            print("[pound/s]",                 POUND_PER_SECOND,                                "pound/s"           );
            print("[mile/h]*[pound/s]",         MILE_PER_HOUR   .multiply  (POUND_PER_SECOND),  "(mile*pound)/(h*s)");
            print("[pound/s]*[mile/h]",        POUND_PER_SECOND .multiply   (MILE_PER_HOUR),    "(pound/s)*(mile/h)");
            print("[mile/h]/[pound/s]",         MILE_PER_HOUR   .divide    (POUND_PER_SECOND),  "(mile/h)/(pound/s)");
            print("[pound/s]/[mile/h]",        POUND_PER_SECOND .divide     (MILE_PER_HOUR),    "(pound/s)/(mile/h)");
            print("[mile/h].convert(1,[m/s])",  MILE_PER_HOUR   .convert(1, METRE_PER_SECOND),  2.237414543194531);
            print("[m/s].convert(1,[mile/h])", METRE_PER_SECOND .convert(1,  MILE_PER_HOUR),    0.4469444444444444);
            print();
            print("[min].convert(0.01,[Hz])",  Unit.get("min").convert(0.01, Unit.get("Hz"    )), 1.6666666666666667);
            print("[S].convert(50,[\u03A9])",  Unit.get("S"  ).convert(50,   Unit.get("\u03A9")), 0.02);
            print();
            print("[°F].convert(0,[°C])",      Unit.get("°F" ).convert(0,    Unit.get("°C"    )),   32);
            print("[°C].convert(32,[°F])",     Unit.get("°C" ).convert(32,   Unit.get("°F"    )),    0);

            print();
            UnitTransform transform=Unit.get("°C").getTransform(Unit.get("°F"));
            System.out.print(transform);
            System.out.print(" 38 ==> ");
            System.out.println(transform.convert(38));
            System.out.print(transform);
            System.out.print(" 27 ==> ");
            System.out.println(transform.convert(27));

            System.out.println();
            System.out.println("Checking exceptions:");
            try {
                METRE.convert(5, SECOND);
                System.err.println("ERROR: [s] -> [m]");
                System.exit(1);
            } catch (UnitException e) {
                System.out.println(e.getMessage());
            }
            try {
                METRE_PER_SECOND.convert(5,JOULE);
                System.err.println("ERROR: [J] -> [m/s]");
                System.exit(1);
            } catch (UnitException e) {
                System.out.println(e.getMessage());
            }
            try {
                MILE_PER_HOUR.convert(5, POUND_PER_SECOND);
                System.err.println("ERROR: [pound/s] -> [mile/h]");
                System.exit(1);
            } catch (UnitException e) {
                System.out.println(e.getMessage());
            }
            try {
                Unit.get("°C").pow(2);
                System.err.println("ERROR: [°C].pow(2)");
                System.exit(1);
            } catch (UnitException e) {
                System.out.println(e.getMessage());
            }
            try {
                Unit.get("°F").multiply(Unit.get("°C"));
                System.err.println("ERROR: [°F].multiply([°C])");
                System.exit(1);
            } catch (UnitException e) {
                System.out.println(e.getMessage());
            }
            try {
                Unit.get("°C").multiply(Unit.get("°F"));
                System.err.println("ERROR: [°C].multiply([°F])");
                System.exit(1);
            } catch (UnitException e) {
                System.out.println(e.getMessage());
            }
            try {
                Unit.get("°F").divide(Unit.get("°C"));
                System.err.println("ERROR: [°F].divide([°C])");
                System.exit(1);
            } catch (UnitException e) {
                System.out.println(e.getMessage());
            }
            try {
                Unit.get("°C").divide(Unit.get("°F"));
                System.err.println("ERROR: [°C].divide([°F])");
                System.exit(1);
            } catch (UnitException e) {
                System.out.println(e.getMessage());
            }
            System.out.println("pool.size= "+Prefix.pool.size());
            System.gc();
            System.out.println("pool.size= "+Prefix.pool.size());
            System.out.flush();
        }
        /*
         * Enregistre les unités en binaire.
         */
        if (true) {
            final ObjectOutputStream out=new ObjectOutputStream(new FileOutputStream(new File(PATHNAME)));
            out.writeObject(units.units);
            out.close();
        }
    }
}
