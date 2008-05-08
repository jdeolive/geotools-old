/**
 * 
 */
package org.geotools.renderer.lite;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import org.geotools.geometry.jts.LiteShape2;
import org.geotools.styling.TextSymbolizer;
import org.geotools.util.NumberRange;
import org.geotools.util.Range;
import org.opengis.feature.simple.SimpleFeature;

/**
 * Allow multiple thread to modify LabelCache.  
 * 
 * @author Jesse
 */
public class SynchronizedLabelCache implements LabelCache {

	private final LabelCache wrapped;
	
	public SynchronizedLabelCache() {
		this(new LabelCacheDefault());
	}
    
    public SynchronizedLabelCache(LabelCacheDefault default1) {
    	wrapped=default1;
	}

	public synchronized void start() {
        wrapped.start();
    }

    
    public synchronized void clear() {
        wrapped.clear();
    }

    
    public synchronized void clear( String layerId ) {
        wrapped.clear(layerId);
    }

    
    public synchronized void enableLayer( String layerId ) {
        wrapped.enableLayer(layerId);
    }

    
    public synchronized void end( Graphics2D graphics, Rectangle displayArea ) {
        wrapped.end(graphics, displayArea);
    }

    
    public synchronized void endLayer( String layerId, Graphics2D graphics, Rectangle displayArea ) {
        wrapped.endLayer(layerId, graphics, displayArea);
    }

    
    public synchronized void put( String layerId, TextSymbolizer symbolizer, SimpleFeature feature, LiteShape2 shape, NumberRange scaleRange ) {
        wrapped.put(layerId, symbolizer, feature, shape, scaleRange);
    }

    
    public synchronized void startLayer( String layerId ) {
        wrapped.startLayer(layerId);
    }

    
    public synchronized void stop() {
        wrapped.stop();
    }

	public synchronized void disableLayer(String layerId) {
		wrapped.disableLayer(layerId);
	}

	public synchronized List orderedLabels() {
		return wrapped.orderedLabels();
	}
    
}
