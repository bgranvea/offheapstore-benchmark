package com.granveaud.offheapbench.tests;

import com.granveaud.offheapbench.bean.Bean;
import com.granveaud.offheapbench.store.OffHeapStore;

import java.util.Random;

public class RandomReadWriteTest extends AbstractTest {
    private Random rand = new Random();

    @Override
    public void setUp(OffHeapStore store) {
        super.setUp(store);

        for (int i = 0; i < NB_RECORDS; i++) {
            Bean bean = new Bean(10, 5);
            store.put(i, bean);
        }
        store.displayStats();
    }

    @Override
    public void run() {
        for (int i = 0; i < NB_RECORDS; i++) {
            if (rand.nextBoolean()) {
                // replace random bean
                int index = rand.nextInt(NB_RECORDS);
                Bean bean = new Bean(10, 5);
                store.put(index, bean);
            } else {
                // read a random bean
                Bean bean = (Bean) store.get(rand.nextInt(NB_RECORDS));

                if (bean.hashCode() != bean.getHashValue()) {
                    throw new RuntimeException("hashValue is different");
                }
            }
        }
    }
}
