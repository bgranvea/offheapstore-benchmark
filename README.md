offheapstore-benchmark
======================

A simple benchmark of some off heap solutions:

- BigMemory Go (http://terracotta.org/products/bigmemorygo)
- MapDB (http://www.mapdb.org/)
- FSTOffHeapMap (https://code.google.com/p/fast-serialization/)
- a Java Chronicle based map (https://github.com/OpenHFT/Java-Chronicle/)
- a simple JNA based map (https://jna.java.net/)
- an Apache DirectMemory based map (http://directmemory.apache.org/)
- my own implementation: DirectMap (https://github.com/bgranvea/directobjects)

We also test a regular heap map to measure the overhead of off heap implementations.

We test different scenarios:

- inserts
- a sequential read of all entries
- random reads
- random reads with a gaussian function so that some elements are more likely to be accessed than others (should improve
performances of implementations like Big Memory Go which uses a heap cache)
- random reads and updates

TODO:
=====
- test with different bean size
- measure memory fragmentation in the read/update scenario
