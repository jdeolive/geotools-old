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
import java.io.ObjectStreamException;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.rsc.ResourceKeys;


/**
 * Classe représentant une unité dérivée de zero, un ou plusieurs unités fondamentales.
 * Les unités fondamentales sont définies par la classe {@link BaseUnit}. Elles représentent par
 * exemple des mesures de longueurs (m) ou de temps (s). Les objets de la classe <code>DerivedUnit</code>
 * combinent ensemble quelques unités fondamentales pour créer par exemple des unités de vitesses (m/s).
 *
 * @version 1.0
 * @author Steven R. Emmerson
 * @author Bill Hibbard
 * @author Martin Desruisseaux
 */
/*public*/ 
final class DerivedUnit extends SimpleUnit {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = -4476414709268904273L;
    
    /**
     * Unité sans dimensions. Cette
     * unité n'aura aucun symbole.
     */
    static final Unit DIMENSIONLESS=new DerivedUnit().intern();
    
    /**
     * Tableau d'unités fondamentales avec leur exposant (par exemple m²). Les différentes
     * méthodes de la classe <code>DerivedUnit</code> doivent s'assurer qu'une même unité
     * n'apparaît pas deux fois dans ce tableau, et qu'aucune unité n'a un exposant de 0.
     */
    private final Factor[] factors;
    
    /**
     * Construit une unité sans dimension.
     */
    private DerivedUnit()
    {this("dimensionless", "", null, new Factor[0]);}
    
    /**
     * Construit une unité dérivée avec le tableau
     * d'unités fondamentales spécifié ainsi qu'un
     * certain symbole.
     *
     * @param factors Tableau d'unités fondamentales avec leurs
     *        exposants. Ce tableau sera supposé déjà simplifié,
     *        c'est-à-dire que la même unité fondamentale n'y
     *        apparait pas deux fois et aucun exposant n'est 0.
     */
    private DerivedUnit(final Factor[] factors) {
        super(null, UnitFormat.DEFAULT.format(factors, new StringBuffer()).toString(), null);
        this.factors=factors;
    }
    
    /**
     * Construit une unité dérivée avec le tableau
     * d'unités fondamentales spécifié ainsi qu'un
     * certain symbole.
     *
     * @param quantityName Nom de la quantité (exemple: "Speed").
     * @param symbol Symbole de cette unité dérivée. Ce symbole
     *        ne doit pas être nul.
     * @param prefix Liste des préfix qui peuvent être placés devant le symbole
     *        <code>symbol</code>, ou <code>null</code> s'il n'y en a pas. Cette
     *        liste sera prise en compte par la méthode {@link #scale}.
     * @param factors Tableau d'unités fondamentales avec leurs
     *        exposants. Ce tableau sera supposé déjà simplifié,
     *        c'est-à-dire que la même unité fondamentale n'y
     *        apparait pas deux fois et aucun exposant n'est 0.
     */
    private DerivedUnit(final String quantityName, final String symbol, final PrefixSet prefix, final Factor[] factors) {
        super(quantityName, symbol, prefix);
        this.factors=factors;
    }
    
    /**
     * Construit une unité dérivée avec le tableau d'unités fondamentales spécifié. Un symbole par défaut
     * sera attribué. Ce symbole ne sera pas nécessairement le plus approprié. Par exemple le symbole
     * "kg*m²/s²" pourrait être créé à la place de "J" pour les unités d'énergie.
     */
    public static SimpleUnit getInstance(final Factor[] factors) {
        return getInstance(null, null, null, factors);
    }
    
