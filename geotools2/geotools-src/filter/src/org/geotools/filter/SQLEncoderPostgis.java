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
 * Encodes a filter into a SQL WHERE statement.  It should hopefully be generic
 * enough that any SQL database will work with it, though it has only been
 * tested with MySQL.  This generic SQL encoder should eventually be able to
 * encode all filters except Geometry Filters (currently LikeFilters are not
 * yet fully implemented, but when they are they should be generic enough).  This
 * is because the OGC's SFS for SQL document specifies two ways of doing SQL
 * databases, one with native geometry types and one without.  To implement
 * an encoder for one of the two types simply subclass off of this encoder
 * and put in the proper GeometryFilter visit method.  Then add the filter
 * types supported to the capabilities in the static capabilities.addType block.
 *
 * @task TODO: Implement LikeFilter encoding, need to figure out escape chars,
 * the rest of the code should work right.  Once fixed be sure to add the LIKE
 * type to capabilities, so others know that they can be encoded.
 *
 * @author Chris Holmes, TOPP
 */
public class SQLEncoderPostgis extends SQLEncoder 
    implements org.geotools.filter.FilterVisitorImpl {
    
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
    public SQLEncoderPostgis(Writer out, AbstractFilterImpl filter, int srid)
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

    public void setSRID(int srid) {
	this.srid = srid;
    }

    public void visit(GeometryFilter filter) {

	log.finer("exporting GeometryFilter");	
	if (filter.getFilterType() == AbstractFilter.GEOMETRY_BBOX) {
	    DefaultExpression left = (DefaultExpression)filter.getLeftGeometry();
	    DefaultExpression right = (DefaultExpression)filter.getRightGeometry();
	    try {
		left.accept(this);
		out.write(" && ");
		right.accept(this);
	    }
	    catch(java.io.IOException ioe){
		log.warning("Unable to export filter" + ioe);
	    }
	} else {
	     log.warning("exporting unknown filter type, only bbox supported");
	}

    }


 public void visit(LiteralExpression expression) {
        log.finer("exporting LiteralExpression");
        try{
	    if (expression.getType() == DefaultExpression.LITERAL_GEOMETRY){
	        visit((BBoxExpression)expression);
		//bit of a hack, but BBox doesn't seem to like to use its visit.
	    } else {
	    out.write("'"+expression.getLiteral()+"'");
	    }        
	}
        catch(java.io.IOException ioe){
            log.warning("Unable to export expresion" + ioe);
        }
    }

    //TODO: throw some sort of error if srid is not set.
    public void visit(BBoxExpression expression) {
	Geometry bbox = (Geometry)expression.getLiteral();
	
	try {
	    String geomText = wkt.write(bbox);
	    out.write("GeometryFromText('" + geomText + "', " + srid + ")");
	} catch (java.io.IOException ioe){
            log.warning("Unable to export filter" + ioe);
        }
    }


}
