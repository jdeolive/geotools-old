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
 * Classe représentant des unités décalées par rapport à d'autres unités.
 * Les valeurs exprimées selon les unités d'un objet <code>OffsetUnit</code>
 * peuvent être converties vers le système d'unités {@link #unit} à l'aide
 * de l'équation suivante:
 *
 * <blockquote><pre>
 * <var>x</var><sub>{@link #unit}</sub>&nbsp;=&nbsp;{@link #offset}&nbsp;+&nbsp;<var>x</var><sub><code>this</code></sub>
 * </pre></blockquote>
 *
 * Les objets <code>OffsetUnit</code> permettent de faire des conversions entre
 * différentes unités, par exemple des degrés Kelvin aux degrés Celsius. Cette
 * classe n'ayant pas de constructeur publique, la seule façon d'obtenir des
 * unités décalées est d'utiliser les méthodes {@link #getInstance} ou {@link #offset}.
 *
 * <p><strong>NOTE: Des erreurs d'arrondissements importants peuvent survenir lors des
 * conversions d'unités utilisant des objets <code>OffsetUnit</code></strong>. La densité Sigma-T,
 * utilisée en océanographie, en est un bon exemple. Cette densité Sigma-T est la densité de l'eau
 * de mer auquel on a retranchée 1000&nbsp;kg/m³. Par exemple une eau de mer de densité 1024&nbsp;kg/m³
 * a une densité Sigma-T de 24&nbsp;kg/m³. Dans un nombre réel de type <code>float</code>, un nombre
 * de l'ordre de 24 peut être mémorisé avec environ 6 chiffres après la virgules. Mais en revanche,
 * un nombre de l'ordre de 1024 ne peut être mémorisé qu'avec environ 3 ou 4 chiffres après la virgule.
 * Les conversions des densités Sigma-T en densité ordinaires (qui consistent à ajouter 1000 kg/m³)
 * risquent donc de se traduire par une perte de 2 ou 3 chiffres significatifs de cette densité. Une façon
 * d'éviter ce problème est de convertir tous les tableaux de <code>float</code> en <code>double</code>
 * avant de convertir les unités.</p>
 *
 * @version 1.0
 * @author Steven R. Emmerson
 * @author Bill Hibbard
 * @author Martin Desruisseaux
 */
/*public*/
final class OffsetUnit extends Unit {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 6259444767590765138L;
    
    /**
     * La constante à ajouter aux mesures exprimées selon ces
     * unités pour obtenir des mesures exprimées selon les unités
     * {@link #unit}. Cette constante sera par exemple 273.15 pour
     * passer des degrés Celsius ou degrés Kelvin.
     */
    public final double offset;
    
    /**
     * Les unités vers lesquelles
     * se feront les conversions.
     */
    public final Unit unit;
    
    /**
     * Construit une unité décalée par rapport
     * à l'unité spécifiée, avec le symbole
     * spécifié.
     *
     * @param  offset La constante du décalage.
     * @param  unit L'unité associée. Pour des raisons
     *         d'efficacité, cette unité ne devrait pas
     *         être un objet <code>OffsetUnit</code>.
     * @param  symbol Le symbole associé à cette unité.
     * @param  prefix Liste des préfix pouvant être placés devant le symbole
     *         <code>symbol</code>, ou <code>null</code> s'il n'y en a pas.
     */
    private OffsetUnit(final double offset, final Unit unit, final String symbol, final PrefixSet prefix) {
        super(symbol, prefix);
        this.offset = offset;
        this.unit   = unit;
        
        if (unit==null) {
            throw new NullPointerException(Resources.format(ResourceKeys.ERROR_NO_UNIT));
        }
        if (Double.isNaN(offset) || Double.isInfinite(offset)) {
            throw new IllegalArgumentException(Resources.format(
                                               ResourceKeys.ERROR_NOT_A_NUMBER_$1,
                                               new Double(offset)));
        }
    }
    
    /**
     * Crée une nouvelle unité de même dimension que l'unité spécifiée, mais dont les mesures
     * seront décalées d'une constante. Les conversions d'unités se feront par l'équation suivante:
     *
     * <center>
     *     <var>x</var><sub><code>unit</code></sub> =
     *     <var>x</var><sub><code>new</code></sub> + <code>offset</code>
     * </center>
     *
     * où <var>x</var><sub><code>unit</code></sub> représente une quantité mesurée
     * selon les unités <code>unit</code> et <var>x</var><sub><code>new</code></sub>
     * représente une quantité mesurée selon les unités retournées par cette méthode.
     *
     * Par exemple on pourrait construire des unités de
     * température en degrés celsius avec le code suivant:
     *
     * <blockquote><pre>
     *     Unit CELSIUS=OffsetUnit.<strong>getInstance</strong>(273.15, KELVIN);
     * </blockquote></pre>
     *
     * Ce code signifie que 0°C correspond à 273.15°K.
     *
     * @param  offset Constante qu'il faudra additionner aux mesures
     *         exprimées selon les nouvelles unités pour les convertir
     *         dans les unités <code>unit</code>.
     * @param  unit Unités par rapport à lesquelles
     *         décaler les nouvelles unités.
     * @return Les unités créées, ou <code>unit</code> si le
     *         paramètre <code>offset</code> est égal à 0.
     *
     * @see BaseUnit#getInstance
     * @see DerivedUnit#getInstance
     * @see ScaledUnit#getInstance
     */
    public static Unit getInstance(final double offset, final Unit unit) {
        return getInstance(offset, unit, null, null);
    }
    
    /**
     * Crée une nouvelle unité de même dimension que l'unité spécifiée, mais dont les mesures
     * seront décalées d'une constante. Les conversions d'unités se feront par l'équation suivante:
     *
     * <center>
     *     <var>x</var><sub><code>unit</code></sub> =
     *     <var>x</var><sub><code>new</code></sub> + <code>offset</code>
     * </center>
     *
     * où <var>x</var><sub><code>unit</code></sub> représente une quantité mesurée
     * selon les unités <code>unit</code> et <var>x</var><sub><code>new</code></sub>
     * représente une quantité mesurée selon les unités retournées par cette méthode.
     *
     * Par exemple on pourrait construire des unités de
     * température en degrés celsius avec le code suivant:
     *
     * <blockquote><pre>
     *     Unit CELSIUS=OffsetUnit.<strong>getInstance</strong>(273.15, KELVIN, "°C", null);
     * </blockquote></pre>
     *
     * Ce code signifie que 0°C correspond à 273.15°K.
     *
     * @param  offset Constante qu'il faudra additionner aux mesures
     *         exprimées selon les nouvelles unités pour les convertir
     *         dans les unités <code>unit</code>.
     * @param  unit Unités par rapport à lesquelles
     *         décaler les nouvelles unités.
     * @param  symbol Le symbole qui représentera les unités créées. Si
     *         nul, alors un symbole par défaut sera créé. Dans l'exemple
     *         précédent, ce symbole par défaut serait "+273.15&nbsp;K"
     * @param  prefix Liste des préfix pouvant être placés devant le symbole
     *         <code>symbol</code>, ou <code>null</code> s'il n'y en a pas.
     * @return Les unités créées, ou <code>unit</code> si le
     *         paramètre <code>offset</code> est égal à 0.
     *
     * @see BaseUnit#getInstance
     * @see DerivedUnit#getInstance
     * @see ScaledUnit#getInstance
     */
    public static Unit getInstance(final double offset, final Unit unit, final String symbol, final PrefixSet prefix) {
        if (offset==0) {
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
            return new OffsetUnit(offset, unit, UnitFormat.DEFAULT.formatOffset(offset, unit, new StringBuffer()).toString(), null).internIgnoreSymbol();
        } else {
            return new OffsetUnit(offset, unit, symbol, prefix).intern();
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
        return getInstance(offset, unit, symbol, prefix);
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
     * Crée une nouvelle unité proportionnelle à cette unité. Par exemple pour
     * convertir en millicelsius des températures exprimées en degrés Celsius,
     * il faut les multiplier par 1000. On peut exprimer cette relation par le
     * code <code>Unit&nbsp;mdegC=degC.scale(0.001)</code>.
     *
     * @param  amount Facteur par lequel il faudra diviser les valeurs
     *         exprimées selon ces unités pour obtenir des valeurs
     *         exprimées selon les nouvelles unités.
     * @return Les nouvelles unités.
     *
     * @see #shift
     */
    public Unit scale(final double amount) {
        return (amount==1) ? this : getInstance(offset/amount, unit.scale(amount));
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
     * @see #scale
     */
    public Unit shift(final double offset) {
        return (offset==0) ? this : getInstance(this.offset+offset, unit);
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
     * exemple <code>KELVIN.convert(15, CELSIUS)</code> retournera <code>288.15</code>.
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
        return unit.convert(value, fromUnit)-offset;
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
                values[i] -= offset;
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
                values[i] = (float) (values[i]-offset);
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
            if (tr instanceof OffsetTransform) {
                return OffsetTransform.getInstance(fromUnit, this, ((OffsetTransform) tr).offset+offset);
            } else {
                return CompoundTransform.getInstance(tr, OffsetTransform.getInstance(unit, this, offset));
            }
        } else {
            return IdentityTransform.getInstance(fromUnit, this);
        }
    }
    
    /**
     * Convertit une mesure vers d'autre unités.
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
        return unit.inverseConvert(value+offset, toUnit);
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
                    values[i] += offset;
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
                    values[i] = (float) (values[i]+offset);
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
            final UnitTransform tr=unit.getInverseTransform(toUnit);
            if (tr instanceof OffsetTransform) {
                return ScaledTransform.getInstance(this, toUnit, ((OffsetTransform) tr).offset-offset);
            } else {
                return CompoundTransform.getInstance(OffsetTransform.getInstance(this, unit, -offset), tr);
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
        if (unit instanceof OffsetUnit) {
            final OffsetUnit that = (OffsetUnit) unit;
            return Double.doubleToLongBits(this.offset)==Double.doubleToLongBits(that.offset) &&
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
        final long code=Double.doubleToLongBits(offset);
        return (int) code ^ (int) (code >>> 32) ^ unit.hashCode();
    }
}
