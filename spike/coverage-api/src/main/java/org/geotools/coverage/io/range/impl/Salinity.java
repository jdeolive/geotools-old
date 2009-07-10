package org.geotools.coverage.io.range.impl;

import javax.measure.quantity.Quantity;
import javax.measure.unit.Unit;

public interface Salinity extends Quantity{
    
    public final static Unit<Salinity> PSU = Unit.ONE.alternate("PSU");

}
