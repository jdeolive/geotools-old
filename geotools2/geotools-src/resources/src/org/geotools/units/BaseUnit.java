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
import java.io.ObjectStreamException;


/**
 * Classe représentant les unités fondamentales d'un système d'unités.
 * Le système international (SI) n'a que quelques unités fondamentales, à partir desquelles
 * sont construites toutes les autres unités. Les unités fondamentales sont du système SI sont:
 *
 * <ul>
 *     <li>Les Ampères pour le courant électrique</li>
 *     <li>Les Candelas pour l'intensité lumineuse</li>
 *     <li>Les degrés Kelvin pour la température</li>
 *     <li>Les kilogrammes pour la masse</li>
 *     <li>Les mètres pour la longueur</li>
 *     <li>Les secondes pour le temps</li>
 * </ul>
 *
 * Il existe aussi d'autres mesures qui sont sans unités, mais pour lesquelles
 * il est pratique de leur affecter des pseudo-unités fondamentales. Les radians, les moles et
 * les mesures de salinité en sont des exemples.
 *
 * @version 1.0
 * @author Steven R. Emmerson
 * @author Bill Hibbard
 * @author Martin Desruisseaux
 */
/*public*/ 
final class BaseUnit extends SimpleUnit {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -2736303035387288589L;
    
    /**
     * Construit une unité fondamentale.
     *
     * @param quantityName Nom de la quantité (exemple: "Mass").
     * @param symbol Symbole de l'unité (exemple: "kg").
     */
    private BaseUnit(final String quantityName, final String symbol, final PrefixSet prefix) {
        super(quantityName, symbol, prefix);
    }
    
    /**
     * Crée une nouvelle unité de base. Les unités de base n'existent
     * normalement qu'en nombre restreint et servent de "briques" à
     * toutes les autres unités. Avant de créer une nouvelle unité
     * avec cette méthode, vérifiez s'il ne s'agit pas en réalité
     * d'une unité dérivée.
     *
     * @param quantityName Le nom de la quantité associée à l'unité.
     *        Des exemples de noms seraient "mass", "electric current",
     *        "temperature", etc.
     *
     * @param symbol Le symbole des unités (par exemple "kg" pour la masse).
     *        Ce symbole est obligatoire et ne doit pas être <code>null</code>.
     *
     * @param prefix Liste des préfix pouvant être utilisés avec le symbole
     *        <code>symbol</code>, ou <code>null</code> s'il n'y en a pas.
     *
     * @return Une unité de base associée à la quantité <code>quantityName</code>
     *         avec le symbole <code>symbol</code> et les préfix <code>prefix</code>.
     *
     * @see DerivedUnit#getInstance
     * @see ScaledUnit#getInstance
     * @see OffsetUnit#getInstance
     */
    public static BaseUnit getInstance(final String quantityName, final String symbol, final PrefixSet prefix) {
        return (BaseUnit) new BaseUnit(quantityName, symbol, prefix).intern();
    }
    
    /**
     * Retourne le symbole {@link #symbol} sans son préfix. Cette méthode retourne habituellement
     * {@link #symbol}, ce qui est correct pour la presque totalité des unités <code>BaseUnit</code>.
     * Dans le système SI, la seule exception notable (qui justifie à elle seule l'existence de cette
     * méthode) est le kilogramme (symbole "kg").
     */
    String getUnprefixedSymbol() {
        return symbol.equals("kg") ? "g" : super.getUnprefixedSymbol();
    }
    
    /**
     * Renvoie une unité identique à celle-ci, mais
     * avec un nouveau symbole et de nouveaux préfix.
     *
     * @param  symbol Nouveau symbole représentant cette unité. Si ce
     *         paramètre est nul, un symbole par défaut sera créé.
     * @param  prefix Liste des préfix autorisés pour le symbole.
     * @return La même unité, mais avec le nouveau symbole. Peut être
     *         <code>this</code>, mais ne sera jamais <code>null</code>.
     */
    public Unit rename(final String symbol, final PrefixSet prefix) { // CAST
        return getInstance(quantityName, symbol, prefix);
    }
    
