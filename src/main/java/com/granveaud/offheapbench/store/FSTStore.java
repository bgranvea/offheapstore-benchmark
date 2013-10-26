package com.granveaud.offheapbench.store;

import de.ruedigermoeller.heapoff.FSTOffHeapMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;

public class FSTStore implements OffHeapStore {
    final static private Logger LOGGER = LoggerFactory.getLogger(FSTStore.class);

    private FSTOffHeapMap<Serializable, Serializable> map;

    public FSTStore() {
        try {
            map = new FSTOffHeapMap<Serializable, Serializable>(800);
        } catch (IOException e) {
            throw new RuntimeException("Cannot create FSTOffHeapMap", e);
        }
    }

    @Override
    public void close() {
        map.clear();
    }

    @Override
    public void put(Serializable key, Serializable value) {
        map.put(key, value);
    }

    @Override
    public Object get(Serializable key) {
        return map.get(key);
    }

    @Override
    public void remove(Serializable key) {
        map.remove(key);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public void displayStats() {
        LOGGER.info("Stats: size=" + map.size() + " entries");
    }
}
