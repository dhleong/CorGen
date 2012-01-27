package net.dhleong.corgen.gran;

import java.util.HashMap;
import java.util.Iterator;

import net.dhleong.corgen.CorpusChunk;

public abstract class AbsGranLevel<E extends GranLevel>
implements GranLevel, Iterable<E> {
    
    private final HashMap<String, E> mItems;
    private final String[] mNames;
    private final String mFullName;
    private final CorpusChunk mWords;
    
    /**
     * Names should be an array of names from Package level 
     *  to Method level, if possible
     * @param names
     */
    public AbsGranLevel(String... names) {
        mItems = new HashMap<String, E>();
        mNames = names;
        
        // create full name (lazily)
        String tmp = mNames[0];
        for (int i=1; i<mNames.length; i++)
            tmp += "/" + mNames[i];
        mFullName = tmp;
        
        mWords = new CorpusChunk(getName() + "::EXTRAS"); // do we need the name...?
    }
    
    /**
     * Add an item. If belongs at a lower level, 
     *  we pass it down. EG: If we're adding a method
     *  to a package level, we want to find the appropriate
     *  class within our package and add it to that 
     * 
     * NOTE: If it's a word, we add it directly 
     * @param item
     * @return True if we added it
     */
    @SuppressWarnings("unchecked")
    public void add(GranLevel item) {
        if (item instanceof Word) {
            mWords.addWord(item.getName());
            return;
        }
        
        // make sure it fits at our level
        if (item.getLevel() == getLevel()+1) {
            if (mItems.containsKey(item.getName())) {
                mItems.get(item.getName()).merge(item);
            } else
                mItems.put( item.getName(), (E) item );
            return;
        }
        
        // pass to the right kid 
        AbsGranLevel<?> cast = (AbsGranLevel<?>) item;
        String kidName = cast.getLevelName(getLevel()+1);
        E kid = mItems.get(kidName);
        if (kid == null) {
            // this should be fine now...?
//            System.err.println("!!! In " + getName() +"; no kid " + 
//                    cast.getLevelName(getLevel()+1));
            kid = generateItem(kidName);
            mItems.put(kidName, kid);
        }
        ((AbsGranLevel<?>) kid).add(item);
    }
    
    /**
     * We might be trying to add a kid of a class
     *  that is not yet inserted... generate it please
     * @param name
     * @return
     */
    protected abstract E generateItem(String name);

    @Override
    public void addToChunk(CorpusChunk c) {
        for (E item : mItems.values())
            item.addToChunk(c);
        c.addAll(mWords);
    }

    public String getLevelName(int level)  {
        assert level >= 0 && level < mNames.length;
        return mNames[level];
    }

    @Override
    public String getName() {
        return mNames[getLevel()];
    }

    public boolean has(E item) {
        return mItems.containsKey(item.getName());
    }
    
    @Override
    public void merge(GranLevel other) {
        AbsGranLevel<?> o = (AbsGranLevel<?>) other;
        mWords.addAll(o.mWords);
//        mItems.putAll(o.mItems); // ?
    }

    @Override
    public Iterator<E> iterator() {
        return mItems.values().iterator();
    }
    
    public CorpusChunk toChunk() {
        CorpusChunk c = new CorpusChunk(mFullName);
        addToChunk(c);
        return c;
    }
}
