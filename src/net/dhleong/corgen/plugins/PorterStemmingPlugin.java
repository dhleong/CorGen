package net.dhleong.corgen.plugins;

import net.dhleong.corgen.CorpusChunk;
import net.dhleong.corgen.Plugin;
import net.dhleong.corgen.util.PorterStemmer;

/**
 * Porter Stemming, using the author's implementation
 * 
 * @author dhleong
 *
 */
@Plugin
public class PorterStemmingPlugin extends AbsPostPlugin {

    @Override
    public void parseWord(CorpusChunk dest, String word) {
        PorterStemmer s = new PorterStemmer(word);
        s.stem();
        dest.addWord(s.toString());
    }
}