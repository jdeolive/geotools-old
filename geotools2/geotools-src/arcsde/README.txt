This is the readme file for the ArcSDE DataStore of the geotools2 project.

Unit testing the ArcSDE DataSource over a live database is disabled
due to the lack of a publicly available one.

If you have a live ArcSDE database and want to run the
unit tests over it, remove or comment the line
<exclude>**/SdeDataStoreTest.java</exclude>
on project.xml and see tests/unit/testData/testparams.properties
for instructions on setting up testing and connection options.

For any comment, suggestion or contribution to this code,
email to the geotools-devel@sourceforge.net mailing list
or contact me directly at groldan@users.sourceforge.net

Gabriel Roldán.
