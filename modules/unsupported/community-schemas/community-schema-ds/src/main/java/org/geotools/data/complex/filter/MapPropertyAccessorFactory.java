package org.geotools.data.complex.filter;

import java.util.Map;

import org.apache.commons.jxpath.JXPathContext;
import org.geotools.factory.Hints;
import org.geotools.feature.IllegalAttributeException;
import org.geotools.filter.expression.PropertyAccessor;
import org.geotools.filter.expression.PropertyAccessorFactory;

/**
 * A {@link PropertyAccessorFactory} that returns a {@link PropertyAccessor}
 * capable of evaluating single attribute names from a {@link Map}.
 * 
 * @author Gabriel Roldan
 * 
 */
public class MapPropertyAccessorFactory implements PropertyAccessorFactory {

    /**
     * Creates a property accessor for a particular class.
     * 
     * @param type
     *            The type of object to be accessed.
     * @param xpath
     *            The xpath expression to evaluate.
     * @param target
     *            The kind of result we are expecting (ie Geometry)
     * @param hints
     *            Hints to be used when creatign the accessor.
     * 
     * @return The property accessor, or <code>null</code> if this factory
     *         cannot create an accessor for the specified type.
     */
    public PropertyAccessor createPropertyAccessor(Class type, String xpath, Class target,
            Hints hints) {
        if (Map.class.isAssignableFrom(type)) {
            return MAP_ACCESSOR;
        }
        return null;
    }

    private static PropertyAccessor MAP_ACCESSOR = new PropertyAccessor() {

        public boolean canHandle(Object object, String xpath, Class target) {
            return object instanceof Map;
        }

        public Object get(Object object, String xpath, Class target)
                throws IllegalArgumentException {
            JXPathContext context = JXPathContext.newContext(object);
            context.setLenient(true);
            Object value = context.getValue(xpath);
            return value;
        }

        public void set(Object object, String xpath, Object value, Class target)
                throws IllegalAttributeException, IllegalArgumentException {
            throw new IllegalAttributeException("not implemented");
        }

    };
}
