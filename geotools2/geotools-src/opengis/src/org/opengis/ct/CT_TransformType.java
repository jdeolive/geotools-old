package org.opengis.ct;

/** Semantic type of transform used in coordinate transformation.
 */
public class CT_TransformType
{
    public int value;

    /** Unknown or unspecified type of transform.
     */
    public static final int CT_TT_Other=0;

    /** Transform depends only on defined parameters.
     *  For example, a cartographic projection.
     */
    public static final int CT_TT_Conversion=1;

    /** Transform depends only on empirically derived parameters.
     *  For example a datum transformation.
     */
    public static final int CT_TT_Transformation=2;

    /** Transform depends on both defined and empirical parameters.
     */
    public static final int CT_TT_ConversionAndTransformation=3;
}

