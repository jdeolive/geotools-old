/*
 * Simple demonstration program for CTS module of Geotools.
 */

// Geotools dependencies
import org.geotools.pt.*;
import org.geotools.cs.*;
import org.geotools.ct.*;

// Other packages
import java.io.*;
import org.geotools.units.*;


/**
 * An example of application reading points from the
 * standard input, transforming them and writting the
 * result to the standard output.
 *
 * @version 1.0
 * @author Martin Desruisseaux
 */
public class TransformationConsole {
    /**
     * The program main entry point.
     *
     * @param  args Array of command-line arguments. This small demo accept only
     *              one argument:   the classification name of the projection to
     *              perform. The default value is "Mercator_1SP". The list of
     *              supported classification name is available at the following
     *              adress:
     *
     *              http://seagis.sourceforge.net/javadoc/seagis/net/seagis/ct/package-summary.html
     *
     * @throws IOException if an error occured while reading the input stream.
     * @throws FactoryException if a coordinate system can't be constructed.
     * @throws TransformException if a transform failed.
     */
    public static void main(String[] args) throws IOException, FactoryException, TransformException {
        //
        // Check command-line arguments.
        //
        String classification;
        switch (args.length) {
            case  0: classification = "Mercator_1SP"; break;
            case  1: classification = args[0]; break;
            default: System.err.println("Expected 0 or 1 argument"); return;
        }
        //
        // Construct the source CoordinateSystem.    We will use a geographic coordinate
        // system,  i.e. one that use (latitude,longitude) coordinates.  Latitude values
        // are increasing north and longitude values area increasing east. Angular units
        // are degrees and prime meridian is Greenwich.  Ellipsoid is WGS 84 (a commonly
        // used one with remote sensing data and with GPS). Note that the SeaGIS library
        // provide simpler ways to construct geographic coordinate systems using default
        // values for some arguments. But we show here the complete way in order to show
        // the range of possibilities and to stay closer to the OpenGIS's specification.
        //
        Unit       angularUnit = Unit.DEGREE;
        HorizontalDatum  datum = HorizontalDatum.WGS84;
        PrimeMeridian meridian = PrimeMeridian.GREENWICH;
        
        CoordinateSystemFactory   csFactory = CoordinateSystemFactory.getDefault();
        GeographicCoordinateSystem sourceCS = csFactory.createGeographicCoordinateSystem(
                "My source CS", angularUnit, datum, meridian, AxisInfo.LATITUDE, AxisInfo.LONGITUDE);
        //
        // Construct the target CoordinateSystem. We will use a projected coordinate
        // system, i.e. one that use linear (in metres) coordinates. We will use the
        // same ellipsoid than the source geographic coordinate system (i.e. WGS84).
        // Note that we use here a convenience constructor for the Projection object;
        // the full OpenGIS specification would require us to construct an array of
        // parameters, which is more powerfull but not needed for this small demo.
        //
        // Note: The 'sourceCS' argument below is the geographic coordinate system
        //       to base projection on. It is also the source coordinate system in
        //       this particular case, but it may not alway be the case.
        //
        Unit       linearUnit = Unit.METRE;
        Ellipsoid   ellipsoid = datum.getEllipsoid();
        Projection projection = csFactory.createProjection(
                "My projection", classification, ellipsoid, null, null);
        
        ProjectedCoordinateSystem targetCS = csFactory.createProjectedCoordinateSystem(
                "My target CS", sourceCS, projection, linearUnit, AxisInfo.X, AxisInfo.Y);
        //
        // Now, we have build source and destination coordinate systems ('sourceCS'
        // and 'targetCS'). There is some observations about their relationships:
        //
        //   * We use the same ellipsoid (WGS 84) for both,  but it could as well
        //     be different. However, conversions between different ellipsoids is
        //     not yet implemented in SeaGIS; this is why we have to use the same
        //     one.
        //
        //   * The axis order is inverted between the source (latitude,longitude)
        //     and the target (x,y).    This is up to the user to choose the axis
        //     order he want; the SeaGIS implementation should correctly swap them
        //     as needed.   User could as well reverse axis orientation (e.g. make
        //     longitude values increasing West); the SeaGIS implementation should
        //     handle that correctly.
        //
        // Now, get the transformation.
        //
        CoordinateTransformationFactory trFactory = CoordinateTransformationFactory.getDefault();
        CoordinateTransformation transformation = trFactory.createFromCoordinateSystems(sourceCS, targetCS);
        //
        // The CoordinateTransformation object contains information about
        // the transformation. It does not actually perform the transform
        // operations on points. In order to transform points, we must get
        // the math transform.
        //
        // Because source and target coordinate systems are both two-dimensional,
        // this transform object will actually be an instance of MathTransform2D.
        // The MathTransform2D class is an addition to SeaGIS that is not part of
        // the OpenGIS's specification. This class provides additional methods for
        // interoperability with Java2D. If the user want to use it, he have to
        // cast the transform to MathTransform2D.
        //
        MathTransform transform = transformation.getMathTransform();
        //
        // Now, read lines from the standard input, transform them,
        // and write the result to the standard output. Note: Java
        // is not very good for console application.  See many bug
        // reports (e.g. http://developer.java.sun.com/developer/bugParade/bugs/4071281.html).
        //
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
