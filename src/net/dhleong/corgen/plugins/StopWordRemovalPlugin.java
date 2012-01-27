package net.dhleong.corgen.plugins;

import java.util.Arrays;

import net.dhleong.corgen.CorGen.Language;
import net.dhleong.corgen.CorpusChunk;

public abstract class StopWordRemovalPlugin extends AbsPostPlugin {
    
    private final String[] stopWords;
    
    public StopWordRemovalPlugin() {
        stopWords = getStopWords();
        Arrays.sort(stopWords); // pre-sort
    }

    public abstract String[] getStopWords();
    
    @Override
    public void parseWord(CorpusChunk dest, String word) {
        if (Arrays.binarySearch(stopWords, word) < 0)
            dest.addWord(word);
    }

    public static StopWordRemovalPlugin forLanguage(Language lang) {
        switch (lang) {
        case CPP:
            return new CppStopWordRemovalPlugin();
        default:
            throw new IllegalArgumentException();
        }
    }

}
