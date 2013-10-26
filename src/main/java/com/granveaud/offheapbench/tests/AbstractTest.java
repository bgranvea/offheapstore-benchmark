package com.granveaud.offheapbench.tests;

import com.granveaud.offheapbench.store.OffHeapStore;

public abstract class AbstractTest implements Test {
    final static public int NB_RECORDS = 500000;

    protected OffHeapStore store;

    @Override
    public void setUp(OffHeapStore store) {
        this.store = store;
    }

    @Override
    public void cleanUp() {
    }
}
