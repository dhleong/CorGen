package net.dhleong.corgen.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;

import net.dhleong.corgen.CorpusChunk;
import net.dhleong.corgen.plugins.PorterStemmingPlugin;
import net.dhleong.corgen.plugins.StupidIdentSplittingPlugin;

import org.junit.Test;


public class PluginTests {

    @Test
    public void testPorterStemming() {
        
        PorterStemmingPlugin p = new PorterStemmingPlugin();
        
        assertStems(p, "caress",    "caresses");
        assertStems(p, "poni",      "ponies");
        assertStems(p, "ti",        "ties");
        assertStems(p, "caress",    "caress");
        assertStems(p, "cat",       "cats");
        
        assertStems(p, "feed",      "feed");
        assertStems(p, "agre",      "agreed");
        assertStems(p, "disabl",    "disabled");
        
        assertStems(p, "mat",       "matting");
        assertStems(p, "mate",      "mating");
        assertStems(p, "meet",      "meeting");
        assertStems(p, "mill",      "milling");
        assertStems(p, "mess",      "messing");
        
        assertStems(p, "meet",      "meetings");
    }
    
    @Test
    public void testStupidIdentSplitting() {
        StupidIdentSplittingPlugin p = new StupidIdentSplittingPlugin();
        CorpusChunk out = new CorpusChunk("Test");
        p.parseWord(out, "SomeClass::method_name");
        HashSet<String> set = new HashSet<String>();
        for (String s : out)
            set.add(s);
        
        assertEquals(3, set.size());
        assertTrue(set.contains("SomeClass")); // we're stupid. no camel case
        assertTrue(set.contains("method"));
        assertTrue(set.contains("name"));
    }
    
    private static void assertStems(PorterStemmingPlugin p, String expected, 
            String input) {
        CorpusChunk out = new CorpusChunk("Test");
        p.parseWord(out, input);
        for (String s : out)
            assertEquals(expected, s);
    }
}
