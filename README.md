# Status
[![CircleCI](https://circleci.com/gh/mtakaki/dropwizard-hikaricp/tree/master.svg?style=svg)](https://circleci.com/gh/mtakaki/dropwizard-hikaricp/tree/master)
[![Coverage Status](https://coveralls.io/repos/github/mtakaki/dropwizard-hikaricp/badge.svg?branch=master)](https://coveralls.io/github/mtakaki/dropwizard-hikaricp?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/b6b6a9a48d334299ab49f012643bc046)](https://www.codacy.com/app/mitsuotakaki/dropwizard-hikaricp)
[![Download](https://maven-badges.herokuapp.com/maven-central/com.github.mtakaki/dropwizard-hikaricp/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.mtakaki/dropwizard-hikaricp)
[![Javadoc](http://javadoc.io/badge/com.github.mtakaki/dropwizard-hikaricp.svg)](http://www.javadoc.io/doc/com.github.mtakaki/dropwizard-hikaricp)
[![Dependency Status](https://www.versioneye.com/user/projects/57e785c679806f002f4ac7e9/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/57e785c679806f002f4ac7e9)

# dropwizard-hikaricp
This library provides a [HikariCP](https://github.com/brettwooldridge/HikariCP) integration for dropwizard, instead of using tomcat connection pool. It replaces the `dropwizard-hibernate` and `dropwizard-db` package, by overriding the classes `DataSourceFactory` and `ManagedPooledDataSource`.

`DataSourceFactory` overrides the original class by building a `HikariConfig` object, which is passed to the `ManagedPooledDataSource`. `ManagedPooledDataSource` extends `HikariDataSource`, instead of Tomcat's `DataSourceProxy`. It should work with existing `dropwizard-hibernate` settings, except for transaction isolation.

These are the available transaction isolation values. If not sure, leave it unset so it uses the default one.

- TRANSACTION\_NONE
- TRANSACTION\_READ\_UNCOMMITTED
- TRANSACTION\_READ\_COMMITTED
- TRANSACTION\_REPEATABLE\_READ
- TRANSACTION\_SERIALIZABLE

These are the supported versions of dropwizard:

| Dropwizard  |  Dropwizard-hikaricp | HikariCP |
|---|---|---|
| 1.0.2  | 1.0.2  | 2.4.4 |
| 1.0.3  | 1.0.3  | 2.5.1 |
| 1.3.1  | 1.3.1  | 3.0.0 |
| 1.3.8  | 1.3.8  | 3.3.0 |

## Maven

The library is available at the maven central, so just add dependency to `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>com.github.mtakaki</groupId>
    <artifactId>dropwizard-hikaricp</artifactId>
    <version>1.3.8</version>
  </dependency>
</dependencies>
```

If the bundle is actually using HikariCP, you should be able to see this right in the beginning of the logs:

```
INFO  [2016-03-14 06:32:06,681] org.eclipse.jetty.util.log: Logging initialized @1894ms
INFO  [2016-03-14 06:32:08,675] com.zaxxer.hikari.HikariDataSource: hibernate - Starting...
```

If you **don't** see it, it means it's not using HikariCP.

## Benchmark

Before looking into the benchmark numbers, I recommend reading [HikariCP wiki](https://github.com/brettwooldridge/HikariCP/wiki/%22My-benchmark-doesn't-show-a-difference.%22) explaining the differences between Hikari and Tomcat. There's also an extensive benchmarking produced by [Nick Babcock](https://nbsoftsolutions.com/blog/the-difficulty-of-performance-evaluation-of-hikaricp-in-dropwizard), in which he talks about these results I've found, plus some very comprehensive tests. 

The benchmark was ran on these specs:

- 1.4 GHz Intel Core i5 CPU
- Connection pool:
    - minimum size: 2
    - maximum size: 5
- 5000 requests
- 50 parallel clients

TomcatCP was used as it is and no additional setting was used to match to HikariCP's reliability.

### Querying

#### Get single entry

| Percentile | Tomcat (ms) | HikariCP (ms) |
|---|---:|---:|
| p50 | 2.364685 | 2.353321 |
| p75 | 4.830371 | 4.374164 |
| p95 | 21.761829 | 14.106643 |
| p98 | 150.097517 | 188.824843 |
| p99 | 197.370100 | 200.131246 |
| p999 | 317.904803 | 309.082305 |

#### Get multiple entries (5,000 records)

| Percentile | Tomcat (ms) | HikariCP (ms) |
|---|---:|---:|
| p50 | 27.840070 | 240.507857 |
| p75 | 52.545500 | 852.952037 |
| p95 | 251.368732 | 2,008.345248 |
| p98 | 318.400576 | 2,420.165156 |
| p99 | 355.222567 | 2,585.712459 |
| p999 | 603.264130 | 3,212.936003 |

### Inserting

| Percentile | Tomcat (ms) | HikariCP (ms) |
|---|---:|---:|
| p50 | 2.791224 | 89.453760 |
| p75 | 6.254607 | 218.588807 |
| p95 | 21.253595 | 350.772996 |
| p98 | 119.653682 | 420.798200 |
| p99 | 135.083573 | 489.772740 |
| p999 | 179.969553 | 555.170417 |

### Updating

| Percentile | Tomcat (ms) | HikariCP (ms) |
|---|---:|---:|
| p50 | 8.727674 | 11.725880 |
| p75 | 17.630370 | 38.890317 |
| p95 | 67.179430 | 290.270407 |
| p98 | 107.325624 | 338.387440 |
| p99 | 132.082066 | 399.467055 |
| p999 | 162.513912 | 542.977120 |

### Deleting

| Percentile | Tomcat (ms) | HikariCP (ms) |
|---|---:|---:|
| p50 | 10.856938 | 190.813232 |
| p75 | 18.364517 | 265.779441 |
| p95 | 80.736182 | 422.216480 |
| p98 | 117.330967 | 552.820746 |
| p99 | 136.620702 | 659.247842 |
| p999 | 162.919074 | 1,030.488379 |
