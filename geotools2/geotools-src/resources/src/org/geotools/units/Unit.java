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
import java.io.PrintStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectStreamException;

// Collections
import java.util.Iterator;

// Divers
import org.geotools.util.WeakHashSet;
import org.geotools.resources.rsc.ResourceKeys;
import org.geotools.resources.rsc.Resources;
import org.geotools.resources.units.Quantities;
import org.geotools.resources.units.Units;


/**
 * Placeholder for future <code>Unit</code> class. This
 * skeleton will be removed when the real classes (from
 * <A HREF="http://www.jcp.org/jsr/detail/108.jsp">JSR-108:
 * Units specification</A>) will be publicly available.
 * <br><br>
 * <strong>IMPORTANT: future version will NOT be compatible
 * will this one. Remind, this is a temporary class!</strong>
 *
 * @version $Id: Unit.java,v 1.5 2002/11/04 20:38:59 crotwell Exp $
 * @author Steven R. Emmerson
 * @author Bill Hibbard
 * @author Martin Desruisseaux
 */
public abstract class Unit implements Serializable {
    /**
     * Serial number for interoperability with different versions.
     */
    private static final long serialVersionUID = 8745958719541785628L;
    
    /**
     * Banque des objets qui ont été précédemment créés et
     * enregistrés par un appel à la méthode {@link #intern}.
     */
    private static final WeakHashSet pool=Prefix.pool; // Must be first!
    
    /**
     * Convenience constant for dimensionless unit.
     */
    public static final Unit DIMENSIONLESS = DerivedUnit.DIMENSIONLESS;
    
    /**
     * Convenience constant for base unit of angle.
     * Not a SI unit, but provides here for convenience.
     */
    public static final Unit RADIAN = get("rad");
    
    /**
     * Convenience constant for unit of angle.
     * Not a SI unit, but provides here for convenience.
     */
    public static final Unit DEGREE = get("\u00b0");
    
    /**
     * Convenience constant for base unit of length.
     */
    public static final Unit METRE = get("m");
    
    /**
     * Convenience constant for derived unit of length.
     */
    public static final Unit KILOMETRE = METRE.scale(1000);
    
    /**
     * Convenience constant for base unit of time.
     */
    public static final Unit SECOND = get("s");
    
    /**
     * Convenience constant for unit of time.
     * Not a SI unit, but provides here for convenience.
     */
    public static final Unit MILLISECOND = get("ms");
    
    /**
     * Convenience constant for unit of time.
     * Not a SI unit, but provides here for convenience.
     */
    public static final Unit DAY = get("d");
    
    /**
     * Convenience constant for base unit of mass.
     */
    public static final Unit KILOGRAM = get("kg");

    /**
     * Unit of arc-second. Used by the EPSG database.
     */
    public static final Unit ARC_SECOND = DEGREE.scale(1.0/3600);
    
    /**
     * Convenience constant for "Degrees Minutes Secondes" unit.
     * For example, this "unit" convert 12.5° into 123000 (i.e.
     * the concatenation of 12°30'00"). In a strict sence, this
     * is a formatting issue rather than an unit transformation
     * issue. Such transformation would be better handle by the
     * {@link org.geotools.pt.AngleFormat} class. However, this
     * "unit" appears really often in the EPSG database, and we
     * need it for interoperability with legacy libraries.
     */
    public static final Unit DMS = (Unit) new DMSUnit(1).intern();
    
    /**
     * Convenience constant for "Degrees dot Minutes Secondes" unit.
     * For example, this "unit" convert 12.5° into 12.3 (i.e.
     * the concatenation of 12°30'00"). In a strict sence, this
     * is a formatting issue rather than an unit transformation
     * issue. Such transformation would be better handle by the
     * {@link org.geotools.pt.AngleFormat} class. However, this
     * "unit" appears really often in the EPSG database, and we
     * need it for interoperability with legacy libraries.
     */
    public static final Unit SEXAGESIMAL_DEGREE = (Unit) new DMSUnit(10000).intern();
    
