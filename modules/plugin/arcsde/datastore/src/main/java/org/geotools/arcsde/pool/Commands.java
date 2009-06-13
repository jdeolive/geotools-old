package org.geotools.arcsde.pool;

import java.io.IOException;
import java.util.logging.Logger;

import org.geotools.util.logging.Logging;

import com.esri.sde.sdk.client.SeConnection;
import com.esri.sde.sdk.client.SeException;
import com.esri.sde.sdk.client.SeVersion;

public class Commands {

    private static final Logger LOGGER = Logging.getLogger("org.geotools.arcsde.data");

    /**
     * Command to fetch a version.
     * <p>
     * Version _name_ is not a primary key in the {@code sde_versions} table so we're applying a
     * simple logic here:
     * <ul>
     * <li>If the required version name is {@link SeVersion#SE_QUALIFIED_DEFAULT_VERSION_NAME
     * qualified default version} name,then return the default version
     * <li>Otherwise, get the list of versions named {@code version name}, if there's more than one,
     * check if it's qualified by owner (like in <owner>.<version name>
     * </ul>
     * </p>
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
            version = new SeVersion(connection, versionName);
            version.getInfo();
            return version;
            // if (SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME.equals(versionName)) {
            // version = new SeVersion(connection, SeVersion.SE_QUALIFIED_DEFAULT_VERSION_NAME);
            // } else {
            // String where = "name = '" + versionName + "'";
            // SeVersion[] versionList = connection.getVersionList(where);
            // if (versionList == null || versionList.length == 0) {
            // /*
            // * hum, may we have a qualified table name?
            // */
            // final int qualifierIndex = versionName.indexOf('.');
            // if (qualifierIndex > 0) {
            // String user = versionName.substring(0, qualifierIndex);
            // String name = versionName.substring(qualifierIndex + 1);
            //
            // where = "name = '" + versionName + "'";
            // versionList = connection.getVersionList(where);
            // }
            // }
            // if (versionList == null || versionList.length == 0) {
            // throw new DataSourceException("Specified ArcSDE version does not exist: '"
            // + versionName + "'");
            // }
            // if (versionList.length > 1) {
            // throw new DataSourceException("There are more than one version named '"
            // + versionName
            // + "'. Try qualifying it with the owner <owner>.<version name>");
            // }
            // version = versionList[0];
            // LOGGER.info("Version " + versionName + " found. " + Arrays.toString(versionList));
            // }
            //
            // version.getInfo();
            // return version;
        }
    }
}
