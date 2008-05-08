package org.geotools.demo.metadata.example;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import org.geotools.metadata.iso.citation.CitationImpl;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.metadata.iso.citation.ResponsiblePartyImpl;
import org.geotools.metadata.iso.citation.TelephoneImpl;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.metadata.citation.Citation;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

public class MetadataExample {

    public static void referenceDocument( Citation citation ){
        System.out.println( citation.getTitle() );
        System.out.println( citation.getTitle().toString( Locale.FRENCH) );
        
        System.out.println( citation.getIdentifiers() );
        System.out.println( citation.getAlternateTitles() );        
    }
    
    public static void telephone(){
        TelephoneImpl phone = new TelephoneImpl();
        phone.setVoices(Collections.singleton("555-1234"));
        phone.setFacsimiles(Collections.singleton("555-2FAX"));
        System.out.println( phone );
    }
    
    public static void wkt(){
        CoordinateReferenceSystem crs = DefaultGeographicCRS.WGS84;
        System.out.println( crs.toWKT() );
    }
    
    public static void main( String[] args ) {
        referenceDocument( Citations.EPSG );
        referenceDocument( Citations.OGC );
        referenceDocument( Citations.ORACLE );
        
        CitationImpl citation = new CitationImpl();
        citation.setEditionDate( new Date() ); // today
        
        Collection parties = Collections.singleton( ResponsiblePartyImpl.GEOTOOLS );
        citation.setCitedResponsibleParties( parties );
        
        referenceDocument( Citations.ORACLE );
        
        telephone();
        wkt();
    }
}

