package net.dhleong.corgen.plugins;

import net.dhleong.corgen.CorpusChunk;
import net.dhleong.corgen.PostPlugin;

public abstract class AbsPostPlugin implements PostPlugin {
    
    
    /**
     * Parse a word. We're given the destination chunk to add
     *  the processed version(s) to
     * 
     * @param word
     */
    public abstract void parseWord(CorpusChunk dest, String word);

    @Override
    public CorpusChunk parseChunk(CorpusChunk chunk) {
        CorpusChunk newChunk = new CorpusChunk(chunk.getName());
        for (String word : chunk) {
            parseWord(newChunk, word);
        }
            
        return newChunk;
    }

}
