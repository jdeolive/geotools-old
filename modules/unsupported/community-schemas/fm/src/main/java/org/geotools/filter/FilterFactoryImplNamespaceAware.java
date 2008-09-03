package org.geotools.filter;

import org.geotools.factory.Hints;
import org.geotools.filter.expression.FeaturePropertyAccessorFactory;
import org.opengis.filter.expression.PropertyName;
import org.xml.sax.helpers.NamespaceSupport;

public class FilterFactoryImplNamespaceAware extends FilterFactoryImpl {

    private Hints namespaceHints;

    /**
     * Empty constructor, no namespace context received, behaves exactly like
     * {@link FilterFactoryImpl}
     */
    public FilterFactoryImplNamespaceAware() {
        super();
    }

    public FilterFactoryImplNamespaceAware(NamespaceSupport namespaces) {
        setNamepaceContext(namespaces);
    }

    //@Override
    public PropertyName property(String name) {
        return new AttributeExpressionImpl(name, namespaceHints);
    }
    
    public void setNamepaceContext(NamespaceSupport namespaces){
        namespaceHints = new Hints(FeaturePropertyAccessorFactory.NAMESPACE_CONTEXT,
                namespaces);
    }
}
