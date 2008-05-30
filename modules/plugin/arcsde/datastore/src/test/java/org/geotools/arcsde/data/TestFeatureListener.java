package org.geotools.arcsde.data;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.geotools.data.FeatureEvent;
import org.geotools.data.FeatureListener;

/**
 * This class copies out some of the details about each event received.
 * 
 * @author Jody Garnett
 */
public class TestFeatureListener implements FeatureListener {
	List<FeatureEvent> list = new LinkedList<FeatureEvent>();
	public void changed(FeatureEvent e) {
		list.add( new FeatureEvent( e ));
	}
}
