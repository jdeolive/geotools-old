/**
 * 
 */
package org.geotools.data.shapefile;

/**
 * 
 * @author jesse
 * @param <V>
 *                The type of value that the result is
 * @param <S>
 *                The state of the return for example this may be a an enum that
 *                Provides state values such as NONE, FAILURE, etc..
 */
public class Result<V, S> {

    public final V value;
    public final S state;

    public Result(V value, S state) {
        this.value = value;
        this.state = state;
    }

    @Override
    public String toString() {
        return "State: " + state + " value: " + value;
    }
}
