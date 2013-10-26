package com.granveaud.offheapbench.store;

import com.granveaud.directobjects.DirectObject;
import com.granveaud.directobjects.map.DirectMap;
import com.granveaud.offheapbench.bean.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class DirectMapStore implements OffHeapStore {
    final static private Logger LOGGER = LoggerFactory.getLogger(DirectMapStore.class);

    private DirectMap<Serializable, DirectObject> map;

    public DirectMapStore() {
        map = new DirectMap<Serializable, DirectObject>();
    }

    @Override
    public void close() {
        map.clear();
    }

    @Override
    public void put(Serializable key, Serializable value) {
        map.put(key, (DirectObject) value);
    }

    @Override
    public Object get(Serializable key) {
        Bean bean = new Bean(); // warning: we suppose here that values are Bean
        if (map.get(key, bean)) {
            return bean;
        }
        return null;
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
