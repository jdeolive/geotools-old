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

// Divers
import org.geotools.resources.units.Units;


/**
 * Classe de base pour les unités fondamentales ({@link BaseUnit}) ou les
 * unités dérivées ({@link DerivedUnit}). Aucune autre sous-classe n'est permise. Les objets
 * <code>SimpleUnit</code> représentent donc toujours une combinaison d'unités fondamentales,
 * sans facteur multiplicatif ni constante ajoutée.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
/*public*/ 
abstract class SimpleUnit extends Unit {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -1702845175242358392L;
    
    /**
     * La quantité de cette unité. Cette quantité peut être par exemple
     * "mass" pour les kilogramme ou "speed" pour les mètres par seconde),
     * ou <code>null</code> si elle n'est pas connue.
     */
    final String quantityName;
    
    /**
     * Construit des unités représentées
     * par le ou les symboles spécifiés.
     */
    SimpleUnit(final String quantityName, final String symbol, final PrefixSet prefix) {
        super(symbol, prefix);
        this.quantityName=quantityName;
    }
    
    /**
     * Retourne la quantité que représente cette unité. Les quantités sont des chaînes de
     * caractères qui décrivent le paramètre physique mesuré, comme "mass" ou "speed". Si
     * aucune quantité n'est définie pour cette unité, retourne <code>null</code>.
     */
    public final String getQuantityName() {
        return quantityName;
    }
    
    /**
     * Retourne le nom de l'unité dans la langue de l'utilisateur.
     * Par exemple le symbole "kg" sera traduit par "kilogramme"
     * dans la langue française. Si aucun nom n'est disponible
     * pour l'unité courante, retourne simplement son symbole.
     */
    public String getLocalizedName() {
        String unpref=getUnprefixedSymbol();
        if (prefix!=null && symbol.endsWith(unpref)) {
            final Prefix p=prefix.getPrefix(symbol.substring(0, symbol.length()-unpref.length()));
            if (p!=null) {
                return p.getLocalizedName()+Units.localize(unpref);
            }
        }
        return super.getLocalizedName();
    }
    
    /**
     * Retourne le symbole {@link #symbol} sans son préfix. L'implémentation par défaut
     * retourne {@link #symbol}, ce qui est correct pour la presque totalité des unités
     * des classes {@link BaseUnit} et {@link DerivedUnit}. Dans le système SI, la seule
     * exception notable (qui justifie à elle seule l'existence de cette méthode) est le
     * kilogramme (symbole "kg"). Dans ce dernier cas, le symbole sans préfix est "g".
     */
    String getUnprefixedSymbol() {
        return symbol;
    }
    
    /**
     * Élève ces unités à une puissance entière. Contrairement à la méthode
     * {@link Unit#pow}, cette méthode ne lance jamais d'exception puisque
     * cette opération est toujours définie pour les unités fondamentales
     * ou dérivées.
     * <br><br>
     * Note: Si <em>JavaSoft</em> donne suite aux RFE 4144488 ou 4106143,
     *       alors la signature de cette méthode sera modifiée pour retourner
     *       explicitement un objet <code>SimpleUnit</code>.
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
    public abstract Unit pow(int power); // CAST
    
    final              Unit inverseMultiply    (final ScaledUnit      that) throws UnitException {return ScaledUnit.getInstance(that.amount, (SimpleUnit) that.unit.multiply(this));}
    abstract /*Simple*/Unit inverseMultiply    (final BaseUnit        that) throws UnitException; // CAST
    abstract /*Simple*/Unit inverseMultiply    (final DerivedUnit     that) throws UnitException; // CAST
    final              Unit inverseDivide      (final ScaledUnit      that) throws UnitException {return ScaledUnit.getInstance(that.amount, (SimpleUnit) that.unit.divide(this));}
    abstract /*Simple*/Unit inverseDivide      (final BaseUnit        that) throws UnitException; // CAST
    abstract /*Simple*/Unit inverseDivide      (final DerivedUnit     that) throws UnitException; // CAST
    abstract  UnitTransform getTransform       (final BaseUnit    fromUnit) throws UnitException;
    abstract  UnitTransform getTransform       (final DerivedUnit fromUnit) throws UnitException;
    abstract  UnitTransform getInverseTransform(final BaseUnit      toUnit) throws UnitException;
    abstract  UnitTransform getInverseTransform(final DerivedUnit   toUnit) throws UnitException;
    
    /**
     * Crée une nouvelle unité proportionnelle à cette unité. Par exemple
     * pour convertir en kilomètres des mesures exprimées en mètres, il
     * faut les diviser par 1000. On peut exprimer cette relation par le
     * code <code>Unit&nbsp;km=metre.scale(1000)</code>.
     *
     * @param  amount Facteur par lequel il faudra diviser les valeurs
     *         exprimées selon ces unités pour obtenir des valeurs
     *         exprimées selon les nouvelles unités.
     * @return Les nouvelles unités.
     *
     * @see #pow
     * @see #multiply
     * @see #divide
     * @see #shift
     */
    public final Unit scale(final double amount) {
        return ScaledUnit.getInstance(amount, this);
    }
    
    /**
     * Crée une nouvelle unité décalée par rapport à cette unité. Par exemple
     * pour convertir des degrés Kelvin en degrés Celsius, il faut soustraire
     * 273.15 aux degrés Kelvin. On peut exprimer cette relation par le code
     * <code>Unit&nbsp;celsius=kelvin.shift(273.15)</code>.
     *
     * @param  offset Constante à soustraire aux valeurs exprimées selon ces
     *         unités pour obtenir des valeurs exprimées selon les nouvelles
     *         unités.
     * @return Les nouvelles unités.
     *
     * @see #pow
     * @see #multiply
     * @see #divide
     * @see #scale
     */
    public final Unit shift(final double offset) {
        return OffsetUnit.getInstance(offset, this);
    }
}
