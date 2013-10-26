package com.granveaud.offheapbench.tests;

import com.granveaud.offheapbench.store.OffHeapStore;

public interface Test {
    void setUp(OffHeapStore store);

    void run();

    void cleanUp();
}