    /**
     * Élève cette unité à une puissance entière.
     *
     * @param power La puissance à laquelle élever cette unité.
     * @return Les unités résultant de l'élévation des unités
     *         <code>this</code> à la puissance <code>power</code>.
     *
     * @see #multiply
     * @see #divide
     * @see #scale
     * @see #shift
     */
    public Unit pow(final int power) {
        switch (power) {
            case 0:  return DerivedUnit.DIMENSIONLESS;
            case 1:  return this;
            default: return DerivedUnit.getInstance(new Factor[] {Factor.getFactor(this, power)});
        }
    }
    
    /**
     * Multiplie cette unité par une autre unité.
     *
     * @param  that L'unité par laquelle multiplier cette unité.
     * @return Le produit de <code>this</code> par <code>that</code>.
     * @throws UnitException Si l'unité <code>that</code> est de la
     *         classe {@link OffsetUnit} ou d'une autre classe invalide.
     *
     * @see #pow
     * @see #divide
     * @see #scale
     * @see #shift
     */
    public Unit multiply(final Unit that) throws UnitException {
        return that.inverseMultiply(this);
    }
    
    /**
     * Multiply a derived unit by a base unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final DerivedUnit that) {
        return that.multiply(new Factor[] {Factor.getFactor(this, +1)});
    }
    
    /**
     * Multiply a base unit by another base unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final BaseUnit that) throws UnitException {
        final Factor[] factors;
        if (equalsIgnoreSymbol(that)) {
            factors=new Factor[] {
                Factor.getFactor(that, 2)
            };
        } else {
            factors=new Factor[] {
                Factor.getFactor(that, 1),
                Factor.getFactor(this, 1)
            };
        }
        return DerivedUnit.getInstance(factors);
    }
    
    /**
     * Divise cette unité par une autre unité.
     *
     * @param that L'unité par laquelle diviser cette unité.
     * @return Le quotient de <code>this</code> par <code>that</code>.
     * @throws UnitException Si l'unité <code>that</code> est de la
     *         classe {@link OffsetUnit} ou d'une autre classe invalide.
     *
     * @see #pow
     * @see #multiply
     * @see #scale
     * @see #shift
     */
    public Unit divide(final Unit that) throws UnitException {
        return that.inverseDivide(this);
    }
    
    /**
     * Divide a derived unit by a base unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final DerivedUnit that) {
        return that.multiply(new Factor[] {Factor.getFactor(this, -1)});
    }
    
    /**
     * Divide a base unit by another base unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final BaseUnit that) throws UnitException {
        if (!equalsIgnoreSymbol(that)) {
            return DerivedUnit.getInstance(new Factor[] {
                Factor.getFactor(that, +1),
                Factor.getFactor(this, -1)
            });
        } else {
            return DerivedUnit.DIMENSIONLESS;
        }
    }
    
    /**
     * Indique si les unités <code>this</code> et <code>that</code> sont compatibles.
     * Si elles le sont, alors les méthodes <code>convert</code> ne lanceront jamais
     * d'exception pour ces unités.
     *
     * @param that Autre unités avec laquelle on veut
     *        vérifier si ces unités sont compatibles.
     * @return <code>true</code> Si l'on garantie que les méthodes
     *         <code>convert</code> ne lanceront pas d'exceptions.
     */
    public boolean canConvert(final Unit        that) {return that.canConvert(this);} // Do not move in superclass
           boolean canConvert(final DerivedUnit that) {return that.canConvert(this);} // Do not move in superclass
           boolean canConvert(final BaseUnit    that) {return equalsIgnoreSymbol(that);}
    
    /**
     * Effectue la conversion d'une mesure exprimée selon d'autres unités. Par
     * exemple <code>METRE.convert(1,&nbsp;FOOT)</code> retournera <code>0.3048</code>.
     *
     * @param value La valeur exprimée selon les autres unités (<code>fromUnit</code>).
     * @param fromUnit Les autres unités.
     * @return La valeur convertie selon ces unités (<code>this</code>).
     * @throws UnitException Si les unités ne sont pas compatibles.
     */
    public double convert(final double value, final Unit        fromUnit) throws UnitException {return fromUnit.inverseConvert(value, this);} // Do not move in superclass
           double convert(final double value, final DerivedUnit fromUnit) throws UnitException {return fromUnit.inverseConvert(value, this);} // Do not move in superclass
           double convert(final double value, final BaseUnit    fromUnit) throws UnitException {if (!equalsIgnoreSymbol(fromUnit)) throw incompatibleUnitsException(fromUnit); return value;}
    
