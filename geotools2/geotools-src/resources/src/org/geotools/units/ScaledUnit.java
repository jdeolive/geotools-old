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
import java.util.Arrays;
import org.geotools.resources.XMath;
import org.geotools.resources.units.Units;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.rsc.ResourceKeys;


/**
 * Classe représentant des unités proportionnelles à d'autres unités.
 * Les valeurs exprimées selon les unités d'un objet <code>ScaledUnit</code>
 * peuvent être converties vers le système d'unités {@link #unit} à l'aide de
 * l'équation suivante:
 *
 * <blockquote><pre>
 * <var>x</var><sub>{@link #unit}</sub>&nbsp;=&nbsp;<var>x</var><sub><code>this</code></sub>&nbsp;*&nbsp;{@link #amount}
 * </pre></blockquote>
 *
 * Les objets <code>ScaledUnit</code> permettent de faire des conversions
 * entre différentes unités, par exemple des pieds en mètres. Cette classe
 * n'ayant pas de constructeur publique, la seule façon d'obtenir des unités
 * proportionnelles est d'utiliser les méthodes {@link #getInstance} ou
 * {@link #scale}.
 *
 * @version 1.0
 * @author Steven R. Emmerson
 * @author Bill Hibbard
 * @author Martin Desruisseaux
 */
/*public*/ 
final class ScaledUnit extends Unit {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -5387831470112872874L;
    
    /**
     * Inverse d'une petite valeur servant à éviter des erreurs d'arrondissements.
     * Cette valeur est définie arbitrairement à 2^24, soit environ 1.678E+7.
     */
    private static final double INV_EPS = 16777216;
    
    /**
     * Le facteur par lequel multiplier les mesures exprimées
     * selon ces unités pour obtenir des mesures exprimées
     * selon les unités {@link #unit}.
     */
    public final double amount;
    
    /**
     * Les unités vers lesquelles se feront les conversions.
     * Il ne peut s'agir que d'unités de bases (par exemple
     * "m" ou "s") ou dérivées (par exemple "m/s").
     */
    public final SimpleUnit unit;
    
