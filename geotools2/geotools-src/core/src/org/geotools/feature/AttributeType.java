package org.geotools.feature;

/**
 * <p>Stores metadata about a single attribute object.  Note that schemas are 
 * also attributes.  All attribute implementations must be immutable.
 * 
 * All attributes must have four properties:<ol>
 * <li>Name: A string that is used to reference the attribute.</li>
 * <li>Occurences: Number of instances of this attribute per <code>Feature</code></li> 
 * <li>Type: The expected Java class of this attribute.</li>
 * <li>Position: All schemas are ordered, so they store the associated position
 * of the attribute.</li></ol></p>
 *
 * @author Rob Hranac, VFNY
 */
public interface AttributeType {
        
    /**
     * Sets the position of this attribute in the schema.
     * 
     * @param position Position of attribute.
     * @return Copy of attribute with modified position.
     */
    public AttributeType setPosition(int position);

    /**
     * Whether or not this attribute is a schema.
     * 
     * @return True if schema.
     */
    public boolean isNested();
    
    /**
     * Gets the name of this attribute.
     * 
     * @return Name.
     */
    public String getName();
    
    /**
     * Gets the type of this attribute.
     * 
     * @return Type.
     */
    public Class getType();
    
    /**
     * Gets the occurences of this attribute.
     * 
     * @return Occurrences.
     */
    public int getOccurrences();
    
    /**
     * Gets the position of this attribute.
     * 
     * @return Position.
     */
    public int getPosition();    
}
