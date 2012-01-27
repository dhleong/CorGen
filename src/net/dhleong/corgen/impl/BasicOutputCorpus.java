package net.dhleong.corgen.impl;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

import net.dhleong.corgen.CorpusChunk;
import net.dhleong.corgen.OutputCorpus;

/**
 * Simple output corpus that creates two files: corpus.txt
 *  and corpus-mapping.txt. Each line of corpus.txt corresponds
 *  to a line in corpus-mapping.txt; the line in corpus.txt
 *  is a space-delimited word in the corpus, and the line in
 *  corpus-mapping is the name of that chunk of the corpus, 
 *  based on the output granularity.
 *  
 * @author dhleong
 *
 */
public class BasicOutputCorpus implements OutputCorpus {
    
    BufferedWriter outCor, outMap;
    
    /**
     * 
     * @param outputPath String path to the folder where
     *  we should output our corpus.
     */
    public BasicOutputCorpus(String outputPath) {
        this(new File(outputPath));
    }
    
    public BasicOutputCorpus(File outputDir) {
     
        if (!outputDir.exists() && !outputDir.mkdirs()) 
            throw new RuntimeException("Couldn't create output directory " +
                    outputDir.getAbsolutePath());
        
        if (!outputDir.isDirectory())
            throw new IllegalArgumentException("outputPath must be a directory");
        
        try {
            FileWriter fw = new FileWriter(new File(outputDir, "corpus.txt"));
            outCor = new BufferedWriter(fw);
            
            FileWriter fw2 = new FileWriter(new File(outputDir, "corpus-mapping.txt"));
            outMap = new BufferedWriter(fw2);
        } catch (FileNotFoundException e) {
            // shoudln't happen...?
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace(); // ?
        }
    }

    @Override
    public void add(CorpusChunk chunk) {
        try {
            for (String word : chunk) {
                outCor.write(word);
                outCor.write(' ');
            }
            outCor.write('\n');
            outMap.write(chunk.getName());
            outMap.write('\n');
        } catch (IOException e) {
            e.printStackTrace(); // is this a concern?
        }
    }

    @Override
    public void finish() {
        try {
            outCor.flush();
            outCor.close();
            
            outMap.flush();
            outMap.close();
        } catch (IOException e) {
            // we're done anyway, so...
        }
    }
}
