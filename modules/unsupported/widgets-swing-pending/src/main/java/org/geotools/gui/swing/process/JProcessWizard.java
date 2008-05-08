package org.geotools.gui.swing.process;

import java.awt.Dialog;
import java.awt.HeadlessException;

public class JProcessWizard extends JWizard {
    private static final long serialVersionUID = -5885825548881784615L;    
    public JProcessWizard( String title ) throws HeadlessException {
        super(title);
        initPages();
    }

    public JProcessWizard( Dialog owner, String title){
        super( owner, title );
        initPages();
        
    }
    private void initPages() {
        // hey eclisia implement this by looking at ProcessFinder :-D
        // registerWizardPanel( new JProcessPage() );
    	
    	ProcessSelectionPage selectionPage = new ProcessSelectionPage();    	
        registerWizardPanel( selectionPage );
        selectionPage.setJProcessWizard(this);
        setCurrentPanel( selectionPage.getIdentifier() );
    }

    public static void main( String args[] ){
        JProcessWizard wizard = new JProcessWizard("Test Input Parameter UI");
        int result = wizard.showModalDialog();
        System.out.println("finished " + result );
    }
}
