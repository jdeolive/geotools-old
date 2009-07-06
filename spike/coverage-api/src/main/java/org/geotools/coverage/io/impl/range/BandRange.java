package org.geotools.coverage.io.impl.range;

import javax.measure.Measure;
import javax.measure.quantity.Quantity;

import org.geotools.util.Range;

public abstract class BandRange<T,QA extends Quantity> extends Measure<Range<Comparable<? super T>>, QA> {

}
