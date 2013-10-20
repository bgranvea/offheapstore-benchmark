package com.granveaud.offheapbench.store;

import com.granveaud.offheapbench.bean.Bean;
import de.ruedigermoeller.serialization.FSTConfiguration;
import de.ruedigermoeller.serialization.FSTObjectInput;
import de.ruedigermoeller.serialization.FSTObjectOutput;
import org.apache.directmemory.DirectMemory;
import org.apache.directmemory.cache.CacheService;
import org.apache.directmemory.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;

public class DirectMemoryStore implements OffHeapStore {
    final static private Logger LOGGER = LoggerFactory.getLogger(DirectMemoryStore.class);

    final static private Serializer FST_SERIALIZER = new Serializer() {
        private FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

        @Override
        public <T> byte[] serialize(T obj) throws IOException {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            FSTObjectOutput out = conf.getObjectOutput(bos);
            out.writeObject( obj, Bean.class );
            out.flush();

            return bos.toByteArray();
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T deserialize(byte[] source, Class<T> clazz) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
            FSTObjectInput in = conf.getObjectInput(source, 0, source.length);
            try {
                return (T) in.readObject(clazz);
            } catch (Exception e) {
                throw new IOException("Cannot read object", e);
            }
        }
    };

    private CacheService<Serializable, Serializable> cacheService;

    public DirectMemoryStore() {
        cacheService = new DirectMemory<Serializable, Serializable>()
                .setNumberOfBuffers(60)
                .setSize(10000000)
                .setInitialCapacity(1000000)
                .setConcurrencyLevel(2)
                .setSerializer(FST_SERIALIZER)
                .newCacheService();
    }

    @Override
    public void close() {
        cacheService.clear();
        try {
            cacheService.close();
        } catch (IOException e) {
            LOGGER.warn("Exception during close", e);
        }
    }

    @Override
    public void put(Serializable key, Serializable value) {
        cacheService.put(key, value);
    }

    @Override
    public Object get(Serializable key) {
        return cacheService.retrieve(key);
    }

    @Override
    public void remove(Serializable key) {
        cacheService.free(key);
    }

    @Override
    public int size() {
        return (int) cacheService.entries();
    }

    @Override
    public void displayStats() {
        LOGGER.info("Stats: size=" + size() + " entries");
    }
}
