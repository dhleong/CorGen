package net.dhleong.corgen.frontend.actions;

import java.io.File;
import java.util.LinkedList;

import net.dhleong.corgen.CorGen;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class CleanPreCorpusAction implements IObjectActionDelegate {

    private File mInputPath;
    private Shell shell;
	
	/**
	 * Constructor for Action1.
	 */
	public CleanPreCorpusAction() {
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
        Job job = new Job("CorpusGeneration") { 
            @Override 
            protected IStatus run(IProgressMonitor monitor) { 
                monitor.beginTask("Cleaning CorGen data", 
                        IProgressMonitor.UNKNOWN);
                File preCorpus = CorGen.getPreCorpusPath(mInputPath);
                System.out.println("Delete: " + preCorpus.getAbsolutePath());
                if (preCorpus != null && preCorpus.exists()) {
                    deleteDirectory(preCorpus);
                }

//                if (isModal(this)) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        getCompletedAction().run();
                    }
                    
                });
//                } else {
//                    setProperty(IProgressConstants.KEEP_PROPERTY, Boolean.TRUE);
//                    setProperty(IProgressConstants.ACTION_PROPERTY, 
//                            getCompletedAction());
//                }
                
                return Status.OK_STATUS;
            }
        };

        job.setUser(true);
        job.schedule();
    }

    /**
     * "Recursively" delete the folder and all files it contains.
     *  This is unrolled recursion using a work list to make sure
     *  we don't overflow the stack in deeply nested code hierarchies.
     *  Probably won't ever happen, but just in case....
     * @param dir
     */
    protected static void deleteDirectory(File dir) {
        LinkedList<File> workList = new LinkedList<File>();
        workList.addLast(dir);
        
        while (workList.size() > 0) {
            File curr = workList.removeFirst();
            if (curr.isDirectory()) {
                // scan for any kids to place on the work list
                File[] kids = curr.listFiles();
                for (File f : kids)
                    workList.addLast(f);
                
                if (kids.length != 0) {
                    // we had kids, so put us back on the work list
                    //  and try to delete us after we've deleted the kids
                    workList.addLast(curr);
                    continue;
                }
                
                // IE: if kids.length == 0, fall through and delete
            } 
            
            // it's a file or empty directory... delete it
            System.out.println("-- rm: " + curr.getAbsolutePath());
            curr.delete();
        }
    }

//    private static final boolean isModal(Job job) {
//        Boolean isModal = (Boolean)job.getProperty(
//                IProgressConstants.PROPERTY_IN_DIALOG);
//        return isModal != null && isModal.booleanValue();
//    }
    
    private Action getCompletedAction() {
        return new Action("View Corpus Cleaning Status") {
            @Override
            public void run() {
                MessageDialog.openInformation(shell, "CorGen Cleaning", 
                "Successfully cleaned CorGen data");
            }
        };
    }

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@Override
    public void selectionChanged(IAction action, ISelection selection) {
	    IStructuredSelection s = (IStructuredSelection) selection;
	    if (s == null)
	        return;
	    
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
