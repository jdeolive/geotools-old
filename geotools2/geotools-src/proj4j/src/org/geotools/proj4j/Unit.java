/*
 * Unit.java
 *
 * Created on February 19, 2002, 4:20 PM
 */

package proj4j.src.org.geotools.proj4j;

/**
 *
 * @author  jamesm
 */
public class Unit {
    String id;          /* units keyword */
    double to_meter;	/* multiply by value to get meters */
    String name;        /* comments */
    /** Creates a new instance of Unit */
    public Unit(String id,double to_meter,String name) {
        this.id = id;
        this.to_meter = to_meter;
        this.name = name;
    }
    
    
    static final Unit[] units={
        new Unit("km",1000,	"Kilometer"),
        new Unit("m",1,	"Meter"),
        new Unit("dm",(1d/10d),"Decimeter"),
        new Unit("cm",(1d/100d),"Centimeter"),
        new Unit("mm",(1d/1000d),"Millimeter"),
        new Unit("kmi",1852,"International Nautical Mile")
    };
    /*
	"in",	"0.0254",	"International Inch",
	"ft",	"0.3048",	"International Foot",
	"yd",	"0.9144",	"International Yard",
	"mi",	"1609.344",	"International Statute Mile",
	"fath",	"1.8288",	"International Fathom",
	"ch",	"20.1168",	"International Chain",
	"link",	"0.201168",	"International Link",
	"us-in",	"1./39.37",	"U.S. Surveyor's Inch",
	"us-ft",	"0.304800609601219",	"U.S. Surveyor's Foot",
	"us-yd",	"0.914401828803658",	"U.S. Surveyor's Yard",
	"us-ch",	"20.11684023368047",	"U.S. Surveyor's Chain",
	"us-mi",	"1609.347218694437",	"U.S. Surveyor's Statute Mile",
	"ind-yd",	"0.91439523",	"Indian Yard",
	"ind-ft",	"0.30479841",	"Indian Foot",
	"ind-ch",	"20.11669506",	"Indian Chain",
        */

}
