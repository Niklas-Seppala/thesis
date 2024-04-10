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
Benchmark                                                                    Mode  Cnt           Score         Error   Units
ThroughPutAccessBenchmark.FFM_indexWordAccess                               thrpt   25      127497.151 ±     471.365   ops/s
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.alloc.rate                 thrpt   25          84.624 ±       0.313  MB/sec
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.alloc.rate.norm            thrpt   25         696.007 ±       0.011    B/op
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.count                      thrpt   25         142.000                counts
ThroughPutAccessBenchmark.FFM_indexWordAccess:gc.time                       thrpt   25          71.000                    ms
ThroughPutAccessBenchmark.JNA_indexWordAccess                               thrpt   25       86827.140 ±    3343.634   ops/s
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.alloc.rate                 thrpt   25          72.094 ±       2.803  MB/sec
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.alloc.rate.norm            thrpt   25         872.578 ±      43.910    B/op
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.count                      thrpt   25         118.000                counts
ThroughPutAccessBenchmark.JNA_indexWordAccess:gc.time                       thrpt   25       10475.000                    ms
ThroughPutAccessBenchmark.JNI_indexWordAccess                               thrpt   25      117912.382 ±     376.098   ops/s
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.alloc.rate                 thrpt   25          44.979 ±       0.143  MB/sec
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.alloc.rate.norm            thrpt   25         400.008 ±       0.011    B/op
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.count                      thrpt   25          50.000                counts
ThroughPutAccessBenchmark.JNI_indexWordAccess:gc.time                       thrpt   25       10511.000                    ms
ThroughPutAccessBenchmark.POJO_BUFFERED_indexWordAccess                     thrpt   25      154867.888 ±     458.831   ops/s
ThroughPutAccessBenchmark.POJO_BUFFERED_indexWordAccess:gc.alloc.rate       thrpt   25          67.581 ±       1.422  MB/sec
ThroughPutAccessBenchmark.POJO_BUFFERED_indexWordAccess:gc.alloc.rate.norm  thrpt   25         457.606 ±       9.786    B/op
ThroughPutAccessBenchmark.POJO_BUFFERED_indexWordAccess:gc.count            thrpt   25         114.000                counts
ThroughPutAccessBenchmark.POJO_BUFFERED_indexWordAccess:gc.time             thrpt   25          64.000                    ms
ThroughPutAccessBenchmark.POJO_indexWordAccess                              thrpt   25      154555.030 ±     429.548   ops/s
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.alloc.rate                thrpt   25          69.804 ±       1.440  MB/sec
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.alloc.rate.norm           thrpt   25         473.606 ±       9.786    B/op
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.count                     thrpt   25         118.000                counts
ThroughPutAccessBenchmark.POJO_indexWordAccess:gc.time                      thrpt   25          66.000                    ms
BulkBenchmark.FFM_bulkCreate                                                   ss    5        6068.858 ±     267.312   ms/op
BulkBenchmark.FFM_bulkCreate:gc.alloc.rate                                     ss    5           0.679 ±       0.029  MB/sec
BulkBenchmark.FFM_bulkCreate:gc.alloc.rate.norm                                ss    5     4392161.600 ±     330.636    B/op
BulkBenchmark.FFM_bulkCreate:gc.count                                          ss    5             ≈ 0                counts
BulkBenchmark.JNA_bulkCreate                                                   ss    5        6076.802 ±     578.585   ms/op
BulkBenchmark.JNA_bulkCreate:gc.alloc.rate                                     ss    5           0.688 ±       0.069  MB/sec
BulkBenchmark.JNA_bulkCreate:gc.alloc.rate.norm                                ss    5     4451827.200 ±     191.884    B/op
BulkBenchmark.JNA_bulkCreate:gc.count                                          ss    5             ≈ 0                counts
BulkBenchmark.JNI_bulkCreate                                                   ss    5        6096.743 ±     333.520   ms/op
BulkBenchmark.JNI_bulkCreate:gc.alloc.rate                                     ss    5           0.649 ±       0.034  MB/sec
BulkBenchmark.JNI_bulkCreate:gc.alloc.rate.norm                                ss    5     4220408.000 ±   22026.308    B/op
BulkBenchmark.JNI_bulkCreate:gc.count                                          ss    5             ≈ 0                counts
BulkBenchmark.POJO_BUFFERED_bulkCreate                                         ss    5        8749.889 ±     363.475   ms/op
BulkBenchmark.POJO_BUFFERED_bulkCreate:gc.alloc.rate                           ss    5         760.000 ±      31.814  MB/sec
BulkBenchmark.POJO_BUFFERED_bulkCreate:gc.alloc.rate.norm                      ss    5  7047244449.600 ±   13911.424    B/op
BulkBenchmark.POJO_BUFFERED_bulkCreate:gc.count                                ss    5         106.000                counts
BulkBenchmark.POJO_BUFFERED_bulkCreate:gc.time                                 ss    5        5128.000                    ms
BulkBenchmark.POJO_bulkCreate                                                  ss    5      262473.587 ±    1832.814   ms/op
BulkBenchmark.POJO_bulkCreate:gc.alloc.rate                                    ss    5          32.239 ±       0.216  MB/sec
BulkBenchmark.POJO_bulkCreate:gc.alloc.rate.norm                               ss    5  8875854852.800 ± 5136313.598    B/op
BulkBenchmark.POJO_bulkCreate:gc.count                                         ss    5         535.000                counts
BulkBenchmark.POJO_bulkCreate:gc.time                                          ss    5        6687.000                    ms
ColdStartBenchmark.FFM_coldStartIndexing                                       ss               97.171                 ms/op
ColdStartBenchmark.FFM_coldStartIndexing:gc.alloc.rate                         ss               21.141                MB/sec
ColdStartBenchmark.FFM_coldStartIndexing:gc.alloc.rate.norm                    ss          4362728.000                  B/op
ColdStartBenchmark.FFM_coldStartIndexing:gc.count                              ss                  ≈ 0                counts
ColdStartBenchmark.JNA_coldStartIndexing                                       ss              104.623                 ms/op
ColdStartBenchmark.JNA_coldStartIndexing:gc.alloc.rate                         ss               19.142                MB/sec
ColdStartBenchmark.JNA_coldStartIndexing:gc.alloc.rate.norm                    ss          4411616.000                  B/op
ColdStartBenchmark.JNA_coldStartIndexing:gc.count                              ss                  ≈ 0                counts
ColdStartBenchmark.JNI_coldStartIndexing                                       ss               96.569                 ms/op
ColdStartBenchmark.JNI_coldStartIndexing:gc.alloc.rate                         ss               22.165                MB/sec
ColdStartBenchmark.JNI_coldStartIndexing:gc.alloc.rate.norm                    ss          4204160.000                  B/op
ColdStartBenchmark.JNI_coldStartIndexing:gc.count                              ss                  ≈ 0                counts
ColdStartBenchmark.POJO_BUFFERED_coldStartIndexing                             ss              186.121                 ms/op
ColdStartBenchmark.POJO_BUFFERED_coldStartIndexing:gc.alloc.rate               ss              396.913                MB/sec
ColdStartBenchmark.POJO_BUFFERED_coldStartIndexing:gc.alloc.rate.norm          ss        114374792.000                  B/op
ColdStartBenchmark.POJO_BUFFERED_coldStartIndexing:gc.count                    ss                2.000                counts
ColdStartBenchmark.POJO_BUFFERED_coldStartIndexing:gc.time                     ss               14.000                    ms
ColdStartBenchmark.POJO_coldStartIndexing                                      ss             4153.910                 ms/op
ColdStartBenchmark.POJO_coldStartIndexing:gc.alloc.rate                        ss               32.054                MB/sec
ColdStartBenchmark.POJO_coldStartIndexing:gc.alloc.rate.norm                   ss        142936320.000                  B/op
ColdStartBenchmark.POJO_coldStartIndexing:gc.count                             ss                3.000                counts
ColdStartBenchmark.POJO_coldStartIndexing:gc.time                              ss               32.000                    ms
```
