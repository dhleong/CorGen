package net.dhleong.corgen.impl;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;

import net.dhleong.corgen.CorGen.Language;
import net.dhleong.corgen.PreCorpusBuilder;

/**
 * Builds the Pre-processed Corpus using SrcML 
 *  (because I'm lazy and it's awesome)
 * 
 * @author dhleong
 *
 */
public class SrcMLPreCorpusBuilder implements PreCorpusBuilder {
    
    private final SimpleFilter mFilter;
    
    private final Language language;
    private final File srcmlBin, input, output;


    /**
     * 
     * @param srcmlBin Path to the src2srcml binary
     * @param input input source directory
     * @param output output directory (where the precorpus gets put 
     */
    public SrcMLPreCorpusBuilder(Language l, File srcmlBin, File input, File output) {
        this.language = l;
        this.srcmlBin = srcmlBin;
        this.input = input;
        this.output = output;
        
        mFilter = new SimpleFilter(language);
    }

    @Override
    public void build() {
        String inRoot = input.getAbsolutePath();
        
        LinkedList<File> workList = new LinkedList<File>();
        workList.addLast(input);
        
        while (workList.size() > 0) {
            File curr = workList.removeFirst();
            if (curr.isDirectory()) {
                // scan for sub-directories to place on the work list
                for (File f : curr.listFiles(mFilter)) {
                    workList.addLast(f);
                }
            } else {
                // parse the file
                String outRel = curr.getAbsolutePath().substring(inRoot.length());
                build(curr, new File(output, outRel));
            }
        }
        
        // TODO remove pre-corpus files for which the
        //  source file no longer exists
    }
    
    /**
     * Build the pre-processed corpus piece for a given file
     * 
     * @param in
     * @param out
     */
    private void build(File in, File out) {
        Runtime r = Runtime.getRuntime();
        try {
            if (!out.getParentFile().exists() && !out.getParentFile().mkdirs()) {
                throw new RuntimeException("Couldn't access/create directory for: " + out.getAbsolutePath());
            }
            
            // compare modified dates
            if (in.lastModified() <= out.lastModified())
                return; // no changes... don't rebuild
            
            Process p  = r.exec(String.format("%s --language=%s %s %s",
                    srcmlBin.getAbsolutePath(),
                    language.string,
                    in.getAbsolutePath(),
                    out.getAbsolutePath()));
            p.waitFor();
            
            System.out.println("* " + in.getAbsolutePath() +" > " + out.getAbsolutePath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    /**
     * This simple filter accepts directories and files with
     *  extensions appropriate to a given language 
     * 
     * @author dhleong
     *
     */
    public static final class SimpleFilter implements FileFilter {
        
        private final Language language;

        public SimpleFilter(Language language) {
            this.language = language;
        }

        @Override
        public boolean accept(File file) {
            return !file.getName().startsWith(".") &&
                (file.isDirectory() ||
                languageAccepts(file.getName()));
        }   
        
        private boolean languageAccepts(String name) {
            switch (language) {
            case CPP:
                return name.endsWith(".cpp") || name.endsWith(".cc") || 
                    name.endsWith(".c") ||
                    name.endsWith(".hpp") || name.endsWith(".hh") || 
                    name.endsWith(".h");
            }
            
            return false;
        }
    }
}
