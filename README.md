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
Benchmark                                                           Mode  Cnt           Score           Error   Units
ThroughPutAccessBenchmark.FFM_indexWordAccess                      thrpt   25      127749.878 ±       719.310   ops/s
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.alloc.rate        thrpt   25          84.792 ±         0.477  MB/sec
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.alloc.rate.norm   thrpt   25         696.007 ±         0.011    B/op
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.count             thrpt   25         145.000                  counts
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.time              thrpt   25          74.000                      ms
ThroughPutAccessBenchmark.JNA_indexWordAccess                      thrpt   25       87132.659 ±      3921.816   ops/s
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.alloc.rate        thrpt   25          71.898 ±         2.756  MB/sec
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.alloc.rate.norm   thrpt   25         867.618 ±        43.757    B/op
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.count             thrpt   25         121.000                  counts
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.time              thrpt   25       10708.000                      ms
ThroughPutAccessBenchmark.JNI_indexWordAccess                      thrpt   25      117969.185 ±       460.321   ops/s
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.alloc.rate        thrpt   25          45.000 ±         0.176  MB/sec
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.alloc.rate.norm   thrpt   25         400.008 ±         0.012    B/op
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.count             thrpt   25          50.000                  counts
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.time              thrpt   25       10482.000                      ms
ThroughPutAccessBenchmark.POJO_indexWordAccess                     thrpt   25      153751.833 ±       925.130   ops/s
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.alloc.rate       thrpt   25          67.559 ±         1.675  MB/sec
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.alloc.rate.norm  thrpt   25         460.806 ±        11.985    B/op
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.count            thrpt   25         114.000                  counts
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.time             thrpt   25          64.000                      ms
BulkBenchmark.FFM_bulkCreate                                          ss    5        6172.116 ±       429.652   ms/op
BulkBenchmark.FFM_bulkCreate:gc.alloc.rate                            ss    5           0.668 ±         0.043  MB/sec
BulkBenchmark.FFM_bulkCreate:gc.alloc.rate.norm                       ss    5     4389105.600 ±     17385.059    B/op
BulkBenchmark.FFM_bulkCreate:gc.count                                 ss    5             ≈ 0                  counts
BulkBenchmark.JNA_bulkCreate                                          ss    5        6064.801 ±       751.437   ms/op
BulkBenchmark.JNA_bulkCreate:gc.alloc.rate                            ss    5           0.689 ±         0.079  MB/sec
BulkBenchmark.JNA_bulkCreate:gc.alloc.rate.norm                       ss    5     4450444.800 ±     26404.863    B/op
BulkBenchmark.JNA_bulkCreate:gc.count                                 ss    5             ≈ 0                  counts
BulkBenchmark.JNI_bulkCreate                                          ss    5        6082.662 ±       582.326   ms/op
BulkBenchmark.JNI_bulkCreate:gc.alloc.rate                            ss    5           0.652 ±         0.062  MB/sec
BulkBenchmark.JNI_bulkCreate:gc.alloc.rate.norm                       ss    5     4222286.400 ±     26797.989    B/op
BulkBenchmark.JNI_bulkCreate:gc.count                                 ss    5             ≈ 0                  counts
BulkBenchmark.POJO_bulkCreate                                         ss    5      262362.337 ±      3274.501   ms/op
BulkBenchmark.POJO_bulkCreate:gc.alloc.rate                           ss    5          31.690 ±         1.424  MB/sec
BulkBenchmark.POJO_bulkCreate:gc.alloc.rate.norm                      ss    5  8721046396.800 ± 337143597.964    B/op
BulkBenchmark.POJO_bulkCreate:gc.count                                ss    5         532.000                  counts
BulkBenchmark.POJO_bulkCreate:gc.time                                 ss    5        6660.000                      ms
ColdStartBenchmark.FFM_coldStartIndexing                              ss    5         100.599 ±         7.887   ms/op
ColdStartBenchmark.FFM_coldStartIndexing:gc.alloc.rate                ss    5          20.350 ±         4.337  MB/sec
ColdStartBenchmark.FFM_coldStartIndexing:gc.alloc.rate.norm           ss    5     4362577.600 ±     22589.676    B/op
ColdStartBenchmark.FFM_coldStartIndexing:gc.count                     ss    5             ≈ 0                  counts
ColdStartBenchmark.JNA_coldStartIndexing                              ss    5         101.849 ±         9.452   ms/op
ColdStartBenchmark.JNA_coldStartIndexing:gc.alloc.rate                ss    5          21.600 ±         5.787  MB/sec
ColdStartBenchmark.JNA_coldStartIndexing:gc.alloc.rate.norm           ss    5     4409993.600 ±     13942.495    B/op
ColdStartBenchmark.JNA_coldStartIndexing:gc.count                     ss    5             ≈ 0                  counts
ColdStartBenchmark.JNI_coldStartIndexing                              ss    5          96.623 ±        12.662   ms/op
ColdStartBenchmark.JNI_coldStartIndexing:gc.alloc.rate                ss    5          20.597 ±         3.398  MB/sec
ColdStartBenchmark.JNI_coldStartIndexing:gc.alloc.rate.norm           ss    5     4209036.800 ±     17593.944    B/op
ColdStartBenchmark.JNI_coldStartIndexing:gc.count                     ss    5             ≈ 0                  counts
ColdStartBenchmark.POJO_coldStartIndexing                             ss    5        4151.374 ±        66.291   ms/op
ColdStartBenchmark.POJO_coldStartIndexing:gc.alloc.rate               ss    5          32.071 ±         0.735  MB/sec
ColdStartBenchmark.POJO_coldStartIndexing:gc.alloc.rate.norm          ss    5   142934964.800 ±     14610.828    B/op
ColdStartBenchmark.POJO_coldStartIndexing:gc.count                    ss    5          10.000                  counts
ColdStartBenchmark.POJO_coldStartIndexing:gc.time                     ss    5          68.000                      ms
```
