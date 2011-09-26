package org.geotools.data.ogr.bridj;
import org.bridj.Pointer;
import org.bridj.StructObject;
import org.bridj.ann.Field;
import org.bridj.ann.Library;
import org.bridj.ann.Union;
import org.geotools.data.ogr.bridj.OgrLibrary.GByte;
/**
 * <i>native declaration : /home/aaime/devel/gdal/gdal-1.8.0/ogr/ogr_core.h:571</i><br>
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> or <a href="http://bridj.googlecode.com/">BridJ</a> .
 */
@Union 
@Library("ogr") 
public class OGRField extends StructObject {
	public OGRField() {
		super();
	}
	public OGRField(Pointer pointer) {
		super(pointer);
	}
	@Field(0) 
	public int Integer() {
		return this.io.getIntField(this, 0);
	}
	@Field(0) 
	public OGRField Integer(int Integer) {
		this.io.setIntField(this, 0, Integer);
		return this;
	}
	@Field(1) 
	public double Real() {
		return this.io.getDoubleField(this, 1);
	}
	@Field(1) 
	public OGRField Real(double Real) {
		this.io.setDoubleField(this, 1, Real);
		return this;
	}
	/// C type : char*
	@Field(2) 
	public Pointer<Byte > String() {
		return this.io.getPointerField(this, 2);
	}
	/// C type : char*
	@Field(2) 
	public OGRField String(Pointer<Byte > String) {
		this.io.setPointerField(this, 2, String);
		return this;
	}
	/// C type : IntegerList_struct
	@Field(3) 
	public OGRField.IntegerList_struct IntegerList() {
		return this.io.getNativeObjectField(this, 3);
	}
	/// C type : IntegerList_struct
	@Field(3) 
	public OGRField IntegerList(OGRField.IntegerList_struct IntegerList) {
		this.io.setNativeObjectField(this, 3, IntegerList);
		return this;
	}
	/// C type : RealList_struct
	@Field(4) 
	public OGRField.RealList_struct RealList() {
		return this.io.getNativeObjectField(this, 4);
	}
	/// C type : RealList_struct
	@Field(4) 
	public OGRField RealList(OGRField.RealList_struct RealList) {
		this.io.setNativeObjectField(this, 4, RealList);
		return this;
	}
	/// C type : StringList_struct
	@Field(5) 
	public OGRField.StringList_struct StringList() {
		return this.io.getNativeObjectField(this, 5);
	}
	/// C type : StringList_struct
	@Field(5) 
	public OGRField StringList(OGRField.StringList_struct StringList) {
		this.io.setNativeObjectField(this, 5, StringList);
		return this;
	}
	/// C type : Binary_struct
	@Field(6) 
	public OGRField.Binary_struct Binary() {
		return this.io.getNativeObjectField(this, 6);
	}
	/// C type : Binary_struct
	@Field(6) 
	public OGRField Binary(OGRField.Binary_struct Binary) {
		this.io.setNativeObjectField(this, 6, Binary);
		return this;
	}
	/// C type : Set_struct
	@Field(7) 
	public OGRField.Set_struct Set() {
		return this.io.getNativeObjectField(this, 7);
	}
	/// C type : Set_struct
	@Field(7) 
	public OGRField Set(OGRField.Set_struct Set) {
		this.io.setNativeObjectField(this, 7, Set);
		return this;
	}
	/// C type : Date_struct
	@Field(8) 
	public OGRField.Date_struct Date() {
		return this.io.getNativeObjectField(this, 8);
	}
	/// C type : Date_struct
	@Field(8) 
	public OGRField Date(OGRField.Date_struct Date) {
		this.io.setNativeObjectField(this, 8, Date);
		return this;
	}
	/// <i>native declaration : /home/aaime/devel/gdal/gdal-1.8.0/ogr/ogr_core.h:544</i>
	public static class IntegerList_struct extends StructObject {
		public IntegerList_struct() {
			super();
		}
		public IntegerList_struct(Pointer pointer) {
			super(pointer);
		}
		@Field(0) 
		public int nCount() {
			return this.io.getIntField(this, 0);
		}
		@Field(0) 
		public IntegerList_struct nCount(int nCount) {
			this.io.setIntField(this, 0, nCount);
			return this;
		}
		/// C type : int*
		@Field(1) 
		public Pointer<Integer > paList() {
			return this.io.getPointerField(this, 1);
		}
		/// C type : int*
		@Field(1) 
		public IntegerList_struct paList(Pointer<Integer > paList) {
			this.io.setPointerField(this, 1, paList);
			return this;
		}
	};
	/// <i>native declaration : /home/aaime/devel/gdal/gdal-1.8.0/ogr/ogr_core.h:548</i>
	public static class RealList_struct extends StructObject {
		public RealList_struct() {
			super();
		}
		public RealList_struct(Pointer pointer) {
			super(pointer);
		}
		@Field(0) 
		public int nCount() {
			return this.io.getIntField(this, 0);
		}
		@Field(0) 
		public RealList_struct nCount(int nCount) {
			this.io.setIntField(this, 0, nCount);
			return this;
		}
		/// C type : double*
		@Field(1) 
		public Pointer<Double > paList() {
			return this.io.getPointerField(this, 1);
		}
		/// C type : double*
		@Field(1) 
		public RealList_struct paList(Pointer<Double > paList) {
			this.io.setPointerField(this, 1, paList);
			return this;
		}
	};
	/// <i>native declaration : /home/aaime/devel/gdal/gdal-1.8.0/ogr/ogr_core.h:552</i>
	public static class StringList_struct extends StructObject {
		public StringList_struct() {
			super();
		}
		public StringList_struct(Pointer pointer) {
			super(pointer);
		}
		@Field(0) 
		public int nCount() {
			return this.io.getIntField(this, 0);
		}
		@Field(0) 
		public StringList_struct nCount(int nCount) {
			this.io.setIntField(this, 0, nCount);
			return this;
		}
		/// C type : char**
		@Field(1) 
		public Pointer<Pointer<Byte > > paList() {
			return this.io.getPointerField(this, 1);
		}
		/// C type : char**
		@Field(1) 
		public StringList_struct paList(Pointer<Pointer<Byte > > paList) {
			this.io.setPointerField(this, 1, paList);
			return this;
		}
	};
	/// <i>native declaration : /home/aaime/devel/gdal/gdal-1.8.0/ogr/ogr_core.h:556</i>
	public static class Binary_struct extends StructObject {
		public Binary_struct() {
			super();
		}
		public Binary_struct(Pointer pointer) {
			super(pointer);
		}
		@Field(0) 
		public int nCount() {
			return this.io.getIntField(this, 0);
		}
		@Field(0) 
		public Binary_struct nCount(int nCount) {
			this.io.setIntField(this, 0, nCount);
			return this;
		}
		/// C type : GByte*
		@Field(1) 
		public Pointer<GByte > paData() {
			return this.io.getPointerField(this, 1);
		}
		/// C type : GByte*
		@Field(1) 
		public Binary_struct paData(Pointer<GByte > paData) {
			this.io.setPointerField(this, 1, paData);
			return this;
		}
	};
	/// <i>native declaration : /home/aaime/devel/gdal/gdal-1.8.0/ogr/ogr_core.h:560</i>
	public static class Set_struct extends StructObject {
		public Set_struct() {
			super();
		}
		public Set_struct(Pointer pointer) {
			super(pointer);
		}
		@Field(0) 
		public int nMarker1() {
			return this.io.getIntField(this, 0);
		}
		@Field(0) 
		public Set_struct nMarker1(int nMarker1) {
			this.io.setIntField(this, 0, nMarker1);
			return this;
		}
		@Field(1) 
		public int nMarker2() {
			return this.io.getIntField(this, 1);
		}
		@Field(1) 
		public Set_struct nMarker2(int nMarker2) {
			this.io.setIntField(this, 1, nMarker2);
			return this;
		}
	};
	/// <i>native declaration : /home/aaime/devel/gdal/gdal-1.8.0/ogr/ogr_core.h:570</i>
	public static class Date_struct extends StructObject {
		public Date_struct() {
			super();
		}
		public Date_struct(Pointer pointer) {
			super(pointer);
		}
	};
}
