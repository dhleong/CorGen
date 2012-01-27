package net.dhleong.corgen.plugins;

import net.dhleong.corgen.CorpusChunk;
import net.dhleong.corgen.Plugin;

/**
 * Super basic identifier splitting plugin that splits
 *  on underscore and double colon (::, for C++). 
 *  
 * @author dhleong
 *
 */
@Plugin
public class StupidIdentSplittingPlugin extends AbsPostPlugin {

    @Override
    public void parseWord(CorpusChunk dest, String word) {
        for (String s : word.split("(_|::)"))
            dest.addWord(s);
    }

}
