package org.geotools.filter;

import com.vividsolutions.jts.geom.Envelope;

import org.geotools.feature.FeatureType;

// J2SE dependencies
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


public abstract class FilterFactory {
    /**
 * The logger 
 */
    protected static final Logger LOGGER = Logger.getLogger(
            "org.geotools.filter");
    private static FilterFactory factory = null;

    /** creates an instance of a Filter factory
 * @return an instance of the Filter factory
 */
    public static FilterFactory createFilterFactory() { //throws FilterFactoryCreationException{ 

        if (factory != null) {
            return factory;
        }

        String factoryClass = System.getProperty("FilterFactoryImpl");
        LOGGER.fine("loaded property = " + factoryClass);

        FilterFactory sf = null;

        if ((factoryClass != null) && (factoryClass != "")) {
            //try{
            sf = createFilterFactory(factoryClass);

            if (sf != null) {
                return sf;
            }

            //} catch (FilterFactoryCreationException e){
            // do nothing yet or should we give up now
            //  LOGGER.info("Failed to create " + factoryClass + " because \n" + e);
            //}
        } else {
            sf = createFilterFactory("org.geotools.filter.FilterFactoryImpl");
        }

        factory = sf;

        return sf;
    }

    public static FilterFactory createFilterFactory(String factoryClass) { // throws FilterFactoryCreationException{

        try {
            return factory = (FilterFactory) Class.forName(factoryClass)
                                                  .newInstance();
        } catch (ClassNotFoundException cnfe) {
            severe("createFilterFactory",
                "failed to find implementation " + factoryClass, cnfe);

            //throw new FilterFactoryCreationException("Failed to find implementation " + factoryClass, cnfe); 
        } catch (InstantiationException ie) {
            severe("createFilterFactory",
                "failed to insantiate implementation " + factoryClass, ie);

            //  throw new FilterFactoryCreationException("Failed to insantiate implementation " + factoryClass, ie);
        } catch (IllegalAccessException iae) {
            severe("createFilterFactory",
                "failed to access implementation " + factoryClass, iae);

            //throw new FilterFactoryCreationException("Failed to access implementation " + factoryClass, iae);
        }

        return null;
    }

    public abstract LogicFilter createLogicFilter(Filter filter1,
        Filter filter2, short filterType) throws IllegalFilterException;

    public abstract LogicFilter createLogicFilter(short filterType)
        throws IllegalFilterException;

    public abstract LogicFilter createLogicFilter(Filter filter,
        short filterType) throws IllegalFilterException;

    public abstract BBoxExpression createBBoxExpression(Envelope env)
        throws IllegalFilterException;

    public abstract LiteralExpression createLiteralExpression(int i);

    public abstract MathExpression createMathExpression()
        throws IllegalFilterException;

    public abstract FidFilter createFidFilter();

    public abstract AttributeExpression createAttributeExpression(
        FeatureType schema, String path) throws IllegalFilterException;

    public abstract LiteralExpression createLiteralExpression(Object o)
        throws IllegalFilterException;

    public abstract CompareFilter createCompareFilter(short type)
        throws IllegalFilterException;

    public abstract LiteralExpression createLiteralExpression();

    public abstract LiteralExpression createLiteralExpression(String s);

    public abstract LiteralExpression createLiteralExpression(double d);

    public abstract AttributeExpression createAttributeExpression(
        FeatureType schema);

    public abstract MathExpression createMathExpression(short expressionType)
        throws IllegalFilterException;

    public abstract NullFilter createNullFilter();

    public abstract BetweenFilter createBetweenFilter()
        throws IllegalFilterException;

    public abstract GeometryFilter createGeometryFilter(short filterType)
        throws IllegalFilterException;

    public abstract FidFilter createFidFilter(String fid);

    public abstract LikeFilter createLikeFilter();

    public abstract FunctionExpression createFunctionExpression(String name);

    /**
 * Convenience method for logging a message with an exception.
 */
    protected static void severe(final String method, final String message,
        final Exception exception) {
        final LogRecord record = new LogRecord(Level.SEVERE, message);
        record.setSourceMethodName(method);
        record.setThrown(exception);
        LOGGER.log(record);
    }
}
