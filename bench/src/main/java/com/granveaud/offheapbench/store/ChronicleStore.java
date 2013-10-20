package com.granveaud.offheapbench.store;

import com.granveaud.offheapbench.bean.Bean;
import com.granveaud.offheapbench.utils.FileUtils;
import gnu.trove.impl.Constants;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.iterator.TLongLongIterator;
import gnu.trove.list.TLongList;
import gnu.trove.list.array.TLongArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.TLongIntMap;
import gnu.trove.map.TLongLongMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TLongIntHashMap;
import gnu.trove.map.hash.TLongLongHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.procedure.TIntProcedure;
import net.openhft.chronicle.Chronicle;
import net.openhft.chronicle.Excerpt;
import net.openhft.chronicle.ExcerptAppender;
import net.openhft.chronicle.IndexedChronicle;
import net.openhft.lang.io.IOTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

public class ChronicleStore implements OffHeapStore {
    final static private Logger LOGGER = LoggerFactory.getLogger(ChronicleStore.class);

    final static private int NO_ENTRY = -1;

    final static private int CHUNK_SIZE = 32;

    private File file;
    private Chronicle chronicle;
    private Excerpt randomAccessor;
    private ExcerptAppender appender;

    private TObjectLongMap<Serializable> posMap; // key => excerpt position
    private TLongIntMap posSizeMap; // excerpt position => size in chunks (for used excerpts)
    private TIntObjectMap<TLongList> freePosSizeMap; // size in chunks => list of free excerpt position
    private int freePosMaxSize; // max size in chunks contained in freePosSizeMap

    public ChronicleStore() throws IOException {
        file = FileUtils.createTempFile();

        LOGGER.info("Using " + file.getAbsolutePath());

        chronicle = new IndexedChronicle(file.getAbsolutePath());
        posMap = new TObjectLongHashMap<Serializable>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY);
        posSizeMap = new TLongIntHashMap(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY, NO_ENTRY);
        freePosSizeMap = new TIntObjectHashMap<TLongList>(Constants.DEFAULT_CAPACITY, Constants.DEFAULT_LOAD_FACTOR, NO_ENTRY);
        freePosMaxSize = 0;

        appender = chronicle.createAppender();
        randomAccessor = chronicle.createExcerpt();
    }

    @Override
    public void close() {
        IOTools.close(chronicle);
        file.delete();
    }

    @Override
    public void put(Serializable key, Serializable value) {
        Bean b = (Bean) value;
        long beanSize = 4 + b.getChronicleLength();
        int nbChunks = (int) ((beanSize - 1) / CHUNK_SIZE) + 1;
        int paddedSize = nbChunks * CHUNK_SIZE;

        if (posMap.containsKey(key)) {
            // free existing entry
            remove(key);
        }

        // if possible, try to reuse existing free entry
        if (!freePosSizeMap.isEmpty() && putInFreePosition(key, value, nbChunks)) {
            return;
        }

        // append new excerpt
        appender.startExcerpt(paddedSize);
        b.write(appender);
        appender.position(paddedSize); // padding
        appender.finish();

        // update maps
        long pos = appender.lastWrittenIndex();
        posMap.put(key, pos);
        posSizeMap.put(pos, nbChunks);
    }

    private boolean putInFreePosition(Serializable key, Serializable value, int nbChunks) {
        Bean b = (Bean) value;

        // find the smallest block big enough
        int i = nbChunks;
        while (i <= freePosMaxSize) {
            TLongList posList = freePosSizeMap.get(i);
            if (posList == null) {
                i++;
                continue;
            }

            // take last position of list (should be faster)
            long pos = posList.removeAt(posList.size() - 1);

            // remove list if empty
            if (posList.isEmpty()) {
                freePosSizeMap.remove(i);
                determineFreePosMaxSize();
            }

            // update existing entry
            randomAccessor.index(pos);
            b.write(randomAccessor);
            randomAccessor.position(nbChunks * CHUNK_SIZE); // padding
            randomAccessor.finish();

            // update maps
            posMap.put(key, pos);
            posSizeMap.put(pos, nbChunks);

            return true;
        }

        return false;
    }

    private void determineFreePosMaxSize() {
        freePosMaxSize = 0;

        freePosSizeMap.forEachKey(new TIntProcedure() {
            @Override
            public boolean execute(int size) {
                if (size > freePosMaxSize) freePosMaxSize = size;
                return true;
            }
        });
    }

    @Override
    public Object get(Serializable key) {
        long pos = posMap.get(key);
        if (pos == NO_ENTRY) return null;

        Bean b = new Bean();

        // read
        randomAccessor.index(pos);
        b.read(randomAccessor);
        randomAccessor.finish();

        return b;
    }

    @Override
    public void remove(Serializable key) {
        long pos = posMap.get(key);
        if (pos == NO_ENTRY) return;

        // update maps
        int nbChunks = posSizeMap.get(pos);
        posMap.remove(pos);
        posSizeMap.remove(pos);

        TLongList posList = freePosSizeMap.get(nbChunks);
        if (posList == null) {
            posList = new TLongArrayList(Constants.DEFAULT_CAPACITY, NO_ENTRY);
            freePosSizeMap.put(nbChunks, posList);
        }

        posList.add(pos);

        if (nbChunks > freePosMaxSize) freePosMaxSize = nbChunks;
    }

    @Override
    public int size() {
        return posMap.size();
    }

    private int getNbFreeChunks() {
        int res = 0;

        TIntObjectIterator<TLongList> it = freePosSizeMap.iterator();
        while (it.hasNext()) {
            it.advance();
            res += it.key() * it.value().size();
        }

        return res;
    }

    @Override
    public void displayStats() {
        LOGGER.info("Stats: size=" + posMap.size() + " entries / " + appender.lastWrittenIndex() + " chronicle bytes");
        LOGGER.info("Stats: freeChunks={}", getNbFreeChunks());
        LOGGER.info("Stats: freePosMaxSize={}", freePosMaxSize);
    }
}
