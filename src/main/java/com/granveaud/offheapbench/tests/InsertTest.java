package com.granveaud.offheapbench.tests;

import com.granveaud.offheapbench.bean.Bean;

public class InsertTest extends AbstractTest {
    @Override
    public void run() {
        for (int i = 0; i < NB_RECORDS; i++) {
            Bean bean = new Bean(10, 5);
            store.put(i, bean);
        }

        store.displayStats();
    }
}
