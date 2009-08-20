package org.geotools.referencing.operation.projection;

import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.operation.projection.MapProjection.AbstractProvider;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.parameter.ParameterNotFoundException;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.operation.CylindricalProjection;
import org.opengis.referencing.operation.MathTransform;

public class MercatorPseudoProvider extends AbstractProvider {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 118002069939741891L;

    /**
     * The parameters group.
     */
    static final ParameterDescriptorGroup PARAMETERS = createDescriptorGroup(new NamedIdentifier[] {
            new NamedIdentifier(Citations.EPSG, "Popular Visualisation Pseudo Mercator"),  
            new NamedIdentifier (Citations.EPSG, "1024") 
        }, new ParameterDescriptor[] {
            SEMI_MAJOR, SEMI_MINOR,
            LATITUDE_OF_ORIGIN, CENTRAL_MERIDIAN, SCALE_FACTOR,
            FALSE_EASTING, FALSE_NORTHING
        });

    /**
     * Constructs a new provider.
     */
    public MercatorPseudoProvider() {
        super(PARAMETERS);
    }

    /**
     * Returns the operation type for this map projection.
     */
    @Override
    public Class<CylindricalProjection> getOperationType() {
        return CylindricalProjection.class;
    }

    /**
     * Creates a transform from the specified group of parameter values.
     *
     * @param  parameters The group of parameter values.
     * @return The created math transform.
     * @throws ParameterNotFoundException if a required parameter was not found.
     */
    protected MathTransform createMathTransform(final ParameterValueGroup parameters)
            throws ParameterNotFoundException
    {
     // make sure we assume a spherical reference
        parameters.parameter("semi_minor").setValue(parameters.parameter("semi_major").getValue());
        return new Mercator1SP.Spherical(parameters);
    }

}
