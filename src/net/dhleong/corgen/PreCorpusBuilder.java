package net.dhleong.corgen;

/**
 * This is used to build precorpora
 * 
 * @author dhleong
 *
 */
public interface PreCorpusBuilder {

    /**
     * Go forth and build. Note that this can be
     *  called even if the corpus exists. Implementations
     *  should only rebuild parts of the corpus that need
     *  rebuilding (IE: the source file's last modified
     *  time is more recent than the pre-corpus's file)
     */
    void build();

}
