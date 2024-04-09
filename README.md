# thesis

![](https://github.com/Niklas-Seppala/thesis-jni/actions/workflows/gradle.yml/badge.svg)

## NOTES
[Javadoc](https://niklas-seppala.github.io/thesis/)

### Benchmarks
```
VM version: JDK 19.0.2, OpenJDK 64-Bit Server VM, 19.0.2+7-Ubuntu-0ubuntu322.04
VM invoker: /usr/lib/jvm/java-19-openjdk-amd64/bin/java
VM options: --enable-preview --enable-native-access=ALL-UNNAMED -Xmx4G
------------------------------------------------------------------------

ThroughPutAccessBenchmark.FFM_indexWordAccess                                thrpt    5         128.400 ±   0.815  ops/ms
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.alloc.rate                  thrpt    5          85.224 ±   0.540  MB/sec
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.alloc.rate.norm             thrpt    5         696.007 ±   0.059    B/op
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.count                       thrpt    5          29.000            counts
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.time                        thrpt    5          13.000                ms
ThroughPutAccessBenchmark.JNA_indexWordAccess                                thrpt    5          87.809 ±  18.638  ops/ms
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.alloc.rate                  thrpt    5          73.373 ±  21.672  MB/sec
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.alloc.rate.norm             thrpt    5         876.360 ± 196.963    B/op
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.count                       thrpt    5          25.000            counts
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.time                        thrpt    5        1920.000                ms
ThroughPutAccessBenchmark.JNI_indexWordAccess                                thrpt    5         119.156 ±   5.052  ops/ms
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.alloc.rate                  thrpt    5          45.454 ±   1.927  MB/sec
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.alloc.rate.norm             thrpt    5         400.008 ±   0.063    B/op
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.count                       thrpt    5           9.000            counts
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.time                        thrpt    5        1889.000                ms
ThroughPutAccessBenchmark.POJO_OPTIMIZED_indexWordAccess                     thrpt    5         155.800 ±   1.018  ops/ms
ThroughPutAccessBenchmark.POJO_OPTIMIZED_indexWordAccess:gc.alloc.rate       thrpt    5          66.563 ±   0.434  MB/sec
ThroughPutAccessBenchmark.POJO_OPTIMIZED_indexWordAccess:gc.alloc.rate.norm  thrpt    5         448.006 ±   0.049    B/op
ThroughPutAccessBenchmark.POJO_OPTIMIZED_indexWordAccess:gc.count            thrpt    5          22.000            counts
ThroughPutAccessBenchmark.POJO_OPTIMIZED_indexWordAccess:gc.time             thrpt    5          11.000                ms
ThroughPutAccessBenchmark.POJO_indexWordAccess                               thrpt    5         154.590 ±   1.096  ops/ms
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.alloc.rate                 thrpt    5          66.046 ±   0.467  MB/sec
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.alloc.rate.norm            thrpt    5         448.006 ±   0.049    B/op
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.count                      thrpt    5          22.000            counts
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.time                       thrpt    5          10.000                ms
BulkBenchmark.FFM_bulkCreate                                                    ss             4946.092             ms/op
BulkBenchmark.FFM_bulkCreate:gc.alloc.rate                                      ss                0.526            MB/sec
BulkBenchmark.FFM_bulkCreate:gc.alloc.rate.norm                                 ss          2731096.000              B/op
BulkBenchmark.FFM_bulkCreate:gc.count                                           ss                  ≈ 0            counts
BulkBenchmark.JNA_bulkCreate                                                    ss             4935.460             ms/op
BulkBenchmark.JNA_bulkCreate:gc.alloc.rate                                      ss                0.448            MB/sec
BulkBenchmark.JNA_bulkCreate:gc.alloc.rate.norm                                 ss          2323696.000              B/op
BulkBenchmark.JNA_bulkCreate:gc.count                                           ss                  ≈ 0            counts
BulkBenchmark.JNI_bulkCreate                                                    ss             4906.291             ms/op
BulkBenchmark.JNI_bulkCreate:gc.alloc.rate                                      ss                0.066            MB/sec
BulkBenchmark.JNI_bulkCreate:gc.alloc.rate.norm                                 ss           338592.000              B/op
BulkBenchmark.JNI_bulkCreate:gc.count                                           ss                  ≈ 0            counts
BulkBenchmark.POJO_OPTIMIZED_bulkCreate                                         ss             8339.052             ms/op
BulkBenchmark.POJO_OPTIMIZED_bulkCreate:gc.alloc.rate                           ss              805.327            MB/sec
BulkBenchmark.POJO_OPTIMIZED_bulkCreate:gc.alloc.rate.norm                      ss       7050119040.000              B/op
BulkBenchmark.POJO_OPTIMIZED_bulkCreate:gc.count                                ss               20.000            counts
BulkBenchmark.POJO_OPTIMIZED_bulkCreate:gc.time                                 ss              980.000                ms
BulkBenchmark.POJO_bulkCreate                                                   ss           263725.438             ms/op
BulkBenchmark.POJO_bulkCreate:gc.alloc.rate                                     ss               31.204            MB/sec
BulkBenchmark.POJO_bulkCreate:gc.alloc.rate.norm                                ss       8629339264.000              B/op
BulkBenchmark.POJO_bulkCreate:gc.count                                          ss              105.000            counts
BulkBenchmark.POJO_bulkCreate:gc.time                                           ss             1272.000                ms
ColdStartBenchmark.FFM_coldStartIndexing                                        ss              120.552             ms/op
ColdStartBenchmark.FFM_coldStartIndexing:gc.alloc.rate                          ss               20.134            MB/sec
ColdStartBenchmark.FFM_coldStartIndexing:gc.alloc.rate.norm                     ss          2702184.000              B/op
ColdStartBenchmark.FFM_coldStartIndexing:gc.count                               ss                  ≈ 0            counts
ColdStartBenchmark.JNA_coldStartIndexing                                        ss              122.149             ms/op
ColdStartBenchmark.JNA_coldStartIndexing:gc.alloc.rate                          ss               16.772            MB/sec
ColdStartBenchmark.JNA_coldStartIndexing:gc.alloc.rate.norm                     ss          2278488.000              B/op
ColdStartBenchmark.JNA_coldStartIndexing:gc.count                               ss                  ≈ 0            counts
ColdStartBenchmark.JNI_coldStartIndexing                                        ss              100.792             ms/op
ColdStartBenchmark.JNI_coldStartIndexing:gc.alloc.rate                          ss                2.839            MB/sec
ColdStartBenchmark.JNI_coldStartIndexing:gc.alloc.rate.norm                     ss           331160.000              B/op
ColdStartBenchmark.JNI_coldStartIndexing:gc.count                               ss                  ≈ 0            counts
ColdStartBenchmark.POJO_OPTIMIZED_coldStartIndexing                             ss              197.291             ms/op
ColdStartBenchmark.POJO_OPTIMIZED_coldStartIndexing:gc.alloc.rate               ss              508.369            MB/sec
ColdStartBenchmark.POJO_OPTIMIZED_coldStartIndexing:gc.alloc.rate.norm          ss        110532512.000              B/op
ColdStartBenchmark.POJO_OPTIMIZED_coldStartIndexing:gc.count                    ss                2.000            counts
ColdStartBenchmark.POJO_OPTIMIZED_coldStartIndexing:gc.time                     ss               10.000                ms
ColdStartBenchmark.POJO_coldStartIndexing                                       ss             4178.876             ms/op
ColdStartBenchmark.POJO_coldStartIndexing:gc.alloc.rate                         ss               31.483            MB/sec
ColdStartBenchmark.POJO_coldStartIndexing:gc.alloc.rate.norm                    ss        138292872.000              B/op
ColdStartBenchmark.POJO_coldStartIndexing:gc.count                              ss                2.000            counts
ColdStartBenchmark.POJO_coldStartIndexing:gc.time                               ss               14.000                ms
```
