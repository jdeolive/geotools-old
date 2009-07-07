package org.geotools.coverage.io.range.impl;

import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.util.List;

import javax.measure.quantity.Dimensionless;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.operator.BandMergeDescriptor;
import javax.media.jai.operator.ConstantDescriptor;

import org.geotools.coverage.io.impl.range.DimensionlessAxisBin;
import org.geotools.coverage.io.impl.range.IMAGE_PROCESSING_ELEMENTS;
import org.geotools.coverage.io.range.Axis;
import org.geotools.coverage.io.range.FieldType;
import org.junit.Test;
/**
 * Tests for the axis class and its related classes
 * 
 * @author Simone Giannecchini, GeoSolutions
 *
 */
public class SyntheticDataTest extends org.junit.Assert{
	
	public static RenderedImage band= ConstantDescriptor.create(512.0f, 512.0f, new Byte[]{0}, null);

	@Test
	public void testAxis(){
		final Axis<Dimensionless> axis = IMAGE_PROCESSING_ELEMENTS.SYNTHETIC_COLOR_AXIS;
		assertEquals(axis, IMAGE_PROCESSING_ELEMENTS.SYNTHETIC_COLOR_AXIS);
		final Axis<Dimensionless> axis2= new Axis<Dimensionless>(
				axis.getName(),
				axis.getDescription(),
				axis.getUnitOfMeasure());
		assertEquals(axis, axis2);
		
	}
	@Test
	public void testAxisBin(){
		
		// get the bin for the single gray band
		List<DimensionlessAxisBin> bins = IMAGE_PROCESSING_ELEMENTS.getBinsFromRenderedImage(band);
		assertEquals(1, bins.size());
		// we should be able to get the same bin using the enum keys
		assertEquals(IMAGE_PROCESSING_ELEMENTS.GRAY.getAxisBin(), bins.get(0));
		
		// now two bands
		final RenderedImage twoBands= BandMergeDescriptor.create(band, band, null);
		bins = IMAGE_PROCESSING_ELEMENTS.getBinsFromRenderedImage(twoBands);
		assertEquals(2, bins.size());
		// we should be able to get the same bin using the enum keys
		assertEquals(IMAGE_PROCESSING_ELEMENTS.GRAY.getAxisBin(), bins.get(0));
		// we should be able to get the same bin using the enum keys
		assertEquals(IMAGE_PROCESSING_ELEMENTS.ALPHA.getAxisBin(), bins.get(1));
		
		//RGB
		final ImageLayout layout= new ImageLayout();
		layout.setColorModel(
				new ComponentColorModel(
						ColorSpace.getInstance(ColorSpace.CS_sRGB),
						false,
						false,
						Transparency.OPAQUE,
						DataBuffer.TYPE_BYTE
				)
			);
		final RenderingHints hints= new RenderingHints(JAI.KEY_IMAGE_LAYOUT,layout);
		final RenderedImage rgb= BandMergeDescriptor.create(band, twoBands, hints);
		bins = IMAGE_PROCESSING_ELEMENTS.getBinsFromRenderedImage(rgb);
		assertEquals(3, bins.size());
		// we should be able to get the same bin using the enum keys
		assertEquals(IMAGE_PROCESSING_ELEMENTS.RED.getAxisBin(), bins.get(0));
		// we should be able to get the same bin using the enum keys
		assertEquals(IMAGE_PROCESSING_ELEMENTS.GREEN.getAxisBin(), bins.get(1));
		// we should be able to get the same bin using the enum keys
		assertEquals(IMAGE_PROCESSING_ELEMENTS.BLUE.getAxisBin(), bins.get(2));		
		
	}
	
	@Test
	public void testFieldType(){
		
		// get the bin for the single gray band
		final FieldType<String, Dimensionless> fieldType = IMAGE_PROCESSING_ELEMENTS.getFieldTypeFromRenderedImage(band);
		assertNotNull(fieldType.getAxes());
		assertEquals(1,fieldType.getAxes().size());
		assertEquals(IMAGE_PROCESSING_ELEMENTS.SYNTHETIC_COLOR_AXIS,fieldType.getAxes().iterator().next());
		System.out.println(fieldType);
	}
}
