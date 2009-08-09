package org.geotools.gui.swing.dndlist;

import static org.junit.Assert.*;

import java.awt.dnd.DragGestureEvent;

import org.junit.Test;

public class DnDListTest {

    @Test
    public void testDnDList() {
        DnDList<String> list = new DnDList<String>();
        assertNotNull( list );
    }

    @Test
    public void testDnDListDnDListModelOfT() {
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
        DnDListModel<String> model = new DnDListModel<String>();
        model.addItem("one");
        model.addItem("two");
        
        DnDList<String> list = new DnDList<String>( model );
        assertSame( model, list.getModel() );
    }

}
