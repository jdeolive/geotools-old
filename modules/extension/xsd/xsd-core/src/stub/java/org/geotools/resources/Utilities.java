package org.geotools.resources;

public class Utilities {

    public static boolean equals(final Object object1, final Object object2) {
        return (object1==object2) || (object1!=null && object1.equals(object2));
    }
}
