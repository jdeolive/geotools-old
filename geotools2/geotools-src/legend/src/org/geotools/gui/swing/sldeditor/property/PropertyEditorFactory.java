/*
 * Created on 15-feb-2004
 */
package org.geotools.gui.swing.sldeditor.property;

import org.geotools.factory.Factory;
import org.geotools.factory.FactoryConfigurationError;
import org.geotools.factory.FactoryFinder;
import org.geotools.feature.FeatureType;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public abstract class PropertyEditorFactory implements Factory {
    private static PropertyEditorFactory factory = null;
    protected boolean inExpertMode;

    /**
     * Create an instance of the factory.
     *
     * @return An instance of the Factory, or null if the Factory could not be
     *         created.
     *
     * @throws FactoryConfigurationError
     */
    public static PropertyEditorFactory createPropertyEditorFactory()
        throws FactoryConfigurationError {
        if (factory == null) {
            factory = (PropertyEditorFactory) FactoryFinder.findFactory("org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory",
                    "org.geotools.gui.swing.sldeditor.property.std.DefaultPropertyEditorFactory");
        }

        return factory;
    }

    public void setInExpertMode(boolean inExpertMode) {
        this.inExpertMode = inExpertMode;
    }

    public boolean isInExpertMode() {
        return inExpertMode;
    }

    public abstract ExpressionEditor createExpressionEditor(FeatureType featureType);

    public abstract FilterEditor createFilterEditor(FeatureType featureType);

    public abstract ExpressionEditor createColorEditor(FeatureType featureType);

    public abstract DashArrayEditor createDashArrayEditor();

    public abstract ExternalGraphicEditor createExternalGraphicEditor();

    public abstract ExpressionEditor createFeatureAttributeChooser(
        FeatureType featureType);

    public abstract FillEditor createFillEditor(FeatureType featureType);

    public abstract FillEditor createCompactFillEditor(FeatureType featureType);

    public abstract FontListChooser createFontListChooser();

    public abstract GeometryChooser createGeometryChooser(
        FeatureType featureType);

    public abstract GraphicEditor createGraphicEditor(FeatureType featureType);

    public abstract GraphicEditor createGraphicFillEditor(FeatureType featureType);

    public abstract GraphicEditor createGraphicStrokeEditor(FeatureType featureType);

    public abstract LabelPlacementEditor createLabelPlacementEditor(FeatureType featureType);

    public abstract MarkEditor createMarkEditor(FeatureType featureType);

    public abstract FeatureTypeChooser createFeatureTypeChooser(
        FeatureType featureType);

    public abstract ExpressionEditor createNumberEditor(Number startValue,
        Number minValue, Number maxValue, Number step, FeatureType featureType);

    public abstract ExpressionEditor createOpacityEditor(FeatureType featureType);

    public abstract ExpressionEditor createIntSizeEditor(FeatureType featureType);
    
    public abstract ExpressionEditor createDoubleEditor(FeatureType featureType);

    public abstract ExpressionEditor createRotationEditor(FeatureType featureType);
    
    public abstract ScaleEditor createScaleEditor();
    
    public abstract StrokeEditor createStrokeEditor(FeatureType featureType);
    
    public abstract SymbolEditor createSymbolEditor(FeatureType featureType);
}
