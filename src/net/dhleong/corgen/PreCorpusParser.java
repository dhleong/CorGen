package net.dhleong.corgen;

import java.io.File;

import net.dhleong.corgen.CorGen.Config;

/**
 * The PreCorpusParser reads the pre-corpus off disk and 
 *  creates a {@link PreCorpus}. Implementations should
 *  expect that {@link #parse(File, Config)} can be called
 *  multiple times for different arguments, and the results
 *  of each call should be completely independent of other
 *  calls. In other words, DON'T preserve state in class
 *  variables!
 *  
 * Ideally, implementations should also be thread-safe, but
 *  for now its not a concern...
 *  
 * @author dhleong
 *
 */
public interface PreCorpusParser {

    /**
     * Create a PreCorpus from the source files found at srcRoot
     *  
     * @param srcRoot
     * @param config 
     * @return
     */
    PreCorpus parse(File srcRoot, Config config);

}
