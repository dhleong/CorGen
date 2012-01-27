package net.dhleong.corgen.plugins;

import net.dhleong.corgen.CorpusChunk;
import net.dhleong.corgen.Plugin;

/**
 * Converts all words to lower case. A "real" identifier
 *  splitting plugin should probably do this itself, but
 *  this is to further demonstrate plugin flexibility etc.  
 * 
 * @author dhleong
 *
 */
@Plugin
public class ToLowerPlugin extends AbsPostPlugin {

    @Override
    public void parseWord(CorpusChunk dest, String word) {
        dest.addWord(word.toLowerCase());
    }

}
