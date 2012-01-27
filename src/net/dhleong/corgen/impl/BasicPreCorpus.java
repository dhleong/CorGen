package net.dhleong.corgen.impl;

import java.util.LinkedList;

import net.dhleong.corgen.CorpusChunk;
import net.dhleong.corgen.CorpusChunk.Granularity;
import net.dhleong.corgen.PreCorpus;
import net.dhleong.corgen.gran.ClassLevel;
import net.dhleong.corgen.gran.GranLevel;
import net.dhleong.corgen.gran.MethodLevel;
import net.dhleong.corgen.gran.PackageLevel;
import net.dhleong.corgen.gran.ProjectLevel;

/**
 * 
 * @author dhleong
 *
 */
public class BasicPreCorpus implements PreCorpus {
    
    private final ProjectLevel mData = new ProjectLevel();
    
    public void add(GranLevel item) {
        mData.add(item);
    }
    
    @Override
    public Iterable<CorpusChunk> getChunks(Granularity granularity) {
        LinkedList<CorpusChunk> chunks = new LinkedList<CorpusChunk>();
        
        // TODO better would be to make a real iterator and load this
        //  lazily, instead of eagerly... but this is fine for now
        switch (granularity) {
        case PACKAGE:
            for (PackageLevel p : mData) 
                chunks.add(p.toChunk());
            break;
        case CLASS:
            for (PackageLevel p : mData) {
                for (ClassLevel c : p)
                    chunks.add(c.toChunk());
            }
            break;
        case METHOD:
            for (PackageLevel p : mData) {
                for (ClassLevel c : p) {
                    for (MethodLevel m : c)
                        chunks.add(m.toChunk());
                }
            }
            break;
        }
        
        return chunks;
    }

}
