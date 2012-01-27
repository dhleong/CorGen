package net.dhleong.corgen.frontend;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.ValueConverter;
import net.dhleong.corgen.CorGen;
import net.dhleong.corgen.CorGen.Language;
import net.dhleong.corgen.CorpusChunk.Granularity;
import net.dhleong.corgen.OutputCorpus;
import net.dhleong.corgen.Plugin;
import net.dhleong.corgen.PostPlugin;
import net.dhleong.corgen.impl.BasicOutputCorpus;
import net.dhleong.corgen.plugins.StopWordRemovalPlugin;

import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

/**
 * The terminal-based frontend interface to CorGen
 * @author dhleong
 *
 */
public class ConsoleFrontend {

    private static final class PluginConverter implements
            ValueConverter<PostPlugin> {
        
        HashMap<String, Class<?>> types = new HashMap<String, Class<?>>();
        StringBuilder pattern = new StringBuilder();
        
        public PluginConverter() {
//            Set<URL> urls = ClasspathHelper.forClassLoader();
//            for (URL u : urls)
//                System.out.println("> " + u);
            
            Reflections r = new Reflections(
                    new ConfigurationBuilder()
                        .filterInputsBy(new FilterBuilder.Include(
                                FilterBuilder.prefix("net.dhleong.corgen")))
                        .setUrls(ClasspathHelper.forClassLoader())
                        .setScanners(new TypeAnnotationsScanner()));
            Set<Class<?>> availablePlugins = 
                r.getTypesAnnotatedWith(Plugin.class);

            for (Class<?> c : availablePlugins) {
                if (PostPlugin.class.isAssignableFrom(c)) {
                    String n = c.getSimpleName().replace("Plugin", "");
                    if (types.size() > 0)
                        pattern.append("/");
                    types.put(n.toLowerCase(), c);
                    pattern.append(n);
                }
            }
        }
        
        @Override
        public PostPlugin convert(String value) {
            String lower = value.toLowerCase();
            if (!types.containsKey(lower))
                throw new IllegalArgumentException("Unknown plugin name ``" + 
                        value +"''");
            try {
                return (PostPlugin) types.get(lower).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            
            throw new RuntimeException("Couldn't instantiate plugin ``"
                    + value +"''");
        }

        @Override
        public String valuePattern() {
            return pattern.toString();
        }

        @Override
        public Class<PostPlugin> valueType() {
            return PostPlugin.class;
        }
    }

    static final ValueConverter<Language> mLangConverter = 
        new ValueConverter<Language>() {

        @Override
        public Language convert(String value) {
            try {
                System.out.println("Converting " + value +": " + Language.valueOf(value.toUpperCase()));
                return Language.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unhandled language ``" + value + "''");
            }
        }

        @Override
        public String valuePattern() {
            return "cpp";
        }

        @Override
        public Class<Language> valueType() {
            return Language.class;
        }

    };

    static final ValueConverter<Granularity> mGranConverter = 
        new ValueConverter<Granularity>() {

        @Override
        public Granularity convert(String value) {
            try {
                return Granularity.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown granularity ``" + value + "''");
            }
        }

        @Override
        public String valuePattern() {
            return "method/class/package";
        }

        @Override
        public Class<Granularity> valueType() {
            return Granularity.class;
        }

    };
    
    static final ValueConverter<PostPlugin> mPluginConverter = 
        new PluginConverter();

    public static final void main(String[] args) throws IOException {
        OptionParser parser = new OptionParser();
        parser.acceptsAll( asList( "h", "?" ), "Show help" );
        parser.acceptsAll( asList( "v", "verbose" ), "Show extra detail" );
        
        OptionSpec<Language> lang = parser
            .acceptsAll( asList( "l", "language"), 
                        "Input source code language (cpp only for now)" )
                    .withRequiredArg()
//                    .required()
//                    .ofType(CorGen.Language.class)
                    .withValuesConvertedBy( mLangConverter )
                    .defaultsTo(Language.CPP);
        OptionSpec<Granularity> granularity = parser
            .acceptsAll( asList( "g", "granularity"), "Corpus granularity level" )
                .withRequiredArg()
//                .required()
//                .ofType(Granularity.class)
                .withValuesConvertedBy( mGranConverter )
                .defaultsTo(Granularity.CLASS);
        parser.acceptsAll( asList( "nc", "no-comments" ), 
                "Strip comments from corpus" );
        
        OptionSpec<PostPlugin> plugins = parser
            .acceptsAll( asList("p", "plugins"), "Post-processing plugins. " +
            		"Note that they are blindly executed IN ORDER SPECIFIED " +
            		"(separate by commas)" )
                .withRequiredArg()
//                .ofType(PostPlugin.class)
                .withValuesSeparatedBy(',')
                .withValuesConvertedBy(mPluginConverter);
        
        parser.acceptsAll( asList("s", "stop-words"), 
                "Strip language-specific stop words from corpus" );
            

        OptionSpec<File> out = parser
            .acceptsAll( asList( "o", "output-path" ), 
                        "Folder where the output corpus files (corpus.txt and " +
                        "corpus-mapping.txt) will be placed." )
                     .withRequiredArg()
//                     .required()
                     .ofType( File.class );
        OptionSpec<File> in = parser
            .acceptsAll( asList( "i", "input-path" ), 
                        "Root directory of source code from which to generate corpus")
                    .withRequiredArg()
//                    .required()
                    .ofType( File.class );

        // parse
        OptionSet o = parser.parse(args);
        
        // handle
        if (!o.hasOptions() || o.has("h") || o.has("?") || o.has("help")) {
            parser.printHelpOn( System.out );
            return;
        }
        
        // corgen instance
        Language l = lang.value(o);
        File i = in.value(o);
        CorGen cg = null;
        try {
            cg = new CorGen(l, i);
        } catch (FileNotFoundException e) {
            System.err.println("Could not find input directory: " + i.getAbsolutePath());
            return;
        }
        
        // config instance
        Granularity gran = granularity.value(o);
        boolean com = !(o.has("nc") || o.has("no-comments"));
        boolean verb = o.has("v") || o.has("verbose");
        CorGen.Config conf = new CorGen.Config(gran, com, verb);
        
        // special case plugin
        if (o.has("s") || o.has("stop-words"))
            conf.addPlugin(StopWordRemovalPlugin.forLanguage(l));
        
        // add plugins
        for (PostPlugin p : plugins.values(o))
            conf.addPlugin(p);
        
        // output
        File dest = out.value(o);
        if (dest == null) {
            System.err.println("Please specify the output path (-o)");
            return;
        }
        OutputCorpus output = new BasicOutputCorpus(dest);
        cg.output(conf, output);
    }

    protected static List<String> asList(String... strings) {
        List<String> l = new ArrayList<String>(strings.length);
        for (String s : strings)
            l.add(s);
        
        return l;
    }
}
