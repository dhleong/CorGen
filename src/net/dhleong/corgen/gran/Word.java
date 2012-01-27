package net.dhleong.corgen.gran;

import net.dhleong.corgen.CorpusChunk;

public class Word implements GranLevel {
    
    private final String mWord;
    
    private int count = 1;

    public Word(String word) {
        mWord = word;
    }
    
    @Override
    public void addToChunk(CorpusChunk c) {
        for (int i=0; i<count; i++)
            c.addWord(mWord);
    }

    @Override
    public String getName() {
        // more or less dummy implementation
        return mWord;
    }

    @Override
    public int getLevel() {
        return WORD;
    }

    @Override
    public void merge(GranLevel other) {
        assert other instanceof Word;
        assert mWord.equals(((Word)other).mWord);
        count++;
    }
}
