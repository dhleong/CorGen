package net.dhleong.corgen.frontend.wizard;

import java.io.File;
import java.io.FileNotFoundException;

import net.dhleong.corgen.CorGen;
import net.dhleong.corgen.CorGen.Language;
import net.dhleong.corgen.CorpusChunk.Granularity;
import net.dhleong.corgen.OutputCorpus;
import net.dhleong.corgen.PostPlugin;
import net.dhleong.corgen.frontend.EclipsePluginActivator;
import net.dhleong.corgen.impl.BasicOutputCorpus;
import net.dhleong.corgen.plugins.StopWordRemovalPlugin;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;


public class CorGenWizard extends Wizard {
    
    File mInputPath;
    private CorGenWizardPage page;
    
    @Override
    public void addPages() {
        page = new CorGenWizardPage("CorpusGeneration", mInputPath);
        addPage(page);
    }

    @Override
    public boolean performFinish() {

        // fetch input strings here to avoid
        //  invalid thread access stuff
        final String sLang = page.language.getText();
        final String sGranularity = page.granularity.getText();
        final boolean stripComments = page.stripComments.getSelection();
        final boolean stopWords = page.stopWords.getSelection();
        final String[] pluginsSelected = page.pluginsSelected.getSelection();
        final String sOutput = page.outputPath.getText();
        
        Job job = new Job("CorpusGeneration") { 
            @Override 
            protected IStatus run(IProgressMonitor monitor) { 
                monitor.beginTask("Generating Corpus", 
                        IProgressMonitor.UNKNOWN);
                monitor.subTask("Configuring...");
                
                // prepare CorGen object
                Language lang = null;
                for (Language l : CorGen.Language.values()) {
                    if (l.string.equals(sLang)) {
                        lang = l;
                        break;
                    }
                }
                
                CorGen g = null;
                try {
                    g = new CorGen(lang, mInputPath);
                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
                    return new Status(Status.ERROR, EclipsePluginActivator.PLUGIN_ID, 
                                "Specified corpus input path not found", e);
                }
                
                // prepare Config
                Granularity gran = Granularity.valueOf(sGranularity);
                boolean useComments = !stripComments;
                CorGen.Config c = new CorGen.Config(gran, useComments);
                
                // plugins
                if (stopWords)
                    c.addPlugin(StopWordRemovalPlugin.forLanguage(lang));
                
                for (String s : pluginsSelected) {
                    Class<? extends PostPlugin> klass = page.pluginsMap.get(s);
                    try {
                        c.addPlugin(klass.newInstance());
                    } catch (InstantiationException e) {
                        return new Status(Status.ERROR, EclipsePluginActivator.PLUGIN_ID, 
                                "Could not initialize plugin " + s, e);
                    } catch (IllegalAccessException e) {
                        return new Status(Status.ERROR, EclipsePluginActivator.PLUGIN_ID, 
                                "Could not initialize plugin " + s, e);
                    }
                }
                
                OutputCorpus o = new BasicOutputCorpus(sOutput);
                monitor.worked(1);
                
                monitor.subTask("Pre-processing source code...");
                g.ensurePreCorpus();
                monitor.worked(1);
                
                monitor.subTask("Building output corpus...");
                g.output(c, o, true);
                monitor.worked(1);
 
                monitor.done(); 
                
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
        return true;
    }
    
//    private static final boolean isModal(Job job) {
//        Boolean isModal = (Boolean)job.getProperty(
//                IProgressConstants.PROPERTY_IN_DIALOG);
//        return isModal != null && isModal.booleanValue();
//    }
//    
    private Action getCompletedAction() {
        return new Action("View Corpus Generation Status") {
            @Override
            public void run() {
                MessageDialog.openInformation(getShell(), "CorGen Corpus", 
                    "Successfully generated corpus");
            }
        };
    }

    public void setInputPath(File path) {
        mInputPath = path;
    }

}
