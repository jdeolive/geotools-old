/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
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
package org.geotools.metadata;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlSeeAlso;

import org.opengis.metadata.citation.ResponsibleParty;
import org.opengis.metadata.distribution.Distributor;
import org.opengis.metadata.identification.CharacterSet;
import org.opengis.metadata.identification.Identification;
import org.opengis.metadata.spatial.DimensionNameType;

import org.geotools.metadata.iso.MetaDataImpl;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.metadata.iso.citation.ResponsiblePartyImpl;
import org.geotools.metadata.iso.distribution.DistributionImpl;
import org.geotools.metadata.iso.distribution.DistributorImpl;
import org.geotools.metadata.iso.identification.DataIdentificationImpl;
import org.geotools.metadata.iso.identification.IdentificationImpl;
import org.geotools.metadata.iso.spatial.DimensionImpl;
import org.geotools.metadata.iso.spatial.GridSpatialRepresentationImpl;
import org.geotools.test.Dummy;
import org.geotools.util.GrowableInternationalString;

import org.junit.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;


/**
 * A test class for annotations written in the Metadata module.
 * First, it marshalls all annotations in a XML temporary file, starting with the
 * {@link MetadataImpl} class as root element. Then, the temporary XML file is
 * unmarshalled, in order to get a {@linkplain MetadataImpl metadata} object.
 * Finally some fields of this object are compared with the original value.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class MetadataAnnotationsTest {
    /**
     * Ensure that the version of JAXB used is high enough to do all the process of the
     * marshalling. JAXB has to handle the {@link XmlSeeAlso} annotation. If it can't
     * then this annotation will not be present on class like {@link IdentificationImpl},
     * for which we know that it contains this annotation.
     * <p>
     * Test the presence of JAXB in the endorsed folder as well. Since JAXB jars, which
     * can be downloaded on the JAXB website, have a different package structure than the
     * one present in the JDK6, we disable the use of the endorsed process for the test.
     *
     * @return True if the JAXB present in the user configuration is able to do the
     *         following tests. False otherwise.
     */
    private static boolean ensuresCorrectJaxbVersion() {
        if (IdentificationImpl.class.getAnnotation(XmlSeeAlso.class) == null) {
            return false;
        }
        try {
            Class c = Class.forName("com.sun.xml.bind.marshaller.NamespacePrefixMapper");
            return false;
        } catch (ClassNotFoundException e) {
            /*
             * Ensure that this class is not present in the classpath. If this class is
             * present, it means that the user has put an other version of JAXB in the
             * endorsed folder of his jdk, and the test is then stopped since we can't
             * know whether the version is correct or not.
             */
            return true;
        }
    }

    /**
     * Generates an XML tree from the annotations on the class MetaDataImpl, and
     * write it into a temporary file. This file is then red by the unmarshaller.
     * Some assertions about the validity of the data red are done.
     *
     * @throws JAXBException If an error occurs during the creation of the JAXB context,
     *                       or during marshalling / unmarshalling processes.
     * @throws IOException If a writing error in the temporary file occurs.
     */
    @Test
    public void testMetadataAnnotations() throws JAXBException, IOException {
        // First ensures that user's jdk contains a correct version of JAXB.
        // The {@code XmlSeeAlso} annotation appears in version 2.1 of JAXB,
        // that's why it is tested.
        /*assumeTrue(ensuresCorrectJaxbVersion());
        final NamespacePrefixMapperImpl defaultNamespace =
                new NamespacePrefixMapperImpl("http://www.isotc211.org/2005/gmd");
        if (defaultNamespace instanceof Dummy) {
            return;
        }
        final JAXBContext context = JAXBContext.newInstance(MetaDataImpl.class);
        final Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        marshaller.setProperty("com.sun.xml.internal.bind.namespacePrefixMapper",
                defaultNamespace);
        final File tempXml = File.createTempFile("jaxb", ".tmp");
        tempXml.deleteOnExit();
        //
        // Fill metadata values.
        //
        final MetaDataImpl metadata = new MetaDataImpl();
        metadata.setLanguage(Locale.FRENCH);
        metadata.setCharacterSet(CharacterSet.UTF_8);
        metadata.setDateStamp(new Date());
        metadata.setContacts(Arrays.asList(new ResponsibleParty[] {
            ResponsiblePartyImpl.GEOTOOLS, ResponsiblePartyImpl.OPEN_GIS
        }));
        final DataIdentificationImpl dataIdent = new DataIdentificationImpl();
        dataIdent.setCitation(Citations.GEOTOOLS);
        //dataIdent.setAbstract(new SimpleInternationalString("Geotools, OpenSource Project"));
        final GrowableInternationalString growableString = new GrowableInternationalString();
        growableString.add(Locale.ENGLISH, "Geotools, OpenSource Project");
        growableString.add(Locale.FRENCH, "Geotools, projet OpenSource");
        growableString.add(Locale.ITALIAN, "Geotools, progetto OpenSource");
        dataIdent.setAbstract(growableString);
        dataIdent.setLanguage(Arrays.asList(new Locale[] {
            Locale.FRENCH
        }));
        metadata.setIdentificationInfo(Arrays.asList(new Identification[] {
            dataIdent
        }));
        final DimensionImpl dimension = new DimensionImpl();
        dimension.setDimensionName(DimensionNameType.COLUMN);
        dimension.setDimensionSize(830);
        dimension.setResolution(new Double(70.5));
        final GridSpatialRepresentationImpl gridSpatialRepres = new GridSpatialRepresentationImpl();
        gridSpatialRepres.setAxisDimensionsProperties(Arrays.asList(dimension));
        metadata.setSpatialRepresentationInfo(Arrays.asList(gridSpatialRepres));
        final DistributionImpl distrib = new DistributionImpl();
        distrib.setDistributors(Arrays.asList(new Distributor[] {
            new DistributorImpl(ResponsiblePartyImpl.GEOTOOLS)
        }));
        metadata.setDistributionInfo(distrib);
        final FileWriter writer = new FileWriter(tempXml);
        //
        // Write in output file.
        //
        marshaller.marshal(metadata, writer);
        writer.close();
        // Verify that something has been written in the xml file.
        assertFalse(tempXml.length() == 0);
        //
        // Read in the xml file.
        //
        final Unmarshaller unmarshaller = context.createUnmarshaller();
        final Object obj = unmarshaller.unmarshal(tempXml);
        //
        // Validation tests
        //
        assertNotNull(obj);
        if (obj instanceof MetaDataImpl) {
            final MetaDataImpl dataUnmarsh = (MetaDataImpl) obj;
            assertEquals(dataUnmarsh.getCharacterSet(), metadata.getCharacterSet());
            assertEquals(dataUnmarsh.getLanguage(), metadata.getLanguage());
            assertEquals(dataUnmarsh.getIdentificationInfo().iterator().next().getAbstract(),
                         metadata.getIdentificationInfo().iterator().next().getAbstract());
        } else {
            fail("The unmarshalled object gotten from the XML file marshalled is not " +
                    "an instance of MetaDataImpl. So the unmarshalling has failed on this XML file.");
        }*/
    }
    
}
