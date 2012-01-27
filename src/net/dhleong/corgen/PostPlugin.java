package net.dhleong.corgen;

import net.dhleong.corgen.plugins.AbsPostPlugin;

/**
 * The PostPlugin interface allows for
 *  plugins to handle post processing
 * @author dhleong
 *
 */
public interface PostPlugin {

    
    /**
     * Parse a chunk, returning a new chunk with all
     *  words in the chunk processed. 
     *  
     * If we're only interested in each word, the default
     *  implementation of {@link AbsPostPlugin} passes all 
     *  words in a chunk to a parseWords function
     *  
     * @param chunk
     * @return
     */
    public CorpusChunk parseChunk(CorpusChunk chunk);
}
