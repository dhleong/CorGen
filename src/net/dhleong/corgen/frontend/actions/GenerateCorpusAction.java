package net.dhleong.corgen.frontend.actions;

import java.io.File;

import net.dhleong.corgen.frontend.wizard.CorGenWizard;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;


public class GenerateCorpusAction implements IObjectActionDelegate {

	private Shell shell;
    private File mInputPath;
	
	/**
	 * Constructor for Action1.
	 */
	public GenerateCorpusAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	@Override
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
		
		
	}

    @Override
    public void run(IAction action) {
        CorGenWizard wizard = new CorGenWizard();
        wizard.setInputPath(mInputPath);
        WizardDialog dialog = new WizardDialog(shell, wizard);
        dialog.create();
        dialog.open();        
    }


	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
    public void selectionChanged(IAction action, ISelection selection) {
	    IStructuredSelection s = (IStructuredSelection) selection;
	    IFolder selectedFolder = (IFolder) s.getFirstElement();
	    if (selectedFolder == null)
	        return;
	    
	    IPath input = selectedFolder.getLocation();
	    if (input == null) {
	        // die horribly...
	        throw new RuntimeException("Couldn't determine source directory path...");
	    }
	    
	    mInputPath = new File(input.toOSString());
	}

}
