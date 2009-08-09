package org.geotools.gui.swing.dndlist;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import java.awt.GraphicsEnvironment;

import org.junit.Test;

public class DnDListTest {

    boolean isHeadless(){
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment(); 
        return ge.isHeadless();
    }
    @Test
    public void testDnDList() {
        if( isHeadless() ) return;

        DnDList<String> list = new DnDList<String>();
        assertNotNull( list );
    }

    @Test
    public void testDnDListDnDListModelOfT() {
        if( isHeadless() ) return;

        DnDListModel<String> model = new DnDListModel<String>();
        model.addItem("one");
        model.addItem("two");
        
        DnDList<String> list = new DnDList<String>( model );
        assertSame( model, list.getModel() );
        
        try {
           list = new DnDList<String>( null );
           fail( "Expected illegal argument exception");
        }
        catch( IllegalArgumentException expected ){            
        }
    }

    @Test
    public void testGetModel() {
        if( isHeadless() ) return;

        DnDListModel<String> model = new DnDListModel<String>();
        model.addItem("one");
        model.addItem("two");
        
        DnDList<String> list = new DnDList<String>( model );
        assertSame( model, list.getModel() );
    }

}
