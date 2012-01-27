package net.dhleong.corgen;

/**
 * The OutputCorpus interface lets us be flexible
 *  about how we want to handle the formatting
 *  of our output corpus.
 * @author dhleong
 *
 */
public interface OutputCorpus {

    /**
     * Called to add the given chunk to the 
     *  output corpus
     * 
     * @param chunk
     */
    public void add(CorpusChunk chunk);

    /**
     * Called when we've finished adding chunks
     *  to this corpus. Implementations can safely
     *  assume that this object will never be 
     *  referenced again
     */
    void finish();
}
