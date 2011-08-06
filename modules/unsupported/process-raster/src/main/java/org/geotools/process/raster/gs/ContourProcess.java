/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2011, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008-2011 TOPP - www.openplans.org.
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
 */
package org.geotools.process.raster.gs;

import org.jaitools.media.jai.contour.ContourDescriptor;
import org.jaitools.media.jai.contour.ContourRIF;
import org.jaitools.numeric.Range;

import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.process.gs.GSProcess;
import org.geotools.process.raster.CoverageUtilities;
import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.ViewType;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.image.jai.Registry;
import org.geotools.process.ProcessException;
import org.geotools.resources.i18n.Vocabulary;
import org.geotools.resources.i18n.VocabularyKeys;
import org.geotools.util.NumberRange;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.util.AffineTransformation;

/**
 * A process that wraps a {@link GridCoverage2D} as a collection of point feature.
 * 
 * @author Simone Giannecchini, GeoSolutions
 * 
 */
@DescribeProcess(title = "Contour", description = "Perform the contouring on a provided raster")
public class ContourProcess implements GSProcess {

    private static final InternationalString NO_DATA = Vocabulary
            .formatInternational(VocabularyKeys.NODATA);

    static {
        Registry.registerRIF(JAI.getDefaultInstance(), new ContourDescriptor(), new ContourRIF(),
                Registry.JAI_TOOLS_PRODUCT);
    }

    /**
     * Perform the contouring on a provided raster
     * 
     * @param data The raster to be used as the source
     * @param band The source image band to process
     * @param levels Values for which to generate contours
     * @param interval Interval between contour values (ignored if levels arg is supplied)
     * @param simplify Values for which to generate contours
     * @param smooth Values for which to generate contours
     * @param roi The geometry used to delineate the area of interest in model space
     * @return the contours feature collection
     * @throws ProcessException
     */
    public static SimpleFeatureCollection process(GridCoverage2D gc2d, Integer band,
            double[] levels, Double interval, Boolean simplify, Boolean smooth, Geometry roi,
            ProgressListener progressListener) throws ProcessException {
        ContourProcess process = new ContourProcess();
        return process.execute(gc2d, band, levels, interval, simplify, smooth, roi,
                progressListener);
    }

    @DescribeResult(name = "result", description = "The contours feature collection")
    public SimpleFeatureCollection execute(
            @DescribeParameter(name = "data", description = "The raster to be used as the source") GridCoverage2D gc2d,
            @DescribeParameter(name = "band", description = "The source image band to process", min = 0, max = 1) Integer band,
            @DescribeParameter(name = "levels", description = "Values for which to generate contours") double[] levels,
            @DescribeParameter(name = "interval", description = "Interval between contour values (ignored if levels arg is supplied)", min = 0) Double interval,
            @DescribeParameter(name = "simplify", description = "Values for which to generate contours", min = 0) Boolean simplify,
            @DescribeParameter(name = "smooth", description = "Values for which to generate contours", min = 0) Boolean smooth,
            @DescribeParameter(name = "roi", description = "The geometry used to delineate the area of interest in model space", min = 0) Geometry roi,
            ProgressListener progressListener) throws ProcessException {

        //
        // initial checks
        //
        if (gc2d == null) {
            throw new ProcessException("Invalid input, source grid coverage should be not null");
        }
        if (band != null && (band < 0 || band >= gc2d.getNumSampleDimensions())) {
            throw new ProcessException("Invalid input, invalid band number:" + band);
        }
        boolean hasValues = !(levels == null || levels.length == 0);
        if (!hasValues && interval == null) {
            throw new ProcessException("One between interval and values must be valid");

        }

        // switch to geophisics if necessary
        gc2d = gc2d.view(ViewType.GEOPHYSICS);

        //
        // GRID TO WORLD preparation
        //
        final AffineTransform mt2D = (AffineTransform) gc2d.getGridGeometry().getGridToCRS2D(
                PixelOrientation.CENTER);

        // get the list of nodata, if any
        List<Object> noDataList = new ArrayList<Object>();
        for (GridSampleDimension sd : gc2d.getSampleDimensions()) {
            // grab all the explicit nodata
            final double[] sdNoData = sd.getNoDataValues();
            if (sdNoData != null) {
                for (double nodata : sdNoData) {
                    noDataList.add(nodata);
                }
            }

            // handle also readers setting up nodata in a category with a specific name
            if (sd.getCategories() != null) {
                for (Category cat : sd.getCategories()) {
                    if (cat.getName().equals(NO_DATA)) {
                        final NumberRange<? extends Number> catRange = cat.getRange();
                        if (catRange.getMinimum() == catRange.getMaximum()) {
                            noDataList.add(catRange.getMinimum());
                        } else {
                            Range<Double> noData = new Range<Double>(catRange.getMinimum(),
                                    catRange.isMinIncluded(), catRange.getMaximum(),
                                    catRange.isMaxIncluded());
                            noDataList.add(noData);
                        }
                    }
                }
            }
        }

        // get the rendered image
        final RenderedImage raster = gc2d.getRenderedImage();

        // perform jai operation
        ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
        pb.setSource("source0", raster);

        if (roi != null) {
            pb.setParameter("roi", CoverageUtilities.prepareROI(roi, mt2D));
        }
        if (band != null) {
            pb.setParameter("band", band);
        }
        if (interval != null) {
            pb.setParameter("interval", interval);
        } else {
            final ArrayList<Double> elements = new ArrayList<Double>(levels.length);
            for (double level : levels)
                elements.add(level);
            pb.setParameter("levels", elements);
        }
        if (simplify != null) {
            pb.setParameter("simplify", simplify);
        }
        if (smooth != null) {
            pb.setParameter("smooth", smooth);
        }
        if (noDataList != null) {
            pb.setParameter("nodata", noDataList);
        }

        final RenderedOp dest = JAI.create("Contour", pb);
        @SuppressWarnings("unchecked")
        final Collection<LineString> prop = (Collection<LineString>) dest
                .getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);

        // wrap as a feature collection and return
        final SimpleFeatureType schema = CoverageUtilities
                .createFeatureType(gc2d, LineString.class);
        final SimpleFeatureBuilder builder = new SimpleFeatureBuilder(schema);
        int i = 0;
        final ListFeatureCollection featureCollection = new ListFeatureCollection(schema);
        final AffineTransformation jtsTransformation = new AffineTransformation(mt2D.getScaleX(),
                mt2D.getShearX(), mt2D.getTranslateX(), mt2D.getShearY(), mt2D.getScaleY(),
                mt2D.getTranslateY());
        for (LineString line : prop) {

            // get value
            Double value = (Double) line.getUserData();
            line.setUserData(null);
            // filter coordinates in place
            line.apply(jtsTransformation);

            // create feature and add to list
            builder.set("the_geom", line);
            builder.set("value", value);

            featureCollection.add(builder.buildFeature(String.valueOf(i++)));

        }

        // return value

        return featureCollection;

    }

}
