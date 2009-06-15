Upgrade notes
----------------------------------------------

When you need to upgrade the database:
- gunzip all the files
- overwrite the data, fkeys and tables scripts with the ones coming from EPSG 
  (the indexes one has been created to improve performance)
- gzip again all the files
- run the tests to make sure nothing broke