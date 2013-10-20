package com.granveaud.offheapbench.store;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;
import net.sf.ehcache.config.MemoryUnit;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerAdapter;
import net.sf.ehcache.statistics.StatisticsGateway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

public class BigMemoryGoStore implements OffHeapStore {
    final static private Logger LOGGER = LoggerFactory.getLogger(BigMemoryGoStore.class);

    private CacheManager cacheManager;
    private Cache cache;
    private boolean cacheFull;

    public BigMemoryGoStore() {
        Configuration managerConfiguration = new Configuration()
                .name("benchmark")
                .cache(new CacheConfiguration()
                        .name("store")
                        .maxBytesLocalHeap(50, MemoryUnit.MEGABYTES)
                        .maxBytesLocalOffHeap(500, MemoryUnit.MEGABYTES)
                        .eternal(true)
                );

        cacheManager = CacheManager.create(managerConfiguration);
        cache = cacheManager.getCache("store");

        // get notified when cache is not big enough
        CacheEventListener evictionListener = new CacheEventListenerAdapter() {
            @Override
            public void notifyElementEvicted(Ehcache ehcache, Element element) {
                cacheFull = true;
            }
        };
        cache.getCacheEventNotificationService().registerListener(evictionListener);
    }

    @Override
    public void close() {
        LOGGER.info("CLOSE STORE");
        if (cacheManager != null) {
            cacheManager.shutdown();
        }
        LOGGER.info("CLOSE STORE OK");
    }

    @Override
    public void put(Serializable key, Serializable value) {
        cache.put(new Element(key, value));

        if (cacheFull) {
            throw new RuntimeException("Cache store is full!");
        }
    }

    @Override
    public Object get(Serializable key) {
        Element elt = cache.get(key);
        return (elt != null ? elt.getObjectValue() : null);
    }

    @Override
    public void remove(Serializable key) {
        cache.remove(key);
    }

    @Override
    public int size() {
        return cache.getSize();
    }

    @Override
    public void displayStats() {
        StatisticsGateway s = cache.getStatistics();
        LOGGER.info("Stats: size=" + s.getSize() + " localHeap=" + s.getLocalHeapSize() + " entries/" + s.getLocalHeapSizeInBytes()
                + " bytes localOffHeap=" + s.getLocalOffHeapSize() + " entries/" + s.getLocalOffHeapSizeInBytes() + " bytes");
    }
}