    /**
     * Construit une unité proportionnelle à l'unité de base ou dérivée
     * spécifiée. <code>new&nbsp;Scale(0.44704,&nbsp;unit)</code> créé
     * une unité de milles/heure si <code>unit</code> représentait des
     * mètres/seconde.
     *
     * @param amount Le facteur proportionnel.
     * @param unit L'unité de base ou dérivée associée.
     * @param symbol Le symbole associé à cette unité. Ne doit pas être nul.
     * @param prefix Liste des préfix pouvant être placés devant le symbole
     *        <code>symbol</code>, ou <code>null</code> s'il n'y en a pas.
     */
    private ScaledUnit(final double amount, final SimpleUnit unit, final String symbol, final PrefixSet prefix) {
        super(symbol, prefix);
        this.amount = amount;
        this.unit   = unit;
        
        if (unit==null) {
            throw new NullPointerException(Resources.format(ResourceKeys.ERROR_NO_UNIT));
        }
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount==0) {
            throw new IllegalArgumentException(Resources.format(
                                               ResourceKeys.ERROR_NOT_DIFFERENT_THAN_ZERO_$1,
                                               new Double(amount)));
        }
    }
    
    /**
     * Crée une nouvelle unité de même dimension que l'unité spécifiée,
     * mais dont les données devront être multipliée par un facteur.
     * Les conversions d'unités se feront par l'équation suivante:
     *
     * <center>
     *     <var>x</var><sub><code>unit</code></sub> =
     *     <var>x</var><sub><code>new</code></sub> * <code>amount</code>
     * </center>
     *
     * où <var>x</var><sub><code>unit</code></sub> représente une quantité mesurée
     * selon les unités <code>unit</code> et <var>x</var><sub><code>new</code></sub>
     * représente une quantité mesurée selon les unités retournées par cette méthode.
     *
     * Les unités crées par cette méthode <code>getInstance</code> servent
     * à passer d'un système de d'unités à l'autre. Par exemple on pourrait
     * construire des unités de miles anglais avec le code suivant:
     *
     * <blockquote><pre>
     *     Unit MILE=ScaledUnit.<strong>getInstance</strong>(1609, METRE);
     * </blockquote></pre>
     *
     * Ce code signifie qu'il faudra compter 1609 mètres dans chaque mille anglais.
     *
     * @param  amount Facteur par lequel il faudra multiplier les mesures
     *         exprimées selon les nouvelles unités pour les convertir dans
     *         les unités <code>unit</code>.
     * @param  unit Unités proportionnelles aux unités à créer. Les nouvelles
     *         unités créées représenteront <code>amount</code> de ces unités.
     * @return Les unités créées, ou <code>unit</code> si le
     *         paramètre <code>amount</code> est égal à 1.
     *
     * @see BaseUnit#getInstance
     * @see DerivedUnit#getInstance
     * @see OffsetUnit#getInstance
     */
    public static Unit getInstance(final double amount, final SimpleUnit unit) {
        return getInstance(amount, unit, null, null);
    }
    
    /**
     * Crée une nouvelle unité de même dimension que l'unité spécifiée,
     * mais dont les données devront être multipliée par un facteur.
     * Les conversions d'unités se feront par l'équation suivante:
     *
     * <center>
     *     <var>x</var><sub><code>unit</code></sub> =
     *     <var>x</var><sub><code>new</code></sub> * <code>amount</code>
     * </center>
     *
     * où <var>x</var><sub><code>unit</code></sub> représente une quantité mesurée
     * selon les unités <code>unit</code> et <var>x</var><sub><code>new</code></sub>
     * représente une quantité mesurée selon les unités retournées par cette méthode.
     *
     * Les unités crées par cette méthode <code>getInstance</code> servent
     * à passer d'un système de d'unités à l'autre. Par exemple on pourrait
     * construire des unités de miles anglais avec le code suivant:
     *
     * <blockquote><pre>
     *     Unit MILE=ScaledUnit.<strong>getInstance</strong>(1609, METRE, "mile", null);
     * </blockquote></pre>
     *
     * Ce code signifie qu'il faudra compter 1609 mètres dans chaque mille anglais.
     *
     * @param  amount Facteur par lequel il faudra multiplier les mesures
     *         exprimées selon les nouvelles unités pour les convertir dans
     *         les unités <code>unit</code>.
     * @param  unit Unités proportionnelles aux unités à créer. Les nouvelles
     *         unités créées représenteront <code>amount</code> de ces unités.
     * @param  symbol Le symbole qui représentera les unités créées. Si
     *         nul, alors un symbole par défaut sera créé. Dans l'exemple
     *         précédent, ce symbole par défaut serait "\u00D71609&nbsp;m"
     * @param  prefix Liste des préfix pouvant être placés devant le symbole
     *         <code>symbol</code>, ou <code>null</code> s'il n'y en a pas.
     * @return Les unités créées, ou <code>unit</code> si le
     *         paramètre <code>amount</code> est égal à 1.
     *
     * @see BaseUnit#getInstance
     * @see DerivedUnit#getInstance
     * @see OffsetUnit#getInstance
     */
    public static Unit getInstance(double amount, final SimpleUnit unit, String symbol, final PrefixSet prefix) {
        /*
         * Si <code>amount</code> est presqu'une puissance de 10, arrondi
         * à la puissance de 10 la plus proche. Cette étape vise à réduire
         * certaines erreurs d'arrondissement.
         */
        final double power = Math.rint(XMath.log10(amount)*INV_EPS)/INV_EPS;
        if (power==Math.rint(power)) {
            amount=XMath.pow10(power);
        }
        /*
         * Retourne les unités.
         */
        if (amount==1) {
            if (unit==null) {
                throw new NullPointerException(Resources.format(ResourceKeys.ERROR_NO_UNIT));
            }
            if (symbol!=null) {
                // TODO: Que faire si le symbole spécifié
                //       n'est pas le symbole de 'unit'?
            }
            return unit;
        }
        if (symbol==null) {
            symbol = UnitFormat.DEFAULT.formatScaled(amount, unit, new StringBuffer()).toString();
            return new ScaledUnit(amount, unit, symbol, null).internIgnoreSymbol();
        } else {
            return new ScaledUnit(amount, unit, symbol, prefix).intern();
        }
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
        return getInstance(amount, unit, symbol, prefix);
    }
    
    /**
     * Retourne le nom de l'unité dans la langue de l'utilisateur.
     * Par exemple le symbole "cm" sera traduit par "centimètre"
     * dans la langue française. Si aucun nom n'est disponible
     * pour l'unité courante, retourne simplement son symbole.
     */
    public String getLocalizedName() {
        PrefixSet prefix=unit.prefix;
        String    unpref=unit.getUnprefixedSymbol();
        if (prefix!=null && symbol.endsWith(unpref)) {
            final Prefix p=prefix.getPrefix(symbol.substring(0, symbol.length()-unpref.length()));
            if (p!=null) {
                return p.getLocalizedName()+Units.localize(unpref);
            }
        }
        return super.getLocalizedName();
    }
    
    /**
     * Retourne la quantité que représente cette unité. Les quantités sont des chaînes de
     * caractères qui décrivent le paramètre physique mesuré, comme "mass" ou "speed". Si
     * aucune quantité n'est définie pour cette unité, retourne <code>null</code>.
     */
    public String getQuantityName() {
        return unit.getQuantityName();
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
            default: return getInstance(Math.pow(amount, power), /*CAST*/ (SimpleUnit) unit.pow(power));
        }
    }
    
    /**
     * Élève ces unités à une puissance fractionnaire. Cette méthode est utile entre
     * autre pour prendre la racine carré d'un nombre, ce qui revient à l'élever à la
     * puissance ½.
     *
     * @param power La puissance à laquelle élever cette unité.
     * @return Les unités résultant de l'élévation des unités
     *         <code>this</code> à la puissance <code>power</code>.
     * @throws UnitException Si cette unité ne peut pas être élevée
     *         à une puissance non-entière.
     */
    public Unit pow(final double power) throws UnitException {
        final int integer=(int) power;
        if (integer==power) {
            return pow(integer);
        }
        return getInstance(Math.pow(amount, power), /*CAST*/ (SimpleUnit) unit.pow(power));
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
     * Multiply a base unit by a scaled unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final BaseUnit that) throws UnitException {
        final SimpleUnit unitThatThis = (SimpleUnit) unit.inverseMultiply(that);
        return (unitThatThis!=unit) ? getInstance(amount, unitThatThis) : this;
    }
    
    /**
     * Multiply a derived unit by a scaled unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final DerivedUnit that) throws UnitException {
        final SimpleUnit unitThatThis = (SimpleUnit) unit.inverseMultiply(that);
        return (unitThatThis!=unit) ? getInstance(amount, unitThatThis) : this;
    }
    
    /**
     * Multiply a scaled unit by a scaled unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final ScaledUnit that) throws UnitException {
        final double newAmount = that.amount*this.amount;
        final SimpleUnit unitThatThis = (SimpleUnit) that.unit.multiply(unit);
        return getInstance(newAmount, unitThatThis);
    }
    
    /**
     * Divise cette unité par une autre unité.
     *
     * @param  that L'unité par laquelle diviser cette unité.
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
     * Divise a base unit by a scaled unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final BaseUnit that) throws UnitException {
        final SimpleUnit unitThatThis = /*CAST*/ (SimpleUnit) unit.inverseDivide(that);
        return (unitThatThis!=unit) ? getInstance(amount, unitThatThis) : this;
    }
    
    /**
     * Divide a derived unit by a scaled unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final DerivedUnit that) throws UnitException {
        final SimpleUnit unitThatThis = /*CAST*/ (SimpleUnit) unit.inverseDivide(that);
        return (unitThatThis!=unit) ? getInstance(amount, unitThatThis) : this;
    }
    
    /**
     * Divide a scaled unit by a scaled unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final ScaledUnit that) throws UnitException {
        final double newAmount = that.amount/this.amount;
        final SimpleUnit unitThatThis = /*CAST*/ (SimpleUnit) that.unit.divide(unit);
        return getInstance(newAmount, unitThatThis);
    }
    
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
    public Unit scale(final double amount) {
        return (amount==1) ?  this : getInstance(this.amount*amount, unit);
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
    public Unit shift(final double offset) {
        return (offset==0) ? this : OffsetUnit.getInstance(offset, this);
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
    public boolean canConvert(final Unit that) {
        return unit.canConvert(that);
    }
    
    /**
     * Effectue la conversion d'une mesure exprimée selon d'autres unités. Par
     * exemple <code>METRE.convert(1,&nbsp;FOOT)</code> retournera <code>0.3048</code>.
     *
     * @param value    La valeur exprimée selon les autres unités (<code>fromUnit</code>).
     * @param fromUnit Les autres unités.
     * @return         La valeur convertie selon ces unités (<code>this</code>).
     * @throws         UnitException Si les unités ne sont pas compatibles.
     */
    public double convert(final double value, final Unit fromUnit) throws UnitException {
        if (fromUnit==this) {
            return value; // sligh optimization
        }
        return unit.convert(value, fromUnit)/amount;
    }
    
    /**
     * Effectue sur-place la conversion de mesures exprimées selon d'autres
     * unités. Les valeurs converties remplaceront les anciennes valeurs.
     *
     * @param  values En entré, les valeurs exprimées selon les autres unités
     *         (<code>fromUnit</code>). En sortie, les valeurs exprimées selon ces
     *         unités (<code>this</code>).
     * @param  fromUnit Les autres unités.
     * @throws UnitException Si les unités ne sont pas compatibles. Dans ce
     *         cas, aucun élément de <code>values</code> n'aura été modifié.
     */
    public void convert(final double[] values, final Unit fromUnit) throws UnitException {
        if (!equalsIgnoreSymbol(fromUnit)) {
            unit.convert(values, fromUnit);
            for (int i=0; i<values.length; i++) {
                values[i] /= amount;
            }
        }
    }
    
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
    public void convert(final float[] values, final Unit fromUnit) throws UnitException {
        if (!equalsIgnoreSymbol(fromUnit)) {
            unit.convert(values, fromUnit);
            for (int i=0; i<values.length; i++) {
                values[i] = (float) (values[i]/amount);
            }
        }
    }
    
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
    public UnitTransform getTransform(final Unit fromUnit) throws UnitException {
        if (!equalsIgnoreSymbol(fromUnit)) {
            final UnitTransform tr=unit.getTransform(fromUnit);
            if (tr instanceof ScaledTransform) {
                return ScaledTransform.getInstance(fromUnit, this, ((ScaledTransform) tr).amount*amount);
            } else {
                return CompoundTransform.getInstance(tr, ScaledTransform.getInstance(unit, this, amount));
            }
        }  else {
            return IdentityTransform.getInstance(fromUnit, this);
        }
    }
    
    /**
     * Convertit une mesure vers d'autre unités. Par exemple
     * <code>METRE.inverseConvert(1,&nbsp;FOOT)</code> retournera
     * <code>3.2808</code>. Cette méthode est l'inverse de la méthode
     * {@link #convert(double,Unit)}.
     *
     * @param value  La valeur exprimée selon ces unités (<code>this</code>).
     * @param toUnit Les autres unités.
     * @return       La valeur convertie selon les autres unités (<code>toUnit</code>).
     * @throws       UnitException Si les unités ne sont pas compatibles.
     */
    protected double inverseConvert(final double value, final Unit toUnit) throws UnitException {
        if (toUnit==this) {
            return value; // sligh optimization
        }
        return unit.inverseConvert(value*amount, toUnit);
    }
    
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
    protected void inverseConvert(final double[] values, final Unit toUnit) throws UnitException {
        if (!equalsIgnoreSymbol(toUnit)) {
            if (unit.canConvert(toUnit)) {
                for (int i=0; i<values.length; i++) {
                    values[i] *= amount;
                }
                unit.inverseConvert(values, toUnit);
            } else {
                throw toUnit.incompatibleUnitsException(this);
            }
        }
    }
    
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
    protected void inverseConvert(final float[] values, final Unit toUnit) throws UnitException {
        if (!equalsIgnoreSymbol(toUnit)) {
            if (unit.canConvert(toUnit)) {
                for (int i=0; i<values.length; i++) {
                    values[i] = (float) (values[i]*amount);
                }
                unit.inverseConvert(values, toUnit);
            } else {
                throw toUnit.incompatibleUnitsException(this);
            }
        }
    }
    
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
    protected UnitTransform getInverseTransform(final Unit toUnit) throws UnitException {
        if (!equalsIgnoreSymbol(toUnit)) {
            final UnitTransform tr=InverseTransform.getInstance(unit, toUnit);
            if (tr instanceof ScaledTransform) {
                final double amount=((ScaledTransform) tr).amount/this.amount;
                return ScaledTransform.getInstance(this, toUnit, amount);
            }  else {
                return CompoundTransform.getInstance(ScaledTransform.getInstance(this, unit, 1/amount), tr);
            }
        } else {
            return IdentityTransform.getInstance(this, toUnit);
        }
    }
    
    /**
     * Indique si deux unités sont égales, en ignorant leurs symboles. Le
     * champs {@link #symbol} de chacune des deux unités ne sera pas pris
     * en compte.
     */
    public boolean equalsIgnoreSymbol(final Unit unit) {
        if (unit instanceof ScaledUnit) {
            final ScaledUnit that = (ScaledUnit) unit;
            return Double.doubleToLongBits(this.amount)==Double.doubleToLongBits(that.amount) &&
            this.unit.equalsIgnoreSymbol(that.unit);
        } else {
            return false;
        }
    }
    
    /**
     * Retourne un code
     * pour cette unité.
     */
    public int hashCode() {
        final long code = Double.doubleToLongBits(amount);
        return (int) code ^ (int) (code >>> 32) ^ unit.hashCode();
    }
}
