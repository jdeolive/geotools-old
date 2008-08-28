/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.coverage.io.impl.range;

import java.awt.image.SampleModel;
import java.util.ArrayList;
import java.util.List;

import javax.measure.quantity.Dimensionless;
import javax.measure.unit.Unit;

import org.geotools.coverage.io.range.Axis;
import org.opengis.coverage.SampleDimension;
import org.opengis.feature.type.Name;
import org.opengis.referencing.crs.SingleCRS;
import org.opengis.util.InternationalString;

/**
 * Implementation of {@link Axis} for multibands images.
 * 
 * <p>
 * This implementation of Axis can be seen as a stub implementation since in
 * this case we do not really have an {@link Axis} for this kind of data, or
 * rather we have an axis that just represents an ordinal or a certain set of .
 * 
 * @author Simone Giannecchini, GeoSolutions
 * @todo add convenience constructor based on {@link SampleDimension} and or
 *       {@link SampleModel}
 */
public class DimensionlessAxis implements Axis<Dimensionless, BandIndexMeasure> {

    /**
     * Textual representation for the various bands in this {@link Axis}.
     */
    private String[] bandsKeys = null;

    private Name name = null;

    private InternationalString description;

    /**
     * 
     */
    public DimensionlessAxis(final int bandsNumber, final Name name,
            final InternationalString description) {
        String[] bandsKeys = new String[bandsNumber];
        for (int i = 0; i < bandsNumber; i++)
            bandsKeys[i] = Integer.toString(i);
        init(bandsKeys, name, description);
    }

    /**
     * 
     */
    public DimensionlessAxis(final String[] bands, final Name name,
            final InternationalString description) {
        init(bands, name, description);
    }

    /**
     * 
     */
    public DimensionlessAxis(final List<String> bandsKeys, final Name name,
            final InternationalString description) {
        init((String[]) bandsKeys.toArray(), name, description);
    }

    private void init(String[] bandsKeys, final Name name,
            final InternationalString description) {
        this.name = name;
        this.description = description;
        this.bandsKeys = bandsKeys;
    }
    
    /**
     * @see org.geotools.coverage.io.range.Axis#getCoordinateReferenceSystem()
     */
    public SingleCRS getCoordinateReferenceSystem() {
        return null;
    }

    /**
     * @see org.geotools.coverage.io.range.Axis#getDescription()
     */
    public InternationalString getDescription() {
        return this.description;
    }

    /**
     * @see org.geotools.coverage.io.range.Axis#getKey(int)
     */
    public BandIndexMeasure getKey(int keyIndex) {
        return new BandIndexMeasure(keyIndex, this.bandsKeys[keyIndex]);
    }

    /**
     * @see org.geotools.coverage.io.range.Axis#getKeys()
     */
    public List<BandIndexMeasure> getKeys() {
        List<BandIndexMeasure> list = new ArrayList<BandIndexMeasure>(
                this.bandsKeys.length);
        int i = 0;
        for (String band : this.bandsKeys)
            list.add(new BandIndexMeasure(i++, band));
        return list;
    }

    /**
     * @see org.geotools.coverage.io.range.Axis#getName()
     */
    public Name getName() {
        return this.name;
    }

    /**
     * @see org.geotools.coverage.io.range.Axis#getNumKeys()
     */
    public int getNumKeys() {
        return bandsKeys.length;
    }

    /**
     * @see org.geotools.coverage.io.range.Axis#getUnitOfMeasure()
     */
    public Unit<Dimensionless> getUnitOfMeasure() {
        return Unit.ONE;
    }

}
