/*
 * Simple demonstration program for Geotools 2's CTS module.
 * http://www.geotools.org
 */

// Geotools dependencies
import org.geotools.pt.*;
import org.geotools.cs.*;
import org.geotools.ct.*;

// Other packages
import java.io.*;
import org.geotools.units.*;
import javax.media.jai.ParameterList;


/**
 * An example of application reading points from the standard input,  transforming
 * them and writting the result to the standard output. This class can be run from
 * the command-line using the following syntax:
 *
 * <blockquote><pre>
 * java TransformationConsole [classification]
 * </pre></blockquote>
 *
 * Where [classification] is the the classification name of the projection to perform.
 * The default value is "Mercator_1SP". The list of supported classification name is
 * available here:
 *
 *   http://modules.geotools.org/cts-coordtrans/apidocs/org/geotools/ct/package-summary.html
 *
 * To exit from the application, enter "exit".
 *
 * @version $Id: TransformationConsole.java,v 1.3 2003/07/25 18:06:42 desruisseaux Exp $
 * @author Martin Desruisseaux
 */
public class TransformationConsole {
    /**
     * The program main entry point.
     *
     * @param  args Array of command-line arguments. This small demo accept only
     *              one argument: the classification name of the projection to
     *              perform.
     *
     * @throws IOException if an error occured while reading the input stream.
     * @throws FactoryException if a coordinate system can't be constructed.
     * @throws TransformException if a transform failed.
     */
    public static void main(String[] args) throws IOException, FactoryException, TransformException {
        /*
         * Check command-line arguments.
         */
        String classification;
        switch (args.length) {
            case  0: classification = "Mercator_1SP"; break;
            case  1: classification = args[0]; break;
            default: System.err.println("Expected 0 or 1 argument"); return;
        }
        /*
         * The factory to use for constructing coordinate systems.
         */
        CoordinateSystemFactory csFactory = CoordinateSystemFactory.getDefault();
        /*
         * Construct the source CoordinateSystem.     We will use a geographic coordinate
         * system,  i.e. one that use (latitude,longitude) coordinates.   Latitude values
         * are increasing north and longitude values area increasing east.  Angular units
         * are degrees and prime meridian is Greenwich.  Ellipsoid is WGS 84  (a commonly
         * used one for remote sensing data and GPS).      Note that the Geotools library
         * provides simpler ways to construct geographic coordinate systems using default
         * values for some arguments.  But we show here the complete way in order to show
         * the range of possibilities and to stay closer to the OpenGIS's specification.
         */
        Unit       angularUnit = Unit.DEGREE;
        HorizontalDatum  datum = HorizontalDatum.WGS84;
        PrimeMeridian meridian = PrimeMeridian.GREENWICH;
        GeographicCoordinateSystem sourceCS = csFactory.createGeographicCoordinateSystem(
                "My source CS", angularUnit, datum, meridian, AxisInfo.LATITUDE, AxisInfo.LONGITUDE);
        /*
         * Construct the target CoordinateSystem. We will use a projected coordinate
         * system, i.e. one that use linear (in metres) coordinates. We will use the
         * same ellipsoid than the source geographic coordinate system (i.e. WGS84).
         *
         * Note: The 'sourceCS' argument below is the geographic coordinate system
         *       to base projection on. It is also the source coordinate system in
         *       this particular case, but it may not alway be the case.
         */
        Unit       linearUnit = Unit.METRE;
        Ellipsoid   ellipsoid = datum.getEllipsoid();
        ParameterList  params = csFactory.createProjectionParameterList(classification);
        if (false) {
            // Set optional parameters here. This example set the false
            // easting and northing just for demonstration purpose.
            params.setParameter("false_easting",  1000.0);
            params.setParameter("false_northing", 1000.0);
        }
        Projection projection = csFactory.createProjection("My projection", classification, params);
        ProjectedCoordinateSystem targetCS = csFactory.createProjectedCoordinateSystem(
                "My target CS", sourceCS, projection, linearUnit, AxisInfo.X, AxisInfo.Y);
        /*
         * Now, we have built source and destination coordinate systems ('sourceCS'
         * and 'targetCS'). There is some observations about their relationships:
         *
         *   * We use the same ellipsoid (WGS 84) for both,  but it could as well be
         *     different. However, datum shift seems broken in current 'cts-coordtrans'
         *     implementation. It is safer to use the same datum for now.
         *
         *   * The axis order is inverted between the source (latitude,longitude)
         *     and the target (x,y).    This is up to the user to choose the axis
         *     order he want; Geotools should correctly swap them as needed. User
         *     could as well reverse axis orientation (e.g. make longitude values
         *     increasing West); Geotools should handle that correctly.
         *
         * Now, get the transformation.
         */
        CoordinateTransformationFactory trFactory = CoordinateTransformationFactory.getDefault();
        CoordinateTransformation transformation = trFactory.createFromCoordinateSystems(sourceCS, targetCS);
        /*
         * The CoordinateTransformation object contains information about
         * the transformation. It does not actually perform the transform
         * operations on points. In order to transform points, we must get
         * the math transform.
         *
         * Because source and target coordinate systems are both two-dimensional,
         * this transform object will actually be an instance of MathTransform2D.
         * The MathTransform2D interface is a Geotools's extension that is not part
         * of the OpenGIS's specification. This class provides additional methods
         * for interoperability with Java2D. If the user want to use it, he have to
         * cast the transform to MathTransform2D.
         */
        MathTransform transform = transformation.getMathTransform();
        /*
         * Now, read lines from the standard input, transform them,
         * and write the result to the standard output. Note: Java
         * is not very good for console application.  See many bug
         * reports (e.g. http://developer.java.sun.com/developer/bugParade/bugs/4071281.html).
         */
        System.out.print("Projection classification is ");
        System.out.println(classification);
        System.out.println("Enter (latitude longitude) coordinates separated by a space.");
        System.out.println("Enter \"exit\" to finish.");
        final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String line; while ((line=in.readLine()) != null) {
            line = line.trim();
            if (line.equalsIgnoreCase("exit")) {
                break;
            }
            int split = line.indexOf(' ');
            if (split >= 0) {
                double latitude  = Double.parseDouble(line.substring(0, split));
                double longitude = Double.parseDouble(line.substring(   split));
                CoordinatePoint point = new CoordinatePoint(latitude, longitude);
                point = transform.transform(point, point);
                System.out.println(point);
            }
        }
    }
}