    /**
     * Crée une nouvelle unité dérivée de une ou plusieurs unités de bases.
     * Par exemple si <code>SECOND</code> est une unité de base mesurant
     * le temps en secondes et <code>METRE</code> une unité de base pour les
     * distances, alors on pourrait créer une unité de vitesse avec le code
     * suivant:
     *
     * <blockquote><pre>
     * &nbsp;Unit METRE_PER_SECOND=DerivedUnit.<strong>getInstance</strong>("speed", "m/s", null, new Factor[]
     * &nbsp;{
     * &nbsp;    Factor.getFactor(METRE,  +1),
     * &nbsp;    Factor.getFactor(SECOND, -1)
     * &nbsp;});
     * </pre></blockquote>
     *
     * @param  quantityName Nom de la quantité (exemple: "Speed").
     * @param  symbol Le symbole qui représentera cette unité dérivée
     *         (par exemple "J" pour les joules). Si nul, alors un symbole
     *         par défaut sera créé. Par exemple le symbole "kg*m²/s²"
     *         pourrait être créé à la place de "J" pour les unités d'énergie.
     * @param  prefix Liste des préfix qui peuvent être placés devant le symbole
     *         <code>symbol</code>, ou <code>null</code> s'il n'y en a pas. Cette
     *         liste sera prise en compte par la méthode {@link #scale}.
     * @param  factors Liste des unités de bases ainsi que de
     *         leurs exposants qui composeront l'unité dérivées.
     *         Les éléments nuls ainsi que ceux qui ont un exposant
     *         de 0 seront ignorés.
     * @return Une nouvelle unité dérivées. S'il existait déjà
     *         une unité dérivée qui répondait aux spécifications,
     *         celle-ci sera retournée. Il est possible que cette
     *         méthode retourne un objet {@link BaseUnit} au lieu
     *         d'un objet {@link DerivedUnit}, si une telle
     *         simplification était possible.
     *
     * @see BaseUnit#getInstance
     * @see ScaledUnit#getInstance
     * @see OffsetUnit#getInstance
     */
    public static SimpleUnit getInstance(final String quantityName, final String symbol, final PrefixSet prefix, Factor[] factors) {
        /*
         * Construit un tableau de facteurs dans lequel les doublons auront été fusionnés.
         * Le tableau retourné sera toujours une copie du tableau original, de sorte que
         * son contenu ne sera pas affecté par d'éventuels changements du tableau original.
         */
        Factor[] oldFactors=factors;
        factors=new Factor[oldFactors.length];
        System.arraycopy(oldFactors, 0, factors, 0, factors.length);
        for (int i=0; i<factors.length; i++) {
            Factor fi=factors[i];
            if (fi!=null) {
                final BaseUnit ui=fi.baseUnit;
                for (int j=i+1; j<factors.length; j++) {
                    final Factor fj=factors[j];
                    if (fj!=null && ui.equalsIgnoreSymbol(fj.baseUnit)) {
                        factors[i]=fi=Factor.getFactor(ui, fi.power+fj.power);
                        factors[j]=null;
                    }
                }
            }
        }
        int length=0;
        for (int i=0; i<factors.length; i++) {
            final Factor fi=factors[i];
            if (fi!=null && fi.power!=0) {
                length++;
            }
        }
        if (factors.length!=length) {
            oldFactors=factors;
            factors=new Factor[length];
            for (int i=oldFactors.length; --i>=0;) {
                final Factor fi=oldFactors[i];
                if (fi!=null && fi.power!=0) {
                    factors[--length]=fi;
                }
            }
        }
        /*
         * Maintenant que le tableau <code>factors</code> a
         * été simplifié, construit des unités avec ce tableau.
         */
        switch (factors.length) {
            case 0: return (SimpleUnit) DIMENSIONLESS;
            case 1: if (factors[0].power==1) return factors[0].baseUnit;
        }
        if (symbol!=null) {
            return (SimpleUnit) new DerivedUnit(quantityName, symbol, prefix, factors).intern();
        } else {
            return (SimpleUnit) new DerivedUnit(factors).internIgnoreSymbol();
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
    public Unit rename(final String symbol, final PrefixSet prefix) {// CAST
        return getInstance(quantityName, symbol, prefix, factors);
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
            case 0:  return DIMENSIONLESS;
            case 1:  return this;
            default: {
                final Factor[] newFactors=new Factor[factors.length];
                for (int i=0; i<newFactors.length; i++) {
                    newFactors[i] = Factor.getFactor(factors[i].baseUnit, factors[i].power*power);
                }
                return getInstance(newFactors);
            }
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
        if (integer==power) return pow(integer);
        final Factor[] newFactors=new Factor[factors.length];
        for (int i=0; i<newFactors.length; i++) {
            final float floatPower=(float) (factors[i].power*power); // Round result
            final int integerPower=(int) floatPower;
            if (integerPower==floatPower) {
                newFactors[i]=Factor.getFactor(factors[i].baseUnit, integerPower);
            } else {
                throw new UnitException(Resources.format(
                                        ResourceKeys.ERROR_BAD_UNIT_POWER_$2,
                                        new Double(power), this), this, null);
            }
        }
        return getInstance(newFactors);
    }
    
    /**
     * Multiplie cette unité par une autre unité.
     *
     * @param that L'unité par laquelle multiplier cette unité.
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
     * Multiply a base unit by a derived unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final BaseUnit that) throws UnitException {
        return multiply(new Factor[] {Factor.getFactor(that, 1)}, factors, +1);
    }
    
    /**
     * Multiply a derived unit by another derived unit
     * (<code>that</code>*<code>this</code>).
     */
    Unit inverseMultiply(final DerivedUnit that) throws UnitException {
        return that.multiply(factors);
    }
    
    /**
     * Retourne une unité qui résulte de la multiplication de
     * <code>this</code> par les facteurs <code>f</code>.
     */
    final SimpleUnit multiply(final Factor[] f) {
        return multiply(factors, f, +1);
    }
    
    /**
     * Retourne une unité qui résulte de la multiplication des
     * facteurs <code>f1</code> par les facteurs <code>f2</code>.
     */
    private static SimpleUnit multiply(final Factor[] f1, final Factor[] f2, final int power2) {
        final Factor[] factors=new Factor[f1.length + f2.length];
        System.arraycopy(f1, 0, factors, 0,         f1.length);
        System.arraycopy(f2, 0, factors, f1.length, f2.length);
        switch (power2) {
            case +1: break;
            case -1: for (int i=f1.length; i<factors.length; i++) factors[i]=factors[i].inverse(); break;
            default: throw new IllegalArgumentException(String.valueOf(power2));
        }
        return getInstance(factors);
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
     * Divide a base unit by a derived unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final BaseUnit that) throws UnitException {
        return multiply(new Factor[] {Factor.getFactor(that, 1)}, factors, -1);
    }
    
    /**
     * Divide a derived unit by another derived unit
     * (<code>that</code>/<code>this</code>).
     */
    Unit inverseDivide(final DerivedUnit that) throws UnitException {
        return multiply(that.factors, factors, -1);
    }
    
    /**
     * Indicate whether or not this unit has the same dimensionality
     * or the reciprocal dimensionality as a base unit. Is is required
     * that <code>this.compareDimensionality(that)</code> give the same
     * result as <code>that.compareDimensionality(this)</code>.
     *
     * @param that The base unit.
     * @return <code>+1</code> if both units have the same dimensionality.<br>
     *         <code>-1</code> if the unit dimensionalities are reciprocals of
     *                         each other (for example <i>length</i>/<i>time</i>
     *                         and <i>time</i>/<i>length</i>).
     *         <code> 0</code> if the unit dimensionalities are neither the same
     *                         or reciprocals each other.
     */
    private int compareDimensionality(final BaseUnit that) {
        if (factors.length==1) {
            final Factor factor=factors[0];
            if (factor.baseUnit.equalsIgnoreSymbol(that)) {
                switch (factor.power) {
                    case -1: return -1;
                    case +1: return +1;
                }
            }
        }
        return 0;
    }
    
    /**
     * Indicate whether or not this unit has the same dimensionality
     * or the reciprocal dimensionality as a derived unit. Is is required
     * that <code>this.compareDimensionality(that)</code> give the same
     * result as <code>that.compareDimensionality(this)</code>.
     *
     * @param that The derived unit.
     * @return <code>+1</code> if both units have the same dimensionality.<br>
     *         <code>-1</code> if the unit dimensionalities are reciprocals of
     *                         each other (for example <i>length</i>/<i>time</i>
     *                         and <i>time</i>/<i>length</i>).
     *         <code> 0</code> if the unit dimensionalities are neither the same
     *                         or reciprocals each other.
     */
    private int compareDimensionality(final DerivedUnit that) {
        int result=0;
        int count=that.factors.length;
        final Factor[] cmpFactors=new Factor[count];
        System.arraycopy(that.factors, 0, cmpFactors, 0, count);
  loop: for (int i=0; i<factors.length; i++) {
            final Factor factor=factors[i];
            for (int j=0; j<cmpFactors.length; j++) {
                final int cmp=factor.compareDimensionality(cmpFactors[j]);
                if (cmp!=0) {
                    if (result!=cmp) {
                        if (result!=0) {
                            return 0;
                        }
                        result=cmp;
                    }
                    count--;
                    cmpFactors[j]=null;
                    continue loop;
                }
            }
            return 0;
        }
        if (count !=0) return 0; // if the two units don't have the same number of base units
        if (result==0) return 1; // if we compared two dimensionless units (the for loop has not been executed)
        return result;
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
           boolean canConvert(final BaseUnit    that) {return compareDimensionality(that)!=0;}
           boolean canConvert(final DerivedUnit that) {return compareDimensionality(that)!=0;}
    
    /**
     * Effectue la conversion d'une mesure exprimée selon d'autres unités. Par
     * exemple <code>METRE_PER_SECOND.convert(1,&nbsp;KILOMETRE_PER_HOUR)</code>
     * retournera <code>0.2778</code>.
     *
     * @param value La valeur exprimée selon les autres unités (<code>fromUnit</code>).
     * @param fromUnit Les autres unités.
     * @return La valeur convertie selon ces unités (<code>this</code>).
     * @throws UnitException Si les unités ne sont pas compatibles.
     */
    public double convert(final double value, final Unit fromUnit) throws UnitException {
        return fromUnit.inverseConvert(value, this); // Do not move in superclass
    }
    
    double convert(final double value, final BaseUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: return 1/value;
            case +1: return value;
            default: throw incompatibleUnitsException(fromUnit);
        }
    }
    
    double convert(final double value, final DerivedUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: return 1/value;
            case +1: return value;
            default: throw incompatibleUnitsException(fromUnit);
        }
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
        fromUnit.inverseConvert(values, this); // Do not move in superclass
    }
    
    void convert(final double[] values, final BaseUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw incompatibleUnitsException(fromUnit);
        }
    }
    
