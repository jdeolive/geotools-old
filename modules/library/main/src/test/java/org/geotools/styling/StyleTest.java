package org.geotools.styling;

import org.geotools.factory.CommonFactoryFinder;
import org.junit.Test;
import org.opengis.filter.FilterFactory2;
import org.opengis.filter.expression.Expression;
import org.opengis.style.Displacement;
import org.opengis.style.StyleVisitor;

import static org.junit.Assert.*;

/**
 * Test the various Impl classes; many of these provide public api that is used by StyleImplFactory.
 * <p>
 * In particular this class is focused on:
 * <ul>
 * <li>Testing any methods not hit by StyleFactoryImpl and SLDParsing
 * <li>Going over the "cast" methods used to promote org.opengis.styling instances to a StyleImpl if
 * required. These are used to ensure that any set methods can handle a org.opengis.styling
 * instances.
 * </ul>
 */
public class StyleTest {
    static FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2(null);

    @Test
    public void testDisplacementImpl() {
        assertNull(DisplacementImpl.cast(null));

        DisplacementImpl displacement = new DisplacementImpl();
        displacement.setDisplacementX(1.0);
        displacement.setDisplacementY(1.0);

        assertSame(displacement, DisplacementImpl.cast(displacement));

        org.opengis.style.Displacement external = new Displacement() {
            public Expression getDisplacementY() {
                return ff.literal(1.0);
            }

            public Expression getDisplacementX() {
                return ff.literal(1.0);
            }

            public Object accept(StyleVisitor visitor, Object data) {
                return visitor.visit(this, data);
            }
        };
        
        displacement = DisplacementImpl.cast( external );
        assertEquals( ff.literal(1.0), displacement.getDisplacementX());
    }
}