    /**
     * Effectue sur-place la conversion de mesures exprimées selon d'autres
     * unités. Les valeurs converties remplaceront les anciennes valeurs.
     *
     * @param  values En entré, les valeurs exprimées selon les autres unités
     *         (<code>fromUnit</code>). En sortie, les valeurs exprimées selon
     *         ces unités (<code>this</code>).
     * @param  fromUnit Les autres unités.
     * @throws UnitException Si les unités ne sont pas compatibles. Dans ce
     *         cas, aucun élément de <code>values</code> n'aura été modifié.
     */
    public void convert(final double[] values, final Unit        fromUnit) throws UnitException {fromUnit.inverseConvert(values, this);} // Do not move in superclass
           void convert(final double[] values, final DerivedUnit fromUnit) throws UnitException {fromUnit.inverseConvert(values, this);} // Do not move in superclass
           void convert(final double[] values, final BaseUnit    fromUnit) throws UnitException {if (!equalsIgnoreSymbol(fromUnit)) throw incompatibleUnitsException(fromUnit);}
    
    /**
     * Effectue sur-place la conversion de mesures exprimées selon d'autres
     * unités. Les valeurs converties remplaceront les anciennes valeurs.
     * Notez que d'importantes erreurs d'arrondissement peuvent survenir
     * si <code>fromUnit</code> est de la classe {@link OffsetUnit}.
     *
     * @param  values En entré, les valeurs exprimées selon les autres
     *         unités (<code>fromUnit</code>). En sortie, les valeurs exprimées
     *         selon ces unités (<code>this</code>).
     * @param  fromUnit Les autres unités.
     * @throws UnitException Si les unités ne sont pas compatibles. Dans ce
     *         cas, aucun élément de <code>values</code> n'aura été modifié.
     */
    public void convert(final float[] values, final Unit        fromUnit) throws UnitException {fromUnit.inverseConvert(values, this);} // Do not move in superclass
           void convert(final float[] values, final DerivedUnit fromUnit) throws UnitException {fromUnit.inverseConvert(values, this);} // Do not move in superclass
           void convert(final float[] values, final BaseUnit    fromUnit) throws UnitException {if (!equalsIgnoreSymbol(fromUnit)) throw incompatibleUnitsException(fromUnit);}
    
    /**
     * Retourne un objet qui saura convertir selon ces unités les valeurs exprimées
     * selon d'autres unités. Cette méthode est avantageuse si on prévoie faîre
     * plusieurs conversions, car la transformation à utiliser est déterminée une
     * fois pour toute.
     *
     * @param  fromUnit Unités à partir de lesquel faire les conversions.
     * @return Une transformation des unités <code>fromUnit</code>
     *         vers les unités <code>this</code>. Cette méthode ne
     *         retourne jamais <code>null</code>.
     * @throws UnitException Si les unités ne sont pas compatibles.
     */
    public UnitTransform getTransform(final Unit        fromUnit) throws UnitException {return fromUnit.getInverseTransform(this);} // Do not move in superclass
           UnitTransform getTransform(final DerivedUnit fromUnit) throws UnitException {return fromUnit.getInverseTransform(this);} // Do not move in superclass
           UnitTransform getTransform(final BaseUnit    fromUnit) throws UnitException {return IdentityTransform.getInstance(fromUnit, this);}
    
    /**
     * Convertit une mesure vers d'autre unités. Par exemple
     * <code>METRE.inverseConvert(1,&nbsp;FOOT)</code> retournera
     * <code>3.2808</code>. Cette méthode est l'inverse de la méthode
     * {@link #convert(double,Unit)}.
     *
     * @param value La valeur exprimée selon ces unités (<code>this</code>).
     * @param toUnit Les autres unités.
     * @return La valeur convertie selon les autres unités (<code>toUnit</code>).
     * @throws UnitException Si les unités ne sont pas compatibles.
     */
    protected double inverseConvert(final double value, final Unit        toUnit) throws UnitException {return toUnit.convert(value, this);} // Do not move in superclass
              double inverseConvert(final double value, final DerivedUnit toUnit) throws UnitException {return toUnit.convert(value, this);} // Do not move in superclass
              double inverseConvert(final double value, final BaseUnit    toUnit) throws UnitException {if (!equalsIgnoreSymbol(toUnit)) throw toUnit.incompatibleUnitsException(this); return value;}
    
