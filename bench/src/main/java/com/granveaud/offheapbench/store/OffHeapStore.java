package com.granveaud.offheapbench.store;

import java.io.Serializable;

public interface OffHeapStore {
    void put(Serializable key, Serializable value);

    Object get(Serializable key);

    void remove(Serializable key);

    int size();

    void close();

    void displayStats();
}
