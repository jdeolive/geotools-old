package org.geotools.data.collection;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.feature.CollectionListener;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.opengis.filter.sort.SortBy;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.util.ProgressListener;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.ItemVisitor;
import com.vividsolutions.jts.index.SpatialIndex;
import com.vividsolutions.jts.index.strtree.AbstractNode;
import com.vividsolutions.jts.index.strtree.STRtree;

public class SpatialIndexFeatureCollection implements SimpleFeatureCollection {
    /** SpatialIndex holding the contents of the FeatureCollection */
    protected STRtree index;

    protected SimpleFeatureType schema;

    protected boolean locked;

    public SpatialIndexFeatureCollection() {
        this.index = new STRtree();
        this.locked = false;
    }

    protected synchronized void lock() {
        if (!locked) {
            index.build();
            locked = true;
        }
    }

    @SuppressWarnings("unchecked")
    public SimpleFeatureIterator features() {
        lock();

        Envelope everything = new Envelope(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        final List<SimpleFeature> list = (List<SimpleFeature>) index.query(everything);
        final Iterator<SimpleFeature> iterator = list.iterator();
        return new SimpleFeatureIterator() {
            public SimpleFeature next() throws NoSuchElementException {
                return iterator.next();
            }

            public boolean hasNext() {
                return iterator.hasNext();
            }

            public void close() {
            }
        };
    }

    public SimpleFeatureCollection sort(SortBy order) {
        throw new UnsupportedOperationException();
    }

    public SimpleFeatureCollection subCollection(Filter filter) {
        throw new UnsupportedOperationException();
    }

    public void accepts(final FeatureVisitor visitor, ProgressListener listener) throws IOException {
        lock();

        Envelope everything = new Envelope(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY,
                Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        final ProgressListener progress = listener != null ? listener : new NullProgressListener();
        progress.started();
        final float size = (float) size();
        index.query(everything, new ItemVisitor() {
            float count = 0f;

            public void visitItem(Object item) {
                try {
                    SimpleFeature feature = (SimpleFeature) item;
                    visitor.visit(feature);
                } catch (Throwable t) {
                    progress.exceptionOccurred(t);
                } finally {
                    progress.progress(count / size);
                }
            }
        });
        progress.complete();
    }

    public boolean add(SimpleFeature feature) {
        if (locked)
            return false;

        ReferencedEnvelope bounds = ReferencedEnvelope.reference(feature.getBounds());
        index.insert(bounds, feature);

        return false;
    }

    public boolean addAll(Collection<? extends SimpleFeature> collection) {
        if (locked)
            return false;

        for (SimpleFeature feature : collection) {
            try {
                ReferencedEnvelope bounds = ReferencedEnvelope.reference(feature.getBounds());
                index.insert(bounds, feature);
            } catch (Throwable t) {
            }
        }
        return false;
    }

    public boolean addAll(
            FeatureCollection<? extends SimpleFeatureType, ? extends SimpleFeature> collection) {
        if (locked)
            return false;

        FeatureIterator<? extends SimpleFeature> iter = collection.features();
        try {
            while (iter.hasNext()) {
                try {
                    SimpleFeature feature = iter.next();
                    ReferencedEnvelope bounds = ReferencedEnvelope.reference(feature.getBounds());
                    index.insert(bounds, feature);
                } catch (Throwable t) {
                }
            }
        } finally {
            iter.close();
        }
        return false;
    }

    public void addListener(CollectionListener listener) throws NullPointerException {
    }

    public synchronized void clear() {
        index = null;
        index = new STRtree();
        locked = false;
    }

    public void close(FeatureIterator<SimpleFeature> close) {
    }

    public void close(Iterator<SimpleFeature> close) {
    }

    @SuppressWarnings("unchecked")
    public boolean contains(Object obj) {
        if( obj instanceof SimpleFeature ){
            SimpleFeature feature = (SimpleFeature) obj;
            ReferencedEnvelope bounds = ReferencedEnvelope.reference( feature.getBounds() );
            for( Iterator<SimpleFeature> iter = (Iterator<SimpleFeature>) index.query( bounds ); iter.hasNext(); ){
                SimpleFeature sample = iter.next();
                if( sample == feature ){
                    return true;
                }
            }
            
        }
        return false;
    }

    public boolean containsAll(Collection<?> collection) {
        boolean containsAll = true;
        for( Object obj : collection ){
            boolean contains = contains( obj );
            if( !contains ){
                containsAll =false;
                break;
            }
        }
        return containsAll;
    }

    public ReferencedEnvelope getBounds() {
        CoordinateReferenceSystem crs = schema.getCoordinateReferenceSystem();
        Envelope bounds = (Envelope) index.getRoot().getBounds();
        return new ReferencedEnvelope( bounds, crs );
    }

    public String getID() {
        return null;
    }

    public SimpleFeatureType getSchema() {
        return schema;
    }

    public boolean isEmpty() {
        return index.itemsTree().isEmpty();
    }

    public Iterator<SimpleFeature> iterator() {
        return null;
    }

    public void purge() {
    }

    public boolean remove(Object o) {
        if( o instanceof SimpleFeature ){
            SimpleFeature feature = (SimpleFeature) o;
            ReferencedEnvelope bounds = ReferencedEnvelope.reference( feature.getBounds() );
            return index.remove( bounds, feature );
        }
        return false;
    }

    public boolean removeAll(Collection<?> c) {
        boolean allFound = true;
        for( Object obj : c ){
            boolean removed = remove( obj );
            if( !removed ){
                allFound = false;
            }
        }
        return allFound;
    }

    public void removeListener(CollectionListener listener) throws NullPointerException {
    }

    public boolean retainAll(Collection<?> c) {
        return false;
    }

    public int size() {
        return index.size();
    }

    public Object[] toArray() {
        return null;
    }

    public <O> O[] toArray(O[] a) {
        return null;
    }

}