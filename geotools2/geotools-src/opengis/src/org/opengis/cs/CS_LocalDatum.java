package org.opengis.cs;
import org.opengis.pt.*;

/** Local datum.
 *  If two local datum objects have the same datum type and name, then they
 *  can be considered equal.  This means that coordinates can be transformed
 *  between two different local coordinate systems, as long as they are based
 *  on the same local datum.
 */
public interface CS_LocalDatum extends CS_Datum
{
}
