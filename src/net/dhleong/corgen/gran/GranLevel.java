package net.dhleong.corgen.gran;

import net.dhleong.corgen.CorpusChunk;

public interface GranLevel {
    
    public static final int PACKAGE = 0;
    public static final int CLASS   = 1;
    public static final int METHOD  = 2;
    public static final int WORD    = 3;

    void addToChunk(CorpusChunk c);
    
    int getLevel();
    
    String getName();

    void merge(GranLevel other);
}
