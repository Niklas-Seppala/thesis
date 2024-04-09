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

Benchmark                                                                     Mode  Cnt           Score       Error   Units
ThroughPutAccessBenchmark.FFM_indexWordAccess                                thrpt    5      127821.886 ±  1065.123   ops/s
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.alloc.rate                  thrpt    5          84.840 ±     0.706  MB/sec
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.alloc.rate.norm             thrpt    5         696.007 ±     0.060    B/op
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.count                       thrpt    5          29.000              counts
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.time                        thrpt    5          16.000                  ms
ThroughPutAccessBenchmark.JNA_indexWordAccess                                thrpt    5       87325.501 ± 18812.527   ops/s
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.alloc.rate                  thrpt    5          71.598 ±    10.691  MB/sec
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.alloc.rate.norm             thrpt    5         861.611 ±   202.590    B/op
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.count                       thrpt    5          29.000              counts
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.time                        thrpt    5        2018.000                  ms
ThroughPutAccessBenchmark.JNI_indexWordAccess                                thrpt    5      117667.429 ± 10115.148   ops/s
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.alloc.rate                  thrpt    5          44.697 ±     2.985  MB/sec
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.alloc.rate.norm             thrpt    5         400.008 ±     0.064    B/op
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.count                       thrpt    5          10.000              counts
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.time                        thrpt    5        2082.000                  ms
ThroughPutAccessBenchmark.POJO_OPTIMIZED_indexWordAccess                     thrpt    5      153415.043 ±   605.119   ops/s
ThroughPutAccessBenchmark.POJO_OPTIMIZED_indexWordAccess:gc.alloc.rate       thrpt    5          67.885 ±     0.265  MB/sec
ThroughPutAccessBenchmark.POJO_OPTIMIZED_indexWordAccess:gc.alloc.rate.norm  thrpt    5         464.006 ±     0.050    B/op
ThroughPutAccessBenchmark.POJO_OPTIMIZED_indexWordAccess:gc.count            thrpt    5          23.000              counts
ThroughPutAccessBenchmark.POJO_OPTIMIZED_indexWordAccess:gc.time             thrpt    5          13.000                  ms
ThroughPutAccessBenchmark.POJO_indexWordAccess                               thrpt    5      155094.627 ±  1921.303   ops/s
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.alloc.rate                 thrpt    5          68.628 ±     0.854  MB/sec
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.alloc.rate.norm            thrpt    5         464.006 ±     0.049    B/op
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.count                      thrpt    5          24.000              counts
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.time                       thrpt    5          13.000                  ms
BulkBenchmark.FFM_bulkCreate                                                    ss             6360.806               ms/op
BulkBenchmark.FFM_bulkCreate:gc.alloc.rate                                      ss                0.648              MB/sec
BulkBenchmark.FFM_bulkCreate:gc.alloc.rate.norm                                 ss          4392344.000                B/op
BulkBenchmark.FFM_bulkCreate:gc.count                                           ss                  ≈ 0              counts
BulkBenchmark.JNA_bulkCreate                                                    ss             6090.443               ms/op
BulkBenchmark.JNA_bulkCreate:gc.alloc.rate                                      ss                0.686              MB/sec
BulkBenchmark.JNA_bulkCreate:gc.alloc.rate.norm                                 ss          4452160.000                B/op
BulkBenchmark.JNA_bulkCreate:gc.count                                           ss                  ≈ 0              counts
BulkBenchmark.JNI_bulkCreate                                                    ss             6207.105               ms/op
BulkBenchmark.JNI_bulkCreate:gc.alloc.rate                                      ss                0.637              MB/sec
BulkBenchmark.JNI_bulkCreate:gc.alloc.rate.norm                                 ss          4220728.000                B/op
BulkBenchmark.JNI_bulkCreate:gc.count                                           ss                  ≈ 0              counts
BulkBenchmark.POJO_bulkCreate                                                   ss           264190.247               ms/op
BulkBenchmark.POJO_bulkCreate:gc.alloc.rate                                     ss               32.047              MB/sec
BulkBenchmark.POJO_bulkCreate:gc.alloc.rate.norm                                ss       8880651704.000                B/op
BulkBenchmark.POJO_bulkCreate:gc.count                                          ss              107.000              counts
BulkBenchmark.POJO_bulkCreate:gc.time                                           ss             1340.000                  ms
ColdStartBenchmark.FFM_coldStartIndexing                                        ss              101.580               ms/op
ColdStartBenchmark.FFM_coldStartIndexing:gc.alloc.rate                          ss               20.206              MB/sec
ColdStartBenchmark.FFM_coldStartIndexing:gc.alloc.rate.norm                     ss          4354040.000                B/op
ColdStartBenchmark.FFM_coldStartIndexing:gc.count                               ss                  ≈ 0              counts
ColdStartBenchmark.JNA_coldStartIndexing                                        ss              104.666               ms/op
ColdStartBenchmark.JNA_coldStartIndexing:gc.alloc.rate                          ss               19.611              MB/sec
ColdStartBenchmark.JNA_coldStartIndexing:gc.alloc.rate.norm                     ss          4411344.000                B/op
ColdStartBenchmark.JNA_coldStartIndexing:gc.count                               ss                  ≈ 0              counts
ColdStartBenchmark.JNI_coldStartIndexing                                        ss               94.001               ms/op
ColdStartBenchmark.JNI_coldStartIndexing:gc.alloc.rate                          ss               23.185              MB/sec
ColdStartBenchmark.JNI_coldStartIndexing:gc.alloc.rate.norm                     ss          4204160.000                B/op
ColdStartBenchmark.JNI_coldStartIndexing:gc.count                               ss                  ≈ 0              counts
ColdStartBenchmark.POJO_coldStartIndexing                                       ss             4211.509               ms/op
ColdStartBenchmark.POJO_coldStartIndexing:gc.alloc.rate                         ss               31.551              MB/sec
ColdStartBenchmark.POJO_coldStartIndexing:gc.alloc.rate.norm                    ss        142936216.000                B/op
ColdStartBenchmark.POJO_coldStartIndexing:gc.count                              ss                2.000              counts
ColdStartBenchmark.POJO_coldStartIndexing:gc.time                               ss               14.000                  ms                             ss               14.000                ms
```
