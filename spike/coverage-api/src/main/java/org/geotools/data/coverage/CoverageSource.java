package org.geotools.data.coverage;

import java.io.IOException;

import org.geotools.data.DataAccess;
import org.geotools.data.ResourceInfo;
import org.geotools.factory.Hints;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.feature.type.PropertyDescriptor;
import org.opengis.util.ProgressListener;

public interface CoverageSource {

    /**
     * Information describing the contents of this resoruce.
     * <p>
     * Please note that for FeatureContent:
     * <ul>
     * <li>name - unqiue with in the context of a Service
     * <li>schema - used to identify the type of resource; usually gml schema; although it may be more specific
     * <ul>
     */
    ResourceInfo getInfo(final ProgressListener listener);

    /**
     * Returns the qualified name for the Features this FeatureSource serves.
     * <p>
     * Note this is different from {@code getSchema().getType().getName()} (that
     * is, the feature type name), this name specifies the
     * {@link PropertyDescriptor#getName() AttributeDescriptor name} for the
     * Features served by this source. So,
     * {@code FeatureSoruce.getName() ==  FeatureSource.getFeatures().next().getAttributeDescriptor().getName()}.
     * </p>
     * <p>
     * Though it's a common practice when dealing with {@link SimpleFeatureType}
     * and {@link SimpleFeature} to assume they're equal. There's no conflict
     * (as per the dynamic typing system the {@code org.opengis.feature} package
     * defines) in a Feature and its type sharing the same name, as well as in a
     * GML schema an element declaration and a type definition may be named the
     * same. Yet, the distinction becomes important as we get rid of that
     * assumption and thus allow to reuse a type definition for different
     * FeatureSoruces, decoupling the descriptor (homologous to the Feature
     * element declaration in a GML schema) from its type definition.
     * </p>
     * <p>
     * So, even if implementors are allowed to delegate to
     * {@code getSchema().getName()} if they want to call the fatures and their
     * type the same, client code asking a
     * {@link DataAccess#getFeatureSource(Name)} shall use this name to request
     * for a FeatureSource, rather than the type name, as used in pre 2.5
     * versions of GeoTools. For example, if we have a FeatureSource named
     * {@code Roads} and its type is named {@code Roads_Type}, the
     * {@code DataAccess} shall be queried through {@code Roads}, not
     * {@code Roads_Type}.
     * </p>
     * 
     * @since 2.5
     * @return the name of the AttributeDescriptor for the Features served by
     *         this FeatureSource
     */
    Name getName(final ProgressListener listener);

    /**
     * Access to the DataStore implementing this FeatureStore.
     *
     * @return DataStore implementing this FeatureStore
     */
    CoverageDataStore getParentCoverageDataSet();
    
    public void dispose();
    
    public CoverageResponse read(CoverageRequest request, Hints ints)throws IOException;
    
    public Object getCoverageOffering(final ProgressListener listener, final boolean brief) throws IOException;
    

}