    /**
     * Effectue sur-place la conversion de mesures vers d'autres unités.
     * Les valeurs converties remplaceront les anciennes valeurs. Cette
     * méthode est l'inverse de la méthode {@link #convert(double[],Unit)}.
     *
     * @param  values En entré, les valeur exprimées selon ces unités
     *         (<code>this</code>). En sortie, les valeurs exprimées
     *         selon les autres unités (<code>toUnit</code>).
     * @param  toUnit Les autres unités.
     * @throws UnitException Si les unités ne sont pas compatibles. Dans ce
     *         cas, aucun élément de <code>values</code> n'aura été modifié.
     */
    protected void inverseConvert(final double[] values, final Unit        toUnit) throws UnitException {toUnit.convert(values, this);} // Do not move in superclass
              void inverseConvert(final double[] values, final DerivedUnit toUnit) throws UnitException {toUnit.convert(values, this);} // Do not move in superclass
              void inverseConvert(final double[] values, final BaseUnit    toUnit) throws UnitException {if (!equalsIgnoreSymbol(toUnit)) throw toUnit.incompatibleUnitsException(this);}
    
    /**
     * Effectue sur-place la conversion de mesures vers d'autres unités.
     * Les valeurs converties remplaceront les anciennes valeurs. Cette
     * méthode est l'inverse de la méthode {@link #convert(float[],Unit)}.
     *
     * @param  values En entré, les valeur exprimées selon ces unités
     *         (<code>this</code>). En sortie, les valeurs exprimées
     *         selon les autres unités (<code>toUnit</code>).
     * @param  toUnit Les autres unités.
     * @throws UnitException Si les unités ne sont pas compatibles. Dans ce
     *         cas, aucun élément de <code>values</code> n'aura été modifié.
     */
    protected void inverseConvert(final float[] values, final Unit        toUnit) throws UnitException {toUnit.convert(values, this);} // Do not move in superclass
              void inverseConvert(final float[] values, final DerivedUnit toUnit) throws UnitException {toUnit.convert(values, this);} // Do not move in superclass
              void inverseConvert(final float[] values, final BaseUnit    toUnit) throws UnitException {if (!equalsIgnoreSymbol(toUnit)) throw toUnit.incompatibleUnitsException(this);}
    
    /**
     * Retourne un objet qui saura convertir selon d'autres unités les
     * valeurs exprimées selon ces unités. Cette méthode est l'inverse
     * de {@link #getTransform}.
     *
     * @param  toUnit Unités vers lesquel faire les conversions.
     * @return Une transformation des unités <code>this</code>
     *         vers les unités <code>toUnit</code>. Cette méthode
     *         ne retourne jamais <code>null</code>.
     * @throws UnitException Si les unités ne sont pas compatibles.
     */
    protected UnitTransform getInverseTransform(final Unit        toUnit) throws UnitException {return toUnit.getTransform(this);} // Do not move in superclass
              UnitTransform getInverseTransform(final DerivedUnit toUnit) throws UnitException {return toUnit.getTransform(this);} // Do not move in superclass
              UnitTransform getInverseTransform(final BaseUnit    toUnit) throws UnitException {return IdentityTransform.getInstance(this, toUnit);}
    
    /**
     * Vérifie si cette unité est identique à une autre. Deux unités sont considérés
     * identiques s'ils ont le même champ {@link #quantityName}. Des symboles et prefix
     * différents sont autorisés.
     */
    final boolean equalsIgnoreSymbol(final BaseUnit unit) {
        return (unit==this) || quantityName.equals(unit.quantityName);
    }
    
    /**
     * Indique si deux unités sont égales, en ignorant leurs symboles.
     * La comparaison ne prend en compte que les quantités telles que
     * retournées par {@link #getQuantityName}.
     */
    public boolean equalsIgnoreSymbol(final Unit unit) {
        return (unit instanceof BaseUnit) && equalsIgnoreSymbol((BaseUnit) unit);
    }
    
    /**
     * Retourne un code
     * pour cette unité.
     */
    public int hashCode() {
        return quantityName.hashCode();
    }
}
