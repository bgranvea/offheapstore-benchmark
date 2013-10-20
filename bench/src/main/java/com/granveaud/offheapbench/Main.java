package com.granveaud.offheapbench;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Snapshot;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.granveaud.offheapbench.store.*;
import com.granveaud.offheapbench.tests.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class Main {
    final static private Logger LOGGER = LoggerFactory.getLogger(Main.class);

    final static private int NB_WARMUPS = 1;
    final static private int NB_RUNS = 2;

    @SuppressWarnings("unchecked")
    final static private List<Class<? extends OffHeapStore>> STORE_CLASSES = Lists.<Class<? extends OffHeapStore>>newArrayList(
            HeapStore.class,
            BigMemoryGoStore.class,
            MapDBStore.class,
            ChronicleStore.class,
            JNAStore.class,
            FSTStore.class
            /*DirectMemoryStore.class*/
    );

    @SuppressWarnings("unchecked")
    final static private List<Class<? extends AbstractTest>> TEST_CLASSES = Lists.<Class<? extends AbstractTest>>newArrayList(
            InsertTest.class,
            SequentialReadTest.class,
            RandomReadTest.class,
            GaussianRandomReadTest.class,
            RandomReadWriteTest.class
    );

    final static private MetricRegistry metrics = new MetricRegistry();

    final static private File OUTPUT_CSV = new File("results.csv");
    final static private Charset UTF8 = Charset.forName("UTF8");

    public static void main(String[] args) throws IOException {
        Files.write("test\tstore\tcount\tmin\tmax\tmean\tmedian\tp75\tp95\tp98\t99\tstatus\n", OUTPUT_CSV, UTF8);

        for (Class<? extends Test> t : TEST_CLASSES) {
            for (Class<? extends OffHeapStore> s : STORE_CLASSES) {
                runTest(t, s);
            }
        }
    }

    private static void runTest(Class<? extends Test> testClass, Class<? extends OffHeapStore> storeClass) throws IOException {
        Histogram histo = metrics.histogram("times-" + testClass.getSimpleName() + "-" + storeClass);

        Throwable throwable = null;
        OffHeapStore store = null;
        try {
            for (int i = -NB_WARMUPS; i < NB_RUNS; i++) {
                gc();

                LOGGER.info((i < 0 ? "Warmup..." : "Run " + i));

                LOGGER.info("Preparing " + testClass.getSimpleName() + " with store " + storeClass.getSimpleName());

                Test test = testClass.newInstance();
                store = storeClass.newInstance();

                test.setUp(store);

                LOGGER.info("Running " + testClass.getSimpleName() + " with store " + storeClass.getSimpleName());

                long time0 = System.currentTimeMillis();
                test.run();
                histo.update(System.currentTimeMillis() - time0);

                LOGGER.info("Finishing " + testClass.getSimpleName() + " with store " + storeClass.getSimpleName());
                test.cleanUp();
            }
        } catch (Throwable t) {
            throwable = t;
            LOGGER.info("Error during test", t);
        } finally {
            // always cleanup store
            try {
                store.close();
            } catch (Throwable t2) {
            }
        }

        displayResults(testClass, storeClass, histo, throwable);
        saveResults(testClass, storeClass, histo, throwable);
    }

    private static void gc() {
        LOGGER.info("Calling GC and sleeping 20s");
        System.gc();
        try {
            Thread.sleep(20000);
        } catch (InterruptedException e) {
        }
    }

    private static void displayResults(Class<? extends Test> testClass, Class<? extends OffHeapStore> storeClass, Histogram histo, Throwable t) throws IOException {
        Snapshot snapshot = histo.getSnapshot();

        LOGGER.info(
                String.format(
                        "Results: %s %s count=%d min=%f max=%f mean=%f median=%f 75p=%f 95p=%f 98p=%f 99p=%f status=%s",
                        testClass.getSimpleName(),
                        storeClass.getSimpleName(),
                        histo.getCount(),
                        snapshot.getMin() / 1000.0,
                        snapshot.getMax() / 1000.0,
                        snapshot.getMean() / 1000.0,
                        snapshot.getMedian() / 1000.0,
                        snapshot.get75thPercentile() / 1000.0,
                        snapshot.get95thPercentile() / 1000.0,
                        snapshot.get98thPercentile() / 1000.0,
                        snapshot.get99thPercentile() / 1000.0,
                        t != null ? t.getMessage() : "OK"
                )
        );
    }

    private static void saveResults(Class<? extends Test> testClass, Class<? extends OffHeapStore> storeClass, Histogram histo, Throwable t) throws IOException {
        Snapshot snapshot = histo.getSnapshot();

        Files.append(
                String.format(
                        "%s\t%s\t%d\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%f\t%s\n",
                        testClass.getSimpleName(),
                        storeClass.getSimpleName(),
                        histo.getCount(),
                        snapshot.getMin() / 1000.0,
                        snapshot.getMax() / 1000.0,
                        snapshot.getMean() / 1000.0,
                        snapshot.getMedian() / 1000.0,
                        snapshot.get75thPercentile() / 1000.0,
                        snapshot.get95thPercentile() / 1000.0,
                        snapshot.get98thPercentile() / 1000.0,
                        snapshot.get99thPercentile() / 1000.0,
                        t != null ? t.getMessage() : "OK"
                ),
                OUTPUT_CSV,
                UTF8);
    }
}