    /**
     * Symbole des unités de cet objet <code>Unit</code> (par exemple "kg").
     * Ce champs sera initialisé lors de la construction de chaque objet
     * <code>Unit</code> et ne sera jamais nul. Ce symbole peut commencer
     * par un des préfix énumérés dans le champ <code>prefix</code>. C'est
     * le cas par exemple des symboles "kg" (kilogramme) et "km" (kilomètre).
     */
    /*public*/ final String symbol;
    
    /**
     * Ensemble des préfix qui peuvent être combinés avec le symbole de l'unité.
     * Cet ensemble peut contenir par exemple les préfix "milli" (m), "centi" (c) et
     * "kilo" (k) qui, combinés avec les mètres (m), donneront les millimètres (mm),
     * centimètres (cm) ou kilomètre (km). Ce champ intervient lors des appels à la
     * méthode {@link #scale}. Il peut être nul si aucun préfix n'est autorisé pour
     * le symbole.
     */
    /*public*/ final PrefixSet prefix;
    
    /**
     * Construit une unité qui aura le symbole spécifié.
     * @param  symbol Symbole de ces unités (ne doit pas être nul).
     * @param  prefix Ensemble des préfix utilisables avec {@link #symbol},
     *         ou <code>null</code> s'il n'y en a aucun. Cet ensemble sera
     *         affecté au champ {@link #prefix} et interviendra lors des
     *         appels à la méthode {@link #scale}.
     * @throws NullPointerException Si <code>symbol</code> est nul.
     */
    /*protected*/ Unit(final String symbol, final PrefixSet prefix) throws NullPointerException {
        this.symbol=symbol.trim();
        this.prefix=prefix;
    }
    
    /**
     * Retourne les unités qui correspondent au symbole spécifié. Si plus d'une
     * unité correspond au symbole spécifié, une unité arbitraire sera choisie.
     * Si aucune unité n'a été trouvée, alors cette méthode retourne <code>null</code>.
     *
     * @param  symbol Symbole des unités recherchées. Cet argument ne doit pas être nul.
     * @return Les unités demandées.
     */
    public static Unit get(final String symbol) {
        Object unit=null;
        Unit[] units=null;
        for (int i=0; i<3; i++) {
            switch (i) {
                case 2: units=getDefaultUnits(); // fallthrough
                case 1: unit=UnitFormat.DEFAULT.parse(symbol); break;
                case 0: unit=getCached(symbol);                break;
            }
            if (unit instanceof Unit) {
                return (Unit) unit;
            }
        }
        return null;
    }
    
