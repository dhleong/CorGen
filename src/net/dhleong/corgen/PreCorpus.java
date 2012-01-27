package net.dhleong.corgen;


/**
 * The Pre-processed Corpus
 * @author dhleong
 *
 */
public interface PreCorpus {

    public Iterable<CorpusChunk> getChunks(CorpusChunk.Granularity granularity);
}