    void convert(final double[] values, final DerivedUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw incompatibleUnitsException(fromUnit);
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
        fromUnit.inverseConvert(values, this); // Do not move in superclass
    }
    
    void convert(final float[] values, final BaseUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw incompatibleUnitsException(fromUnit);
        }
    }
    
    void convert(final float[] values, final DerivedUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw incompatibleUnitsException(fromUnit);
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
        return fromUnit.getInverseTransform(this); // Do not move in superclass
    }
    
    UnitTransform getTransform(final BaseUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: return InverseTransform   .getInstance(fromUnit, this);
            case +1: return IdentityTransform  .getInstance(fromUnit, this);
            default: throw  this.incompatibleUnitsException(fromUnit);
        }
    }
    
    UnitTransform getTransform(final DerivedUnit fromUnit) throws UnitException {
        switch (compareDimensionality(fromUnit)) {
            case -1: return InverseTransform   .getInstance(fromUnit, this);
            case +1: return IdentityTransform  .getInstance(fromUnit, this);
            default: throw  this.incompatibleUnitsException(fromUnit);
        }
    }
    
    /**
     * Convertit une mesure vers d'autre unités. Par exemple
     * <code>METRE_PER_SECOND.inverseConvert(1,&nbsp;KILOMETRE_PER_HOUR)</code>
     * retournera <code>3.6</code>. Cette méthode est l'inverse de la méthode
     * {@link #convert(double,Unit)}.
     *
     * @param  value La valeur exprimée selon ces unités (<code>this</code>).
     * @param  toUnit Les autres unités.
     * @return La valeur convertie selon les autres unités (<code>toUnit</code>).
     * @throws UnitException Si les unités ne sont pas compatibles.
     */
    protected double inverseConvert(final double value, final Unit toUnit) throws UnitException {
        return toUnit.convert(value, this); // Do not move in superclass
    }
    
    double inverseConvert(final double value, final BaseUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: return 1/value;
            case +1: return value;
            default: throw toUnit.incompatibleUnitsException(this);
        }
    }
    
    double inverseConvert(final double value, final DerivedUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: return 1/value;
            case +1: return value;
            default: throw toUnit.incompatibleUnitsException(this);
        }
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
        toUnit.convert(values, this); // Do not move in superclass
    }
    
    void inverseConvert(final double[] values, final BaseUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw toUnit.incompatibleUnitsException(this);
        }
    }
    
    void inverseConvert(final double[] values, final DerivedUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw toUnit.incompatibleUnitsException(this);
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
        toUnit.convert(values, this); // Do not move in superclass
    }
    
    void inverseConvert(final float[] values, final BaseUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw toUnit.incompatibleUnitsException(this);
        }
    }
    
    void inverseConvert(final float[] values, final DerivedUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: for (int i=0; i<values.length; i++) values[i]=1/values[i];
            case +1: break;
            default: throw toUnit.incompatibleUnitsException(this);
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
        return toUnit.getTransform(this); // Do not move in superclass
    }
    
    UnitTransform getInverseTransform(final BaseUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: return InverseTransform     .getInstance(this, toUnit);
            case +1: return IdentityTransform    .getInstance(this, toUnit);
            default: throw  toUnit.incompatibleUnitsException(this);
        }
    }
    
    UnitTransform getInverseTransform(final DerivedUnit toUnit) throws UnitException {
        switch (compareDimensionality(toUnit)) {
            case -1: return InverseTransform     .getInstance(this, toUnit);
            case +1: return IdentityTransform    .getInstance(this, toUnit);
            default: throw  toUnit.incompatibleUnitsException(this);
        }
    }
    
    /**
     * Indique si deux unités sont égales, en ignorant leurs symboles. Il n'est pas nécessaire que
     * les unités fondamentales y apparaissent dans le même ordre. Par exemple, "m²*s" sera considéré
     * identique à "s*m²".
     */
    public boolean equalsIgnoreSymbol(final Unit unit) {
        return (unit instanceof DerivedUnit) && compareDimensionality((DerivedUnit) unit)==1;
    }
    
    /**
     * Retourne un code
     * pour cette unité.
     */
    public int hashCode() {
        int code=92718538;
        for (int i=0; i<factors.length; i++) {
            code += factors[i].hashCode();
        }
        return code;
    }
}
