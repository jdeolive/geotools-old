DROP TABLE ora_test_points;
DROP TABLE ora_test_lines;
DROP TABLE ora_test_polygons;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'ORA_TEST_POINTS';
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'ORA_TEST_POLYGONS';
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME = 'ORA_TEST_LINES';


CREATE TABLE ora_test_points (
    name    VARCHAR(255),
    intval  NUMBER,
    id      NUMBER  PRIMARY KEY,
    shape   MDSYS.SDO_GEOMETRY
);
    

CREATE TABLE ora_test_lines (
    name    VARCHAR(255),
    intval  NUMBER,
    id      NUMBER  PRIMARY KEY,
    shape   MDSYS.SDO_GEOMETRY
);


CREATE TABLE ora_test_polygons (
    name    VARCHAR(255),
    intval  NUMBER,
    id      NUMBER  PRIMARY KEY,
    shape   MDSYS.SDO_GEOMETRY
);


INSERT INTO USER_SDO_GEOM_METADATA VALUES (
    'ORA_TEST_POINTS',
    'SHAPE',
    MDSYS.SDO_DIM_ARRAY(
        MDSYS.SDO_DIM_ELEMENT('X',-180,180,0.005),
        MDSYS.SDO_DIM_ELEMENT('Y',-90,90,0.005)
    ),
    NULL
);



INSERT INTO USER_SDO_GEOM_METADATA VALUES (
    'ORA_TEST_LINES',
    'SHAPE',
    MDSYS.SDO_DIM_ARRAY(
        MDSYS.SDO_DIM_ELEMENT('X',-180,180,0.005),
        MDSYS.SDO_DIM_ELEMENT('Y',-90,90,0.005)
    ),
    NULL
);



INSERT INTO USER_SDO_GEOM_METADATA VALUES (
    'ORA_TEST_POLYGONS',
    'SHAPE',
    MDSYS.SDO_DIM_ARRAY(
        MDSYS.SDO_DIM_ELEMENT('X',-180,180,0.005),
        MDSYS.SDO_DIM_ELEMENT('Y',-90,90,0.005)
    ),
    NULL
);





INSERT INTO ora_test_points VALUES (
    'point 1',
    10,
    1,    
    MDSYS.SDO_GEOMETRY(
        2001,
        NULL,
        NULL,
        MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),
        MDSYS.SDO_ORDINATE_ARRAY(10,10) 
    )
);



INSERT INTO ora_test_points VALUES (
    'point 2',
    20,
    2,
    MDSYS.SDO_GEOMETRY(
        2001,
        NULL,
        NULL,
        MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),
        MDSYS.SDO_ORDINATE_ARRAY(20,10)
    )
);



INSERT INTO ora_test_points VALUES (
    'point 3',
    30,
    3,
    MDSYS.SDO_GEOMETRY(
        2001,
        NULL,
        NULL,
        MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),
        MDSYS.SDO_ORDINATE_ARRAY(20,30)
    )
);


INSERT INTO ora_test_points VALUES (
    'point 4',
    40,
    4,
    MDSYS.SDO_GEOMETRY(
        2001,
        NULL,
        NULL,
        MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),
        MDSYS.SDO_ORDINATE_ARRAY(30,10)
    )
);



INSERT INTO ora_test_points VALUES (
    'point 5',
    50,
    5,
    MDSYS.SDO_GEOMETRY(
        2001,
        NULL,
        NULL,
        MDSYS.SDO_ELEM_INFO_ARRAY(1,1,1),
        MDSYS.SDO_ORDINATE_ARRAY(-20,10)
    )
);



create index test_point_index on ORA_TEST_POINTS(SHAPE) INDEXTYPE IS MDSYS.SPATIAL_INDEX;