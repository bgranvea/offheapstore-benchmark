package com.granveaud.offheapbench.store;

import com.granveaud.offheapbench.utils.FileUtils;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

public class MapDBStore implements OffHeapStore {
    final static private Logger LOGGER = LoggerFactory.getLogger(MapDBStore.class);

    private DB db;
    private BTreeMap<Serializable, Serializable> map;

    public MapDBStore() throws IOException {
        File dbFile = FileUtils.createTempFile();
        LOGGER.info("Using temp file " + dbFile);
        db = DBMaker
                .newFileDB(dbFile)
                .deleteFilesAfterClose()
                .transactionDisable()
//                .asyncWriteDisable()
                .make();

        map = db.createTreeMap("map")
                .make();
    }

    @Override
    public void close() {
        LOGGER.info("Size=" + map.size()); // force sync

        map.close();
        db.close();
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
    }
}
