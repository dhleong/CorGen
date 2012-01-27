package net.dhleong.corgen;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A CorpusChunk is one element of a Corpus at
 *  a specified granularity
 * @author dhleong
 *
 */
public class CorpusChunk implements Iterable<String> {

    public enum Granularity {
        METHOD,
        CLASS,
        PACKAGE
    }
    
    List<String> mWords = new ArrayList<String>();
    private final String mName;
    
    public CorpusChunk(String name) {
        mName = name;
    }

    public void addWord(String word) {
        mWords.add(word);
    }
    
    /**
     * Get the name of this chunk. If method granularity,
     *  this is the method's name, etc.
     */
    public String getName() {
        return mName;
    }

    @Override
    public Iterator<String> iterator() {
        return mWords.iterator();
    }

    public void addAll(CorpusChunk other) {
        mWords.addAll(other.mWords);
    }

    public void clear() {
        mWords.clear();
    }
}
