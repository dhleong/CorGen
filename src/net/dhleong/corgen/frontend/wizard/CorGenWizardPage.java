package net.dhleong.corgen.frontend.wizard;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.dhleong.corgen.CorGen;
import net.dhleong.corgen.CorpusChunk.Granularity;
import net.dhleong.corgen.Plugin;
import net.dhleong.corgen.PostPlugin;
import net.dhleong.corgen.plugins.PorterStemmingPlugin;
import net.dhleong.corgen.plugins.StupidIdentSplittingPlugin;
import net.dhleong.corgen.plugins.ToLowerPlugin;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

public class CorGenWizardPage extends WizardPage {

    Text outputPath;
    final File mInputPath;
    Combo language;
    Combo granularity;
    Button stripComments;
    Button stopWords;
    List pluginsAvailable;
    List pluginsSelected;
    
    HashMap<String, Class<? extends PostPlugin>> pluginsMap;

    protected CorGenWizardPage(String pageName, File inputPath) {
        super(pageName);
        
        mInputPath = inputPath;

        setTitle("Corpus Generation");
        setDescription("Please configure the corpus options");
    }

    @Override
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        composite.setLayout(layout);
        setControl(composite);
        
        // input path
        new Label(composite, SWT.NONE).setText("Input path");
        Text input = new Text(composite, SWT.READ_ONLY);
        input.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        input.setText(mInputPath.toString());
        input.setEnabled(false);
        new Label(composite, SWT.NONE); // place-holder
        
        // output path
        new Label(composite,SWT.NONE).setText("Output Path");
        outputPath = new Text(composite,SWT.NONE);
        outputPath.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Button browse = new Button(composite, SWT.NONE);
        browse.setText("Browse");
        browse.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog fd = new DirectoryDialog(getShell(), SWT.OPEN);
                fd.setText("Open");
//                fd.setFilterPath("C:/");
                String selected = fd.open();
                if (selected != null)
                    outputPath.setText(selected);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                System.out.println("What does default mean?");
            }
            
        });
        
        // language
        new Label(composite,SWT.NONE).setText("Language");
        language = new Combo(composite,SWT.READ_ONLY);
        for (CorGen.Language l : CorGen.Language.values()) 
            language.add(l.string);
        language.select(0);
        new Label(composite, SWT.NONE); // place-holder
        
        // granularity
        new Label(composite,SWT.NONE).setText("Granularity");
        granularity = new Combo(composite,SWT.READ_ONLY);
        for (Granularity g : Granularity.values()) 
            granularity.add(g.name());
        granularity.select(1);
        new Label(composite, SWT.NONE); // place-holder
        
        // strip comments?
        stripComments = new Button(composite,SWT.CHECK);
        stripComments.setText("Strip comments");
        // strip stop words?
        stopWords = new Button(composite,SWT.CHECK);
        stopWords.setText("Strip stop words");
        new Label(composite, SWT.NONE); // place-holder
        
        // (divider)
        new Label(composite, SWT.NONE); // place-holder
        new Label(composite, SWT.NONE).setText("Plugin Selection");
        new Label(composite, SWT.NONE); // place-holder
        
        pluginsAvailable = new List(composite, SWT.NONE);
        pluginsAvailable.setLayoutData(new GridData(GridData.FILL_BOTH));
        pluginsAvailable.addMouseListener(new MouseListener() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                addSelected();
            }

            @Override
            public void mouseDown(MouseEvent e) {}
            @Override
            public void mouseUp(MouseEvent e) {}
            
        });
        
        pluginsMap = getPlugins();
        for (Entry<String, Class<? extends PostPlugin>> e : pluginsMap.entrySet()) {
            pluginsAvailable.add(e.getKey());
        }
        
        pluginsSelected = new List(composite, SWT.FILL);
        pluginsSelected.setLayoutData(new GridData(GridData.FILL_BOTH));
        new Label(composite, SWT.NONE); // place-holder
        pluginsSelected.addMouseListener(new MouseListener() {

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                removeSelected();
            }

            @Override
            public void mouseDown(MouseEvent e) {}
            @Override
            public void mouseUp(MouseEvent e) {}
            
        });
        
        Button add = new Button(composite, SWT.NONE);
        add.setText(">>");
        add.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                addSelected();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                System.out.println("What does default mean?");
            }
            
        });
        Button remove = new Button(composite, SWT.NONE);
        remove.setText("<<");
        remove.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                removeSelected();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                System.out.println("What does default mean?");
            }
            
        });
    }
    
    private void addSelected() {
        String[] selected = pluginsAvailable.getSelection();
        for (String string : selected) {
            pluginsAvailable.remove(string);
            pluginsSelected.add(string);
        }
    }
    
    private void removeSelected() {
        String[] selected = pluginsSelected.getSelection();
        for (String string : selected) {
            pluginsSelected.remove(string);
            pluginsAvailable.add(string);
        }
    }
    
    @SuppressWarnings("unchecked")
    private HashMap<String, Class<? extends PostPlugin>> getPlugins() {
        HashMap<String, Class<? extends PostPlugin>> types = 
            new HashMap<String, Class<? extends PostPlugin>>();
        
        
        Reflections r = new Reflections(
                new ConfigurationBuilder()
                    .filterInputsBy(new FilterBuilder.Include(
                            FilterBuilder.prefix("net.dhleong.corgen")))
                    .setUrls(ClasspathHelper.forClassLoader())
                    .setScanners(new TypeAnnotationsScanner()));
        Set<Class<?>> availablePlugins = 
            r.getTypesAnnotatedWith(Plugin.class);

        if (availablePlugins.size() == 0) {
            System.out.println("WARN: Reflections API couldn't find plugins; " +
            		"Using manual inputs");
            // reflections doesn't work for some reason, so we have to
            //  do this by hand :(
            availablePlugins = new HashSet<Class<?>>();
            availablePlugins.add(PorterStemmingPlugin.class);
            availablePlugins.add(StupidIdentSplittingPlugin.class);
            availablePlugins.add(ToLowerPlugin.class);
        }
        
//        System.out.println("Fetch plugins");
        for (Class<?> c : availablePlugins) {
//            System.out.println("+ " + c);
            if (PostPlugin.class.isAssignableFrom(c)) {
                String n = c.getSimpleName().replace("Plugin", "");
//                types.put(n.toLowerCase(), c);
                types.put(n, (Class<? extends PostPlugin>) c); // we don't need to lower-case it since it's gui
//                pattern.append(n);
//                System.out.println(n);
            }
        }
        
        return types;
    }
}
