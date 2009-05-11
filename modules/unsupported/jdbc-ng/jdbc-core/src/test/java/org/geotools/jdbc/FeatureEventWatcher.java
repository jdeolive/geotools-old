package org.geotools.jdbc;

import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureEvent.Type;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;

/**
 * Records FeatureEvents and provides a record that we can check.
 */
public class FeatureEventWatcher implements FeatureListener {
    /** Last known type */
    public Type type = null;

    /** last known event source */
    public FeatureSource<? extends FeatureType, ? extends Feature> source;

    /** Total bounds since last reset*/
    public ReferencedEnvelope bounds = new ReferencedEnvelope();

    /** number of events since last reset */
    public int count = 0;
    
    public void changed(FeatureEvent featureEvent) {
        type = featureEvent.getType();
        if( bounds == null ){
            bounds = featureEvent.getBounds();
        }
        else {
            bounds.expandToInclude( featureEvent.getBounds() );
        }
        source = featureEvent.getFeatureSource();
        count++;
    }
    
    public void reset(){
        type = null;
        bounds = new ReferencedEnvelope();
        source = null;
        count = 0;
    }
}