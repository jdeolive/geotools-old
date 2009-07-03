package org.geotools.arcsde.session;

import java.io.IOException;
import java.util.ArrayList;

import org.geotools.arcsde.ArcSdeException;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeVersion;

public class Commands {

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
            } catch (SeException cause) {

                if (cause.getSeError().getSdeError() == -126) {
                    ArrayList<String> available = new ArrayList<String>();
                    try {
                        SeVersion[] versionList = connection.getVersionList(null);
                        for (SeVersion v : versionList) {
                            available.add(v.getName());
                        }
                        throw new ArcSdeException("Specified ArcSDE version does not exist: "
                                + versionName + ". Available versions are: " + available, cause);
                    } catch (SeException ignorable) {
                        // hum... ignore
                        throw new ArcSdeException("Specified ArcSDE version does not exist: "
                                + versionName, cause);
                    }
                } else {
                    throw cause;
                }
            }
            version.getInfo();
            return version;
        }
    }
}
