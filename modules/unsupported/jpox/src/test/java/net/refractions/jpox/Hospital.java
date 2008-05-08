package net.refractions.jpox;

import com.vividsolutions.jts.geom.Geometry;

/*
CREATE TABLE bc_hospitals
(
  gid serial NOT NULL,
  id int4,
  authority varchar,
  name varchar,
  the_geom geometry,
  CONSTRAINT bc_hospitals_pkey PRIMARY KEY (gid),
  CONSTRAINT enforce_dims_the_geom CHECK (ndims(the_geom) = 2),
  CONSTRAINT enforce_geotype_the_geom CHECK (geometrytype(the_geom) = 'POINT'::text OR the_geom IS NULL),
  CONSTRAINT enforce_srid_the_geom CHECK (srid(the_geom) = 3005)
) 
 */

public class Hospital {

	private Integer gid;
	
	private Integer id;

	private String authority;

	private String name;

	private Geometry the_geom;


	public String getAuthority() {
		return authority;
	}
	
	public Integer getGid() {
		return gid;
	}
	
	public Integer getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public Geometry getGeometry() {
		return the_geom;
	}
	
	public String toString() {
		return "gid = " + gid + " / id = " + id + " / authority = " + authority + " / the_geom = " + the_geom;
	}

}