    /**
     * Retourne l'ensemble des unités prédéfinies par défaut. Les unités seront
     * retournées sans ordre particulier. Si les unités par défaut n'ont pas pu
     * être obtenues, cette méthode retourne <code>null</code>.
     */
    private static Unit[] getDefaultUnits() {
        final InputStream in=Unit.class.getClassLoader().getResourceAsStream(UnitSet.PATHNAME);
        if (in!=null) try {
            final ObjectInputStream oin=new ObjectInputStream(in);
            final Unit[] units=(Unit[]) oin.readObject();
            oin.close();
            /*
             * Appelle 'intern()' simplement par précaution.
             * En principe, ce n'est pas nécessaire.
             */
            for (int i=0; i<units.length; i++) {
                units[i]=units[i].intern();
            }
            return units;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return null;
    }
    
    /**
     * Recherche une unité correspondant au symbole spécifié. Cette méthode est temporaire.
     * Il serait plus efficace d'utiliser un objet {@link java.util.HashMap} qui ferait
     * correspondre les symboles avec des unités.
     */
    static Unit getCached(final String symbol) {
        for (final Iterator it=pool.iterator(); it.hasNext();) {
            final Object object=it.next();
            if (object instanceof Unit) {
                final Unit unit=(Unit) object;
                if (unit.symbol.equals(symbol)) return unit;
            }
        }
        return null;
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
    public abstract Unit rename(final String symbol, final PrefixSet prefix);
    
    /**
     * Retourne le nom de l'unité dans la langue de l'utilisateur.
     * Par exemple le symbole "m" sera traduit par "mètre" dans la
     * langue française. Si aucun nom n'est disponible pour l'unité
     * courante, retourne simplement son symbole.
     */
    public String getLocalizedName() {
        return Units.localize(symbol);
    }
    
    /**
     * Retourne la quantité que représente cette unité. Les quantités sont des chaînes de
     * caractères qui décrivent le paramètre physique mesuré, comme "mass" ou "speed". Si
     * aucune quantité n'est définie pour cette unité, retourne <code>null</code>.
     */
    public abstract String getQuantityName();
    
    /**
     * Retourne la quantité que représente cette unité dans la langue de l'utilisateur.
     * Les quantités sont des chaînes de caractères qui décrivent le paramètre physique
     * mesuré, comme "masse" ou "vitesse". Si aucune quantité n'est définie pour cette
     * unité, retourne <code>null</code>.
     */
    public String getLocalizedQuantityName() {
        return Quantities.localize(getQuantityName());
    }
    
    /**
     * Élève ces unités à une puissance entière. Notez que ce ne sont pas toutes les
     * unités qui peuvent être élevées à une puissance. Par exemple les unités de
     * température en degrés Celcius (°C), en Fahrenheit (°F) et la densité sigma-T
     * ne peuvent pas être élevées à une puissance autre que 0 et 1.
     *
     *
     * L'implémentation par défaut retourne une unité sans dimension ou <code>this</code>
     * selon que <code>power</code> ait la valeur 0 ou 1 respectivement, et lance une exception
     * dans tous les autres cas.
     *
     * @param power La puissance à laquelle élever cette unité.
     * @return Les unités résultant de l'élévation des unités
     *         <code>this</code> à la puissance <code>power</code>.
     * @throws UnitException Si ces unités ne peuvent être
     *         élevées à une puissance autre que 0 et 1.
     *
     * @see #multiply
     * @see #divide
     * @see #scale
     * @see #shift
     */
    public Unit pow(final int power) throws UnitException {
        switch (power) {
            case 0:  return DerivedUnit.DIMENSIONLESS;
            case 1:  return this;
            default: throw new UnitException(Resources.format(
                                             ResourceKeys.ERROR_BAD_UNIT_POWER_$2,
                                             new Integer(power), this), this, null);
        }
    }
    
    /**
     * Élève ces unités à une puissance fractionnaire. Cette méthode est utile entre
     * autre pour prendre la racine carrée d'un nombre, ce qui revient à l'élever à la
     * puissance ½. L'implémentation par défaut appele la méthode {@link #pow(int)}
     * pour les puissances entières, et lance une exception dans tous les autres cas.
     *
     * @param power La puissance à laquelle élever cette unité.
     * @return Les unités résultant de l'élévation des unités
     *         <code>this</code> à la puissance <code>power</code>.
     * @throws UnitException Si ces unités ne peuvent être
     *         élevées à une puissance autre que 0 et 1.
     */
    public Unit pow(final double power) throws UnitException {
        final int integer=(int) power;
        if (integer==power) {
            return pow(integer);
        }
        throw new UnitException(Resources.format(
                                ResourceKeys.ERROR_BAD_UNIT_POWER_$2,
                                new Double(power), this), this, null);
    }
    
    /**
     * Multiplie cette unité par une autre unité.
     * L'implémentation par défaut retourne <code>this</code> si <code>that</code> est
     * égal à une unité sans dimension, et lance une exception dams tous les autres cas.
     *
     * @param that L'unité par laquelle multiplier cette unité.
     * @return Le produit de <code>this</code> par <code>that</code>.
     * @throws UnitException Si les unités <code>this</code>
     *         <code>that</code> ne peuvent pas être multipliées.
     *
     * @see #pow
     * @see #divide
     * @see #scale
     * @see #shift
     */
    public Unit multiply(final Unit that) throws UnitException {
        if (DerivedUnit.DIMENSIONLESS.equals(that)) {
            return this;
        }
        throw illegalUnitOperationException(that);
    }
    /** Overrided by {@link SimpleUnit}.*/ Unit inverseMultiply(BaseUnit    that) throws UnitException {throw that.illegalUnitOperationException(this);}
    /** Overrided by {@link SimpleUnit}.*/ Unit inverseMultiply(DerivedUnit that) throws UnitException {throw that.illegalUnitOperationException(this);}
    /** Overrided by {@link SimpleUnit}.*/ Unit inverseMultiply(ScaledUnit  that) throws UnitException {throw that.illegalUnitOperationException(this);}
    
    /**
     * Divise cette unité par une autre unité.
     * L'implémentation par défaut retourne <code>this</code> si <code>that</code> est
     * égal à une unité sans dimension, et lance une exception dams tous les autres cas.
     *
     * @param that L'unité par laquelle diviser cette unité.
     * @return Le quotient de <code>this</code> par <code>that</code>.
     * @throws UnitException Si les unités <code>this</code>
     *         <code>that</code> ne peuvent pas être divisées.
     *
     * @see #pow
     * @see #multiply
     * @see #scale
     * @see #shift
     */
    public Unit divide(final Unit that) throws UnitException {
        if (DerivedUnit.DIMENSIONLESS.equals(that)) {
            return this;
        }
        throw illegalUnitOperationException(that);
    }
    /** Overrided by {@link SimpleUnit}.*/ Unit inverseDivide(BaseUnit    that) throws UnitException {throw that.illegalUnitOperationException(this);}
    /** Overrided by {@link SimpleUnit}.*/ Unit inverseDivide(DerivedUnit that) throws UnitException {throw that.illegalUnitOperationException(this);}
    /** Overrided by {@link SimpleUnit}.*/ Unit inverseDivide(ScaledUnit  that) throws UnitException {throw that.illegalUnitOperationException(this);}
    
    /**
     * Crée une nouvelle unité proportionnelle à cette unité. Par exemple
     * pour convertir en kilomètres des mesures exprimées en mètres, il
     * faut les diviser par 1000. On peut exprimer cette relation par le
     * code <code>Unit&nbsp;KILOMETRE=METRE.scale(1000)</code>.
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
    public abstract Unit scale(double amount);
    
    /**
     * Crée une nouvelle unité décalée par rapport à cette unité. Par exemple
     * pour convertir des degrés Kelvin en degrés Celsius, il faut soustraire
     * 273.15 aux degrés Kelvin. On peut exprimer cette relation par le code
     * <code>Unit&nbsp;CELCIUS=KELVIN.shift(273.15)</code>.
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
    public abstract Unit shift(double offset);
    
    /**
     * Indique si les unités <code>this</code> et <code>that</code> sont compatibles.
     * Si elles le sont, alors les méthodes <code>convert</code> ne lanceront jamais
     * d'exception pour ces unités. Toutes les classes du paquet <code>org.geotools.units</code>
     * garantissent que <code>this.canConvert(that)</code> donnera toujours le même
     * résultat que <code>that.canConvert(this)</code>. Si vous écrivez vos propres
     * classes dérivées de <code>Unit</code>, vous devrez vous assurer que cette
     * condition reste respectée. Mais évitez d'appeller <code>that.canConvert(this)</code>
     * à l'intérieur de cette méthode sous peine de tomber dans une boucle sans fin.
     *
     * @param that Autre unités avec laquelle on veut
     *        vérifier si ces unités sont compatibles.
     * @return <code>true</code> Si l'on garantie que les méthodes
     *         <code>convert</code> ne lanceront pas d'exceptions.
     */
    public abstract boolean canConvert(Unit        that);
    /**SimpleUnit*/ boolean canConvert(BaseUnit    that) {return canConvert((Unit) that);}
    /**SimpleUnit*/ boolean canConvert(DerivedUnit that) {return canConvert((Unit) that);}
    
    /**
     * Effectue la conversion d'une mesure exprimée selon d'autres unités. Par
     * exemple <code>METRE.convert(1,&nbsp;FOOT)</code> retournera <code>0.3048</code>.
     *
     * @param value La valeur exprimée selon les autres unités (<code>fromUnit</code>).
     * @param fromUnit Les autres unités.
     * @return La valeur convertie selon ces unités (<code>this</code>).
     * @throws UnitException Si les unités ne sont pas compatibles.
     */
    public abstract double convert(double value, Unit        fromUnit) throws UnitException;
    /**SimpleUnit*/ double convert(double value, BaseUnit    fromUnit) throws UnitException {return convert(value, (Unit) fromUnit);}
    /**SimpleUnit*/ double convert(double value, DerivedUnit fromUnit) throws UnitException {return convert(value, (Unit) fromUnit);}
    
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
    public abstract void convert(double[] values, Unit        fromUnit) throws UnitException;
    /**SimpleUnit*/ void convert(double[] values, BaseUnit    fromUnit) throws UnitException {convert(values, (Unit) fromUnit);}
    /**SimpleUnit*/ void convert(double[] values, DerivedUnit fromUnit) throws UnitException {convert(values, (Unit) fromUnit);}
    
    /**
     * Effectue sur-place la conversion de mesures exprimées selon d'autres
     * unités. Les valeurs converties remplaceront les anciennes valeurs.
     *
     * @param  values En entré, les valeurs exprimées selon les autres
     *         unités (<code>fromUnit</code>). En sortie, les valeurs exprimées
     *         selon ces unités (<code>this</code>).
     * @param  fromUnit Les autres unités.
     * @throws UnitException Si les unités ne sont pas compatibles. Dans ce
     *         cas, aucun élément de <code>values</code> n'aura été modifié.
     */
    public abstract void convert(float[] values, Unit        fromUnit) throws UnitException;
    /**SimpleUnit*/ void convert(float[] values, BaseUnit    fromUnit) throws UnitException {convert(values, (Unit) fromUnit);}
    /**SimpleUnit*/ void convert(float[] values, DerivedUnit fromUnit) throws UnitException {convert(values, (Unit) fromUnit);}
    
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
    public abstract UnitTransform getTransform(Unit        fromUnit) throws UnitException;
    /**SimpleUnit*/ UnitTransform getTransform(BaseUnit    fromUnit) throws UnitException {return getTransform((Unit) fromUnit);}
    /**SimpleUnit*/ UnitTransform getTransform(DerivedUnit fromUnit) throws UnitException {return getTransform((Unit) fromUnit);}
    
    /**
     * Convertit une mesure vers d'autre unités. Par exemple
     * <code>METRE.inverseConvert(1,&nbsp;FOOT)</code> retournera
     * <code>3.2808</code>. Cette méthode est l'inverse de la méthode
     * {@link #convert(double,Unit)}. Bien que n'étant pas destinée à
     * être appellée directement, les classes dérivées devront quand
     * même la définir pour un fonctionnement correcte.
     *
     * @param  value La valeur exprimée selon ces unités (<code>this</code>).
     * @param  toUnit Les autres unités.
     * @return La valeur convertie selon les autres unités (<code>toUnit</code>).
     * @throws UnitException Si les unités ne sont pas compatibles.
     */
    /*protected*/ abstract double inverseConvert(double value, Unit        toUnit) throws UnitException;
    /**SimpleUnit*/        double inverseConvert(double value, BaseUnit    toUnit) throws UnitException {return inverseConvert(value, (Unit) toUnit);}
    /**SimpleUnit*/        double inverseConvert(double value, DerivedUnit toUnit) throws UnitException {return inverseConvert(value, (Unit) toUnit);}
    
    /**
     * Effectue sur-place la conversion de mesures vers d'autres unités.
     * Les valeurs converties remplaceront les anciennes valeurs. Cette
     * méthode est l'inverse de la méthode {@link #convert(double[],Unit)}.
     * Bien que n'étant pas destinée à être appellée directement, les classes
     * dérivées devront quand même la définir pour un fonctionnement correcte.
     *
     * @param  values En entré, les valeur exprimées selon ces unités
     *         (<code>this</code>). En sortie, les valeurs exprimées
     *         selon les autres unités (<code>toUnit</code>).
     * @param  toUnit Les autres unités.
     * @throws UnitException Si les unités ne sont pas compatibles. Dans ce
     *         cas, aucun élément de <code>values</code> n'aura été modifié.
     */
    /*protected*/ abstract void inverseConvert(double[] values, Unit        toUnit) throws UnitException;
    /**SimpleUnit*/        void inverseConvert(double[] values, BaseUnit    toUnit) throws UnitException {inverseConvert(values, (Unit) toUnit);}
    /**SimpleUnit*/        void inverseConvert(double[] values, DerivedUnit toUnit) throws UnitException {inverseConvert(values, (Unit) toUnit);}
    
    /**
     * Effectue sur-place la conversion de mesures vers d'autres unités.
     * Les valeurs converties remplaceront les anciennes valeurs. Cette
     * méthode est l'inverse de la méthode {@link #convert(float[],Unit)}.
     * Bien que n'étant pas destinée à être appellée directement, les classes
     * dérivées devront quand même la définir pour un fonctionnement correcte.
     *
     * @param  values En entré, les valeur exprimées selon ces unités
     *         (<code>this</code>). En sortie, les valeurs exprimées
     *         selon les autres unités (<code>toUnit</code>).
     * @param  toUnit Les autres unités.
     * @throws UnitException Si les unités ne sont pas compatibles. Dans ce
     *         cas, aucun élément de <code>values</code> n'aura été modifié.
     */
    /*protected*/ abstract void inverseConvert(float[] values, Unit        toUnit) throws UnitException;
    /**SimpleUnit*/        void inverseConvert(float[] values, BaseUnit    toUnit) throws UnitException {inverseConvert(values, (Unit) toUnit);}
    /**SimpleUnit*/        void inverseConvert(float[] values, DerivedUnit toUnit) throws UnitException {inverseConvert(values, (Unit) toUnit);}
    
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
    /*protected*/ abstract UnitTransform getInverseTransform(Unit        toUnit) throws UnitException;
    /**SimpleUnit*/        UnitTransform getInverseTransform(BaseUnit    toUnit) throws UnitException {return getInverseTransform((Unit) toUnit);}
    /**SimpleUnit*/        UnitTransform getInverseTransform(DerivedUnit toUnit) throws UnitException {return getInverseTransform((Unit) toUnit);}
    
    /**
     * Retourne une exception à lancer lorsque
     * l'opération demandée n'est pas permise.
     */
    final UnitException illegalUnitOperationException(Unit that) {
        return new UnitException(Resources.format(
                                 ResourceKeys.ERROR_BAD_UNIT_OPERATION_$2, this, that), this, that);
    }
    
    /**
     * Retourne une exception à lancer lorsque
     * les unités ne sont pas compatibles.
     */
    final UnitException incompatibleUnitsException(Unit that) {
        return new UnitException(Resources.format(
                                 ResourceKeys.ERROR_NON_CONVERTIBLE_UNITS_$2, this, that), this, that);
    }
    
    /**
     * Après la lecture binaire, vérifie si les
     * unitées lues existaient déjà en mémoire.
     */
    final Object readResolve() throws ObjectStreamException {
        return intern();
    }
    
    /**
     * Retourne un exemplaire unique de cette unité. Une banque d'unités, initialement
     * vide, est maintenue de façon interne par la classe <code>Unit</code>. Lorsque la
     * méthode <code>intern</code> est appelée, elle recherchera des unités égales à
     * <code>this</code> au sens de la méthode {@link #equals}. Si de telles unités
     * sont trouvées, elles seront retournées. Sinon, les unités <code>this</code>
     * seront ajoutées à la banque de données en utilisant une référence faible
     * et cette méthode retournera <code>this</code>.
     * <br><br>
     * De cette méthode il s'ensuit que pour deux unités <var>u</var> et <var>v</var>,
     * la condition <code>u.intern()==v.intern()</code> sera vrai si et seulement si
     * <code>u.equals(v)</code> est vrai.
     */
    /*protected*/ final Unit intern() {
        return (Unit) pool.canonicalize(this);
    }
    
    /**
     * Retourne un exemplaire unique de cette unité, quel que soit son symbole. Une banque d'unités,
     * initialement vide, est maintenue de façon interne par la classe <code>Unit</code>. Lorsque la
     * méthode <code>internIgnoreSymbol</code> est appelée, elle recherchera des unités égales à
     * <code>this</code> au sens de la méthode {@link #equalsIgnoreSymbol}. Si de telles unités
     * sont trouvées, elles seront retournées. Sinon, les unités <code>this</code> seront ajoutées
     * à la banque de données en utilisant une référence faible et cette méthode retournera <code>this</code>.
     * <br><br>
     * De cette méthode il s'ensuit que pour deux unités <var>u</var> et <var>v</var>,
     * la condition <code>u.internIgnoreSymbol()==v.internIgnoreSymbol()</code> sera
     * généralement vrai si <code>u.equalsIgnoreSymbol(v)</code> est vrai. Toutefois,
     * si la banque de données contient plusieurs unités identiques en tout sauf leurs
     * symboles, alors il n'y a aucune garantie de quelle unité sera choisie par cette
     * méthode.
     */
    /*protected*/ final Unit internIgnoreSymbol() {
        synchronized (pool) {
            final Object canonical = pool.get(new Unamed());
            if (canonical instanceof Unit) {
                return (Unit) canonical;
            }
            pool.add(this);
            return this;
        }
    }
    
    /**
     * Indique si deux unités sont égales et utilisent le même symbole.
     */
    public boolean equals(final Object unit) {
        if (unit!=null) {
            if (unit instanceof Unamed) {
                return unit.equals(this);
            }
            if (getClass().equals(unit.getClass())) {
                final Unit cast = (Unit) unit;
                if (symbol.equals(cast.symbol)) {
                    if (equalsIgnoreSymbol(cast)) {
                        if (prefix==cast.prefix) return true;
                        if (prefix==null)        return false;
                        return prefix.equals(cast.prefix);
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Indique si deux unités sont égales, en ignorant leurs symboles. Le
     * champs {@link #symbol} de chacune des deux unités ne sera pas pris
     * en compte.
     */
    /*public*/ abstract boolean equalsIgnoreSymbol(Unit unit);
    
    /**
     * Retourne un code propre à cette unité. Le calcul de
     * ce code ne devrait pas prendre en compte le symbole
     * de l'unité.
     */
    public abstract int hashCode();
    
    /**
     * Retourne les symboles de ces unités, par exemple "m/s".
     * S'il existe un symbole particulier pour la langue de
     * l'utilisateur, ce symbole sera retourné.
     */
    public String toString() {
        return symbol;
    }
    
    
    
    
    /**
     * Enveloppe des unités qui seront comparées sans tenir compte des symboles.
     * Cette classe est utilisée par {@link #internIgnoreSymbol} afin de puiser
     * dans la banque d'unités {@link #pool} n'importe quel unité de dimensions
     * appropriées, quel que soit son symbole.
     *
     * @version 1.0
     * @author Martin Desruisseaux
     */
    private final class Unamed {
        /**
         * Renvoie le code des
         * unités enveloppées.
         */
        public int hashCode() {
            return Unit.this.hashCode();
        }
        
        /**
         * Vérifie si les unités enveloppées sont
         * égales aux unités spécifiées, sans tenir
         * compte de leurs symboles respectifs.
         */
        public boolean equals(final Object obj) {
            return (obj instanceof Unit) && equalsIgnoreSymbol((Unit) obj);
        }
    }
}
