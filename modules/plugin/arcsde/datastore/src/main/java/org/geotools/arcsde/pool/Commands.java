package org.geotools.arcsde.pool;

import java.io.IOException;
import java.util.logging.Logger;

import org.geotools.arcsde.ArcSdeException;
import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeVersion;

public class Commands {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.data");

    /**
     * Command to fetch a version.
     * 
     * @author Gabriel Roldan
     */
    public static final class GetVersionCommand extends Command<SeVersion> {

        private String versionName;

        public GetVersionCommand(final String versionName) {
            this.versionName = versionName;
        }

        @Override
        public SeVersion execute(ISession session, SeConnection connection) throws SeException,
                IOException {

            final SeVersion version;
            try {
                version = new SeVersion(connection, versionName);
            } catch (SeException e) {
                if (e.getSeError().getSdeError() == -126) {
                    throw new ArcSdeException("Specified ArcSDE version does not exist: "
                            + versionName, e);
                } else {
                    throw e;
                }
            }
            version.getInfo();
            return version;
        }
    }
}
