package net.dhleong.corgen;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;

import net.dhleong.corgen.CorpusChunk.Granularity;
import net.dhleong.corgen.impl.BasicOutputCorpus;
import net.dhleong.corgen.impl.SrcMLPreCorpusBuilder;
import net.dhleong.corgen.impl.SrcMLPreCorpusParser;
import net.dhleong.corgen.plugins.PorterStemmingPlugin;
import net.dhleong.corgen.plugins.StopWordRemovalPlugin;
import net.dhleong.corgen.plugins.StupidIdentSplittingPlugin;

/**
 * Main interface to CorGen. This represents a single
 *  instance of the framework, which operates on a single
 *  source tree for a single language, generating a single
 *  output corpus.
 * 
 * @author dhleong
 *
 */
public class CorGen {
    
    public enum Language {
        CPP("C++");
        
        public final String string;
        Language(String str) {
            string = str;
        }
    }
    
    /**
     * Configuration options for an output corpus
     * @author dhleong
     *
     */
    public static class Config {
        
        public final boolean useComments;
        public final boolean verbose;
        final Granularity granularity;
        
        private final LinkedList<PostPlugin> mPlugins = new LinkedList<PostPlugin>();
        
        public Config(Granularity granularity, boolean useComments) {
            this(granularity, useComments, false);
        }
        
        public Config(Granularity granularity, boolean useComments, 
                boolean verbose) {
            this.granularity = granularity;
            this.useComments = useComments;
            this.verbose = verbose;
        }
        
        public void addPlugin(PostPlugin p) {
            mPlugins.add(p);
        }
    }
    
    private static final String SRCML_PATH = "/Users/dhleong/Documents/workspace/CorGen/src2srcml";

    private final Language language;
    private final File projRoot;
    private final File preCorpus;
    
    private final PreCorpusBuilder builder;
    private final PreCorpusParser parser;

    /**
     * Initialize a CorGen instance for the project whose
     *  source root directory is at the given path, and which
     *  is in the given language
     *  
     * @param l
     * @param projectPath
     * @throws FileNotFoundException
     */
    public CorGen(Language l, String projectPath) throws FileNotFoundException {
        this(l, new File(projectPath));
    }
    
    public CorGen(Language l, File projectPath) throws FileNotFoundException {
        language = l;
        
        projRoot = projectPath;
        if (!projRoot.exists())
            throw new FileNotFoundException();
        
        preCorpus = getPreCorpusPath(projectPath);
        
        // This is where we would switch off builders based on language
        builder = new SrcMLPreCorpusBuilder(language, 
                new File(SRCML_PATH), projRoot, preCorpus);
        parser = new SrcMLPreCorpusParser(language);
    }

    public static final File getPreCorpusPath(File projectPath) {
        return new File(projectPath, ".corgen-pre");
    }

    /**
     * Make sure the pre-corpus exists and is up-to-date. 
     *  Generally, you won't need to call this-- just use
     *  {@link #output(Config, OutputCorpus)}, as it takes
     *  care of this. But, if you want more fine-grained
     *  status, then you could run this by itself.
     */
    public void ensurePreCorpus() {
        if (!preCorpus.exists()) {
            // generate pre-corpus
            if (!preCorpus.mkdirs()) 
                throw new RuntimeException("Could not create pre-corpus directory " + 
                        preCorpus.getAbsolutePath());
        }    
            
        // builders should take into account anything already
        //  done, so simply running this is fine
        builder.build();
    }
    
    /**
     * Shortcut to run {@link #output(Config, OutputCorpus, boolean)}
     *  with "false" as the last parameter. This should be the default
     *  way to generate an output corpus, unless you really want to
     *  have the fine-grained status of being able to say "updating
     *  pre-corpus" and "generating corpus" separately. 
     *  
     * @param config
     * @param output
     */
    public void output(Config config, OutputCorpus output) {
        output(config, output, false);
    }
    
    /**
     * Generate an output corpus following the given configuration
     *  into the folder at "outputPath."
     *  
     * Most users will probably want to use {@link #output(Config, OutputCorpus)}
     *  instead, to make sure you don't accidentally send True for
     *  ignorePreCorpus and have something go terribly wrong
     * 
     * @param outputPath
     * @param config
     * @param ignorePreCorpus If True, we'll assume the pre-corpus is
     *  created and up-to date. Make sure you know what you're doing
     *  before passing "true"! 
     */
    public void output(Config config, OutputCorpus output, boolean ignorePreCorpus) {
        
        if (!ignorePreCorpus) {
            // make sure the pre-corpus is up-to-date
            System.out.println("Updating pre-corpus");
            ensurePreCorpus();
        }
        
        // go forth and build output
        System.out.println("Loading pre-corpus");
        PreCorpus prec = parser.parse(preCorpus, config); // load from preCorpus
        
        System.out.println("Generating output corpus");
        for (CorpusChunk chunk : prec.getChunks(config.granularity)) {
            
            System.out.print('.');
            
            // plugins' parse loop
            for (PostPlugin p : config.mPlugins) 
                chunk = p.parseChunk(chunk);
            
            // add the processed chunk to the output
            output.add(chunk); 
        }
        
        output.finish();
        System.out.println("Done!");
    }

    public static final void main(String[] args) throws FileNotFoundException {
        Language lang = Language.CPP;
        CorGen g = new CorGen(lang, "/Users/dhleong/git/firesheep/backend/src");
//        CorGen g = new CorGen(Language.CPP, "/Users/dhleong/git/mongo/db");
        Config c = new Config(Granularity.METHOD, true);
        
        // add plugins
        c.addPlugin(StopWordRemovalPlugin.forLanguage(lang));
        c.addPlugin(new StupidIdentSplittingPlugin());
        c.addPlugin(new PorterStemmingPlugin()); // do stemming after splitting
        
        OutputCorpus o = new BasicOutputCorpus("/Users/dhleong/code/corgen/firesheep");
//        OutputCorpus o = new BasicOutputCorpus("/Users/dhleong/code/corgen/mongo");
        g.output(c, o);
    }
}
