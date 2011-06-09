package org.geotools.filter.visitor;

import org.geotools.factory.CommonFactoryFinder;
import org.opengis.filter.FilterFactory;

import junit.framework.TestCase;

public class JoinExtractingVisitorTest extends TestCase {

    FilterFactory ff;
    
    @Override
    protected void setUp() throws Exception {
        ff = CommonFactoryFinder.getFilterFactory(null);
    }

    public void testSimple() throws Exception {
        JoinExtractingVisitor v = new JoinExtractingVisitor();
        ff.equal(ff.property("foo"), ff.property("bar"), true).accept(v, null);

        assertEquals(1, v.getJoins().size());
        assertEquals(0, v.getFilters().size());
    }
    
    public void testAnd() throws Exception {
        JoinExtractingVisitor v = new JoinExtractingVisitor();
        ff.and(ff.equal(ff.property("foo"), ff.property("bar"), true), 
               ff.equal(ff.property("foo"), ff.literal("bar"), true)).accept(v, null);

        assertEquals(1, v.getJoins().size());
        assertEquals(1, v.getFilters().size());
        
        v = new JoinExtractingVisitor();
        ff.and(ff.equal(ff.property("foo"), ff.property("bar"), true),
               ff.or(
                  ff.equal(ff.property("foo"), ff.property("bar"), true),
                  ff.equal(ff.property("foo"), ff.literal("bar"), true)
               )).accept(v, null);
        
        assertEquals(1, v.getJoins().size());
        assertEquals(1, v.getFilters().size());
    }
    
    public void testNestedAnd() throws Exception {
        JoinExtractingVisitor v = new JoinExtractingVisitor();
        ff.and(ff.equal(ff.property("foo"), ff.property("bar"), true),
               ff.and(
                  ff.equal(ff.property("foo"), ff.property("baz"), true),
                  ff.equal(ff.property("foo"), ff.literal("bar"), true)
               )).accept(v, null);
        
        assertEquals(2, v.getJoins().size());
        assertEquals(1, v.getFilters().size());
    }
}
