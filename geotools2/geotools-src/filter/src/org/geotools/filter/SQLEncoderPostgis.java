/*
 *    Geotools - OpenSource mapping toolkit
 *    (C) 2002, Centre for Computational Geography
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package org.geotools.filter;

import java.util.logging.Logger;
import java.io.Writer;
import java.io.StringWriter;
import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.io.WKTWriter;

/**
 * Encodes a filter into a SQL WHERE statement for postgis.  This class
 * adds the ability to turn geometry filters into sql statements if they
 * are bboxes.  
 * TODO: add support for other spatial queries.
 *
 * @author Chris Holmes, TOPP
 */
public class SQLEncoderPostgis extends SQLEncoder 
    implements  org.geotools.filter.FilterVisitor {
    
    private static FilterCapabilities capabilities = SQLEncoder.getCapabilities();
    /** Standard java logger */
    private static Logger log = Logger.getLogger("org.geotools.filter");

    private static WKTWriter wkt = new WKTWriter();

    /** The escaped version of the single wildcard for the REGEXP pattern. */
    //    private Writer out;
    /** the encoded string for return */
    private String encodedSQL;

    //    private Writer out;


    /** The srid of the schema, so the bbox conforms.  Could be better to have
     * it in the bbox filter itself, but this works for now.*/
    private int srid;
    
    private String defaultGeometry;

    static {
	capabilities.addType(AbstractFilter.GEOMETRY_BBOX);
    }
    

    /**
     * Empty constructor
     * TODO: rethink empty constructor, as BBOXes _need_ an SRID, must make
     * client set it somehow.  Maybe detect when encode is called?
     */
    public SQLEncoderPostgis(){
    }

     public SQLEncoderPostgis(int srid){
	 this.srid = srid;
     }

    /** 
     * Convenience constructor to perform the whole encoding
     * process at once.
     *
     * @param out the writer to encode the SQL to.
     * @param filter the Filter to be encoded.
     */
    public SQLEncoderPostgis(Writer out, AbstractFilter filter, int srid)
	throws SQLEncoderException {
	this(srid);
        if (capabilities.fullySupports(filter)) {
	    super.out = out;
	    try{
		out.write("WHERE ");
		filter.accept(this);
		//out.write(";"); this should probably be added by client.
	    }
	    catch(java.io.IOException ioe){
		log.warning("Unable to export filter" + ioe);
	    }
	} else { 
	    throw new SQLEncoderException("Filter type not supported");
	}
    }

    /**
     * Sets a spatial reference system ESPG number, so that the geometry can
     * be properly encoded for postgis.  If geotools starts actually creating
     * geometries with valid srids then this method will no longer be needed.
     *
     * @param srid the integer code for the EPSG spatial reference system.
     */
    public void setSRID(int srid) {
	this.srid = srid;
    }

    /**
     * Sets the default geometry, so that filters with null for one of
     * their expressions can assume that the default geometry is intended.
     *
     * @param name the name of the default geometry Attribute.
     * @tasks REVISIT: pass in a featureType so that geometries can figure
     * out their own default geometry?  
     */
    //Do we really want clients to be using malformed filters?  I mean, this
    //is a useful method for unit tests, but shouldn't fully formed filters
    //usually be used?  Though I guess adding the option wouldn't hurt me. -ch
    public void setDefaultGeometry(String name) {
        this.defaultGeometry = name;
    }


    /**
     * Turns a geometry filter into the postgis sql bbox statement.
     *
     * @param filter the geometry filter to be encoded.
     */
    public void visit(GeometryFilter filter) {

	log.finer("exporting GeometryFilter");	
	if (filter.getFilterType() == AbstractFilter.GEOMETRY_BBOX) {
	    DefaultExpression left = (DefaultExpression)filter.getLeftGeometry();
	    DefaultExpression right = (DefaultExpression)filter.getRightGeometry();
	    try {
                if(left == null){
                    out.write(defaultGeometry);
                }
                else {
                    left.accept(this);
                }
		out.write(" && ");
		if(right == null){
                    out.write(defaultGeometry);
                }
                else {
                    right.accept(this);
                }
	    }
	    catch(java.io.IOException ioe){
		log.warning("Unable to export filter" + ioe);
	    }
	} else {
	     log.warning("exporting unknown filter type, only bbox supported");
	}

    }

    /**
     * Checks to see if the literal is a geometry, and encodes it if it 
     * is, if not just sends to the parent class.
     */
    public void visit(LiteralExpression expression) {
        log.finer("exporting LiteralExpression");
        try{
	    if (expression.getType() == DefaultExpression.LITERAL_GEOMETRY){
		Geometry bbox = (Geometry)expression.getLiteral();
		 String geomText = wkt.write(bbox);
	    out.write("GeometryFromText('" + geomText + "', " + srid + ")");
	    } else {
		out.write("'"+expression.getLiteral()+"'");
		//super.visit(expression);
	    }        
	}
        catch(java.io.IOException ioe){
            log.warning("Unable to export expresion" + ioe);
        }
    }


}
