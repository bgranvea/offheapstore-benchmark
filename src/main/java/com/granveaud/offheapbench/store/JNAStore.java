package com.granveaud.offheapbench.store;

import com.granveaud.offheapbench.bean.Bean;
import com.sun.jna.Memory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class JNAStore implements OffHeapStore {
    final static private Logger LOGGER = LoggerFactory.getLogger(JNAStore.class);

    // Note: Memory.finalize will free native memory if an element is replaced/removed
    private Map<Serializable, Memory> memoryMap;
    private long allocated;

    public JNAStore() {
        memoryMap = new HashMap<Serializable, Memory>();
    }

    @Override
    public void close() {
        memoryMap.clear();

        // Note: Memory.finalize will free native memory
    }

    @Override
    public void put(Serializable key, Serializable value) {
        Bean b = (Bean) value;

        Memory m = new Memory(b.getJNALength());
        b.write(m);

        memoryMap.put(key, m);

        allocated += m.size();
    }

    @Override
    public Object get(Serializable key) {
        Memory m = memoryMap.get(key);
        if (m == null) return null;

        Bean b = new Bean();
        b.read(m);

        return b;
    }

    @Override
    public void remove(Serializable key) {
        memoryMap.remove(key);
    }

    @Override
    public int size() {
        return memoryMap.size();
    }

    @Override
    public void displayStats() {
        LOGGER.info("Stats: size=" + memoryMap.size() + " entries / allocated=" + allocated);
    }
}
