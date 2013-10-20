package com.granveaud.offheapbench.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class HeapStore implements OffHeapStore {
    final static private Logger LOGGER = LoggerFactory.getLogger(HeapStore.class);

    private Map<Serializable, Serializable> map;

    public HeapStore() {
        map = new HashMap<Serializable, Serializable>();
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
