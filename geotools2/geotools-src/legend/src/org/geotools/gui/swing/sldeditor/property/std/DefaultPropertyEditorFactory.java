/*
 * Created on 15-feb-2004
 */
package org.geotools.gui.swing.sldeditor.property.std;

import org.geotools.feature.FeatureType;
import org.geotools.gui.swing.sldeditor.property.DashArrayEditor;
import org.geotools.gui.swing.sldeditor.property.ExpressionEditor;
import org.geotools.gui.swing.sldeditor.property.ExternalGraphicEditor;
import org.geotools.gui.swing.sldeditor.property.FeatureTypeChooser;
import org.geotools.gui.swing.sldeditor.property.FillEditor;
import org.geotools.gui.swing.sldeditor.property.FilterEditor;
import org.geotools.gui.swing.sldeditor.property.FontListChooser;
import org.geotools.gui.swing.sldeditor.property.GeometryChooser;
import org.geotools.gui.swing.sldeditor.property.GraphicEditor;
import org.geotools.gui.swing.sldeditor.property.LabelPlacementEditor;
import org.geotools.gui.swing.sldeditor.property.MarkEditor;
import org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory;
import org.geotools.gui.swing.sldeditor.property.ScaleEditor;
import org.geotools.gui.swing.sldeditor.property.StrokeEditor;
import org.geotools.gui.swing.sldeditor.property.SymbolEditor;
import org.geotools.gui.swing.sldeditor.util.FormUtils;


/**
 * DOCUMENT ME!
 *
 * @author wolf
 */
public class DefaultPropertyEditorFactory extends PropertyEditorFactory {
    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createExpressionEditor()
     */
    public ExpressionEditor createExpressionEditor(FeatureType featureType) {
        return new DefaultExpressionEditor(featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createFilterEditor()
     */
    public FilterEditor createFilterEditor(FeatureType featureType) {
        return new DefaultFilterEditor(featureType);
    }

    protected ExpressionEditor wrapExpert(ExpressionEditor editor, FeatureType featureType) {
        return new ExpressionEditorWrapper(editor, featureType, inExpertMode);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createColorEditor()
     */
    public ExpressionEditor createColorEditor(FeatureType featureType) {
        return wrapExpert(new DefaultColorEditor(), featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createDashArrayEditor()
     */
    public DashArrayEditor createDashArrayEditor() {
        return new DefaultDashArrayEditor();
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createExternalGraphicEditor()
     */
    public ExternalGraphicEditor createExternalGraphicEditor() {
        return new DefaultExternalGraphicEditor();
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createFeatureAttributeChooser(org.geotools.feature.FeatureType)
     */
    public ExpressionEditor createFeatureAttributeChooser(
        FeatureType featureType) {
        return wrapExpert(new DefaultFeatureAttributeChooser(featureType), featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createFillEditor()
     */
    public FillEditor createFillEditor(FeatureType featureType) {
        return new DefaultFillEditor(featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createCompactFillEditor()
     */
    public FillEditor createCompactFillEditor(FeatureType featureType) {
        return new DefaultCompactFillEditor(featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createFontListChooser()
     */
    public FontListChooser createFontListChooser() {
        return new DefaultFontListChooser();
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createGeometryChooser()
     */
    public GeometryChooser createGeometryChooser(FeatureType featureType) {
        return new DefaultGeometryChooser(featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createGraphicEditor()
     */
    public GraphicEditor createGraphicEditor(FeatureType featureType) {
        return new DefaultGraphicEditor(featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createGraphicFillEditor()
     */
    public GraphicEditor createGraphicFillEditor(FeatureType featureType) {
        return new DefaultGraphicFillEditor(FormUtils.getColorButtonDimension(),
            true, featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createGraphicStrokeEditor()
     */
    public GraphicEditor createGraphicStrokeEditor(FeatureType featureType) {
        return new DefaultGraphicFillEditor(FormUtils.getColorButtonDimension(),
            false, featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createLabelPlacementEditor()
     */
    public LabelPlacementEditor createLabelPlacementEditor(FeatureType featureType) {
        return new DefaultLabelPlacementEditor(featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createMarkEditor()
     */
    public MarkEditor createMarkEditor(FeatureType featureType) {
        return new DefaultMarkEditor(featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createFeatureTypeChooser()
     */
    public FeatureTypeChooser createFeatureTypeChooser(FeatureType featureType) {
        return new DefaultFeatureTypeChooser(featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createNumberEditor(java.lang.Number,
     *      java.lang.Number, java.lang.Number, java.lang.Number)
     */
    public ExpressionEditor createNumberEditor(Number startValue,
        Number minValue, Number maxValue, Number step, FeatureType featureType) {
        return wrapExpert(new DefaultNumberEditor(startValue, minValue, maxValue, step), featureType);
    }

    public ExpressionEditor createOpacityEditor(FeatureType featureType) {
        return wrapExpert(new DefaultNumberEditor(new Double(1.0), new Double(0.0),
            new Double(1.0), new Double(0.1)), featureType);
    }

    public ExpressionEditor createDoubleEditor(FeatureType featureType) {
        return wrapExpert(new DefaultNumberEditor(new Double(0.0),
            new Double(Double.MIN_VALUE), new Double(Double.MAX_VALUE),
            new Double(1)), featureType);
    }

    public ExpressionEditor createIntSizeEditor(FeatureType featureType) {
        return wrapExpert(new DefaultNumberEditor(new Integer(1), new Integer(0),
            new Integer(Integer.MAX_VALUE), new Integer(1)), featureType);
    }

    public ExpressionEditor createRotationEditor(FeatureType featureType) {
        DefaultNumberEditor editor = new DefaultNumberEditor(new Integer(0),
                new Integer(0), new Integer(360), new Integer(1),
                180.0 / Math.PI);
        editor.setCyclic(true);

        return wrapExpert(editor, featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createScaleEditor()
     */
    public ScaleEditor createScaleEditor() {
        return new DefaultScaleEditor();
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createStrokeEditor()
     */
    public StrokeEditor createStrokeEditor(FeatureType featureType) {
        return new DefaultStrokeEditor(featureType);
    }

    /**
     * @see org.geotools.gui.swing.sldeditor.property.PropertyEditorFactory#createSymbolEditor()
     */
    public SymbolEditor createSymbolEditor(FeatureType featureType) {
        return new DefaultSymbolEditor(featureType);
    }

}
