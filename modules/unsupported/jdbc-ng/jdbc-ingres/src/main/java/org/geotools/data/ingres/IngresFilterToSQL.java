package org.geotools.data.ingres;

import java.io.IOException;

import org.geotools.data.jdbc.FilterToSQL;
import org.opengis.filter.expression.Literal;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;

public class IngresFilterToSQL extends FilterToSQL {
	
    protected void visitLiteralGeometry(Literal expression) throws IOException {
        // evaluate the literal and store it for later
        Geometry geom  = (Geometry) evaluateLiteral(expression, Geometry.class);
        
/*        if ( geom instanceof LinearRing ) {
            //postgis does not handle linear rings, convert to just a line string
            geom = geom.getFactory().createLineString(((LinearRing) geom).getCoordinateSequence());
        }*/
        out.write("GeometryFromText('");
        out.write(geom.toText());
        out.write("', " + currentSRID + ")");
    }

}
