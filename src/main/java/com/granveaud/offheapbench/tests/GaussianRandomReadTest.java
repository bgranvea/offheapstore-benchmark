package com.granveaud.offheapbench.tests;

import com.granveaud.offheapbench.bean.Bean;
import com.granveaud.offheapbench.store.OffHeapStore;

import java.util.Random;

public class GaussianRandomReadTest extends AbstractTest {

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
        int halfNbRecords = NB_RECORDS / 2;
        for (int i = 0; i < NB_RECORDS; i++) {
            // note: index can be out of range because of the nextGaussian function
            double gaussian = rand.nextGaussian() / 2;
            int index = halfNbRecords + (int) (halfNbRecords * gaussian);

            Bean bean = (Bean) store.get(index);

            if (bean != null && bean.hashCode() != bean.getHashValue()) {
                throw new RuntimeException("hashValue is different");
            }
        }
    }
}
