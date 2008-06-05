/*
 *    GeoTools - The Open Source Java GIS Tookit
 *    http://geotools.org
 * 
 *    (C) 2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotools.resources.jaxb.code;

import javax.xml.bind.annotation.XmlElement;
import org.geotools.resources.jaxb.metadata.MetadataAdapter;
import org.opengis.metadata.identification.TopicCategory;


/**
 * JAXB adapter for {@link TopicCategory}, in order to integrate the value in a tags
 * respecting the ISO-19139 standard. See package documentation to have more information
 * about the handling of CodeList in ISO-19139.
 *
 * @since 2.5
 * @source $URL$
 * @author Cédric Briançon
 */
public final class TopicCategoryAdapter extends MetadataAdapter<TopicCategoryAdapter, TopicCategory> {
    /**
     * Empty constructor for JAXB only.
     */
    private TopicCategoryAdapter() {
    }

    public TopicCategoryAdapter(final TopicCategory topicCategory) {
        super(topicCategory);
    }

    protected TopicCategoryAdapter wrap(TopicCategory proxy) {
        return new TopicCategoryAdapter(proxy);
    }

    /**
     * Returns the {@link AddressImpl} generated from the metadata value.
     * This method is systematically called at marshalling-time by JAXB.
     */
    @XmlElement(name = "MD_TopicCategoryCode")
    public String getTopicCategory() {
        return metadata.identifier();
    }

    /**
     * Sets the value for the {@link AddressImpl}. This method is systematically
     * called at unmarshalling-time by JAXB.
     */
    public void setTopicCategory(final String topicCategory) {
        this.metadata = TopicCategory.valueOf(topicCategory);
    }
}
